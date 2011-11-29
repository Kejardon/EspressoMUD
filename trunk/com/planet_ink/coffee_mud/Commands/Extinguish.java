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
public class Extinguish extends StdCommand
{
	public Extinguish(){}

	private String[] access={"EXTINGUISH"};
	public String[] getAccessWords(){return access;}

	public void putout(MOB mob, Vector commands, boolean quiet)
	{
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Extinguish what?");
			return false;
		}
		commands.removeElementAt(0);

		int maxToExt=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToExt<0) return false;

		String target=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(target.toUpperCase().startsWith("ALL.")){ allFlag=true; target="ALL "+target.substring(4);}
		if(target.toUpperCase().endsWith(".ALL")){ allFlag=true; target="ALL "+target.substring(0,target.length()-4);}
		if(allFlag)
		{
			Vector items=CMLib.english().fetchInteractables(target, false, 1, maxToExt, mob, mob.location());
			if(items.size()==0)
				mob.tell("You don't see '"+target+"' here.");
			else
			for(int i=0;i<items.size();i++)
			{
				Item I=(Item)items.elementAt(i);
				if((items.size()==1)||(I instanceof Light))
					mob.location().doMessage(CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.EXTINGUISH),"<S-NAME> extinguish(es) <T-NAME>."));
			}
		}
		else
		{
			Interactable I=CMLib.english().fetchInteractable(target, false, 1, mob, mob.location());
			if(I==null)
				mob.tell("You don't see '"+target+"' here.");
			else
				mob.location().doMessage(CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.EXTINGUISH),"<S-NAME> extinguish(es) <T-NAME>."));
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
