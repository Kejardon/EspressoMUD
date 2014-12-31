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

public class Description extends StdCommand
{
	public Description(){access=new String[]{"DESCRIPTION"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Body myBody=mob.body();
		if(myBody==null)
		{
			mob.tell("You do not currently possess a body to describe.");
		}
		if(commands.size()<2)
		{
			mob.tell("Your current description:\r\n"+myBody.description());
			mob.tell("\r\nEnter DESCRIPTION [NEW TEXT] to change.");
			return false;
		}
		String s=CMParms.combine(commands,1);
		if(s.length()>1024)
			mob.tell("Your description exceeds 1024 characters in length.  Please re-enter a shorter one.");
		else
		{
			myBody.setDescription(s);
			mob.tell("Your description has been changed.");
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return false;}
}