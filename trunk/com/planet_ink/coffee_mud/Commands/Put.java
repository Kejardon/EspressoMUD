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
public class Put extends StdCommand
{
	public Put(){access=new String[]{"PUT","PU","P"};}

	public boolean asRider(MOB mob, Vector<String> commands, int partition)
	{
		int maxToPut=CMLib.english().calculateMaxToGive(mob,commands,mob,false);
		if(maxToPut<0) return false;
		Room R=mob.location();

		String rideable=CMParms.combine(commands,partition);
		Interactable rideThis=CMLib.english().fetchInteractable(rideable,false,1,mob,R);
		if(rideThis==null)
		{
			mob.tell("I don't see a "+rideable+" here.");
			return false;
		}
		else if(!(rideThis instanceof Rideable))
		{
			mob.tell("You can't put anything on that!");
			return false;
		}

		String holdThis=CMParms.combine(commands,0,partition);
		boolean allFlag=(commands.size()>0)?commands.elementAt(0).equalsIgnoreCase("all"):false;
		if(holdThis.toUpperCase().startsWith("ALL.")){ allFlag=true; holdThis="ALL "+holdThis.substring(4);}
		if(holdThis.toUpperCase().endsWith(".ALL")){ allFlag=true; holdThis="ALL "+holdThis.substring(0,holdThis.length()-4);}
		if(allFlag)
		{
			Vector<Interactable> V=CMLib.english().fetchInteractables(holdThis,false,1,maxToPut,mob.getItemCollection());
			if(V.size()==0)
			{
				mob.tell("You don't have '"+holdThis+"'.");
				return false;
			}
			for(Item I : (Item[])V.toArray(Item.dummyItemArray))
			{
				CMMsg msg=CMClass.getMsg(mob,rideThis,I,EnumSet.of(CMMsg.MsgCode.MOUNT),"<S-NAME> put(s) <O-NAME> on <T-NAME>.");
				if(!R.doMessage(msg))
				{
					msg.returnMsg();
					break;
				}
				msg.returnMsg();
			}
		}
		else
		{
			Item I=(Item)CMLib.english().fetchInteractable(holdThis,false,1,mob.getItemCollection());
			if(I==null)
			{
				mob.tell("You don't have '"+holdThis+"'.");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,rideThis,I,EnumSet.of(CMMsg.MsgCode.MOUNT),"<S-NAME> put(s) <O-NAME> on <T-NAME>.");
			R.doMessage(msg);
			msg.returnMsg();
		}
		return false;
	}
	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<3)
		{
			mob.tell("Put what where?");
			return false;
		}

		commands.removeElementAt(0);
		boolean asRider=false;
		int partition=CMLib.english().getPartitionIndex(commands, "in");
		if(partition==-1)
		{
			partition=CMLib.english().getPartitionIndex(commands, "on");
			if(partition!=-1) return asRider(mob, commands, partition);
			else partition=commands.size()-1;
		}

		int maxToPut=CMLib.english().calculateMaxToGive(mob,commands,mob,false);
		if(maxToPut<0) return false;
		Room R=mob.location();

		String holder=CMParms.combine(commands,partition);
		Interactable container=CMLib.english().fetchInteractable(holder,false,1,mob,R);
		if(container==null)
		{
			mob.tell("I don't see a "+holder+" here.");
			return false;
		}
		else if(!(container instanceof Container))
		{
			mob.tell("You can't put anything in that!");
			return false;
		}

		String holdThis=CMParms.combine(commands,0,partition);
		boolean allFlag=(commands.size()>0)?commands.elementAt(0).equalsIgnoreCase("all"):false;
		if(holdThis.toUpperCase().startsWith("ALL.")){ allFlag=true; holdThis="ALL "+holdThis.substring(4);}
		if(holdThis.toUpperCase().endsWith(".ALL")){ allFlag=true; holdThis="ALL "+holdThis.substring(0,holdThis.length()-4);}
		if(allFlag)
		{
			Vector<Interactable> V=CMLib.english().fetchInteractables(holdThis,false,1,maxToPut,mob.getItemCollection());
			if(V.size()==0)
			{
				mob.tell("You don't have '"+holdThis+"'.");
				return false;
			}
			for(Item I : (Item[])V.toArray(Item.dummyItemArray))
			{
				CMMsg msg=CMClass.getMsg(mob,container,I,EnumSet.of(CMMsg.MsgCode.PUT),"<S-NAME> put(s) <O-NAME> in <T-NAME>.");
				if(!R.doMessage(msg))
				{
					msg.returnMsg();
					break;
				}
				msg.returnMsg();
			}
		}
		else
		{
			Item I=(Item)CMLib.english().fetchInteractable(holdThis,false,1,mob.getItemCollection());
			if(I==null)
			{
				mob.tell("You don't have '"+holdThis+"'.");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,container,I,EnumSet.of(CMMsg.MsgCode.PUT),"<S-NAME> put(s) <O-NAME> in <T-NAME>.");
			R.doMessage(msg);
			msg.returnMsg();
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}