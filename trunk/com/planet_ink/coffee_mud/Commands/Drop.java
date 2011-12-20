package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Drop extends StdCommand
{
	public Drop(){}

	private String[] access={"DROP","DRO"};
	public String[] getAccessWords(){return access;}

	public boolean drop(MOB mob, Item dropThis, boolean quiet)
	{
		Room R=mob.location();
		CMMsg msg=CMClass.getMsg(mob,dropThis,null,EnumSet.of(CMMsg.MsgCode.DROP),quiet?null:"<S-NAME> drop(s) <T-NAME>.");
		boolean success=R.doMessage(msg);
/*		if(dropThis instanceof Coins)
			((Coins)dropThis).putCoinsBack();
		else if(dropThis instanceof RawMaterial)
			((RawMaterial)dropThis).rebundle();
*/
		return success;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String whatToDrop=null;

		if(commands.size()<2)
		{
			mob.tell("Drop what?");
			return false;
		}
		commands.removeElementAt(0);

		int maxToDrop=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToDrop<0) return false;

		whatToDrop=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
		if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}

		Vector<Item> V=(Vector)CMLib.english().fetchInteractables(whatToDrop, false, 1, allFlag?maxToDrop:1, mob.getItemCollection());
		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
			drop(mob,V.elementAt(i),false);
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}