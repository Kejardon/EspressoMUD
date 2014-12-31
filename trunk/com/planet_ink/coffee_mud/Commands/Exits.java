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

public class Exits extends StdCommand
{
	public Exits(){access=new String[]{"EXITS","EX"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(mob.location()!=null)
			CMLib.commands().lookAtExits(mob.location(),mob);
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}