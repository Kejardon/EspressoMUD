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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Knock extends StdCommand
{
	public Knock(){}

	private String[] access={"KNOCK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<=1)
		{
			mob.tell("Knock on what?");
			return false;
		}
		int volume=-1;
		if((commands.size()>2)&&(CMath.isInteger((String)commands.lastElement())))
		{
			volume=CMath.s_int((String)commands.lastElement());
			commands.remove(commands.size()-1);
		}
		String knockWhat=CMParms.combine(commands,1).toUpperCase();
		Interactable I=CMLib.english().fetchInteractable(knockWhat, false, 1, mob.location());
		if(I!=null)
		{
			CMMsg msg=CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.KNOCK),"<S-NAME> knock(s) on <T-NAMESELF>.");
			if(volume!=-1)
				msg.setValue(volume);
			mob.location().doMessage(msg);
		}
		else
		{
			mob.tell("You don't see '"+knockWhat+"' here.");
			return false;
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
