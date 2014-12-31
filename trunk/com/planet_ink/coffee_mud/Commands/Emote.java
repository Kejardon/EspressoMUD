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
//Todo: When Socials are put back in, they should require this in some form. Probably start with ; and find a valid social, : will always be manuals

public class Emote extends StdCommand
{
	public Emote(){access=new String[]{"EMOTE",";",":"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<2)
		{
			mob.tell(" EMOTE what?");
			return false;
		}
		String combinedCommands=CMParms.combine(commands,1);
		if(combinedCommands.trim().startsWith("'")||combinedCommands.trim().startsWith("`"))
			combinedCommands=combinedCommands.trim();
		else
			combinedCommands=" "+combinedCommands.trim();
		CMMsg msg=CMClass.getMsg(mob,null,(Vector)null,EnumSet.of(CMMsg.MsgCode.EMOTE),"^E^[S-NAME]"+combinedCommands);
		mob.location().doMessage(msg);
		msg.returnMsg();
		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}