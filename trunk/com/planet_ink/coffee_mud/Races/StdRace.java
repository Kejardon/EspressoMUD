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

public abstract class StdRace implements Race
{
	public static final EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	public Gender[] myGenders;

	//public String ID(){	return "StdRace"; }
	//public String name(){ return "StdRace"; }
	//public String racialCategory(){return "Unknown";}

	//public HashMap<String, Body.BodyPart> bodyMap(){return null;}

	@Override public StdRace newInstance(){return this;}
	@Override public void initializeClass(){}
	@Override public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	@Override public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	@Override public void registerListeners(ListenHolder here) { }
	@Override public void registerAllListeners(){}
	@Override public void clearAllListeners(){}

	//protected static final Vector empty=new Vector();
	//protected String baseStatChgDesc = "";
//	protected String sensesChgDesc = null;
//	protected String dispChgDesc = null;
//	protected String abilitiesDesc = null;
	//protected String languagesDesc = "";

	//public StdRace() {}

	@Override public int availabilityCode(){return -1;}

	@Override public Gender[] possibleGenders(){return myGenders;}

	public int fertile(String S){return -100;}

	@Override public CMObject copyOf()
	{
		return this;
	}

	@Override public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
	}
	@Override public void affectCharStats(MOB affectedMob, CharStats charStats)
	{
	}
	@Override public boolean okMessage(ListenHolder.OkChecker myBody, CMMsg msg)
	{
		return true;
	}
	@Override public boolean respondTo(CMMsg msg, Object data){return true;}
	//This really isn't supported! Don't call this respondTo
	@Override public boolean respondTo(CMMsg msg){return true;}
	public boolean respondTo(Body myBody, CMMsg msg){return true;}
	@Override public void executeMsg(ListenHolder.ExcChecker myBody, CMMsg msg)
	{
	}

	@Override public void recoverTick(Body body, CharStats stats)
	{
	}
	
	@Override public ActionCode getAction(ActionCode.Type T)
	{
		switch(T)
		{
			case GET: return defaultGetCode;
			case GIVE: return defaultGiveCode;
			case MOVE: return defaultMoveCode;
		}
		return null;
	}
	//TOOO
	public static final ActionCode defaultGetCode=new ActionCode()
	{
		@Override public ArrayList<MOB.QueuedCommand> prereqs(MOB mob, Body body, CMMsg msg) {
			return CMClass.emptyAL;
		}

		@Override public long sendAction(MOB mob, Body body, CMMsg msg) {
			return 0;
		}

		@Override public boolean satisfiesPrereqs(CMMsg msg) {
			return true;
		}

		@Override public void handleAction(CMMsg msg) {
		}
	};
	public static final ActionCode defaultGiveCode=new ActionCode()
	{
		@Override public ArrayList<MOB.QueuedCommand> prereqs(MOB mob, Body body, CMMsg msg)
		{
			//TODO: Calculate if mob's body can reach
			return CMClass.emptyAL;
		}

		@Override public long sendAction(MOB mob, Body body, CMMsg msg)
		{
			//TODO: Calculate if mob's body can reach
			for(CMObject I : (CMObject[])msg.tool().toArray(CMObject.dummyCMOArray))
			{
				CMMsg msg2=CMClass.getMsg(body,msg.target(),I,EnumSet.of(CMMsg.MsgCode.GIVE),"^[S-NAME] give^s ^[O-NAME] to ^[T-NAMESELF].");
				if(!mob.location().doMessage(msg2))
				{
					msg2.returnMsg();
					return -1;
				}
				msg2.returnMsg();
			}
			return 0;
		}

		@Override public boolean satisfiesPrereqs(CMMsg msg)
		{
			//TODO: Calculate if mob's body can reach
			return true;
		}

		@Override public void handleAction(CMMsg msg)
		{
			Vector<CMObject> objects=msg.tool();
			Interactable target = msg.target();
			if(target instanceof MOB) {
				MOB mob=((MOB)target);
				CMObject I;
				for(int i=0;i<objects.size();i++)
					if((I=objects.get(i)) instanceof Item)
						mob.giveItem((Item)I);
			}
			
		}
		
	};
	//TOOO
	public static final ActionCode defaultMoveCode=new ActionCode()
	{
		@Override public ArrayList<MOB.QueuedCommand> prereqs(MOB mob, Body body, CMMsg msg) {
			return CMClass.emptyAL;
		}

		@Override public long sendAction(MOB mob, Body body, CMMsg msg) {
			return 0;
		}

		@Override public boolean satisfiesPrereqs(CMMsg msg) {
			return true;
		}

		@Override public void handleAction(CMMsg msg) {
		}
	};
	
	@Override public long sendEat(MOB mob, Body body, Vector<Interactable> items)
	{
		return -1;
	}
	@Override public boolean handleEat(CMMsg msg)
	{
		return false;
	}
	@Override public ArrayList<MOB.QueuedCommand> eatPrereqs(MOB mob, Body body, Vector<Interactable> items) //Vector<Item> failed
	{
		mob.tell("You don't need to eat.");
		return null;
	}
	@Override public boolean satisfiesEatReqs(CMMsg msg)
	{
		Interactable source=msg.firstSource();
		if(source instanceof MOB)
			((MOB)source).tell("You don't need to eat.");
		return false;
	}
	@Override public boolean satisfiesEatPrereqs(CMMsg msg)
	{
		Interactable source=msg.firstSource();
		if(source instanceof MOB)
			((MOB)source).tell("You don't need to eat.");
		return false;
	}
	@Override public int diet(Body body, RawMaterial.Resource material)
	{
		return 0;
	}
	@Override public void applyDiet(Body body, Item source, int volume)
	{
	}
	@Override public int getBiteSize(Body body, Item source)
	{
		return 0;
	}
	@Override public int getMaxBiteSize(Body body)
	{
		return 0;
	}
	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

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
		return E==this;
	}
}
