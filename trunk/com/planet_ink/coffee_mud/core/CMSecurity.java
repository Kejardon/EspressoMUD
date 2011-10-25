package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
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

import java.io.File; // does some cmfile type stuff
import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/**
 * 
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

@SuppressWarnings("unchecked")
public class CMSecurity
{
	protected final long startTime=System.currentTimeMillis();
	private static CMSecurity secs=null;
	public CMSecurity()
	{
		if(secs==null) secs=this;
	}
	public static CMSecurity instance()
	{
		if(secs==null) return new CMSecurity();
		return secs;
	}
	
	protected Hashtable groups=new Hashtable();
	protected Vector compiledSysop=null;
	
	public static void setSysOp(String zapCheck)
	{
		instance().compiledSysop=CMLib.masking().maskCompile(zapCheck);
	}

	public static void clearGroups(){ instance().groups.clear();}
	
	public static void parseGroups(Properties page)
	{
		clearGroups();
		if(page==null) return;
		for(Enumeration e=page.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key.startsWith("GROUP_"))
			{
				addGroup(key.substring(6),(String)page.get(key));
			}
		}
	}
	
	public static void addGroup(String name, HashSet set)
	{
		name=name.toUpperCase().trim();
		if(instance().groups.containsKey(name)) 
			instance().groups.remove(name);
		instance().groups.put(name,set);
	}
	
	public static void addGroup(String name, Vector set)
	{
		HashSet H=new HashSet();
		for(int v=0;v<set.size();v++)
		{
			String s=(String)set.elementAt(v);
			H.add(s.trim().toUpperCase());
		}
		addGroup(name,H);
	}
	public static void addGroup(String name, String set)
	{
		addGroup(name,CMParms.parseCommas(set,true));
	}
	
	public static boolean isASysOp(MOB mob)
	{
		return CMLib.masking().maskCheck(instance().compiledSysop,mob,true);
	}
	
	
	public static boolean isStaff(MOB mob)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null)
			return false;
		return true;
	}
	
	public static Vector getAccessibleDirs(MOB mob, Room room)
	{
		Vector DIRSV=new Vector();
		if(isASysOp(mob)){ DIRSV.addElement("/"); return DIRSV; }
		if(mob==null) return DIRSV;
		if(mob.playerStats()==null)
			return DIRSV;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
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
	
	public static boolean canTraverseDir(MOB mob, Room room, String path)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
		if(path.equals("/")||path.equals(".")) path="";
		String areaPath=("AREA "+path).trim();
		String pathSlash=path+"/";
		String set=null;
		String setSlash=null;
		for(int v=0;v<V.size();v++)
		{
			set=((String)V.elementAt(v)).toUpperCase();
			if(set.startsWith("FS:"))
				set=set.substring(3).trim();
			else
			{
				HashSet H=(HashSet)instance().groups.get(set);
				if(H==null) continue;
				for(Iterator i=H.iterator();i.hasNext();)
				{
					set=((String)i.next()).toUpperCase();
					if(set.startsWith("FS:"))
						set=set.substring(3).trim();
					else
						continue;
					if((set.length()==0)||(path.length()==0)) 
						return true;
					setSlash=set.endsWith("/")?set:set+"/";
					if(set.startsWith(pathSlash)
					||path.startsWith(setSlash)
					||set.equals(path))
						return true;
				}
				continue;
			}
			if((set.length()==0)||(path.length()==0)) return true;
			if(set.startsWith("/")) set=set.substring(1);
			if(set.startsWith(pathSlash)
			||path.startsWith(set+"/")
			||set.equals(path))
			   return true;
		}
		return false;
	}
	
	public static boolean canAccessFile(MOB mob, Room room, String path, boolean isVFS)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
		if(path.equals("/")||path.equals(".")) path="";
		String set=null;
		String setSlash=null;
		for(int v=0;v<V.size();v++)
		{
			set=((String)V.elementAt(v)).toUpperCase();
			if(set.startsWith("FS:"))
				set=set.substring(3).trim();
			else
			{
				HashSet H=(HashSet)instance().groups.get(set);
				if(H==null) continue;
				for(Iterator i=H.iterator();i.hasNext();)
				{
					set=((String)i.next()).toUpperCase();
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
	
	public static Vector getSecurityCodes(MOB mob, Room room)
	{
		if((mob==null)||(mob.playerStats()==null)) return new Vector();
		Vector codes=(Vector)mob.playerStats().getSecurityGroups().clone();
		HashSet tried=new HashSet();
		for(Enumeration e=instance().groups.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			codes.addElement(key);
			HashSet H=(HashSet)instance().groups.get(key);
			for(Iterator i=H.iterator();i.hasNext();)
			{
				String s=(String)i.next();
				if(!tried.contains(s))
				{
					tried.add(s);
					if(isAllowed(mob,room,s))
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
	
	
	public static boolean isAllowedStartsWith(MOB mob, Room room, String code)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.startsWith(code))
			   return true;
			HashSet H=(HashSet)instance().groups.get(set);
			if(H!=null)
			{
				for(Iterator i=H.iterator();i.hasNext();)
				{
					String s=(String)i.next();
					if(s.startsWith(code))
						return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isAllowed(MOB mob, Room room, String code)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code))
			   return true;
			HashSet H=(HashSet)instance().groups.get(set);
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		return false;
	}
	public static boolean isAllowedStartsWith(MOB mob, String code)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.startsWith(code))
			   return true;
			HashSet H=(HashSet)instance().groups.get(set);
			if(H!=null)
			{
				for(Iterator i=H.iterator();i.hasNext();)
				{
					String s=(String)i.next();
					if(s.startsWith(code))
						return true;
				}
			}
		}
		return false;
	}
	public static boolean isAllowedEverywhere(MOB mob, String code)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code)) return true;
			HashSet H=(HashSet)instance().groups.get(set);
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		return false;
	}
	public static boolean isAllowedAnywhere(MOB mob, String code)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if(mob.playerStats()==null)
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code))
			   return true;
			HashSet H=(HashSet)instance().groups.get(set);
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		return false;
	}
	
	
	public static boolean isDebugging(String key)
	{ return (dbgVars.size()>0)&&dbgVars.contains(key);}
	
	public static boolean isDisabled(String key)
	{ return (disVars.size()>0)&&disVars.contains(key);}
	
	public static void setDebugVars(String vars)
	{
		Vector V=CMParms.parseCommas(vars.toUpperCase(),true);
		dbgVars.clear();
		for(int v=0;v<V.size();v++)
			dbgVars.add(((String)V.elementAt(v)).trim());
	}
	
	public static void setDisableVars(String vars)
	{
		Vector V=CMParms.parseCommas(vars.toUpperCase(),true);
		disVars.clear();
		for(int v=0;v<V.size();v++)
			disVars.add((String)V.elementAt(v));
	}
	public static void setDisableVar(String var, boolean delete)
	{
		if((var!=null)&&(delete)&&(disVars.size()>0))
			disVars.remove(var);
		else
		if((var!=null)&&(!delete))
			disVars.add(var);
	}
	protected static HashSet disVars=new HashSet();
	protected static HashSet dbgVars=new HashSet();

	public static long getStartTime(){return instance().startTime;}
	
	public static boolean isBanned(String login)
	{
		if((login==null)||(login.length()<=0))
			return false;
		login=login.toUpperCase();
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String str=(String)banned.elementAt(b);
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
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		{
			for(int b=0;b<banned.size();b++)
			{
				String B=(String)banned.elementAt(b);
				if((!B.equals(unBanMe))&&(B.trim().length()>0))
					newBanned.append(B+"\n");
			}
			Resources.updateFileResource("banned.ini",newBanned);
		}
	}
	
	public static void unban(int unBanMe)
	{
		StringBuffer newBanned=new StringBuffer("");
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		{
			for(int b=0;b<banned.size();b++)
			{
				String B=(String)banned.elementAt(b);
				if(((b+1)!=unBanMe)&&(B.trim().length()>0))
					newBanned.append(B+"\n");
			}
			Resources.updateFileResource("banned.ini",newBanned);
		}
	}
	
	public static int ban(String banMe)
	{
		if((banMe==null)||(banMe.length()<=0))
			return -1;
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String B=(String)banned.elementAt(b);
			if(B.equals(banMe))
				return b;
		}
		StringBuffer str=Resources.getFileResource("banned.ini",false);
		if(banMe.trim().length()>0) str.append(banMe+"\n");
		Resources.updateFileResource("banned.ini",str);
		return -1;
	}
}
