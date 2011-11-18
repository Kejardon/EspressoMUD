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
public class Feed extends StdCommand
{
	public Feed(){}

	private String[] access={"FEED"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Feed who what?");
			return false;
		}
		commands.removeElementAt(0);

		int partition=CMLib.english().getPartitionIndex(commands, "to", commands.size()-1);

		String what=CMParms.combine(commands,partition);
		String whom=CMParms.combine(commands,0,partition);
		MOB target=mob.location().fetchInhabitant(whom);
		if(target==null)
		{
			mob.tell("I don't see "+whom+" here.");
			return false;
		}
		if(target.willFollowOrdersOf(mob))
		{
			Item item=mob.fetchInventory(what);
			if(item==null)
			{
				mob.tell("I don't see "+what+" here.");
				return false;
			}
			if((!(item instanceof Food))&&(!(item instanceof Drink)))
			{
				mob.tell("You might want to try feeding them something edibile or drinkable.");
				return false;
			}
			EnumSet<CMMsg.MsgCode> code=null;
			if(item instanceof Food)
				code=EnumSet.of(CMMsg.MsgCode.EAT);
			else
				code=EnumSet.of(CMMsg.MsgCode.DRINK);
			mob.location().doMessage(CMClass.getMsg(mob,target,item,code,"<S-NAME> feed(s) "+item.name()+" to <T-NAMESELF>."));
		}
		else
			mob.tell(target.name()+" won't let you.");
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
