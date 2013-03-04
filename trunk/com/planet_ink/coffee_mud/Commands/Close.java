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
public class Close extends StdCommand
{
	public Close(){access=new String[]{"CLOSE","CLOS","CLO","CL"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String whatToClose=CMParms.combine(commands,1);
		if(whatToClose.length()==0)
		{
			mob.tell("Close what?");
			return false;
		}
		Interactable closeThis=CMLib.english().fetchInteractable(whatToClose, false, 1, mob, mob.location());
/*
		int dirCode=Directions.getGoodDirectionCode(whatToClose);
		if(dirCode>=0)
			closeThis=mob.location().getExitInDir(dirCode);
*/

		if(closeThis==null)
		{
			mob.tell("You don't see '"+whatToClose+"' here.");
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,closeThis,null,EnumSet.of(CMMsg.MsgCode.CLOSE),"<S-NAME> close(s) <T-NAMESELF>.");
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}