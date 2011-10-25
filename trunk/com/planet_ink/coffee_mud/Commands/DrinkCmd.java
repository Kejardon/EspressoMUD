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
public class DrinkCmd extends StdCommand
{
	public DrinkCmd(){}

	private String[] access={"DRINK","DR","DRI"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()<2)&&(!(mob.location() instanceof Drink)))
		{
			mob.tell("Drink what?");
			return false;
		}
		commands.removeElementAt(0);
        if((commands.size()>1)&&(((String)commands.firstElement()).equalsIgnoreCase("from")))
            commands.removeElementAt(0);
        
		Environmental thisThang=null;
		if((commands.size()==0)&&(mob.location() instanceof Drink))
			thisThang=mob.location();
		else
		{
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,CMParms.combine(commands,0),Wearable.FILTER_ANY);
			if((thisThang==null)
			||((!mob.isMine(thisThang))
			   &&(!CMLib.flags().canBeSeenBy(thisThang,mob))))
			{
				mob.tell("You don't see '"+CMParms.combine(commands,0)+"' here.");
				return false;
			}
		}
		String str="<S-NAME> take(s) a drink from <T-NAMESELF>.";
		Environmental tool=null;
		if((thisThang instanceof Drink)
		&&(((Drink)thisThang).liquidRemaining()>0)
		&&(((Drink)thisThang).liquidType()!=RawMaterial.RESOURCE_FRESHWATER))
			str="<S-NAME> take(s) a drink of "+RawMaterial.CODES.NAME(((Drink)thisThang).liquidType()).toLowerCase()+" from <T-NAMESELF>.";
		else
		if(thisThang instanceof Container)
		{
			Vector V=((Container)thisThang).getContents();
			for(int v=0;v<V.size();v++)
			{
				Item I=(Item)V.elementAt(v);
				if((I instanceof Drink)&&(I instanceof RawMaterial))
				{
					tool=thisThang;
					thisThang=I;
					str="<S-NAME> take(s) a drink of <T-NAMESELF> from <O-NAMESELF>.";
					break;
				}
			}
		}
		CMMsg newMsg=CMClass.getMsg(mob,thisThang,tool,CMMsg.MSG_DRINK,str+CMProps.msp("drink.wav",10));
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
