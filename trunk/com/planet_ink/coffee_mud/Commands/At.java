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

public class At extends StdCommand
{
	public At(){access=new String[]{"AT"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.remove(0);
		if(commands.size()==0)
		{
			mob.tell("At where do what?");
			return false;
		}
		String cmd=commands.remove(0);
		Room room=CMLib.map().findWorldRoomLiberally(mob,cmd,"APMIR",1000,120);
		if(room==null)
		{
			if(CMSecurity.isAllowed(mob,"AT"))
				mob.tell("At where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,"AT"))
		{
			mob.tell("You aren't powerful enough to do that there.");
			return false;
		}
		Room R=mob.location();
		if(R!=room)	room.bringHere(mob.body(), true);
		
		String subCommand=CMParms.combine(commands,0);
		Command O=CMLib.english().findCommand(mob,subCommand);
		if(O==null){ CMLib.commands().handleUnknownCommand(mob,subCommand); return false;}
		MOB.QueuedCommand qCom=O.prepCommand(mob, subCommand, metaFlags);
		mob.doCommand(qCom);
		
		if(mob.location()!=R) R.bringHere(mob.body(), true);
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"AT");}
}