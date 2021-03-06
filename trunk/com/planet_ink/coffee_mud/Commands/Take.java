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

public class Take extends StdCommand
{
	public Take(){access=new String[]{"TAKE"};}

	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"ORDER")
		||CMSecurity.isAllowed(mob,"CMDMOBS")
		||CMSecurity.isAllowed(mob,"CMDROOMS");}
	
	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<3)
		{
			mob.tell("Take what from whom?");
			return false;
		}
		int partition=CMLib.english().getPartitionIndex(commands, "from", commands.size()-1);
		Room R=mob.location();

		String victimName=CMParms.combine(commands,partition);
		MOB victim=R.fetchInhabitant(victimName);
		if(victim==null)
		{
			mob.tell("I don't see anyone called "+victimName+" here.");
			return false;
		}
		if((!victim.isMonster())&&(!CMSecurity.isAllowed(mob,"ORDER")))
		{
			mob.tell(victim.name()+" is a player!");
			return false;
		}

		int maxToGive=CMLib.english().calculateMaxToGive(mob,commands,victim,false);
		if(maxToGive<0) return false;

		String thingToGive=CMParms.combine(commands,0,partition);
		Vector V=new Vector();
		boolean allFlag=commands.elementAt(0).equalsIgnoreCase("all");
		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
		if(allFlag)
		{
			Vector<Interactable> getThese=CMLib.english().fetchInteractables(thingToGive,false,1,maxToGive,victim);
			for(int i=getThese.size()-1;i>=0;i--)
				if(!(getThese.get(i) instanceof Item))
					getThese.remove(i);
			if(getThese.isEmpty())
			{
				mob.tell("You don't see '"+thingToGive+"' on "+victim.name()+".");
				return false;
			}
			//Random musings: If ALWAYS is truly always, it's somewhat plausible for an archon to pick themself up. This would be a horrible thing and so even always should have exceptions.
			//Also it's possible to 'take Someguy from Someguy'. This is silly but probably ok.
			for(Item I : (Item[])getThese.toArray(Item.dummyItemArray))
			{
				CMMsg msg=CMClass.getMsg(mob,I,(Vector)null,EnumSet.of(CMMsg.MsgCode.GET,CMMsg.MsgCode.ALWAYS),"^[S-NAME] take^s ^[T-NAME] from "+victim.name()+".");
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
			Interactable getThis=CMLib.english().fetchInteractable(thingToGive,false,1,victim);
			if(getThis==null)
			{
				mob.tell("You don't see '"+thingToGive+"' on "+victim.name()+".");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,getThis,(Vector)null,EnumSet.of(CMMsg.MsgCode.GET,CMMsg.MsgCode.ALWAYS),"^[S-NAME] take^s ^[T-NAME] from "+victim.name()+".");
			R.doMessage(msg);
			msg.returnMsg();
		}
		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}
