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
public class Empty extends Drop
{
	public Empty(){}

	private String[] access={"EMPTY","EMP"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String whatToDrop=null;
		Interactable target=mob;
		if(commands.size()<2)
		{
			mob.tell("Empty what where?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()>1)
		{
			String s=(String)commands.lastElement();
			if((s.equalsIgnoreCase("here"))
				||(s.equalsIgnoreCase("floor"))
				||(s.equalsIgnoreCase("ground")))
				target=mob.location();
			else
			if((s.equalsIgnoreCase("me"))
				||(s.equalsIgnoreCase("self"))
				||("INVENTORY".startsWith(s.toUpperCase()))) target=mob;
			else
			{
				target=mob.fetchInventory(s);
				if(target==null) 
					target=mob.location().fetchItem(s);
			}
			if(target!=null)
				commands.removeElementAt(commands.size()-1);
		}

		if(target==null)
		{
			mob.tell("Empty it where?");
			return false;
		}

		int maxToDrop=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToDrop<0) return false;

		whatToDrop=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
		if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}
		
		Vector<Item> V=(Vector)CMLib.english().fetchInteractables(whatToDrop, false, 1, allFlag?maxToDrop:1, mob.getItemCollection());
		if(V.size()==0)
		{
			mob.tell("You don't seem to be carrying that.");
			return false;
		}
		Vector<Drink> drinks=new Vector();
		for(int i=V.size()-1;i>=0;i--)
		{
			Item I=V.get(i);
			if(!(I instanceof Container))
				V.remove(i);
			if(I instanceof Drink)
				drinks.add((Drink)I);
		}

		String str="<S-NAME> empt(ys) <T-NAME>";
		if(target instanceof Room) str+=" here.";
		else
		if(target instanceof MOB) str+=".";
		else str+=" into "+target.name()+".";
		
		if((V.size()==1)&&(V.firstElement()==target))
			mob.tell("You can't empty something into itself!");
		else
		if((V.size()==0)
		&&(drinks.size()==1)
		&&(drinks.firstElement().nourishment()==0))
			mob.tell(mob,(Drink)V.firstElement(),null,"<T-NAME> is already empty.");
		else
		{
			Room R=mob.location();
			ItemCollection col=ItemCollection.O.getFrom(target);
			if(col!=null)
			for(int v=0;v<V.size();v++)
			{
				Container C=(Container)V.elementAt(v);
				if(C==target) continue;
				col=ItemCollection.O.getFrom(C);
				if(col==null) continue;
				Vector<Item> stuff=col.allItems();
				
				if(!R.doMessage(CMClass.getMsg(mob,C,null,EnumSet.of(CMMsg.MsgCode.VISUAL),str))) continue;
				R.doMessage(CMClass.getMsg(mob,target,stuff,EnumSet.of(CMMsg.MsgCode.DROP),null));
/*
				for(Item I : stuff)
				{
					if(I instanceof Coins)
						((Coins)I).putCoinsBack();
					if(I instanceof RawMaterial)
						((RawMaterial)I).rebundle();
				}
*/
			}
			if((target instanceof Drink)||(target instanceof Room))
			{
				if(target instanceof Room) target=null;
				for(int v=0;v<drinks.size();v++)
				{
					Drink D=drinks.elementAt(v);
					CMMsg fillMsg=CMClass.getMsg(mob,target,D,EnumSet.of(CMMsg.MsgCode.FILL),(target!=null)?"<S-NAME> pour(s) <O-NAME> into <T-NAME>.":"<S-NAME> pour(s) <O-NAME> out.");
					mob.location().doMessage(fillMsg);
				}
			}
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}

	
}
