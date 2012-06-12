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
public class Goto extends At
{
	public Goto(){access=new String[]{"GOTO"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Room room=null;
		if(commands.size()<2)
		{
			mob.tell("Go where? Try a Room ID, player name, area name, or room text!");
			return false;
		}
		commands.removeElementAt(0);
		StringBuffer cmd = new StringBuffer(CMParms.combine(commands,0));
		Vector<Integer> stack=(Vector)Resources.getResource("GOTOS_FOR_"+mob.name().toUpperCase());
		if(stack==null)
		{
			stack=new Vector();
			Resources.submitResource("GOTOS_FOR_"+mob.name().toUpperCase(),stack);
		}
		else
		if(stack.size()>10)
			stack.removeElementAt(0);
		Room curRoom=mob.location();
		if("PREVIOUS".startsWith(cmd.toString().toUpperCase()))
		{
			if(stack.size()==0)
				mob.tell("Your previous room stack is empty.");
			else
			{
				room=SIDLib.ROOM.get(stack.lastElement());
				stack.removeElementAt(stack.size()-1);
			}
		}
		else
			room=CMLib.map().findWorldRoomLiberally(mob,cmd.toString(),"RIPMA",1000,120);

		if(room==null)
		{
			mob.tell("Goto where? Try a Room ID, player name, area name, room text, or PREVIOUS!");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,"GOTO"))
		{
			mob.tell("You aren't powerful enough to do that. Try 'GO'.");
			return false;
		}
		if(curRoom==room)
		{
			mob.tell("Done.");
			return false;
		}
		if(!"PREVIOUS".startsWith(cmd.toString().toUpperCase()))
		{
			if((stack.size()==0)||(stack.lastElement()!=mob.location().saveNum()))
				stack.addElement(mob.location().saveNum());
		}
		room.bringHere(mob.body(),false);
		CMLib.commands().postLook(mob);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"GOTO");}
}