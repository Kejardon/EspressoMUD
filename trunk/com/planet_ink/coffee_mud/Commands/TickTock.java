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
public class TickTock extends StdCommand
{
	public TickTock(){access=new String[]{"TICKTOCK"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String s=CMParms.combine(commands,1).toLowerCase();
		try
		{
			if(CMath.isInteger(s))
			{
				int h=CMath.s_int(s);
				if(h==0) h=1;
				mob.tell("..tick..tock..");
				mob.location().getArea().getTimeObj().tickTock(h);
			}
			else
			{
				for(Iterator<CMLibrary> e=CMLib.libraries();e.hasNext();)
				{
					CMLibrary lib=e.next();
					if((lib.getSupportThread()!=null)&&(s.equalsIgnoreCase(lib.getSupportThread().getName())))
					{
						if(lib instanceof Runnable)
							((Runnable)lib).run();
						else
							lib.getSupportThread().interrupt();
						mob.tell("Done.");
						return false;
					}
				}
				mob.tell("Ticktock what?  Enter a number of mud-hours, or thread id.");
			}
		}
		catch(Exception e)
		{
			mob.tell("Ticktock failed: "+e.getMessage());
		}
		
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"TICKTOCK");}
}