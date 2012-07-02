package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

//import com.planet_ink.coffee_mud.Exits.StdExit;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import java.lang.ref.WeakReference;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMMap extends StdLibrary implements WorldMap
{
	public String ID(){return "CMMap";}
	protected Area[] sortedAreas=Area.dummyAreaArray;
	protected SortedVector<Area> areasList = new SortedVector<Area>();
	protected Hashtable<CMMsg.MsgCode,CopyOnWriteArrayList<ListenHolder.MsgListener>> globalHandlers=new Hashtable();
	private ThreadEngine.SupportThread thread=null;
	protected long lastVReset=0;
	//protected Exit openExit;
	//protected SortedVector<Exit> exits = new SortedVector();

	public ThreadEngine.SupportThread getSupportThread() { return thread;}
	public void initializeClass()
	{
		//openExit=CMClass.EXIT.get("OpenExit");
		CMClass.LOCALE.add(LimboRoom);
		LimboRoom.getArea();
		LimboRoom.getItemCollection();
		SIDLib.ROOM.assignNumber(LimboRoom.saveNum(), LimboRoom);
	}
	//TODO: Look at this after StdRoom
	protected static Room LimboRoom=new com.planet_ink.coffee_mud.Locales.StdRoom()
	{
		//protected Area myArea=null;
		//protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
		//protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
		//protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK, ListenHolder.Flags.EXC);
		//protected ItemCollection inventory=null;
		public String ID(){return "LimboRoom";}
		//public Room(){}
		public CMObject newInstance(){return this;}
		public Environmental getEnvObject() {return (Environmental)CMClass.COMMON.get("DefaultEnvironmental");}
		public String name(){ return "Limbo";}
		public String plainName(){ return "Limbo";}
		public void setName(String newName){}
		public String displayText(){return "Nothing should be here. If there is, an archon should take care of it.";}
		public String plainDisplayText(){return "Nothing should be here. If there is, an archon should take care of it.";}
		public void setDisplayText(String newDisplayText){}
		public String description(){return "";}
		public String plainDescription(){return "";}
		public void setDescription(String newDescription){}
		public CMObject copyOf(){ return this; }
		public int numExits() { return 0;}
		public void addExit(Exit E, Room destination){}
		public void addExit(REMap R){}
		public void removeExit(Exit E, Room R){}
		public void removeExit(REMap R){}
		public Exit getExit(int i){ return null;}
		public Exit getExit(String target){ return null;}
		public Room getExitDestination(int i){ return null;}
		public Room getExitDestination(Exit E){ return null;}
		public REMap getREMap(int i){ return null;}
		public REMap getREMap(String S){ return null;}
		public boolean changeExit(REMap R, Exit newExit){ return false;}
		public boolean changeExit(REMap R, Room newRoom){ return false;}
		public boolean changeExit(REMap R, REMap newMap){ return false;}
		public Area getArea(){
			found:
			if(myArea==null) synchronized(this){
				if(myArea!=null) break found;
				myArea=SIDLib.AREA.get(1);
				if(myArea!=null) break found;
				myArea=CMClass.AREA.getNew("StdArea");
				myArea.setSaveNum(1);
				myArea.setName("Limbo");
				myArea.addProperRoom(this);
				CMLib.map().addArea(myArea); }
			return myArea; }
		public void setArea(Area newArea){}
		public void setAreaRaw(Area newArea){}
		//public boolean okMessage(OkChecker myHost, CMMsg msg) { return msg.hasSourceCode(CMMsg.MsgCode.LOOK); }
		//public boolean respondTo(CMMsg msg){return true;}
		//public void executeMsg(ExcChecker myHost, CMMsg msg) {}
		public Tickable.TickStat getTickStatus(){return Tickable.TickStat.Not;}
		public boolean tick(int tickTo){return false;}
		public int tickCounter(){return 0;}
		//public void tickAct(){}
		public void recoverRoomStats(){}
		/*public void bringHere(Item I, boolean andRiders) {
			if(I==null) return;
			CMObject o=I.container();
			if((o==null)||(o==this)) return;
			if(!(andRiders)) {
				Rideable R=Rideable.O.getFrom(I);
				if(R!=null) for(Iterator<Item> iter=R.allRiders();iter.hasNext();) R.removeRider(iter.next()); }
			ItemCollection col=ItemCollection.O.getFrom(o);
			if(col!=null) col.removeItem(I);
			getItemCollection().addItem(I); } */
		public void destroy(){}
		public boolean amDestroyed(){return false;}
		public void removeListener(Listener oldAffect, EnumSet<ListenHolder.Flags> flags) {
			ListenHolder.O.removeListener(this, oldAffect, flags); }
		public void addListener(Listener newAffect, EnumSet<ListenHolder.Flags> flags) {
			ListenHolder.O.addListener(this, newAffect, flags); }
		public void registerAllListeners() {}
		public void clearAllListeners() {}
		//public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
		//public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
		//public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
		//public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
		public CopyOnWriteArrayList<TickActer> tickActers(){return null;}
		//public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
		public void addEffect(Effect to){}
		public void delEffect(Effect to){}
		public boolean hasEffect(Effect to){return false;}
		public int numEffects(){return 0;}
		public Effect fetchEffect(int index){return null;}
		public Vector<Effect> fetchEffect(String ID){ return new Vector(1); }
		public Iterator<Effect> allEffects() { return Collections.emptyIterator(); }
		public void addBehavior(Behavior to){}
		public void delBehavior(Behavior to){}
		public int numBehaviors(){return 0;}
		public Behavior fetchBehavior(int index){return null;}
		public Behavior fetchBehavior(String ID){return null;}
		public Iterator<Behavior> allBehaviors(){ return Collections.emptyIterator(); }
		public ItemCollection getItemCollection(){
			found:
			if(inventory==null) synchronized(this){
				if(inventory!=null) break found;
				inventory=SIDLib.ITEMCOLLECTION.get(1);
				if(inventory!=null) break found;
				inventory=(ItemCollection)((Ownable)CMClass.COMMON.getNew("DefaultItemCol")).setOwner(this);
				inventory.setSaveNum(1); }
			return inventory; }
		public boolean sameAs(Interactable E){return E==this;}
		public SaveEnum[] totalEnumS(){return CMSavable.dummySEArray;}
		public Enum[] headerEnumS(){return CMClass.dummyEnumArray;}
		public ModEnum[] totalEnumM(){return CMModifiable.dummyMEArray;}
		public Enum[] headerEnumM(){return CMClass.dummyEnumArray;}
		public int saveNum(){ return 1; }
		public void setSaveNum(int num){}
		public boolean needLink(){return true;}
		public void link(){
			myArea = SIDLib.AREA.get(1);
			if(myArea==null) getArea();
			else myArea.addProperRoom(this);
			inventory = SIDLib.ITEMCOLLECTION.get(1);
			if(inventory==null) getItemCollection();
			else ((Ownable)inventory).setOwner(this); }
		public void saveThis(){}
		public void prepDefault(){getArea(); getItemCollection();}
	};

	// areas
	public int numAreas() { return areasList.size(); }
	public void addArea(Area newOne) { synchronized(areasList){areasList.addRandom(newOne); sortedAreas=areasList.toArray(Area.dummyAreaArray);}}
	public void delArea(Area oneToDel) { synchronized(areasList){areasList.remove(oneToDel); sortedAreas=areasList.toArray(Area.dummyAreaArray); }}
	public Area getArea(String calledThis)
	{
		Area[] areas=sortedAreas;
		int start=0;
		int end=areas.length-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=calledThis.compareToIgnoreCase(areas[mid].name());
			if(comp==0) return areas[mid];
			else if(comp<=0) end=mid-1;
			else start=mid+1;
		}
		return null;
	}
	public Area findAreaStartsWith(String calledThis)
	{
		calledThis=calledThis.toUpperCase();
		Area[] areas=sortedAreas;
		int start=0;
		int end=areas.length-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=areas[mid].name().compareTo(calledThis);
			if(areas[mid].name().toUpperCase().startsWith(calledThis)) return areas[mid];
			else if(comp<=0) end=mid-1;
			else start=mid+1;
		}
		return null;
	}

	public Iterator<Area> areas() { return new CMParms.IteratorWrapper<Area>(sortedAreas); }
	public Area getFirstArea()
	{
		if(sortedAreas.length>0) return sortedAreas[0];
		return null;
	}
	public Area getRandomArea()
	{
		if(sortedAreas.length>0) return sortedAreas[CMath.random(sortedAreas.length)];
		return null;
	}

	public void addGlobalHandler(ListenHolder.MsgListener E, CMMsg.MsgCode category)
	{
		if(E==null) return;
		CopyOnWriteArrayList<ListenHolder.MsgListener> V=globalHandlers.get(category);
		if(V==null)
		{
			synchronized(globalHandlers)
			{
				V=globalHandlers.get(category);
				if(V==null)
				{
					V=new CopyOnWriteArrayList();
					globalHandlers.put(category,V);
				}
			}
		}
		V.addIfAbsent(E);
	}

	public void delGlobalHandler(ListenHolder.MsgListener E, CMMsg.MsgCode category)
	{
		CopyOnWriteArrayList<ListenHolder.MsgListener> V=globalHandlers.get(category);
		if((E==null)||(V==null)) return;
		V.remove(E);
	}

	public int numRooms()
	{
		int total=0;
		for(Area A : sortedAreas)
			total+=A.properSize();
		return total;
	}

	public boolean sendGlobalMessage(ListenHolder.MsgListener host, EnumSet<CMMsg.MsgCode> categories, CMMsg msg)
	{
		for(CMMsg.MsgCode category : categories)
		{
			CopyOnWriteArrayList<ListenHolder.MsgListener> V=globalHandlers.get(category);
			if(V!=null)
			try{
				ListenHolder.MsgListener[] listeners=(ListenHolder.MsgListener[])V.toArray(new ListenHolder.MsgListener[0]);
				for(ListenHolder.MsgListener O : listeners)
				{
					if(O instanceof Interactable)
					{
						Interactable E=(Interactable)O;
						if(!CMLib.flags().isInTheGame(E,true))
						{
							if(!CMLib.flags().isInTheGame(E,false))
								delGlobalHandler(E,category);
						}
						else if(!E.okMessage(host,msg))
							return false;
					}
					else if(!O.okMessage(host, msg))
						return false;
				}
				if(!msg.handleResponses())
					return false;
				for(ListenHolder.MsgListener O : listeners)
					O.executeMsg(host,msg);
			} catch(Exception x){Log.errOut("CMMap",x);}
		}
		return true;
	}

	//TODO: These need a second look at after I know the other routines better
	public Vector<Room> findRooms(Iterator<Room> rooms, String srchStr, boolean displayOnly, int millisPerSec)
	{
		Vector<Room> roomsV=new Vector();
		addWorldRoomsLiberally(roomsV,findRooms(rooms,srchStr,displayOnly,false,millisPerSec));
		return roomsV;
	}
	public Room findFirstRoom(Iterator<Room> rooms, String srchStr, boolean displayOnly, int millisPerSec)
	{ 
		Vector<Room> roomsV=new Vector();
		addWorldRoomsLiberally(roomsV,findRooms(rooms,srchStr,displayOnly,true,millisPerSec));
		if(roomsV.size()>0) return roomsV.firstElement();
		return null;
	}
	public Vector<Room> findRooms(Iterator<Room> rooms, String srchStr, boolean displayOnly, boolean returnFirst, int millisPerSec)
	{
		Vector<Room> foundRooms=new Vector();
		ArrayList<Room> completeRooms=CMParms.toArrayList(rooms);
		/*timePct=timePct*10;
		if(timePct>1000) timePct=1000;
		else if(timePct<0) timePct=0; */
		
		Iterator<Room> enumSet;
		enumSet=completeRooms.iterator();
		while(enumSet.hasNext())
		{
			if(displayOnly)
				findRoomsByDisplay(enumSet,foundRooms,srchStr,returnFirst,millisPerSec);
			else
				findRoomsByDispOrDesc(enumSet,foundRooms,srchStr,returnFirst,millisPerSec);
			if((returnFirst)&&(foundRooms.size()>0)) return foundRooms;
			if(enumSet.hasNext()) try{Thread.sleep(1000 - millisPerSec);}catch(Exception e){}
		}
		return foundRooms;
	}
	protected void findRoomsByDisplay(Iterator<Room> rooms, Vector<Room> foundRooms, String srchStr, boolean returnFirst, long maxTime)
	{
		srchStr=srchStr.toLowerCase();
		long startTime=System.currentTimeMillis();
		boolean useTimer=maxTime>1;
		for(;rooms.hasNext();)
		{
			Room room=rooms.next();
			if(CMLib.coffeeFilter().toRawString(room.displayText()).toLowerCase().contains(srchStr))
			{
				foundRooms.add(room);
				if(returnFirst) return;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
				return;
		}
	}
	protected void findRoomsByDispOrDesc(Iterator<Room> rooms, Vector<Room> foundRooms, String srchStr, boolean returnFirst, long maxTime)
	{
		srchStr=srchStr.toLowerCase();
		long startTime=System.currentTimeMillis();
		boolean useTimer=maxTime>1;
		for(;rooms.hasNext();)
		{
			Room room=rooms.next();
			if((CMLib.coffeeFilter().toRawString(room.displayText()).toLowerCase().contains(srchStr))
			  ||(CMLib.coffeeFilter().toRawString(room.description()).toLowerCase().contains(srchStr)))
			{
				foundRooms.add(room);
				if(returnFirst) return;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
				return;
		}
	}
	protected void findRoomsByDesc(Iterator<Room> rooms, Vector<Room> foundRooms, String srchStr, boolean returnFirst, long maxTime)
	{
		srchStr=srchStr.toLowerCase();
		long startTime=System.currentTimeMillis();
		boolean useTimer=maxTime>1;
		for(;rooms.hasNext();)
		{
			Room room=rooms.next();
			if(CMLib.english().containsString(CMStrings.removeColors(room.description()),srchStr))
			{
				foundRooms.add(room);
				if(returnFirst) return;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
				return;
		}
	}
	public Vector<MOB> findInhabitants(Iterator<Room> rooms, String srchStr, int millisPerSec)
	{ return findInhabitants(rooms,srchStr,false,millisPerSec);}
	public MOB findFirstInhabitant(Iterator<Room> rooms, String srchStr, int millisPerSec)
	{ 
		Vector<MOB> found=findInhabitants(rooms,srchStr,true,millisPerSec);
		if(found.size()>0) return (MOB)found.firstElement();
		return null;
	}
	public Vector<MOB> findInhabitants(Iterator<Room> rooms, String srchStr, boolean returnFirst, int millisPerSec)
	{
		Vector<MOB> found=new Vector();
		//long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		//if(delay>1000) delay=1000;
		boolean useTimer = millisPerSec>1;
		long startTime=System.currentTimeMillis();
		for(;rooms.hasNext();)
		{
			found.addAll(rooms.next().fetchInhabitants(srchStr));
			if((returnFirst)&&(found.size()>0)) return found;
			if((useTimer)&&((System.currentTimeMillis()-startTime)>millisPerSec))
				try{Thread.sleep(1000 - millisPerSec); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}
	public Vector<Item> findInventory(Iterator<Room> rooms, String srchStr, int millisPerSec)
	{ return findInventory(rooms,srchStr,false,millisPerSec);}
	public Item findFirstInventory(Iterator<Room> rooms, String srchStr, int millisPerSec)
	{ 
		Vector<Item> found=findInventory(rooms,srchStr,true,millisPerSec);
		if(found.size()>0) return found.firstElement();
		return null;
	}
	public Vector<Item> findInventory(Iterator<Room> rooms, String srchStr, boolean returnFirst, int millisPerSec)
	{
		Vector<Item> found=new Vector();
		//long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		//if(delay>1000) delay=1000;
		boolean useTimer = millisPerSec>1;
		long startTime=System.currentTimeMillis();
		MOB M=null;
		if(rooms==null)
		{
			for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
			{
				M=e.nextElement();
				if(M!=null)
					found.addAll(M.fetchInventories(srchStr));
				if((returnFirst)&&(found.size()>0)) return found;
			}
		}
		else
		for(;rooms.hasNext();)
		{
			Room room=rooms.next();
			ItemCollection coll=ItemCollection.O.getFrom(room);
			if(coll != null)
			{
				for(Iterator<Item> iter=coll.allItems();iter.hasNext();)
				{
					Item I = iter.next();
					if((!(I instanceof Body))||((M=((Body)I).mob())==null)) continue;
					found.addAll(M.fetchInventories(srchStr));
				}
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>millisPerSec)) 
				try{Thread.sleep(1000 - millisPerSec); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}

	public Vector findRoomItems(Iterator<Room> rooms, String srchStr, int millisPerSec)
	{ return findRoomItems(rooms,srchStr,false,millisPerSec);}
	public Item findFirstRoomItem(Iterator<Room> rooms, String srchStr, int millisPerSec)
	{ 
		Vector found=findRoomItems(rooms,srchStr,true,millisPerSec);
		if(found.size()>0) return (Item)found.firstElement();
		return null;
	}
	public Vector<Interactable> findRoomItems(Iterator<Room> rooms, String srchStr, boolean returnFirst, int millisPerSec)
	{
		Vector<Interactable> found=new Vector();
		//long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		//if(delay>1000) delay=1000;
		boolean useTimer = millisPerSec>1;
		long startTime=System.currentTimeMillis();
		for(;rooms.hasNext();)
		{
			Room room=rooms.next();
			if(room != null)
			{
				found.addAll(room.fetchItems(srchStr));
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>millisPerSec)) 
				try{Thread.sleep(1000 - millisPerSec); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}
	public Iterator<Room> rooms(){ return new AreaEnumerator(); }
	public Room getRandomRoom()
	{
		Room R=null;
		int numRooms=-1;
		while((R==null)&&((numRooms=numRooms())>0))
		{
			int which=CMath.random(numRooms);
			int total=0;
			for(Area A : sortedAreas)
			{
				if(which<(total+A.properSize()))
					{ R=A.getRandomProperRoom(); break;}
				total+=A.properSize();
			}
		}
		return R;
	}

	public Room findConnectingRoom(Room room)
	{
		if(room==null) return null;
		Room R=null;
		Room otherChoice=null;
		for(int i=room.numExits()-1;i>=0;i--)
		{
			R=room.getExitDestination(i);
			if(R!=null)
				for(int i1=R.numExits()-1;i1>=0;i1--)
					if(R.getExitDestination(i1)==room)
					{
						if(R.getArea()==room.getArea())
							return R;
						if(otherChoice==null) otherChoice=R;
					}
		}
		for(Iterator<Room> e=rooms();e.hasNext();)
		{
			R=e.next();
			if(R==room) continue;
			for(int i1=R.numExits()-1;i1>=0;i1--)
				if(R.getExitDestination(i1)==room)
				{
					if(R.getArea()==room.getArea())
						return R;
					if(otherChoice==null) otherChoice=R;
				}
		}
		return otherChoice;
	}

	public boolean isClearableRoom(Room R)
	{
		if((R==null)||(R.amDestroyed())) return true;
		MOB M=null;
		Room sR=null;
		Vector<MOB> inhabs=R.fetchInhabitants("ALL");
		for(int i=0;i<inhabs.size();i++)
		{
			M=inhabs.get(i);
			if(M==null) continue;
/*			sR=M.getStartRoom();
			if((sR!=null)
			&&(!sR.roomID().equals(R.roomID())))
				return false; */
			if((M.session()!=null)||(M.playerStats()!=null))
				return false;
		}
		return true;
	}
	public static class AreaEnumerator implements Iterator<Room>
	{
		private Iterator<Area> areas=CMLib.map().areas();
		private Iterator<Room> rooms=null;
		public boolean hasNext()
		{
			while((rooms==null)||(!rooms.hasNext()))
			{
				if(!areas.hasNext()) return false;
				rooms=new CMParms.IteratorWrapper<Room>(areas.next().getProperMap());
			}
			return true;
		}
		public Room next()
		{
			while((rooms==null)||(!rooms.hasNext()))
			{
				if(!areas.hasNext()) throw new NoSuchElementException();
				rooms=new CMParms.IteratorWrapper<Room>(areas.next().getProperMap());
			}
			return rooms.next();
		}
		public void remove(){}
	}

	public void obliterateRoom(Room deadRoom)
	{
		/*for(int a=deadRoom.numEffects()-1;a>=0;a--)
		{
			Effect A=deadRoom.fetchEffect(a);
			if(A!=null)
			{
				A.unInvoke();
				deadRoom.delEffect(A);
			}
		}
		for(int i=deadRoom.numExits()-1;i>=0;i--)
		{
			Room.REMap thatExit=deadRoom.getREMap(i);
			thatExit.room.removeExit(new Room.REMap(deadRoom, thatExit.exit));
			deadRoom.removeExit(thatExit);
		}
		Vector<MOB> inhabs=deadRoom.fetchInhabitants("ALL");
		for(int m=inhabs.size()-1;m>=0;m--)
		{
			MOB M=inhabs.get(m);
			if((M!=null)&&(M.playerStats()!=null))
				CMLib.login().getDefaultStartRoom(M).bringHere(M.body(), true);
		}
		emptyRoom(deadRoom,null); */
		deadRoom.destroy();
		//CMLib.database().DBDeleteRoom(deadRoom);
	}

	public void emptyArea(Area A)
	{
		for(Iterator<Effect> iter=A.allEffects();iter.hasNext();)
		{
			Effect A1=iter.next();
			A1.unInvoke();
			A.delEffect(A1);
		}
		for(Room R : A.getProperMap())
		{
			emptyRoom(R,null);
			R.destroy();
		}
	}
	public Room roomLocation(CMObject E)
	{
		while(true)
		{
			if(E==null||E instanceof Exit)
				return null;
			if(E instanceof Area)
				return ((Area)E).getRandomProperRoom();
			if(E instanceof Room)	//TODO: Expand this for item rooms? :s Not always appropriate though. Add a new function or argument to distinguish
				return (Room)E;
			if(E instanceof MOB)
				return ((MOB)E).location();
			
			E=goUpOne(E);
		}
		/*
		if((E instanceof Item)&&(((Item)E).container() != null))
			return roomLocation(((Item)E).container());
		if((E instanceof Effect)&&(((Effect)E).affecting() != null))
			return roomLocation(((Effect)E).affecting());
		if((E instanceof Behavior)&&(((Behavior)E).behaver() != null))
			return roomLocation(((Behavior)E).behaver());
		return null;
		*/
	}
	public CMObject goUpOne(CMObject E)
	{
		if(E instanceof Room)	//TODO: Expand this for item rooms? :s Not always appropriate though
			return ((Room)E).getArea();
		if(E instanceof MOB)
			return ((MOB)E).body();
		if(E instanceof Item)
			return ((Item)E).container();
		if(E instanceof Effect)
			return ((Effect)E).affecting();
		if(E instanceof Behavior)
			return ((Behavior)E).behaver();
		return null;
	}
	public Area areaLocation(CMObject E)
	{
		if(E==null) return null;
		if(E instanceof Area)
			return (Area)E;
		Room R=roomLocation(E);
		if(R!=null) return R.getArea();
		/*if(E instanceof Room)
			return ((Room)E).getArea();
		if(E instanceof MOB)
			return ((MOB)E).location().getArea();
		if(E instanceof Item)
			return areaLocation(((Item)E).container());*/
		return null;
	}

	public void emptyRoom(Room room, Room bringBackHere)
	{
		if(room==null) return;
		Vector<MOB> inhabs=room.fetchInhabitants("ALL");
		MOB M=null;
		
		for(int m=0;m<inhabs.size();m++)
		{
			M=inhabs.get(m);
			if(bringBackHere!=null)
				bringBackHere.bringHere(M.body(), false);
			else if(M.isMonster())
//			if((M.getStartRoom()==null)
//			||(M.getStartRoom()==room)
//			||(M.getStartRoom().ID().length()==0))
				M.destroy();
			else
			{
				Room R;
				gotARoom:
				{
					R=CMLib.login().getDefaultStartRoom(M);
					if((R!=null)&&(R!=room)) break gotARoom;
					R=CMLib.login().getDefaultDeathRoom(M);
					if((R!=null)&&(R!=room)) break gotARoom;
					R=CMLib.login().getDefaultBodyRoom(M);
					if((R!=null)&&(R!=room)) break gotARoom;
					R=CMLib.login().getDefaultBodyRoom(M);
					if((R!=null)&&(R!=room)) break gotARoom;
					Iterator<Room> rooms=rooms();
					while(((R==null)||(R==room))&&(rooms.hasNext())) R=rooms.next();
				}
				if((R==null)||(R==room))
				{
					Log.errOut("CMMap", "No place to dump players from room.");
					return;
				}
				R.bringHere(M.body(), false);
			}
		}
		Item I=null;
		inhabs = null;
		
		ItemCollection coll=ItemCollection.O.getFrom(room);
		if(bringBackHere!=null)
			for(Iterator<Item> iter=coll.allItems();iter.hasNext();)
				bringBackHere.bringHere(iter.next(),false);
		else
			for(Iterator<Item> iter=coll.allItems();iter.hasNext();)
				iter.next().destroy();
//		CMLib.threads().clearDebri(room,0);
//		room.resetVectors();
	}


	public void obliterateArea(String areaName)
	{
		Area A=getArea(areaName);
		if(A!=null) A.destroy();	//A.destroy will handle everything else, should also call below
	}
	public void finishObliterateArea(Area A, Room[] rooms)
	{
		for(Room R : rooms)
			obliterateRoom(R);
		delArea(A);
	}
//	public CMMsg resetMsg=null;

	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int millisPerSec, int maxSeconds)
	{
		Vector<Room> rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,true,millisPerSec, maxSeconds);
		if((rooms!=null)&&(rooms.size()!=0)) return rooms.firstElement();
		return null;
	}
	
	public Vector<Room> findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int millisPerSec, int maxSeconds)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,false,millisPerSec,maxSeconds); }
	
	public Room findAreaRoomLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int millisPerSec)
	{
		Vector<Room> rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,true,millisPerSec,120);
		if((rooms!=null)&&(rooms.size()!=0)) return rooms.firstElement();
		return null;
	}
	
	public Vector findAreaRoomsLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int millisPerSec)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,false,millisPerSec,120); }
	
	protected Room addWorldRoomsLiberally(Vector<Room> rooms, Vector<? extends Interactable> choicesV)
	{
		if(choicesV==null) return null;
		if(rooms!=null)
		{
			for(Interactable choice : choicesV)
				addWorldRoomsLiberally(rooms,roomLocation(choice));
			return null;
		}
		else
		{
			Room room=null;
			int tries=0;
			while(((room==null)||(room.saveNum()==0))&&((++tries)<200))
				room=roomLocation(choicesV.get(CMath.random(choicesV.size())));
			return room;
		}
	}
	
	protected Room addWorldRoomsLiberally(Vector<Room> rooms, Room room)
	{
		if(room==null) return null;
		if(rooms!=null)
		{ 
			if(!rooms.contains(room))
				rooms.add(room);
			return null;
		}
		return room;
	}
	
	protected Room addWorldRoomsLiberally(Vector<Room> rooms, Area area)
	{ return addWorldRoomsLiberally(rooms,area.getRandomProperRoom()); }
	
	protected Iterator<Room> rightLiberalMap(Area A) {
		if(A==null) return rooms();
		return new CMParms.IteratorWrapper<Room>(A.getProperMap());
	}

	protected Vector<Room> returnResponse(Vector<Room> rooms, Room room)
	{
		if(rooms!=null) return rooms;
		if(room==null) return new Vector<Room>(1);
		return (Vector<Room>)CMParms.makeVector(room);
	}
	
	protected boolean enforceTimeLimit(long startTime, int maxSeconds)
	{
		if(maxSeconds<=0) return false;
		return ((System.currentTimeMillis() - startTime) / 1000) > maxSeconds;
	}
	
	protected Vector<Room> findWorldRoomsLiberally(MOB mob, 
												   String cmd, 
												   String srchWhatAERIPMVK, 
												   Area A, 
												   boolean returnFirst, 
												   int millisPerSec, 
												   int maxSeconds)
	{
		Room room=null;
		Vector<Room> rooms=(returnFirst)?null:new Vector();
		
		Room curRoom=(mob!=null)?mob.location():null;
		
		boolean searchWeakAreas=false;
		boolean searchStrictAreas=false;
		boolean searchRooms=false;
		boolean searchPlayers=false;
		boolean searchItems=false;
		boolean searchInhabs=false;
		boolean searchInventories=false;
		char[] flags = srchWhatAERIPMVK.toUpperCase().toCharArray();
		for(int c=0;c<flags.length;c++)
			switch(flags[c])
			{
				case 'E': searchWeakAreas=true;   break;
				case 'A': searchStrictAreas=true; break;
				case 'R': searchRooms=true;       break;
				case 'P': searchPlayers=true;     break;
				case 'I': searchItems=true;       break;
				case 'M': searchInhabs=true;      break;
				case 'V': searchInventories=true; break;
			}
		long startTime = System.currentTimeMillis();
		if(searchRooms)
		{
//			Directions.Dirs dir=Directions.getGoodDirectionCode(cmd);
//			if((dir>=0)&&(curRoom!=null))
//				room=addWorldRoomsLiberally(rooms,curRoom.rawDoors()[dirCode]);
//			if(room==null)
				room=addWorldRoomsLiberally(rooms,SIDLib.ROOM.get(CMath.s_int(cmd)));
		}

		if(room==null)
		{
			// first get room ids
//			if((cmd.charAt(0)=='#')&&(curRoom!=null)&&(searchRooms))
//				room=addWorldRoomsLiberally(rooms,getRoom(curRoom.getArea().name()+cmd));
//			else
			{
				String srchStr=cmd;
				
				if(searchPlayers)
				{
					// then look for players
					Session sess=CMLib.sessions().findPlayerOnline(srchStr,false);
					MOB M;
					if((sess!=null) && ((M=sess.mob())!=null))
						room=addWorldRoomsLiberally(rooms,M.location());
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// search areas strictly
				if(searchStrictAreas && room==null && (A==null))
				{
					A=getArea(srchStr);
					if((A!=null) &&(A.properSize()>0))
						room=addWorldRoomsLiberally(rooms,A.getRandomProperRoom());
					A=null;
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// look for room inhabitants
				if(searchInhabs && room==null)
				{
					Vector<MOB> candidates=findInhabitants(rightLiberalMap(A), srchStr,returnFirst, millisPerSec);
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// now check room text
				if(searchRooms && room==null)
				{
					if(returnFirst)
						room=findFirstRoom(rightLiberalMap(A), srchStr, false, millisPerSec);
					else
					{
						Vector<Room> candidates=findRooms(rightLiberalMap(A), srchStr, false, millisPerSec);
						if(candidates.size()>0)
							room=addWorldRoomsLiberally(rooms,candidates);
					}
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// check floor items
				if(searchItems && room==null)
				{
					Vector<Interactable> candidates=findRoomItems(rightLiberalMap(A), srchStr,returnFirst,millisPerSec);
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// check inventories
				if(searchInventories && room==null)
				{
					Vector<Item> candidates=findInventory(rightLiberalMap(A), srchStr, returnFirst,millisPerSec);
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// search areas weakly
				if(searchWeakAreas && room==null && (A==null))
				{
					A=findAreaStartsWith(srchStr);
					if((A!=null) &&(A.properSize()>0))
						room=addWorldRoomsLiberally(rooms,A);
					A=null;
				}
			}
		}
		return returnResponse(rooms,room);
	}
	public boolean activate() 
	{
		if(thread==null)
			thread=new ThreadEngine.SupportThread("THMap"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging("SAVETHREAD"));
		if(!thread.started)
			thread.start();
		return true;
	}
	
	public boolean shutdown() {
		areasList.clear();
		sortedAreas=Area.dummyAreaArray;
		globalHandlers.clear();
		thread.shutdown();
		return true;
	}
	
	public void run()
	{
		return;
	}

	/*protected int getGlobalIndex(Vector list, String name)
	{
		if(list.size()==0) return -1;
		int start=0;
		int end=list.size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=((Environmental)list.elementAt(mid)).Name().compareToIgnoreCase(name);
			if(comp==0)
				return mid;
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;
		}
		return -1;
	}
	public Exit getExit(String S)
	{
		Exit E=new StdExit();
		E.setExitID(S);
		synchronized(exits)
		{
			int i=exits.indexOf(E);
			if(i>=0) return exits.get(i);
		}
		return null;
	}
	//Messy code to optimize speed, ideally Exits have an ID and are added in order, or do not have an ID.
	public void addExit(Exit E)
	{
		synchronized(exits)
		{
			if(E.exitID().length()>0)
			{
				if(E.compareTo(exits.lastElement())>0)
					exits.add(E);
				else
				{
					int i=exits.indexOf(E);
					if(i>=0)
						exits.setElementAt(E, i);
					else
						exits.add(-i-1, E);
				}
			}
			else
			{
				E.setExitID(getNewExitID());
				exits.add(E);
			}
		}
	}
	//Umm. Let's just go with numbers for now! Nobody's going to really see these anyways.
	public String getNewExitID()
	{
		Exit last=null;
		if(exits.size()>0) last=exits.lastElement();
		int i=1;
		if(last!=null) i=Integer.parseInt(last.exitID())+1;
		return ""+i;
	}
	public void removeExit(Exit E)
	{
		synchronized(exits) { exits.remove(E); }
	}
		private DVector getAllPlayersHere(Area area)
	{
		DVector playersHere=new DVector(2);
		Session S=null;
		MOB M=null;
		Room R=null;
		for(int s=CMLib.sessions().size()-1;s>=0;s--)
		{
			S=CMLib.sessions().elementAt(s);
			M=(S!=null)?S.mob():null;
			R=(M!=null)?M.location():null;
			if((R!=null)&&(R.getArea()==area)&&(M!=null))
			{
				playersHere.addElement(M,getExtendedRoomID(R));
			}
		}
		return playersHere;
	}
	public void resetArea(Area area)
	{
		int oldFlag=area.getAreaState();
		area.setAreaState(Area.STATE_FROZEN);
		DVector playersHere=getAllPlayersHere(area);
		for(int p=0;p<playersHere.size();p++)
		{
			MOB M=(MOB)playersHere.elementAt(p,1);
			Room R=M.location();
			R.getItemCollection().removeItem(M.body());
		}
		for(Enumeration r=area.getProperMap();r.hasMoreElements();)
			resetRoom((Room)r.nextElement());
		area.fillInAreaRooms();
		for(int p=0;p<playersHere.size();p++)
		{
			MOB M=(MOB)playersHere.elementAt(p,1);
			Room R=getRoom((String)playersHere.elementAt(p,2));
			if(R==null) R=M.getStartRoom();
			if(R==null) R=getStartRoom(M);
			if(R!=null) 
				R.bringHere(M.body(), false);
		}
		area.setAreaState(oldFlag);
	}
	public Area getStartArea(Interactable E)
	{
		if(E instanceof Area) return (Area)E;
		Room R=getStartRoom(E);
		if(R==null) return null;
		return R.getArea();
	}
	public Room getStartRoom(Interactable E)
	{
		if(E ==null) return null;
		if(E instanceof MOB)
			return ((MOB)E).getStartRoom();
		if((E instanceof Item)&&(((Item)E).container() instanceof Interactable))
			return getStartRoom((Interactable)((Item)E).container());
		if((E instanceof Effect)&&(((Effect)E).affecting() instanceof Interactable))
			return getStartRoom((Interactable)((Effect)E).affecting());
		if(E instanceof Area) return ((Area)E).getRandomProperRoom();
		return roomLocation(E);
	}
	//TODO: This probably needs to be fixed. Do I need resetMsg?
	public void resetRoom(Room room)
	{
		if(room==null) return;
		if(room.roomID().length()==0) return;
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=getRoom(room);
//			if(resetMsg==null) resetMsg=CMClass.getMsg(null,room,EnumSet.of(CMMsg.MsgCode.RESET,null);
//			resetMsg.setTarget(room);
//			room.executeMsg(room,resetMsg);
			emptyRoom(room,null);
			Effect A=null;
			for(int a=room.numEffects()-1;a>=0;a--)
			{
				A=room.fetchEffect(a);
				if(A!=null)
					A.unInvoke();
			}
			CMLib.database().DBReadContent(room,null);
		}
	}
	public boolean explored(Room R, Vector areas)
	{
		if((R==null)
		||(CMath.bset(R.envStats().sensesMask(),EnvStats.SENSE_ROOMUNEXPLORABLE))
		||(R.getArea()==null))
			return false;
		return false;
	}
	public String createNewExit(Room from, Room room)
	{
		Room opRoom=from.rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==from))
			return "Opposite room already exists and heads this way.  One-way link created.";

		Exit thisExit=null;
		synchronized(("SYNC"+from.roomID()).intern())
		{
			from=getRoom(from);
			if(opRoom!=null)
				from.rawDoors()[direction]=null;

			from.rawDoors()[direction]=room;
			thisExit=from.getRawExit(direction);
			if(thisExit==null)
			{
				thisExit=CMClass.getExit("StdOpenDoorway");
				from.setRawExit(direction,thisExit);
			}
			CMLib.database().DBUpdateExits(from);
		}
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=getRoom(room);
			if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
			{
				room.rawDoors()[Directions.getOpDirectionCode(direction)]=from;
				room.setRawExit(Directions.getOpDirectionCode(direction),thisExit);
				CMLib.database().DBUpdateExits(room);
			}
		}
		return "";
	}
	//TODO: This will have extra code when Item Rooms are made
	public Room getRoom(Vector<Room> roomSet, String calledThis)
	{
		try
		{
			if(calledThis==null) return null;
			Room R=null;
			if(roomSet==null)
			{
				int x=calledThis.indexOf("#");
				if(x>=0)
				{
					Area A=getArea(calledThis.substring(0,x));
					if(A!=null) R=A.getRoom(calledThis);
					if(R!=null) return R;
				}
			}
			else
			for(Enumeration<Room> e=roomSet.elements();e.hasMoreElements();)
			{
				R=e.nextElement();
				if(R.roomID().equalsIgnoreCase(calledThis))
					return R;
			}
		}
		catch(java.util.NoSuchElementException x){}
		return null;
	}
	public Room getRoom(Hashtable hashedRoomSet, String areaName, String calledThis)
	{
		if(calledThis.startsWith("#"))
		{
			if(hashedRoomSet.containsKey(calledThis.substring(1)))
				return (Room)hashedRoomSet.get(calledThis.substring(1));
		}
		else
		if(calledThis.startsWith(areaName+"#"))
		{
			if(hashedRoomSet.containsKey(calledThis.substring(areaName.length()+1)))
				return (Room)hashedRoomSet.get(calledThis.substring(areaName.length()+1));
		}
		else
		{
			if(hashedRoomSet.containsKey(calledThis))
				return (Room)hashedRoomSet.get(calledThis);
		}
		Room R=getRoom(calledThis);
		if(R!=null) return R;
		return getRoom(areaName+"#"+calledThis);
	}
	public Room getRoom(String calledThis){ return getRoom(null,calledThis); }*/
}