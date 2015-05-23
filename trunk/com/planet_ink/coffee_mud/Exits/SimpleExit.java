package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.ExitInstance.OneToOneExitInstance;
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

/*
TODO: This class is really only useful for optimization and I should not be focusing on optimization yet.
Incomplete: When something attempts to add effects or listeners or stuff this exit should replace itself with
a fully functional exit instead of the simple version.
*/

public class SimpleExit extends AbstractSaveInteractable implements Exit
{
	@Override public String ID(){return "SimpleExit";}
	@Override protected SIDLib.Objects SID(){return SIDLib.EXIT;}
	@Override protected int AI_ENABLES(){return AI_ENV|AI_OK|AI_EXC|AI_TICK;}

	//protected static EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	//protected boolean amDestroyed=false;
	protected SimpleExitInstance exitInstA;
	protected SimpleExitInstance exitInstB;
	protected Room roomA;
	protected Room roomB;
	private int roomALoad;
	private int roomBLoad;

	//protected Environmental myEnvironmental;//=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);

	//protected int saveNum=0;

	public SimpleExit(){}
	protected SimpleExit(SimpleExit clone)
	{
		super(clone);
		if(clone.exitInstA!=null) { exitInstA=clone.exitInstA.copyOf(); exitInstA.setExit(this); }
		if(clone.exitInstB!=null) { exitInstB=clone.exitInstB.copyOf(); exitInstB.setExit(this); }
	}

	/*
	public Environmental getEnvObject(){
		if(myEnvironmental==null)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
	*/
	@Override public Closeable getLidObject() {return null;}

	@Override public String directLook(MOB mob, Room destination) { return displayText()+" It leads to "+destination.displayText(); }
	@Override public String exitListLook(MOB mob, Room destination) { return destination.displayText(); }
	@Override public ExitInstance makeInstance(Room source, Room destination)
	{
		SimpleExitInstance otherExit;
		boolean isA;
		if(exitInstA==null)
		{
			otherExit=exitInstB;
			isA=true;
		}
		else if(exitInstB==null)
		{
			otherExit=exitInstA;
			isA=false;
		}
		else return null;
		if(otherExit!=null && (otherExit.getDestination()!=source || !destination.hasExit(otherExit)))
			return null;
		SimpleExitInstance newInstance=new SimpleExitInstance(this, destination);
		if(isA)
			exitInstA=newInstance;
		else
			exitInstB=newInstance;
		CMLib.database().saveObject(newInstance);
		return newInstance;
	}
	@Override public void removeInstance(ExitInstance myInstance, boolean both)
	{
		SimpleExitInstance alsoRemove=null;
		if(myInstance==exitInstA)
		{
			exitInstA=null;
			if(both) alsoRemove=exitInstB;
			else if(exitInstB==null) both=true;
		}
		else if(myInstance==exitInstB)
		{
			exitInstB=null;
			if(both) alsoRemove=exitInstA;
			else if(exitInstA==null) both=true;
		}
		else return;
		if(both)
		{
			destroy();
			if(alsoRemove!=null && myInstance.getDestination() != null)
				myInstance.getDestination().removeExit(alsoRemove);
			return;
		}
		//myInstance.destroy();
	}

	@Override public ExitInstance oppositeOf(ExitInstance myInstance, Room destination)
	{
		if(myInstance==exitInstA) return exitInstB;
		else if(myInstance==exitInstB) return exitInstA;
		return null;
	}
	@Override public void linkMe(ExitInstance myInstance)
	{
		/* Shouldn't be needed (TODO: Why? Was I going to do this in link()? is link() done?)
		if(!(myInstance instanceof OneToOneExitInstance))
		{
			Log.errOut(ID(),"Incorrect ExitInstance type attempting to link");
		}
		if(exitInstA==null) exitInstA=(OneToOneExitInstance)myInstance;
		else if(exitInstB==null) exitInstB=(OneToOneExitInstance)myInstance;
		else Log.errOut(ID(),"ExitInstance attempting to link to full Exit");
		*/
	}

	@Override public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	@Override public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	@Override public void registerAllListeners() { }
	@Override public void clearAllListeners() { }

	@Override public void initializeClass(){}
	@Override public boolean visibleExit(MOB mob, Room destination){ return true; }
	@Override public String name(){ return "a wide open passage";}
	@Override public String plainName(){ return "a wide open passage";}
	@Override public String displayText(){return "a path to another place.";}
	@Override public String plainDisplayText(){return "a path to another place.";}
	@Override public String description(){return "";}
	@Override public String plainDescription(){return "";}
	@Override public void setVisible(boolean b){}
	//public void setName(String newName){}
	//public void setDisplayText(String newDisplayText){}
	//public void setDescription(String newDescription){}

	//public void destroy() {super.destroy();}
	//public boolean amDestroyed(){return amDestroyed;}

	@Override public SimpleExit newInstance()
	{
		try{return this.getClass().newInstance();}catch(Exception e){Log.errOut(ID(),e);}
		return new SimpleExit();
	}
	/*
	protected void cloneFix(SimpleExit E)
	{
		if(E.myEnvironmental!=null)
			myEnvironmental=(Environmental)((Ownable)myEnvironmental.copyOf()).setOwner(this);
	}
	*/
	@Override public SimpleExit copyOf()
	{
		return new SimpleExit(this);
		/*
		try
		{
			SimpleExit E=(SimpleExit)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
		*/
	}
	@Override public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!myEnvironmental.okMessage(myHost, msg))
			return false;
		return true;
	}
	@Override public boolean respondTo(CMMsg msg, Object data){return true;}
	@Override public boolean respondTo(CMMsg msg){return true;}
	@Override public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		myEnvironmental.executeMsg(myHost, msg);
	}
	//public int compareTo(CMObject o)
	//{
//		if(o instanceof Exit)
//			return exitID.compareTo(((Exit)o).exitID());
	//	return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	//}

	//Affectable
	//public void addEffect(Effect to) { }
	//public void delEffect(Effect to) { }
	//public boolean hasEffect(Effect to) { return false; }
	//public int numEffects(){return 0;}
	//public Effect fetchEffect(int index) { return null; }
	//public Vector<Effect> fetchEffect(String ID) { return CMClass.emptyVector; }
	//public Effect fetchFirstEffect(String ID) {	return null; }
	//public Iterator<Effect> allEffects() { return Collections.emptyIterator(); }

	//Behavable
	//public void addBehavior(Behavior to) { }
	//public void delBehavior(Behavior to) { }
	//public int numBehaviors() { return 0; }
	//public Behavior fetchBehavior(int index) { return null; }
	//public Behavior fetchBehavior(String ID) { return null; }
	//public boolean hasBehavior(String ID) { return false; }
	//public Iterator<Behavior> allBehaviors(){ return Collections.emptyIterator(); }

	//Affectable/Behavable shared
	//public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	//public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	//public CopyOnWriteArrayList<OkChecker> okCheckers(){return null;}
	//public CopyOnWriteArrayList<ExcChecker> excCheckers(){return null;}
	//public CopyOnWriteArrayList<TickActer> tickActers(){return null;}

	@Override public void removeListener(Listener oldAffect, EnumSet flags) { }
	@Override public void addListener(Listener newAffect, EnumSet flags) { }
	//public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	//public Tickable.TickStat getTickStatus(){return Tickable.TickStat.Not;}
	//public boolean tick(int tickTo) { return false; }
	//public int tickCounter(){return 0;}
	//public void tickAct(){}
	//public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	@Override public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null) totalEnumM=CMParms.appendToArray(MCode.values(), super.totalEnumM(), ModEnum[].class);
		return totalEnumM;
	}
	@Override public Enum[] headerEnumM()
	{
		if(headerEnumM==null) headerEnumM=CMParms.appendToArray(new Enum[] {MCode.values()[0]}, super.headerEnumM(), Enum[].class);
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	@Override public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null) totalEnumS=CMParms.appendToArray(SCode.values(), super.totalEnumS(), SaveEnum[].class);
		return totalEnumS;
	}
	@Override public Enum[] headerEnumS()
	{
		if(headerEnumS==null) headerEnumS=CMParms.appendToArray(new Enum[] {SCode.values()[0]}, super.headerEnumS(), Enum[].class);
		return headerEnumS;
	}
	//@Override public boolean needLink(){return true;}
	@Override public void link()
	{
		if(roomALoad!=0)
		{
			roomA=SIDLib.ROOM.get(roomALoad);
		}
		if(roomBLoad!=0)
		{
			roomB=SIDLib.ROOM.get(roomBLoad);
		}
		super.link();
	}
	//public void saveThis(){CMLib.database().saveObject(this);}
	@Override public void prepDefault(){}

	@Override public void finalize() throws Throwable
	{
		//Clean up the database. This might be a good thing to have in all CMSavables, but Exits in particular should have it!
		if((CMProps.Bools.MUDSTARTED.property())&&(!CMProps.Bools.MUDSHUTTINGDOWN.property()))
			destroy();
		super.finalize();
	}

	private enum SCode implements SaveEnum<SimpleExit>{
		ENV(){
			public ByteBuffer save(SimpleExit E){
				if(E.myEnvironmental==null) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.myEnvironmental); }
			public int size(){return -1;}
			public CMSavable subObject(SimpleExit fromThis){return fromThis.myEnvironmental;}
			public void load(SimpleExit E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		RMA(){
			public ByteBuffer save(SimpleExit E){
				if(E.roomA!=null) return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.roomA.saveNum()).rewind();
				return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(0).rewind(); }
			public int size(){return 4;}
			public void load(SimpleExit E, ByteBuffer S){ E.roomALoad = S.getInt(); } },
		RMB(){
			public ByteBuffer save(SimpleExit E){
				if(E.roomB!=null) return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.roomB.saveNum()).rewind();
				return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(0).rewind(); }
			public int size(){return 4;}
			public void load(SimpleExit E, ByteBuffer S){ E.roomBLoad = S.getInt(); } },
		;
		public CMSavable subObject(SimpleExit fromThis){return null;} }
	private enum MCode implements ModEnum<SimpleExit>{
		ENVIRONMENTAL(){
			public String brief(SimpleExit E){return E.getEnvObject().ID();}
			public String prompt(SimpleExit E){return "";}
			public void mod(SimpleExit E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		ROOMA(){
			public String brief(SimpleExit E){return E.roomA==null?"null":(""+E.roomA.saveNum());}
			public String prompt(SimpleExit E){return "";}
			public void mod(SimpleExit E, MOB M){} }, //Ideally this should be an automatic thing affected by moving the room around within an area.
		ROOMB(){
			public String brief(SimpleExit E){return E.roomB==null?"null":(""+E.roomB.saveNum());}
			public String prompt(SimpleExit E){return "";}
			public void mod(SimpleExit E, MOB M){} },
		; }

	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof SimpleExit)) return false;
		return true;
	}
	
	protected static class SimpleExitInstance implements ExitInstance
	{
		protected SimpleExit myExit;
		protected Room leadsTo;

		public SimpleExitInstance(){}
		public SimpleExitInstance(SimpleExit exit, Room room){myExit=exit; leadsTo=room;}

		@Override public Exit getExit(){return myExit;}
		@Override public Room getDestination(){return leadsTo;}
		 public Room getInRoom(){return null;}
		 public void setExit(Exit e) { }
		 public void setDestination(Room r) { leadsTo = r; }
		 public void setInRoom(Room r) { }
		@Override public String ID(){return "SimpleExitInstance";}
		@Override public Environmental getEnvObject() { return myExit.getEnvObject(); }
		@Override public SimpleExitInstance newInstance(){return new SimpleExitInstance();}
		@Override public SimpleExitInstance copyOf(){return new SimpleExitInstance(myExit, leadsTo);}
		@Override public void initializeClass(){}
		@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

		@Override public String name(){return myExit.name();}
		@Override public String plainName(){return myExit.plainName();}
		@Override public void setName(String newName){myExit.setName(newName);}
		@Override public String displayText(){return myExit.directLook(null, leadsTo);}
		@Override public String plainDisplayText(){return CMLib.coffeeFilter().toRawString(myExit.directLook(null, leadsTo));}
		@Override public void setDisplayText(String newDisplayText){myExit.setDisplayText(newDisplayText);}
		@Override public String description(){return myExit.description();}
		@Override public String plainDescription(){return myExit.plainDescription();}
		@Override public void setDescription(String newDescription){myExit.setDescription(newDescription);}

		@Override public void addBehavior(Behavior to){myExit.addBehavior(to);}
		@Override public void delBehavior(Behavior to){myExit.delBehavior(to);}
		@Override public boolean hasBehavior(String ID){return myExit.hasBehavior(ID);}
		@Override public int numBehaviors(){return myExit.numBehaviors();}
		@Override public Behavior fetchBehavior(int index){return myExit.fetchBehavior(index);}
		@Override public Behavior fetchBehavior(String ID){return myExit.fetchBehavior(ID);}
		@Override public Iterator<Behavior> allBehaviors(){return myExit.allBehaviors();}

		@Override public void addEffect(Effect to){myExit.addEffect(to);}
		@Override public void delEffect(Effect to){myExit.delEffect(to);}
		@Override public boolean hasEffect(Effect to){return myExit.hasEffect(to);}
		@Override public int numEffects(){return myExit.numEffects();}
		@Override public Effect fetchEffect(int index){return myExit.fetchEffect(index);}
		@Override public Vector<Effect> fetchEffect(String ID){return myExit.fetchEffect(ID);}
		@Override public Effect fetchFirstEffect(String ID){return myExit.fetchFirstEffect(ID);}
		@Override public Iterator<Effect> allEffects(){return myExit.allEffects();}

		@Override public CopyOnWriteArrayList<CharAffecter> charAffecters(){return myExit.charAffecters();}
		@Override public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return myExit.envAffecters();}
		@Override public CopyOnWriteArrayList<OkChecker> okCheckers(){return myExit.okCheckers();}
		@Override public CopyOnWriteArrayList<ExcChecker> excCheckers(){return myExit.excCheckers();}
		@Override public CopyOnWriteArrayList<TickActer> tickActers(){return myExit.tickActers();}
		@Override public void removeListener(Listener oldAffect, EnumSet flags){myExit.removeListener(oldAffect, flags);}
		@Override public void addListener(Listener newAffect, EnumSet flags){myExit.addListener(newAffect, flags);}
		@Override public void registerListeners(ListenHolder forThis){myExit.registerListeners(forThis);}
		@Override public void registerAllListeners(){myExit.registerAllListeners();}
		@Override public void clearAllListeners(){myExit.clearAllListeners();}
		@Override public int priority(ListenHolder forThis){return myExit.priority(forThis);}
		@Override public EnumSet<ListenHolder.Flags> listenFlags() {return myExit.listenFlags();}
		//Special case for ticking. This will never tick. Exits have a special tick method.
		@Override public Tickable.TickStat getTickStatus(){return myExit.getTickStatus();}
		public boolean tick(int tickTo){return false;}
		public int tickCounter(){return 0;}
		@Override public boolean respondTo(CMMsg msg){return myExit.respondTo(msg);}
		@Override public boolean respondTo(CMMsg msg, Object data){return myExit.respondTo(msg, data);}
		@Override public boolean okMessage(OkChecker myHost, CMMsg msg){return myExit.okMessage(myHost, msg);}
		@Override public void executeMsg(ExcChecker myHost, CMMsg msg){myExit.executeMsg(myHost, msg);}
		public boolean amDestroyed(){return myExit.destroyed;}
		public void destroy()
		{
			//uh?
			myExit.removeInstance(this, false);
		}

		//CMModifiable and CMSavable
		//CMModifiable and CMSavable
		@Override public SaveEnum[] totalEnumS(){return dummySEArray;}
		@Override public Enum[] headerEnumS(){return CMClass.dummyEnumArray;}
		@Override public ModEnum[] totalEnumM(){return MSICode.values();}
		@Override public Enum[] headerEnumM(){return new Enum[] {MSICode.values()[0]};}
		public int saveNum() { return 0; }
		@Override public void setSaveNum(int num){}
		@Override public boolean needLink(){return false;}
		@Override public void link(){}
		@Override public void saveThis(){CMLib.database().saveObject(myExit);}
		@Override public void prepDefault(){}


		private enum MSICode implements ModEnum<SimpleExitInstance>{
			SIMPLEEXIT(){
				public String brief(SimpleExitInstance E){return E.myExit.ID();}
				public String prompt(SimpleExitInstance E){return "";}
				public void mod(SimpleExitInstance E, MOB M){CMLib.genEd().genMiscSet(M, E.myExit);} },
			; }
	}
}
