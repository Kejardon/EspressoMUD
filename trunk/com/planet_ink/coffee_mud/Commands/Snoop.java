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

public class Snoop extends StdCommand
{
	public Snoop(){access=new String[]{"SNOOP"};}

	protected boolean canHear(Session S, Session S2)
	{
		if(S==S2) return true;
		for(Iterator<Session> iter=S2.snoopTargets();iter.hasNext();)
			if(canHear(S, iter.next()))
				return true;
		return false;
	}
	
	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.removeElementAt(0);
		Session snooper=mob.session();
		if(snooper==null) return false;
		//boolean doneSomething=false;
		/* for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			if(S.amBeingSnoopedBy(mob.session()))
			{
				if(S.mob()!=null)
					mob.tell("You stop snooping on "+S.mob().name()+".");
				else
					mob.tell("You stop snooping on someone.");
				doneSomething=true;
				S.stopBeingSnoopedBy(mob.session());
			}
		} */
		if(commands.size()==0)
		{
			//if(!doneSomething)
				mob.tell("Snoop on whom?");
			return false;
		}
		String whom=CMParms.combine(commands,0);
		Session snoopOn=null;
		MOB M=CMLib.players().getPlayer(whom);
		if(M!=null) snoopOn=M.session();
		else for(Session S : CMLib.sessions().toArray())
		{
			if((S.mob()!=null)&&(CMLib.english().containsString(S.mob().name(),whom)))
			{
				snoopOn=S;
				if(S.mob().name().equalsIgnoreCase(whom)) break;
			}
		}
		if(snoopOn==null)
			mob.tell("You can't find anyone to snoop on by that name.");
		else if(!CMLib.flags().isInTheGame(snoopOn.mob(),true))
			mob.tell(snoopOn.mob().name()+" is not yet fully in the game.");
		else if(CMSecurity.isASysOp(snoopOn.mob())&&(!CMSecurity.isASysOp(mob)))
			mob.tell("Only another Archon can snoop on "+snoopOn.mob().name()+".");
		else
		{
			if(snooper.stopSnoopingOn(snoopOn))
			{
				mob.tell("You stop snooping on "+snoopOn.mob().name()+".");
				return false;
			}
			if(canHear(snooper, snoopOn))
			{
				mob.tell("This would create a snoop loop!");
				return false;
			}
			snooper.startSnoopingOn(snoopOn);
			mob.tell("You start snooping on "+snoopOn.mob().name()+".");
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"SNOOP");}
}