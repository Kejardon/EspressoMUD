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
public class Prompt extends StdCommand
{
	public Prompt(){access=new String[]{"PROMPT"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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
			if(mob.session().confirm("Change your prompt to: "+str+", are you sure (Y/n)?","Y"))
			{
				pstats.setPrompt(str);
				mob.session().rawPrintln("Your prompt is currently now set at:\r\n"+pstats.getPrompt());
			}
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return false;}
}