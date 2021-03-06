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

public class Crawl extends StdCommand
{
	public Crawl(){access=new String[]{"CRAWL","CR"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		CMMsg msg=CMClass.getMsg(mob,null,(Vector)null,EnumSet.of(CMMsg.MsgCode.LAYDOWN),"^[S-NAME] lay^s down and start^s to crawl.");
		mob.location().doMessage(msg);
		msg.returnMsg();

		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}