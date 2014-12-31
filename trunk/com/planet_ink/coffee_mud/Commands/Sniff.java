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

public class Sniff extends StdCommand
{
	public Sniff(){access=new String[]{"SNIFF","SMELL"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()>1)
		{
			Interactable thisThang=null;
			
			String ID=CMParms.combine(commands,1);
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;
			else
				thisThang=CMLib.english().fetchInteractable(ID,false,1,mob,mob.location());
			if(thisThang!=null)
			{
				String name=" <T-NAMESELF>";
				if(thisThang==mob.location())
					name=" around";
				CMMsg msg=CMClass.getMsg(mob,thisThang,null,EnumSet.of(CMMsg.MsgCode.SNIFF),"<S-NAME> sniff(s)"+name+".");
				mob.location().doMessage(msg);
				msg.returnMsg();
			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,mob.location(),null,EnumSet.of(CMMsg.MsgCode.SNIFF),"<S-NAME> sniff(s) around.");
			mob.location().doMessage(msg);
			msg.returnMsg();
		}
		return false;
	}

	//TODO: um. Sniff at distance or close distance or..? This is really a racial specific command too...
	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}
