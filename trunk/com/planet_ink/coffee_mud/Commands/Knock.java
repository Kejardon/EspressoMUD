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

public class Knock extends StdCommand
{
	public Knock(){access=new String[]{"KNOCK"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<=1)
		{
			mob.tell("Knock on what?");
			return false;
		}
		int volume=-1;
		if((commands.size()>2)&&(CMath.isInteger(commands.lastElement())))
		{
			volume=CMath.s_int(commands.lastElement());
			commands.remove(commands.size()-1);
		}
		String knockWhat=CMParms.combine(commands,1).toUpperCase();
		Interactable I=CMLib.english().fetchInteractable(knockWhat, false, 1, mob.location());
		if(I!=null)
		{
			CMMsg msg=CMClass.getMsg(mob,I,(Vector)null,EnumSet.of(CMMsg.MsgCode.KNOCK),"^[S-NAME] knock^s on ^[T-NAMESELF].");
			if(volume!=-1)
				msg.setValue(volume);
			mob.location().doMessage(msg);
			msg.returnMsg();
		}
		else
		{
			mob.tell("You don't see '"+knockWhat+"' here.");
			return false;
		}
		return false;
	}
	//TODO: This needs to close in on range first.
	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}