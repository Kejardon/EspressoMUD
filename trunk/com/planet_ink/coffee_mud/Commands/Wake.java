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
public class Wake extends StdCommand
{
	public Wake(){access=new String[]{"WAKE"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
//			if(!CMLib.flags().isSleeping(mob))
//				mob.tell("You aren't sleeping!?");
//			else
			CMMsg msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.STAND),"<S-NAME> awake(s) and stand(s) up.");
			mob.location().doMessage(msg);
			msg.returnMsg();
		}
		else
		{
/*	TODO. Need some way to decide if should attempt SOUND or KNOCK
			String whom=CMParms.combine(commands,0);
			MOB M=mob.location().fetchInhabitant(whom);
			if((M==null)||(!CMLib.flags().canBeSeenBy(M,mob)))
			{
				mob.tell("You don't see '"+whom+"' here.");
				return false;
			}
			if(!CMLib.flags().isSleeping(M))
			{
				mob.tell(M.name()+" is awake!");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,M,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to wake <T-NAME> up.");
			mob.location().doMessage(msg);
*/
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}