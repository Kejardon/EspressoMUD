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
public class Remove extends StdCommand
{
	public Remove(){}

	private String[] access={"REMOVE","REM"};
	public String[] getAccessWords(){return access;}



	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Remove what?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.firstElement() instanceof Item)
		{
			boolean quiet=((commands.size()>1)&&(commands.lastElement() instanceof String)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")));
			Item item=(Item)commands.firstElement();
			CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_REMOVE,quiet?null:"<S-NAME> remove(s) <T-NAME>.");
			if(mob.location().okMessage(mob,newMsg))
			{
				mob.location().send(mob,newMsg);
				return true;
			}
			return false;
		}

		Vector items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_WORNONLY,false);
		if(items.size()==0)
			mob.tell("You don't seem to be wearing that.");
		else
		for(int i=0;i<items.size();i++)
		{
			Item item=(Item)items.elementAt(i);
			CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_REMOVE,"<S-NAME> remove(s) <T-NAME>.");
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
