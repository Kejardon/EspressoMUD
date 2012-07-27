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
import java.lang.ref.WeakReference;
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
	/* CLAIMED NON-TRANSIENT OBJECTS/IDS
	ROOM
		1 - Limbo
	ITEMCOLLECTION
		1 - LimboIC
	AREA
		1 - LimboArea
	*/
	
	public static final Objects<MOB> CREATURE=new Objects<MOB>(MOB.class){};
	public static final Objects<Rideable> RIDEABLE=new Objects<Rideable>(Rideable.class){};
	public static final Objects<Behavior> BEHAVIOR=new Objects<Behavior>(Behavior.class){};
	public static final Objects<Effect> EFFECT=new Objects<Effect>(Effect.class){};
	public static final Objects<Room> ROOM=new Objects<Room>(Room.class){};
	public static final Objects<Item> ITEM=new Objects<Item>(Item.class){};
	public static final Objects<Area> AREA=new Objects<Area>(Area.class){};
	public static final Objects<Exit> EXIT=new Objects<Exit>(Exit.class){};
	public static final Objects<ItemCollection> ITEMCOLLECTION=new Objects<ItemCollection>(ItemCollection.class){};
	public static final Objects<AccountStats> ACCOUNTSTATS=new Objects<AccountStats>(AccountStats.class){};
	public static abstract class Objects<U extends CMSavable>
	{
		//public static final HashMap<String, Objects> objectsNames=new HashMap<String, Objects>();
		//public static Objects valueOf(String S){return objectsNames.get(S);}
		private static Vector<Objects> objVector=new Vector(10);
		private static Objects[] values;
		public static Objects[] values()
		{
			if(values==null)
				values=objVector.toArray(new Objects[0]);
			return values;
		}

		public final Class myClass;
		private int saveNumber=10000;	//1-9999 reserved for non-transient objects that may exist in several places at once.
		private HashMap<Integer, WeakReference<U>> assignedNumbers=new HashMap();

		public Objects(Class S)
		{
			myClass=S;
			//this.name=name;
			values=null;
			objVector.add(this);
		}
		public String name()
		{
			return CMClass.rawClassName(myClass);
		}

		public synchronized int getNumber(U forThis)
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
			assignedNumbers.put(saveNumber, new WeakReference(forThis));
			//Log.sysOut("SID "+name(),"Giving number "+saveNumber+" to "+forThis.ID());
			int result=saveNumber++;
			CMLib.misc().saveThis();
			//CMLib.database().saveObject(forThis);
			return result;
		}
		public int currentNum(){return saveNumber;}
		public void setNum(int num){saveNumber=num;}
		public void assignNumber(int i, U A)
		{
			if(i==0)
				return;
			assignedNumbers.put(i, new WeakReference(A));
			//Log.sysOut("SID "+name(),"Registered num "+i+" for "+A.ID());
		}
		public void removeNumber(int i)
		{
			assignedNumbers.remove(i);
			//Log.sysOut("SID "+name(),"Removed num "+i);
		}
		public U get(Integer i)
		{ 
			WeakReference<U> ref=assignedNumbers.get(i);
			U obj=(ref==null)?null:ref.get();
			//if(obj==null) Log.sysOut("SID "+name(),"No number "+i);
			//else Log.sysOut("SID "+name(),"Found number "+i);
			return obj;
		}
		public void save()
		{
			for(Iterator<WeakReference<U>> e=assignedNumbers.values().iterator();e.hasNext();)
			{
				U obj=e.next().get();
				if(obj!=null) obj.saveThis();	//An extra call this way but will definitely save properly and not save things like OpenExit
				//CMLib.database().saveObject(e.next().get());	//Not the safe/proper way of doing this!
			}
		}
		public Iterator<U> getAll(){return ((HashMap)assignedNumbers.clone()).values().iterator(); }
	}
	
	/*
	public enum Objects
	{
		CREATURE(MOB.class), RIDEABLE(Rideable.class), BEHAVIOR(Behavior.class), EFFECT(Effect.class),
		ROOM(Room.class), ITEM(Item.class), AREA(Area.class), EXIT(Exit.class), 
		ITEMCOLLECTION(ItemCollection.class), ACCOUNTSTATS(AccountStats.class)
		//CLOSEABLE(Closeable.class), ENVIRONMENTAL(Environmental.class), ACCOUNT(PlayerAccount.class), PLAYERDATA(PlayerStats.class), 
		;
		public final Class myClass;
		private int saveNumber=10000;	//1-9999 reserved for non-transient objects that may exist in several places at once.
		private HashMap<Integer, WeakReference<CMSavable>> assignedNumbers=new HashMap();

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
			assignedNumbers.put(saveNumber, new WeakReference(forThis));
			//Log.sysOut("SID "+name(),"Giving number "+saveNumber+" to "+forThis.ID());
			int result=saveNumber++;
			CMLib.misc().saveThis();
			//CMLib.database().saveObject(forThis);
			return result;
		}
		public int currentNum(){return saveNumber;}
		public void setNum(int num){saveNumber=num;}
		public void assignNumber(int i, CMSavable A)
		{
			if(i==0)
				return;
			assignedNumbers.put(i, new WeakReference(A));
			//Log.sysOut("SID "+name(),"Registered num "+i+" for "+A.ID());
		}
		public void removeNumber(int i)
		{
			assignedNumbers.remove(i);
			//Log.sysOut("SID "+name(),"Removed num "+i);
		}
		public CMSavable get(Integer i)
		{ 
			WeakReference<CMSavable> ref=assignedNumbers.get(i);
			CMSavable obj=(ref==null)?null:ref.get();
			//if(obj==null) Log.sysOut("SID "+name(),"No number "+i);
			//else Log.sysOut("SID "+name(),"Found number "+i);
			return obj;
		}
		public void save()
		{
			for(Iterator<WeakReference<CMSavable>> e=assignedNumbers.values().iterator();e.hasNext();)
			{
				CMSavable obj=e.next().get();
				if(obj!=null) obj.saveThis();	//An extra call this way but will definitely save properly and not save things like OpenExit
				//CMLib.database().saveObject(e.next().get());	//Not the safe/proper way of doing this!
			}
		}
		public Iterator getAll(){return ((HashMap)assignedNumbers.clone()).values().iterator(); }
	}
	*/
	public static Objects getType(CMSavable O)
	{
		for(Objects type : Objects.values())
			if(type.myClass.isInstance(O)) return type;
		return null;
	}
}