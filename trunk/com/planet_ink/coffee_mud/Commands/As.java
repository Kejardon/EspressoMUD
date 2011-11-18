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
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class As extends StdCommand
{
	public As(){}

	private String[] access={"AS"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("As whom do what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		if((!CMSecurity.isAllowed(mob,mob.location(),"AS"))||(mob.isMonster()))
		{
			mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		Session mySession=mob.session();
		MOB M=CMLib.players().getLoadPlayer(cmd);
		if(M==null)
		{
			Vector<MOB> V=mob.location().fetchInhabitants(cmd);
			if(V.size()>0) M=V.get(0);
		}
		if(M==null)
		{
			try
			{
				Vector<MOB> targets=CMLib.map().findInhabitants(CMLib.map().rooms(), cmd, 50);
				if(targets.size()>0) 
					M=targets.elementAt(CMath.random(targets.size()));
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
			if(!CMSecurity.isAllowedEverywhere(mob,"ORDER"))
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
//		boolean inside=(oldRoom!=null)?oldRoom.isInhabitant(M):false;
//		boolean dead=(M.body()==null)||(M.body().amDead());
		M.setTempSession(mySession);
		mySession.setMob(M);
		if(((String)commands.firstElement()).equalsIgnoreCase("here")
		   ||((String)commands.firstElement()).equalsIgnoreCase("."))
		{
			if(M.location()!=mob.location())
			{
				oldRoom=M.location();
				mob.location().bringHere(M.body(), true);
			}
			commands.removeElementAt(0);
		}
//		if(dead&&(M.body()!=null)) M.body().bringToLife();
		M.doCommand(commands,metaFlags|Command.METAFLAG_AS);
//		if(M.playerStats()!=null) M.playerStats().setLastUpdated(0);
		if(oldRoom!=null)
			oldRoom.bringHere(M.body(), true);
		M.setTempSession(null);
		mySession.setMob(mob);
//		if(dead) M.removeFromGame(true);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"AS");}

	
}
