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
@SuppressWarnings("unchecked")
public class Throw extends StdCommand
{
	public Throw(){access=new String[]{"THROW","TOSS"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<3)
		{
			mob.tell("Throw what, where or at whom?");
			return false;
		}
		commands.removeElementAt(0);
		
		int partition=CMLib.english().getPartitionIndex(commands, "at", commands.size()-1);
		
		String str=CMParms.combine(commands,partition);
		String what=CMParms.combine(commands,0,partition);
		Item item=(Item)CMLib.english().fetchInteractable(what,false,1,mob.getItemCollection());
		if(item==null)
		{
			mob.tell("You don't seem to have a '"+what+"'!");
			return false;
		}

		Interactable target=CMLib.english().fetchInteractable(str,false,1,mob.location());
		if(target==null)
		{
			mob.tell("You don't see a '"+str+"'!");
			return false;
		}

		CMMsg msg=CMClass.getMsg(mob,target,item,EnumSet.of(CMMsg.MsgCode.THROW),"<S-NAME> throw(s) <O-NAME> at <T-NAMESELF>.");
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}
