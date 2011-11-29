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
public class Throw extends StdCommand
{
	public Throw(){}

	private String[] access={"THROW","TOSS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Throw what, where or at whom?");
			return false;
		}
		commands.removeElementAt(0);
		
		int partition=CMLib.english().getPartitionIndex(commands, "at", commands.size()-1);
		
		String str=CMParms.combine(commands,partition);
		String what=CMParms.combine(commands,0,partition);
		Item item=(Item)CMLib.english().fetchInteractable(what,false,1,mob.getItemCollection());
		if(item==null)
		{
			mob.tell("You don't seem to have a '"+what+"'!");
			return false;
		}

		Interactable target=CMLib.english().fetchInteractable(str,false,1,mob.location());
		if(target==null)
		{
			mob.tell("You don't see a '"+str+"'!");
			return false;
		}

		CMMsg msg=CMClass.getMsg(mob,target,item,EnumSet.of(CMMsg.MsgCode.THROW),"<S-NAME> throw(s) <O-NAME> at <T-NAMESELF>.");
		mob.location().doMessage(msg);
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
