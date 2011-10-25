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
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
		if(!mob.isMonster())
		{
			StringBuffer commandList=new StringBuffer("");
			Vector commandSet=new Vector();
			int col=0;
			HashSet done=new HashSet();
			for(Enumeration e=CMClass.commands();e.hasMoreElements();)
			{
				Command C=(Command)e.nextElement();
				String[] access=C.getAccessWords();
				if((access!=null)
				&&(access.length>0)
				&&(access[0].length()>0)
				&&(!done.contains(access[0]))
				&&(C.securityCheck(mob)))
				{
				    done.add(access[0]);
				    commandSet.add(access[0]);
				}
			}
			Collections.sort(commandSet);
			for(Iterator i=commandSet.iterator();i.hasNext();)
			{
			    String s=(String)i.next();
				if(++col>3){ commandList.append("\n\r"); col=0;}
				commandList.append(CMStrings.padRight("^<HELP^>"+s+"^</HELP^>",19));
			}
			commandList.append("\n\r\n\rEnter HELP 'COMMAND' for more information on these commands.\n\r");
			mob.session().colorOnlyPrintln("^HComplete commands list:^?\n\r"+commandList.toString(),false);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
