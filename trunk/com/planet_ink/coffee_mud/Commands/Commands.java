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

public class Commands extends StdCommand
{
	public Commands(){access=new String[]{"COMMANDS"};}

	
	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Session S=mob.session();
		if(S!=null)
		{
			StringBuffer commandList=new StringBuffer("");
			TreeSet<String> commandSet=new TreeSet<String>();
			int col=0;
			for(Iterator<Command> e=CMClass.COMMAND.all();e.hasNext();)
			{
				Command C=e.next();
				String[] access=C.getAccessWords();
				if((access!=null)
				&&(access.length>0)
				&&(access[0].length()>0)
				&&(C.securityCheck(mob)))
					commandSet.add(access[0]);
			}
			for(Iterator<String> i=commandSet.iterator();i.hasNext();)
			{
				String s=(String)i.next();
				if(++col>3){ commandList.append("\r\n"); col=0;}
				commandList.append(CMStrings.padRight("^<HELP^>"+s+"^</HELP^>",19));
			}
			commandList.append("\r\n\r\nEnter HELP 'COMMAND' for more information on these commands.\r\n");
			S.colorOnlyPrintln("^HComplete commands list:^?\r\n"+commandList.toString(),false);
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
}
