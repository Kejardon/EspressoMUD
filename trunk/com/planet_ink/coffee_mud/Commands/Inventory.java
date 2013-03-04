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
public class Inventory extends StdCommand
{
	public Inventory(){access=new String[]{"INVENTORY","INV","I"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(mob.isMonster()) return false;
		StringBuilder msg=CMLib.commands().getInventory(mob,mob,CMParms.combine(commands,1));
		if(msg.length()==0)
			mob.tell("^HYou are carrying:\r\n^!Nothing!^?\r\n");
		else
			mob.session().wraplessPrintln("^HYou are carrying:^?\r\n"+msg.toString());
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public boolean canBeOrdered(){return true;}
}