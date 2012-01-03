package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Unload extends StdCommand
{
	public Unload(){}

	private String[] access={"UNLOAD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String list="CLASS, HELP, USER, AREA, ALL, [FILENAME]";
		
		String str=CMParms.combine(commands,1);
		if(str.length()==0)
		{
			mob.tell("UNLOAD what? Try "+list);
			return false;
		}
		String what=(String)commands.elementAt(1);
		if((what.equalsIgnoreCase("CLASS")||(CMClass.valueOf(CMClass.Objects.class, what)!=null))
		&&(CMSecurity.isASysOp(mob)))
		{
			if(commands.size()<3)
			{
				mob.tell("Unload which "+what+"?");
				return false;
			}
			if(what.equalsIgnoreCase("CLASS"))
			{
				Object O=CMClass.getClass((String)commands.elementAt(2));
				if(O!=null)
				{
					CMClass.Objects x=CMClass.classCode(O);
					if(x!=null) what=x.toString();
				}
			}
			if(CMClass.valueOf(CMClass.Objects.class, what)==null)
				mob.tell("Don't know how to load a '"+what+"'.  Try one of the following: "+list);
			else
			{
				commands.removeElementAt(0);
				commands.removeElementAt(0);
				for(int i=0;i<commands.size();i++)
				{
					String name=(String)commands.elementAt(0);
					Object O=CMClass.getClass(name);
					if(!(O instanceof CMObject))
						mob.tell("Class '"+name+"' was not found in the class loader.");
					else
					if(!CMClass.classCode(what).remove((CMObject)O))
						mob.tell("Failed to unload class '"+name+"' from the class loader.");
					else
						mob.tell("Class '"+name+"' was unloaded.");
				}
			}
			return false;
		}
		else
		if(str.equalsIgnoreCase("help"))
		{
			CMFile F=new CMFile("//resources/help",mob,false);
			if((F.exists())&&(F.canRead())&&(F.canWrite())&&(F.isDirectory()))
			{
				CMLib.help().unloadHelpFile(mob);
				return false;
			}
			mob.tell("No access to help.");
		}
		else
		if((str.equalsIgnoreCase("all"))&&(CMSecurity.isASysOp(mob)))
		{
			mob.tell("All soft resources unloaded.");
			Resources.clearResources();
			CMLib.help().unloadHelpFile(mob);
			return false;
		}
		else
		// User Unloading
		if((((String)commands.elementAt(1)).equalsIgnoreCase("USER"))
		&&(mob.session()!=null)
		&&(CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")))
		{
			String which=CMParms.combine(commands,2);
			Vector users=new Vector();
			if(which.equalsIgnoreCase("all"))
				for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
					users.addElement((MOB)e.nextElement());
			else
			{
				MOB M=CMLib.players().getPlayer(which);
				if(M==null)
				{
					mob.tell("No such user as '"+which+"'!");
					return false;
				}
				users.addElement(M);
			}
			boolean saveFirst=mob.session().confirm("Save first (Y/n)?","Y");
			for(int u=0;u<users.size();u++)
			{
				MOB M=(MOB)users.elementAt(u);
				if(M.session()!=null)
				{ 
					if(M!=mob)
					{
						if(M.session()!=null) M.session().kill(false,false,false);
						while(M.session()!=null){try{Thread.sleep(100);}catch(Exception e){}}
						if(M.session()!=null) M.session().kill(true,true,true);
					}
					else
						mob.tell("Can't unload yourself -- a destroy is involved, which would disrupt this process.");
				}
				if(saveFirst)
				{
					CMLib.database().saveObject(M);
				}
			}
			int done=0;
			for(int u=0;u<users.size();u++)
			{
				MOB M=(MOB)users.elementAt(u);
				if(M!=mob)
				{
					done++;
					if(M.session()!=null) M.session().kill(true,true,true);
					CMLib.players().delPlayer(M);
					M.destroy();
				}
			}
			
			mob.tell(done+" user(s) unloaded.");
			return true;
		}
		else
		// Area Unloading
		if((((String)commands.elementAt(1)).equalsIgnoreCase("AREA"))
		&&(CMSecurity.isAllowed(mob, mob.location(), "CMDAREAS")))
		{
			String which=CMParms.combine(commands,2);
			Area A=null;
			if(which.length()>0)
				A=CMLib.map().getArea(which);
			if(A==null)
				mob.tell("Unknown Area '"+which+"'.  Use AREAS.");
			else
			{
				return false;
			}
		}
		else
		{
			CMFile F1=new CMFile(str,mob,false,true);
			if(!F1.exists())
			{
				int x=str.indexOf(':');
				if(x<0) x=str.lastIndexOf(' ');
				if(x>=0) F1=new CMFile(str.substring(x+1).trim(),mob,false,true);
			}
			if(!F1.exists())
			{
				F1=new CMFile(Resources.buildResourcePath(str),mob,false,true);
				if(!F1.exists())
				{
					int x=str.indexOf(':');
					if(x<0) x=str.lastIndexOf(' ');
					if(x>=0) F1=new CMFile(Resources.buildResourcePath(str.substring(x+1).trim()),mob,false,true);
				}
			}
			if(F1.exists())
			{
				CMFile F2=new CMFile(F1.getLocalPathAndName(),mob,true);
				if((!F2.exists())||(!F2.canRead()))
				{
					mob.tell("Inaccessible resource: '"+str+"'");
					return false;
				}
			}
			
			Vector V=Resources.findResourceKeys(str);
			if(V.size()==0)
			{
				mob.tell("Unknown resource '"+str+"'.  Use LIST RESOURCES.");
				return false;
			}
			for(int v=0;v<V.size();v++)
			{
				String key=(String)V.elementAt(v);
				Resources.removeResource(key);
				mob.tell("Resource '"+key+"' unloaded.");
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"LOADUNLOAD");}
}