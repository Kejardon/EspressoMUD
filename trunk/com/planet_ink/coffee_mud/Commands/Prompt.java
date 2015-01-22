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

public class Prompt extends StdCommand
{
	public Prompt(){access=new String[]{"PROMPT"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(mob.session()==null) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;

		if(commands.size()==1)
			mob.session().rawPrintln("Your prompt is currently set at:\r\n"+pstats.getPrompt());
		else
		{
			String str=CMParms.combine(commands,1);
			if(("DEFAULT").startsWith(str.toUpperCase()))
				pstats.setPrompt("");
			else
			//if(mob.session().confirm("Change your prompt to: "+str+", are you sure (Y/n)?","Y"))
			{
				pstats.setPrompt(str);
				mob.session().rawPrintln("Your prompt is now set at:\r\n"+pstats.getPrompt());
			}
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return false;}
}