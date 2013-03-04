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
public class Extinguish extends StdCommand
{
	public Extinguish(){access=new String[]{"EXTINGUISH"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<2)
		{
			mob.tell("Extinguish what?");
			return false;
		}
		commands.removeElementAt(0);

		int maxToExt=CMLib.english().calculateMaxToGive(mob,commands,mob,false);
		if(maxToExt<0) return false;

		String target=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?commands.elementAt(0).equalsIgnoreCase("all"):false;
		if(target.toUpperCase().startsWith("ALL.")){ allFlag=true; target="ALL "+target.substring(4);}
		if(target.toUpperCase().endsWith(".ALL")){ allFlag=true; target="ALL "+target.substring(0,target.length()-4);}
		if(allFlag)
		{
			Vector items=CMLib.english().fetchInteractables(target, false, 1, maxToExt, mob, mob.location());
			if(items.size()==0)
				mob.tell("You don't see '"+target+"' here.");
			else
			for(int i=0;i<items.size();i++)
			{
				Item I=(Item)items.elementAt(i);
				if((items.size()==1)||(I instanceof Light))
				{
					CMMsg msg=CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.EXTINGUISH),"<S-NAME> extinguish(es) <T-NAME>.");
					mob.location().doMessage(msg);
					msg.returnMsg();
				}
			}
		}
		else
		{
			Interactable I=CMLib.english().fetchInteractable(target, false, 1, mob, mob.location());
			if(I==null)
				mob.tell("You don't see '"+target+"' here.");
			else
			{
				CMMsg msg=CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.EXTINGUISH),"<S-NAME> extinguish(es) <T-NAME>.");
				mob.location().doMessage(msg);
				msg.returnMsg();
			}
		}
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}