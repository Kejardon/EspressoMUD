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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Order extends StdCommand
{
	public Order(){}

	private String[] access={"ORDER"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Order who to do what?");
			return false;
		}
		commands.removeElementAt(0);

		int maxToOrder=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToOrder<0) return false;

		String whomToOrder=(String)commands.elementAt(0);
		boolean allFlag=whomToOrder.equalsIgnoreCase("all");
		if((whomToOrder.toUpperCase().startsWith("ALL."))||(whomToOrder.toUpperCase().startsWith("ALL "))){ allFlag=true; whomToOrder=whomToOrder.substring(4);}
		if(whomToOrder.toUpperCase().endsWith(".ALL")){ allFlag=true; whomToOrder=whomToOrder.substring(0,whomToOrder.length()-4);}
		
		MOB target=null;
		Vector<MOB> V=null;
		if(allFlag)
			V=mob.location().fetchInhabitants(whomToOrder);
		else
		{
			V=new Vector();
			target=mob.location().fetchInhabitant(whomToOrder);
			if(target!=null) V.add(target);
		}
		if(V.size()==0)
		{
			mob.tell("You don't see them here.");
			return false;
		}
		commands.removeElementAt(0);
		Command O=CMLib.english().findCommand(mob,commands);
		String order=CMParms.combine(commands,0);
		if((!CMSecurity.isAllowed(mob,mob.location(),"ORDER"))&&(!((Command)O).canBeOrdered()))
		{
			mob.tell("You can't order anyone to '"+order+"'.");
			return false;
		}

		maxToOrder=allFlag?(V.size()<maxToOrder?V.size():maxToOrder):1;
		for(int v=0;v<maxToOrder;v++)
		{
			target=(MOB)V.elementAt(v);
			O=CMLib.english().findCommand(target,(Vector)commands.clone());
			if(!target.willFollowOrdersOf(mob))
				mob.tell("You can't order '"+target.name()+"' around.");
			else
			{
				CMMsg msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.ORDER),"^T<S-NAME> order(s) <T-NAMESELF> to '"+order+"'^?.");
				//NOTE: Will probably remove this enqueCommand and put it in StdMOB's reaction...
				if(mob.location().doMessage(msg))
					target.enqueCommand((Vector)commands.clone(),metaFlags|Command.METAFLAG_ORDER,0);
			}
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}