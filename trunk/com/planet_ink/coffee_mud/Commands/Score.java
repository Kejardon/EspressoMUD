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

public class Score extends StdCommand
{
	public Score(){access=new String[]{"SCORE","SC"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(mob.isMonster()) return false;
		StringBuilder msg=null;
		if(commands.size()>1)	//First command is the score command, not interesting.
			msg=CMLib.commands().getScore(mob,commands.get(1));
		else
			msg=CMLib.commands().getScore(mob);
		mob.session().wraplessPrintln(msg.toString());
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
}
