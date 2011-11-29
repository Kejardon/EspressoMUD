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
public class Fill extends StdCommand
{
	public Fill(){}

	private String[] access={"FILL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Fill what, from what?");
			return false;
		}
		commands.removeElementAt(0);
		Interactable fillFromThis=null;
		String thingToFill=null;
		if(commands.size()<2)
		{
			if(!(mob.location() instanceof Drink))
			{
				mob.tell("From what should I fill the "+(String)commands.elementAt(0)+"?");
				return false;
			}
			fillFromThis=mob.location();
			thingToFill=(String)commands.firstElement();
		}
		else
		{
			int partition=CMLib.english().getPartitionIndex(commands, "from", commands.size()-1);
			String thingToFillFrom=CMParms.combine(commands,partition);
			thingToFill=CMParms.combine(commands,0,partition);
			Interactable option=CMLib.english().fetchInteractable(thingToFillFrom,false,1,mob.getItemCollection(),mob.location());
			if(option instanceof Drink)
				fillFromThis=(Drink)option;
			else if(option!=null)
			{
				mob.tell("That doesn't hold any liquid!");
				return false;
			}
			if(fillFromThis==null)
			{
				mob.tell("I don't see "+thingToFillFrom+" here.");
				return false;
			}
		}

		int maxToFill=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToFill<0) return false;

		Interactable fillThis=null;
		Vector<Item> V=null;
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToFill.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToFill="ALL "+thingToFill.substring(4);}
		if(thingToFill.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToFill="ALL "+thingToFill.substring(0,thingToFill.length()-4);}
		if(allFlag)
		{
			V=mob.fetchInventories(thingToFill);
			if(V.size()==0)
			{
				mob.tell("You don't seem to have '"+thingToFill+"'.");
				return false;
			}
			for(int i=0;i<V.size();)
			{
				if(!(V.get(i) instanceof Drink))
				{
					V.remove(i);
					continue;
				}
				i++;
				if(i==maxToFill)
				{
					V.setSize(i);
					break;
				}
			}
			if(V.size()==0)
			{
				mob.tell("Nothing you have called '"+thingToFill+"' can be filled!");
				return false;
			}
		}
		else
		{
			fillThis=mob.fetchInventory(thingToFill);
			if(fillThis==null)
			{
				mob.tell("You don't seem to have '"+thingToFill+"'.");
				return false;
			}
			if(!(fillThis instanceof Drink))
			{
				mob.tell("You can't fill that!");
				return false;
			}
		}

		if(allFlag)
			for(Item fillThisThing : (Item[])V.toArray(new Item[0]))
				if(!mob.location().doMessage(CMClass.getMsg(mob,fillThisThing,fillFromThis,EnumSet.of(CMMsg.MsgCode.FILL),"<S-NAME> pour(s) <O-NAME> into <T-NAME>.")))
					break;
		else
			mob.location().doMessage(CMClass.getMsg(mob,fillThis,fillFromThis,EnumSet.of(CMMsg.MsgCode.FILL),"<S-NAME> pour(s) <O-NAME> into <T-NAME>."));
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
