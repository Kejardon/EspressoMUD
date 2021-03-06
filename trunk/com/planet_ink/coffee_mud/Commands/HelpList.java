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

public class HelpList extends StdCommand
{
	public HelpList(){access=new String[]{"HELPLIST","HLIST"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String helpStr=CMParms.combine(commands,1);
		if(CMLib.help().getHelpFile().size()==0)
		{
			mob.tell("No help is available.");
			return false;
		}
		if(helpStr.length()==0)
		{
			mob.tell("You must enter a search pattern.  Use 'TOPICS' or 'COMMANDS' for an unfiltered list.");
			return false;
		}
		String thisTag=
					CMLib.help().getHelpList(
					helpStr,
					CMLib.help().getHelpFile(),
					CMSecurity.isAllowed(mob,"AHELP")?CMLib.help().getArcHelpFile():null,
					mob);
		if((thisTag==null)||(thisTag.length()==0))
		{
			mob.tell("No help entries match '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			Log.helpOut("Help",mob.name()+" wanted help list match on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln("^xHelp File Matches:^.^?\r\n^N"+thisTag.replace('_',' '));
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
}