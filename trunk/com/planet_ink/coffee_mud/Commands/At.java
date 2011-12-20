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
public class At extends StdCommand
{
	public At(){}

	private String[] access={"AT"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("At where do what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		Room room=CMLib.map().findWorldRoomLiberally(mob,cmd,"APMIR",100,120);
		if(room==null)
		{
			if(CMSecurity.isAllowedAnywhere(mob,"AT"))
				mob.tell("At where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,"AT"))
		{
			mob.tell("You aren't powerful enough to do that there.");
			return false;
		}
		Room R=mob.location();
		if(R!=room)	room.bringHere(mob.body(), true);
		mob.doCommand(commands,metaFlags);
		if(mob.location()!=R) R.bringHere(mob.body(), true);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"AT");}
}