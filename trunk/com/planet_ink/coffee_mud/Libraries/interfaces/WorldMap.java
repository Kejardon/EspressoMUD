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
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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
public interface WorldMap extends CMLibrary, Runnable
{
	public String getNewExitID();
	public void removeExit(Exit E);
	public void addExit(Exit E);
	public Exit getExit(String S);
	/************************************************************************/
	/**							 AREAS										*/
	/************************************************************************/
	public int numAreas();
	public void addArea(Area newOne);
	public void delArea(Area oneToDel);
	public Area getArea(String calledThis);
	public Area findAreaStartsWith(String calledThis);
//	public Area findArea(String calledThis);
	public Enumeration<Area> areas();
//	public Enumeration sortedAreas();
	public Area getFirstArea();
	public Area getRandomArea();
	public void obliterateArea(String areaName);
	
	
	/************************************************************************/
	/**							 ROOMS										*/
	/************************************************************************/
	public int numRooms();
//	public Enumeration roomIDs();
//	public Room getRoom(Room room);
	public Room getRoom(String calledThis);
	public Room getRoom(Vector<Room> roomSet, String calledThis);
	public Room getRoom(Hashtable hashedRoomSet, String areaName, String calledThis);
	public Enumeration rooms();
	public Room getRandomRoom();
//	public void renameRooms(Area A, String oldName, Vector allMyDamnRooms);
	public void obliterateRoom(Room deadRoom);
	public Room findConnectingRoom(Room room);
//	public int getRoomDir(Room from, Room to);
	public Vector findWorldRoomsLiberally(MOB M, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds);
	public Room findWorldRoomLiberally(MOB M, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds);
	public Vector findAreaRoomsLiberally(MOB M, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public Room findAreaRoomLiberally(MOB M, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public Vector findRooms(Enumeration rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public Room findFirstRoom(Enumeration rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public MOB findFirstInhabitant(Enumeration rooms, String srchStr, int timePct);
	public Vector findInhabitants(Enumeration rooms, String srchStr, int timePct);
	public Vector findRoomItems(Enumeration rooms, String srchStr, boolean anyItems, int timePct);
	public Item findFirstRoomItem(Enumeration rooms, String srchStr, boolean anyItems, int timePct);
	public Vector findInventory(Enumeration rooms, String srchStr, int timePct);
	public Item findFirstInventory(Enumeration rooms, String srchStr, int timePct);
	
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
