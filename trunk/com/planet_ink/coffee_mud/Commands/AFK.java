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

public class AFK extends StdCommand
{
	public AFK() { access=new String[]{"AFK"}; }

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(mob.session()==null) return false;
		if(mob.session().afkFlag())
			mob.session().setAfkFlag(false);
		else
		{
			mob.session().setAFKMessage(CMParms.combine(commands,1));
			mob.session().setAfkFlag(true);
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
}
