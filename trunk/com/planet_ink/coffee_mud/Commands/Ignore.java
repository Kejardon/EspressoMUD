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
public class Ignore extends StdCommand
{
	public Ignore(){access=new String[]{"IGNORE"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		if((commands.size()<2)||(commands.elementAt(1).equalsIgnoreCase("list")))
		{
			MOB[] h=pstats.getIgnored();
			if(h.length==0)
				mob.tell("You have no names on your ignore list.  Use IGNORE ADD to add more.");
			else
			{
				StringBuffer str=new StringBuffer("You are ignoring: ");
				for(MOB M : h)
					str.append((M.name())+" ");
				mob.tell(str.toString());
			}
		}
		else
		if(commands.elementAt(1).equalsIgnoreCase("ADD"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Add whom?");
				return false;
			}
			name=CMStrings.capitalizeAndLower(name);
			MOB newIgnore=CMLib.players().getPlayer(name);
			if(newIgnore==null)
			{
				mob.tell("No player by that name was found.");
				return false;
			}
			if(pstats.hasIgnored(newIgnore))
			{
				mob.tell("That name is already on your list.");
				return false;
			}
			pstats.addIgnored(newIgnore);
			newIgnore.playerStats().addIgnoredBy(pstats);
			/*if(newIgnore.playerStats().getAccount()==null)
				newIgnore.playerStats().saveThis();
			else
				newIgnore.playerStats().getAccount().saveThis();
			pstats.saveThis();*/
			mob.tell("The Player '"+name+"' has been added to your ignore list.");
		}
		else
		if(commands.elementAt(1).equalsIgnoreCase("REMOVE"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Remove whom?");
				return false;
			}
			
			for (MOB newIgnore : pstats.getIgnored())
				if(newIgnore.name().equalsIgnoreCase(name))
				{
					pstats.removeIgnored(newIgnore);
					newIgnore.playerStats().removeIgnoredBy(pstats);
					/*if(newIgnore.playerStats().getAccount()==null)
						newIgnore.playerStats().saveThis();
					else
						newIgnore.playerStats().getAccount().saveThis();
					pstats.saveThis();*/
					mob.tell("The Player '"+name+"' has been removed from your ignore list.");
					return false;
				}
			mob.tell("That name '"+name+"' does not appear on your list.");
			return false;
		}
		else
		{
			mob.tell("Parameter '"+commands.elementAt(1)+"' is not recognized.  Try LIST, ADD, or REMOVE.");
			return false;
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}