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
public class Ban extends StdCommand
{
	public Ban(){access=new String[]{"BAN"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.removeElementAt(0);
		String banMe=CMParms.combine(commands,0);
		if(banMe.length()==0)
		{
			mob.tell("Ban what?  Enter an IP address or name mask.");
			return false;
		}
		banMe=banMe.toUpperCase().trim();
		int b=CMSecurity.ban(banMe);
		if(b<0)
			mob.tell("Logins and IPs matching "+banMe+" are now banned.");
		else
		{
			mob.tell("That is already banned.  Do LIST BANNED and check out #"+(b+1)+".");
			return false;
		}
		return true;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"BAN");}
}