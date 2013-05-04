package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class SimpleExit implements Exit
{
	public String ID(){	return "SimpleExit";}

	protected static EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	protected boolean amDestroyed=false;

	protected Environmental myEnvironmental;//=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);

	protected int saveNum=0;

	public SimpleExit(){}

	public Environmental getEnvObject(){
		if(myEnvironmental==null)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
	public Closeable getLidObject() {return null;}

	public String directLook(MOB mob, Room destination) { return displayText()+" It leads to "+destination.displayText(); }
	public String exitListLook(MOB mob, Room destination) { return destination.displayText(); }

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners() { }
	public void clearAllListeners() { }

	public void initializeClass(){}
	public boolean visibleExit(MOB mob, Room destination){ return true; }
	public String name(){ return "a wide open passage";}
	public String plainName(){ return "a wide open passage";}
	public String displayText(){return "a path to another place.";}
	public String plainDisplayText(){return "a path to another place.";}
	public String description(){return "";}
	public String plainDescription(){return "";}
	public void setVisible(boolean b){}
	public void setName(String newName){}
	public void setDisplayText(String newDisplayText){}
	public void setDescription(String newDescription){}

	public void destroy()
	{
		//clearAllListeners();
		amDestroyed=true;
		myEnvironmental.destroy();
		if(saveNum!=0)	//NOTE: I think this should be a standard destroy() check?
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed(){return amDestroyed;}

	public CMObject newInstance()
	{
		try
		{
			return (SimpleExit)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new SimpleExit();
	}
	protected void cloneFix(SimpleExit E)
	{
		if(E.myEnvironmental!=null)
			myEnvironmental=(Environmental)((Ownable)myEnvironmental.copyOf()).setOwner(this);
	}
	public CMObject copyOf()
	{
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
	}
	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!myEnvironmental.okMessage(myHost, msg))
			return false;
		return true;
	}
	public boolean respondTo(CMMsg msg, Object data){return true;}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		myEnvironmental.executeMsg(myHost, msg);
	}
	public int compareTo(CMObject o)
	{
//		if(o instanceof Exit)
//			return exitID.compareTo(((Exit)o).exitID());
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	//Affectable
	public void addEffect(Effect to) { }
	public void delEffect(Effect to) { }
	public boolean hasEffect(Effect to) { return false; }
	public int numEffects(){return 0;}
	public Effect fetchEffect(int index) { return null; }
	public Vector<Effect> fetchEffect(String ID) { return CMClass.emptyVector; }
	public Effect fetchFirstEffect(String ID) {	return null; }
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
	public boolean needLink(){return false;}
	public void link() {}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	public void finalize()
	{
		//Clean up the database. This might be a good thing to have in all CMSavables, but Exits in particular should have it!
		if((CMProps.Bools.MUDSTARTED.property())&&(!CMProps.Bools.MUDSHUTTINGDOWN.property()))
			destroy();
		//super.finalize();
	}


	private enum SCode implements SaveEnum<SimpleExit>{
		ENV(){
			public ByteBuffer save(SimpleExit E){
				if(E.myEnvironmental==null) return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.myEnvironmental); }
			public int size(){return -1;}
			public CMSavable subObject(SimpleExit fromThis){return fromThis.myEnvironmental;}
			public void load(SimpleExit E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		;
		public CMSavable subObject(SimpleExit fromThis){return null;} }
	private enum MCode implements ModEnum<SimpleExit>{
		ENVIRONMENTAL(){
			public String brief(SimpleExit E){return E.getEnvObject().ID();}
			public String prompt(SimpleExit E){return "";}
			public void mod(SimpleExit E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		; }

	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof SimpleExit)) return false;
		return true;
	}
}
