package com.planet_ink.coffee_mud.MOBS.interfaces;
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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;


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

//Let's see.
//A MOB will have a body, that is almost the main feature of a mob.
//A MOB will have its own inventory for things it's holding, a body won't (although the body's stats and stuff will be used to check if it can be held). A body WILL have equipment however as a sort of inventory.
//A MOB will be affectable independant of its body... but generally not targetable independant of its body (things like scry being the exception). So most interactions will go through the body.
//Body will have to be fairly transparent between the MOB and others.
//A body will be rideable but not really moveable without a MOB.
public interface MOB extends ItemCollection.ItemHolder, Interactable, CMSavable, CMModifiable
{
	public Body body();
	public void setBody(Body newBody);

	public Vector getTitles();
	public String getActiveTitle();

	public String titledName();
	public String displayName(MOB mob);
	public String genericName();
//	public String displayText(MOB viewer);

	public Item fetchInventory(String itemName);
//	public Item fetchCarried(Item goodLocation, String itemName);
	public Vector fetchInventories(String itemName);
	public boolean isMine(Interactable env);
	public void giveItem(Item thisContainer);

	/** Some general statistics about MOBs.  See the
	 * CharStats class (in interfaces) for more info. */
	public PlayerStats playerStats();
	public void setPlayerStats(PlayerStats newStats);
	public CharStats baseCharStats();
	public CharStats charStats();
	public void recoverCharStats();
//	public void resetToMaxState();
	public void setBaseCharStats(CharStats newBaseCharStats);
//	public void resetVectors();

	/** Combat and death */
//	public void removeFromGame(boolean killSession);
	public Interactable getVictim();
	public void setVictim(Interactable mob);

	/** Primary mob communication */
	public void tell(Interactable source, Interactable target, Vector<CMObject> tool, String msg);
	public void tell(String msg);
	public void enqueCommand(Vector commands, int metaFlags, double tickDelay);
	public void prequeCommand(Vector commands, int metaFlags, double tickDelay);
	public boolean dequeCommand();
	public int commandQueSize();
	public void doCommand(Vector commands, int metaFlags);
	public double actions();
	public void setActions(double remain);

	/** Whether a sessiob object is attached to this MOB */
	public Session session();
	public void setSession(Session newSession);
	public boolean isMonster();

	// Alternate body stuff. I'll figure this out later.
//	public boolean isPossessing();
//	public MOB soulMate();
//	public void setSoulMate(MOB mob);
//	public void dispossess(boolean giveMsg);

	// gained attributes
//	public long getAgeHours();
//	public void setAgeHours(long newVal);

	// the core state values
//	public Weapon myNaturalWeapon();	//will be in body if anything

	// misc characteristics
/*	public String getLiegeID();
	public boolean isMarriedToLiege();
	public void setLiegeID(String newVal);
	public int getWimpHitPoint();
	public void setWimpHitPoint(int newVal);
	public long lastTickedDateTime(); */
	public boolean willFollowOrdersOf(MOB mob);

	// location!
//	public Room getStartRoom();
//	public void setStartRoom(Room newRoom);
	public Room location();
	public void setLocation(ItemCollection newPlace);
/*
	public Item fetchWornItem(String itemName);
	public Vector fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes);
	public Item fetchFirstWornItem(long wornCode);
	public Item fetchWieldedItem();
*/
}