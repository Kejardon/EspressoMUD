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
public class Sniff extends StdCommand
{
	public Sniff(){}

	private String[] access={"SNIFF","SMELL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		
		if(commands.size()>1)
		{
			Interactable thisThang=null;
			
			String ID=CMParms.combine(commands,1);
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;
			else
				thisThang=CMLib.english().fetchInteractable(ID,false,1,mob,mob.location());
			if(thisThang!=null)
			{
				String name=" <T-NAMESELF>";
				if(thisThang==mob.location())
					name=" around";
				mob.location().doMessage(CMClass.getMsg(mob,thisThang,null,EnumSet.of(CMMsg.MsgCode.SNIFF),"<S-NAME> sniff(s)"+name+"."));
			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,mob.location(),null,EnumSet.of(CMMsg.MsgCode.SNIFF),"<S-NAME> sniff(s) around.");
			mob.location().doMessage(msg);
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
