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


public class Retire extends StdCommand
{
	public Retire(){access=new String[]{"RETIRE"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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

	@Override public int prompter(){return 2;}
	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return false;}
}