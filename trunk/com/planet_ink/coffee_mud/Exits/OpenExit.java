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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class OpenExit implements Exit
{
	//NOTE: myEnvironmental needs to be protected somehow. It should be immutable.
	protected Environmental myEnvironmental=(Environmental)CMClass.Objects.COMMON.getNew("DefaultEnvironmental");
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);

	public OpenExit()
	{
		((Ownable)myEnvironmental).setOwner(this);
	}
	public String ID(){return "OpenExit";}
	public String directLook(MOB mob, Room destination)
	{
		return displayText()+" It leads to "+destination.displayText();
	}
	public String exitListLook(MOB mob, Room destination) { return destination.displayText(); }
	public boolean visibleExit(MOB mob, Room destination){return true;}
	public void setVisible(boolean b){}
	public String exitID(){return "0";}
	public void setExitID(String s){}
	public void initializeClass(){ CMLib.map().addExit(this); }
	public String name(){ return "a wide open passage";}
	public void setName(String newName){}
	public String displayText(){return "a path to another place.";}
	public void setDisplayText(String newDisplayText){}
	public String description(){return "";}
	public void setDescription(String newDescription){}
	public void destroy(){}
	public boolean amDestroyed(){return false;}

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
	public Vector<Effect> fetchEffect(String ID) { return new Vector<Effect>(); }
	public Vector<Effect> allEffects() { return new Vector<Effect>(); }

	//Behavable
	public void addBehavior(Behavior to) { }
	public void delBehavior(Behavior to) { }
	public int numBehaviors() { return 0; }
	public Behavior fetchBehavior(int index) { return null; }
	public Vector<Behavior> fetchBehavior(String ID) { return new Vector<Behavior>(); }
	public Vector<Behavior> allBehaviors(){ return new Vector<Behavior>(); }

	//Affectable/Behavable shared
	public Vector<CharAffecter> charAffecters(){return null;}
	public Vector<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	public Vector<OkChecker> okCheckers(){return null;}
	public Vector<ExcChecker> excCheckers(){return null;}
	public Vector<TickActer> tickActers(){return null;}
	public void removeListener(Listener oldAffect, EnumSet flags) { }
	public void addListener(Listener newAffect, EnumSet flags) { }
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	public Tickable.TickStat getTickStatus(){return Tickable.TickStat.Not;}
	public boolean tick(Tickable ticking, Tickable.TickID tickID) { return false; }
	public long lastAct(){return 0;}	//No Action ticks
	public long lastTick(){return 0;}
	public boolean sameAs(Interactable E) {
		if(!(E instanceof OpenExit)) return false;
		return true;
	}
	public void setSave(boolean b){}
	public boolean needSave(){return false;}
	
	public SaveEnum[] totalEnumS(){return new SaveEnum[0];}
	public Enum[] headerEnumS(){return new Enum[0];}
	public ModEnum[] totalEnumM(){return new ModEnum[0];}
	public Enum[] headerEnumM(){return new Enum[0];}
}
