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
public class DarkenDisplay extends StdCommand
{
	public DarkenDisplay(){access=new String[]{"DARKENDISPLAY","LIGHTENDISPLAY"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		PlayerStats ps=mob.playerStats();
		Session S=mob.session();
		if((ps==null)||(S==null)) return false;
		PlayerAccount acct = ps.getAccount();
		if(Character.toUpperCase(commands.get(0).charAt(0))=='D')
		{
			if(acct != null) acct.setBits(PlayerStats.ATT_DARKENDISPLAY, true);
			if(!ps.hasBits(PlayerStats.ATT_DARKENDISPLAY))
			{
				ps.setBits(PlayerStats.ATT_DARKENDISPLAY, true);
				S.setOther(PlayerStats.ATT_DARKENDISPLAY, true);
				mob.tell("Display defaulting to darker colors.\r\n");
			}
			else
				mob.tell("Display already darkened.\r\n");
			S.setOther(PlayerStats.ATT_DARKENDISPLAY, true);
			return false;
		}	//else { v that stuff v }
		if(acct != null) acct.setBits(PlayerStats.ATT_DARKENDISPLAY, false);
		if(ps.hasBits(PlayerStats.ATT_DARKENDISPLAY))
		{
			ps.setBits(PlayerStats.ATT_DARKENDISPLAY, false);
			mob.tell("Display defaulting to lighter colors.\r\n");
		}
		else
			mob.tell("Display already lightened.\r\n");
		S.setOther(PlayerStats.ATT_DARKENDISPLAY, false);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}