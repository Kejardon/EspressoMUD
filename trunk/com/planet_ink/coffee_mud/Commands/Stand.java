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
public class Stand extends StdCommand
{
	public Stand(){access=new String[]{"STAND","ST","STA","STAN"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
/*
		boolean ifnecessary=((commands.size()>1)&&(commands.lastElement().equalsIgnoreCase("IFNECESSARY")));
		if(CMLib.flags().isStanding(mob))
			mob.tell("You are already standing!");
		else
*/
		CMMsg msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.STAND),"<S-NAME> stand(s) up.");
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}
