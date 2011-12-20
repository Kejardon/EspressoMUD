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
public class Sounds extends StdCommand
{
	public Sounds(){}

	private String[] access={"SOUNDS","MSP"};
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
						mob.tell("MSP Sound/Music enabled.\n\r");
					}
					else
						mob.tell("Your client does not appear to support MSP.");
				}
				else
					mob.tell("MSP Sound/Music is already enabled.\n\r");
				return false;
			}
			else if(((String)commands.get(1)).equalsIgnoreCase("off"))
			{
				if(((ps.getBitmap()&PlayerStats.ATT_SOUND)>0)||(mob.session().clientTelnetMode(Session.TELNET_MSP)))
				{
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()&~PlayerStats.ATT_SOUND);
					mob.session().changeTelnetMode(Session.TELNET_MSP,false);
					mob.session().setClientTelnetMode(Session.TELNET_MSP,false);
					mob.tell("MSP Sound/Music disabled.\n\r");
				}
				else
					mob.tell("MSP Sound/Music already disabled.\n\r");
				return false;
			}
		}
		mob.tell(((ps.getBitmap()&PlayerStats.ATT_SOUND)>0)?("MSP Sound/Music is currently enabled.\n\r"):("MSP Sound/Music is currently disabled.\n\r"));
		mob.tell("Use 'sounds on' or 'sounds off' to set.\n\r");
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return super.securityCheck(mob)&&(!CMSecurity.isDisabled("MSP"));}
}
