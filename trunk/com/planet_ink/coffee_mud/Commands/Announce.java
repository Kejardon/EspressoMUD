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

public class Announce extends StdCommand
{
	public Announce(){access=new String[]{"ANNOUNCE","ANNOUNCETO"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		boolean announceTo="ANNOUNCETO".startsWith(commands.firstElement().toUpperCase());
		if(commands.size()>1)
		{
			if((!announceTo)||(commands.get(1).toUpperCase().equals("ALL")))
			{
				String text="^pA powerful voice rings out '"+CMParms.combine(commands,announceTo?2:1)+"'.^N";

				for(Session S : CMLib.sessions().toArray())
				{
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,"ANNOUNCE")))
						S.stdPrintln(text);
				}
			}
			else
			{
				String text="^pA powerful voice rings out '"+CMParms.combine(commands,2)+"'.^N";
				boolean found=false;
				String name=commands.get(1);
				boolean toHere=name.equalsIgnoreCase("here");
				for(Session S : CMLib.sessions().toArray())
				{
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,"ANNOUNCE"))
					&&(((toHere)&&(S.mob().location()==mob.location()))
						||(CMLib.english().containsString(S.mob().name(),name))))
					{
						S.stdPrintln(text);
						found=true;
						if(!toHere) break;
					}
				}
				if(!found)
					mob.tell("You can't find anyone by that name.");
			}
		}
		else
			mob.tell("Usage ANNOUNCETO [ALL|HERE|(USER NAME)] (MESSAGE)\r\nANNOUNCE (MESSAGE)\r\n");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"ANNOUNCE");}
}
