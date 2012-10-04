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

import java.util.*;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")

//Let's see.
//A MOB will have a body, that is almost the main feature of a mob.
//A MOB will have its own inventory for things it's holding, a body won't (although the body's stats and stuff will be used to check if it can be held). A body WILL have equipment however as a sort of inventory.
//A MOB will be affectable independant of its body... but generally not targetable independant of its body (things like scry being the exception). So most interactions will go through the body.
//Body will have to be fairly transparent between the MOB and others.
//A body will be rideable but not really moveable without a MOB.
public interface MOB extends ItemCollection.ItemHolder, Interactable, CMSavable, CMModifiable, TickActer
{
	public static final MOB[] dummyMOBArray=new MOB[0];
	public class QueuedCommand	//Nothing more than a storage object instead of having an Object[] and typecasting stuff
	{
		public long nextAct;
		public Command command;
		public String cmdString;
		public int commandType;
		public Object data;
		public int metaFlags;
	}

	public Body body();
	public void setBody(Body newBody);

	//public EatCode getEat();

	public String[] getTitles();
	public String getActiveTitle();
	public void setActiveTitle(String S);
	public void addTitle(String title);
	public void removeTitle(String title);

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
	public void setBaseCharStats(CharStats newBaseCharStats);

	/** Combat and death */
//	public void removeFromGame(boolean killSession);
	public Interactable getVictim();
	public void setVictim(Interactable mob);

	/** Primary mob communication */
	public void tell(Interactable source, Interactable target, Vector<CMObject> tool, String msg);
	public void tell(Interactable source, Interactable target, CMObject tool, String msg);
	public void tell(Interactable source, Interactable target, String msg);
	public void tell(String msg);
	public void enqueCommand(QueuedCommand qCom, QueuedCommand afterCommand);
	public void enqueCommand(String commands, int metaFlags);
	public int commandQueSize();
	public boolean doCommand(QueuedCommand command);	//currently return is sorta meaningless
	public double actions();
	public void setActions(double remain);

	/** Whether a sessiob object is attached to this MOB */
	public Session session();
	public void setSession(Session newSession);
	public void setTempSession(Session newSession);
	public boolean isMonster();
	//public boolean canReach(Interactable I);

	// Alternate body stuff. I'll figure this out later.
//	public boolean isPossessing();
//	public MOB soulMate();
//	public void setSoulMate(MOB mob);
//	public void dispossess(boolean giveMsg);

	// gained attributes
//	public long getAgeHours();
//	public void setAgeHours(long newVal);

	// misc characteristics
	public boolean willFollowOrdersOf(MOB mob);

	// location!
	public Room location();
	public void setLocation(Room newPlace);
/*
	public Item fetchWornItem(String itemName);
	public Vector fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes);
	public Item fetchFirstWornItem(long wornCode);
	public Item fetchWieldedItem();
*/
}
