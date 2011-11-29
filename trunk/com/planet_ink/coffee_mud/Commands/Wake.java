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
public class Wake extends StdCommand
{
	public Wake(){}

	private String[] access={"WAKE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
//			if(!CMLib.flags().isSleeping(mob))
//				mob.tell("You aren't sleeping!?");
//			else
				mob.location().doMessage(CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.STAND),"<S-NAME> awake(s) and stand(s) up."));
		}
		else
		{
/*	TODO. Need some way to decide if should attempt SOUND or KNOCK
			String whom=CMParms.combine(commands,0);
			MOB M=mob.location().fetchInhabitant(whom);
			if((M==null)||(!CMLib.flags().canBeSeenBy(M,mob)))
			{
				mob.tell("You don't see '"+whom+"' here.");
				return false;
			}
			if(!CMLib.flags().isSleeping(M))
			{
				mob.tell(M.name()+" is awake!");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,M,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to wake <T-NAME> up.");
			mob.location().doMessage(msg);
*/
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}

	
}
