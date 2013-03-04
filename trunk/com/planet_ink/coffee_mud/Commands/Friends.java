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
//TODO: Rewrite this to cause a prompt for the requested friend, if they accept, then they are added to eachothers' friend list
@SuppressWarnings("unchecked")
public class Friends extends StdCommand
{
	public Friends(){access=new String[]{"FRIENDS"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;

		if((commands.size()<2)||(commands.elementAt(1).equalsIgnoreCase("list")))
		{
			AccountStats[] h=pstats.getFriends();
			if(h.length==0)
				mob.tell("You have no friends listed.  Use FRIENDS ADD to add more.");
			else
			{
				StringBuffer str=new StringBuffer("Your listed friends are: ");
				for(AccountStats M : h)
				{
					if(M instanceof PlayerStats)
						str.append(((PlayerStats)M).mob().name()).append(" ");
					else
						str.append(((PlayerAccount)M).accountName()).append("(acct) ");
				}
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
			MOB newFriend=CMLib.players().getPlayer(name);
			if(newFriend==null)
			{
				mob.tell("No player by that name was found.");
				return false;
			}
			if(pstats.hasFriend(newFriend.playerStats()))
			{
				mob.tell("That name is already on your list.");
				return false;
			}
			if(newFriend.session()==null)
			{
				mob.tell("That person is not online. New friends must be online and confirm to be added to your friend list.");
				return false;
			}
			if(!newFriend.session().confirm(mob.name()+" wishes to add you to their friends list. Is this ok? (y/N)","N"))
			{
				mob.tell(newFriend.name()+" denied your friend request.");
				return false;
			}
			pstats.addFriend(newFriend);
			newFriend.playerStats().addFriend(mob);
			/*if(newFriend.playerStats().getAccount()==null)
				newFriend.playerStats().saveThis();
			else
				newFriend.playerStats().getAccount().saveThis();
			pstats.saveThis();*/
			mob.tell("The Player '"+name+"' has been added to your friends list.");
			newFriend.tell("The Player '"+mob.name()+"' has been added to your friends list.");
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
			AccountStats oldFriend=pstats.removeFriend(name);
			if(oldFriend!=null)
			{
				mob.tell("The Player '"+name+"' has been removed from your friends list.");
				if(oldFriend instanceof PlayerStats)
				{
					MOB oldMob=((PlayerStats)oldFriend).mob();
					if(oldMob.session()!=null)
						oldMob.tell(mob.name()+" has removed you from their friends list.");
				}
				else for(Iterator<MOB> iter=((PlayerAccount)oldFriend).getLoadPlayers();iter.hasNext();)
				{
					MOB oldMob=iter.next();
					if(oldMob.session()!=null)
						oldMob.tell(mob.name()+" has removed you from their friends list.");
				}
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
	
	public boolean prompter(){return true;}
	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return false;}
}