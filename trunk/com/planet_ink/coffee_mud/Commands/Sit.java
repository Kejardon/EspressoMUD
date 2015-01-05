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

public class Sit extends StdCommand
{
	public Sit(){access=new String[]{"SIT","REST","R"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
/*
		if(CMLib.flags().isSitting(mob))
		{
			mob.tell("You are already sitting!");
			return false;
		}
*/
		if(commands.size()<=1)
		{
			CMMsg msg=CMClass.getMsg(mob,null,(Vector)null,EnumSet.of(CMMsg.MsgCode.SIT),"^[S-NAME] sit^s down and take^s a rest.");
			mob.location().doMessage(msg);
			msg.returnMsg();
			return false;
		}
		if("ON".equalsIgnoreCase((String)commands.get(1)))
			commands.remove(1);
		String possibleRideable=CMParms.combine(commands,1);
		Interactable I=CMLib.english().fetchInteractable(possibleRideable, false, 1, mob.location());
		if(I==null)
		{
			mob.tell("You don't see '"+possibleRideable+"' here.");
			return false;
		}
		String mountStr="^[S-NAME] sit^s on ^[T-NAME].";
		CMMsg msg=CMClass.getMsg(mob,I,(Vector)null,EnumSet.of(CMMsg.MsgCode.SIT),mountStr);
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}
