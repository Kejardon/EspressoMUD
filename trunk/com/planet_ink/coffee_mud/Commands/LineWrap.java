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
public class LineWrap extends StdCommand
{
	public LineWrap(){access=new String[]{"LINEWRAP"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if((mob==null)||(mob.playerStats()==null))
			return false;
		
		if(commands.size()<2)
		{
			String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
			mob.tell("Change your line wrap to what? Your current line wrap setting is: "+wrap+". Enter a number larger than 10 or 'disable'.");
			return false;
		}
		String newWrap=CMParms.combine(commands,1);
		int newVal=mob.playerStats().getWrap();
		if((CMath.isInteger(newWrap))&&(CMath.s_int(newWrap)>10))
			newVal=CMath.s_int(newWrap);
		else
		if("DISABLED".startsWith(newWrap.toUpperCase()))
			newVal=0;
		else
		{
			mob.tell("'"+newWrap+"' is not a valid setting. Enter a number larger than 10 or 'disable'.");
			return false;
		}
		mob.playerStats().setWrap(newVal);
		String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
		mob.tell("Your new line wrap setting is: "+wrap+".");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}