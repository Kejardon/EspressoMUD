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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Knock extends StdCommand
{
	public Knock(){}

	private String[] access={"KNOCK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<=1)
		{
			mob.tell("Knock on what?");
			return false;
		}
		int volume=-1;
		if((commands.size()>2)&&(CMath.isInteger((String)commands.lastElement())))
		{
			volume=CMath.s_int((String)commands.lastElement());
			commands.remove(commands.size()-1);
		}
		String knockWhat=CMParms.combine(commands,1).toUpperCase();
		Interactable I=CMLib.english().fetchInteractable(knockWhat, false, 1, mob.location());
		if(I!=null)
		{
			CMMsg msg=CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.KNOCK),"<S-NAME> knock(s) on <T-NAMESELF>.");
			if(volume!=-1)
				msg.setValue(volume);
			mob.location().doMessage(msg);
		}
		else
		{
			mob.tell("You don't see '"+knockWhat+"' here.");
			return false;
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}