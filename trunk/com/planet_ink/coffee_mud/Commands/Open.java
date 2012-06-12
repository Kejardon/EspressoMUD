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
public class Open extends StdCommand
{
	public Open(){access=new String[]{"OPEN","OP","O"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String whatToOpen=CMParms.combine(commands,1);
		if(whatToOpen.length()==0)
		{
			mob.tell("Open what?");
			return false;
		}
		Interactable openThis=CMLib.english().fetchInteractable(whatToOpen, false, 1, mob, mob.location());
		if(openThis==null)
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,openThis,null,EnumSet.of(CMMsg.MsgCode.OPEN),("<S-NAME> open(s) <T-NAMESELF>."));
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}