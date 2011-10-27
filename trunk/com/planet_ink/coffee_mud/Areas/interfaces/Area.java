package com.planet_ink.coffee_mud.Areas.interfaces;
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
   Copyright 2000-2010 Bo Zimmerman, Jeremy Vyska

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
/**
 * An Area is an abstract collection of rooms organized together under a single name
 * in order to share attributes or give some common functionality.  Areas can also
 * include other areas in a parent->child relationship.  
 * 
 * @author Bo Zimmerman, Jeremy Vyska
 *
 */
@SuppressWarnings("unchecked")
public interface Area extends Environmental.EnvHolder, ListenHolder.MsgListener, Affectable, CMModifiable, CMSavable//, Comparable<Area>
{
	public String name();
	public void setName(String newname);
	public TimeClock getTimeObj();
	public void setTimeObj(TimeClock obj);
	public void setAuthorID(String authorID);
	public String getAuthorID();
//	public String getCurrency();
//	public void setCurrency(String currency);
	/* A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 */
//	public int numBlurbFlags();
//	public int numAllBlurbFlags();	//including parents'
//	public String getBlurbFlag(String flag);	//gets the description, not flag
//	public String getBlurbFlag(int which);
//	public void addBlurbFlag(String flagPlusDesc);
//	public void delBlurbFlag(String flagOnly);
	public void addProperRoom(Room R);
	public void delProperRoom(Room R);
	public Room getRoom(String roomID);
	public boolean isRoom(Room R);
	public Room getRandomProperRoom();
	public Enumeration<Room> getProperMap();
//	public void addProperRoomnumber(String roomID);
//	public void delProperRoomnumber(String roomID);
//	public Enumeration<Room> getCompleteMap();
//	public RoomnumberSet getProperRoomnumbers();
//	public void setProperRoomnumbers(RoomnumberSet set);
//	public RoomnumberSet getCachedRoomnumbers();
//	public int numberOfProperIDedRooms();
	public int properSize();
	public Enumeration<Room> getMetroMap();
//	public void addMetroRoom(Room R);
//	public void delMetroRoom(Room R);
//	public void addMetroRoomnumber(String roomID);
//	public void delMetroRoomnumber(String roomID);
	public int metroSize();
	public boolean inMyMetroArea(Area A);
	public Room getRandomMetroRoom();
	public Vector<Room> getMetroCollection();
	public String getNewRoomID();
	public void clearMetroMap();
//	public void setAreaState(int newState);
//	public int getAreaState();
//	public void addSubOp(String username);
//	public void delSubOp(String username);
//	public boolean amISubOp(String username);
//	public String getSubOpList();
//	public void setSubOpList(String list);
//	public Vector getSubOpVectorList();
//	public StringBuffer getAreaStats();
//	public int[] getAreaIStats();
	public void addChildToLoad(String str);
	public void initChildren();
//	public void addParentToLoad(String str);
	public Enumeration<Area> getChildren();
	public String getChildrenList();
	public int getNumChildren();
	public Area getChild(int num);
	public Area getChild(String named);
	public boolean isChild(Area named);
	public boolean isChild(String named);
	public void addChild(Area Adopted);
	public void removeChild(Area Disowned);
	public void removeChild(int Disowned);
	public boolean canChild(Area newChild);
	public Enumeration<Area> getParents();
	public String getParentsList();
	public int getNumParents();
	public Area getParent(int num);
	public Area getParent(String named);
	public Vector<Area> getParentsRecurse();
	public boolean isParent(Area named);
	public boolean isParent(String named);
	public void addParent(Area Adopted);
	public void removeParent(Area Disowned);
	public void removeParent(int Disowned);
	public boolean canParent(Area newParent);
	/**
	 * @author Owner
	 * This class implements the getCompleteMap() method by enumerating through
	 * the complete list of roomIDs for this area and loading any rooms not yet
	 * loaded, all at enumeration-time.
	public class CompleteRoomEnumerator implements Enumeration
	{
		Enumeration roomEnumerator=null;
		Area area=null;
		public CompleteRoomEnumerator(Area myArea){
			area=myArea;
			roomEnumerator=area.getProperRoomnumbers().getRoomIDs();
		}
		public boolean hasMoreElements(){return roomEnumerator.hasMoreElements();}
		public Room nextElement()
		{
			String roomID=(String)roomEnumerator.nextElement();
			if(roomID==null) return null;
			Room R=area.getRoom(roomID);
			if(R==null) return nextElement();
			return CMLib.map().getRoom(R);
		}
	}
	*/
	/**	State flag meaning this area is a THIN type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
//	public final static int FLAG_THIN=1;
	/**	State flag meaning this area is a INSTANCE parent type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
//	public final static int FLAG_INSTANCE_PARENT=2;
	/**	State flag meaning this area is a INSTANCE child type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
//	public final static int FLAG_INSTANCE_CHILD=4;

	/**	State flag for area meaning Area is active.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
//	public final static int STATE_ACTIVE=0;
	/**	State flag for area meaning Area is passive.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
//	public final static int STATE_PASSIVE=1;
	/**	State flag for area meaning Area is frozen.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
//	public final static int STATE_FROZEN=2;
	/**	State flag for area meaning Area is dead.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
//	public final static int STATE_STOPPED=3;
	/**	Amount of time of player absence before an area automatically goes from Active to passive */
//	public final static long TIME_PASSIVE_LAPSE=60*1000*30; // 30 mins
	
}
