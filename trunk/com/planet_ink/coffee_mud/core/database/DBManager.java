package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.DVector.D2Vector;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.io.*;

/*
TODO IMMEDIATELY: CMSavable/Ownable objects!
Prepend both datas (fixed and var) copied into the subdata with a byte.
When loading, if the byte is 0, this is fixed data. If the byte is 1, this is var data. If 2, it is all data (saved with save() instead of fixedSave and varSave)
*/

/*
Need to delete all data in the new slot when moving from an old backup slot to a new backup slot
Need to load all objects on bootup. How will loading work?

Save object details:
Get CMSavable's ID, get files associated with it
	2 data files for fixed and variable data
	Format file for how the data fits in the fixed and variable data
		File? Probably don't need a whole FILE for it... Just embed it in the subdatabase file
		Generate this if it doesn't exist already
			Needs to know size and name for each thisEnum. Names will be fixed at 3 chars, size is 1 int. So the format in subFile will be...
				1 Int(total size, not counting this), 1 Int(ID size), ? Char(ID), ? Enum[3 Char(name), 1 Int(size)]
					Save only the Enums that have a size, all others will be assumed to be 0 meaning variable width
					Check HashMap below to see if it exists runtime, if not generate both the map and write the data to disk.
				Data is saved and loaded in fixed according to the Enum data here
					Some kind of hashmap? HashMap<String,HashMap<String,Int>>. First String is CMSavable ID, second String is Enum name.
					Total size is calculated as sum(thisEnum sizes)+16; 16 being 4 4-byte ints, 1 for SID 3 for variable-length info. So:
						1 Int(SID), Enum[? Byte(data)], 1 Int(variable data start position), 1 Int(variable data capacity), 1 Int(variable data size)
							SID of 0 is invalid, in file means unused.
				Variable data is mushed together and saved:
					Enum[3 Char(name), 1 Int(size), ? Byte(data)]
			CMSavable Enums need a size field and their save return should be a ByteBuffer, void load takes a ByteBuffer
	SID will be the permanent inter-object reference.

These are loaded in RAM and never stored to file. Instead they are rebuilt from scratch on each bootup.
	HashMap<SID, FileIndex> fileMapping - part of SaveFormat
	WVector<Integer> freeSpaces - part of SaveFormat I guess

Note: subTypes only contains things with variable sizes. enums contains ALL subTypes AND fixed-size things, but not purely-variable-size things

sub.bin contains the saved list of formats for the database to parse fixed data. Only written once per type per database.
	Total Size (4 bytes), Name size (4 bytes), Name (X bytes),
	Subobjects [X times (1 byte name size, 3 bytes type, X bytes name)], end of subobjects marker (1 byte 00), 
	Own fixed objects [X times (3 bytes type, 4 bytes size)]
*/


public class DBManager implements CMLibrary, Runnable	//extends Thread
{
	//Easily modifiable settings
	public static final String saveDirectoryName="resources"+File.separator+"database"+File.separator;	//Note: If you change this you must make the new directory manually!
	public static final String fixedExtensionName=".fix";	//1 per CMSavable ID per backup. Holds fixed width data, defined in subFile
	public static final String variableExtensionName=".var";	//1 per CMSavable ID per backup. Holds variable width data
	public static final String mainFileName="main.bin";	//1. Holds general backup details
	public static final String subFileName="sub.bin";	//1 per backup. Holds fixed width data
	public static final int timeTillDoFirst=120000;
	public static final int timeTillDoLast=30000;
	public static final int numBackups=0;
	public static final long waitInterval=timeTillDoLast/2+2000;
//	public static final long finalWaitInterval=1000;
	public static final java.nio.charset.Charset charFormat=java.nio.charset.Charset.forName("ISO-8859-1");	//This should probably not be changed. Mainly, 1-byte chars and ASCII printable char equivalent will be assumed.

	//Internal variables
//	private static Thread myThread;
	private static int currentBackup=0;	//0-(numBackups)
	private static int previousBackup=0;
	private static File mainFile;
	private static File subFile;
	private static MyList updateList=new MyList();
	private static HashSet<CMSavable> publicQueue=new HashSet<>();
	private static HashSet<CMSavable> processingQueue=new HashSet<>();
	private static HashSet<CMSavable> deleteQueue=new HashSet<>();
	private static HashSet<CMSavable> processingDelQueue=new HashSet<>();
	private static long timeOfNextSoonest;
	private static HashMap<String,SaveFormat> formats=new HashMap();
	private static boolean doneLoading=false;
	private static boolean finishUp=false;

	private static class MyList
	{
		private HashMap<CMSavable,ListNode> set;
		public ListNode firstFirst=null;	//These are public for convenience, but should NOT be written to outside of MyList!
		public ListNode firstLast=null;
		private ListNode lastFirst=null;
		private ListNode lastLast=null;

		public MyList(){set=new HashMap<>();}
		public MyList(int size){set=new HashMap<>(size);}

//		public ListNode firstFirst(){return firstFirst;}
//		public ListNode firstLast(){return firstLast;}
		public void add(CMSavable object)	//Make this return true if the first was changed?
		{
			if((object==null)||(object.amDestroyed())||(object.saveNum()==0)) return;
			
			ListNode newNode=set.get(object);
			if(newNode!=null)
			{
				newNode.lastRequest=System.currentTimeMillis();
				if(lastLast==newNode) return;
				newNode.nextLast.previousLast=newNode.previousLast;
				if(firstLast!=newNode)
					newNode.previousLast.nextLast=newNode.nextLast;
				else
					firstLast=newNode.nextLast;
			}
			else
			{
				newNode=new ListNode(object);
				set.put(object,newNode);
				if(firstFirst==null)
				{
					//Special case: This is the first object in the list. Grab all spots and return.
					firstFirst=newNode;
					lastFirst=newNode;
					firstLast=newNode;
					lastLast=newNode;
					return;
				}
				lastFirst.nextFirst=newNode;
				newNode.previousFirst=lastFirst;
				lastFirst=newNode;
			}
			lastLast.nextLast=newNode;
			newNode.previousLast=lastLast;
			lastLast=newNode;
		}
		public CMSavable removeFirst()
		{
			CMSavable O=firstFirst.O;
			set.remove(O);
			if(firstFirst.nextFirst==null)
			{
				firstFirst=null;
				firstLast=null;
				lastFirst=null;
				lastLast=null;
				return O;
			}
			if(firstFirst.previousLast!=null)
				firstFirst.previousLast.nextLast=firstFirst.nextLast;
			else
				firstLast=firstFirst.nextLast;
			if(firstFirst.nextLast!=null)
				firstFirst.nextLast.previousLast=firstFirst.previousLast;
			else
				lastLast=firstFirst.previousLast;

			firstFirst.nextFirst.previousFirst=null;
			firstFirst=firstFirst.nextFirst;
			return O;
		}
		public CMSavable removeLast()
		{
			CMSavable O=firstLast.O;
			set.remove(O);
			if(firstLast.nextLast==null)
			{
				firstFirst=null;
				firstLast=null;
				lastFirst=null;
				lastLast=null;
				return O;
			}
			if(firstLast.previousFirst!=null)
				firstLast.previousFirst.nextFirst=firstLast.nextFirst;
			else
				firstFirst=firstLast.nextFirst;
			if(firstLast.nextFirst!=null)
				firstLast.nextFirst.previousFirst=firstLast.previousFirst;
			else
				lastFirst=firstLast.previousFirst;

			firstLast.nextLast.previousLast=null;
			firstLast=firstLast.nextLast;
			return O;
		}

		public void remove(CMSavable object)
		{
			ListNode oldEntry=set.remove(object);
			if(oldEntry!=null)
			{
				if(oldEntry.previousFirst!=null)
					oldEntry.previousFirst.nextFirst=oldEntry.nextFirst;
				else
					firstFirst=oldEntry.nextFirst;
				if(oldEntry.nextFirst!=null)
					oldEntry.nextFirst.previousFirst=oldEntry.previousFirst;
				else
					lastFirst=oldEntry.previousFirst;
				
				if(oldEntry.previousLast!=null)
					oldEntry.previousLast.nextLast=oldEntry.nextLast;
				else
					firstLast=oldEntry.nextLast;
				if(oldEntry.nextLast!=null)
					oldEntry.nextLast.previousLast=oldEntry.previousLast;
				else
					lastLast=oldEntry.previousLast;
			}
		}
//		public boolean contains(CMSavable object) { return set.containsKey(object); }
	}
	private static class ListNode
	{
		public ListNode nextFirst=null;
		public ListNode nextLast=null;
		public ListNode previousFirst=null;
		public ListNode previousLast=null;
		public final long firstRequest=System.currentTimeMillis();
		public long lastRequest=System.currentTimeMillis();
		public CMSavable O;
		
		public ListNode(CMSavable O) { this.O=O; }
		public boolean equals(Object O){return (O instanceof ListNode)?((ListNode)O).O==this.O:false;}
	}
	public static class SaveFormat
	{
		//One-time generated data
		public final WVector<CMSavable.SaveEnum> enums;  //Contains all enums with size -1 or fixed size
		public final D2Vector<CMSavable.SaveEnum,String> subTypes;  //Contains all enums with size -1
		public final CMSavable myObject;
		public final int size;	//counting the extra 4 Ints, aka ^ + 16
		public final File fixedData;
		public final File variableData;
		public final boolean confirmedGood;
		//Runtime modifiable data
			//These first three need to be populated by the load function!
		public HashMap<SimpleInt, SimpleInt> fileMap=new HashMap<>();	//get(SID), return fileIndex, for fixedData
		public WVector<SimpleInt> freeSpaces=new WVector<>();
			//entry is position(unique!), weight is size(num bytes available), for variableData.
			//First entry is always end-of-KNOWN-data and is 0 weight! After loading, data past this point may be overwritten!
		public ArrayList<SimpleInt> freeEntries=new ArrayList<>();	//Values not in fileMap. Again, first entry is end-of-known-data.
		protected FileChannel fixedChannel;
		protected FileChannel variableChannel;
		public SaveFormat(CMSavable o, WVector<CMSavable.SaveEnum> h, int size, D2Vector<CMSavable.SaveEnum,String> s, boolean c, File[] f)
		{
			myObject=o;
			enums=h;
			subTypes=s;
			this.size=size;
			fixedData=f[0];
			variableData=f[1];
			confirmedGood=c;
			freeEntries.add(new SimpleInt(0));
			freeSpaces.add(new SimpleInt(0),0);
		}
		public SaveFormat(CMSavable o, WVector<CMSavable.SaveEnum> h, D2Vector<CMSavable.SaveEnum,String> s, boolean c, File[] f)
		{
			this(o, h, h.weight()+16, s, c, f);
		}
		public FileChannel fChan()
		{
			if((fixedData!=null)&&((fixedChannel==null)||(!fixedChannel.isOpen())))
				try{ fixedChannel=new RandomAccessFile(fixedData, "rw").getChannel(); }
				catch(Exception e){Log.errOut("DBManager",e); return null;}
			return fixedChannel;
		}
		public FileChannel vChan()
		{
			if((variableData!=null)&&((variableChannel==null)||(!variableChannel.isOpen())))
				try{ variableChannel=new RandomAccessFile(variableData, "rw").getChannel(); }
				catch(Exception e){Log.errOut("DBManager",e); return null;}
			return variableChannel;
		}
		public void claimFreeEntry(int i)
		{
			SimpleInt old=freeEntries.get(0);
			if(i==old.Int)
				old.Int++;
			else if(i>old.Int)
			{
				while(i>old.Int)
				{
					freeEntries.add(new SimpleInt(old.Int));
					old.Int++;
				}
				old.Int++;
			}
			else
				Log.errOut("DBManager", new RuntimeException("claimFreeEntry does not support values below current last value!"));
		}
		public int getFreeEntry()
		{
			if(freeEntries.size()>1) return freeEntries.remove(freeEntries.size()-1).Int;
			return freeEntries.get(0).Int++;
		}
		public void addFreeEntry(int i)
		{
			freeEntries.add(new SimpleInt(i));
		}
		protected int getPos(int position)
		{
			int start=1;	//first is end of file, ignore it!
			int numEntries=freeSpaces.size();
			int end=numEntries;
			while(start!=end)
			{
				int mid=(start+end-1)/2;
				if(freeSpaces.get(mid).Int-position>0)
					end=mid;
				else
					start=mid+1;
			}
			return start;
		}
		public void claimSpace(int position, int size)
		{
			int extraSpace=freeSpaces.get(0).Int-position;
			if(extraSpace>=0)
			{
				freeSpaces.get(0).Int=position+size;
				//No need to check for overlap from a previous free space, it would have been swallowed by 0 if it existed.
				if(extraSpace>0)
					freeSpaces.add(new SimpleInt(position), extraSpace);
			}
			int start=getPos(position)-1;	//start should contain the index of the free space that contains the space claimed by this
//			boolean reachesStart=(freeSpaces.get(start).Int==position);
			boolean reachesEnd=(freeSpaces.get(start).Int+freeSpaces.weight(start)==position+size);
			if(freeSpaces.get(start).Int==position)
			{
				if(reachesEnd)
					freeSpaces.remove(start);
				else
				{
					freeSpaces.get(start).Int=position;
					freeSpaces.setWeight(start, freeSpaces.weight(start)-size);
				}
			}
			else
			{
				if(reachesEnd)
					freeSpaces.setWeight(start, freeSpaces.weight(start)-size);
				else
				{
					freeSpaces.insert(start+1, new SimpleInt(position+size), freeSpaces.weight(start)+freeSpaces.get(start).Int-(size+position));
					freeSpaces.setWeight(start, position-freeSpaces.get(start).Int);
				}
			}
		}
		public void returnSpace(int position, int size)
		{
			int start=getPos(position);
			int numEntries=freeSpaces.size();
			int prevSize=freeSpaces.weight(start-1);
			boolean overlapsPrevious=(freeSpaces.get(start-1).Int+prevSize==position);
			if(start==numEntries)
			{
				boolean endOfFile=(position+size==freeSpaces.get(0).Int);
				if(overlapsPrevious)
				{
					if(endOfFile)
						freeSpaces.get(0).Int=freeSpaces.remove(start-1).Int;
					else
						freeSpaces.setWeight(start-1, prevSize+size);
				}
				else if(endOfFile)
					freeSpaces.get(0).Int=position;
				else
					freeSpaces.add(new SimpleInt(position), size);
			}
			else
			{
				boolean overlapsNext=(freeSpaces.get(start).Int==position+size);
				if(overlapsPrevious)
				{
					if(overlapsNext)
					{
						freeSpaces.setWeight(start-1,prevSize+size+freeSpaces.weight(start));
						freeSpaces.remove(start);
					}
					else
						freeSpaces.setWeight(start-1, prevSize+size);
				}
				else if(overlapsNext)
				{
					freeSpaces.get(start).Int=position;
					freeSpaces.setWeight(start, size+freeSpaces.weight(start));
				}
				else
					freeSpaces.insert(start, new SimpleInt(position), size);
			}
		}
		public int[] getFreeSpace(int size)
		{
			int[] results=new int[2];
			if(freeSpaces.weight(freeSpaces.largestWeight())>=size)
				for(int i=1;i<freeSpaces.size();i++)
					if(freeSpaces.weight(i)>=size)
					{
						results[0]=freeSpaces.get(i).Int;
						results[1]=freeSpaces.weight(i);
						if(results[1]>=size*2)
						{
							freeSpaces.get(i).Int=results[0]+size;
							freeSpaces.setWeight(i, results[1]-size);
							results[1]=size;
						}
						else
							freeSpaces.remove(i);
						return results;
					}
			results[0]=freeSpaces.get(0).Int;
			results[1]=size;
			freeSpaces.get(0).Int+=size;
			return results;
		}
		public ByteBuffer[] getFullFixedVals(CMSavable O)
		{
			int saveNum=O.saveNum();
			ByteBuffer[] fixedVals=new ByteBuffer[enums.size()+1];
			fixedVals[0]=(ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(saveNum).rewind();
			for(CMSavable.SaveEnum thisEnum : O.totalEnumS())
			{
				int index=enums.index(thisEnum)+1;
				if(index==0) continue;
				if(thisEnum.size()==-1)
				{
					CMSavable sub=thisEnum.subObject(O);
					int subindex=subTypes.indexOf(thisEnum);
					if((sub!=null)&&(subindex>=0)&&(subTypes.get(subindex,1)==sub.ID()))	//Safe because intern()'d when loading
						fixedVals[index]=DBManager.getFormat(sub).getPartFixedVals(sub);
					else
						fixedVals[index]=ByteBuffer.wrap(new byte[enums.weight(index-1)]);
				}
				else
					fixedVals[index]=thisEnum.save(O);
			}
			//IN CASE THERE ARE MISSING FIXEDVALS. Ideally this code shouldn't actually be necessary though...
			//if(!confirmedGood)
			for(int i=1;i<fixedVals.length;i++)
				if((fixedVals[i]==null)||(fixedVals[i].remaining()!=enums.weight(i-1)))
					fixedVals[i]=ByteBuffer.wrap(new byte[enums.weight(i-1)]);
			return fixedVals;
		}
		public ByteBuffer getPartFixedVals(CMSavable O)
		{
			//int saveNum=O.saveNum();
			ByteBuffer[] fixedVals=new ByteBuffer[enums.size()];
			for(CMSavable.SaveEnum thisEnum : O.totalEnumS())
			{
				int index=enums.index(thisEnum);
				if(index==-1) continue;
				if(thisEnum.size()==-1)
				{
					CMSavable sub=thisEnum.subObject(O);
					int subindex=subTypes.indexOf(thisEnum);
					if((sub!=null)&&(subindex>=0)&&(subTypes.get(subindex,1)==sub.ID()))	//Safe because intern()'d when loading
						fixedVals[index]=DBManager.getFormat(sub).getPartFixedVals(sub);
					else
						fixedVals[index]=ByteBuffer.wrap(new byte[enums.weight(index)]);
				}
				else
					fixedVals[index]=thisEnum.save(O);
			}
			//IN CASE THERE ARE MISSING FIXEDVALS. Ideally this code shouldn't actually be necessary though...
			//if(!confirmedGood)
			for(int i=0;i<fixedVals.length;i++)
				if((fixedVals[i]==null)||(fixedVals[i].remaining()!=enums.weight(i)))
					fixedVals[i]=ByteBuffer.wrap(new byte[enums.weight(i)]);
			ByteBuffer partVal=ByteBuffer.wrap(new byte[size-15]);	//size meaning this format's size
			partVal.put((byte)3);
			for(int i=0;i<fixedVals.length;i++)
				partVal.put(fixedVals[i].array());
			partVal.rewind();
			return partVal;
		}
		public ByteBuffer[] getVarVals(CMSavable O, int[] totalVarSize)
		{
			ArrayList<ByteBuffer> variableVals=new ArrayList();
			for(CMSavable.SaveEnum thisEnum : O.totalEnumS())
			{
				int index=enums.index(thisEnum);
				if(index==-1)
				{
					ByteBuffer buf=thisEnum.save(O);
					int size=buf.remaining();
					if(size==0) continue;
					variableVals.add(charFormat.encode(thisEnum.name()));
					variableVals.add((ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(size).rewind());
					variableVals.add(buf);
					totalVarSize[0]+=size+7;
				}
				else if(thisEnum.size()==-1)
				{
					CMSavable sub=thisEnum.subObject(O);
					if(sub==null) continue;
					ByteBuffer buf;
					int subindex=subTypes.indexOf(thisEnum);
					if((subindex>=0)&&(subTypes.get(subindex,1)==sub.ID()))
					{
						int vectorPosition=variableVals.size();
						variableVals.add(null);
						variableVals.add(null);
						int size=getFormat(sub).getSubVarVal(sub, variableVals);
						if(size>1)	//if <=, nothing added, move on)
						{
							variableVals.set(vectorPosition, charFormat.encode(thisEnum.name()));
							variableVals.set(vectorPosition+1, (ByteBuffer)ByteBuffer.wrap(new byte[5]).putInt(size).put((byte)1).rewind());
							totalVarSize[0]+=7+size;
						}
						else
						{
							variableVals.remove(vectorPosition+1);
							variableVals.remove(vectorPosition);
						}
						continue;
					} //else. When does this happen? It shouldn't I don't think.
					buf=thisEnum.save(O);
					int size=buf.remaining();
					if(size<=1) continue;	//1 because first byte is (should be) 2 to mark it as fulldata
					variableVals.add(charFormat.encode(thisEnum.name()));
					variableVals.add((ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(size).rewind());
					variableVals.add(buf);
					totalVarSize[0]+=size+7;
				}
			}
			return (ByteBuffer[])variableVals.toArray(new ByteBuffer[variableVals.size()]);
		}
		public int getSubVarVal(CMSavable O, ArrayList<ByteBuffer> variableVals)
		{
			int totalSize=1;
			for(CMSavable.SaveEnum thisEnum : O.totalEnumS())
			{
				int index=enums.index(thisEnum);
				if(index==-1)
				{
					ByteBuffer buf=thisEnum.save(O);
					int thisSize=buf.remaining();
					if(thisSize==0) continue;
					variableVals.add(charFormat.encode(thisEnum.name()));
					variableVals.add((ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(thisSize).rewind());
					variableVals.add(buf);
					totalSize+=7+thisSize;
				}
				else if(thisEnum.size()==-1)
				{
					CMSavable sub=thisEnum.subObject(O);
					if(sub==null) continue;
					ByteBuffer buf;
					int subindex=subTypes.indexOf(thisEnum);
					if((subindex>=0)&&(subTypes.get(subindex,1)==sub.ID()))
					{
						int vectorPosition=variableVals.size();
						variableVals.add(null);
						variableVals.add(null);
						int size=getFormat(sub).getSubVarVal(sub, variableVals);
						if(size>1)	//if <=, nothing added, move on)
						{
							variableVals.set(vectorPosition, charFormat.encode(thisEnum.name()));
							variableVals.set(vectorPosition+1, (ByteBuffer)ByteBuffer.wrap(new byte[5]).putInt(size).put((byte)1).rewind());
							totalSize+=7+size;
						}
						else
						{
							variableVals.remove(vectorPosition+1);
							variableVals.remove(vectorPosition);
						}
						continue;
					}
					buf=thisEnum.save(O);
					int size=buf.remaining();
					if(size<=1) continue;	//1 because first byte is (should be) 2 to mark it as fulldata
					variableVals.add(charFormat.encode(thisEnum.name()));
					variableVals.add((ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(size).rewind());
					variableVals.add(buf);
					totalSize+=size+7;
				}
			}
			return totalSize;
		}
	}
	//Mutable Integer, basically
	private static class SimpleInt implements Comparable<SimpleInt>
	{
		public int Int=0;
		public SimpleInt(){}
		public SimpleInt(int i){Int=i;}
		@Override public int compareTo(SimpleInt other){return Int-other.Int;}
		@Override public boolean equals(Object other){return ((other instanceof SimpleInt)&&(((SimpleInt)other).Int==Int)); }
		@Override public int hashCode(){return Int;}
	}

	public DBManager()
	{
//		super("DBManagerThread");
	}

	//Internal code
	@Override public String ID(){return "DBManager";}
	@Override public CMObject newInstance(){return this;}
	@Override public void initializeClass(){}
	@Override public void finalInitialize(){}
	@Override public CMObject copyOf(){return this;}
	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	@Override public boolean activate()
	{
//		start();
		new Thread(this, "DBManagerThread").start();
		return true;
	}
	@Override public boolean shutdown()
	{
		finishUp=true;
//		myThread.interrupt();
		synchronized(this) { notify(); }
		return true;
	}
	@Override public void propertiesLoaded(){}
	@Override public SupportThread getSupportThread() { return null;}
	public static File getMakeFile(String name)
	{
		File f=new File(name);
		if(!f.exists())
		try{ f.createNewFile(); }
		catch(IOException e){Log.errOut("Database","Error making file: "+e.toString()); return null;}
		return f;
	}
	public static File[] getObjectFiles(CMObject E)
	{
		File[] files=new File[2];
//		if(!(E instanceof Ownable))	//TODO: Would like a check of some sort here to not make unnecessary files!
		{
			files[0]=getMakeFile(saveDirectoryName+currentBackup+File.separator+E.ID()+fixedExtensionName);
			files[1]=getMakeFile(saveDirectoryName+currentBackup+File.separator+E.ID()+variableExtensionName);
		}
		return files;
	}
	public static File mainFile()
	{
		if(mainFile==null)
		{
			File f=new File(saveDirectoryName+mainFileName);
			if(!f.exists())
			{
				byte[] buffer=new byte[2];	//Size of file
				buffer[0]=0;	//First byte of file is current DB to use; default to 0
				buffer[1]=0;	//Second byte of file is currently running (system crash detection); default to false(0)
				ByteBuffer wrap=ByteBuffer.wrap(buffer);
				
				try{
					FileChannel channel=new RandomAccessFile(f, "rw").getChannel();
					channel.write(wrap); }
				catch(IOException e){Log.errOut("DBManager",e); return null;}
			}
			mainFile=f;
		}
		return mainFile;
	}
	public static File subFile()
	{
		if(subFile==null)
		{
			File f=new File(saveDirectoryName+currentBackup+File.separator+subFileName);
			if(!f.exists())
			try{ f.createNewFile(); }
			catch(IOException e){Log.errOut("DBManager",e); return null;}
			subFile=f;
		}
		return subFile;
	}
	public void saveObject(CMSavable O) { publicQueue.add(O); }
	public void deleteObject(CMSavable O) { deleteQueue.add(O); }
	public boolean doneLoading(){return doneLoading;}
	@Override public void run()
	{
//		myThread=Thread.currentThread();
		try{
			{
				File mainDir=new File(saveDirectoryName);
				if(!mainDir.exists())
					mainDir.mkdirs();
			}
			ByteBuffer mainInfo=ByteBuffer.wrap(new byte[2]);
			FileChannel input=new RandomAccessFile(mainFile(), "rw").getChannel();
			input.read(mainInfo);
			input.close();
			previousBackup=mainInfo.get(0);
			currentBackup=previousBackup;
			if(mainInfo.get(1)==1)
				Log.errOut("DBManager","Warning: MUD was not properly shutdown, data may be corrupted!");
				//TODO: Increment currentBackup here. Maybe also an option to not and stick to older data?
			{
				File newDir=new File(saveDirectoryName+currentBackup);
				if(!newDir.exists())
					newDir.mkdirs();
			}
			input=new RandomAccessFile(subFile(), "rw").getChannel();
			ByteBuffer formatInfo=ByteBuffer.wrap(new byte[(int)input.size()]);
			input.read(formatInfo);
			input.close();
			subFile=null;
			formatInfo.rewind();
			while(formatInfo.remaining()>0)
			{
				int end=formatInfo.getInt()+formatInfo.position();
//				ByteBuffer thisData=formatInfo.slice();
//				thisData.limit(size);
//				formatInfo.position(formatInfo.position()+size);
				String ID;
				{
					int IDSize=formatInfo.getInt();
					byte[] temp=new byte[IDSize];
					formatInfo.get(temp);
					ID=new String(temp, charFormat);
				}
				CMSavable obj=(CMSavable)CMClass.getClass(ID);
				if(obj==null)
				{
					Log.errOut("DBManager","Class not found, database data useless! "+ID);
					formatInfo.position(end);
					continue;
				}
				boolean goodFormat=true;
				Enum[] options=obj.headerEnumS();
				byte[] type=new byte[3];
				CMSavable.SaveEnum thisEnum;
				D2Vector<CMSavable.SaveEnum, String> subTypes=new D2Vector<>();
				{
					int subLength;
					while((subLength=formatInfo.get())!=0)
					{
						formatInfo.get(type);
						String option=new String(type, charFormat);
						thisEnum=null;
						for(int j=0;(j<options.length)&&(thisEnum==null);j++)
							thisEnum=getParser(options[j], option);
						if(thisEnum==null) {goodFormat=false; continue;}
						byte[] temp=new byte[subLength];
						formatInfo.get(temp);
						subTypes.put(thisEnum, new String(temp, charFormat).intern());	//intern() it to make it == ID()
					}
				}
//				int totalSize=16;
				WVector<CMSavable.SaveEnum> fixedVars=new WVector();
				{
					for(int i=0;formatInfo.position()<end;i++)
					{
						int size=formatInfo.get(type).getInt();
						String option=new String(type, charFormat);
//						totalSize+=size;
						thisEnum=null;
						for(Enum en : options)
							if((thisEnum=getParser(en, option))!=null) break;
						//On second thought, subformats will always have been saved first already, as attempting to make a new format
						//has to access(make if necessary) all its subformats first
						//On third thought, there is a possible exception. What if something wasn't a subformat before but was later made one?
						//That's probably enough of an excuse to require a db reboot or something...
						//On fourth thought, does subformat matter at this point in the code?
						//Here's a case subformat matters: Marked as subformat in code, not in DB
						if((thisEnum==null)||
						  ((thisEnum.size()==-1)&&(!subTypes.contains(thisEnum)))||
						  (thisEnum.size()!=size))
							goodFormat=false;
						//Still try to load it! Bad entries will be disabled later
						fixedVars.add(thisEnum, size);
					}
				}
				SaveFormat f=new SaveFormat(obj, fixedVars, subTypes, goodFormat, getObjectFiles(obj));	//Don't need totalSize I don't think..
				formats.put(ID, f);
			}
			byte[] enumName=new byte[3];
			for(SaveFormat format : formats.values().toArray(new SaveFormat[0]))
			{
				if(format.fixedData==null) continue;

				CMSavable.SaveEnum[] parsers=new CMSavable.SaveEnum[format.enums.size()];
				int[] weights=new int[parsers.length];
				format.enums.toArrays(parsers, weights);
				Enum[] options=format.myObject.headerEnumS();

				FileChannel channel=format.fChan();
				ByteBuffer fbuf=ByteBuffer.wrap(new byte[(int)channel.size()]);
				channel.read(fbuf);
				fbuf.rewind();
				channel=format.vChan();
				ByteBuffer vbuf=ByteBuffer.wrap(new byte[(int)channel.size()]);
				channel.read(vbuf);
				vbuf.rewind();
				int numEntries=fbuf.remaining()/format.size;
				byte[] thisData=new byte[format.size];
				ByteBuffer thisBuf=ByteBuffer.wrap(thisData);
				int saveNum=0,position=0,cap=0,varSize=0;
				for(int fileIndex=0;fileIndex<numEntries;fileIndex++)
				try {
					thisBuf.rewind();
					fbuf.get(thisData);
					saveNum=thisBuf.getInt();
					if(saveNum==0) continue;	//Deleted entry, skip
					format.fileMap.put(new SimpleInt(saveNum), new SimpleInt(fileIndex));
					format.claimFreeEntry(fileIndex);
					CMSavable thisObj=format.myObject.newInstance();
					thisObj.setSaveNum(saveNum);	//This will also register the object with its associated SID library.
					for(int parserNum=0;parserNum<parsers.length;parserNum++)
					try {
						if(parsers[parserNum]==null)
						{
							thisBuf.position(thisBuf.position()+weights[parserNum]);
							continue;
						}
						ByteBuffer saveBuffer=thisBuf.slice();
						thisBuf.position(thisBuf.position()+weights[parserNum]);
						saveBuffer.limit(weights[parserNum]);
						try{ parsers[parserNum].load(thisObj, saveBuffer); }
						catch(Exception exc){
							Log.errOut("Database",format.myObject.getClass().getName()+" FI:"+fileIndex+" SID:"+saveNum+" "+parsers[parserNum].name());
							Log.errOut("Database",exc);
						}
					} catch(Exception exc){
						Log.errOut("Database",format.myObject.getClass().getName()+" FI:"+fileIndex+" SID:"+saveNum+" "+parsers[parserNum].name());
						throw exc;
					}
					position=thisBuf.getInt();
					cap=thisBuf.getInt();	//Needed for space claiming
					varSize=thisBuf.getInt();
					if(cap==0) continue;	//Done with this object, goto next
					format.claimSpace(position, cap);
					if(varSize==0) continue;
					ByteBuffer varData=((ByteBuffer)vbuf.position(position)).slice();
					varData.limit(varSize);
					while(varData.remaining()>0)
					{
						varData.get(enumName);
						String option=new String(enumName, charFormat);
						CMSavable.SaveEnum thisEnum=null;
						for(int j=0;(j<options.length)&&(thisEnum==null);j++)
							thisEnum=getParser(options[j], option);
						int size=varData.getInt();
						//try{
							if(thisEnum==null)
							{
								varData.position(varData.position()+size);
								continue;
							}
							ByteBuffer saveBuffer=varData.slice();
							varData.position(varData.position()+size);
							saveBuffer.limit(size);
							thisEnum.load(thisObj, saveBuffer);
						//}catch(IllegalArgumentException e){
						//	Log.errOut("DBManager","Hit cap for "+format.myObject.ID()+" "+option+" "+saveNum+": "+varData.position()+" "+size+" "+varData.limit());
						//	throw e; //varData.position(varData.limit());
						//}
					}
				}
				catch(Exception exc){
					Log.errOut("Database",format.myObject.getClass().getName()+" FI:"+fileIndex);
					Log.errOut("Database","Var data for SID:"+saveNum+" pos:"+position+" cap:"+cap+" used:"+varSize);
					throw exc;
				}
				if(format.confirmedGood) continue;
				for(int i=0;i<format.enums.size();i++)
				{
					CMSavable.SaveEnum thisEnum=format.enums.get(i);
					if((thisEnum!=null)&&
					   (((thisEnum.size()==-1)&&(format.subTypes.contains(thisEnum)))||
					    (thisEnum.size()!=format.enums.weight(i))))
						format.enums.replace(i, null, format.enums.weight(i));
				}
			}
//			SIDLib.linkSIDs();	//Should be done here, SIDLib doesn't know about classes
			for(SaveFormat format : formats.values().toArray(new SaveFormat[0]))
			{
				if(!format.myObject.needLink()) continue;	//Owner objects that do not need links themselves should check subobjects' needLink
				SIDLib.Objects type=SIDLib.getType(format.myObject);
				if(type==null) continue;	//Happens for subobjects that need links. Their owner object should call their link.
				for(Iterator<SimpleInt> ee=format.fileMap.keySet().iterator();ee.hasNext();)
				{
					int SID=ee.next().Int;
					try{
						CMSavable obj=type.get(SID);
						if(obj!=null) obj.link();
					}catch(Exception exc){
						Log.errOut("Database",format.myObject.getClass().getName()+" SID:"+SID);
						Log.errOut("Database", exc);
					}
				}
			}
			
			if(numBackups>0)
			{
				currentBackup++;
				if(currentBackup>numBackups) currentBackup=0;
				File newDir=new File(saveDirectoryName+currentBackup);
				newDir.mkdirs();
				File[] oldFiles=newDir.listFiles();
				if(oldFiles!=null)
				for(File oldFile : oldFiles)
					oldFile.delete();
				formats.clear();
				for(SIDLib.Objects type : SIDLib.Objects.values())
					type.save();
				HashSet tempQueueRef=publicQueue;
				publicQueue=processingQueue;
				processingQueue=tempQueueRef;
				for(Iterator<CMSavable> e=processingQueue.iterator();e.hasNext();)
				{ updateList.add(e.next()); }
				processingQueue.clear();
				while(updateList.firstFirst!=null)
					writeToFile(updateList.removeFirst());
			}
			mainInfo.put(1,(byte)1).rewind();
			input=new RandomAccessFile(mainFile(), "rw").getChannel();
			input.write(mainInfo);
			input.close();
		}
		catch(Exception e){Log.errOut("DBManager",e); doneLoading=true; return;}
		publicQueue.clear();	//Ignore requests to save that may have been triggered from booting the mud
		doneLoading=true;
		{
			HashSet tempQueueRef;
			while(!finishUp)
			{
				for (CMSavable O : processingDelQueue)
				{
					updateList.remove(O);
					processingQueue.remove(O);
					publicQueue.remove(O);
					clearFromFile(O);
				}
				processingDelQueue.clear();
				tempQueueRef=deleteQueue;	//Swap queues to prepare for next processing cycle
				deleteQueue=processingDelQueue;
				processingDelQueue=tempQueueRef;
				{
					CMSavable O;
					while((O=getNext())!=null)
						writeToFile(O);
				}
				
				for(Iterator<CMSavable> e=processingQueue.iterator();e.hasNext();)
				{ updateList.add(e.next()); }
				processingQueue.clear();
				tempQueueRef=publicQueue;	//Swap queues to prepare for next processing cycle
				publicQueue=processingQueue;
				processingQueue=tempQueueRef;
				try{ synchronized(this){wait(waitInterval);} } catch(InterruptedException e){Log.sysOut("DBManager","Interrupted!");}
			}
		}
		while((!processingQueue.isEmpty())||(!publicQueue.isEmpty()))
		{
			for(Iterator<CMSavable> e=processingQueue.iterator();e.hasNext();)
				updateList.add(e.next());
/*
			{
				CMSavable obj=e.next();
				Log.sysOut("DBStuff",obj.ID()+" "+obj.saveNum());
				updateList.add(obj);
			}
*/
			processingQueue.clear();
			HashSet tempQueueRef=publicQueue;
			publicQueue=processingQueue;
			processingQueue=tempQueueRef;
			while(updateList.firstFirst!=null)
				writeToFile(updateList.removeFirst());
//		if(finalWaitInterval>0)
//			try{ Thread.sleep(finalWaitInterval); } catch(Exception e){Log.errOut("DBManager",e);}
		}
//		for(Iterator<CMSavable> e=processingQueue.iterator();e.hasNext();)
//		{ updateList.add(e.next()); }
//		while(updateList.firstFirst!=null)
//			writeToFile(updateList.removeFirst());
		byte[] mainFlags=new byte[2];
		mainFlags[0]=(byte)currentBackup;
		mainFlags[1]=(byte)0;
		try (FileChannel output = new RandomAccessFile(mainFile(), "rw").getChannel()) {
			output.write(ByteBuffer.wrap(mainFlags));
		}
		catch(IOException e){Log.errOut("DBManager",e);}
		doneLoading=false;
	}
	public static CMSavable.SaveEnum getParser(Enum E, String S)
	{
		try{return (CMSavable.SaveEnum)Enum.valueOf((Class)E.getClass().getSuperclass(), S);}
		catch(IllegalArgumentException e){if(e.getMessage().startsWith("No")) return null;}
		try{return (CMSavable.SaveEnum)Enum.valueOf(E.getClass(), S);}
		catch(IllegalArgumentException e){}
		return null;
	}
	protected static CMSavable getNext()
	{
		long time=System.currentTimeMillis();
		if(timeOfNextSoonest>time) return null;
		if(updateList.firstFirst==null)
		{
			timeOfNextSoonest=0;
			return null;
		}
		timeOfNextSoonest=updateList.firstFirst.firstRequest+timeTillDoFirst;
		long tempTime=updateList.firstLast.lastRequest+timeTillDoLast;
		if(tempTime<timeOfNextSoonest)
		{
			timeOfNextSoonest=tempTime;
			if(timeOfNextSoonest<time)
				return updateList.removeLast();
		}
		else if(timeOfNextSoonest<time) return updateList.removeFirst();
		return null;
	}
	public static SaveFormat getFormat(CMSavable obj)
	{
		String ID=obj.ID();
		SaveFormat f=formats.get(ID);
		if(f==null)
		{
			CMClass.Objects type=CMClass.getType(obj);
			obj=(CMSavable)type.get(obj.ID());	//Get a clean instance with default types
			obj.prepDefault();	//Ask it to have the expected most-common values. Mainly setting up subobjects
			WVector<CMSavable.SaveEnum> fixedVars=new WVector();
			int totalSize=16;
			byte[] IDBytes=ID.getBytes(charFormat);
			int formatSize=5+IDBytes.length;	//5=SID+End-Of-SubObjects marker. formatSize itself is not counted.
			D2Vector<CMSavable.SaveEnum,String> subObjs=new D2Vector();
			for(CMSavable.SaveEnum thisEnum : obj.totalEnumS())
			{
				int i=thisEnum.size();
				if(i==0) continue;
				if(i==-1)
				{
					CMSavable subObject=thisEnum.subObject(obj);
					if(subObject==null) continue;
					String subObj=subObject.ID();
					i=getFormat(subObject).size-15;	//-16 for SID and var info, +1 for subdata 'header' byte
					if(i==1) continue;	//Don't write it if there is no fixed subdata.
					subObjs.put(thisEnum,subObj);
//					byte[] subIDBytes=subObj.ID().getBytes(charFormat);
					formatSize+=subObj.length()+4;
				}
				fixedVars.add(thisEnum, i);
				totalSize+=i;
				formatSize+=7;
			}
			f=new SaveFormat(obj, fixedVars, subObjs, true, getObjectFiles(obj));
			formats.put(ID, f);
			
			try{
				ByteBuffer fileFormat=ByteBuffer.wrap(new byte[formatSize+4]).putInt(formatSize).putInt(IDBytes.length).put(IDBytes);
				for (int i=0;i<subObjs.size();i++)
				{
					String str=(String)subObjs.get(i,1);
					CMSavable.SaveEnum enm=(CMSavable.SaveEnum)subObjs.get(i,0);
					fileFormat.put((byte)str.length()).put(enm.name().getBytes(charFormat)).put(str.getBytes(charFormat));
				}
				fileFormat.put((byte)0);	//End-of-subobjects marker
				for(int i=0;i<fixedVars.size();i++)
					fileFormat.put(fixedVars.get(i).name().getBytes(charFormat)).putInt(fixedVars.weight(i));
				FileChannel saveFormatChannel=new RandomAccessFile(subFile(), "rw").getChannel();
				saveFormatChannel.write((ByteBuffer)fileFormat.rewind(), saveFormatChannel.size());
			}
			catch(IOException e){Log.errOut("DBManager",e);}
		}
		return f;
	}
	protected static void clearFromFile(CMSavable O)
	{
		try
		{
			SaveFormat format=getFormat(O);
			int fileIndex;
			{
				SimpleInt fileIndexInt=format.fileMap.remove(new SimpleInt(O.saveNum()));
				if(fileIndexInt==null) return;
				fileIndex=fileIndexInt.Int;
				if(format.freeEntries.get(0).Int==fileIndex+1)
					format.freeEntries.get(0).Int--;
				else
					format.freeEntries.add(fileIndexInt);
			}
			FileChannel fChan=format.fChan();
			fChan.write(ByteBuffer.wrap(new byte[4]), fileIndex*format.size);	//Delete SID
			ByteBuffer varInfo=ByteBuffer.wrap(new byte[8]);
			fChan.read(varInfo, (fileIndex+1)*format.size-12);
			int position=varInfo.getInt(0);
			int size=varInfo.getInt(4);
			if(size>0) format.returnSpace(position, size);
			O.setSaveNum(0);
		}
		catch(Exception e){Log.errOut("DBManager",e);}
	}
	protected static void writeToFile(CMSavable O)
	{
		if(O.amDestroyed()) return;	//Quick safety check, convulated thread shenanigans MIGHT be able to cause this..
		try
		{
			SaveFormat format=getFormat(O);
			ByteBuffer[] fixedVals=format.getFullFixedVals(O);
			int[] totalVarSize=new int[1];
			ByteBuffer[] varVals=format.getVarVals(O, totalVarSize);	//stupid inability to pass-by-reference...
			int fileIndex;
			boolean newEntry=false;
			FileChannel fChan=format.fChan();
			{
				SimpleInt fileIndexInt=format.fileMap.get(new SimpleInt(O.saveNum()));
				if(fileIndexInt==null)
				{
					newEntry=true;
					fileIndex=format.getFreeEntry();
					format.fileMap.put(new SimpleInt(O.saveNum()), new SimpleInt(fileIndex));
				}
				else
					fileIndex=fileIndexInt.Int;
			}
			fChan.position(fileIndex*format.size);
			
			//NOTE: If something goes wrong somehow this may corrupt data!
			for(int buffWrite=0;buffWrite<fixedVals.length;buffWrite+=16) //Work around JRE bug
				fChan.write(fixedVals,buffWrite,(16>fixedVals.length-buffWrite)?(fixedVals.length-buffWrite):16);
			ByteBuffer input=ByteBuffer.wrap(new byte[12]);
			long varIntsPos=fChan.position();
			int position=0;
			int capacity=0;
			if(newEntry)
			{
				int[] placement=format.getFreeSpace(totalVarSize[0]*2);
				position=placement[0];
				capacity=placement[1];
			}
			else
			{
				fChan.read(input, varIntsPos);
				input.rewind();	//Prep it to write to channel
				position=input.getInt(0);
				capacity=input.getInt(4);
				if(totalVarSize[0]>capacity)
				{
					if(capacity>0)
						format.returnSpace(position,capacity);
					int[] placement=format.getFreeSpace(totalVarSize[0]*2);
					position=placement[0];
					capacity=placement[1];
				}
			}
			input.putInt(0,position);
			input.putInt(4,capacity);
			input.putInt(8,totalVarSize[0]);
			fChan.write(input, varIntsPos);
			int totalSize=0;
			format.vChan().position(position);
			for(int buffWrite=0;buffWrite<varVals.length;buffWrite+=16) //Work around JRE bug
				totalSize+=format.vChan().write(varVals,buffWrite,(16>varVals.length-buffWrite)?(varVals.length-buffWrite):16);
			if(totalVarSize[0]!=totalSize) Log.sysOut("DBManager","Wrote "+O.ID()+" "+fileIndex+": "+totalSize+" bytes (claims "+totalVarSize[0]+").");

			/*
			for(ByteBuffer buf : varVals)
			{
				if(buf.position()!=0) Log.sysOut("DBManager","Buffer not rewound: "+buf.toString());
			}
			long test=format.vChan().position(position).write(varVals);
			String last;
			if(totalVarSize[0]==test) last = (" bytes.");
			else
			{
				last=(" bytes (claims "+totalVarSize[0]+").");
			}
			Log.sysOut("DBManager","Wrote "+O.ID()+" "+fileIndex+": "+test+last);
			*/
		}
		catch(Exception e){Log.errOut("DBManager",e);}
	}
}