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
public class Help extends StdCommand
{
	public Help(){access=new String[]{"HELP"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String helpStr=CMParms.combine(commands,1);
		if(CMLib.help().getHelpFile().size()==0)
		{
			mob.tell("No help is available.");
			return false;
		}
		String thisTag=null;
		if(helpStr.length()==0)
		{
			StringBuffer thisBuf=Resources.getFileResource("help/help.txt",true);
			if(thisBuf!=null) thisTag=thisBuf.toString();
		}
		else
			thisTag=CMLib.help().getHelpText(helpStr,CMLib.help().getHelpFile(),mob);
		if((thisTag==null)&&(CMSecurity.isAllowed(mob,"AHELP")))
			thisTag=CMLib.help().getHelpText(helpStr,CMLib.help().getArcHelpFile(),mob);
		if(thisTag==null)
		{
			String thisList=
				CMLib.help().getHelpList(
				helpStr,
				CMLib.help().getHelpFile(),
				CMSecurity.isAllowed(mob,"AHELP")?CMLib.help().getArcHelpFile():null,
				mob);
			if((thisList!=null)&&(thisList.length()>0))
				mob.tell("No help is available on '"+helpStr+"'.\r\nHowever, here are some search matches:\r\n^N"+thisList.replace('_',' '));
			else
				mob.tell("No help is available on '"+helpStr+"'.\r\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list, or 'HELPLIST' to search.");
			Log.helpOut("Help",mob.name()+" wanted help on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln(thisTag);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}