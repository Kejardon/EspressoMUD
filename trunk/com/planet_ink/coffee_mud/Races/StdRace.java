package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//Defaults for typical carbon-based lifeform (mainly recoverTick)
@SuppressWarnings("unchecked")
public abstract class StdRace implements Race
{
	public static final EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	public Gender[] myGenders;

	//public String ID(){	return "StdRace"; }
	//public String name(){ return "StdRace"; }
	//public String racialCategory(){return "Unknown";}

	//public HashMap<String, Body.BodyPart> bodyMap(){return null;}

	public CMObject newInstance(){return this;}
	public void initializeClass(){}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { }
	public void registerAllListeners(){}
	public void clearAllListeners(){}

	//protected static final Vector empty=new Vector();
	//protected String baseStatChgDesc = "";
//	protected String sensesChgDesc = null;
//	protected String dispChgDesc = null;
//	protected String abilitiesDesc = null;
	//protected String languagesDesc = "";

	//public StdRace() {}

	public int availabilityCode(){return -1;}

	public Gender[] possibleGenders(){return myGenders;}

	public int fertile(String S){return -100;}

	public CMObject copyOf()
	{
		return this;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
	}
	public void affectCharStats(MOB affectedMob, CharStats charStats)
	{
	}
	public boolean okMessage(ListenHolder.OkChecker myBody, CMMsg msg)
	{
		return true;
	}
	public boolean respondTo(CMMsg msg, Object data){return true;}
	//This really isn't supported! Don't call this respondTo
	public boolean respondTo(CMMsg msg){return true;}
	public boolean respondTo(Body myBody, CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myBody, CMMsg msg)
	{
	}

	public void recoverTick(Body body, CharStats stats)
	{
	}
	public long sendEat(MOB mob, Body body, Vector<Interactable> items)
	{
		return -1;
	}
	public boolean handleEat(CMMsg msg)
	{
		return false;
	}
	public ArrayList<MOB.QueuedCommand> eatPrereqs(MOB mob, Body body, Vector<Interactable> items) //Vector<Item> failed
	{
		mob.tell("You don't need to eat.");
		return null;
	}
	public boolean satisfiesEatReqs(CMMsg msg)
	{
		Interactable source=msg.firstSource();
		if(source instanceof MOB)
			((MOB)source).tell("You don't need to eat.");
		return false;
	}
	public boolean satisfiesEatPrereqs(CMMsg msg)
	{
		Interactable source=msg.firstSource();
		if(source instanceof MOB)
			((MOB)source).tell("You don't need to eat.");
		return false;
	}
	public int diet(Body body, RawMaterial.Resource material)
	{
		return 0;
	}
	public void applyDiet(Body body, Item source, int volume)
	{
	}
	public int getBiteSize(Body body, Item source)
	{
		return 0;
	}
	public int getMaxBiteSize(Body body)
	{
		return 0;
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	/*
	public String getStatAdjDesc()
	{
		return baseStatChgDesc;
	}
	public String getLanguagesDesc()
	{
		return languagesDesc;
	}
	*/

	public boolean sameAs(Race E)
	{
		if(!(E instanceof StdRace)) return false;
		return true;
	}
}
