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
//	protected String exitID="";
	protected boolean visible=true;

	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	protected Vector<OkChecker> okCheckers=null;
	protected Vector<ExcChecker> excCheckers=null;
	protected Vector<TickActer> tickActers=null;
	protected Vector<Effect> affects=new Vector(1);
	protected Vector<Behavior> behaviors=new Vector(1);
	protected long lastTick=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected boolean amDestroyed=false;
//	protected boolean needSave=false;

	protected Environmental myEnvironmental=(Environmental)((Ownable)CMClass.Objects.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected Closeable myDoor=null;

	protected int saveNum=0;
	protected int doorToLoad=0;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;

	public StdExit(){}

	public Environmental getEnvObject() {return myEnvironmental;}
	public Closeable getLidObject() {return myDoor;}

//	public String exitID(){return exitID;}
//	public void setExitID(String s){exitID=s;}

	public String directLook(MOB mob, Room destination)
	{
		String s=display;
		if((myDoor==null)||(myDoor.isOpen()))
			s+=" It leads to "+destination.displayText();
		return s;
	}
	public String exitListLook(MOB mob, Room destination)
	{
		if(myDoor==null)
			return destination.displayText();
		/*if (myDoor instanceof ExitDoor)
			return myDoor.exitListLook();
		*/
		if(myDoor.isOpen())
			return destination.displayText();
		return name;
	}
	public boolean visibleExit(MOB mob, Room destination) {return visible; }
	public void setVisible(boolean b){visible = b; CMLib.database().saveObject(this);}
//	public boolean needSave(){return needSave;}
//	public void setSave(boolean b){needSave=b;}

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners() { }
	public void clearAllListeners() { }

//	protected void finalize(){}
	public void initializeClass(){}
	public String name(){ return name;}
	public void setName(String newName){name=newName; CMLib.database().saveObject(this);}
	public String displayText(){return display;}
	public void setDisplayText(String newDisplayText){display=newDisplayText; CMLib.database().saveObject(this);}
	public String description(){return desc;}
	public void setDescription(String newDescription){desc=newDescription; CMLib.database().saveObject(this);}

	public void destroy()
	{
		myEnvironmental.destroy();
		myDoor.destroy();
		affects=null;
		behaviors=null;
		amDestroyed=true;
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
	protected void cloneFix(Exit E)
	{
		saveNum=0;
		myEnvironmental=(Environmental)E.getEnvObject().copyOf();
		myDoor=E.getLidObject();
		if(myDoor!=null) myDoor=(Closeable)myDoor.copyOf();

		affects=null;
		behaviors=null;
		for(int b=0;b<E.numEffects();b++)
		{
			Effect B=E.fetchEffect(b);
			if(B!=null)
				addEffect((Effect)B.copyOf());
		}
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				addBehavior((Behavior)B.copyOf());
		}
	}
	public CMObject copyOf()
	{
		try
		{
			StdExit E=(StdExit)this.clone();
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
		if(okCheckers!=null)
		for(int i=okCheckers.size();i>0;i--)
			if(!okCheckers.get(i-1).okMessage(myHost,msg))
				return false;
		return true;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		myEnvironmental.executeMsg(myHost, msg);
		if(excCheckers!=null)
		for(int i=excCheckers.size();i>0;i--)
			excCheckers.get(i-1).executeMsg(myHost,msg);
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
		if(to==null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
		CMLib.database().saveObject(this);
	}
	public void delEffect(Effect to)
	{
		if(affects.removeElement(to))
			to.setAffectedOne(null);
		CMLib.database().saveObject(this);
	}
	public int numEffects(){return affects.size();}
	public Effect fetchEffect(int index)
	{
		try { return affects.elementAt(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Effect> fetchEffect(String ID)
	{
		Vector<Effect> V=new Vector<Effect>();
		for(int a=0;a<affects.size();a++)
		{
			Effect A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
				V.add(A);
		}
		return V;
	}
	public Vector<Effect> allEffects() { return (Vector<Effect>)affects.clone(); }

	//Behavable
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		if(fetchBehavior(to.ID())!=null) return;
		to.startBehavior(this);
		behaviors.addElement(to);
		CMLib.database().saveObject(this);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors.removeElement(to))
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
		try { return behaviors.elementAt(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Behavior> fetchBehavior(String ID)
	{
		Vector<Behavior> V=new Vector<Behavior>();
		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				V.add(B);
		}
		return V;
	}
	public Vector<Behavior> allBehaviors(){ return (Vector<Behavior>)behaviors.clone(); }

	//Affectable/Behavable shared
	public Vector<CharAffecter> charAffecters(){return null;}
	public Vector<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	public Vector<OkChecker> okCheckers(){if(okCheckers==null) okCheckers=new Vector(); return okCheckers;}
	public Vector<ExcChecker> excCheckers(){if(excCheckers==null) excCheckers=new Vector(); return excCheckers;}
	public Vector<TickActer> tickActers(){if(tickActers==null) tickActers=new Vector(); return tickActers;}
	//TODO: Exits that tick. What do?
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
//		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
//			if(container instanceof ListenHolder)
//				((ListenHolder)container).removeListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
//		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
//			if(container instanceof ListenHolder)
//				((ListenHolder)container).addListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
		if(tickID==Tickable.TickID.Action) return false;
		tickStatus=Tickable.TickStat.Listener;
		if(tickActers!=null)
		for(int i=tickActers.size()-1;i>=0;i--)
		{
			TickActer T=tickActers.get(i);
			if(!T.tick(ticking, tickID))
				removeListener(T, EnumSet.of(ListenHolder.Flags.TICK));
		}
		tickStatus=Tickable.TickStat.Not;
		lastTick=System.currentTimeMillis();
		return true;
	}
	public long lastAct(){return 0;}	//No Action ticks
	public long lastTick(){return lastTick;}

	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.Objects.EXIT.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.Objects.EXIT.removeNumber(saveNum);
			saveNum=num;
			SIDLib.Objects.EXIT.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(effectsToLoad!=null)
		{
			for(int SID : effectsToLoad)
			{
				Effect to = (Effect)SIDLib.Objects.EFFECT.get(SID);
				if(to==null) continue;
				affects.addElement(to);
				to.setAffectedOne(this);
			}
			effectsToLoad=null;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}


	private enum SCode implements CMSavable.SaveEnum{
		ENV(){
			public ByteBuffer save(StdExit E){ return CMLib.coffeeMaker().savSubFull(E.myEnvironmental); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdExit)fromThis).myEnvironmental;}
			public void load(StdExit E, ByteBuffer S){ E.myEnvironmental=(Environmental)((Ownable)CMLib.coffeeMaker().loadSub(S, E.myEnvironmental)).setOwner(E); } },
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
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(new CMSavable[E.affects.size()]));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdExit E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(StdExit E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(new CMSavable[E.behaviors.size()]));
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
			public ByteBuffer save(StdExit E){ return CMLib.coffeeMaker().savSubFull(E.myDoor); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdExit)fromThis).myDoor;}
			public void load(StdExit E, ByteBuffer S){ E.myDoor=(Closeable)((Ownable)CMLib.coffeeMaker().loadSub(S, E.myDoor)).setOwner(E); } },
		;
		public abstract ByteBuffer save(StdExit E);
		public abstract void load(StdExit E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdExit)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdExit)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		ENVIRONMENTAL(){
			public String brief(StdExit E){return E.myEnvironmental.ID();}
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
