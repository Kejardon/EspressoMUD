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

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface DatabaseEngine extends CMLibrary, Runnable
{
	public void saveObject(CMSavable obj);
	public void deleteObject(CMSavable obj);
	public boolean doneLoading();
/*
No longer used DBInterface(below), switching to DBManager(above)
	public String errorStatus();
	public void resetconnections();
	public DBConnector getConnector();
	// DBABLES, DBCCLASS, DBRACES, DBPLAYERS, DBMAP
	
//	public void DBReadContent(Room thisRoom, Vector rooms);
	public Vector DBReadAreaData(String areaID, boolean reportStatus);
	public Vector DBReadRoomData(String roomID, boolean reportStatus);
//	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus);
	public void DBReadRoomExits(String exitID, boolean reportStatus);
//	public void DBUpdateExits(Room room);
//	public void DBCreateThisItem(String roomID, Item thisItem);
//	public void DBCreateThisMOB(String roomID, MOB thisMOB);
//	public String DBReadRoomMOBData(String roomID, String mobID);
//	public String DBReadRoomDesc(String roomID);
	public void DBReadAllRooms();
//	public void DBUpdateTheseMOBs(Room room, Vector mobs);
//	public void DBUpdateTheseItems(Room room, Vector item);
//	public void DBUpdateMOBs(Room room);
	public void DBCreateRoom(Room room);
	public void DBUpdateRoom(Room room);
//	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus);
	public void DBUpdatePlayer(MOB mob);
	public void DBUpdatePlayerMOBOnly(MOB mob);
	public void DBUpdateAccount(PlayerAccount account);
	public void DBCreateAccount(PlayerAccount account);
	public void DBDeleteAccount(PlayerAccount account);
	public PlayerAccount DBReadAccount(String Login);
	public Vector<PlayerAccount> DBListAccounts(String mask);
//	public void DBUpdateItems(Room room);
	public void DBReCreate(Room room, String oldID);
	public void DBDeleteRoom(Room room);
	public void DBReadPlayer(MOB mob);
//	public void DBUpdatePassword(String name, String password);
	public boolean isConnected();
	public Vector<String> getUserList();
	public void DBDeleteMOB(MOB mob);
	public void DBCreateCharacter(MOB mob);
	public void DBCreateArea(Area A);
	public void DBDeleteArea(Area A);
	public void DBUpdateArea(String keyName,Area A);
	public boolean DBReadUserOnly(MOB mob);
	public String DBReadData(String playerID);
	public Vector DBReadData(Vector sections);
	public void DBUpdateData(String key, String xml);
	public void DBDeleteData(String section);
	public void DBCreateData(String key, String data);
	
	public static class PlayerData
	{
		public String who="";
		public String section="";
		public String key="";
		public String xml="";
	}
*/
}