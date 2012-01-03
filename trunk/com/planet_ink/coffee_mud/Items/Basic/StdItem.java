package com.planet_ink.coffee_mud.Items.Basic;
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
public class StdItem implements Item
{
	public String ID(){	return "StdItem";}

	protected String name="an ordinary item";
	protected String display="a nondescript item sits here doing nothing.";
	protected String desc="";
	protected String miscText="";
	protected boolean damagable=false;
	protected int baseGoldValue=0;
	protected int wornOut=0;
	protected int ridesNumber=0;
	protected Rideable ride=null;
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	protected Vector<OkChecker> okCheckers=null;
	protected Vector<ExcChecker> excCheckers=null;
	protected Vector<TickActer> tickActers=null;
	protected Vector affects=new Vector(1);
	protected Vector behaviors=new Vector(1);
	protected long lastTick=0;
//	protected int actTimer=0;
	//Should this be ItemCollection? Meh.
	protected CMObject container=null;
//	protected int material=RawMaterial.RESOURCE_COTTON;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected boolean destroyed=false;
	protected Environmental myEnvironmental=(Environmental)((Ownable)CMClass.Objects.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected int saveNum=0;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;
	protected int rideToLoad=0;

	public Environmental getEnvObject() {return myEnvironmental;}

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners()
	{
		if(container instanceof ListenHolder)
			((ListenHolder)container).addListener(this, lFlags);
	}
	public void clearAllListeners()
	{
		if(container instanceof ListenHolder)
			((ListenHolder)container).removeListener(this, lFlags);
	}
	public StdItem(){}
	public void initializeClass(){}
	public void setName(String newName){name=newName; CMLib.database().saveObject(this);}
	public String name(){ return name;}
	public String displayText(){return display;}
	public void setDisplayText(String newDisplayText){display=newDisplayText; CMLib.database().saveObject(this);}
	public String description(){return desc;}
	public void setDescription(String newDescription){desc=newDescription; CMLib.database().saveObject(this);}
	public void setMiscText(String newMiscText){miscText=newMiscText; CMLib.database().saveObject(this);}
	public String text(){return miscText;}
	public int ridesNumber(){return ridesNumber;}
	public Rideable ride(){return ride;}
	public void setRide(Rideable R){ride=R; CMLib.database().saveObject(this);}
	public CMObject container(){return container;}
	public void setContainer(CMObject E)
	{
		if(E==container) return;
		clearAllListeners();
		container=E;
		registerAllListeners();
//		CMLib.database().saveObject(this);
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
		{
			to.setAffectedOne(null);
			CMLib.database().saveObject(this);
		}
	}
	public int numEffects(){return affects.size();}
	public Effect fetchEffect(int index)
	{
		try { return (Effect)affects.elementAt(index); }
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
		try { return (Behavior)behaviors.elementAt(index); }
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
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
			if(container instanceof ListenHolder)
				((ListenHolder)container).removeListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
			if(container instanceof ListenHolder)
				((ListenHolder)container).addListener(this, EnumSet.of(ListenHolder.Flags.TICK));
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

	public CMObject newInstance()
	{
		try
		{
			return (CMObject)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdItem();
	}
	protected void cloneFix(Item E)
	{
//		destroyed=false;
		saveNum=0;
		myEnvironmental=(Environmental)E.getEnvObject().copyOf();

		affects=null;
		behaviors=null;
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)	addBehavior((Behavior)B.copyOf());
		}

		for(int a=0;a<E.numEffects();a++)
		{
			Effect A=E.fetchEffect(a);
			if(A!=null)
				addEffect((Effect)A.copyOf());
		}
	}
	public CMObject copyOf()
	{
		try
		{
			StdItem E=(StdItem)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public int value()
	{
		return baseGoldValue();
	}
	public int baseGoldValue(){return baseGoldValue;}
	public void setBaseValue(int newValue) { baseGoldValue=newValue; CMLib.database().saveObject(this);}
	public int wornOut()
	{
		if(damagable)
			return wornOut;
		return 0;
	}
	public void setWornOut(int worn) { wornOut=worn; CMLib.database().saveObject(this);}
	public boolean damagable() {return damagable;}
	public void setDamagable(boolean bool){damagable=bool; CMLib.database().saveObject(this);}


//TODO
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

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

	public int recursiveWeight()
	{
		int weight=myEnvironmental.envStats().weight();
		ItemCollection subItems=ItemCollection.O.getFrom(this);
		if(subItems!=null)
		{
			for(int i=0;i<subItems.numItems();i++)
			{
				Item thisItem=subItems.getItem(i);
				if(thisItem!=null)
					weight+=thisItem.recursiveWeight();
			}
		}
		return weight;
	}
	
	public void destroy()
	{
		clearAllListeners();
		myEnvironmental.destroy();

		destroyed=true;

		ItemCollection owner=ItemCollection.O.getFrom(container);
		if(owner!=null)
			owner.removeItem(this);
		
		ItemCollection inv=ItemCollection.O.getFrom(this);
		if(inv!=null)
		{
			if(owner==null)
				for(int i=inv.numItems()-1;i>=0;i--)
					inv.getItem(i).destroy();
			else
				for(int i=owner.numItems()-1;i>=0;i--)
					owner.addItem(inv.getItem(i));
		}
		CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed(){return destroyed;}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(!destroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.Objects.ITEM.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.Objects.ITEM.removeNumber(saveNum);
			saveNum=num;
			SIDLib.Objects.ITEM.assignNumber(num, this);
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
		if(behavesToLoad!=null)
		{
			for(int SID : behavesToLoad)
			{
				Behavior to = (Behavior)SIDLib.Objects.BEHAVIOR.get(SID);
				if(to==null) continue;
				to.startBehavior(this);
				behaviors.addElement(to);
			}
			behavesToLoad=null;
		}
		if(rideToLoad!=0)
		{
			ride=(Rideable)SIDLib.Objects.RIDEABLE.get(rideToLoad);
			if(ride!=null)
				ride.addRider(this);
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}

	private enum SCode implements CMSavable.SaveEnum{
		VAL(){
			public ByteBuffer save(StdItem E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.baseGoldValue).rewind(); }
			public int size(){return 4;}
			public void load(StdItem E, ByteBuffer S){ E.baseGoldValue=S.getInt(); } },
		ENV(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savSubFull(E.myEnvironmental); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdItem)fromThis).myEnvironmental;}
			public void load(StdItem E, ByteBuffer S){ E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E.myEnvironmental); } },
		WRN(){
			public ByteBuffer save(StdItem E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.wornOut).rewind(); }
			public int size(){return 4;}
			public void load(StdItem E, ByteBuffer S){ E.wornOut=S.getInt(); } },
		DMG(){
			public ByteBuffer save(StdItem E){ return (ByteBuffer)ByteBuffer.wrap(new byte[1]).put((byte)(E.damagable?1:0)).rewind(); }
			public int size(){return 1;}
			public void load(StdItem E, ByteBuffer S){ E.damagable=(S.get()==1); } },
		DSP(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.display); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.display=CMLib.coffeeMaker().loadString(S); } },
		DSC(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.desc); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.desc=CMLib.coffeeMaker().loadString(S); } },
		TXT(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.miscText); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.miscText=CMLib.coffeeMaker().loadString(S); } },
		RNM(){	//NOTE: this may be more efficient as a var enum since it's not often used..
			public ByteBuffer save(StdItem E){
				if(E.ride!=null)
					return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.ride.saveNum()).rewind();
				return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(0).rewind(); }
			public int size(){return 4;}
			public void load(StdItem E, ByteBuffer S){ E.rideToLoad=S.getInt(); } },
		EFC(){
			public ByteBuffer save(StdItem E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(new CMSavable[E.affects.size()]));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(StdItem E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(new CMSavable[E.behaviors.size()]));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		NAM(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		;
		public abstract ByteBuffer save(StdItem E);
		public abstract void load(StdItem E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdItem)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdItem)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		VALUE(){
			public String brief(StdItem E){return ""+E.baseGoldValue;}
			public String prompt(StdItem E){return ""+E.baseGoldValue;}
			public void mod(StdItem E, MOB M){E.setBaseValue(CMLib.genEd().intPrompt(M, ""+E.baseGoldValue));} },
		ENVIRONMENTAL(){
			public String brief(StdItem E){return E.myEnvironmental.ID();}
			public String prompt(StdItem E){return "";}
			public void mod(StdItem E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		WORNOUT(){
			public String brief(StdItem E){return ""+E.wornOut;}
			public String prompt(StdItem E){return ""+E.wornOut;}
			public void mod(StdItem E, MOB M){E.setWornOut(CMLib.genEd().intPrompt(M, ""+E.wornOut));} },
		DAMAGABLE(){
			public String brief(StdItem E){return ""+E.damagable;}
			public String prompt(StdItem E){return ""+E.damagable;}
			public void mod(StdItem E, MOB M){E.setDamagable(CMLib.genEd().booleanPrompt(M, ""+E.damagable));} },
		DISPLAY(){
			public String brief(StdItem E){return E.display;}
			public String prompt(StdItem E){return ""+E.display;}
			public void mod(StdItem E, MOB M){E.setDisplayText(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		DESCRIPTION(){
			public String brief(StdItem E){return E.desc;}
			public String prompt(StdItem E){return ""+E.desc;}
			public void mod(StdItem E, MOB M){E.setDescription(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		TEXT(){
			public String brief(StdItem E){return E.miscText;}
			public String prompt(StdItem E){return ""+E.miscText;}
			public void mod(StdItem E, MOB M){E.setMiscText(CMLib.genEd().stringPrompt(M, ""+E.miscText, false));} },
		EFFECTS(){
			public String brief(StdItem E){return ""+E.affects.size();}
			public String prompt(StdItem E){return "";}
			public void mod(StdItem E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(StdItem E){return ""+E.behaviors.size();}
			public String prompt(StdItem E){return "";}
			public void mod(StdItem E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		NAME(){
			public String brief(StdItem E){return ""+E.name;}
			public String prompt(StdItem E){return ""+E.name;}
			public void mod(StdItem E, MOB M){E.setName(CMLib.genEd().stringPrompt(M, ""+E.name, false));} }
		;
		public abstract String brief(StdItem fromThis);
		public abstract String prompt(StdItem fromThis);
		public abstract void mod(StdItem toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdItem)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdItem)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdItem)toThis, M);} }
	public boolean sameAs(Interactable E)
	{
/*TODO
		if(!(E instanceof StdItem)) return false;
		return true;
*/
		return false;
	}
}