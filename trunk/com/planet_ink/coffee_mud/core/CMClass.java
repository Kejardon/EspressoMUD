package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
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


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMClass extends ClassLoader
{
	protected static boolean debugging=false;
	protected static Hashtable classes=new Hashtable();
	public static EnumSet<Objects> ItemTypes = EnumSet.of(Objects.ITEM, Objects.WEARABLE, Objects.WEAPON);
	protected static final Vector<CMMsg> MSGS_CACHE=new Vector();
	protected static CMClass instance=new CMClass();
	public static CMClass instance(){return instance;}

	public enum Objects
	{
		RACE("com.planet_ink.coffee_mud.Races.interfaces.Race"),
		GENDER("com.planet_ink.coffee_mud.Races.interfaces.Gender"),
		MOB("com.planet_ink.coffee_mud.MOBS.interfaces.MOB"),
		COMMON("com.planet_ink.coffee_mud.Common.interfaces.CMCommon"),
		LOCALE("com.planet_ink.coffee_mud.Locales.interfaces.Room"),
		EXIT("com.planet_ink.coffee_mud.Exits.interfaces.Exit"),
		CLOSEABLE("com.planet_ink.coffee_mud.core.interfaces.Closeable"),
		ITEM("com.planet_ink.coffee_mud.Items.interfaces.Item"),
		WEAPON("com.planet_ink.coffee_mud.Items.interfaces.Weapon"),
		WEARABLE("com.planet_ink.coffee_mud.Items.interfaces.Wearable"),
		EFFECT("com.planet_ink.coffee_mud.Effects.interfaces.Effect"),
		BEHAVIOR("com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior"),
		AREA("com.planet_ink.coffee_mud.Areas.interfaces.Area"),
		COMMAND("com.planet_ink.coffee_mud.Commands.interfaces.Command"){
			private HashMap<String, Command> commandWordsMap=new HashMap<String, Command>();
			private SortedVector<String> commandWordsList=new SortedVector<String>();
			public synchronized void add(CMObject O){
				super.add(O);
				if(((Command)O).getAccessWords()==null) Log.errOut("C.O.COMMAND","Null accesswords: "+O.ID());
				else
				for(String S : ((Command)O).getAccessWords()){
					//S=S.trim().toUpperCase();	//No, ideally this should not be needed. Just make sure the input to this is always good.
					commandWordsMap.put(S,(Command)O);
					commandWordsList.addRandom(S); }
				}
			public synchronized boolean remove(CMObject O){
				if(!super.remove(O)) return false;
				for(String S : ((Command)O).getAccessWords()){
					commandWordsMap.remove(S);
					commandWordsList.remove(S); }
				return true; }
			public Command getCommand(String word, boolean exactOnly){
				word=word.trim().toUpperCase();
				Command C=commandWordsMap.get(word);
				if((exactOnly)||(C!=null)) return C;
				if(word.length()==0) return null;
				String potentialWord=commandWordsList.get((-Collections.binarySearch(commandWordsList, word))-1);
				if(potentialWord.startsWith(word)) return commandWordsMap.get(potentialWord);
				return null; } },
		LIBRARY("com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary"){
			public void add(CMObject O){
				super.add(O);
				CMLib.registerLibrary((CMLibrary)O); }
			//public void remove(CMObject O){}	//This really is not supported by CMLib. No, just don't remove libraries.
			},
		;
		private String ancestor;
		public HashMap<String, CMObject> options=new HashMap<String, CMObject>();
		
		private Objects(String S){ancestor=S;}
		
		//A little hackish but probably the best solution.
		public Command getCommand(String word, boolean exactOnly){return COMMAND.getCommand(word, exactOnly);}

		public String ancestor(){return ancestor;}
		public CMObject getAny() {
			Iterator<CMObject> I=options.values().iterator();
			if(I.hasNext()) return I.next();
			return null; }
		public CMObject get(String ID) { return options.get(ID); }
		public CMObject getNew(String ID) {
			CMObject O=options.get(ID);
			if(O!=null) return O.newInstance();
			Log.errOut(name(),"Failed request: "+ID);
			return null; }
		public void add(CMObject O){options.put(O.ID(), O);}
		public boolean remove(CMObject O){return (options.remove(O.ID())!=null);}
		public void initialize()
		{
			for(CMObject O : (CMObject[])options.values().toArray(new CMObject[0]))
				O.initializeClass();
		}
		public int size(){return options.size();}
		public Iterator<? extends CMObject> all(){ return options.values().iterator(); }
	}

	public static Objects getType(Object O)
	{
		if(O instanceof Gender) return Objects.GENDER;
		if(O instanceof Race) return Objects.RACE;
		if(O instanceof Effect) return Objects.EFFECT;
		if(O instanceof Room) return Objects.LOCALE;
		if(O instanceof MOB) return Objects.MOB;
		if(O instanceof Exit) return Objects.EXIT;
		if(O instanceof Behavior) return Objects.BEHAVIOR;
		if(O instanceof Area) return Objects.AREA;
		if(O instanceof CMLibrary) return Objects.LIBRARY;
		if(O instanceof CMCommon) return Objects.COMMON;
		if(O instanceof Command) return Objects.COMMAND;
		if(O instanceof Wearable) return Objects.WEARABLE;
		if(O instanceof Weapon) return Objects.WEAPON;
		if(O instanceof Item) return Objects.ITEM;
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
		return Objects.valueOf(name);
	}

	public static Objects classCode(Object O)
	{
		for(Objects e : Objects.values())
		{
			try{
				Class ancestorCl = instance.loadClass(e.ancestor());
				if(checkAncestry(O.getClass(),ancestorCl))
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
		return !loadListToObj(set,path,classCode(classType).ancestor(),quiet);
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
		if(classCode(classType)==null)
			return null;
		if(!path.toUpperCase().endsWith(".CLASS"))
		{
			path=path.replace('.','/');
			path+=".class";
		}
		if(!loadListToObj(V,path,classCode(classType).ancestor(),quiet))
			return null;
		if(V.size()==0) return null;
		return (Object)V.firstElement();
	}

	public static boolean checkForCMClass(String classType, String path)
	{
		return unsortedLoadClass(classType,path,true)!=null;
	}

	public static String ancestor(String code)
	{
		Objects num=classCode(code);
		if(num!=null)
			return num.ancestor();
		return "";
	}
	
	public static Object getClass(String calledThis)
	{
		String shortThis=calledThis;
		int x=shortThis.lastIndexOf('.');
		if(x>0) shortThis=shortThis.substring(x+1);
		Object set=null;
		Object thisItem=null;
		for(Objects e : Objects.values())
		{
			thisItem=e.get(calledThis);
			if(thisItem!=null) return thisItem;
		}
		try{ return ((CMObject)classes.get(calledThis)).newInstance();}catch(Exception e){}
		return thisItem;
	}

	public static Interactable getUnknown(String calledThis)
	{
		Interactable thisItem=(Interactable)Objects.ITEM.getNew(calledThis);
		if(thisItem==null) thisItem=(Interactable)Objects.WEARABLE.getNew(calledThis);
		if(thisItem==null) thisItem=(Interactable)Objects.WEAPON.getNew(calledThis);
		if(thisItem==null) thisItem=(Interactable)Objects.MOB.getNew(calledThis);
		if(thisItem==null) thisItem=(Interactable)Objects.EFFECT.getNew(calledThis);
		if((thisItem==null)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Unknown '"+calledThis+"'.");
		return thisItem;
	}

	//TODO: I'm curious if this actually happens and works. Should log and see sometime.
	public static boolean returnMsg(CMMsg msg)
	{
		synchronized(CMClass.MSGS_CACHE)
		{
			if(MSGS_CACHE.size()<10000)
			{
				MSGS_CACHE.addElement(msg);
				return true;
			}
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
				msg=(CMMsg)Objects.COMMON.get("DefaultMessage");
			else
			{
				msg=MSGS_CACHE.lastElement();
				MSGS_CACHE.removeElementAt(i-1);
			}
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
		M.setOthersCode(newAllCode.clone());
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

	public static boolean loadListToObj(Object toThis, String filePath, String ancestor, boolean quiet)
	{
		CMClass loader=new CMClass();
		Class ancestorCl=null;
		if (ancestor != null && ancestor.length() != 0)
		{
			try
			{
				ancestorCl = loader.loadClass(ancestor);
			}
			catch (ClassNotFoundException e)
			{
				if(!quiet)
					Log.sysOut("CMClass","WARNING: Couldn't load ancestor class: "+ancestor);
			}
		}
		return loadListToObj(toThis, filePath, ancestorCl, quiet);
	}

	public static boolean loadListToObj(Object toThis, String filePath, Class ancestorCl, boolean quiet)
	{
		CMClass loader=new CMClass();
		CMFile file=new CMFile(filePath,null,true);
		Vector fileList=new Vector();
		if(file.canRead())
		{
			if(file.isDirectory())
			{
				CMFile[] list=file.listFiles();
				for(int l=0;l<list.length;l++)
					if((list[l].getName().indexOf("$")<0)&&(list[l].getName().toUpperCase().endsWith(".CLASS")))
						fileList.addElement(list[l].getVFSPathAndName());
			}
			else
			{
				fileList.addElement(file.getVFSPathAndName());
			}
		}
		else
		{
			if(!quiet)
				Log.errOut("CMClass","Unable to access path "+file.getVFSPathAndName());
			return false;
		}
		for(int l=0;l<fileList.size();l++)
		{
			String item=(String)fileList.elementAt(l);
			if(item.startsWith("/")) item=item.substring(1);
			try
			{
				Object O=null;
				String packageName=item.replace('/','.');
				if(packageName.toUpperCase().endsWith(".CLASS"))
					packageName=packageName.substring(0,packageName.length()-6);
				Class C=loader.loadClass(packageName,true);
				if(C!=null)
				{
					if(!checkAncestry(C,ancestorCl))
					{
						if(!quiet)
							Log.sysOut("CMClass","WARNING: class failed ancestral check: "+packageName);
					}
					else
						O=C.newInstance();
				}
				if(O==null)
				{
					if(!quiet)
						Log.sysOut("CMClass","Unable to create class '"+packageName+"'");
				}
				else
				{
					String itemName=O.getClass().getName();
					int x=itemName.lastIndexOf(".");
					if(x>=0) itemName=itemName.substring(x+1);
					if(toThis instanceof Hashtable)
					{
						Hashtable H=(Hashtable)toThis;
						if(H.containsKey(itemName.trim().toUpperCase()))
							H.remove(itemName.trim().toUpperCase());
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
					{
						((Objects)toThis).remove((CMObject)O);
						((Objects)toThis).add((CMObject)O);
					}
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

	public static boolean checkAncestry(Class cl, Class ancestorCl)
	{
		if (cl == null) return false;
		if (cl.isPrimitive() || cl.isInterface()) return false;
		if ( Modifier.isAbstract( cl.getModifiers()) || !Modifier.isPublic( cl.getModifiers()) ) return false;
		if (ancestorCl == null) return true;
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
				return rawClassName(e);
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
				else
					throw e;
			}
			else
				throw e;
		}
		if (result==null){throw new ClassFormatError();}
		if (resolveIt){resolveClass(result);}
		if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
		classes.put(className, result);
		return result;
	}

	/**
	 * This is the required version of loadClass which is called
	 * both from loadClass above and from the internal function
	 * FindClassFromClass.
	 */
	public synchronized Class loadClass(String className, boolean resolveIt)
		throws ClassNotFoundException
	{
		String pathName=null;
		if(className.endsWith(".class")) className=className.substring(0,className.length()-6);
		pathName=className.replace('.','/')+".class";
		Class result = (Class)classes.get(className);
		if (result!=null)
		{
			if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
			return result;
		}
		if((super.findLoadedClass(className)!=null)
		||(className.indexOf("com.planet_ink.coffee_mud.")<0)
		||(className.startsWith("com.planet_ink.coffee_mud.core."))
		||(className.startsWith("com.planet_ink.coffee_mud.application."))
		||(className.indexOf(".interfaces.")>=0))
		{
			try{
				result=super.findSystemClass(className);
				if(result!=null)
				{
					if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
					return result;
				}
			} catch(Throwable t){}
		}
		/* Try to load it from our repository */
		CMFile CF=new CMFile(pathName,null,false);
		byte[] classData=CF.raw();
		if((classData==null)||(classData.length==0))
			throw new ClassNotFoundException("File "+pathName+" not readable!");
		result=finishDefineClass(className,classData,null,resolveIt);
		return result;
	}

	public static boolean loadClasses()
	{
		try
		{
			String prefix="com/planet_ink/coffee_mud/";
			debugging=CMSecurity.isDebugging("CLASSLOADER");
			
			Objects O=Objects.LIBRARY;
			loadListToObj(O, prefix+"Libraries/", O.ancestor(), false);
			if(O.size()==0) return false;
			if(CMLib.unregistered().length()>0)
			{
				Log.errOut("CMClass","Fatal Error: libraries are unregistered: "+CMLib.unregistered().substring(0,CMLib.unregistered().length()-2));
				return false;
			}

			O=Objects.COMMON;
			loadListToObj(O, prefix+"Common/", O.ancestor(), false);
			if(O.size()==0) return false;

			O=Objects.GENDER;
			loadListToObj(O, prefix+"Races/Genders/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Genders loaded    : "+O.size());
			if(O.size()==0) return false;

			O=Objects.RACE;
			loadListToObj(O, prefix+"Races/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Races loaded      : "+O.size());
			if(O.size()==0) return false;

			O=Objects.MOB;
			loadListToObj(O, prefix+"MOBS/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"MOB Types loaded  : "+O.size());
			if(O.size()==0) return false;

			O=Objects.EXIT;
			loadListToObj(O, prefix+"Exits/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Exit Types loaded : "+O.size());
			if(O.size()==0) return false;

			O=Objects.AREA;
			loadListToObj(O, prefix+"Areas/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Area Types loaded : "+O.size());
			if(O.size()==0) return false;

			O=Objects.LOCALE;
			loadListToObj(O, prefix+"Locales/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Locales loaded    : "+O.size());
			if(O.size()==0) return false;

			O=Objects.EFFECT;
			loadListToObj(O, prefix+"Effects/", O.ancestor(), false);
			loadListToObj(O, prefix+"Effects/Languages/", O.ancestor(), false);
//			loadListToObj(O, prefix+"Effects/Archon/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Effects loaded    : "+O.size());
			if(O.size()==0) return false;

			O=Objects.ITEM;
			loadListToObj(O, prefix+"Items/Basic/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Basic Items loaded: "+O.size());

			O=Objects.WEAPON;
			loadListToObj(O, prefix+"Items/Weapons/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Weapons loaded    : "+O.size());

			O=Objects.WEARABLE;
			loadListToObj(O, prefix+"Items/Armor/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Armor loaded      : "+O.size());

			if((Objects.ITEM.size()+Objects.WEAPON.size()+Objects.WEARABLE.size())==0)
				return false;

			O=Objects.BEHAVIOR;
			loadListToObj(O, prefix+"Behaviors/", O.ancestor(), false);
			Log.sysOut(Thread.currentThread().getName(),"Behaviors loaded  : "+O.size());
			if(O.size()==0) return false;

			O=Objects.COMMAND;
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
	public static Enum valueOf(Enum E, String S)
	{
		try{return E.valueOf(E.getClass(), S);}
		catch(IllegalArgumentException e){if(e.getMessage().startsWith("No")) return null;}
		try{return E.valueOf((Class)E.getClass().getSuperclass(), S);}
		catch(IllegalArgumentException e){}
		return null;
	}
	public static Enum valueOf(Class E, String S)
	{
		try{return Enum.valueOf(E, S);}
		catch(IllegalArgumentException e){if(e.getMessage().startsWith("No")) return null;}
		try{return Enum.valueOf((Class)E.getSuperclass(), S);}
		catch(IllegalArgumentException e){}
		return null;
	}
}
