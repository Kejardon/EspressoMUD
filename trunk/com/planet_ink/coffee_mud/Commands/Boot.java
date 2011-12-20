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
public class Boot extends StdCommand
{
	public Boot(){}

	private String[] access={"BOOT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(mob.session()==null) return false;
		if(commands.size()==0)
		{
			mob.tell("Boot out who?");
			return false;
		}
		String whom=CMParms.combine(commands,0);
		boolean boot=false;
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			if(((S.mob()!=null)&&(CMLib.english().containsString(S.mob().name(),whom)))
			||(S.getAddress().equalsIgnoreCase(whom)))
			{
				if(S==mob.session())
				{
					mob.tell("Try QUIT.");
					return false;
				}
				if(S.mob()!=null)
				{
					mob.tell("You boot "+S.mob().name());
					if(S.mob().location()!=null)
						S.mob().location().show(S.mob(),null,null,EnumSet.of(CMMsg.MsgCode.VISUAL),"Something is happening to <S-NAME>.");
				}
				else
					mob.tell("You boot "+S.getAddress());
				S.kill(false,false,false);
				if(((S.previousCMD()==null)||(S.previousCMD().size()==0))
				&&(!CMLib.flags().isInTheGame(S.mob(),true)))
					CMLib.sessions().stopSessionAtAllCosts(S);
				boot=true;
				break;
			}
		}
		if(!boot)
			mob.tell("You can't find anyone by that name or ip address.");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"BOOT");}
}