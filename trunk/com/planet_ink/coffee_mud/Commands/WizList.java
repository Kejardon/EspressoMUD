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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class WizList extends StdCommand
{
	public WizList(){}

	private String[] access={"WIZLIST"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean isArchonLooker=CMSecurity.isASysOp(mob);
		String output=(String)Resources.getResource(isArchonLooker?"WIZLIST_ARCHON":"WIZLIST_NORMAL");
		if(output==null)
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			head.append(CMStrings.padRight("Race",8)+" ");
			if(isArchonLooker)
				head.append(CMStrings.padRight("Last",18)+" ");
			head.append("] Archon Character Name^.^?\n\r");
			for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
			{
				MOB U=e.nextElement();
				if(CMSecurity.isASysOp(U))
				{
					head.append("[");
					head.append(CMStrings.padRight(U.body().raceName(),8)+" ");
					if(isArchonLooker)
						head.append(CMStrings.padRight(CMLib.time().date2String(U.playerStats().lastDateTime()),18)+" ");
					head.append("] "+CMStrings.padRight(U.name(),25));
					head.append("\n\r");
				}
			}
			output=head.toString();
			Resources.submitResource((isArchonLooker?"WIZLIST_ARCHON":"WIZLIST_NORMAL"),output);
		}
		mob.tell("^x[The Archons of "+CMProps.Strings.MUDNAME.property()+"]^.^?");
		mob.tell(output);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
