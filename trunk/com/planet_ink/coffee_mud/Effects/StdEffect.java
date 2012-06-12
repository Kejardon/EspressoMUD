package com.planet_ink.coffee_mud.Effects;
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
public class StdEffect implements Effect
{
	protected int tickDown=-1;
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	//protected long lastTick=0;

	protected Affectable affected=null;
	protected boolean unInvoked=false;
	protected EnumSet<Flags> myFlags=EnumSet.noneOf(Flags.class);
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected int tickCount;
	
	protected int saveNum=0;
	protected boolean amDestroyed=false;
	protected int[] effectsToLoad=null;

	public String ID() { return "StdEffect"; }
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here)
	{
		if(here==affected)
			here.addListener(this, lFlags);
	}
	public void registerAllListeners()
	{
		if(affected instanceof ListenHolder)
			((ListenHolder)affected).addListener(this, lFlags);
	}
	public void clearAllListeners()
	{
		if(affected instanceof ListenHolder)
			((ListenHolder)affected).removeListener(this, lFlags);
	}
	public void initializeClass() {}
	public StdEffect(){}

	public CMObject newInstance()
	{
		try { return (CMObject)this.getClass().newInstance(); }
		catch(Exception e) { Log.errOut(ID(),e); }
		return new StdEffect();
	}
	public EnumSet<Flags> effectFlags(){ return myFlags; }

	public void startTickDown(Affectable affected, int tickTime)
	{
		affected.addEffect(this);
		tickDown=tickTime;
		CMLib.database().saveObject(this);
	}

	public int compareTo(CMObject o)
	{
		return ID().compareTo(o.ID());
	}
	public Effect copyOnto(Affectable being)
	{
		try
		{
			StdEffect E=(StdEffect)this.clone();
			E.saveNum=0;
			being.addEffect(E);
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			StdEffect E=(StdEffect)this.newInstance();
			E.startTickDown(being, tickDown);
			return E;
		}
	}
	protected void cloneFix(StdEffect E)
	{
		okCheckers=new CopyOnWriteArrayList();
		excCheckers=new CopyOnWriteArrayList();
		tickActers=new CopyOnWriteArrayList();
		tickStatus=Tickable.TickStat.Not;
		myFlags=myFlags.clone();
		lFlags=lFlags.clone();
		//affected=null;
		affects=new CopyOnWriteArrayList();
		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
	}
	public CMObject copyOf()
	{
		try
		{
			StdEffect E=(StdEffect)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public Affectable affecting()
	{
		return affected;
	}
	public void setAffectedOne(Affectable being)
	{
		if(affected instanceof ListenHolder)
			((ListenHolder)affected).removeListener(this, lFlags);
		affected=being;
		if(affected instanceof ListenHolder)
			((ListenHolder)affected).addListener(this, lFlags);
		else if(affected==null)
			destroy();
	}

	public void unInvoke()
	{
		unInvoked=true;
		if(affected!=null)
		{
			affected.delEffect(this);
			setAffectedOne(null);	//this should be redundant because of affected.delEffect but just in case...
		}
	}

	public boolean invoke(Affectable target, int asLevel)
	{
		return true;
	}
	public void affectCharStats(CMObject affected, CharStats stats){}
	public void affectEnvStats(Environmental affected, EnvStats stats){}

	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		for(OkChecker O : okCheckers)
			if(!O.okMessage(myHost,msg))
				return false;
		return true;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		for(ExcChecker E : excCheckers)
			E.executeMsg(myHost,msg);
	}

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
			if(!doTick()) {lFlags.remove(ListenHolder.Flags.TICK); tickCount=0; return false;}
		}
		return true;
	}
	public boolean doTick()
	{
		tickStatus=Tickable.TickStat.Listener;
		if(tickActers!=null)
		for(TickActer T : tickActers)
			if(!T.tick(tickCount))
				tickActers.remove(T);
		tickStatus=Tickable.TickStat.Not;
		if(tickDown!=Integer.MAX_VALUE)
		{
			if((--tickDown)<=0)
			{
				tickDown=-1;
				unInvoke();
				return false;
			}
			CMLib.database().saveObject(this);
			return true;
		}
		return (tickActers.size()>0);
	}
	public int tickCounter(){return tickCount;}
	public void tickAct(){}
	//public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}
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
	//Affectable/Behavable shared
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
	//TODO/NOTE: I might make a flag or something so this code will not clear TICK for effects that want to tick themselves,
	//instead of needing to override these methods
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
			if(affected instanceof ListenHolder)
				((ListenHolder)affected).removeListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
			if(affected instanceof ListenHolder)
				((ListenHolder)affected).addListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}

	public void destroy()
	{
		unInvoke();
		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		clearAllListeners();
		amDestroyed=true;
		if(saveNum!=0)
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	//CMModifiable and CMSavable
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
				saveNum=SIDLib.EFFECT.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.EFFECT.removeNumber(saveNum);
			saveNum=num;
			SIDLib.EFFECT.assignNumber(num, this);
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
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	private enum SCode implements CMSavable.SaveEnum{
		TIC(){
			public ByteBuffer save(StdEffect E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.tickDown).rewind(); }
			public int size(){return 4;}
			public void load(StdEffect E, ByteBuffer S){ E.tickDown=S.getInt(); } },
		EFC(){
			public ByteBuffer save(StdEffect E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdEffect E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		;
		public abstract ByteBuffer save(StdEffect E);
		public abstract void load(StdEffect E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdEffect)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdEffect)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		TICKDOWN(){
			public String brief(StdEffect E){return ""+E.tickDown;}
			public String prompt(StdEffect E){return ""+E.tickDown;}
			public void mod(StdEffect E, MOB M){E.tickDown=CMLib.genEd().intPrompt(M, ""+E.tickDown);} },
		EFFECTS(){
			public String brief(StdEffect E){return ""+E.affects.size();}
			public String prompt(StdEffect E){return "";}
			public void mod(StdEffect E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		;
		public abstract String brief(StdEffect fromThis);
		public abstract String prompt(StdEffect fromThis);
		public abstract void mod(StdEffect toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdEffect)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdEffect)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdEffect)toThis, M);} }
}