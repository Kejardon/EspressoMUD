package com.planet_ink.coffee_mud.core.database;
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

import java.sql.*;
import java.util.*;

//import com.planet_ink.coffee_mud.Libraries.CMCatalog.CataDataImpl;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class RoomLoader
{
	protected DBConnector DB=null;
	public RoomLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	private int recordCount=1;
	private int currentRecordPos=1;
	private int updateBreak=1;
	private final static String zeroes="000000000000";

	public Vector<Area> DBReadAreaData(String areaID, boolean reportStatus)
	{
		DBConnection D=null;
		Vector<Area> areas=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Counting Areas");
			ResultSet R=D.query("SELECT * FROM CMAREA"+((areaID==null)?"":" WHERE CMAREA='"+areaID+"'"));
			recordCount=DB.getRecordCount(D,R);
			areas=new Vector<Area>(recordCount);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String areaName=DBConnections.getRes(R,"CMAREA");
				String areaType=DBConnections.getRes(R,"CMTYPE");
				Area A=(Area)CMClass.Objects.AREA.getNew(areaType);
//				if(A==null) A=CMClass.getAreaType("StdArea");
				if(A==null)
				{
					Log.errOut("Could not create area type "+areaType+" for "+areaName);
					continue;
				}
				A.setName(areaName.intern());
				CMLib.coffeeMaker().setPropertiesStr(A, DBConnections.getRes(R,"CMDATA"));
				if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
					CMProps.Strings.MUDSTATUS.setProperty("Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");
				areas.addElement(A);
			}
			DB.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",sqle);
			if(D!=null) DB.DBDone(D);
			return null;
		}
		return areas;
	}

	public Vector<Room> DBReadRoomData(String singleRoomIDtoLoad,
//								 RoomnumberSet roomsToLoad,
								 boolean reportStatus)
//								 Vector unknownAreas)
	{
		Vector<Room> rooms=null;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Counting Rooms");
			ResultSet R=D.query("SELECT * FROM CMROOM"+((singleRoomIDtoLoad==null)?"":" WHERE CMROID='"+singleRoomIDtoLoad+"'"));
			recordCount=DB.getRecordCount(D,R);
			rooms=new Vector<Room>(recordCount);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			String roomID=null;
			while(R.next())
			{
				currentRecordPos=R.getRow();
				roomID=DBConnections.getRes(R,"CMROID");
//				if((roomsToLoad!=null)&&(!roomsToLoad.contains(roomID)))
//					continue;
				String localeID=DBConnections.getRes(R,"CMLOID");
/*				Area myArea=CMLib.map().getArea(areaName);
				if(myArea==null)
				{
					myArea=(Area)CMClass.getAreaType("StdArea").copyOf();
					myArea.setName(areaName);
					if((unknownAreas!=null)
					&&(!unknownAreas.contains(areaName)))
						unknownAreas.addElement(areaName);
				}
				myArea.addProperRoomnumber(roomID);
*/
				Room newRoom=(Room)CMClass.Objects.LOCALE.getNew(localeID);
				if(newRoom==null)
					Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
				else
				{
					CMLib.coffeeMaker().setPropertiesStr(newRoom, DBConnections.getRes(R,"CMDATA"));
//					newRoom.setRoomID(roomID);
//					newRoom.setArea(myArea);
//					newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
//					newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
//					newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
//					addRoom(rooms,newRoom);	//Sorting them isn't necessary
					rooms.add(newRoom);
				}
				if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
					CMProps.Strings.MUDSTATUS.setProperty("Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			rooms=null;
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
		return rooms;
	}

	public Vector<Exit> DBReadRoomExits(String exitID, boolean reportStatus)
	{
		DBConnection D=null;
		Vector<Exit> exits=null;
		// now grab the exits
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Counting Exits");
			ResultSet R=D.query("SELECT * FROM CMEXIT"+((exitID==null)?"":" WHERE CMEXID='"+exitID+"'"));
//			Room thisRoom=null;
//			Room newRoom=null;
			recordCount=DB.getRecordCount(D,R);
			exits=new Vector<Exit>(recordCount);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				exitID=DBConnections.getRes(R,"CMEXID");
				String exitType=DBConnections.getRes(R,"CMTYPE");
				Exit newExit=(Exit)CMClass.Objects.EXIT.getNew(exitType);
				if(newExit==null)
					Log.errOut("Room","Invalid Exit type "+exitType+" for "+exitID);
				else
				{
					newExit.setExitID(exitID.intern());
					CMLib.coffeeMaker().setPropertiesStr(newExit, DBConnections.getRes(R,"CMDATA"));
					CMLib.map().addExit(newExit);
					exits.add(newExit);
				}
				if(reportStatus&&((currentRecordPos%updateBreak)==0))
					CMProps.Strings.MUDSTATUS.setProperty("Booting: Loading Exits ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
		return exits;
	}
	
	public void DBReadAllRooms()
	{
		Vector<Area> areas=null;
//		Vector newAreasToCreate=new Vector();
//		while(CMLib.map().numAreas()>0)CMLib.map().delArea(CMLib.map().getFirstArea());

		areas=DBReadAreaData(null,true);
		if(areas==null) return;
		for(int a=0;a<areas.size();a++)
			CMLib.map().addArea(areas.elementAt(a));
//		areas.clear();	//Tiny bit of processing to save a tiny bit of memory a tiny bit in advance?

		Vector<Room> rooms=DBReadRoomData(null,true);
		
		// handle stray areas
/*		for(Enumeration e=newAreasToCreate.elements();e.hasMoreElements();)
		{
			String areaName=(String)e.nextElement();
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			Area A=CMClass.getAreaType("StdArea");
			A.setName(areaName);
			DBCreate(A);
			CMLib.map().addArea(A);
			for(Enumeration r=rooms.elements();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equals(areaName))
					R.setArea(A);
			}
		}
*/
//		Vector<Exit> exits=
		DBReadRoomExits(null,true);

//		DBReadContent(null,null,rooms,set==null);

		CMProps.Strings.MUDSTATUS.setProperty("Booting: Finalizing room data)");

		//Initialize Areas, link rooms
		for(int a=0;a<areas.size();a++)
			areas.get(a).initChildren();
		for(int a=0;a<rooms.size();a++)
			rooms.get(a).initExits();

//		for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
//			((Area)a.nextElement()).getAreaStats();
	}

	public void DBUpdateRoom(Room room)
	{
		if(room.amDestroyed()) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Start updating room "+room.roomID());
		DB.update(
		"UPDATE CMROOM SET "
		+"CMLOID='"+room.ID()+"',"
		+"CMDATA='"+CMLib.coffeeMaker().getPropertiesStr(room)+"',"
		+"WHERE CMROID='"+room.roomID()+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating room "+room.roomID());
	}
	public void DBCreate(Room room)
	{
		if(room.amDestroyed()) return;
//		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating new room "+room.roomID());
		DB.update(
		"INSERT INTO CMROOM ("
		+"CMROID,"
		+"CMLOID,"
		+"CMDATA"
		+") values ("
		+"'"+room.roomID()+"',"
		+"'"+room.ID()+"',"
		+"'"+CMLib.coffeeMaker().getPropertiesStr(room)+"')");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done creating new room "+room.roomID());
	}
	public void DBReCreate(Room room, String oldID)
	{
		if(room.amDestroyed()) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Recreating room "+room.roomID());
		
		DB.update(
		"UPDATE CMROOM SET "
		+"CMROID='"+room.roomID()+"', "
		+"CMLOID='"+room.ID()+"',"
		+"CMDATA='"+CMLib.coffeeMaker().getPropertiesStr(room)+"',"
		+"WHERE CMROID='"+oldID+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done recreating room "+room.roomID());
	}
	public void DBDelete(Room room)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Destroying room "+room.roomID());
		room.destroy();
		DB.update("DELETE FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
		room.destroy();
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done gestroying room "+room.roomID());
	}

	public void DBUpdate(String keyName,Area A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Updating area "+A.name());
//		boolean ignoreType=CMSecurity.isDisabled("FATAREAS")||CMSecurity.isDisabled("THINAREAS");
		DB.update(
		"UPDATE CMAREA SET "
		+"CMAREA='"+A.name()+"',"
		+"CMTYPE='"+A.ID()+"',"
		+"CMDATA='"+CMLib.coffeeMaker().getPropertiesStr(A)+"',"
		+"WHERE CMAREA='"+keyName+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating area "+A.name());
	}
	public void DBCreate(Area A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating area "+A.name());
		if((A==null)||(A.name().length()==0)) {
			Log.errOut("RoomLoader","Unable to create area "+((A!=null)?A.name():"null"));
			return;
		}
		//CMLib.map().addArea(A); // not sure why I ever toyed with this idea, but apparantly I did.
		DB.update(
		"INSERT INTO CMAREA ("
		+"CMAREA,"
		+"CMTYPE,"
		+"CMDATA"
		+") values ("
		+"'"+A.name()+"',"
		+"'"+A.ID()+"',"
		+"'"+CMLib.coffeeMaker().getPropertiesStr(A)+"')");
//		A.setAreaState(Area.STATE_ACTIVE);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done creating area "+A.name());
	}
	public void DBDelete(Area A)
	{
		if(A==null) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Destroying area "+A.name());
//		A.setAreaState(Area.STATE_STOPPED);
		DB.update("DELETE FROM CMAREA WHERE CMAREA='"+A.name()+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done destroying area "+A.name()+".");
	}

	public void DBUpdate(String keyName, Exit A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMEXIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Updating exit "+A.exitID());
//		boolean ignoreType=CMSecurity.isDisabled("FATAREAS")||CMSecurity.isDisabled("THINAREAS");
		DB.update(
		"UPDATE CMEXIT SET "
		+"CMEXID='"+A.exitID()+"',"
		+"CMTYPE='"+A.ID()+"',"
		+"CMDATA='"+CMLib.coffeeMaker().getPropertiesStr(A)+"',"
		+"WHERE CMAREA='"+keyName+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMEXIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating area "+A.exitID());
	}
	public void DBCreate(Exit A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMEXIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating exit "+A.exitID());
		if((A==null)||(A.exitID().length()==0)) {
			Log.errOut("RoomLoader","Unable to create exit "+((A!=null)?A.exitID():"null"));
			return;
		}
		//CMLib.map().addArea(A); // not sure why I ever toyed with this idea, but apparantly I did.
		DB.update(
		"INSERT INTO CMEXIT ("
		+"CMEXID,"
		+"CMTYPE,"
		+"CMDATA,"
		+") values ("
		+"'"+A.exitID()+"',"
		+"'"+A.ID()+"',"
		+"'"+CMLib.coffeeMaker().getPropertiesStr(A)+"')");
//		A.setAreaState(Area.STATE_ACTIVE);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMEXIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done creating exit "+A.name());
	}
	public void DBDelete(Exit A)
	{
		if(A==null) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMEXIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Destroying exit "+A.exitID());
		DB.update("DELETE FROM CMEXIT WHERE CMEXID='"+A.exitID()+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMEXIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done destroying exit "+A.exitID()+".");
	}

/*
	public String DBReadRoomDesc(String roomID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomID+"'");
			if(R.next())
			{
				String txt=DBConnections.getRes(R,"CMDESC2");
				DB.DBDone(D);
				return txt;
			}
			DB.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DB.DBDone(D);
		}
		return null;
	}
	public String DBReadRoomMOBData(String roomID, String mobID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+roomID+"'");
			while(R.next())
			{
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				if(NUMID.equalsIgnoreCase(mobID))
				{
					String txt=DBConnections.getRes(R,"CMCHTX");
					DB.DBDone(D);
					return txt;
				}
			}
			DB.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DB.DBDone(D);
		}
		return null;
	}
	private void fixItemKeys(Hashtable itemLocs, Hashtable itemNums)
	{
		for(Enumeration e=itemLocs.keys();e.hasMoreElements();)
		{
			Item keyItem=(Item)e.nextElement();
			String location=(String)itemLocs.get(keyItem);
			Environmental container=(Environmental)itemNums.get(location);
			if((container instanceof Container)&&(((Container)container).capacity()>0))
				keyItem.setContainer((Container)container);
			else
			if(container instanceof Item)
				keyItem.setContainer((Item)container);
		}
	}
	private void fixContentContainers(Hashtable content, Hashtable stuff, String roomID, Room room, boolean debug)
	{
		String lastName=null;
		Hashtable itemLocs=null;
		if(room != null)
			for(Enumeration i=content.elements();i.hasMoreElements();)
			{
				Environmental E=(Environmental)i.nextElement();
				if((debug)&&((lastName==null)||(!lastName.equals(E.Name()))))
				{lastName=E.Name(); Log.debugOut("RoomLoader","Loading object(s): "+E.Name());}
				if(E instanceof Item)
					room.getItemCollection().addItem((Item)E);
				else
				{
					((MOB)E).setStartRoom(room);
					((MOB)E).bringToLife(room,true);
				}
			}
		itemLocs=(Hashtable)stuff.get("LOCSFOR"+roomID.toUpperCase());
		if(itemLocs!=null)
		{
			fixItemKeys(itemLocs,content);
			if(room!=null)
			{
				room.recoverRoomStats();
				room.recoverRoomStats();
			}
		}
	}
	public void DBReadContent(String thisRoomID, Room thisRoom, Vector rooms, boolean setStatus)
	{
		boolean debug=Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMPOP"));
		if(debug||(Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMS"))))
			Log.debugOut("RoomLoader","Reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));
		
		Hashtable stuff=new Hashtable();
		Hashtable itemNums=null;
		Hashtable cataData=null;
		Hashtable itemLocs=null;
		

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			if(setStatus)
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Counting Items");
			ResultSet R=D.query("SELECT * FROM CMROIT"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus) recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				itemNums=(Hashtable)stuff.get("NUMSFOR"+roomID.toUpperCase());
				if(itemNums==null)
				{
					itemNums=new Hashtable();
					stuff.put("NUMSFOR"+roomID.toUpperCase(),itemNums);
				}
				itemLocs=(Hashtable)stuff.get("LOCSFOR"+roomID.toUpperCase());
				if(itemLocs==null)
				{
					itemLocs=new Hashtable();
					stuff.put("LOCSFOR"+roomID.toUpperCase(),itemLocs);
				}
				String itemNum=DBConnections.getRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"'");
				else
				{
					itemNums.put(itemNum,newItem);
					String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						Item container=(Item)itemNums.get(loc);
						if(container!=null)
							newItem.setContainer(container);
						else
							itemLocs.put(newItem,loc);
					}
					try {
						newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
						newItem.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
						newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
						newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
						newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
						newItem.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
						newItem.recoverEnvStats();
					} catch(Exception e) { Log.errOut("RoomLoader",e); itemNums.remove(itemNum);}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.Strings.MUDSTATUS.setProperty("Booting: Loading Items ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}

		// now grab the inhabitants
		try
		{
			D=DB.DBFetch();
			if(setStatus)
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Counting MOBS");
			ResultSet R=D.query("SELECT * FROM CMROCH"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus) recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				String MOBID=DBConnections.getRes(R,"CMCHID");

				itemNums=(Hashtable)stuff.get("NUMSFOR"+roomID.toUpperCase());
				if(itemNums==null)
				{
					itemNums=new Hashtable();
					stuff.put("NUMSFOR"+roomID.toUpperCase(),itemNums);
				}

				MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"'");
				else
				{
					itemNums.put(NUMID,newMOB);
					if(thisRoom!=null)
					{
						newMOB.setStartRoom(thisRoom);
						newMOB.setLocation(thisRoom);
					}
					try {
						newMOB.setMiscText(DBConnections.getResQuietly(R,"CMCHTX"));
						newMOB.baseEnvStats().setLevel(((int)DBConnections.getLongRes(R,"CMCHLV")));
						newMOB.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMCHAB"));
						newMOB.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMCHRE"));
						newMOB.recoverCharStats();
						newMOB.recoverEnvStats();
						newMOB.resetToMaxState();
					} catch(Exception e) { Log.errOut("RoomLoader",e); itemNums.remove(NUMID);}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.Strings.MUDSTATUS.setProperty("Booting: Loading MOBs ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
		if(thisRoom!=null)
		{
			rooms=new Vector();
			rooms.addElement(thisRoom);
		}
		if(rooms!=null) recordCount=rooms.size();
		updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
		currentRecordPos=0;

		// now load the rooms
		if(rooms!=null)
		for(Enumeration e=rooms.elements();e.hasMoreElements();)
		{
			if((((++currentRecordPos)%updateBreak)==0)&&(setStatus))
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Populating Rooms ("+(currentRecordPos)+" of "+recordCount+")");
			Room room=(Room)e.nextElement();
			if(debug) Log.debugOut("RoomLoader","Populating room: "+room.roomID());
			itemNums=(Hashtable)stuff.get("NUMSFOR"+room.roomID().toUpperCase());
			if(itemNums!=null)
				fixContentContainers(itemNums,stuff,room.roomID(),room,debug);
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));
	}
	private Vector DBGetContents(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return new Vector();
		Vector contents=new Vector();
		ItemCollection stuff=room.getItemCollection();
		for(int i=0;i<stuff.numItems();i++)
		{
			Item thisItem=stuff.getItem(i);
			if((thisItem!=null)&&(!contents.contains(thisItem))&&thisItem.savable())
				contents.addElement(thisItem);
		}
		return contents;
	}
	protected String getDBCreateItemString(String roomID, Item thisItem)
	{
		Environmental container=thisItem.container();
		String itemID=""+thisItem;
		String text=thisItem.text();
		return
		"INSERT INTO CMROIT ("
		+"CMROID, "
		+"CMITNM, "
		+"CMITID, "
		+"CMITLO, "
		+"CMITTX, "
		+"CMITRE, "
		+"CMITUR, "
		+"CMITLV, "
		+"CMITAB, "
		+"CMHEIT"
		+") values ("
		+"'"+roomID+"',"
		+"'"+itemID+"',"
		+"'"+thisItem.ID()+"',"
		+"'"+((container!=null)?(""+container):"")+"',"
		+"'"+text+" ',"
		+thisItem.baseEnvStats().rejuv()+","
		+thisItem.usesRemaining()+","
		+thisItem.baseEnvStats().level()+","
		+thisItem.baseEnvStats().ability()+","
		+thisItem.baseEnvStats().height()+")";
	}
	public void DBCreateThisItem(String roomID, Item thisItem)
	{
		DB.update(getDBCreateItemString(roomID,thisItem));
	}
	public void DBUpdateTheseItems(Room room, Vector items)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Start item update for room "+room.roomID());
		Vector statements=new Vector();
		statements.addElement("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'");
		for(int i=0;i<items.size();i++)
		{
			Item thisItem=(Item)items.elementAt(i);
			statements.addElement(getDBCreateItemString(room.roomID(),thisItem));
		}
		DB.update(CMParms.toStringArray(statements));
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Finished items update for room "+room.roomID());
	}
	public void DBUpdateItems(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		DBUpdateTheseItems(room,DBGetContents(room));
	}
	public void DBCreateThisMOB(String roomID, MOB thisMOB)
	{
		DB.update(getDBCreateMOBString(roomID,thisMOB));
	}
	public String getDBCreateMOBString(String roomID, MOB thisMOB)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating mob "+thisMOB.name()+" for room "+roomID);
		
		String mobID=""+thisMOB;
		
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Created mob "+thisMOB.name()+" for room "+roomID);
		
		return
		"INSERT INTO CMROCH ("
		+"CMROID, "
		+"CMCHNM, "
		+"CMCHID, "
		+"CMCHTX, "
		+"CMCHLV, "
		+"CMCHAB, "
		+"CMCHRE "
		+") values ("
		+"'"+roomID+"',"
		+"'"+mobID+"',"
		+"'"+CMClass.classID(thisMOB)+"',"
		+"'"+thisMOB.text()+" ',"
		+thisMOB.baseEnvStats().level()+","
		+thisMOB.baseEnvStats().ability()+","
		+thisMOB.baseEnvStats().rejuv()
		+")";
	}
	public void DBUpdateTheseMOBs(Room room, Vector mobs)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Updating mobs for room "+room.roomID());
		if(mobs==null) mobs=new Vector();
		Vector statements=new Vector();
		statements.addElement("DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'");
		for(int m=0;m<mobs.size();m++)
		{
			MOB thisMOB=(MOB)mobs.elementAt(m);
			statements.addElement(getDBCreateMOBString(room.roomID(),thisMOB));
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating mobs for room "+room.roomID());
		DB.update(CMParms.toStringArray(statements));
	}
	public void DBUpdateMOBs(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		Vector mobs=new Vector();
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB thisMOB=room.fetchInhabitant(m);
			if((thisMOB!=null)&&(thisMOB.savable()))
				mobs.addElement(thisMOB);
		}
		DBUpdateTheseMOBs(room,mobs);
	}
	protected void addRoom(Vector rooms, Room R)
	{
		try {
			String roomID=R.roomID();
			int start=0;
			int end=rooms.size()-1;
			int lastStart=0;
			int lastEnd=rooms.size()-1;
			int comp=-1;
			int mid=-1;
			while(start<=end)
			{
				mid=(end+start)/2;
				comp=((Room)rooms.elementAt(mid)).roomID().compareToIgnoreCase(roomID);
				if(comp==0)
					break;
				else
				if(comp>0)
				{
					lastEnd=end;
					end=mid-1;
				}
				else
				{
					lastStart=start;
					start=mid+1;
				}
			}
			if(comp==0)
				rooms.setElementAt(R,mid);
			else
			{
				if(mid>=0)
					for(comp=lastStart;comp<=lastEnd;comp++)
						if(((Room)rooms.elementAt(comp)).roomID().compareToIgnoreCase(roomID)>0)
						{
							rooms.insertElementAt(R,comp);
							return;
						}
				rooms.addElement(R);
			}
		}
		catch(Throwable t){ t.printStackTrace();}
	}
	public Room getRoom(Vector rooms, String roomID)
	{
		if(rooms.size()==0) return null;
		int start=0;
		int end=rooms.size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=((Room)rooms.elementAt(mid)).roomID().compareToIgnoreCase(roomID);
			if(comp==0)
				return (Room)rooms.elementAt(mid);
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;

		}
		return null;
	}
	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus)
	{
		RoomnumberSet roomSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Fetching roomnums for "+areaName);
			ResultSet R=D.query("SELECT * FROM CMROOM"+((areaName==null)?"":" WHERE CMAREA='"+areaName+"'"));
			while(R.next())
				roomSet.add(DBConnections.getRes(R,"CMROID"));
			DB.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("RoomSet",sqle);
			if(D!=null) DB.DBDone(D);
			return null;
		}
		return roomSet;
	}
	public Vector DBReadRoomData(String singleRoomIDtoLoad, boolean reportStatus)
	{ 
		return DBReadRoomData(singleRoomIDtoLoad,null,reportStatus,null);
	}
	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if(reportStatus)
				CMProps.Strings.MUDSTATUS.setProperty("Booting: Counting Rooms");
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomIDtoLoad+"'");
			recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			String roomID=null;
			if(R.next())
			{
				currentRecordPos=R.getRow();
				roomID=DBConnections.getRes(R,"CMROID");
				String localeID=DBConnections.getRes(R,"CMLOID");
				//String areaName=DBConnections.getRes(R,"CMAREA");
				Room newRoom=CMClass.getLocale(localeID);
				if(newRoom==null)
					Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
				else
				{
					newRoom.setRoomID(roomID);
					newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
					newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
					newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
				}
				if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
					CMProps.Strings.MUDSTATUS.setProperty("Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
				return newRoom;
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
		return null;
	}
	public void DBUpdateAll(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		DBUpdateRoom(room);
//		DBUpdateMOBs(room);
//		DBUpdateExits(room);
//		DBUpdateItems(room);
	}
	public void DBUpdateExits(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROEX")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Starting exit update for room "+room.roomID());
		Vector statements=new Vector();
		statements.addElement("DELETE FROM CMROEX WHERE CMROID='"+room.roomID()+"'");
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit thisExit=room.getRawExit(d);
			Room thisRoom=room.rawDoors()[d];
			if(((thisRoom!=null)||(thisExit!=null))
			   &&((thisRoom==null)||(thisRoom.savable())))
			{
				statements.addElement(
				"INSERT INTO CMROEX ("
				+"CMROID, "
				+"CMDIRE, "
				+"CMEXID, "
				+"CMEXTX, "
				+"CMNRID"
				+") values ("
				+"'"+room.roomID()+"',"
				+d+","
				+"'"+((thisExit==null)?" ":thisExit.ID())+"',"
				+"'"+((thisExit==null)?" ":thisExit.text())+" ',"
				+"'"+((thisRoom==null)?" ":thisRoom.roomID())+"')");
			}
		}
		DB.update(CMParms.toStringArray(statements));
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROEX")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Finished exit update for room "+room.roomID());
	}
*/
}
