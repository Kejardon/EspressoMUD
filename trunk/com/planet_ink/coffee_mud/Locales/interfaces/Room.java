package com.planet_ink.coffee_mud.Locales.interfaces;
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

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface Room extends ItemCollection.ItemHolder, Interactable, CMSavable, CMModifiable //ContainableRoom will extend Item.
{
	public static class REMap
	{
		public final Room room;
		public final Exit exit;
		public REMap(Room R, Exit E){room=R; exit=E;}
		public boolean equals(Object O)
		{
			if(O instanceof REMap)
				return (((REMap)O).exit==exit)&&(((REMap)O).room==room);
			return false;
		}
	}
	public enum Domain
	{
		UNDERWATER, AIR, WATERSURFACE,
		WOODS, JUNGLE, SWAMP, PLAINS, DESERT, ROCKS, MOUNTAINS, CITY
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

	public int numExits();
	public void addExit(Exit E, Room destination);
	public void addExit(REMap R);
	public void removeExit(REMap R);
	public void removeExit(Exit E, Room R);
	public Exit getExit(int i);
	public Exit getExit(String target);
	public Room getExitDestination(int i);
	public Room getExitDestination(Exit E);
	public REMap getREMap(int i);
	public REMap getREMap(String target);
	public boolean changeExit(REMap M, Exit newE);
	public boolean changeExit(REMap M, Room newR);
	public boolean changeExit(REMap M, REMap newM);
//	public int getExitIndex(String target);
//	public int getExitIndex(Exit E, Room R);
//	public void initExits();

	public boolean doMessage(CMMsg msg);
	public void send(CMMsg msg);
	public void showHappens(EnumSet<CMMsg.MsgCode> allCode, Object like, String allMessage);
	public boolean show(Object source,
						Interactable target,
						Object tool,
						EnumSet<CMMsg.MsgCode> allCode,
						String allMessage);
	public boolean show(Object source,
						Interactable target,
						Object tool,
						EnumSet<CMMsg.MsgCode> srcCode,
						String srcMessage,
						EnumSet<CMMsg.MsgCode> tarCode,
						String tarMessage,
						EnumSet<CMMsg.MsgCode> othCode,
						String othMessage);

	public void bringHere(Item I, boolean andRiders);

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