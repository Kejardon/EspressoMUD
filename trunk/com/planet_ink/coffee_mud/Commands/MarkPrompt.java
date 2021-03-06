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

public class MarkPrompt extends StdCommand
{
	public MarkPrompt(){access=new String[]{"MARKPROMPT"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		PlayerStats ps=mob.playerStats();
		if(ps==null) return false;
		PlayerAccount acct = ps.getAccount();
		if(commands.size()>1)
		{
			if(commands.get(1).equalsIgnoreCase("on"))
			{
				if(acct != null) acct.setBits(PlayerStats.ATT_GOAHEAD, true);
				if((ps.getBitmap()&PlayerStats.ATT_GOAHEAD)==0)
				{
					ps.setBitmap(ps.getBitmap()|PlayerStats.ATT_GOAHEAD);
					mob.tell("Prompt marks enabled.\r\n");
				}
				else
					mob.tell("Prompt marks are already enabled.\r\n");
				mob.session().setClientTelnetMode(Session.TELNET_GA,true);
				//mob.session().setServerTelnetMode(Session.TELNET_ANSI,true);
				return false;
			}
			else if(commands.get(1).equalsIgnoreCase("off"))
			{
				if(acct != null) acct.setBits(PlayerStats.ATT_GOAHEAD, false);
				if((ps.getBitmap()&PlayerStats.ATT_GOAHEAD)>0)
				{
					ps.setBitmap(ps.getBitmap()&~PlayerStats.ATT_GOAHEAD);
					mob.tell("Prompt marks disabled.\r\n");
				}
				else
					mob.tell("Prompt marks are already disabled.\r\n");
				mob.session().setClientTelnetMode(Session.TELNET_GA,false);
				//mob.session().setServerTelnetMode(Session.TELNET_ANSI,false);
				return false;
			}
		}
		mob.tell(((ps.getBitmap()&PlayerStats.ATT_GOAHEAD)>0)?("Prompt marks (IAC GA at end of prompt) are currently enabled.\r\n"):("Prompt marks (IAC GA at end of prompt) are currently disabled.\r\n"));
		mob.tell("Use 'markprompt on' or 'markprompt off' to set colour.\r\n");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
}