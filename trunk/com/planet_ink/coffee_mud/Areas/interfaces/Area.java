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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * An Area is an abstract collection of rooms organized together under a single name
 * in order to share attributes or give some common functionality.  Areas can also
 * include other areas in a parent->child relationship.  
 * @author Bo Zimmerman, Jeremy Vyska
 */
@SuppressWarnings("unchecked")
public interface Area extends Environmental.EnvHolder, ListenHolder.MsgListener, Affectable, CMModifiable, CMSavable//, Comparable<Area>
{
	public static Area[] dummyAreaArray=new Area[0];
	public String name();
	public void setName(String newname);
	public TimeClock getTimeObj();
	public void setTimeObj(TimeClock obj);
	public void setAuthorID(String authorID);
	public String getAuthorID();
	public void addProperRoom(Room R);
	public void delProperRoom(Room R);
	public void addTickingRoom(Room R);
	public void removeTickingRoom(Room R);
	public boolean isRoom(Room R);
	public Room getRandomProperRoom();
	public Room[] getProperMap();
	public int properSize();
	//public Enumeration<Room> getMetroMap();
	public int metroSize();
	public boolean inMyMetroArea(Area A);
	public Room getRandomMetroRoom();
	public Room[] getMetroCollection();
	public void sendMessageEverywhere(CMMsg msg);
	public void showMessageEverywhere(Interactable source, Interactable target, CMObject tool, String message);
	public void showMessageEverywhere(Interactable source, Interactable target, CMObject tool, String srcMessage, String tarMessage, String othMessage);
	public void clearMetroMap();
	public Iterator<Area> getChildren();
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
	public Iterator<Area> getParents();
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

	/* A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 */
//	public int numBlurbFlags();
//	public int numAllBlurbFlags();	//including parents'
//	public String getBlurbFlag(String flag);	//gets the description, not flag
//	public String getBlurbFlag(int which);
//	public void addBlurbFlag(String flagPlusDesc);
//	public void delBlurbFlag(String flagOnly);
	
}
