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
public class Read extends StdCommand
{
	public Read(){}

	private String[] access={"READ"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
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
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
