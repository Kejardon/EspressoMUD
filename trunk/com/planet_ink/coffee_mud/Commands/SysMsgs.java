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

public class SysMsgs extends StdCommand
{
	public SysMsgs(){access=new String[]{"SYSMSGS"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		PlayerStats ps=mob.playerStats();
		if(ps==null) return false;
		if(commands.size()>1)
		{
			if(commands.get(1).equalsIgnoreCase("on"))
			{
				if((ps.getBitmap()&PlayerStats.ATT_SYSOPMSGS)==0)
				{
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_SYSOPMSGS);
					mob.tell("Extended messages enabled.\r\n");
				}
				else
					mob.tell("Extended messages are already enabled.\r\n");
				return false;
			}
			else if(commands.get(1).equalsIgnoreCase("off"))
			{
				if((ps.getBitmap()&PlayerStats.ATT_SYSOPMSGS)>0)
				{
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()&~PlayerStats.ATT_SYSOPMSGS);
					mob.tell("Extended messages disabled.\r\n");
				}
				else
					mob.tell("Extended messages are already disabled.\r\n");
				return false;
			}
		}
		mob.tell(((ps.getBitmap()&PlayerStats.ATT_SYSOPMSGS)>0)?("Extended messages are currently enabled.\r\n"):("Extended messages are currently disabled.\r\n"));
		mob.tell("Use 'sysmsgs on' or 'sysmsgs off' to set.\r\n");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"SYSMSGS");}
}
