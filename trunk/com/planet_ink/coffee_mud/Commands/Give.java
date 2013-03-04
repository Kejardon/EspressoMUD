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
@SuppressWarnings("unchecked")
public class Give extends StdCommand
{
	public Give(){access=new String[]{"GIVE","GI"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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

		int maxToGive=CMLib.english().calculateMaxToGive(mob,commands,mob,false);
		if(maxToGive<0) return false;
		
		String thingToGive=CMParms.combine(commands,0,partition);
		Vector<Item> V=new Vector();
		boolean allFlag=(commands.size()>0)?commands.elementAt(0).equalsIgnoreCase("all"):false;
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

		for(Item I : (Item[])V.toArray(Item.dummyItemArray))
		{
			CMMsg msg=CMClass.getMsg(mob,recipient,I,EnumSet.of(CMMsg.MsgCode.GIVE),"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
			if(!mob.location().doMessage(msg))
			{
				msg.returnMsg();
				break;
			}
			msg.returnMsg();
		}
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}