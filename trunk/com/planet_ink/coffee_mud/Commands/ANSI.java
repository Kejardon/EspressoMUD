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
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
		if(!mob.isMonster())
		{
			PlayerAccount acct = null;
			if(mob.playerStats()!=null)
				acct = mob.playerStats().getAccount();
			if(acct != null) acct.setFlag(PlayerAccount.FLAG_ANSI, true);
			if(!CMath.bset(mob.playerStats().getBitmap(),PlayerStats.ATT_ANSI))
			{
				mob.playerStats().setBitmap(CMath.setb(mob.playerStats().getBitmap(),PlayerStats.ATT_ANSI));
				mob.tell("^!ANSI^N ^Hcolour^N enabled.\n\r");
			}
			else
			{
				mob.tell("^!ANSI^N is ^Halready^N enabled.\n\r");
			}
            mob.session().setClientTelnetMode(Session.TELNET_ANSI,true);
            mob.session().setServerTelnetMode(Session.TELNET_ANSI,true);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
