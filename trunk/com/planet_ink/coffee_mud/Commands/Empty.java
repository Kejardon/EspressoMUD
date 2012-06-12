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
public class Empty extends Drop
{
	public Empty(){access=new String[]{"EMPTY","EMP"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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
			String s=commands.lastElement();
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

		int maxToDrop=CMLib.english().calculateMaxToGive(mob,commands,mob,false);
		if(maxToDrop<0) return false;

		whatToDrop=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?commands.elementAt(0).equalsIgnoreCase("all"):false;
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
			mob.tell(mob,(Drink)V.firstElement(),"<T-NAME> is already empty.");
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
				Vector<Item> stuff=CMParms.denumerate(col.allItems());
				CMMsg msg=CMClass.getMsg(mob,C,null,EnumSet.of(CMMsg.MsgCode.VISUAL),str);
				if(!R.doMessage(msg)) {msg.returnMsg(); continue;}
				msg.returnMsg();
				msg=CMClass.getMsg(mob,target,stuff,EnumSet.of(CMMsg.MsgCode.DROP),null);
				R.doMessage(msg);
				msg.returnMsg();
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
					fillMsg.returnMsg();
				}
			}
		}
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}