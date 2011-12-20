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
public class SysMsgs extends StdCommand
{
	public SysMsgs(){}

	private String[] access={"SYSMSGS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		PlayerStats ps=mob.playerStats();
		if(ps==null) return false;
		if(commands.size()>1)
		{
			if(((String)commands.get(1)).equalsIgnoreCase("on"))
			{
				if((ps.getBitmap()&PlayerStats.ATT_SYSOPMSGS)==0)
				{
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_SYSOPMSGS);
					mob.tell("Extended messages enabled.\n\r");
				}
				else
					mob.tell("Extended messages are already enabled.\n\r");
				return false;
			}
			else if(((String)commands.get(1)).equalsIgnoreCase("off"))
			{
				if((ps.getBitmap()&PlayerStats.ATT_SYSOPMSGS)>0)
				{
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()&~PlayerStats.ATT_SYSOPMSGS);
					mob.tell("Extended messages disabled.\n\r");
				}
				else
					mob.tell("Extended messages are already disabled.\n\r");
				return false;
			}
		}
		mob.tell(((ps.getBitmap()&PlayerStats.ATT_SYSOPMSGS)>0)?("Extended messages are currently enabled.\n\r"):("Extended messages are currently disabled.\n\r"));
		mob.tell("Use 'sysmsgs on' or 'sysmsgs off' to set.\n\r");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"SYSMSGS");}

	
}
