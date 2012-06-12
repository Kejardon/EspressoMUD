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
public class Description extends StdCommand
{
	public Description(){access=new String[]{"DESCRIPTION"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<2)
		{
			mob.tell("^xYour current description:^?\r\n"+mob.description());
			mob.tell("\r\nEnter DESCRIPTION [NEW TEXT] to change.");
			return false;
		}
		String s=CMParms.combine(commands,1);
		if(s.length()>255)
			mob.tell("Your description exceeds 255 characters in length.  Please re-enter a shorter one.");
		else
		{
			mob.setDescription(s);
			mob.tell("Your description has been changed.");
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return false;}
}