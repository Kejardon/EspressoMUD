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

public class ANSI extends StdCommand
{
	public ANSI(){access=new String[]{"ANSI","COLOR","COLOUR"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		PlayerStats ps=mob.playerStats();
		if(ps==null) return false;
		PlayerAccount acct = ps.getAccount();
		if(commands.size()>1)
		{
			if(commands.get(1).equalsIgnoreCase("on"))
			{
				if(acct != null) acct.setBits(PlayerStats.ATT_ANSI, true);
				if(!ps.hasBits(PlayerStats.ATT_ANSI))
				{
					ps.setBits(PlayerStats.ATT_ANSI, true);
					mob.tell("^!ANSI^N ^Hcolour^N enabled.\r\n");
				}
				else
					mob.tell("^!ANSI^N is ^Halready^N enabled.\r\n");
				mob.session().setClientTelnetMode(Session.TELNET_ANSI,true);
				//mob.session().setServerTelnetMode(Session.TELNET_ANSI,true);
				return false;
			}
			else if(commands.get(1).equalsIgnoreCase("off"))
			{
				if(acct != null) acct.setBits(PlayerStats.ATT_ANSI, false);
				if(ps.hasBits(PlayerStats.ATT_ANSI))
				{
					ps.setBits(PlayerStats.ATT_ANSI, false);
					mob.tell("ANSI colour disabled.\r\n");
				}
				else
					mob.tell("ANSI is already disabled.\r\n");
				mob.session().setClientTelnetMode(Session.TELNET_ANSI,false);
				//mob.session().setServerTelnetMode(Session.TELNET_ANSI,false);
				return false;
			}
		}
		mob.tell((ps.hasBits(PlayerStats.ATT_ANSI))?("^!ANSI^N is ^Hcurrently^N enabled.\r\n"):("ANSI is currently disabled.\r\n"));
		mob.tell("Use 'ansi on' or 'ansi off' to set colour.\r\n");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
}