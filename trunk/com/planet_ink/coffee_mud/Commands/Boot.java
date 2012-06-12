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
	public Boot(){access=new String[]{"BOOT"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(mob.session()==null) return false;
		if(commands.size()<=1)
		{
			mob.tell("Boot out who?");
			return false;
		}
		String whom=CMParms.combine(commands,1);
		boolean boot=false;
		for(Session S : CMLib.sessions().toArray())
		{
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
//					if(S.mob().location()!=null)
//						S.mob().location().show(S.mob(),null,null,EnumSet.of(CMMsg.MsgCode.VISUAL),"Something is happening to <S-NAME>.");
				}
				else
					mob.tell("You boot "+S.getAddress());
				S.kill(false);
				if(((S.previousCMD()==null)||(S.previousCMD().length()==0))
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

	public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"BOOT");}
}