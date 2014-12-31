package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;

import java.io.File; // does some cmfile type stuff
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * The below notes are mostly out of date.
supported: AFTER, AHELP, ANNOUNCE, AT, BAN, BEACON, BOOT, CHARGEN
COPYMOBS, COPYITEMS, COPYROOMS, CMDSOCIALS, CMDROOMS,
CMDITEMS, CMDEXITS, CMDAREAS, CMDRACES, CMDCLASSES, NOPURGE, KILLBUGS,
KILLIDEAS, KILLTYPOS, DUMPFILE, GOTO, LOADUNLOAD, CMDPLAYERS
POSSESS, SHUTDOWN, SNOOP, STAT, SYSMSGS, TICKTOCK, TRANSFER, WHERE
RESET, RESETUTILS, KILLDEAD, MERGE, IMPORTROOMS, IMPORTMOBS, IMPORTITEMS
IMPORTPLAYERS, EXPORT, EXPORTPLAYERS, EXPORTFILE, RESTRING, PURGE, TASKS
ORDER (includes TAKE, GIVE, DRESS, mob passivity, all follow)
WIZINV (includes see WIZINV), CMDABILITIES
CMDMOBS (also prevents walkaways), KILLASSIST, ALLSKILLS, GMODIFY, CATALOG
SUPERSKILL (never fails skills), IMMORT (never dies), MXPTAGS, IDLEOK
PKILL, SESSIONS, TRAILTO, COMPONENTS, EXPERTISES, TITLES
FS:relative path from /coffeemud/ -- read/write access to regular file sys
VFS:relative path from /coffeemud/ -- read/write access to virtual file sys
LIST: (affected by killx, cmdplayers, loadunload, ban, nopurge,
cmditems, cmdmobs, cmdrooms, sessions, cmdareas, listadmin, stat
*/ 


public class CMSecurity
{
	protected final long startTime=System.currentTimeMillis();
	private static CMSecurity secs=null;
	protected static HashSet disVars=new HashSet();
	protected static HashSet dbgVars=new HashSet();
	protected Hashtable<String, HashSet<String>> groups=new Hashtable();
	protected Vector compiledSysop=null;
	public static CMSecurity instance()
	{
		if(secs==null) return new CMSecurity();
		return secs;
	}
	public CMSecurity()
	{
		if(secs==null) secs=this;
	}
	
	public static void setSysOp(String zapCheck)
	{
		instance().compiledSysop=CMLib.masking().maskCompile(zapCheck);
	}

	public static void clearGroups(){ instance().groups.clear();}
	
	public static void parseGroups(Properties page)
	{
		clearGroups();
		if(page==null) return;
		for(Enumeration<String> e=(Enumeration)page.keys();e.hasMoreElements();)
		{
			String key=e.nextElement();
			if(key.startsWith("GROUP_"))
			{
				addGroup(key.substring(6),(String)page.get(key));
			}
		}
	}
	
	public static void addGroup(String name, HashSet<String> set)
	{
		name=name.toUpperCase().trim();
		//if(instance().groups.containsKey(name))
			//instance().groups.remove(name);
		instance().groups.put(name,set);
	}
	
	public static void addGroup(String name, Vector<String> set)
	{
		HashSet<String> H=new HashSet();
		for(int v=0;v<set.size();v++)
			H.add(set.elementAt(v).trim().toUpperCase());
		addGroup(name,H);
	}
	public static void addGroup(String name, String set)
	{
		addGroup(name,CMParms.parseCommas(set,true));
	}
	
	public static boolean isASysOp(MOB mob)
	{
		if(mob==null||mob.playerStats()==null) return false;
		if(mob.playerStats().getSecurityGroups().contains("SYSOP")) return true;
		return CMLib.masking().maskCheck(instance().compiledSysop,mob,true);
	}
	
/*	public static boolean canTraverseDir(MOB mob, String path)
	{
		if(mob==null) return false;
		if(mob.playerStats()==null)
			return false;
		if(isASysOp(mob)) return true;
		HashSet<String> V=(HashSet)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
		if(path.equals("/")||path.equals(".")) path="";
		String set=null;
		for(Iterator<String> iter=V.iterator();iter.hasNext();)
		{
			set=iter.next().toUpperCase();
			if(set.startsWith("FS:"))
				set=set.substring(3).trim();
			else
			{
				HashSet<String> H=instance().groups.get(set);
				if(H==null) continue;
				for(Iterator<String> i=H.iterator();i.hasNext();)
				{
					set=i.next().toUpperCase();
					if(set.startsWith("FS:"))
						set=set.substring(3).trim();
					else
						continue;
					if((set.length()==0)||(path.length()==0)) 
						return true;
					if(set.startsWith(path)
					||path.startsWith(set))
						return true;
				}
				continue;
			}
			if((set.length()==0)||(path.length()==0)) return true;
			if(set.startsWith("/")) set=set.substring(1);
			if(set.startsWith(path)
			||path.startsWith(set))
			   return true;
		}
		return false;
	}*/
	public static boolean canAccessFile(MOB mob, String path)
	{
		if(mob==null) return false;
		if(mob.playerStats()==null)
			return false;
		if(isASysOp(mob)) return true;
		HashSet<String> V=(HashSet)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
		if(path.equals("/")||path.equals(".")) path="";
		String set=null;
		String setSlash=null;
		for(Iterator<String> iter=V.iterator();iter.hasNext();)
		{
			set=iter.next().toUpperCase();
			if(set.startsWith("FS:"))
				set=set.substring(3).trim();
			else
			{
				HashSet<String> H=instance().groups.get(set);
				if(H==null) continue;
				for(Iterator<String> i=H.iterator();i.hasNext();)
				{
					set=i.next().toUpperCase();
					if(set.startsWith("FS:"))
						set=set.substring(3).trim();
					else
						continue;
					if(set.length()==0) 
						return true;
					if(set.startsWith("/")) set=set.substring(1);
					setSlash=set.endsWith("/")?set:set+"/";
					if(path.startsWith(setSlash)
					||(path.equals(set)))
						return true;
				}
				continue;
			}
			if(set.length()==0) return true;
			if(set.startsWith("/")) set=set.substring(1);
			setSlash=set.endsWith("/")?set:set+"/";
			if(path.startsWith(setSlash)
			||(path.equals(set)))
			   return true;
		}
		return false;
	}

/*
	public static Vector getSecurityCodes(MOB mob, Room room)
	{
		if((mob==null)||(mob.playerStats()==null)) return new Vector();
		HashSet<String> codes=(HashSet)mob.playerStats().getSecurityGroups().clone();
		HashSet tried=new HashSet();
		for(Enumeration e=instance().groups.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			codes.addElement(key);	//Why this?
			HashSet H=(HashSet)instance().groups.get(key);
			for(Iterator i=H.iterator();i.hasNext();)
			{
				String s=(String)i.next();
				if(!tried.contains(s))
				{
					tried.add(s);
					if(isAllowed(mob,s))
					{
						if(s.startsWith("AREA ")) 
							s=s.substring(5).trim();
						if(!codes.contains(s))
							codes.addElement(s);
					}
				}
			}
		}
		return codes;
	}
*/
	
	public static boolean isAllowedStartsWith(MOB mob, String code)
	{
		if(mob==null) return false;
		if(mob.playerStats()==null)
			return false;
		if(isASysOp(mob)) return true;
		HashSet<String> V=(HashSet)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		
		for(Iterator<String> iter=V.iterator();iter.hasNext();)
		{
			String set=iter.next().toUpperCase();
			if(set.startsWith(code))
			   return true;
			HashSet<String> H=(HashSet)instance().groups.get(set);
			if(H!=null)
			{
				for(Iterator<String> i=H.iterator();i.hasNext();)
				{
					String s=i.next();
					if(s.startsWith(code))
						return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isAllowed(MOB mob, String code)
	{
		if(mob==null) return false;
		if(mob.playerStats()==null)
			return false;
		if(isASysOp(mob)) return true;
		HashSet<String> V=(HashSet)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		
		for(Iterator<String> iter=V.iterator();iter.hasNext();)
		{
			String set=iter.next().toUpperCase();
			if(set.equals(code))
			   return true;
			HashSet H=(HashSet)instance().groups.get(set);
			if((H!=null)&&(H.contains(code)))
				return true;
		}
		return false;
	}

	public static boolean isDebugging(String key)
	{ return (dbgVars.size()>0)&&dbgVars.contains(key);}
	
	public static boolean isDisabled(String key)
	{ return (disVars.size()>0)&&disVars.contains(key);}
	
	public static void setDebugVars(String vars)
	{
		Vector<String> V=CMParms.parseCommas(vars.toUpperCase(),true);
		dbgVars.clear();
		for(int v=0;v<V.size();v++)
			dbgVars.add(V.elementAt(v).trim());
	}
	
	public static void setDisableVars(String vars)
	{
		Vector<String> V=CMParms.parseCommas(vars.toUpperCase(),true);
		disVars.clear();
		for(int v=0;v<V.size();v++)
			disVars.add(V.elementAt(v).trim());
	}
	public static void setDisableVar(String var, boolean delete)
	{
		if(delete)
			disVars.remove(var);
		else
			disVars.add(var);
	}

	public static long getStartTime(){return instance().startTime;}
	
	public static boolean isBanned(String login)
	{
		if((login==null)||(login.length()<=0))
			return false;
		login=login.toUpperCase();
		LinkedList<String> banned=(LinkedList)Resources.getResource("BANNEDLIST");
		if(banned==null)
		{
			banned=Resources.getFileLineList(Resources.getFileResource("banned.ini",false));
			Resources.submitResource("BANNEDLIST",banned);
		}
		if((banned!=null)&&(banned.peek()!=null))
		for(Iterator<String> iter=banned.iterator();iter.hasNext();)
		{
			String str=iter.next();
			if(str.length()>0)
			{
				if(str.equals("*")||((str.indexOf("*")<0))&&(str.equals(login))) return true;
				else
				if(str.startsWith("*")&&str.endsWith("*")&&(login.indexOf(str.substring(1,str.length()-1))>=0)) return true;
				else
				if(str.startsWith("*")&&(login.endsWith(str.substring(1)))) return true;
				else
				if(str.endsWith("*")&&(login.startsWith(str.substring(0,str.length()-1)))) return true;
			}
		}
		return false;
	}

	public static void unban(String unBanMe)
	{
		if((unBanMe==null)||(unBanMe.length()<=0))
			return;
		StringBuffer newBanned=new StringBuffer("");
		LinkedList<String> banned=(LinkedList)Resources.getResource("BANNEDLIST");
		if(banned==null)
		{
			banned=Resources.getFileLineList(Resources.getFileResource("banned.ini",false));
			Resources.submitResource("BANNEDLIST",banned);
		}
		if((banned!=null)&&(banned.peek()!=null))
		{
			for(Iterator<String> iter=banned.iterator();iter.hasNext();)
			{
				String B=iter.next();
				if((!B.equals(unBanMe))&&(B.trim().length()>0))
					newBanned.append(B+"\n");
			}
			Resources.updateFileResource("banned.ini",newBanned);
			Resources.removeResource("BANNEDLIST");
		}
	}
	
	public static void unban(int unBanMe)
	{
		StringBuffer newBanned=new StringBuffer("");
		LinkedList<String> banned=(LinkedList)Resources.getResource("BANNEDLIST");
		if(banned==null)
		{
			banned=Resources.getFileLineList(Resources.getFileResource("banned.ini",false));
			Resources.submitResource("BANNEDLIST",banned);
		}
		if((banned!=null)&&(banned.peek()!=null))
		{
			int b=0;
			for(Iterator<String> iter=banned.iterator();iter.hasNext();)
			{
				String B=iter.next();
				if(((b+1)!=unBanMe)&&(B.trim().length()>0))
					newBanned.append(B+"\n");
				b++;
			}
			Resources.updateFileResource("banned.ini",newBanned);
			Resources.removeResource("BANNEDLIST");
		}
	}
	
	public static int ban(String banMe)
	{
		if((banMe==null)||(banMe.length()<=0))
			return -1;
		banMe=banMe.toUpperCase();
		LinkedList<String> banned=(LinkedList)Resources.getResource("BANNEDLIST");
		if(banned==null)
		{
			banned=Resources.getFileLineList(Resources.getFileResource("banned.ini",false));
			Resources.submitResource("BANNEDLIST",banned);
		}
		//String B;
		if(banned!=null)
		{
			Iterator<String> iter=banned.iterator();
			for(int b=0;iter.hasNext();b++)
			{
				String B=iter.next();
				if(B.equals(banMe))
					return b;
			}
		}
		StringBuffer str=Resources.getFileResource("banned.ini",false);
		if(banMe.trim().length()>0) str.append(banMe+"\n");
		Resources.updateFileResource("banned.ini",str);
		Resources.removeResource("BANNEDLIST");
		return -1;
	}
/*	public static Vector getAccessibleDirs(MOB mob, Room room)
	{
		Vector DIRSV=new Vector();
		if(isASysOp(mob)){ DIRSV.addElement("/"); return DIRSV; }
		if(mob==null) return DIRSV;
		if(mob.playerStats()==null)
			return DIRSV;
		Vector V=(HashSet)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return DIRSV;
		String set=null;
		for(int v=0;v<V.size();v++)
		{
			set=((String)V.elementAt(v)).toUpperCase();
			if(set.startsWith("FS:"))
			{
				set=set.substring(3).trim();
				DIRSV.addElement("//"+set);
			}
			else
			{
				HashSet H=(HashSet)instance().groups.get(set);
				if(H==null) continue;
				for(Iterator i=H.iterator();i.hasNext();)
				{
					set=((String)i.next()).toUpperCase();
					if(set.startsWith("FS:"))
					{
						set=set.substring(3).trim();
						DIRSV.addElement("//"+set);
					}
				}
			}
		}
		String dir=null;
		for(int d=0;d<DIRSV.size();d++)
		{
			dir=(String)DIRSV.elementAt(d);
			if(dir.startsWith("//"))
			{
				dir=dir.substring(2);
				String path="";
				String subPath=null;
				while(dir.startsWith("/")) dir=dir.substring(1);
				while(dir.length()>0)
				{
					while(dir.startsWith("/")) dir=dir.substring(1);
					int x=dir.indexOf('/');
					subPath=dir;
					if(x>0)
					{ 
						subPath=dir.substring(0,x).trim(); 
						dir=dir.substring(x+1).trim();
					}
					else
					{
						subPath=dir.trim();
						dir="";
					}
					CMFile F=new CMFile(path,null,true,false);
					if((F.exists())&&(F.canRead())&&(F.isDirectory()))
					{
						String[] files=F.list();
						for(int f=0;f<files.length;f++)
							if(files[f].equalsIgnoreCase(subPath))
							{
								if(path.length()>0)
									path+="/";
								path+=files[f];
								break;
							}
					}
				}
				DIRSV.setElementAt("//"+path,d);
			}
		}
		return DIRSV;
	}
	public static boolean hasAccessibleDir(MOB mob, Room room)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		String set=null;
		for(int v=0;v<V.size();v++)
		{
			set=((String)V.elementAt(v)).toUpperCase();
			if(set.startsWith("FS:"))
				return true;
			else
			{
				HashSet H=(HashSet)instance().groups.get(set);
				if(H==null) continue;
				for(Iterator i=H.iterator();i.hasNext();)
				{
					set=((String)i.next()).toUpperCase();
					if(set.startsWith("FS:"))
						return true;
					else
						continue;
				}
				continue;
			}
		}
		return false;
	}
*/
}
