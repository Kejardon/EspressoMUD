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
public class Display extends StdCommand
{
	public Display(){}

	private String[] access={"DISPLAY","SHOW"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		if(commands.size()<2)
		{
			mob.tell("Who should I show what?");
			return false;
		}

		
		MOB recipient=null;
		{
			Vector<MOB> V=mob.location().fetchInhabitants((String)commands.get(0));
			if(V.size()>0)
				recipient=V.get(0);
		}
		if(recipient==null)
		{
			mob.tell("I don't see anyone called "+(String)commands.get(0)+" here.");
			return false;
		}
		commands.remove(0);

//		int maxToGive=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
//		if(maxToGive<0) return false;

		String thingToGive=CMParms.combine(commands,0);
//		int addendum=1;
//		String addendumStr="";
//		Vector V=new Vector();
//		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
//		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
//		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
//		while(((allFlag)&&(addendum<=maxToGive)))
			Item giveThis=mob.fetchInventory(thingToGive); //+addendumStr
//			if(giveThis==null) break;
//			V.addElement(giveThis);
//			addendumStr="."+(++addendum);

//		if(V.size()==0)
		if(giveThis==null)
			mob.tell("You don't seem to be carrying that.");
		else
//		for(int i=0;i<V.size();i++)
		{
//			Environmental giveThis=(Environmental)V.elementAt(i);
			CMMsg newMsg=CMClass.getMsg(recipient,giveThis,mob,EnumSet.of(CMMsg.MsgCode.LOOK),"<O-NAME> show(s) <T-NAME> to <S-NAMESELF>.");
			mob.location().doMessage(newMsg);
//				recipient.tell(recipient,giveThis,mob,"<O-NAME> show(s) <T-NAME> to <S-NAMESELF>.");
		}
		return false;
	}
	public boolean canBeOrdered(){return true;}
}
