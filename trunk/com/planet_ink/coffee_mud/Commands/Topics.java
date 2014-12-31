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

public class Topics extends ATopics
{
	public Topics(){access=new String[]{"TOPICS"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Properties helpFile=CMLib.help().getHelpFile();
		if(helpFile.size()==0)
		{
			if(mob!=null)
				mob.tell("No help is available.");
			return false;
		}

		doTopics(mob,helpFile,"HELP", "PLAYER TOPICS");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return true;}
}
