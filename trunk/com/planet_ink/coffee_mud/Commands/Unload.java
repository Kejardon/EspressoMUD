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
	public Unload(){access=new String[]{"UNLOAD"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String list="CLASS, (CLASS TYPE), HELP, ALL, (RESOURCE SUBSTRING)";
		
		String str=CMParms.combine(commands,1);
		if(str.length()==0)
		{
			mob.tell("UNLOAD what? Try "+list);
			return false;
		}
		String what=commands.elementAt(1);
		if((what.equalsIgnoreCase("CLASS")||(CMClass.classCode(what)!=null))
		&&(CMSecurity.isASysOp(mob)))
		{
			if(commands.size()<3)
			{
				mob.tell("Unload which "+what+"?");
				return false;
			}
			CMClass.Objects classType=null;
			if(what.equalsIgnoreCase("CLASS"))
			{
				CMObject O=CMClass.getClass(commands.elementAt(2));
				if(O!=null)
					classType=CMClass.classCode(O);
			}
			else
				classType=CMClass.classCode(what);
			if(classType==null)
				mob.tell("Don't know how to unload '"+str+"'.  Try one of the following: "+list);	//should only fail on class, with invalid arguments
			else for(int i=2;i<commands.size();i++)
			{
				String name=commands.elementAt(i);
				CMObject O=CMClass.getClass(name);
				if(O==null)
					mob.tell("Class '"+name+"' was not found in the class loader.");
				else if(!classType.remove(O))
					mob.tell("Failed to unload class '"+name+"' from the class loader.");
				else
					mob.tell("Class '"+name+"' was unloaded.");
			}
			return false;
		}
		else if(str.equalsIgnoreCase("help"))
		{
			CMFile F=new CMFile("resources/help",mob,false);
			if((F.canWrite())&&(F.isDirectory()))
			{
				CMLib.help().unloadHelpFile(mob);
				return false;
			}
			mob.tell("No access to help.");
		}
		else if((str.equalsIgnoreCase("all"))&&(CMSecurity.isASysOp(mob)))
		{
			mob.tell("All soft resources unloaded.");
			Resources.clearResources();
			CMLib.help().unloadHelpFile(mob);
			return false;
		}
		else
		{
			ArrayList<String> V=Resources.findResourceKeys(str);
			if(V.size()==0)
			{
				mob.tell("Unknown resource '"+str+"'.  Use LIST RESOURCES.");
				return false;
			}
			for(int v=0;v<V.size();v++)
			{
				String key=V.get(v);
				Resources.removeResource(key);
				mob.tell("Resource '"+key+"' unloaded.");
			}
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"LOADUNLOAD");}
}