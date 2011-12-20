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
public class Commands extends StdCommand
{
	public Commands(){}

	private String[] access={"COMMANDS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Session S=mob.session();
		if(S!=null)
		{
			StringBuffer commandList=new StringBuffer("");
			TreeSet<String> commandSet=new TreeSet<String>();
			int col=0;
			for(Iterator e=CMClass.Objects.COMMAND.all();e.hasNext();)
			{
				Command C=(Command)e.next();
				String[] access=C.getAccessWords();
				if((access!=null)
				&&(access.length>0)
				&&(access[0].length()>0)
				&&(C.securityCheck(mob)))
					commandSet.add(access[0]);
			}
			for(Iterator i=commandSet.iterator();i.hasNext();)
			{
				String s=(String)i.next();
				if(++col>3){ commandList.append("\n\r"); col=0;}
				commandList.append(CMStrings.padRight("^<HELP^>"+s+"^</HELP^>",19));
			}
			commandList.append("\n\r\n\rEnter HELP 'COMMAND' for more information on these commands.\n\r");
			S.colorOnlyPrintln("^HComplete commands list:^?\n\r"+commandList.toString(),false);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
}
