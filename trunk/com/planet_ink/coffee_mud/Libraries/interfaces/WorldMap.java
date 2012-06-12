package com.planet_ink.coffee_mud.Libraries.interfaces;
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

import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface WorldMap extends CMLibrary, Runnable
{
//	public String getNewExitID();
//	public void removeExit(Exit E);
//	public void addExit(Exit E);
//	public Exit getExit(String S);
	/************************************************************************/
	/**							 AREAS										*/
	/************************************************************************/
	public int numAreas();
	public void addArea(Area newOne);
	public void delArea(Area oneToDel);
	public Area getArea(String calledThis);
	public Area findAreaStartsWith(String calledThis);
//	public Area findArea(String calledThis);
	public Iterator<Area> areas();
//	public Enumeration sortedAreas();
	public Area getFirstArea();
	public Area getRandomArea();
	public void obliterateArea(String areaName);
	public void finishObliterateArea(Area A, Room[] rooms);
	
	/************************************************************************/
	/**							 ROOMS										*/
	/************************************************************************/
	public int numRooms();
//	public Enumeration roomIDs();
//	public Room getRoom(Room room);
//	public Room getRoom(String calledThis);
//	public Room getRoom(Vector<Room> roomSet, String calledThis);
//	public Room getRoom(Hashtable hashedRoomSet, String areaName, String calledThis);
	public Iterator<Room> rooms();
	public Room getRandomRoom();
//	public void renameRooms(Area A, String oldName, Vector allMyDamnRooms);
	public void obliterateRoom(Room deadRoom);
	public Room findConnectingRoom(Room room);
//	public int getRoomDir(Room from, Room to);
	public Vector findWorldRoomsLiberally(MOB M, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds);
	public Room findWorldRoomLiberally(MOB M, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds);
	public Vector findAreaRoomsLiberally(MOB M, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public Room findAreaRoomLiberally(MOB M, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public Vector findRooms(Iterator<Room> rooms, String srchStr, boolean displayOnly, int timePct);
	public Room findFirstRoom(Iterator<Room> rooms, String srchStr, boolean displayOnly, int timePct);
	public MOB findFirstInhabitant(Iterator<Room> rooms, String srchStr, int timePct);
	public Vector findInhabitants(Iterator<Room> rooms, String srchStr, int timePct);
	public Vector findRoomItems(Iterator<Room> rooms, String srchStr, int timePct);
	public Item findFirstRoomItem(Iterator<Room> rooms, String srchStr, int timePct);
	public Vector findInventory(Iterator<Room> rooms, String srchStr, int timePct);
	public Item findFirstInventory(Iterator<Room> rooms, String srchStr, int timePct);
	
	/************************************************************************/
	/**							 ROOM-AREA-UTILITIES						*/
	/************************************************************************/
//	public void resetArea(Area area);
//	public void resetRoom(Room room);
//	public void resetRoom(Room room, boolean rebuildGrids);
//	public Room getStartRoom(Interactable E);
//	public Area getStartArea(Interactable E);
	public Room roomLocation(CMObject E);
	public void emptyRoom(Room room, Room bringBackHere);
	public void emptyArea(Area A);
//	public boolean hasASky(Room room);
	public boolean isClearableRoom(Room room);
//	public String createNewExit(Room from, Room room, int direction);
	public Area areaLocation(CMObject E);
//	public boolean explored(Room R, Vector areas);
	
	/************************************************************************/
	/**							 MESSAGES	 								*/
	/************************************************************************/
	public enum Global
	{
		
	}
	public void addGlobalHandler(ListenHolder.MsgListener E, CMMsg.MsgCode category);
	public void delGlobalHandler(ListenHolder.MsgListener E, CMMsg.MsgCode category);
//	public MOB mobCreated();
//	public MOB mobCreated(Room R);
	public boolean sendGlobalMessage(ListenHolder.MsgListener host, EnumSet<CMMsg.MsgCode> category, CMMsg msg);
	
	public static class CrossExit
	{
		public int x;
		public int y;
		public int dir;
		public String destRoomID="";
		public boolean out=false;
		public static CrossExit make(int xx, int xy, int xdir, String xdestRoomID, boolean xout)
		{   CrossExit EX=new CrossExit();
			EX.x=xx;EX.y=xy;EX.dir=xdir;EX.destRoomID=xdestRoomID;EX.out=xout;
			return EX;
		}
	}
	public final static long ROOM_EXPIRATION_MILLIS=2500000;
}
