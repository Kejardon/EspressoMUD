package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.database.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.concurrent.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMClass extends ClassLoader
{
	public static final Object[] dummyObjectArray=new Object[0];
	public static final String[] dummyStringArray=new String[0];
	public static final ArrayList[] dummyALArray=new ArrayList[0];
	public static final Vector[] dummyVectorArray=new Vector[0];
	public static final HashSet[] dummyHashSetArray=new HashSet[0];
	public static final Enum[] dummyEnumArray=new Enum[0];
	public static final long[] dummylongArray=new long[0];
	public static final int[] dummyintArray=new int[0];
	public static final byte[] dummybyteArray=new byte[0];

	public static final Vector emptyVector=new Vector();
	public static final ArrayList emptyAL=new ArrayList();
	//public static final Enumeration emptyEnumeration=emptyVector.elements();
	//public static final Iterator emptyIterator=emptyVector.iterator();

	public static final ExecutorService threadPool=Executors.newCachedThreadPool();	//Gotta put it somewhere, here's a good spot
	protected static boolean debugging=false;
	//protected static Hashtable<String, Object> classes=new Hashtable();
	//public static EnumSet<Objects> ItemTypes = EnumSet.of(Objects.ITEM, Objects.WEARABLE, Objects.WEAPON);
	protected static final Vector<CMMsg> MSGS_CACHE=new Vector();
	protected static CMClass instance=new CMClass();
	public static CMClass instance(){return instance;}
	protected static final byte[] charTranslatorTable=
	{// 0	1	2	3	4	5	6	7	8	9	A	B	C	D	E	F
		-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,
		-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	-1,
		0,	1,	-1,	2,	3,	4,	5,	6,	7,	8,	9,	10,	11,	12,	13,	14,
		15,	16,	17,	18,	19,	20,	21,	22,	23,	24,	25,	26,	27,	28,	29,	30,
		31,	32,	33,	34,	35,	36,	37,	38,	39,	40,	41,	42,	43,	44,	45,	46,
		47,	48,	49,	50,	51,	52,	53,	54,	55,	56,	57,	58,	59,	60,	61,	62,
		6,	32,	33,	34,	35,	36,	37,	38,	39,	40,	41,	42,	43,	44,	45,	46,
		47,	48,	49,	50,	51,	52,	53,	54,	55,	56,	57,	63,	64,	65,	66
	};
	protected static final byte charTranslatorTableMax=67;
	private static class ByteIndexTable
	{
		public ByteIndexTable[] subTables;
		public short[] indexOffsets;
			//0+ : add to startIndex. short SHOULD be sufficient unless there are a truly absurd number of commands
			//-1 : Invalid, throw an IndexOutOfBoundsException
		public int maxRange=1;
		public void initializeArrays()
		{
			subTables=new ByteIndexTable[charTranslatorTableMax];
			indexOffsets=new short[charTranslatorTableMax];
			for(int i=0;i<indexOffsets.length;i++)
				indexOffsets[i]=-1;
		}
		public ByteIndexTable get(byte val, int[] offset)
		{
			int adjust=indexOffsets[val];
			if(adjust==-1) throw new IndexOutOfBoundsException();
			offset[0]+=adjust;
			return subTables[val];
		}
	}

	public static abstract class Objects<U extends CMObject>
	{
		public static final HashMap<String, Objects> objectsNames=new HashMap<String, Objects>();
		public static Objects valueOf(String S){return objectsNames.get(S);}
		public static Collection<Objects> values(){return objectsNames.values();}

		public final Class ancestor;
		public final String name;
		public ConcurrentHashMap<String, U> options=new ConcurrentHashMap<String, U>();
		
		public Objects(Class S, String name)
		{
			ancestor=S;
			//try{ancestor=CMClass.instance.loadClass(S, true);}
			//catch(ClassNotFoundException e){Log.errOut("CMClass","Ancestor not found: "+S);}
			this.name=name;
			objectsNames.put(name, this);
		}
		
		//A little hackish but probably the best solution.
		public Command getCommand(String word, boolean exactOnly, MOB mob){return null;}
		public void compileCommands(){}

		public Class ancestor(){return ancestor;}
		public U getAny() {
			Iterator<U> I=options.values().iterator();
			if(I.hasNext()) return I.next();
			return null; }
		public U get(String ID) { return options.get(ID); }
		public U getNew(String ID) {
			U O=options.get(ID);
			if(O!=null) return (U)O.newInstance();
			Log.errOut(name,"Failed request: "+ID);
			return null; }
		public void add(U O){options.put(O.ID(), O);}
		public boolean remove(U O){return (options.remove(O.ID())!=null);}
		public void initialize()
		{
			for(U O : options.values())
				O.initializeClass();
		}
		public int size(){return options.size();}
		public Iterator<U> all(){ return options.values().iterator(); }
	}
	public static final Objects<Gender> GENDER=new Objects<Gender>(Gender.class, "GENDER"){};
	public static final Objects<Race> RACE=new Objects<Race>(Race.class, "RACE"){};
	public static final Objects<Effect> EFFECT=new Objects<Effect>(Effect.class, "EFFECT"){};
	public static final Objects<Room> LOCALE=new Objects<Room>(Room.class, "LOCALE"){};
	public static final Objects<MOB> CREATURE=new Objects<MOB>(MOB.class, "MOB"){};
	public static final Objects<Exit> EXIT=new Objects<Exit>(Exit.class, "EXIT"){};
	public static final Objects<Behavior> BEHAVIOR=new Objects<Behavior>(Behavior.class, "BEHAVIOR"){};
	public static final Objects<Area> AREA=new Objects<Area>(Area.class, "AREA"){};
	public static final Objects<Closeable> CLOSEABLE=new Objects<Closeable>(Closeable.class, "CLOSEABLE"){};
	public static final Objects<CMCommon> COMMON=new Objects<CMCommon>(CMCommon.class, "COMMON"){};
	public static final Objects<Wearable> WEARABLE=new Objects<Wearable>(Wearable.class, "WEARABLE"){};
	public static final Objects<Weapon> WEAPON=new Objects<Weapon>(Weapon.class, "WEAPON"){};
	public static final Objects<Item> ITEM=new Objects<Item>(Item.class, "ITEM"){};
	public static final Objects<Command> COMMAND=new Objects<Command>(Command.class, "COMMAND"){
		private HashMap<String, Command> commandWordsMap=new HashMap<String, Command>();
		private HashMap<String, Command> tempWordsMap=new HashMap<String, Command>();
		private String[] commandWordsList=new String[0];
		private SortedVector<String> tempWordsList=new SortedVector<String>();
		private ByteIndexTable rootTable=null;
		public synchronized void add(Command O){
			super.add(O);
			if(O.getAccessWords()==null) Log.errOut("C.O.COMMAND","Null accesswords: "+O.ID());
			else synchronized(tempWordsMap){
				for(String S : O.getAccessWords()){
					//S=S.trim().toUpperCase();	//No, ideally this should not be needed. Just make sure the input to this is always good.
					tempWordsMap.put(S,O);
					tempWordsList.addRandom(S); } } }
		public synchronized boolean remove(Command O){
			if(!super.remove(O)) return false;
			synchronized(tempWordsMap){
				for(String S : O.getAccessWords()){
					tempWordsMap.remove(S);
					tempWordsList.remove(S); } }
			return true; }
		public void compileCommands() {
			ByteIndexTable tempTable=new ByteIndexTable();
			tempTable.initializeArrays();
			for(short i=0;i<tempWordsList.size();){
				byte arrayIndex=charTranslatorTable[tempWordsList.get(i).getBytes(DBManager.charFormat)[0]];
				ByteIndexTable newTable=new ByteIndexTable();
				tempTable.subTables[arrayIndex]=newTable;
				tempTable.indexOffsets[arrayIndex]=i;
				i+=subCompile(1, i, newTable); }
			commandWordsList=(String[])tempWordsList.toArray(CMClass.dummyStringArray);
			commandWordsMap=(HashMap)tempWordsMap.clone();
			rootTable=tempTable; }
		private int subCompile(int stringIndex, int listIndex, ByteIndexTable newTable) {
			String firstStr=tempWordsList.get(listIndex);
			if((tempWordsList.size()>listIndex+1)
			  &&(firstStr.regionMatches(0,tempWordsList.get(listIndex+1),0,stringIndex))) {
				newTable.initializeArrays();
				short subRange=0;
				if(firstStr.length()==stringIndex) subRange++;
				//Do not need the actual subtable/indexoffset for above spot as hashtable will handle the request for it
				do{
					byte arrayIndex=charTranslatorTable[tempWordsList.get(listIndex+subRange).getBytes(DBManager.charFormat)[stringIndex]];
					ByteIndexTable subTable=new ByteIndexTable();
					newTable.subTables[arrayIndex]=subTable;
					newTable.indexOffsets[arrayIndex]=subRange;
					subRange+=subCompile(stringIndex+1, listIndex+subRange, subTable); }
					while((subRange+listIndex<tempWordsList.size())&&(firstStr.regionMatches(0,tempWordsList.get(listIndex+subRange),0,stringIndex)));
				newTable.maxRange=subRange; }
			return newTable.maxRange; }
		public Command getCommand(String word, boolean exactOnly, MOB mob){
			word=word.trim().toUpperCase();
			Command C=commandWordsMap.get(word);
			if((exactOnly)||(C!=null))
			{
				if((C!=null)&&((mob==null)||(C.securityCheck(mob))))
					return C;
				return null;
			}
			byte[] requestBytes=word.getBytes(DBManager.charFormat);
			try{
				ByteIndexTable byteTable=rootTable;
				int[] startIndex=new int[] {0};
				int i=0;
				while(i<requestBytes.length) {
					byteTable=byteTable.get(charTranslatorTable[requestBytes[i++]], startIndex);
					if(byteTable.maxRange<=1) break; }
				int adjust=0;
				while(adjust<byteTable.maxRange) {
					String option=commandWordsList[startIndex[0]+adjust++];
					if((i==requestBytes.length)||(option.startsWith(word))) {
						C=commandWordsMap.get(option);
						if((mob==null)||(C.securityCheck(mob))) return C; } } }catch(IndexOutOfBoundsException e){}	//This exception can happen normally when the player enters a command that doesn't exist
			return null; } };
	public static final Objects<CMLibrary> LIBRARY=new Objects<CMLibrary>(CMLibrary.class, "LIBRARY"){
			public void add(CMLibrary O){
				super.add(O);
				CMLib.registerLibrary((CMLibrary)O); }
			//public void remove(CMObject O){}	//This really is not supported by CMLib. No, just don't remove libraries.
		};

	public static Objects getType(CMObject O)
	{
		if(O instanceof Gender) return GENDER;
		if(O instanceof Race) return RACE;
		if(O instanceof Effect) return EFFECT;
		if(O instanceof Room) return LOCALE;
		if(O instanceof MOB) return CREATURE;
		if(O instanceof Exit) return EXIT;
		if(O instanceof Behavior) return BEHAVIOR;
		if(O instanceof Area) return AREA;
		if(O instanceof Closeable) return CLOSEABLE;
		if(O instanceof CMCommon) return COMMON;
		if(O instanceof Wearable) return WEARABLE;
		if(O instanceof Weapon) return WEAPON;
		if(O instanceof Item) return ITEM;
		if(O instanceof Command) return COMMAND;
		if(O instanceof CMLibrary) return LIBRARY;
		return null;
	}

//	public static Command getCommand(String word, boolean exactOnly)
//	CMClass.Objects.COMMAND.getCommand(word, exactOnly)

//	public static boolean delClass(String type, CMObject O)
//	No, use something like classCode(type).remove(O)

//	protected static Object getClassSet(String type) { return getClassSet(classCode(type));}
//	protected static Object getClassSet(Objects code)
//	No, call them directly. I guess I might need to make their options directly callable, but I'd prefer not to.

//	public static boolean addClass(String type, CMObject O)
//	No, use something like classCode(type).add(O) instead

	public static Objects classCode(String name)
	{
		try{ return Objects.valueOf(name); }
		catch(IllegalArgumentException e){return null;}
	}

	public static Objects classCode(Object O)
	{
		for(Objects e : Objects.values())
		{
			try{
				//Class ancestorCl = instance.loadClass(e.ancestor());
				if(checkAncestry(O.getClass(),e.ancestor()))
					return e;
			}catch(Exception ex){}
		}
		return null;
	}

	public static boolean loadClass(String classType, String path, boolean quiet)
	{
		debugging=CMSecurity.isDebugging("CLASSLOADER");
		Objects set=classCode(classType);
		if(set==null) return false;
		return !loadListToObj(set,path,set.ancestor(),quiet);
	}

	public static CMObject getClass(String calledThis)
	{
		//String shortThis=calledThis;
		//int x=shortThis.lastIndexOf('.');
		//if(x>0) shortThis=shortThis.substring(x+1);
		//Object set=null;
		int x=calledThis.lastIndexOf('.');
		if(x>0) calledThis=calledThis.substring(x+1);
		CMObject thisItem=null;
		for(Objects e : Objects.values())
		{
			thisItem=e.get(calledThis);
			if(thisItem!=null) return thisItem;
		}
		//try{ return ((CMObject)classes.get(calledThis)).newInstance();}catch(Exception e){}
		return null;
	}

	public static Interactable getUnknown(String calledThis)
	{
		Interactable thisItem=ITEM.getNew(calledThis);
		if(thisItem==null) thisItem=WEARABLE.getNew(calledThis);
		if(thisItem==null) thisItem=WEAPON.getNew(calledThis);
		if(thisItem==null) thisItem=CREATURE.getNew(calledThis);
		//if(thisItem==null) thisItem=EFFECT.getNew(calledThis);
		if((thisItem==null)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Unknown '"+calledThis+"'.");
		return thisItem;
	}

	//TODO: I'm curious if this actually happens and works. Should log and see sometime.
	public static boolean returnMsg(CMMsg msg)
	{
		if(MSGS_CACHE.size()<10000)
		{
			//if(MSGS_CACHE.remove(msg))
			//	Log.errOut("CMClass",new RuntimeException("Returned an already cached message!"));
			MSGS_CACHE.add(msg);	//This should be ok without synchronization, really.
			return true;
		}
		return false;
	}

	public static CMMsg MsgFactory()
	{
		CMMsg msg=null;
		synchronized(CMClass.MSGS_CACHE)
		{
			int i=MSGS_CACHE.size();
			if(i==0)
				msg=(CMMsg)COMMON.getNew("DefaultMessage");
			else
				msg=MSGS_CACHE.remove(i-1);
		}
		return msg;
	}

	public static CMMsg getMsg(Object source, Interactable target, Object tool, EnumSet newAllCode, String allMessage)
	{
		CMMsg M=MsgFactory();
		if(source!=null)
		{
			if(source instanceof Vector)
				M.setSource((Vector<Interactable>)source);
			else
				M.addSource((Interactable)source);
		}
		if(target!=null)
			M.setTarget(target);
		if(tool!=null)
		{
			if(tool instanceof Vector)
				M.setTools((Vector<CMObject>)tool);
			else
				M.addTool((CMObject)tool);
		}
		M.setSourceCode(newAllCode.clone());
		M.setTargetCode(newAllCode.clone());
		M.setOthersCode(newAllCode);
		if(allMessage!=null)
		{
			M.setSourceMessage(allMessage);
			M.setTargetMessage(allMessage);
			M.setOthersMessage(allMessage);
		}
		return M;
	}
	public static CMMsg getMsg(Object source, Interactable target, Object tool, EnumSet newSourceCode, String sourceMessage, EnumSet newTargetCode, String targetMessage, EnumSet newOthersCode, String othersMessage)
	{
		CMMsg M=MsgFactory();
		if(source!=null)
		{
			if(source instanceof Vector)
				M.setSource((Vector<Interactable>)source);
			else
				M.addSource((Interactable)source);
		}
		if(target!=null)
			M.setTarget(target);
		if(tool!=null)
		{
			if(tool instanceof Vector)
				M.setTools((Vector<CMObject>)tool);
			else
				M.addTool((CMObject)tool);
		}
		M.setSourceCode(newSourceCode);
		if(sourceMessage!=null)
			M.setSourceMessage(sourceMessage);
		M.setTargetCode(newTargetCode);
		if(targetMessage!=null)
			M.setTargetMessage(targetMessage);
		M.setOthersCode(newOthersCode);
		if(othersMessage!=null)
			M.setOthersMessage(othersMessage);
		return M;
	}

	public static void initializeClasses()
	{
		for(Objects e : Objects.values())
			e.initialize();
	}

	public static boolean loadListToObj(Objects toThis, String filePath, Class ancestorCl, boolean quiet)
	{
		//CMClass loader=new CMClass();
		CMFile file=new CMFile(filePath,null,true);
		LinkedList<String> fileList=new LinkedList();
		if(file.canRead())
		{
			if(file.isDirectory())
			{
				CMFile[] list=file.listFiles();
				for(int l=0;l<list.length;l++)
					if((list[l].getName().indexOf("$")<0)&&(list[l].getName().toUpperCase().endsWith(".CLASS")))
						fileList.add(list[l].getVFSPathAndName());
			}
			else
				fileList.add(file.getVFSPathAndName());
		}
		else
		{
			if(!quiet)
				Log.errOut("CMClass","Unable to access path "+file.getVFSPathAndName());
			return false;
		}
		while(fileList.peek()!=null)
		{
			String item=fileList.pop();
			//if(item.startsWith("/")) item=item.substring(1);
			try
			{
				CMObject O=null;
				if(item.toUpperCase().endsWith(".CLASS"))
					item=item.substring(0,item.length()-6);
				item=item.replace('/','.');
				Class<CMObject> C=instance.loadClass(item,true);
				if(C!=null)
				{
					if(!checkAncestry(C,ancestorCl))
					{
						if(!quiet)
							Log.sysOut("CMClass","WARNING: class failed ancestral check: "+item);
					}
					else
						O=C.newInstance();
				}
				if(O==null)
				{
					if(!quiet)
						Log.sysOut("CMClass","Unable to create class '"+item+"'");
				}
				else
				{
/*					String itemName=C.getName();
					int x=itemName.lastIndexOf(".");
					if(x>=0) itemName=itemName.substring(x+1);
					if(toThis instanceof Hashtable)
					{
						Hashtable H=(Hashtable)toThis;
						H.put(itemName.trim().toUpperCase(),O);
					}
					else
					if(toThis instanceof Vector)
					{
						Vector V=(Vector)toThis;
						boolean doNotAdd=false;
						for(int v=0;v<V.size();v++)
							if(rawClassName(V.elementAt(v)).equals(itemName))
							{
								V.setElementAt(O,v);
								doNotAdd=true;
								break;
							}
						if(!doNotAdd)
							V.addElement(O);
					}
					else
					if(toThis instanceof Objects)
					{ */
					toThis.remove(O);	//This is only really needed for commands... but I'll leave it in anyways
					toThis.add(O);
//					}
				}
			}
			catch(Throwable e)
			{
				if(!quiet)
					Log.errOut("CMClass",e);
				return false;
			}
		}
		return true;
	}

	public static String rawClassName(Object O)
	{
		if(O==null) return "";
		return rawClassName(O.getClass());
	}

	public static String rawClassName(Class C)
	{
		if(C==null) return "";
		String name=C.getName();
		int lastDot=name.lastIndexOf(".");
		if(lastDot>=0)
			return name.substring(lastDot+1);
		return name;
	}

	public static boolean checkAncestry(Class cl, Class ancestorCl)
	{
		if (cl == null) return false;
		int mod=cl.getModifiers();
		if((mod&(Modifier.ABSTRACT|Modifier.INTERFACE))!=0||
		   (mod&Modifier.PUBLIC)==0||
		   cl.isPrimitive())
			return false;
//		if (cl.isPrimitive() || cl.isInterface()) return false;
//		if ( Modifier.isAbstract( cl.getModifiers()) || !Modifier.isPublic( cl.getModifiers()) ) return false;
//		if (ancestorCl == null) return true;
		return (ancestorCl.isAssignableFrom(cl)) ;
	}

	public static String classPtrStr(Object e)
	{
		String ptr=""+e;
		int x=ptr.lastIndexOf("@");
		if(x>0)return ptr.substring(x+1);
		return ptr;
	}

	public static String classID(Object e)
	{
		if(e!=null)
		{
			if(e instanceof CMObject)
				return ((CMObject)e).ID();
			else
				return rawClassName(e.getClass());
		}
		return "";
	}

	/**
	 * This is a simple version for external clients since they
	 * will always want the class resolved before it is returned
	 * to them.
	 */
	public Class loadClass(String className) throws ClassNotFoundException {
		return (loadClass(className, true));
	}

	/**
	 * This is the required version of loadClass which is called
	 * both from loadClass above and from the internal function
	 * FindClassFromClass.
	 */
	public synchronized Class loadClass(String className, boolean resolveIt)
		throws ClassNotFoundException
	{
		//String pathName=null;
		if(className.endsWith(".class")) className=className.substring(0,className.length()-6);
		//pathName=className.replace('.','/')+".class";
/*
		Class result = (Class)classes.get(className);
		if (result!=null)
		{
			if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
			return result;
		}
		if((result=findLoadedClass(className))!=null)
			return result;
		if((className.indexOf("com.planet_ink.coffee_mud.")<0)
		||(className.startsWith("com.planet_ink.coffee_mud.core."))
		||(className.startsWith("com.planet_ink.coffee_mud.application."))
		||(className.indexOf(".interfaces.")>=0))
		{
*/
			//try{
				Class result=super.findSystemClass(className);
				if(result!=null)
				{
					if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
					return result;
				}
			//} catch(Throwable t){}
/*		}
		CMFile CF=new CMFile(pathName,null,false);
		byte[] classData=CF.raw();
		if((classData==null)||(classData.length==0))
			throw new ClassNotFoundException("File "+pathName+" not readable!");
		result=finishDefineClass(className,classData,null,resolveIt);
*/
		return null;
	}

	//Currently this order is important because of dependancies. I don't like that, would like to fix that later.
	public static boolean loadClasses()
	{
		try
		{
			String prefix="com/planet_ink/coffee_mud/";
			debugging=CMSecurity.isDebugging("CLASSLOADER");

			Objects O=LIBRARY;
			loadListToObj(O, prefix+"Libraries/", O.ancestor(), false);
			if(O.size()==0) return false;
			if(CMLib.unregistered().length()>0)
			{
				Log.errOut("CMClass","Fatal Error: libraries are unregistered: "+CMLib.unregistered().substring(0,CMLib.unregistered().length()-2));
				return false;
			}

			O=GENDER;
			loadListToObj(O, prefix+"Races/Genders/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Genders loaded    : "+O.size());
			if(O.size()==0) return false;

			O=RACE;
			loadListToObj(O, prefix+"Races/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Races loaded      : "+O.size());
			if(O.size()==0) return false;

			O=EFFECT;
			loadListToObj(O, prefix+"Effects/", O.ancestor(), false);
			loadListToObj(O, prefix+"Effects/Languages/", O.ancestor(), false);
//			loadListToObj(O, prefix+"Effects/Archon/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Effects loaded    : "+O.size());
			if(O.size()==0) return false;

			O=LOCALE;
			loadListToObj(O, prefix+"Locales/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Locales loaded    : "+O.size());
			if(O.size()==0) return false;

			O=BEHAVIOR;
			loadListToObj(O, prefix+"Behaviors/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Behaviors loaded  : "+O.size());
			if(O.size()==0) return false;

			O=AREA;
			loadListToObj(O, prefix+"Areas/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Area Types loaded : "+O.size());
			if(O.size()==0) return false;

			O=CLOSEABLE;
			loadListToObj(O, prefix+"Common/Closeable/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Closeables loaded : "+O.size());

			O=COMMON;
			loadListToObj(O, prefix+"Common/", O.ancestor(), false);
			if(O.size()==0) return false;

			O=WEARABLE;
			loadListToObj(O, prefix+"Items/Armor/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Armor loaded      : "+O.size());

			O=WEAPON;
			loadListToObj(O, prefix+"Items/Weapons/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Weapons loaded    : "+O.size());

			O=ITEM;
			loadListToObj(O, prefix+"Items/Basic/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Basic Items loaded: "+O.size());

			if((ITEM.size()+WEAPON.size()+WEARABLE.size())==0)
				return false;

			O=EXIT;
			loadListToObj(O, prefix+"Exits/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Exit Types loaded : "+O.size());
			if(O.size()==0) return false;

			O=CREATURE;
			loadListToObj(O, prefix+"MOBS/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"MOB Types loaded  : "+O.size());
			if(O.size()==0) return false;

			O=COMMAND;
			loadListToObj(O, prefix+"Commands/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Commands loaded   : "+O.size());
			if(O.size()==0) return false;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			return false;
		}

		// misc startup stuff
/*		for(int r=0;r<races.size();r++)
		{
			Race R=(Race)races.elementAt(r);
			R.copyOf();
		}
*/

		CMProps.Strings.MUDSTATUS.setProperty("Booting: initializing classes");
		initializeClasses();
		return true;
	}
	public static <T extends Enum> T valueOf(T E, String S)
	{
		try{return (T)E.valueOf(E.getClass(), S);}
		catch(IllegalArgumentException e){if(e.getMessage().startsWith("No")) return null;}
		try{return (T)E.valueOf((Class<T>)E.getClass().getSuperclass(), S);}
		catch(IllegalArgumentException e){}
		return null;
	}
	public static <T extends Enum> T valueOf(Class<T> E, String S)
	{
		try{return (T)Enum.valueOf(E, S);}
		catch(IllegalArgumentException e){if(e.getMessage().startsWith("No")) return null;}
		try{return (T)Enum.valueOf((Class<T>)E.getSuperclass(), S);}
		catch(IllegalArgumentException e){}
		return null;
	}
	public static String getStackTrace(Thread theThread)
	{
		java.lang.StackTraceElement[] s=(java.lang.StackTraceElement[])theThread.getStackTrace();
		StringBuffer dump = new StringBuffer("");
		for(int i=0;i<s.length;i++)
			dump.append("\n   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
		return dump.toString();
	}
/*
	public Class finishDefineClass(String className, byte[] classData, String overPackage, boolean resolveIt)
		throws ClassFormatError
	{
		Class result=null;
		if(overPackage!=null)
		{
			int x=className.lastIndexOf(".");
			if(x>=0)
				className=overPackage+className.substring(x);
			else
				className=overPackage+"."+className;
		}
		try{result=defineClass(className, classData, 0, classData.length);}
		catch(NoClassDefFoundError e)
		{
			if(e.getMessage().toLowerCase().indexOf("(wrong name:")>=0)
			{
				int x=className.lastIndexOf(".");
				if(x>=0)
				{
					String notherName=className.substring(x+1);
					result=defineClass(notherName, classData, 0, classData.length);
				}
				else throw e;
			}
			else throw e;
		}
		if (result==null){throw new ClassFormatError();}
		if (resolveIt){resolveClass(result);}
		if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
		classes.put(className, result);
		return result;
	}
	public static Vector loadClassList(String filePath, String auxPath, String subDir, Class ancestorC1, boolean quiet)
	{
		Vector v=new Vector();
		int x=auxPath.indexOf(";");
		while(x>=0)
		{
			String path=auxPath.substring(0,x).trim();
			auxPath=auxPath.substring(x+1).trim();
			if(path.equalsIgnoreCase("%default%"))
				loadListToObj(v,filePath, ancestorC1, quiet);
			else
				loadListToObj(v,path,ancestorC1, quiet);
			x=auxPath.indexOf(";");
		}
		if(auxPath.equalsIgnoreCase("%default%"))
			loadListToObj(v,filePath, ancestorC1, quiet);
		else
			loadListToObj(v,auxPath,ancestorC1, quiet);
		return v;
	}
	public static boolean checkForCMClass(String classType, String path)
	{
		return unsortedLoadClass(classType,path,true)!=null;
	}
	public static Object unsortedLoadClass(String classType, String path, boolean quiet)
	{
		if((path==null)||(path.length()==0))
			return null;
		try{
			String pathLess=path;
			if(pathLess.toUpperCase().endsWith(".CLASS"))
				pathLess=pathLess.substring(0,pathLess.length()-6);
			pathLess=pathLess.replace('/','.');
			pathLess=pathLess.replace('\\','.');
			if(classes.contains(pathLess))
				return ((CMObject)classes.get(pathLess)).newInstance();
		}catch(Exception e){}
		Vector V=new Vector();
		Objects set=classCode(classType);
		if(set==null)
			return null;
		if(!path.toUpperCase().endsWith(".CLASS"))
		{
			path=path.replace('.','/');
			path+=".class";
		}
		if(!loadListToObj(V,path,set.ancestor(),quiet))
			return null;
		if(V.size()==0) return null;
		return V.firstElement();
	}
	//Nothing calls this ever. Wonder what it's for.
	public static CMFile getClassDir(Class C) 
	{
		URL location = C.getProtectionDomain().getCodeSource().getLocation();
		String loc;
		if(location == null) {
			return null;
		}
		
		loc=location.getPath();
		loc=loc.replace('/',File.separatorChar);
		String floc=new java.io.File(".").getAbsolutePath();
		if(floc.endsWith(".")) floc=floc.substring(0,floc.length()-1);
		if(floc.endsWith(File.separator)) floc=floc.substring(0,floc.length()-File.separator.length());
		int x=floc.indexOf(File.separator);
		if(x>=0)floc=floc.substring(File.separator.length());
		x=loc.indexOf(floc);
		loc=loc.substring(x+floc.length());
		loc=loc.replace(File.separatorChar,'/');
		return new CMFile("/"+loc,null,false);
	}
	public static String ancestor(String code)
	{
		Objects num=classCode(code);
		if(num!=null)
			return num.ancestor();
		return "";
	}
	public static boolean loadListToObj(Object toThis, String filePath, String ancestor, boolean quiet)
	{
		//CMClass loader=new CMClass();
		Class ancestorCl=null;
		if (ancestor != null && ancestor.length() != 0)
		{
			try
			{
				ancestorCl = instance.loadClass(ancestor);
			}
			catch (ClassNotFoundException e)
			{
				if(!quiet)
					Log.sysOut("CMClass","WARNING: Couldn't load ancestor class: "+ancestor);
			}
		}
		return loadListToObj(toThis, filePath, ancestorCl, quiet);
	}
*/
}
