package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Exits.StdExit;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

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
	protected SortedVector<Area> areasList = new SortedVector<Area>();
	protected Hashtable<CMMsg.MsgCode,Vector<WeakReference<ListenHolder.MsgListener>>> globalHandlers=new Hashtable<CMMsg.MsgCode,Vector<WeakReference<ListenHolder.MsgListener>>>();
	private ThreadEngine.SupportThread thread=null;
	protected long lastVReset=0;
//	protected Exit openExit;
//	protected SortedVector<Exit> exits = new SortedVector();

	public ThreadEngine.SupportThread getSupportThread() { return thread;}
	public void initializeClass()
	{
		//openExit=(Exit)CMClass.Objects.EXIT.get("OpenExit");
	}

/*	protected int getGlobalIndex(Vector list, String name)
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
*/

/*	public Exit getExit(String S)
	{
		Exit E=new StdExit();
		E.setExitID(S);
		synchronized(exits)
		{
			int i=exits.indexOf(E);
			if(i>=0) return exits.get(i);
		}
		return null;
	} */
	//Messy code to optimize speed, ideally Exits have an ID and are added in order, or do not have an ID.
/*	public void addExit(Exit E)
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
*/

	// areas
	public int numAreas() { return areasList.size(); }
	public void addArea(Area newOne) { synchronized(areasList){areasList.addRandom(newOne); }}
	public void delArea(Area oneToDel) { synchronized(areasList){areasList.remove(oneToDel); }}
	public Area getArea(String calledThis)
	{
//		dummyArea.setName(calledThis);
		synchronized(areasList)
		{
			int start=0;
			int end=areasList.size()-1;
			int mid=0;
			while(start<=end)
			{
				mid=(end+start)/2;
				int comp=calledThis.compareToIgnoreCase(areasList.get(mid).name());
				if(comp==0) return areasList.get(mid);
				else if(comp<=0) end=mid-1;
				else start=mid+1;
			}
		}
		return null;
	}
	public Area findAreaStartsWith(String calledThis)
	{
		calledThis=calledThis.toUpperCase();
		synchronized(areasList)
		{
			int start=0;
			int end=areasList.size()-1;
			int mid=0;
			while(start<=end)
			{
				mid=(end+start)/2;
				int comp=areasList.get(mid).name().compareTo(calledThis);
				if(areasList.get(mid).name().toUpperCase().startsWith(calledThis)) return areasList.get(mid);
				else if(comp<=0) end=mid-1;
				else start=mid+1;
			}
		}
		return null;
	}

	public Enumeration<Area> areas() { return areasList.elements(); }
	public Area getFirstArea()
	{
		if(areasList.size()>0) return areasList.get(0);
		return null;
	}
	public Area getRandomArea()
	{
		Area A=null;
		while((areasList.size()>0)&&(A==null))
		{
			try{ A=areasList.elementAt(CMath.random(areasList.size())); }
			catch(ArrayIndexOutOfBoundsException e){}
		}
		return A;
	}

	public void addGlobalHandler(ListenHolder.MsgListener E, CMMsg.MsgCode category)
	{
		if(E==null) return;
		Vector<WeakReference<ListenHolder.MsgListener>> V=globalHandlers.get(category);
		if(V==null)
		{
			V=new Vector<WeakReference<ListenHolder.MsgListener>>();
			globalHandlers.put(category,V);
		}
		synchronized(V)
		{
			for(Enumeration<WeakReference<ListenHolder.MsgListener>> e=V.elements();e.hasMoreElements();)
				if(e.nextElement().get()==E)
					return;
			V.add(new WeakReference(E));
		}
	}

	public void delGlobalHandler(ListenHolder.MsgListener E, CMMsg.MsgCode category)
	{
		Vector<WeakReference<ListenHolder.MsgListener>> V=globalHandlers.get(category);
		if((E==null)||(V==null)) return;
		synchronized(V)
		{
			WeakReference foundW=null;
			for(Enumeration<WeakReference<ListenHolder.MsgListener>> e=V.elements();e.hasMoreElements();)
			{
				WeakReference W=e.nextElement();
				if(W.get()==E)
					foundW=W;
			}
			if(foundW != null)
				V.remove(foundW);
		}
	}

/*
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
*/

	public int numRooms()
	{
		int total=0;
		for(Enumeration<Area> e=areas();e.hasMoreElements();)
			total+=e.nextElement().properSize();
		return total;
	}

	public boolean sendGlobalMessage(ListenHolder.MsgListener host, EnumSet<CMMsg.MsgCode> categories, CMMsg msg)
	{
		for(CMMsg.MsgCode category : (CMMsg.MsgCode[])categories.toArray(new CMMsg.MsgCode[0]))
		{
			Vector<WeakReference<ListenHolder.MsgListener>> V=globalHandlers.get(category);
			if(V!=null)
			synchronized(V)
			{
				try{
					ListenHolder.MsgListener O=null;
					Interactable E=null;
					WeakReference<ListenHolder.MsgListener> W=null;
					for(int v=V.size()-1;v>=0;v--)
					{
						W=V.elementAt(v);
						O=W.get();
						if(O==null)
							V.removeElementAt(v);
						else if(O instanceof Interactable)
						{
							E=(Interactable)O;
							if(!CMLib.flags().isInTheGame(E,true))
							{
								if(!CMLib.flags().isInTheGame(E,false))
									delGlobalHandler(E,category);
							}
							else
							if(!E.okMessage(host,msg))
								return false;
						}
						else if(!O.okMessage(host, msg))
							return false;
					}
					if(!msg.handleResponses())
						return false;
					for(int v=V.size()-1;v>=0;v--)
					{
						W=V.elementAt(v);
						O=W.get();
						O.executeMsg(host,msg);
					}
				}
				catch(java.lang.ArrayIndexOutOfBoundsException xx){}
				catch(Exception x){Log.errOut("CMMap",x);}
			}
		}
		return true;
	}

	//TODO: This will have extra code when Item Rooms are made
/*	public Room getRoom(Vector<Room> roomSet, String calledThis)
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
*/
	//TODO: These need a second look at after I know the other routines better
	public Vector findRooms(Enumeration rooms, MOB mob, String srchStr, boolean displayOnly, int timePct)
	{
		Vector roomsV=new Vector();
/*		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,getRoom(mob.location().getArea().name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,getRoom(srchStr));
*/		addWorldRoomsLiberally(roomsV,findRooms(rooms,srchStr,displayOnly,false,timePct));
		return roomsV;
	}
	public Room findFirstRoom(Enumeration rooms, MOB mob, String srchStr, boolean displayOnly, int timePct)
	{ 
		Vector roomsV=new Vector();
/*		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,getRoom(mob.location().getArea().name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,getRoom(srchStr));
		if(roomsV.size()>0) return (Room)roomsV.firstElement();
*/		addWorldRoomsLiberally(roomsV,findRooms(rooms,srchStr,displayOnly,true,timePct));
		if(roomsV.size()>0) return (Room)roomsV.firstElement();
		return null;
	}
	public Vector<Room> findRooms(Enumeration<Room> rooms, String srchStr, boolean displayOnly, boolean returnFirst, int timePct)
	{
		Vector<Room> foundRooms=new Vector();
		Vector<Room> completeRooms=new Vector();
		try { completeRooms=(Vector<Room>)CMParms.makeVector(rooms); }catch(NoSuchElementException nse){}
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		
		Enumeration<Room> enumSet;
		enumSet=completeRooms.elements();
		while(enumSet.hasMoreElements())
		{
			findRoomsByDisplay(enumSet,foundRooms,srchStr,returnFirst,delay);
			if((returnFirst)&&(foundRooms.size()>0)) return foundRooms;
			if(enumSet.hasMoreElements()) try{Thread.sleep(1000 - delay);}catch(Exception e){}
		}
		if(!displayOnly)
		{
			enumSet=completeRooms.elements();
			while(enumSet.hasMoreElements())
			{
				findRoomsByDesc(enumSet,foundRooms,srchStr,returnFirst,delay);
				if((returnFirst)&&(foundRooms.size()>0)) return foundRooms;
				if(enumSet.hasMoreElements()) try{Thread.sleep(1000 - delay);}catch(Exception e){}
			}
		}
		return foundRooms;
	}
	protected void findRoomsByDisplay(Enumeration<Room> rooms, Vector<Room> foundRooms, String srchStr, boolean returnFirst, long maxTime)
	{
		long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			boolean useTimer=maxTime>1;
			for(;rooms.hasMoreElements();)
			{
				Room room=rooms.nextElement();
				if(CMLib.english().containsString(CMStrings.removeColors(room.displayText()),srchStr))
					foundRooms.addElement(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
		}catch(NoSuchElementException nse){}
	}
	protected void findRoomsByDesc(Enumeration<Room> rooms, Vector<Room> foundRooms, String srchStr, boolean returnFirst, long maxTime)
	{
		long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			boolean useTimer=maxTime>1;
			for(;rooms.hasMoreElements();)
			{
				Room room=rooms.nextElement();
				if(CMLib.english().containsString(CMStrings.removeColors(room.description()),srchStr))
					foundRooms.addElement(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
		}catch(NoSuchElementException nse){}
	}
	public Vector findInhabitants(Enumeration rooms, String srchStr, int timePct)
	{ return findInhabitants(rooms,srchStr,false,timePct);}
	public MOB findFirstInhabitant(Enumeration rooms, String srchStr, int timePct)
	{ 
		Vector found=findInhabitants(rooms,srchStr,true,timePct);
		if(found.size()>0) return (MOB)found.firstElement();
		return null;
	}
	public Vector<Interactable> findInhabitants(Enumeration<Room> rooms, String srchStr, boolean returnFirst, int timePct)
	{
		Vector<Interactable> found=new Vector();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		for(;rooms.hasMoreElements();)
		{
			Room room=rooms.nextElement();
			if(room != null)
			{
				found.addAll(room.fetchInhabitants(srchStr));
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
				try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}
	public Vector findInventory(Enumeration rooms, String srchStr, int timePct)
	{ return findInventory(rooms,srchStr,false,timePct);}
	public Item findFirstInventory(Enumeration rooms, String srchStr, int timePct)
	{ 
		Vector found=findInventory(rooms,srchStr,true,timePct);
		if(found.size()>0) return (Item)found.firstElement();
		return null;
	}
	public Vector<Interactable> findInventory(Enumeration<Room> rooms, String srchStr, boolean returnFirst, int timePct)
	{
		Vector<Interactable> found=new Vector();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		boolean useTimer = delay>1;
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
		for(;rooms.hasMoreElements();)
		{
			Room room=rooms.nextElement();
			ItemCollection coll=ItemCollection.O.getFrom(room);
			if(coll != null)
			{
				for(int m=0;m<coll.numItems();m++)
				{
					Item I = coll.getItem(m);
					if((!(I instanceof Body))||((M=((Body)I).mob())==null)) continue;
					found.addAll(M.fetchInventories(srchStr));
				}
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
				try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}

	public Vector findRoomItems(Enumeration rooms, String srchStr, boolean anyItems, int timePct)
	{ return findRoomItems(rooms,srchStr,anyItems,false,timePct);}
	public Item findFirstRoomItem(Enumeration rooms, String srchStr, boolean anyItems, int timePct)
	{ 
		Vector found=findRoomItems(rooms,srchStr,anyItems,true,timePct);
		if(found.size()>0) return (Item)found.firstElement();
		return null;
	}
	public Vector<Interactable> findRoomItems(Enumeration<Room> rooms, String srchStr, boolean anyItems, boolean returnFirst, int timePct)
	{
		Vector<Interactable> found=new Vector();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		for(;rooms.hasMoreElements();)
		{
			Room room=rooms.nextElement();
			if(room != null)
			{
				found.addAll(room.fetchItems(srchStr));
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
				try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}
/*	public Room getRoom(Hashtable hashedRoomSet, String areaName, String calledThis)
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
*/
//	public Room getRoom(String calledThis){ return getRoom(null,calledThis); }
	public Enumeration rooms(){ return new AreaEnumerator(); }
	public Room getRandomRoom()
	{
		Room R=null;
		int numRooms=-1;
		while((R==null)&&((numRooms=numRooms())>0))
		{
			try
			{
				int which=CMath.random(numRooms);
				int total=0;
				for(Enumeration<Area> e=areas();e.hasMoreElements();)
				{
					Area A=e.nextElement();
					if(which<(total+A.properSize()))
					{ R=A.getRandomProperRoom(); break;}
					total+=A.properSize();
				}
			}catch(NoSuchElementException e){}
		}
		return R;
	}

	public Room findConnectingRoom(Room room)
	{
		if(room==null) return null;
		Room R=null;
		Vector otherChoices=new Vector();
		for(int i=room.numExits()-1;i>=0;i--)
		{
			R=room.getExitDestination(i);
			if(R!=null)
				for(int i1=R.numExits()-1;i1>=0;i1--)
					if(R.getExitDestination(i1)==room)
					{
						if(R.getArea()==room.getArea())
							return R;
						otherChoices.add(R);
					}
		}
		for(Enumeration<Room> e=rooms();e.hasMoreElements();)
		{
			R=e.nextElement();
			if(R==room) continue;
			for(int i1=R.numExits()-1;i1>=0;i1--)
				if(R.getExitDestination(i1)==room)
				{
					if(R.getArea()==room.getArea())
						return R;
					otherChoices.add(R);
				}
		}
		if(otherChoices.size()>0)
			return (Room)otherChoices.firstElement();
		return null;
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
			if(M.session()!=null)
				return false;
		}
		return true;
	}
/*
	public boolean explored(Room R, Vector areas)
	{
		if((R==null)
		||(CMath.bset(R.envStats().sensesMask(),EnvStats.SENSE_ROOMUNEXPLORABLE))
		||(R.getArea()==null))
			return false;
		return false;
	}
*/
	public static class AreaEnumerator implements Enumeration
	{
		private Enumeration<Area> curAreaEnumeration=null;
		private Enumeration<Room> curRoomEnumeration=null;
		public AreaEnumerator() {}
		public boolean hasMoreElements()
		{
			if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
			while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
			{
				if(!curAreaEnumeration.hasMoreElements()) return false;
				curRoomEnumeration=curAreaEnumeration.nextElement().getProperMap();
			}
			return curRoomEnumeration.hasMoreElements();
		}
		public Room nextElement()
		{
			if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
			while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
			{
				if(!curAreaEnumeration.hasMoreElements()) return null;
				curRoomEnumeration=curAreaEnumeration.nextElement().getProperMap();
			}
			return curRoomEnumeration.nextElement();
		}
	}

	public void obliterateRoom(Room deadRoom)
	{
		for(int a=deadRoom.numEffects()-1;a>=0;a--)
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
		emptyRoom(deadRoom,null);
		deadRoom.destroy();
//		CMLib.database().DBDeleteRoom(deadRoom);
	}

	public void emptyArea(Area A)
	{
		for(int a=A.numEffects()-1;a>=0;a--)
		{
			Effect A1=A.fetchEffect(a);
			if(A1!=null)
			{
				A1.unInvoke();
				A.delEffect(A1);
			}
		}
		for(Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
		{
			Room R=e.nextElement();
			emptyRoom(R,null);
			R.destroy();
		}
	}
	public Room roomLocation(CMObject E)
	{
		if(E==null||E instanceof Exit)
			return null;
		if(E instanceof Area)
			return ((Area)E).getRandomProperRoom();
		if(E instanceof Room)
			return (Room)E;
		if(E instanceof MOB)
			return ((MOB)E).location();
		if((E instanceof Item)&&(((Item)E).container() != null))
			return roomLocation(((Item)E).container());
		if((E instanceof Effect)&&(((Effect)E).affecting() != null))
			return roomLocation(((Effect)E).affecting());
		if((E instanceof Behavior)&&(((Behavior)E).behaver() != null))
			return roomLocation(((Behavior)E).behaver());
		return null;
	}
/*	public Area getStartArea(Interactable E)
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
*/
	public Area areaLocation(CMObject E)
	{
		if(E==null) return null;
		if(E instanceof Area)
			return (Area)E;
		else
		if(E instanceof Room)
			return ((Room)E).getArea();
		else
		if(E instanceof MOB)
			return ((MOB)E).location().getArea();
		else
		if(E instanceof Item)
			return areaLocation(((Item)E).container());
		return null;
	}

	public void emptyRoom(Room room, Room bringBackHere)
	{
		if(room==null) return;
		Vector inhabs=room.fetchInhabitants("ALL");
		MOB M=null;
		
		for(int m=0;m<inhabs.size();m++)
		{
			M=(MOB)inhabs.elementAt(m);
			if(bringBackHere!=null)
				bringBackHere.bringHere(M.body(), false);
			else
//			if((M.getStartRoom()==null)
//			||(M.getStartRoom()==room)
//			||(M.getStartRoom().ID().length()==0))
				M.destroy();
//			else
//				M.getStartRoom().bringHere(M.body(), false);
		}
		Item I=null;
		inhabs = null;
		
		Vector contents = new Vector();
		
		ItemCollection coll=ItemCollection.O.getFrom(room);
		for(int i=0;i<coll.numItems();i++)
		{
			I=coll.getItem(i);
			if(I!=null) contents.addElement(I);
		}
		for(int i=0;i<contents.size();i++)
		{
			I=(Item)contents.elementAt(i);
			if(bringBackHere!=null)
				bringBackHere.bringHere(I,false);
			else
				I.destroy();
		}
//		CMLib.threads().clearDebri(room,0);
//		room.resetVectors();
	}


	public void obliterateArea(String areaName)
	{
		Area A=getArea(areaName);
		if(A==null) return;
		Vector rooms=new Vector(100);
		Room R=null;
		Enumeration<Room> e=A.getProperMap();
		while(e.hasMoreElements())
		{
			R=e.nextElement();
			if(R!=null)
				obliterateRoom(R);
		}
//		CMLib.database().DBDeleteArea(A);
		CMLib.database().deleteObject(A);
		delArea(A);
	}
//	public CMMsg resetMsg=null;

//TODO: This probably needs to be fixed. Do I need resetMsg?
/*	public void resetRoom(Room room)
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
*/
	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds)
	{
		Vector rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,true,timePct, maxSeconds);
		if((rooms!=null)&&(rooms.size()!=0)) return (Room)rooms.firstElement();
		return null;
	}
	
	public Vector findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,false,timePct,maxSeconds); }
	
	public Room findAreaRoomLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int timePct)
	{
		Vector rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,true,timePct,120);
		if((rooms!=null)&&(rooms.size()!=0)) return (Room)rooms.firstElement();
		return null;
	}
	
	public Vector findAreaRoomsLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int timePct)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,false,timePct,120); }
	
	protected Room addWorldRoomsLiberally(Vector rooms, Vector<? extends Interactable> choicesV)
	{
		if(choicesV==null) return null;
		if(rooms!=null)
		{
			for(Enumeration<? extends Interactable> choices=choicesV.elements();choices.hasMoreElements();)
				addWorldRoomsLiberally(rooms,roomLocation(choices.nextElement()));
			return null;
		}
		else
		{
			Room room=null;
			int tries=0;
			while(((room==null)||(room.saveNum()==0))&&((++tries)<200))
				room=roomLocation(choicesV.elementAt(CMLib.dice().roll(1,choicesV.size(),-1)));
			return room;
		}
	}
	
	protected Room addWorldRoomsLiberally(Vector rooms, Room room)
	{
		if(room==null) return null;
		if(rooms!=null)
		{ 
			if(!rooms.contains(room))
				rooms.addElement(room);
			return null;
		}
		return room;
	}
	
	protected Room addWorldRoomsLiberally(Vector rooms, Area area)
	{ return addWorldRoomsLiberally(rooms,area.getRandomProperRoom()); }
	
	protected Enumeration<Room> rightLiberalMap(Area A) {
		if(A==null) return rooms();
		return A.getProperMap();
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
												   int timePct, 
												   int maxSeconds)
	{
		Room room=null;
		Vector<Room> rooms=(returnFirst)?null:new Vector<Room>();
		
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
//				case 'E': searchWeakAreas=true;   break;
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
				room=addWorldRoomsLiberally(rooms,(Room)SIDLib.Objects.ROOM.get(Integer.parseInt(cmd)));
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
					if((sess!=null) && (sess.mob()!=null))
						room=addWorldRoomsLiberally(rooms,sess.mob().location());
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// search areas strictly
				if(searchStrictAreas && room==null && (A==null))
				{
					A=getArea(srchStr);
					if((A!=null) &&(A.properSize()>0) &&(A.properSize()>0))
						room=addWorldRoomsLiberally(rooms,A.getRandomProperRoom());
					A=null;
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// look for room inhabitants
				if(searchInhabs && room==null)
				{
					Vector<Interactable> candidates=findInhabitants(rightLiberalMap(A), srchStr,returnFirst, timePct);
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// now check room text
				if(searchRooms && room==null)
				{
					Vector<Room> candidates=null;
					if(returnFirst)
					{
						candidates=new Vector();
						Room R=findFirstRoom(rightLiberalMap(A), mob, srchStr, false, timePct);
						if(R!=null) candidates.add(R);
					}
					else
						candidates=findRooms(rightLiberalMap(A), mob, srchStr, false, timePct);
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// check floor items
				if(searchItems && room==null)
				{
					Vector<Interactable> candidates=findRoomItems(rightLiberalMap(A), srchStr, false,returnFirst,timePct);
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// check inventories
				if(searchInventories && room==null)
				{
					Vector<Interactable> candidates=findInventory(rightLiberalMap(A), srchStr, returnFirst,timePct);
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// search areas weakly
/*				if(searchWeakAreas && room==null && (A==null))
				{
					A=findArea(srchStr);
					if((A!=null) &&(A.properSize()>0) &&(A.getProperRoomnumbers().roomCountAllAreas()>0))
						room=addWorldRoomsLiberally(rooms,A);
					A=null;
				}
*/			}
		}
		return returnResponse(rooms,room);
	}
/*
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
*/
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
		globalHandlers.clear();
		thread.shutdown();
		return true;
	}
	
	public void run()
	{
		return;
	}
}