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
//Note: This is not a prompter command, it must be responded to immediately by the player
@SuppressWarnings("unchecked")
public class Retire extends StdCommand
{
	public Retire(){access=new String[]{"RETIRE"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Session session=mob.session();
		if(mob.isMonster()) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;

		mob.tell("^HThis will delete your player from the system FOREVER!");
		String pwd=session.prompt("If that's what you want, re-enter your password:","",30000);
		if(pwd.length()==0) return false;
		if(!BCrypt.checkpw(pwd,pstats.password()))
		{
			mob.tell("Password incorrect.");
			return false;
		}
		if(!CMSecurity.isDisabled("RETIREREASON"))
		{
			String reason=session.prompt("OK.  Please leave us a short message as to why you are deleting this"
											  +" character.  Your answers will be kept confidential, "
											  +"and are for administrative purposes only.\r\n: ","",120000);
			Log.sysOut("Retire",mob.name()+" retiring: "+reason);
		}
		CMLib.players().obliteratePlayer(mob,false);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return false;}
}