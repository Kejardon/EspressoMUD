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

public class Sounds extends StdCommand
{
	public Sounds(){access=new String[]{"SOUNDS","MSP"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
/*
		PlayerStats ps=mob.playerStats();
		if(ps==null) return false;
		if(commands.size()>1)
		{
			if(((String)commands.get(1)).equalsIgnoreCase("on"))
			{
				if(((ps.getBitmap()&PlayerStats.ATT_SOUND)==0)
				||(!mob.session().clientTelnetMode(Session.TELNET_MSP)))
				{
					mob.session().changeTelnetMode(Session.TELNET_MSP,true);
					for(int i=0;((i<5)&&(!mob.session().clientTelnetMode(Session.TELNET_MSP)));i++)
					{
						try{mob.session().prompt("",100);}catch(Exception e){}
					}
					if(mob.session().clientTelnetMode(Session.TELNET_MSP))
					{
						mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_SOUND);
						mob.tell("MSP Sound/Music enabled.\r\n");
					}
					else
						mob.tell("Your client does not appear to support MSP.");
				}
				else
					mob.tell("MSP Sound/Music is already enabled.\r\n");
				return false;
			}
			else if(((String)commands.get(1)).equalsIgnoreCase("off"))
			{
				if(((ps.getBitmap()&PlayerStats.ATT_SOUND)>0)||(mob.session().clientTelnetMode(Session.TELNET_MSP)))
				{
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()&~PlayerStats.ATT_SOUND);
					mob.session().changeTelnetMode(Session.TELNET_MSP,false);
					mob.session().setClientTelnetMode(Session.TELNET_MSP,false);
					mob.tell("MSP Sound/Music disabled.\r\n");
				}
				else
					mob.tell("MSP Sound/Music already disabled.\r\n");
				return false;
			}
		}
		mob.tell(((ps.getBitmap()&PlayerStats.ATT_SOUND)>0)?("MSP Sound/Music is currently enabled.\r\n"):("MSP Sound/Music is currently disabled.\r\n"));
		mob.tell("Use 'sounds on' or 'sounds off' to set.\r\n");
*/
		mob.tell("MSP is currently disabled in this MUD.");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return super.securityCheck(mob)&&(!CMSecurity.isDisabled("MSP"));}
}
