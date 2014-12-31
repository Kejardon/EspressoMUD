package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Display extends StdCommand
{
	public Display(){access=new String[]{"DISPLAY","SHOW"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.remove(0);
		if(commands.size()<2)
		{
			mob.tell("Who should I show what?");
			return false;
		}

		MOB recipient=mob.location().fetchInhabitant(commands.get(0));
		if(recipient==null)
		{
			mob.tell("I don't see anyone called "+commands.get(0)+" here.");
			return false;
		}
		commands.remove(0);

		String thingToGive=CMParms.combine(commands,0);
		Item giveThis=mob.fetchInventory(thingToGive);

		if(giveThis==null)
		{
			mob.tell("You don't seem to be carrying that.");
			return false;
		}
		CMMsg newMsg=CMClass.getMsg(recipient,giveThis,mob,EnumSet.of(CMMsg.MsgCode.LOOK),"<O-NAME> show(s) <T-NAME> to <S-NAMESELF>.");
		mob.location().doMessage(newMsg);
		newMsg.returnMsg();
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}