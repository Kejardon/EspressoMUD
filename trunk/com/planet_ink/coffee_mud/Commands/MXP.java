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
public class MXP extends StdCommand
{
	public MXP(){}

	private String[] access={"MXP"};
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
				if(((ps.getBitmap()&PlayerStats.ATT_MXP)==0)
				||(!mob.session().clientTelnetMode(Session.TELNET_MXP)))
				{
					mob.session().changeTelnetMode(Session.TELNET_MXP,true);
					if(mob.session().getTerminalType().toLowerCase().startsWith("mushclient"))
						mob.session().negotiateTelnetMode(Session.TELNET_MXP);
					for(int i=0;((i<5)&&(!mob.session().clientTelnetMode(Session.TELNET_MXP)));i++)
					{
						try{mob.session().prompt("",100);}catch(Exception e){}
					}
					if(mob.session().clientTelnetMode(Session.TELNET_MXP))
					{
						mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_MXP);
						StringBuffer mxpText=Resources.getFileResource("text/mxp.txt",true);
						if(mxpText!=null)
							mob.session().rawOut("\033[6z\n\r"+mxpText.toString()+"\n\r");
						mob.tell("MXP codes enabled.\n\r");
					}
					else
						mob.tell("Your client does not appear to support MXP.");
				}
				else
					mob.tell("MXP codes are already enabled.\n\r");
			}
			else if(((String)commands.get(1)).equalsIgnoreCase("off"))
			{
				if(((ps.getBitmap()&PlayerStats.ATT_MXP)>0)||(mob.session().clientTelnetMode(Session.TELNET_MXP)))
				{
					if(mob.session().clientTelnetMode(Session.TELNET_MXP))
						mob.session().rawOut("\033[3z \033[7z");
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()&~PlayerStats.ATT_MXP);
					mob.session().changeTelnetMode(Session.TELNET_MXP,false);
					mob.session().setClientTelnetMode(Session.TELNET_MXP,false);
					mob.tell("MXP codes are disabled.\n\r");
				}
				else
					mob.tell("MXP codes are already disabled.\n\r");
			}
		}
		mob.tell(((ps.getBitmap()&PlayerStats.ATT_MXP)>0)?("MXP codes are currently enabled.\n\r"):("MXP codes are currently disabled.\n\r"));
		mob.tell("Use 'mxp on' or 'mxp off' to set codes.\n\r");
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return super.securityCheck(mob)&&(!CMSecurity.isDisabled("MXP"));}
}