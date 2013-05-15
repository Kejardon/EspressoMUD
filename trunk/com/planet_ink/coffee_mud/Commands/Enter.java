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
public class Enter extends Go
{
	public Enter(){access=new String[]{"ENTER","EN"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<=1)
		{
			mob.tell("Enter what or where? Try EXITS.");
			return false;
		}
		String enterWhat=CMParms.combine(commands,1).toUpperCase();
		Room R=mob.location();
		ExitInstance map=R.getExitInstance(enterWhat);
		if(map!=null)
			move(mob,R,map,false,false);
		else
			mob.tell("There is no exit like that.");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}