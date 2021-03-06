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

public class Fill extends StdCommand
{
	public Fill(){access=new String[]{"FILL"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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
				mob.tell("From what should I fill the "+commands.elementAt(0)+"?");
				return false;
			}
			fillFromThis=mob.location();
			thingToFill=commands.firstElement();
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

		int maxToFill=CMLib.english().calculateMaxToGive(mob,commands,mob,false);
		if(maxToFill<0) return false;

		Interactable fillThis=null;
		Vector<Item> V=null;
		boolean allFlag=(commands.size()>0)?commands.elementAt(0).equalsIgnoreCase("all"):false;
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
			for(Item fillThisThing : (Item[])V.toArray(Item.dummyItemArray))
			{
				CMMsg msg=CMClass.getMsg(mob,fillThisThing,fillFromThis,EnumSet.of(CMMsg.MsgCode.FILL),"<S-NAME> pour(s) <O-NAME> into <T-NAME>.");
				if(!mob.location().doMessage(msg))
				{
					msg.returnMsg();
					break;
				}
				msg.returnMsg();
			}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,fillThis,fillFromThis,EnumSet.of(CMMsg.MsgCode.FILL),"<S-NAME> pour(s) <O-NAME> into <T-NAME>.");
			mob.location().doMessage(msg);
			msg.returnMsg();
		}
		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}