package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class TickTock extends StdCommand
{
	public TickTock(){access=new String[]{"TICKTOCK"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"TICKTOCK");}
}