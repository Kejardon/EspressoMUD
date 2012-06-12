package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Kill extends StdCommand
{
	public Kill(){access=new String[]{"KILL","K","ATTACK"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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
			CMMsg msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.VISUAL),"^F^<FIGHT^><S-NAME> touch(es) <T-NAMESELF>.^</FIGHT^>^?");
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
			mob.tell("^f^<FIGHT^>You are already fighting "+mob.getVictim().name()+".^</FIGHT^>^?");
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.ATTACK),null);
		if(location.okMessage(location,msg))
		{
			mob.tell("^f^<FIGHT^>You are now targeting "+target.name()+".^</FIGHT^>^?");
			mob.setVictim(target);
		}
		msg.returnMsg();
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_HIGH_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}