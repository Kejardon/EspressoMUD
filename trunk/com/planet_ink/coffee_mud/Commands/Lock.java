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
public class Lock extends StdCommand
{
	public Lock(){access=new String[]{"LOCK","LOC"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String whatTolock=CMParms.combine(commands,1);
		if(whatTolock.length()==0)
		{
			mob.tell("Lock what?");
			return false;
		}
		Interactable lockThis=CMLib.english().fetchInteractable(whatTolock, false, 1, mob, mob.location());

		if(lockThis==null)
		{
			mob.tell("You don't see '"+whatTolock+"' here.");
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,lockThis,null,EnumSet.of(CMMsg.MsgCode.LOCK),"<S-NAME> lock(s) <T-NAMESELF>.");
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}
	
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}