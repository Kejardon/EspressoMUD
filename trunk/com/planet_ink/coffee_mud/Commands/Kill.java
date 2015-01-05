package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Kill extends StdCommand
{
	public Kill(){access=new String[]{"KILL","K","ATTACK"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		MOB target=null;
		if(commands.size()<2)
		{
			mob.tell("Kill whom?");
			return false;
		}
		
		boolean reallyKill=false;
		String whomToKill=CMParms.combine(commands,1);
		Room location=mob.location();
		if(CMSecurity.isAllowed(mob,"KILLDEAD")&&(!mob.isMonster()))
		{
			if(commands.lastElement().equalsIgnoreCase("DEAD"))
			{
				commands.removeElementAt(commands.size()-1);
				whomToKill=CMParms.combine(commands,1);
				reallyKill=true;
			}
		}

		if(target==null)
		{
			target=location.fetchInhabitant(whomToKill);
			if(target==null)
			{
				mob.tell("You don't see '"+whomToKill+"' here.");
				return false;
			}
		}
		
		if(reallyKill)
		{
			CMMsg msg=CMClass.getMsg(mob,target,(Vector)null,EnumSet.of(CMMsg.MsgCode.VISUAL),"^F^[S-NAME] touch^e ^[T-NAMESELF^].^?"); //<FIGHT></FIGHT>
			CMLib.color().fixSourceFightColor(msg);
			if(location.doMessage(msg))
			{
				target.body().charStats().setPoints(CharStats.Points.HIT, 0);
//				CMLib.combat().postDeath(mob,target,null);
			}
			msg.returnMsg();
			return false;
		}
		
		Interactable oldVictim=mob.getVictim();
		if((oldVictim!=null)&&(oldVictim==target))
		{
			mob.tell("^fYou are already fighting "+mob.getVictim().name()+".^?"); //<FIGHT></FIGHT>
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,target,(Vector)null,EnumSet.of(CMMsg.MsgCode.ATTACK),null);
		if(location.okMessage(location,msg))
		{
			mob.tell("^fYou are now targeting "+target.name()+".^?"); //<FIGHT></FIGHT>
			mob.setVictim(target);
		}
		msg.returnMsg();
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_HIGH_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}