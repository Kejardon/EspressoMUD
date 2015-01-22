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

public class Give extends StdCommand
{
	public static final Command PPGive=new StdCommand()
	{
		{
			ID="PPGive";
		}
		@Override public boolean execute(MOB mob, MOB.QueuedCommand commands)
		{
			MOB.PostPrereqCommand cmd=(MOB.PostPrereqCommand)commands;
			Body body=mob.body();
			ActionCode code=body.getAction(ActionCode.Type.GIVE);
			CMMsg msg=cmd.parsedData;
			ArrayList<MOB.QueuedCommand> prereqs = code.prereqs(mob, body, msg);
			if(prereqs!=null && prereqs.isEmpty())
				code.sendAction(mob, body, msg);
			else
				msg.returnMsg();
			return false;
		}
	};
	public Give(){access=new String[]{"GIVE","GI"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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
		Vector<Item> V;
		boolean allFlag=(commands.size()>0)?commands.elementAt(0).equalsIgnoreCase("all"):false;
		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
		
		if(allFlag)
		{
			V=mob.fetchInventories(thingToGive);
			if(V.isEmpty())
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
		CMMsg msg=CMClass.getMsg(mob,recipient,(Vector)V,EnumSet.of(CMMsg.MsgCode.GIVE),"");
		Body body=mob.body();
		ActionCode code=body.getAction(ActionCode.Type.GIVE);
		ArrayList<MOB.QueuedCommand> prereqs = code.prereqs(mob, body, msg);
		if(prereqs==null)
		{
			msg.returnMsg();
			return false;
		}
		if(prereqs.isEmpty())
		{
			code.sendAction(mob, body, msg);
			return false;
		}
		MOB.PostPrereqCommand ppc = MOB.PostPrereqCommand.newPPC();
		prereqs.add(ppc);
		ppc.parsedData=msg;
		ppc.commandType=CT_LOW_P_ACTION;
		ppc.metaFlags=metaFlags;
		ppc.command=PPGive;
		//TODO: Anything else needed here?
		mob.enqueCommands(prereqs, null);
		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}