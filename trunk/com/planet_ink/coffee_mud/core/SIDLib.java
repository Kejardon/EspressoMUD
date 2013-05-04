package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
	// CLAIMED NON-TRANSIENT OBJECTS/IDS. None of these should have the same number and category, nor number over 9999.
	//ROOM
	public static final int LimboRoomSaveNum=1;
	
	//ITEMCOLLECTION
	public static final int LimboICSaveNum=1;
	
	//AREA
	public static final int LimboAreaSaveNum=1;
	
	public static final Objects<MOB> CREATURE=new Objects<MOB>(MOB.class,"CREATURE");
	public static final Objects<Rideable> RIDEABLE=new Objects<Rideable>(Rideable.class,"RIDEABLE");
	public static final Objects<Behavior> BEHAVIOR=new Objects<Behavior>(Behavior.class,"BEHAVIOR");
	public static final Objects<Effect> EFFECT=new Objects<Effect>(Effect.class,"EFFECT");
	public static final Objects<Room> ROOM=new Objects<Room>(Room.class,"ROOM");
	public static final Objects<Item> ITEM=new Objects<Item>(Item.class,"ITEM");
	public static final Objects<Area> AREA=new Objects<Area>(Area.class,"AREA");
	public static final Objects<Exit> EXIT=new Objects<Exit>(Exit.class,"EXIT");
	public static final Objects<ItemCollection> ITEMCOLLECTION=new Objects<ItemCollection>(ItemCollection.class,"ITEMCOLLECTION");
	public static final Objects<EnvMap> ENVMAP=new Objects<EnvMap>(EnvMap.class,"ENVMAP");
	public static final Objects<AccountStats> ACCOUNTSTATS=new Objects<AccountStats>(AccountStats.class,"ACCOUNTSTATS");
	public static final Objects<BindCollection> BINDCOLLECTION=new Objects<BindCollection>(BindCollection.class,"BINDCOLLECTION");
	public static final Objects<Bind> BIND=new Objects<Bind>(Bind.class,"BIND");
	public static final Objects<ExitInstance> EXITINSTANCE=new Objects<ExitInstance>(ExitInstance.class,"EXITINSTANCE");
	public static class Objects<U extends CMSavable>
	{
		private static final HashMap<String, Objects> objectsNames=new HashMap<String, Objects>();
		private static Vector<Objects> objVector=new Vector(10);
		private static Objects[] values;
		public static Objects[] values()
		{
			if(values==null)
				values=objVector.toArray(new Objects[0]);
			return values;
		}
		public static Objects valueOf(String S){return objectsNames.get(S);}

		public final Class myClass;
		public final String name;
		private int saveNumber=10000;	//1-9999 reserved for non-transient objects that may exist in several places at once.
		private HashMap<Integer, WeakReference<U>> assignedNumbers=new HashMap();

		public Objects(Class S, String name)
		{
			myClass=S;
			this.name=name;
			objectsNames.put(name, this);
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
			if(obj==null) Log.sysOut("SID "+name(),"No number "+i);
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
	public static Objects classCode(String name)
	{
		try{ return Objects.valueOf(name); }
		catch(IllegalArgumentException e){return null;}
	}
	public static Objects getType(CMSavable O)
	{
		for(Objects type : Objects.values())
			if(type.myClass.isInstance(O)) return type;
		return null;
	}
}