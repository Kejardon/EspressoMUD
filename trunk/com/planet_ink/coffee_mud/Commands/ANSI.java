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
public class ANSI extends StdCommand
{
	public ANSI(){}

	private String[] access={"ANSI","COLOR","COLOUR"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		PlayerStats ps=mob.playerStats();
		if(ps==null) return false;
		PlayerAccount acct = ps.getAccount();
		if(commands.size()>1)
		{
			if(((String)commands.get(1)).equalsIgnoreCase("on"))
			{
				if(acct != null) acct.setFlag(PlayerAccount.FLAG_ANSI, true);
				if((ps.getBitmap()&PlayerStats.ATT_ANSI)==0)
				{
					ps.setBitmap(ps.getBitmap()|PlayerStats.ATT_ANSI);
					mob.tell("^!ANSI^N ^Hcolour^N enabled.\n\r");
				}
				else
					mob.tell("^!ANSI^N is ^Halready^N enabled.\n\r");
				mob.session().setClientTelnetMode(Session.TELNET_ANSI,true);
				mob.session().setServerTelnetMode(Session.TELNET_ANSI,true);
				return false;
			}
			else if(((String)commands.get(1)).equalsIgnoreCase("off"))
			{
				if(acct != null) acct.setFlag(PlayerAccount.FLAG_ANSI, false);
				if((ps.getBitmap()&PlayerStats.ATT_ANSI)>0)
				{
					ps.setBitmap(ps.getBitmap()&~PlayerStats.ATT_ANSI);
					mob.tell("ANSI colour disabled.\n\r");
				}
				else
					mob.tell("ANSI is already disabled.\n\r");
				mob.session().setClientTelnetMode(Session.TELNET_ANSI,false);
				mob.session().setServerTelnetMode(Session.TELNET_ANSI,false);
				return false;
			}
		}
		mob.tell(((ps.getBitmap()&PlayerStats.ATT_ANSI)>0)?("^!ANSI^N is ^Hcurrently^N enabled.\n\r"):("ANSI is currently disabled.\n\r"));
		mob.tell("Use 'ansi on' or 'ansi off' to set colour.\n\r");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
}