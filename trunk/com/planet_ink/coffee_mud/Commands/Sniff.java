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
public class Sniff extends StdCommand
{
	public Sniff(){}

	private String[] access={"SNIFF","SMELL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		
		if(commands.size()>1)
		{
			Interactable thisThang=null;
			
			String ID=CMParms.combine(commands,1);
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;
			else
				thisThang=CMLib.english().fetchInteractable(ID,false,1,mob,mob.location());
			if(thisThang!=null)
			{
				String name=" <T-NAMESELF>";
				if(thisThang==mob.location())
					name=" around";
				mob.location().doMessage(CMClass.getMsg(mob,thisThang,null,EnumSet.of(CMMsg.MsgCode.SNIFF),"<S-NAME> sniff(s)"+name+"."));
			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,mob.location(),null,EnumSet.of(CMMsg.MsgCode.SNIFF),"<S-NAME> sniff(s) around.");
			mob.location().doMessage(msg);
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
