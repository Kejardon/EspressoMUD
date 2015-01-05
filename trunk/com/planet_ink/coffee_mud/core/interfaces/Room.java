package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public interface Room extends ItemCollection.ItemHolder, Interactable, CMSavable, CMModifiable //ContainableRoom will extend Item.
{
	//public static final EnumSet<CMMsg.MsgCode> showHappensSet=EnumSet.of(CMMsg.MsgCode.SHOWHAPPEN);
	public static Room[] dummyRoomArray=new Room[0];
	/*
	public static class REMap implements Interactable
	{
		protected static final ConcurrentLinkedQueue<REMap> REMapCache = new ConcurrentLinkedQueue();
		public Room room;
		public Exit exit;
		public REMap(Room R, Exit E){room=R; exit=E;}
		protected REMap(){}
		
		public static REMap newMap(Room R, Exit E)
		{
			REMap map = REMapCache.poll();
			if(map==null)
				map=new REMap();
			map.room=R;
			map.exit=E;
			return map;
		}
		public void returnThis(){room=null; exit=null; REMapCache.offer(this);}

		public boolean equals(Object O)
		{
			if(O instanceof REMap)
				return (((REMap)O).exit==exit)&&(((REMap)O).room==room);
			return false;
		}
		@Override public String ID(){return "REMap";}
		public Environmental getEnvObject() { return exit.getEnvObject(); }
		public CMObject newInstance(){return null;}
		public CMObject copyOf(){return new REMap(room, exit);}
		@Override public void initializeClass(){}
		public int compareTo(CMObject O){return -1;}

		public String name(){return exit.name();}
		public String plainName(){return exit.plainName();}
		public void setName(String newName){}
		public String displayText(){return exit.directLook(null, room);}
		public String plainDisplayText(){return CMLib.coffeeFilter().toRawString(exit.directLook(null, room));}
		public void setDisplayText(String newDisplayText){}
		public String description(){return exit.description();}
		public String plainDescription(){return exit.plainDescription();}
		public void setDescription(String newDescription){}

		public void addBehavior(Behavior to){}
		public void delBehavior(Behavior to){}
		public boolean hasBehavior(String ID){return false;}
		public int numBehaviors(){return 0;}
		public Behavior fetchBehavior(int index){return null;}
		public Behavior fetchBehavior(String ID){return null;}
		public Iterator<Behavior> allBehaviors(){return Collections.emptyIterator();}

		public void addEffect(Effect to){}
		public void delEffect(Effect to){}
		public boolean hasEffect(Effect to){return false;}
		public int numEffects(){return 0;}
		public Effect fetchEffect(int index){return null;}
		public Vector<Effect> fetchEffect(String ID){return CMClass.emptyVector;}
		public Effect fetchFirstEffect(String ID){return null;}
		public Iterator<Effect> allEffects(){return Collections.emptyIterator();}

		public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
		public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
		public CopyOnWriteArrayList<OkChecker> okCheckers(){return null;}
		public CopyOnWriteArrayList<ExcChecker> excCheckers(){return null;}
		public CopyOnWriteArrayList<TickActer> tickActers(){return null;}
		public void removeListener(Listener oldAffect, EnumSet flags){}
		public void addListener(Listener newAffect, EnumSet flags){}
		public void registerListeners(ListenHolder forThis){}
		public void registerAllListeners(){}
		public void clearAllListeners(){}
		public int priority(ListenHolder forThis){return 0;}
		public EnumSet<ListenHolder.Flags> listenFlags() {return EnumSet.noneOf(ListenHolder.Flags.class);}
		public Tickable.TickStat getTickStatus(){return Tickable.TickStat.Not;}
		public boolean tick(int tickTo){return false;}
		public int tickCounter(){return 0;}
		@Override public boolean respondTo(CMMsg msg){return true;}
		@Override public boolean respondTo(CMMsg msg, Object data){return true;}
		@Override public boolean okMessage(OkChecker myHost, CMMsg msg){return true;}
		@Override public void executeMsg(ExcChecker myHost, CMMsg msg){return;}
	}
	*/
/*
	public enum Substance
	{
		AIR, WATER, DIRT;
	}
	*/
	public enum Domain
	{
		AIR, WATER, DIRT
		/*
		UNDERWATER, AIR, WATERSURFACE,
		WOODS, JUNGLE, SWAMP, PLAINS, DESERT, ROCKS, MOUNTAINS, CITY,
		*/
		;
		protected static Domain[] options;
		public static Domain getDomain(int i)
		{
			if(options==null)
				options=values();
			return options[i];
		}
	}
	public enum Enclosure
	{
		OPEN, WALLS, ENCLOSED, AIRTIGHT
		;
		protected static Enclosure[] options;
		public static Enclosure getEnclosure(int i)
		{
			if(options==null)
				options=values();
			return options[i];
		}
	}

//	public String roomID();
//	public void setRoomID(String newRoomID);
	public Domain domain();
	public Enclosure enclosure();
	public void recoverRoomStats();
	public Area getArea();
	public void setArea(Area newArea);
	public void setAreaRaw(Area newArea);

	@Override public Room newInstance();
	@Override public Room copyOf();
	public int numExits();
	public void addExit(Exit E, Room destination);
	public void addExit(ExitInstance E);
	public void removeExit(ExitInstance R);
	//public void removeExit(Exit E, Room R);
	public Exit getExit(int i);
	public Exit getExit(String target);
	public Room getExitDestination(int i);
	public Room getExitDestination(Exit E);
	public ExitInstance getExitInstance(int i);
	public ExitInstance getExitInstance(String target);
	public ExitInstance getExitInstance(Exit E, Room R);
	public boolean hasExit(ExitInstance E);
	//public REMap hasREMap(REMap M);
	public Iterator<ExitInstance> getAllExits();
	/*
	public boolean changeExit(ExitInstance M, Exit newE);
	public boolean changeExit(ExitInstance M, Room newR);
	public boolean changeExit(ExitInstance M, ExitInstance newM);
	*/
//	public int getExitIndex(String target);
//	public int getExitIndex(Exit E, Room R);
//	public void initExits();

	public boolean hasLock(Thread T);
	public boolean hasLock();
	public String undoLock();
	public int getLock(long time);
	public void returnLock();
	public boolean hasPositions();
	public EnvMap.EnvLocation findObject(String findThis, MOB finder, EnvMap.EnvLocation lookingFromHere);
	public EnvMap.EnvLocation positionOf(Environmental.EnvHolder ofThis);

	public boolean doMessage(CMMsg msg);
	public boolean doAndReturnMsg(CMMsg msg);
	public void send(CMMsg msg);
	public void show(Interactable like, String allMessage);
	public void show(Interactable source,
						Interactable target,
						CMObject tool,
						String allMessage);
	public void show(Interactable source,
						Interactable target,
						CMObject tool,
						String srcMessage,
						String tarMessage,
						String othMessage);

	public void bringHere(Item I, boolean andRiders);
	public void placeHere(Environmental.EnvHolder I, boolean andRiders, int x, int y, int z);

	public boolean isContent(Item item, boolean checkSubItems);
	public Item fetchItem(String itemID);
	public Vector<Item> fetchItems(String itemID);
	public Vector<MOB> fetchInhabitants(String inhabitantID);
	public MOB fetchInhabitant(String inhabitantID);

/*
	public void sendOthers(CMObject source, CMMsg msg);
	public boolean isHere(Environmental E);
	public MOB fetchInhabitant(String inhabitantID);
	public Vector fetchInhabitants(String inhabitantID);
	public void addInhabitant(MOB mob);
	public void delInhabitant(MOB mob);
	public MOB fetchInhabitant(int i);
	public int numInhabitants();
	public int numPCInhabitants();
	public MOB fetchPCInhabitant(int i);
	public String getContextName(Environmental E);
	public Item fetchAnyItem(String itemID);
	public Vector fetchAnyItems(String itemID);
	public Environmental fetchFromRoomFavorItems(Item goodLocation, String thingName,int wornFilter);
	public Environmental fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, int wornFilter);
	public Environmental fetchFromRoomFavorMOBs(Item goodLocation, String thingName, int wornFilter);
	public Environmental fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, int wornFilter);
	public Environmental fetchFromMOBRoomFavorsMOBs(MOB mob, Item goodLocation, String thingName, int wornFilter);
	public boolean isInhabitant(MOB mob);
*/
}