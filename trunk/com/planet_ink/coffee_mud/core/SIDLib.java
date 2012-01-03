package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
TODO: Significant problem that needs to be resolved. If an object is deleted and a new object takes its SID,
if references to the old object still exist and are not updated/saved by the time the mud shuts down, they
will refer to the new object next reboot.
	Make sure no references exist in-mud somehow?
		I don't think a 'find existing references to this object' exists. Would have to keep everything in mind when coding.
	How do SQL libraries do it? Junction tables require it, using foreign keys. Ask Sharl?
*/
@SuppressWarnings("unchecked")
public class SIDLib
{
	public enum Objects
	{
		CREATURE(MOB.class), RIDEABLE(Rideable.class), BEHAVIOR(Behavior.class), EFFECT(Effect.class),
		ROOM(Room.class), ITEM(Item.class), AREA(Area.class), EXIT(Exit.class), ACCOUNT(PlayerAccount.class),
		PLAYERDATA(PlayerStats.class), ITEMCOLLECTION(ItemCollection.class)
		//CLOSEABLE(Closeable.class), ENVIRONMENTAL(Environmental.class), 
		;
		public final Class myClass;
		private int saveNumber=10000;	//1-9999 reserved for non-transient objects that may exist in several places at once.
//		private boolean started=false;
		private HashMap<Integer, CMSavable> assignedNumbers=new HashMap<Integer, CMSavable>();

		private Objects(Class type){myClass=type;}

		public synchronized int getNumber(CMSavable forThis)
		{
			if(forThis==null) return 0;
			if(assignedNumbers.containsKey(saveNumber)||((saveNumber<10000)&&(saveNumber>=0)))
			{
				int inc=1;
				while((saveNumber+inc<10000&&saveNumber+inc>=0)||assignedNumbers.containsKey(saveNumber+inc))
				{
					inc=inc*2;
					if(inc==0) {saveNumber+=1580030169; inc=1;} //(2^32)/e ; optimal interval for poking around randomly
				}
				saveNumber+=inc;
			}
			assignedNumbers.put(saveNumber, forThis);
			Log.sysOut("SID "+name(),"Giving number "+saveNumber+" to "+forThis.ID());
			return saveNumber++;
		}
		public int currentNum(){return saveNumber;}
		public void setNum(int num){saveNumber=num;}
		public void assignNumber(int i, CMSavable A) { assignedNumbers.put(i, A); Log.sysOut("SID "+name(),"Registered num "+i+" for "+A.ID());
			}
		public void removeNumber(int i) { assignedNumbers.remove(i); Log.sysOut("SID "+name(),"Removed num "+i);
			}
		public CMSavable get(Integer i)
		{ 
			CMSavable obj=assignedNumbers.get(i);
			if(obj==null) Log.sysOut("SID "+name(),"No number "+i);
			else Log.sysOut("SID "+name(),"Found number "+i);
			return assignedNumbers.get(i);
		}
		public void save()
		{
			for(Iterator<CMSavable> e=assignedNumbers.values().iterator();e.hasNext();)
				CMLib.database().saveObject(e.next());
		}
		public Iterator getAll(){return ((HashMap)assignedNumbers.clone()).values().iterator(); }
	}
	public static Objects getType(CMSavable O)
	{
		for(Objects type : Objects.values())
			if(type.myClass.isInstance(O)) return type;
		return null;
	}
}