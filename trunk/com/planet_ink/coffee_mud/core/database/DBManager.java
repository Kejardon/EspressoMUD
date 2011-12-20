package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.io.*;

/*
TODO IMMEDIATELY: CMSavable/Ownable objects!
Format stuff
	Array[32; Int size] of subobject types(Example from CMClass, compare IDs (O.ID()==subObjs[i].ID(), faster and safe because of how Java works))
	Int of how many subobject types exist.
Data stuff
	Bitflag of sub objects(saved as an Int, &1 to get bit and >>1 each time to go to next) that are exceptions. Insert this into the save data, not the format itself.

When reading data, check if found thisEnum.size()==-1(is subobject). If so check if format is good.
	If good, this is variabledata. Find 
	If not

IGNORE ALL THAT
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
*/

@SuppressWarnings("unchecked")
public class DBManager extends Thread implements DatabaseEngine
{
	//Easily modifiable settings
	public static final String saveDirectoryName="resources"+File.pathSeparator+"database"+File.pathSeparator;	//Note: If you change this you must make the new directory manually!
	public static final String fixedExtensionName=File.separator+"fix";	//1 per CMSavable ID per backup. Holds fixed width data, defined in subFile
	public static final String variableExtensionName=File.separator+"var";	//1 per CMSavable ID per backup. Holds variable width data
	public static final String mainFileName="main"+File.separator+"bin";	//1. Holds general backup details
	public static final String subFileName="sub"+File.separator+"bin";	//1 per backup. Holds fixed width data
	public static final int timeTillDoFirst=600000;
	public static final int timeTillDoLast=60000;
	public static final int numBackups=2;
	public static final long waitInterval=1000;
	public static final long finalWaitInterval=1000;
	public static final java.nio.charset.Charset charFormat=java.nio.charset.Charset.forName("UTF-8");	//This should probably not be changed. Mainly, 1-byte chars will be assumed.

	//Internal variables
	private static int currentBackup=0;	//0-(numBackups)
	private static File mainFile;
	private static File subFile;
	private static MyList updateList=new MyList();
	private static HashSet<CMSavable> publicQueue=new HashSet<CMSavable>();
	private static HashSet<CMSavable> processingQueue=new HashSet<CMSavable>();
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

		public MyList(){set=new HashMap<CMSavable,ListNode>();}
		public MyList(int size){set=new HashMap<CMSavable,ListNode>(size);}

//		public ListNode firstFirst(){return firstFirst;}
//		public ListNode firstLast(){return firstLast;}
		public void add(CMSavable object)	//Make this return true if the first was changed?
		{
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
	private static class SaveFormat
	{
		//One-time generated data
		public final WVector<CMSavable.SaveEnum> enums;	//This is not meant to be edited once written. Never modify enums.
		public final int size;	//counting the extra 4 Ints, aka ^ + 16
		public final File fixedData;
		public final File variableData;
		public final boolean confirmedGood;
		//Runtime modifiable data
			//These first three need to be populated by the load function!
		public HashMap<SimpleInt, SimpleInt> fileMap=new HashMap();	//get(SID), return fileIndex, for fixedData
		public WVector<SimpleInt> freeSpaces=new WVector();
			//entry is position(unique!), weight is size(num bytes available), for variableData.
			//First entry is always end-of-KNOWN-data and is 0 weight! After loading, data past this point may be overwritten!
		public Vector<SimpleInt> freeEntries=new Vector();	//Values not in fileMap. Again, first entry is end-of-known-data.
		protected FileChannel fixedChannel;
		protected FileChannel variableChannel;
		protected final HashMap<CMSavable.SaveEnum,String> subTypes;
		public SaveFormat(WVector<CMSavable.SaveEnum> h, HashMap<CMSavable.SaveEnum,String> s, boolean c, File[] f)
		{
			enums=h;
			subTypes=s;
			size=h.weight()+16;
			fixedData=f[0];
			variableData=f[1];
			confirmedGood=c;
			freeEntries.add(new SimpleInt(0));
			freeSpaces.add(new SimpleInt(0),0);
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
		public int getFreeEntry()
		{
			if(freeEntries.size()>1) return freeEntries.remove(1).Int;
			return freeEntries.get(0).Int++;
		}
		public void addFreeEntry(int i)
		{
			freeEntries.add(new SimpleInt(i));
		}
		public void returnSpace(int position, int size)
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
					CMSavable.CMSubSavable sub=thisEnum.subObject(O);
					if((sub!=null)&&(subTypes.get(new SimpleInt(index-1))==sub.ID()))	//Safe because intern()'d when loading
						fixedVals[index]=DBManager.getFormat(sub).getPartFixedVals(sub);
					else
						fixedVals[index]=ByteBuffer.wrap(new byte[enums.weight(index-1)]);
				}
				else
					fixedVals[index]=thisEnum.save(O);
			}
			//IN CASE THERE ARE MISSING FIXEDVALS. Ideally this code shouldn't actually be necessary though...
			if(!confirmedGood)
			for(int i=1;i<fixedVals.length;i++)
				if(fixedVals[i]==null)
					fixedVals[i]=ByteBuffer.wrap(new byte[enums.weight(i)]);
			return fixedVals;
		}
		public ByteBuffer getPartFixedVals(CMSavable O)
		{
			int saveNum=O.saveNum();
			ByteBuffer[] fixedVals=new ByteBuffer[enums.size()];
			for(CMSavable.SaveEnum thisEnum : O.totalEnumS())
			{
				int index=enums.index(thisEnum);
				if(index==-1) continue;
				if(thisEnum.size()==-1)
				{
					CMSavable.CMSubSavable sub=thisEnum.subObject(O);
					if((sub!=null)&&(subTypes.get(new SimpleInt(index))==sub.ID()))	//Safe because intern()'d when loading
						fixedVals[index]=DBManager.getFormat(sub).getPartFixedVals(sub);
					else
						fixedVals[index]=ByteBuffer.wrap(new byte[enums.weight(index)]);
				}
				else
					fixedVals[index]=thisEnum.save(O);
			}
			//IN CASE THERE ARE MISSING FIXEDVALS. Ideally this code shouldn't actually be necessary though...
			if(!confirmedGood)
			for(int i=0;i<fixedVals.length;i++)
				if(fixedVals[i]==null)
					fixedVals[i]=ByteBuffer.wrap(new byte[enums.weight(i)]);
			ByteBuffer partVal=ByteBuffer.wrap(new byte[size-16]);	//size meaning this format's size
			for(int i=0;i<fixedVals.length;i++)
				partVal.put(fixedVals[i].array());
			partVal.rewind();
			return partVal;
		}
		public ByteBuffer[] getVarVals(CMSavable O, int[] totalVarSize)
		{
			Vector<ByteBuffer> variableVals=new Vector();
			for(CMSavable.SaveEnum thisEnum : O.totalEnumS())
			{
				index=enums.index(thisEnum);
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
					CMSavable.CMSubSavable sub=thisEnum.subObject(O);
					if(sub==null) continue;
					if(subTypes.get(new SimpleInt(index))==sub.ID())
					{
						ByteBuffer buf=sub.varSave();
						if(buf.remaining()<=1) continue;	//1 because first byte is (should be) 1 to mark it as vardata.
						variableVals.add(charFormat.encode(thisEnum.name()));
						variableVals.add((ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(size).rewind());
						variableVals.add(buf);
						totalVarSize[0]+=size+7;
					}
					else
					{
						ByteBuffer buf=thisEnum.save(O);
						int size=buf.remaining();
						if(size==0) continue;
						variableVals.add(charFormat.encode(thisEnum.name()));
						variableVals.add((ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(size).rewind());
						variableVals.add(buf);
						totalVarSize[0]+=size+7;
					}
				}
			}
			return (ByteBuffer[])variableVals.toArray(new ByteBuffer[0]);
		}
	}
	private static class SimpleInt implements Comparable<SimpleInt>
	{
		public int Int=0;
		public SimpleInt(){}
		public SimpleInt(int i){Int=i;}
		public int compareTo(SimpleInt other){return Int-other.Int;}
		public boolean equals(Object other){return ((other instanceof SimpleInt)&&(((SimpleInt)other).Int==Int)); }
		public int hashCode(){return Int;}
	}

	public DBManager()
	{
		super("DBManagerThread");
	}

	//Internal code
	public String ID(){return "DBManager";}
	public CMObject newInstance(){return this;}
	public void initializeClass(){}
	public CMObject copyOf(){return this;}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public boolean activate()
	{
		start();
		return true;
	}
	public boolean shutdown()
	{
		finishUp=true;;
		return true;
	}
	public void propertiesLoaded(){}
	public ThreadEngine.SupportThread getSupportThread() { return null;}
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
		if(!(E instanceof CMSavable.CMSubSavable))
		{
			files[0]=getMakeFile(saveDirectoryName+currentBackup+File.pathSeparator+E.ID()+fixedExtensionName);
			files[1]=getMakeFile(saveDirectoryName+E.ID()+variableExtensionName);
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
				catch(Exception e){Log.errOut("DBManager",e); return null;}
			}
			mainFile=f;
		}
		return mainFile;
	}
	public static File subFile()
	{
		if(subFile==null)
		{
			File f=new File(saveDirectoryName+currentBackup+File.pathSeparator+subFileName);
			if(!f.exists())
			try{ f.createNewFile(); }
			catch(Exception e){Log.errOut("DBManager",e); return null;}
			subFile=f;
		}
		return subFile;
	}
	public static void saveObject(CMSavable O) { publicQueue.add(O); }
	public static boolean doneLoading(){return doneLoading;}
	public void run()
	{
		try{
			ByteBuffer mainInfo=ByteBuffer.wrap(new byte[2]);
			FileChannel input=new RandomAccessFile(mainFile(), "rw").getChannel();
			input.read(mainInfo);
			input.close();
			currentBackup=mainInfo.get(0);
			if(mainInfo.get(1)==1)
				Log.errOut("DBManager","Warning: MUD was not properly shutdown, data may be corrupted!");
			input=new RandomAccessFile(subFile(), "rw").getChannel();
			ByteBuffer formatInfo=ByteBuffer.wrap(new byte[input.size()]);
			input.read(formatInfo);
			input.close();
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
				Enum[] options=E.headerEnumS();
				byte[] type=new byte[3];
				CMSavable.SaveEnum thisEnum;
				HashMap<CMSavable.SaveEnum, String> subTypes=new HashMap();
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
				WVector<CMSavable.SaveEnum> fixedVars=new WVector();
				{
					for(int i=0;formatInfo.position()<end;i++)
					{
						int size=formatInfo.get(type).getInt();
						String option=new String(type, charFormat);
						thisEnum=null;
						for(int j=0;(j<options.length)&&(thisEnum==null);j++)
							thisEnum=getParser(options[j], option);
						if(thisEnum==null) {goodFormat=false; continue;}
						if(thisEnum.size()==-1)
						{
							thisEnum.subObject(O);
							//TODO: You Are Here. How get subobject size, What do if mismatch?
							//On second thought, subformats will always have been saved first already, as attempting to make a new format
							//has to access(make if necessary) all its subformats first
						}
						else if(thisEnum.size()!=size) {goodFormat=false; continue;}
						fixedVars.add(thisEnum, size);
					}
				}

				//TODO: load data
			}
			
			if(numBackups>0)
			{
				currentBackup++;
				if(currentBackup>numBackups) currentBackup=0;
				File newDir=new File(saveDirectoryName+currentBackup);
				newDir.delete();
				newDir.mkdirs();
				//TODO: save data
			}
			mainInfo.put(1,(byte)1).rewind();
			input=new RandomAccessFile(mainFile(), "rw").getChannel();
			input.write(mainInfo);
			input.close();
		}
		catch(Exception e){Log.errOut("DBManager",e); return;}
		doneLoading=true;
		while(!finishUp)
		{
			{
				CMSavable O;
				while((O=getNext())!=null)
					writeToFile(O);
			}
			
			for(Iterator<CMSavable> e=processingQueue.iterator();e.hasNext();)
			{ updateList.add(e.next()); }
			processingQueue.clear();
			{ //Swap queues to prepare for next processing cycle
				HashSet tempQueueRef=publicQueue;
				publicQueue=processingQueue;
				processingQueue=tempQueueRef;
			}
			try{ sleep(waitInterval); } catch(Exception e){Log.errOut("DBManager",e);}
		}
		for(Iterator<CMSavable> e=processingQueue.iterator();e.hasNext();)
		{ updateList.add(e.next()); }
		processingQueue=publicQueue;
		if(finalWaitInterval>0)
			try{ sleep(finalWaitInterval); } catch(Exception e){Log.errOut("DBManager",e);}
		for(Iterator<CMSavable> e=processingQueue.iterator();e.hasNext();)
		{ updateList.add(e.next()); }
		while(updateList.firstFirst!=null)
			writeToFile(updateList.removeFirst());
		byte[] mainFlags=new byte[2];
		mainFlags[0]=(byte)currentBackup;
		mainFlags[0]=(byte)0;
		try{
			FileChannel output=new RandomAccessFile(mainFile(), "rw").getChannel();
			output.write(ByteBuffer.wrap(mainFlags));
			output.close();
		}
		catch(Exception e){Log.errOut("DBManager",e);}
	}
	protected CMSavable.SaveEnum getParser(Enum E, String S)
	{
		try{return (CMSavable.SaveEnum)E.valueOf((Class)E.getClass().getSuperclass(), S);}
		catch(IllegalArgumentException e){if(e.getMessage().startsWith("No")) return null;}
		try{return (CMSavable.SaveEnum)E.valueOf(E.getClass(), S);}
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
	protected static SaveFormat getFormat(CMSavable obj)
	{
		String ID=obj.ID();
		SaveFormat f=formats.get(ID);
		if(f==null)
		{
			CMClass.Objects type=CMClass.getType(obj);
			obj=(CMSavable)type.get(obj.ID());	//Get a clean instance with default types
			WVector<CMSavable.SaveEnum> fixedVars=new WVector();
			int totalSize=16;
			byte[] IDBytes=ID.getBytes(charFormat);
			int formatSize=5+IDBytes.length;	//5=SID+End-Of-SubObjects marker. formatSize itself is not counted.
			HashMap<CMSavable.SaveEnum,String> subObjs=new HashMap();
			for(CMSavable.SaveEnum thisEnum : obj.totalEnumS())
			{
				int i=thisEnum.size();
				if(i==0) continue;
				if(i==-1)
				{
					CMSavable.CMSubSavable subObject=thisEnum.subObject(obj);
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
			f=new SaveFormat(fixedVars, subObjs, true, getObjectFiles(obj));
			formats.put(ID, f);
			
			try{
				ByteBuffer fileFormat=ByteBuffer.wrap(new byte[formatSize+4]).putInt(formatSize).putInt(IDBytes.length).put(IDBytes);
				for(Iterator<CMSavable.SaveEnum> e=subObjs.keySet().iterator(); e.hasNext();)
				{
					CMSavable.SaveEnum i=e.next();
					String str=e.get(i);
					fileFormat.put((byte)str.length()).put(i.name().getBytes(charFormat)).put(str.getBytes(charFormat));
				}
				fileFormat.put((byte)0);	//End-of-subobjects marker
				for(int i=0;i<fixedVars.size();i++)
					fileFormat.put(fixedVars.get(i).name().getBytes(charFormat)).putInt(fixedVars.weight(i));
				FileChannel saveFormatChannel=new RandomAccessFile(subFile(), "rw").getChannel();
				saveFormatChannel.write(fileFormat, saveFormatChannel.size());
			}
			catch(Exception e){Log.errOut("DBManager",e);}
		}
		return f;
	}
	protected static void writeToFile(CMSavable O)
	{
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
			fChan.write(fixedVals);	//NOTE: If something goes wrong somehow this may corrupt data!
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
			format.vChan().position(position).write(varVals);
		}
		catch(Exception e){Log.errOut("DBManager",e);}
	}
}