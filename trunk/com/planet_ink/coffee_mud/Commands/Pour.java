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
public class Pour extends StdCommand
{
	public Pour(){}

	private String[] access={"POUR"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Pour what, into what?");
			return false;
		}
		commands.removeElementAt(0);
		int partition=commands.size()-1;
		for(int i=1; i<commands.size()-1; i++)
			if("into".equalsIgnoreCase((String)commands.get(i)))
			{
				commands.remove(i);
				partition=i;
				break;
			}

		Interactable fillFromThis=null;
		Vector<Item> V=null;	//fillFromThese
		String thingToFillFrom=CMParms.combine(commands,0,partition);
		String thingToFill=CMParms.combine(commands,partition);
		int maxToFill=CMLib.english().calculateMaxToGive(mob,thingToFillFrom,true,mob,false);
		if(maxToFill<0) return false;
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToFillFrom.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToFillFrom="ALL "+thingToFillFrom.substring(4);}
		if(thingToFillFrom.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToFillFrom="ALL "+thingToFillFrom.substring(0,thingToFillFrom.length()-4);}
		if(allFlag)
		{
			V=mob.fetchInventories(thingToFillFrom);
			if(V.size()==0)
			{
				mob.tell("You don't seem to have '"+thingToFillFrom+"'.");
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
				mob.tell("Nothing you have called '"+thingToFillFrom+"' can be poured!");
				return false;
			}
		}
		else
		{
			fillFromThis=mob.fetchInventory(thingToFillFrom);
			if(fillFromThis==null)
			{
				mob.tell("You don't seem to have '"+thingToFillFrom+"'.");
				return false;
			}
			if(!(fillFromThis instanceof Drink))
			{
				mob.tell("You can't pour that!");
				return false;
			}
		}

		Drink fillThis=null;
		if(!thingToFill.equalsIgnoreCase("out"))
		{
			Interactable option=CMLib.english().fetchInteractable(thingToFill,false,1,mob.getItemCollection(),mob.location());
			if(option instanceof Drink)
			{
				fillThis=(Drink)option;
				break;
			}
			else if(option!=null)
			{
				mob.tell("That won't hold any liquid!");
				return false;
			}
			if(fillThis==null)
			{
				mob.tell("There's no "+thingToFill+" you can pour that into.");
				return false;
			}
		}
		if(allFlag)
			for(Item fillFromThisThing : (Item[])V.toArray())
				if(!mob.location().doMessage(CMClass.getMsg(mob,fillThis,fillFromThisThing,EnumSet.of(CMMsg.MsgCode.FILL),(fillThis!=null)?"<S-NAME> pour(s) <O-NAME> into <T-NAME>.":"<S-NAME> pour(s) <O-NAME> out.")))
					break;
		else
			mob.location().doMessage(CMClass.getMsg(mob,fillThis,fillFromThis,EnumSet.of(CMMsg.MsgCode.FILL),(fillThis!=null)?"<S-NAME> pour(s) <O-NAME> into <T-NAME>.":"<S-NAME> pour(s) <O-NAME> out."));
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
