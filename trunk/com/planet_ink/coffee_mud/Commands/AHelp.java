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
public class AHelp extends StdCommand
{
	public AHelp(){access=new String[]{"ARCHELP","AHELP"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String helpStr=CMParms.combine(commands,1);
		if(CMLib.help().getArcHelpFile().size()==0)
		{
			mob.tell("No archon help is available.");
			return false;
		}
		String thisTag=null;
		if(helpStr.length()==0)
		{
			StringBuffer thisBuf=Resources.getFileResource("help/arc_help.txt",true);
			if(thisBuf!=null) thisTag=thisBuf.toString();
		}
		else
			thisTag=CMLib.help().getHelpText(helpStr,CMLib.help().getArcHelpFile(),mob);
		if(thisTag==null)
		{
			mob.tell("No archon help is available on "+helpStr+" .\r\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			Log.errOut("Help: "+mob.name()+" wanted archon help on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln(thisTag);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"AHELP");}
}