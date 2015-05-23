package com.planet_ink.coffee_mud.Exits;
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

public class StdExit extends AbstractSaveInteractable implements Exit
{
	@Override protected int AI_ENABLES(){return AI_ENV|AI_AFFECTS|AI_BEHAVES|AI_OK|AI_EXC|AI_TICK|AI_NAME|AI_DISP|AI_DESC;}
	@Override public String ID(){return "StdExit";}
	@Override protected SIDLib.Objects SID(){return SIDLib.EXIT;}

	protected boolean visible=true;
	protected ExitInstance exitInstA;
	protected ExitInstance exitInstB;

	//protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	//protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<Behavior> behaviors=new CopyOnWriteArrayList();
	//protected long lastTick=0;
	//protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	//protected int tickCount=0;
	//protected boolean amDestroyed=false;

	//protected Environmental myEnvironmental;//=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected Closeable myDoor=null;

	//protected int saveNum=0;
	protected int doorToLoad=0;
	//protected int[] effectsToLoad=null;
	//protected int[] behavesToLoad=null;

	public StdExit(){}
	protected StdExit(StdExit clone)
	{
		super(clone);
		visible = clone.visible;
		if(clone.exitInstA!=null) { exitInstA=clone.exitInstA.copyOf(); exitInstA.setExit(this); }
		if(clone.exitInstB!=null) { exitInstB=clone.exitInstB.copyOf(); exitInstB.setExit(this); }
		if(clone.myDoor!=null) { myDoor=clone.myDoor.copyOf();  } //myDoor.setOwner(this);
		
	}

	/*
	public Environmental getEnvObject(){
		if(myEnvironmental==null)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
	*/
	@Override public Closeable getLidObject() {return myDoor;}

	@Override public String directLook(MOB mob, Room destination)
	{
		String s=displayText();
		if((myDoor==null)||(!myDoor.closed()))
			s+=" It leads to "+destination.displayText();
		return s;
	}
	@Override public String exitListLook(MOB mob, Room destination)
	{
		if((myDoor==null)||(!myDoor.closed()))
			return destination.displayText();
		return name;
	}
	@Override public boolean visibleExit(MOB mob, Room destination) {return visible; }
	@Override public void setVisible(boolean b){visible = b; CMLib.database().saveObject(this);}
	@Override public ExitInstance makeInstance(Room source, Room destination)
	{
		ExitInstance otherExit;
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
		//StdExitInstance newInstance=new StdExitInstance(this, destination);
		ExitInstance newInstance=CMClass.EXITINSTANCE.getNew("StdExitInstance");
		newInstance.setExit(this);
		newInstance.setDestination(destination);
		if(isA)
			exitInstA=newInstance;
		else
			exitInstB=newInstance;
		CMLib.database().saveObject(newInstance);
		return newInstance;
	}
	@Override public void removeInstance(ExitInstance myInstance, boolean both)
	{
		ExitInstance alsoRemove=null;
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
			if(alsoRemove!=null)
				myInstance.getDestination().removeExit(alsoRemove);
			return;
		}
		myInstance.destroy();
	}

	@Override public ExitInstance oppositeOf(ExitInstance myInstance, Room destination)
	{
		if(myInstance==exitInstA) return exitInstB;
		else if(myInstance==exitInstB) return exitInstA;
		return null;
	}
	@Override public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	@Override public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	@Override public void registerAllListeners() { }
	@Override public void clearAllListeners() { }

	//@Override public void initializeClass(){}
	@Override public String name(){ return name==""?"an ordinary pathway":name;}
	@Override public String displayText(){return display==""?"an open passage to another place.":display;}
	@Override public String description(){return desc;}

	@Override public void destroy()
	{
		if(myDoor!=null) myDoor.destroy();
		if(exitInstA!=null)
			exitInstA.destroy();
		if(exitInstB!=null)
			exitInstB.destroy();
		super.destroy();
	}
	//public boolean amDestroyed(){return amDestroyed;}

	@Override public StdExit newInstance()
	{
		try{return this.getClass().newInstance();}catch(Exception e){Log.errOut(ID(),e);}
		return new StdExit();
	} 
	/*
	protected void cloneFix(StdExit E)
	{
		super.cloneFix(this);
		//TODO: clone instances, change exits of instances to this Exit.
	}
	*/
	@Override public StdExit copyOf()
	{
		return new StdExit(this);
		/*
		try
		{
			StdExit E=(StdExit)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e){return this.newInstance();}
		*/
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
		CMMsg msg=CMClass.getMsg(mob,ladder,null,CMMsg.MSG_MOUNT,"^[S-NAME] mounts ^[T-NAMESELF].");
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

	@Override public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!myEnvironmental.okMessage(myHost, msg))
			return false;
		for(OkChecker O : okCheckers)
			if(!O.okMessage(this,msg))
				return false;
		return true;
	}
	@Override public boolean respondTo(CMMsg msg, Object data){return true;}
	@Override public boolean respondTo(CMMsg msg){return true;}
	@Override public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		myEnvironmental.executeMsg(myHost, msg);
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost, msg);
	}
	//@Override public int compareTo(CMObject o){return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Affectable/Behavable shared
	//public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	//public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	//public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	//public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	//public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}

	@Override public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
			CMLib.threads().delExit(this);
	}
	@Override public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
			CMLib.threads().addExit(this);
	}
	//public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	//public Tickable.TickStat getTickStatus(){return tickStatus;}
	@Override public boolean tick(int tickTo)
	{
		boolean result=super.tick(tickTo);
		if(!result) lFlags.remove(ListenHolder.Flags.TICK);
		return result;
	}
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
	//@Override public void link(){super.link();}
	@Override public void linkMe(ExitInstance myInstance)
	{
		if(exitInstA==null) exitInstA=myInstance;
		else if(exitInstB==null) exitInstB=myInstance;
		else Log.errOut(ID(),"ExitInstance attempting to link to full Exit");
	}
	//public void prepDefault(){}

	@Override public void finalize() throws Throwable
	{
		//Clean up the database. This might be a good thing to have in all CMSavables, but Exits in particular should have it!
		if((CMProps.Bools.MUDSTARTED.property())&&(!CMProps.Bools.MUDSHUTTINGDOWN.property()))
			destroy();
		super.finalize();
	}
	
	@Override public void saveThis()
	{
		super.saveThis();
		if(exitInstA!=null) exitInstA.saveThis();
		if(exitInstB!=null) exitInstB.saveThis();
	}

	private enum SCode implements SaveEnum<StdExit>{
		VIS(){
			public ByteBuffer save(StdExit E){ return (ByteBuffer)ByteBuffer.wrap(new byte[1]).put(E.visible?(byte)1:(byte)0).rewind(); }
			public int size(){return 1;}
			public void load(StdExit E, ByteBuffer S){ E.visible=(S.get()!=0); } },
		CLS(){
			public ByteBuffer save(StdExit E){
				if(E.myDoor==null) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.myDoor); }
			public int size(){return -1;}
			public CMSavable subObject(StdExit fromThis){return fromThis.myDoor;}
			public void load(StdExit E, ByteBuffer S){
				Closeable old=E.myDoor;
				E.myDoor=(Closeable)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myDoor!=null) ((Ownable)E.myDoor).setOwner(E);
				if((old!=null)&&(old!=E.myDoor)) old.destroy(); } },
		;
		public CMSavable subObject(StdExit fromThis){return null;} }
	private enum MCode implements ModEnum<StdExit>{
		VISIBLE(){
			public String brief(StdExit E){return ""+E.visible;}
			public String prompt(StdExit E){return ""+E.visible;}
			public void mod(StdExit E, MOB M){E.visible=CMLib.genEd().booleanPrompt(M, ""+E.visible);} },
		DOOR(){
			public String brief(StdExit E){return (E.myDoor==null)?("null"):(E.myDoor.ID());}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){
				if(E.myDoor==null) E.myDoor=(Closeable)CMLib.genEd().genMiscSet(M, CMLib.genEd().newAnyCloseable(M));
				else {
					char action=M.session().prompt("(E)dit or (D)estroy this closeable? (E)","E").trim().toUpperCase().charAt(0);
					if(action=='E') CMLib.genEd().genMiscSet(M, E.myDoor);
					else if(action=='D') {E.myDoor.destroy(); E.myDoor=null;} } } }
		; }

	/*
	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof StdExit)) return false;
		return true;
	}
	*/
}
