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
public class Sleep extends StdCommand
{
	public Sleep(){access=new String[]{"SLEEP","SL"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
/*
		if(CMLib.flags().isSleeping(mob))
		{
			mob.tell("You are already asleep!");
			return false;
		}
*/
		if(commands.size()<=1)
		{
			CMMsg msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.SLEEP),"<S-NAME> lay(s) down and take(s) a nap.");
			mob.location().doMessage(msg);
			msg.returnMsg();
			return false;
		}
		if("ON".equalsIgnoreCase(commands.get(1)))
			commands.remove(1);
		String possibleRideable=CMParms.combine(commands,1);
		Interactable I=CMLib.english().fetchInteractable(possibleRideable, false, 1, mob.location());
		if(I==null)
		{
			mob.tell("You don't see '"+possibleRideable+"' here.");
			return false;
		}
		String mountStr="<S-NAME> sleep(s) on <T-NAME>.";
		CMMsg msg=CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.SLEEP),mountStr);
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}
