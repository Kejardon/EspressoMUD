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

public class Feed extends StdCommand
{
	public Feed(){access=new String[]{"FEED"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<3)
		{
			mob.tell("Feed who what?");
			return false;
		}
		commands.removeElementAt(0);

		int partition=CMLib.english().getPartitionIndex(commands, "to", commands.size()-1);

		String what=CMParms.combine(commands,partition);
		String whom=CMParms.combine(commands,0,partition);
		MOB target=mob.location().fetchInhabitant(whom);
		if(target==null)
		{
			mob.tell("I don't see "+whom+" here.");
			return false;
		}
		if(target.willFollowOrdersOf(mob))
		{
			Item item=mob.fetchInventory(what);
			if(item==null)
			{
				mob.tell("I don't see "+what+" here.");
				return false;
			}
			if((!(item instanceof Food))&&(!(item instanceof Drink)))
			{
				mob.tell("You might want to try feeding them something edibile or drinkable.");
				return false;
			}
			EnumSet<CMMsg.MsgCode> code=null;
			if(item instanceof Food)
				code=EnumSet.of(CMMsg.MsgCode.EAT);
			else
				code=EnumSet.of(CMMsg.MsgCode.DRINK);
			CMMsg msg=CMClass.getMsg(mob,target,item,code,"<S-NAME> feed(s) "+item.name()+" to <T-NAMESELF>.");
			mob.location().doMessage(msg);
			msg.returnMsg();
		}
		else
			mob.tell(target.name()+" won't let you.");
		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}