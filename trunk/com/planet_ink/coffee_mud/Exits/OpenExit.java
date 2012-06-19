package com.planet_ink.coffee_mud.Exits;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class OpenExit implements Exit
{
	//NOTE: myEnvironmental needs to be protected somehow. It should be immutable.
	protected Environmental myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	private static OpenExit myInstance;	//To make sure it doesn't get garbagecollected even when no instances of it are in the MUD.

	public OpenExit() {if(myInstance==null) myInstance=this;}
	public String ID(){return "OpenExit";}
	public String directLook(MOB mob, Room destination) { return displayText()+" It leads to "+destination.displayText(); }
	public String exitListLook(MOB mob, Room destination) { return destination.displayText(); }
	public boolean visibleExit(MOB mob, Room destination){ return true; }
	public int saveNum(){return 1;}
	public void initializeClass(){ SIDLib.EXIT.assignNumber(saveNum(), this); }
	public String name(){ return "a wide open passage";}
	public String plainName(){ return "a wide open passage";}
	public String displayText(){return "a path to another place.";}
	public String plainDisplayText(){return "a path to another place.";}
	public String description(){return "";}
	public String plainDescription(){return "";}
	public void destroy(){}
	public boolean amDestroyed(){return false;}
	public void setVisible(boolean b){}
	public void setName(String newName){}
	public void setDisplayText(String newDisplayText){}
	public void setDescription(String newDescription){}

	public Environmental getEnvObject() {return myEnvironmental;}
	public Closeable getLidObject() {return null;}

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners() { }
	public void clearAllListeners() { }

	public CMObject newInstance() { return this; }
	public CMObject copyOf() { return this; }
	//NOTE: This needs typical Exit stuff when I get to it
	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		return true;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Affectable
	public void addEffect(Effect to) { }
	public void delEffect(Effect to) { }
	public int numEffects(){return 0;}
	public Effect fetchEffect(int index) { return null; }
	public boolean hasEffect(Effect to) { return false; }
	public Vector<Effect> fetchEffect(String ID) { return CMClass.emptyVector; }
	public Iterator<Effect> allEffects() { return Collections.emptyIterator(); }

	//Behavable
	public void addBehavior(Behavior to) { }
	public void delBehavior(Behavior to) { }
	public int numBehaviors() { return 0; }
	public Behavior fetchBehavior(int index) { return null; }
	public Behavior fetchBehavior(String ID) { return null; }
	public boolean hasBehavior(String ID) { return false; }
	public Iterator<Behavior> allBehaviors(){ return Collections.emptyIterator(); }

	//Affectable/Behavable shared
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return null;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return null;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return null;}
	public void removeListener(Listener oldAffect, EnumSet flags) { }
	public void addListener(Listener newAffect, EnumSet flags) { }
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	public Tickable.TickStat getTickStatus(){return Tickable.TickStat.Not;}
	public boolean tick(int tickTo) { return false; }
	public int tickCounter(){return 0;}
	public void tickAct(){}
	//public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return 0;}
	public boolean sameAs(Interactable E) {
		if(!(E instanceof OpenExit)) return false;
		return true;
	}
	public SaveEnum[] totalEnumS(){return CMSavable.dummySEArray;}
	public Enum[] headerEnumS(){return CMClass.dummyEnumArray;}
	public ModEnum[] totalEnumM(){return CMModifiable.dummyMEArray;}
	public Enum[] headerEnumM(){return CMClass.dummyEnumArray;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){}
	public void prepDefault(){}
}
