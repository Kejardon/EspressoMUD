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
@SuppressWarnings("unchecked")
public class PreviousCmd extends StdCommand
{
	public PreviousCmd(){access=new String[]{"!"};}

	public boolean execute(MOB mob, MOB.QueuedCommand commands)
	{
		Session S=mob.session();
		if(S!=null)
			mob.enqueCommand(S.previousCMD(), commands.metaFlags);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}