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
public class StdExit implements Exit
{
	public String ID(){	return "StdExit";}

	protected String name="an ordinary pathway";
	protected String display="an open passage to another place.";
	protected String desc="";
	protected String plainName;
	protected String plainNameOf;
	protected String plainDisplay;
	protected String plainDisplayOf;
	protected String plainDesc;
	protected String plainDescOf;
	protected boolean visible=true;

	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Behavior> behaviors=new CopyOnWriteArrayList();
	protected long lastTick=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected int tickCount=0;
	protected boolean amDestroyed=false;

	protected Environmental myEnvironmental;//=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected Closeable myDoor=null;

	protected int saveNum=0;
	protected int doorToLoad=0;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;

	public StdExit(){}

	public Environmental getEnvObject(){
		if(myEnvironmental==null)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
	public Closeable getLidObject() {return myDoor;}

	public String directLook(MOB mob, Room destination)
	{
		String s=display;
		if((myDoor==null)||(!myDoor.closed()))
			s+=" It leads to "+destination.displayText();
		return s;
	}
	public String exitListLook(MOB mob, Room destination)
	{
		if((myDoor==null)||(!myDoor.closed()))
			return destination.displayText();
		return name;
	}
	public boolean visibleExit(MOB mob, Room destination) {return visible; }
	public void setVisible(boolean b){visible = b; CMLib.database().saveObject(this);}

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners() { }
	public void clearAllListeners() { }

	public void initializeClass(){}
	public String name(){ return name;}
	public String plainName()
	{
		if(name==plainNameOf)
			return plainName;
		String newName=name;
		String newPlain=CMLib.coffeeFilter().toRawString(newName);
		synchronized(this)
		{
			plainName=newPlain;
			plainNameOf=newName;
		}
		return newPlain;
	}
	public void setName(String newName){name=newName; CMLib.database().saveObject(this);}
	public String displayText(){return display;}
	public String plainDisplayText()
	{
		if(display==plainDisplayOf)
			return plainDisplay;
		String newDisplay=display;
		String newPlain=CMLib.coffeeFilter().toRawString(newDisplay);
		synchronized(this)
		{
			plainDisplay=newPlain;
			plainDisplayOf=newDisplay;
		}
		return newPlain;
	}
	public void setDisplayText(String newDisplayText){display=newDisplayText; CMLib.database().saveObject(this);}
	public String description(){return desc;}
	public String plainDescription()
	{
		if(desc==plainDescOf)
			return plainDesc;
		String newDesc=desc;
		String newPlain=CMLib.coffeeFilter().toRawString(newDesc);
		synchronized(this)
		{
			plainDesc=newPlain;
			plainDescOf=newDesc;
		}
		return newPlain;
	}
	public void setDescription(String newDescription){desc=newDescription; CMLib.database().saveObject(this);}

	public void destroy()
	{
		//clearAllListeners();
		amDestroyed=true;
		myEnvironmental.destroy();
		if(myDoor!=null) myDoor.destroy();
		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		for(Behavior B : behaviors)
			B.startBehavior(null);
		behaviors.clear();
		if(saveNum!=0)	//NOTE: I think this should be a standard destroy() check?
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed(){return amDestroyed;}

	public CMObject newInstance()
	{
		try
		{
			return (StdExit)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdExit();
	} 
	protected void cloneFix(StdExit E)
	{
		okCheckers=new CopyOnWriteArrayList();
		excCheckers=new CopyOnWriteArrayList();
		tickActers=new CopyOnWriteArrayList();
		lFlags=lFlags.clone();
		affects=new CopyOnWriteArrayList();
		behaviors=new CopyOnWriteArrayList();
		tickStatus=Tickable.TickStat.Not;
		if(E.myEnvironmental!=null)
			myEnvironmental=(Environmental)((Ownable)myEnvironmental.copyOf()).setOwner(this);
		if(E.myDoor!=null)
			myDoor=(Closeable)E.myDoor.copyOf();

		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
		for(Behavior B : E.behaviors)
			addBehavior((Behavior)B.copyOf());
	}
	public CMObject copyOf()
	{
		try
		{
			StdExit E=(StdExit)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	} 
/*	protected Rideable findALadder(MOB mob, Room room)
	{
		if(room==null) return null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.getItem(i);
			if((I!=null)
			   &&(I instanceof Rideable)
			   &&(CMLib.flags().canBeSeenBy(I,mob))
			   &&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_LADDER))
				return (Rideable)I;
		}
		return null;
	}
	protected void mountLadder(MOB mob, Rideable ladder)
	{
		CMMsg msg=CMClass.getMsg(mob,ladder,null,CMMsg.MSG_MOUNT,"<S-NAME> mounts <T-NAMESELF>.");
		Room room=(Room)((Item)ladder).owner();
		if(mob.location()==room) room=null;
		if((mob.location().okMessage(mob,msg))
		&&((room==null)||(room.okMessage(mob,msg))))
		{
			mob.location().send(mob,msg);
			if(room!=null)
				room.sendOthers(mob,msg);
		}
	} */

	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!myEnvironmental.okMessage(myHost, msg))
			return false;
		for(OkChecker O : okCheckers)
			if(!O.okMessage(this,msg))
				return false;
		return true;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		myEnvironmental.executeMsg(myHost, msg);
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost, msg);
	}
	public int compareTo(CMObject o)
	{
//		if(o instanceof Exit)
//			return exitID.compareTo(((Exit)o).exitID());
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	//Affectable
	public void addEffect(Effect to)
	{
		affects.add(to);
		to.setAffectedOne(this);
		CMLib.database().saveObject(this);
	}
	public void delEffect(Effect to)
	{
		if(affects.remove(to))
		{
			to.setAffectedOne(null);
			CMLib.database().saveObject(this);
		}
	}
	public boolean hasEffect(Effect to)
	{
		return affects.contains(to);
	}
	public int numEffects(){return affects.size();}
	public Effect fetchEffect(int index)
	{
		try { return affects.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Effect> fetchEffect(String ID)
	{
		Vector<Effect> V=new Vector(1);
		for(Effect E : affects)
			if(E.ID().equals(ID))
				V.add(E);
		return V;
	}
	public Iterator<Effect> allEffects() { return affects.iterator(); }

	//Behavable
	public void addBehavior(Behavior to)
	{
		synchronized(behaviors)
		{
			if(fetchBehavior(to.ID())!=null) return;
			to.startBehavior(this);
			behaviors.add(to);
		}
		CMLib.database().saveObject(this);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors.remove(to))
		{
			to.startBehavior(null);
			CMLib.database().saveObject(this);
		}
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		try { return behaviors.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(Behavior B : behaviors)
			if(B.ID().equals(ID))
				return B;
		return null;
	}
	public boolean hasBehavior(String ID)
	{
		for(Behavior B : behaviors)
			if(B.ID().equals(ID))
				return true;
		return false;
	}
	public Iterator<Behavior> allBehaviors() { return behaviors.iterator(); }

	//Affectable/Behavable shared
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}

	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
			CMLib.threads().delExit(this);
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
			CMLib.threads().addExit(this);
	}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		else if(tickTo>tickCount+10)
			tickTo=tickCount+10;
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {tickCount=0; return false;}	//Possibly also lFlags.remove(ListenHolder.Flags.TICK);
		}
		return true;
	}
	public int tickCounter(){return tickCount;}
	public void tickAct(){}
	protected boolean doTick()
	{
		tickStatus=Tickable.TickStat.Listener;
		for(TickActer T : tickActers)
			if(!T.tick(tickCount))
				tickActers.remove(T);
		tickStatus=Tickable.TickStat.Not;
		return (tickActers.size()>0);
	}
	//public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.EXIT.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.EXIT.removeNumber(saveNum);
			saveNum=num;
			SIDLib.EXIT.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(effectsToLoad!=null)
		{
			for(int SID : effectsToLoad)
			{
				Effect to = SIDLib.EFFECT.get(SID);
				if(to==null) continue;
				affects.add(to);
				to.setAffectedOne(this);
			}
			effectsToLoad=null;
		}
		if(behavesToLoad!=null)
		{
			for(int SID : behavesToLoad)
			{
				Behavior to = SIDLib.BEHAVIOR.get(SID);
				if(to==null) continue;
				to.startBehavior(this);
				behaviors.add(to);
			}
			behavesToLoad=null;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	public void finalize()
	{
		//Clean up the database. This might be a good thing to have in all CMSavables, but Exits in particular should have it!
		if((CMProps.Bools.MUDSTARTED.property())&&(!CMProps.Bools.MUDSHUTTINGDOWN.property()))
			destroy();
		//super.finalize();
	}


	private enum SCode implements CMSavable.SaveEnum{
		ENV(){
			public ByteBuffer save(StdExit E){
				if(E.myEnvironmental==null) return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.myEnvironmental); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdExit)fromThis).myEnvironmental;}
			public void load(StdExit E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		DSP(){
			public ByteBuffer save(StdExit E){
				if(E.display=="an open passage to another place.") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.display); }
			public int size(){return 0;}
			public void load(StdExit E, ByteBuffer S){ E.display=CMLib.coffeeMaker().loadString(S); } },
		DSC(){
			public ByteBuffer save(StdExit E){ return CMLib.coffeeMaker().savString(E.desc); }
			public int size(){return 0;}
			public void load(StdExit E, ByteBuffer S){ E.desc=CMLib.coffeeMaker().loadString(S); } },
		EFC(){
			public ByteBuffer save(StdExit E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdExit E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(StdExit E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdExit E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		VIS(){
			public ByteBuffer save(StdExit E){ return (ByteBuffer)ByteBuffer.wrap(new byte[1]).put(E.visible?(byte)1:(byte)0).rewind(); }
			public int size(){return 1;}
			public void load(StdExit E, ByteBuffer S){ E.visible=(S.get()!=0); } },
		NAM(){
			public ByteBuffer save(StdExit E){
				if(E.name=="an ordinary pathway") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(StdExit E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		CLS(){
			public ByteBuffer save(StdExit E){
				if(E.myDoor==null) return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.myDoor); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdExit)fromThis).myDoor;}
			public void load(StdExit E, ByteBuffer S){
				Closeable old=E.myDoor;
				E.myDoor=(Closeable)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myDoor!=null) ((Ownable)E.myDoor).setOwner(E);
				if((old!=null)&&(old!=E.myDoor)) old.destroy(); } },
		;
		public abstract ByteBuffer save(StdExit E);
		public abstract void load(StdExit E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdExit)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdExit)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		ENVIRONMENTAL(){
			public String brief(StdExit E){return E.getEnvObject().ID();}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		DISPLAY(){
			public String brief(StdExit E){return E.display;}
			public String prompt(StdExit E){return ""+E.display;}
			public void mod(StdExit E, MOB M){E.display=CMLib.genEd().stringPrompt(M, ""+E.display, false);} },
		DESCRIPTION(){
			public String brief(StdExit E){return E.display;}
			public String prompt(StdExit E){return ""+E.display;}
			public void mod(StdExit E, MOB M){E.display=CMLib.genEd().stringPrompt(M, ""+E.display, false);} },
		EFFECTS(){
			public String brief(StdExit E){return ""+E.affects.size();}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(StdExit E){return ""+E.behaviors.size();}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		VISIBLE(){
			public String brief(StdExit E){return ""+E.visible;}
			public String prompt(StdExit E){return ""+E.visible;}
			public void mod(StdExit E, MOB M){E.visible=CMLib.genEd().booleanPrompt(M, ""+E.visible);} },
		NAME(){
			public String brief(StdExit E){return E.name;}
			public String prompt(StdExit E){return E.name;}
			public void mod(StdExit E, MOB M){E.name=CMLib.genEd().stringPrompt(M, E.name, false);} },
		DOOR(){
			public String brief(StdExit E){return (E.myDoor==null)?("null"):(E.myDoor.ID());}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){
				if(E.myDoor==null) E.myDoor=(Closeable)CMLib.genEd().genMiscSet(M, CMLib.genEd().newAnyCloseable(M));
				else {
					char action=M.session().prompt("(E)dit or (D)estroy this closeable? (E)","E").trim().toUpperCase().charAt(0);
					if(action=='E') CMLib.genEd().genMiscSet(M, E.myDoor);
					else if(action=='D') {E.myDoor.destroy(); E.myDoor=null;} } } }
		;
		public abstract String brief(StdExit fromThis);
		public abstract String prompt(StdExit fromThis);
		public abstract void mod(StdExit toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdExit)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdExit)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdExit)toThis, M);} }

	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof StdExit)) return false;
		return true;
	}
}
