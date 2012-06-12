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
public class WizEmote extends StdCommand
{
	public WizEmote(){access=new String[]{"WIZEMOTE"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()>2)
		{
			String who=commands.elementAt(1);
			String msg=CMParms.combineWithQuotes(commands,2);
			Room R=SIDLib.ROOM.get(CMath.s_int(who));
			if(who.toUpperCase().equals("HERE")) R=mob.location();
			Area A=CMLib.map().findAreaStartsWith(who);
			if(who.toUpperCase().equals("ALL"))
			{
				for(Session S : CMLib.sessions().toArray())
				{
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,"WIZEMOTE")))
	  					S.stdPrintln("^w"+msg+"^?");
				}
			}
			else
			if(R!=null)
			{
				for(Session S : CMLib.sessions().toArray())
				{
					if((S.mob()!=null)
					&&(S.mob().location()==R)
					&&(CMSecurity.isAllowed(mob,"WIZEMOTE")))
						S.stdPrintln("^w"+msg+"^?");
				}
			}
			else
			if(A!=null)
			{
				for(Session S : CMLib.sessions().toArray())
				{
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(A.inMyMetroArea(S.mob().location().getArea()))
					&&(CMSecurity.isAllowed(mob,"WIZEMOTE")))
						S.stdPrintln("^w"+msg+"^?");
				}
			}
			else
			{
				boolean found=false;
				for(Session S : CMLib.sessions().toArray())
				{
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,"WIZEMOTE"))
					&&(CMLib.english().containsString(S.mob().name(),who)
						||CMLib.english().containsString(S.mob().location().getArea().name(),who)))
					{
	  					S.stdPrintln("^w"+msg+"^?");
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell("You can't find anyone or anywhere by that name.");
			}
		}
		else
			mob.tell("You must specify either all, or an area/mob name, and an message.");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"WIZEMOTE");}
}