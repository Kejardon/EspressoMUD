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
public class Tell extends StdCommand
{
	public Tell(){access=new String[]{"TELL","T"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Session sourceSession=mob.session();
		if(sourceSession==null) return false;
		if(commands.size()<3)
		{
			mob.tell("Tell whom what?");
			return false;
		}
		commands.removeElementAt(0);
		
		if(((String)commands.firstElement()).equalsIgnoreCase("last")
		   &&(CMath.isNumber(CMParms.combine(commands,1)))
		   &&(mob.playerStats()!=null))
		{
			LinkedList<String> V=mob.playerStats().getTellStack();
			int listSize=V.size();
			if(listSize==0)
				mob.tell("No telling.");
			else
			{
				int num=CMath.s_int(CMParms.combine(commands,1));
				if(num>listSize) num=listSize;
				for(ListIterator<String> iter=V.listIterator(listSize-num);iter.hasNext();)
					mob.tell(iter.next());
			}
			return false;
		}
		
		MOB target=null;
		Session targetSession=null;
		String targetName=commands.elementAt(0).toUpperCase();
		for(Session thisSession : CMLib.sessions().toArray())
		{
			if((thisSession.mob()!=null)
			   &&(!thisSession.killFlag())
			   &&(thisSession.mob().name().equalsIgnoreCase(targetName)))
			{
				target=thisSession.mob();
				targetSession=thisSession;
				break;
			}
		}
		if(target==null)
		for(Session thisSession : CMLib.sessions().toArray())
		{
			if((thisSession.mob()!=null)
			   &&(!thisSession.killFlag())
			   &&(CMLib.english().containsString(thisSession.mob().name(),targetName)))
			{
				target=thisSession.mob();
				targetSession=thisSession;
				break;
			}
		}
		for(int i=1;i<commands.size();i++)
		{
			String s=commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		String combinedCommands=CMParms.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell("Tell them what?");
			return false;
		}
		if(target==null)
		{
			mob.tell("That person doesn't appear to be online.");
			return false;
		}
		
		boolean ignore=((target.playerStats()!=null)&&(target.playerStats().hasIgnored(mob)));
		String msg="^t^<TELL \""+mob.name()+"\"^><S-NAME> tell(s) <T-NAME> '"+combinedCommands+"'^</TELL^>^?^.";
		mob.tell(mob,target,msg);
		if((mob!=target)&&(!ignore))
		{
			target.tell(mob,target,msg);
			if((!mob.isMonster())&&(!target.isMonster()))
			{
				if(mob.playerStats()!=null)
				{
					mob.playerStats().setReplyTo(target,PlayerStats.REPLY_TELL);
					mob.playerStats().addTellStack(CMLib.coffeeFilter().fullOutFilter(sourceSession,mob,mob,target,null,CMStrings.removeColors(msg),false));
				}
				if(target.playerStats()!=null)
				{
					target.playerStats().setReplyTo(mob,PlayerStats.REPLY_TELL);
					target.playerStats().addTellStack(CMLib.coffeeFilter().fullOutFilter(targetSession,target,mob,target,null,CMStrings.removeColors(msg),false));
				}
			}
		}

		if(targetSession.afkFlag())
			mob.tell(targetSession.afkMessage());
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return false;}
}
