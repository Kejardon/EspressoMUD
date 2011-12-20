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
public class Sit extends StdCommand
{
	public Sit(){}

	private String[] access={"SIT","REST","R"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
/*
		if(CMLib.flags().isSitting(mob))
		{
			mob.tell("You are already sitting!");
			return false;
		}
*/
		if(commands.size()<=1)
		{
			mob.location().doMessage(CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.SIT),"<S-NAME> sit(s) down and take(s) a rest."));
			return false;
		}
		if("ON".equalsIgnoreCase((String)commands.get(1)))
			commands.remove(1);
		String possibleRideable=CMParms.combine(commands,1);
		Interactable I=CMLib.english().fetchInteractable(possibleRideable, false, 1, mob.location());
		if(I==null)
		{
			mob.tell("You don't see '"+possibleRideable+"' here.");
			return false;
		}
		String mountStr="<S-NAME> sit(s) on <T-NAME>.";
		CMMsg msg=CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.SIT),mountStr);
		mob.location().doMessage(msg);
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
