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
//TODO: This file almost definitely needs to be reworked

public class As extends StdCommand
{
	public As(){access=new String[]{"AS"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.remove(0);
		if(commands.size()<2)
		{
			mob.tell("As whom do what?");
			return false;
		}
		String cmd=commands.remove(0);
		if((!CMSecurity.isAllowed(mob,"AS"))||(mob.isMonster()))
		{
			mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		Session mySession=mob.session();
		MOB M=CMLib.players().getPlayer(cmd);
		if(M==null)
			M=mob.location().fetchInhabitant(cmd);
		if(M==null)
		{
			try
			{
				Vector<MOB> targets=CMLib.map().findInhabitants(CMLib.map().rooms(), cmd, 500);
				if(targets.size()>0) 
					M=targets.get(CMath.random(targets.size()));
			}
			catch(NoSuchElementException e){}
		}
		if(M==null)
		{
			mob.tell("You don't know of anyone by that name.");
			return false;
		}
		if((CMSecurity.isASysOp(M))&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		if(!M.isMonster())
		{
			if(!CMSecurity.isAllowed(mob,"ORDER"))
			{
				mob.tell("You can't do things as players if you can't order them.");
				return false;
			}
		}
		else if(M.session()!=null)
		{
			mob.tell("Someone else is currently controlling him, you can't do that right now.");
		}
		Room oldRoom=null;
//		boolean dead=(M.body()==null)||(M.body().amDead());
		M.setTempSession(mySession);
		mySession.setMob(M);
		if(commands.firstElement().equalsIgnoreCase("here")||commands.firstElement().equalsIgnoreCase("."))
		{
			if(M.location()!=mob.location())
			{
				oldRoom=M.location();
				mob.location().bringHere(M.body(), true);
			}
			commands.remove(0);
		}
//		if(dead&&(M.body()!=null)) M.body().bringToLife();
		metaFlags|=Command.METAFLAG_AS;
		String subCommand=CMParms.combine(commands,0);
		Command O=CMLib.english().findCommand(M,subCommand);
		if(O==null){ CMLib.commands().handleUnknownCommand(M,subCommand); return false;}
		MOB.QueuedCommand qCom=O.prepCommand(M, subCommand, metaFlags);
		M.doCommand(qCom);
//		if(M.playerStats()!=null) M.playerStats().setLastUpdated(0);
		if(oldRoom!=null)
			oldRoom.bringHere(M.body(), true);
		M.setTempSession(null);
		mySession.setMob(mob);
//		if(dead) M.removeFromGame(true);
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"AS");}
}