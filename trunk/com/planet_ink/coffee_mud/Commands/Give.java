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
public class Give extends StdCommand
{
	public Give(){}

	private String[] access={"GIVE","GI"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Give what to whom?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("To whom should I give that?");
			return false;
		}

		int partition=CMLib.english().getPartitionIndex(commands, "to", commands.size()-1);
		String targetName=CMParms.combine(commands,partition);
		MOB recipient=mob.location().fetchInhabitant(targetName);
		if(recipient==null)
		{
			mob.tell("I don't see anyone called "+targetName+" here.");
			return false;
		}
		commands.setSize(partition);

		int maxToGive=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToGive<0) return false;
		
		String thingToGive=CMParms.combine(commands,0,partition);
		Vector<Item> V=new Vector();
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
		
		if(allFlag)
		{
			V=mob.fetchInventories(thingToGive);
			if(V.size()==0)
			{
				mob.tell("You don't seem to have '"+thingToGive+"'.");
				return false;
			}
		}
		else
		{
			Item I=mob.fetchInventory(thingToGive);
			if(I==null)
			{
				mob.tell("You don't seem to have '"+thingToGive+"'.");
				return false;
			}
			V=new Vector();
			V.add(I);
		}

		for(Item I : (Item[])V.toArray(new Item[0]))
			if(!mob.location().doMessage(CMClass.getMsg(mob,recipient,I,EnumSet.of(CMMsg.MsgCode.GIVE),"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.")))
				break;
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}

	
}
