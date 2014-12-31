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

public class Read extends StdCommand
{
	public Read(){access=new String[]{"READ"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<2)
		{
			mob.tell("Read what?");
			return false;
		}
		commands.remove(0);

		Interactable thisThang=CMLib.english().fetchInteractable(CMParms.combine(commands,0),false,1,mob,mob.location());
		//TODO: How do I want Readables to work exactly?
/*
		if((thisThang==null)||((!(thisThang instanceof Item)&&(!(thisThang instanceof Exit)))))
		{
			mob.tell("You don't seem to have that.");
			return;
		}
		if(thisThang instanceof Item)
		{
			Item thisItem=(Item)thisThang;
			if((CMLib.flags().isGettable(thisItem))&&(!mob.isMine(thisItem)))
			{
				mob.tell("You don't seem to be carrying that.");
				return;
			}
		}
		String srcMsg="<S-NAME> read(s) <T-NAMESELF>.";
		String soMsg=(mob.isMine(thisThang)?srcMsg:null);
		String tMsg=theRest;
		if((tMsg==null)||(tMsg.trim().length()==0)||(thisThang instanceof MOB)) tMsg=soMsg;
		mob.location().doMessage(CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_READ,srcMsg,CMMsg.MSG_READ,tMsg,CMMsg.MSG_READ,soMsg));
*/
		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}
