package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.http.ProcessHTTPrequest;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
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
public class DBInterface implements DatabaseEngine
{
	public String ID(){return "DBInterface";}
	MOBloader MOBloader=null;
	RoomLoader RoomLoader=null;
	DataLoader DataLoader=null;
	DBConnector DB=null;
	public DBInterface(DBConnector DB)
	{
		this.DB=DB;
		this.MOBloader=new MOBloader(DB);
		this.RoomLoader=new RoomLoader(DB);
		this.DataLoader=new DataLoader(DB);
	}
	public CMObject newInstance(){return new DBInterface(DB);}
	public void initializeClass(){}
	public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public DBConnector getConnector(){ return DB;}
	public boolean activate(){ return true;}
	public boolean shutdown(){ return true;}
	public void propertiesLoaded(){}
	public ThreadEngine.SupportThread getSupportThread() { return null;}
	
	public List<String> getUserList()
	{return MOBloader.getUserList();}

	public boolean isConnected(){return DB.amIOk();}
	
	public void DBUpdateAccount(PlayerAccount account)
	{ MOBloader.DBUpdateAccount(account);}
	
	public void DBCreateAccount(PlayerAccount account)
	{ MOBloader.DBCreateAccount(account);}
	
	public PlayerAccount DBReadAccount(String Login)
	{ return MOBloader.DBReadAccount(Login);}
	
	public Vector<PlayerAccount> DBListAccounts(String mask)
	{ return MOBloader.DBListAccounts(mask);}
	
	public Vector DBReadAreaData(String areaID, boolean reportStatus)
	{return RoomLoader.DBReadAreaData(areaID,reportStatus);}
	
	public Vector DBReadRoomData(String roomID, boolean reportStatus)
	{return RoomLoader.DBReadRoomData(roomID,reportStatus);}
	
	public void DBReadAllRooms()
	{ RoomLoader.DBReadAllRooms();}
	
	public void DBReadRoomExits(String exitID, boolean reportStatus)
	{RoomLoader.DBReadRoomExits(exitID, reportStatus);}
	
	public void DBCreateRoom(Room room)
	{RoomLoader.DBCreate(room);}
	
	public void DBUpdateRoom(Room room)
	{RoomLoader.DBUpdateRoom(room);}
	
	public void DBUpdatePlayer(MOB mob)
	{MOBloader.DBUpdate(mob);}
	
	public void DBUpdatePlayerMOBOnly(MOB mob)
	{MOBloader.DBUpdateJustMOB(mob);}
	
	public void DBReCreate(Room room, String oldID)
	{RoomLoader.DBReCreate(room,oldID);}
	
	public boolean DBReadUserOnly(MOB mob)
	{return MOBloader.DBReadUserOnly(mob);}
	
	public void DBCreateArea(Area A)
	{RoomLoader.DBCreate(A);}
	
	public void DBDeleteArea(Area A)
	{RoomLoader.DBDelete(A);}
	
	public void DBUpdateArea(String keyName, Area A)
	{RoomLoader.DBUpdate(keyName,A);}

	public void DBDeleteRoom(Room room)
	{RoomLoader.DBDelete(room);}
	
	public void DBReadPlayer(MOB mob)
	{MOBloader.DBRead(mob);}
	
	public void DBDeleteMOB(MOB mob)
	{MOBloader.DBDelete(mob);}
	
	public void DBDeleteAccount(PlayerAccount account)
	{ MOBloader.DBDeleteAccount(account);}
	
	public void DBCreateCharacter(MOB mob)
	{MOBloader.DBCreateCharacter(mob);}

	public String DBReadData(String ID)
	{ return DataLoader.DBRead(ID);}
	
	public Vector DBReadData(Vector sections)
	{ return DataLoader.DBRead(sections);}

	public void DBDeleteData(String ID)
	{ DataLoader.DBDelete(ID);}
	
	public void DBUpdateData(String key, String data)
	{ DataLoader.DBUpdate(key,data);}
	
	public void DBCreateData(String key, String data)
	{ DataLoader.DBCreate(key,data);}
	
	public String errorStatus()
	{return DB.errorStatus().toString();}
	
	public void resetconnections()
	{DB.reconnect();}
/*
	public void DBUpdateItems(Room room)
	{RoomLoader.DBUpdateItems(room);}
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{return MOBloader.getExtendedUserList();}
	public PlayerLibrary.ThinPlayer getThinUser(String name)
	{ return MOBloader.getThinUser(name);}
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login)
	{return MOBloader.DBUserSearch(Login);}
	public void DBUpdatePassword(String name, String password)
	{ MOBloader.DBUpdatePassword(name, password);}
	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus)
	{return RoomLoader.DBReadAreaRoomList(areaName,reportStatus);}
	public void DBCreateThisItem(String roomID, Item thisItem)
	{RoomLoader.DBCreateThisItem(roomID,thisItem);}
	public void DBCreateThisMOB(String roomID, MOB thisMOB)
	{RoomLoader.DBCreateThisMOB(roomID,thisMOB);}
	public void DBUpdateExits(Room room)
	{RoomLoader.DBUpdateExits(room);}
	public String DBReadRoomMOBData(String roomID, String mobID)
	{ return RoomLoader.DBReadRoomMOBData(roomID,mobID);}
	public String DBReadRoomDesc(String roomID)
	{ return RoomLoader.DBReadRoomDesc(roomID);}
	public void DBUpdateTheseMOBs(Room room, Vector mobs)
	{RoomLoader.DBUpdateTheseMOBs(room,mobs);}
	public void DBUpdateTheseItems(Room room, Vector items)
	{RoomLoader.DBUpdateTheseItems(room,items);}
	public void DBUpdateMOBs(Room room)
	{RoomLoader.DBUpdateMOBs(room);}
	public void DBReadContent(Room thisRoom, Vector rooms)
	{RoomLoader.DBReadContent((thisRoom!=null)?thisRoom.roomID():null,thisRoom, rooms,null,false);}
	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus)
	{ return RoomLoader.DBReadRoomObject(roomIDtoLoad, reportStatus);}
*/
}
