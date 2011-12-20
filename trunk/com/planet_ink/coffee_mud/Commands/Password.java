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
public class Password extends StdCommand
{
	public Password(){}

	private String[] access={"PASSWORD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		if(mob.isMonster()) return false;
		String old=mob.session().prompt("Enter your old password : ");
		String nep=mob.session().prompt("Enter a new password    : ");
		String ne2=mob.session().prompt("Enter new password again: ");
		if(!BCrypt.checkpw(old,pstats.password()))
		{
			mob.tell("Your old password was not entered correctly.");
			return false;
		}
		if(!nep.equals(ne2))
		{
			mob.tell("Your new password was not entered the same way twice!");
			return false;
		}
		pstats.setPassword(BCrypt.hashpw(nep,BCrypt.gensalt()));
		mob.tell("Your password has been changed.");
//		if(pstats.getAccount()!=null)
//			CMLib.database().DBUpdateAccount(pstats.getAccount());
//		CMLib.database().DBUpdatePassword(mob.Name(),nep);
		return false;
	}
	
	public boolean canBeOrdered(){return false;}
}