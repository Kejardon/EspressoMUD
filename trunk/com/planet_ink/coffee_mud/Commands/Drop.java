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
public class Drop extends StdCommand
{
	public Drop(){}

	private String[] access={"DROP","DRO"};
	public String[] getAccessWords(){return access;}

	public boolean drop(MOB mob, Environmental dropThis, boolean quiet, boolean optimize)
	{
        Room R=mob.location();
		CMMsg msg=CMClass.getMsg(mob,dropThis,null,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_DROP,quiet?null:"<S-NAME> drop(s) <T-NAME>.");
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if(dropThis instanceof Coins)
			    ((Coins)dropThis).putCoinsBack();
			if(dropThis instanceof RawMaterial)
				((RawMaterial)dropThis).rebundle();
			return true;
		}
		if(dropThis instanceof Coins)
		    ((Coins)dropThis).putCoinsBack();
		if(dropThis instanceof RawMaterial)
			((RawMaterial)dropThis).rebundle();
		return false;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String whatToDrop=null;
		Item container=null;
		Vector V=new Vector();

		if((commands.size()>=3)
		&&(commands.firstElement() instanceof Item)
		&&(commands.elementAt(1) instanceof Boolean)
		&&(commands.elementAt(2) instanceof Boolean))
        {
			return drop(mob,(Item)commands.firstElement(),
						((Boolean)commands.elementAt(1)).booleanValue(),
						((Boolean)commands.elementAt(2)).booleanValue());
        }

		if(commands.size()<2)
		{
			mob.tell("Drop what?");
			return false;
		}
		commands.removeElementAt(0);

        // uncommenting this allows dropping directly from containers
        // "drop all sack" will no longer drop all of your "sack", but will drop 
        // all of the contents of your 1.sack, leaving the sack in inventory.
		//container=CMLib.english().possibleContainer(mob,commands,true,Wearable.FILTER_UNWORNONLY);


		int maxToDrop=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
        if(maxToDrop<0) return false;
        
		whatToDrop=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
		if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}
		int addendum=1;
		String addendumStr="";
        Item dropThis=CMLib.english().bestPossibleGold(mob,null,whatToDrop);
        if(dropThis!=null)
        {
            if(((Coins)dropThis).getNumberOfCoins()<CMLib.english().numPossibleGold(mob,whatToDrop+addendumStr))
                return false;
            if(CMLib.flags().canBeSeenBy(dropThis,mob))
                V.addElement(dropThis);
        }
		boolean doBugFix = true;
        if(V.size()==0)
		while(doBugFix || ((allFlag)&&(addendum<=maxToDrop)))
		{
			doBugFix=false;
            dropThis=mob.fetchCarried(container,whatToDrop+addendumStr);
			if((dropThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
			{
				dropThis=mob.fetchWornItem(whatToDrop);
				if(dropThis!=null)
				{
					if((!dropThis.amWearingAt(Wearable.WORN_HELD))&&(!dropThis.amWearingAt(Wearable.WORN_WIELD)))
					{
						mob.tell("You must remove that first.");
						return false;
					}
					CMMsg newMsg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
					if(mob.location().okMessage(mob,newMsg))
						mob.location().send(mob,newMsg);
					else
						return false;
				}
			}
            if((allFlag)&&(dropThis instanceof Coins)&&(whatToDrop.equalsIgnoreCase("all")))
                dropThis=null;
            else
            {
    			if(dropThis==null) break;
    			if((CMLib.flags().canBeSeenBy(dropThis,mob)||(dropThis instanceof Light))
    			&&(!V.contains(dropThis)))
    				V.addElement(dropThis);
            }
			addendumStr="."+(++addendum);
		}

		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
			drop(mob,(Item)V.elementAt(i),false,true);
		mob.location().recoverRoomStats();
		mob.location().recoverRoomStats();
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
