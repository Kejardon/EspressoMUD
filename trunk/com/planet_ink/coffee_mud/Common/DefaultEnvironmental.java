package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import com.planet_ink.coffee_mud.Effects.Temperature;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class DefaultEnvironmental implements Environmental, Ownable
{
	protected CMSavable parent;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected int tickCount=0;
	//This is ok for now because there is no alternative to DefaultEnvStats
	protected EnvStats baseEnvStats=(EnvStats)(new DefaultEnvStats().setOwner(this));
	protected EnvStats envStats=(EnvStats)(new DefaultEnvStats().setOwner(this));
	protected CopyOnWriteArrayList<EnvAffecter> envAffecters=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	//protected long lastTick=0;
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	protected int[] effectsToLoad=null;
	protected int lastTemperature=-1;

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	//Affectable
	public void addEffect(Effect to)
	{
		affects.add(to);
		to.setAffectedOne(this);
		if(parent!=null)parent.saveThis();
	}
	public void delEffect(Effect to)
	{
		if(affects.remove(to))
		{
			to.setAffectedOne(null);
			if(parent!=null)parent.saveThis();
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
	public Effect fetchFirstEffect(String ID)
	{
		for(Effect E : affects)
			if(E.ID().equals(ID))
				return E;
		return null;
	}
	public Iterator<Effect> allEffects() { return affects.iterator(); }
	
	//Affectable/Behavable shared
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
			if(parent instanceof ListenHolder)
				((ListenHolder)parent).removeListener(this, EnumSet.of(ListenHolder.Flags.TICK));
		if((flags.contains(ListenHolder.Flags.OK))&&(okCheckers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.OK)))
			if(parent instanceof ListenHolder)
				((ListenHolder)parent).removeListener(this, EnumSet.of(ListenHolder.Flags.OK));
		if((flags.contains(ListenHolder.Flags.EXC))&&(excCheckers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.EXC)))
			if(parent instanceof ListenHolder)
				((ListenHolder)parent).removeListener(this, EnumSet.of(ListenHolder.Flags.EXC));
	}
	public void addListener(Listener newAffect, EnumSet<ListenHolder.Flags> flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
			if(parent instanceof ListenHolder)
				((ListenHolder)parent).addListener(this, EnumSet.of(ListenHolder.Flags.TICK));
		if((flags.contains(ListenHolder.Flags.OK))&&(!okCheckers.isEmpty())&&(lFlags.add(ListenHolder.Flags.OK)))
			if(parent instanceof ListenHolder)
				((ListenHolder)parent).addListener(this, EnumSet.of(ListenHolder.Flags.OK));
		if((flags.contains(ListenHolder.Flags.EXC))&&(!excCheckers.isEmpty())&&(lFlags.add(ListenHolder.Flags.EXC)))
			if(parent instanceof ListenHolder)
				((ListenHolder)parent).addListener(this, EnumSet.of(ListenHolder.Flags.EXC));
	}
	public void registerAllListeners()
	{
		if(parent instanceof ListenHolder)
			((ListenHolder)parent).addListener(this, lFlags);
	}
	public void clearAllListeners()
	{
		if(parent instanceof ListenHolder)
			((ListenHolder)parent).removeListener(this, lFlags);
	}
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return envAffecters;}
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
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
			if(!doTick()) {lFlags.remove(ListenHolder.Flags.TICK); tickCount=0; return false;}
		}
		return true;
	}
	public int tickCounter(){return tickCount;}
	public void tickAct(){}
	protected boolean doTick()
	{
		tickStatus=Tickable.TickStat.Listener;
		for(Tickable T : tickActers)
			if(!T.tick(tickCount))
				tickActers.remove(T);
		tickStatus=Tickable.TickStat.Not;
		return (tickActers.size()>0);
	}
	//public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}
//	public int actTimer(){return actTimer;}
//	public void setActT(int i){actTimer=i;}

	//CMObject
	public String ID(){return "DefaultEnvironmental";}
	public CMObject newInstance(){return new DefaultEnvironmental();}
	public CMObject copyOf()
	{
		try
		{
			DefaultEnvironmental E=(DefaultEnvironmental)this.clone();
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	protected void cloneFix(DefaultEnvironmental E)
	{
		//parent=null;	//Undecided if this is appropriate or not.
		tickStatus=Tickable.TickStat.Not;
		tickCount=0;
		envAffecters=new CopyOnWriteArrayList();
		okCheckers=new CopyOnWriteArrayList();
		excCheckers=new CopyOnWriteArrayList();
		tickActers=new CopyOnWriteArrayList();
		affects=new CopyOnWriteArrayList();
		lFlags=lFlags.clone();

		baseEnvStats=(EnvStats)((Ownable)E.baseEnvStats.copyOf()).setOwner(this);
		envStats=(EnvStats)baseEnvStats.copyOf();

		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
	}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//MsgListener
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		for(ExcChecker E : excCheckers)
			E.executeMsg(myHost,msg);
	}
	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		for(OkChecker O : okCheckers)
			if(!O.okMessage(myHost,msg))
				return false;
		return true;
	}
	public boolean respondTo(CMMsg msg, Object data){return true;}
	public boolean respondTo(CMMsg msg){return true;}	//shouldn't actually ever be called
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here)
	{
		if(here==parent)
			here.addListener(this, lFlags);
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
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
	public void saveThis(){if(parent!=null)parent.saveThis();}
	public void prepDefault(){}	//baseEnvStats will already default fine.

	//Environmental
	protected int parentTemperature()
	{
		CMObject obj = CMLib.map().goUpOne(parent);
		if(obj instanceof Area)
			return ((Area)obj).ambientTemperature();
		Environmental env = Environmental.O.getFrom(obj);
		if(env!=null)
			return env.temperature();
		return -1;
	}
	public int temperature()
	{
		//TODO
		if(lastTemperature==-1)
			lastTemperature=parentTemperature();
		return lastTemperature;
	}
	public void setTemperature(int temperature)
	{
		if(lastTemperature == temperature) return;
		//TODO: Handle children
		int reactTemperature=Integer.MAX_VALUE;
		long boil;
		long melt;
		if(envStats.isComposite())
		{
			WVector.CachedLists<RawMaterial.Resource> materials=envStats.materialSet().lists();
			boil=0;
			melt=0;
			for(int i=0;i<materials.objects.length;i++)
			{
				RawMaterial.Resource mat = materials.objects[i];
				int weight = materials.weights[i];
				if(mat.reaction.temperature < temperature)
				{ //React

				}
				boil+=weight*mat.phases.boiling;
				melt+=weight*mat.phases.melting;
			}
			boil/=materials.total;
			melt/=materials.total;
		}
		else
		{
			RawMaterial.Resource mat=envStats.material();
			if(mat.reaction.temperature < temperature)
			{ //React
				
			}
			boil=mat.phases.boiling;
			melt=mat.phases.melting;
		}
		
		if(boil < temperature && boil > lastTemperature && fetchFirstEffect("VaporPhase")==null)
		{ //Boil to vapor
			Effect e = CMClass.EFFECT.getNew("VaporPhase");
			if(e!=null)
				e.invoke(this, 0);
		}
		else if(melt < temperature && fetchFirstEffect("LiquidPhase")==null)
		{
			Effect e = CMClass.EFFECT.getNew("LiquidPhase");
			if(e!=null)
				e.invoke(this, 0);
			/*
			if(melt > lastTemperature)
			{ //Melt to liquid
				
			}
			else if(boil < lastTemperature)
			{ //Condense to liquid
				
			}
			*/
		}
		else if(melt < lastTemperature)
		{ //Solidify to solid
			Effect e = fetchFirstEffect("VaporPhase");
			if(e!=null)
				e.unInvoke();
			e = fetchFirstEffect("LiquidPhase");
			if(e!=null)
				e.unInvoke();
		}
		
		lastTemperature=temperature;
	}
	public EnvStats baseEnvStats(){return baseEnvStats;}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=(EnvStats)((Ownable)newBaseEnvStats).setOwner(this);
		envStats=(EnvStats)newBaseEnvStats.copyOf();
		recoverEnvStats();
		if(parent!=null)parent.saveThis();
	}
	public EnvStats envStats(){return envStats;}
	public void recoverEnvStats()
	{
		baseEnvStats.copyInto(envStats);
		for(EnvAffecter E : envAffecters)
			E.affectEnvStats(this,envStats);
	}
	public void destroy()
	{
		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		clearAllListeners();
	}
	public boolean amDestroyed()
	{
		if(parent!=null)
			return parent.amDestroyed();
		return true;
	}
	public boolean sameAs(Environmental E)
	{
		if(ID().equals(E.ID()))
			return true;
		return false;
	}

	private enum SCode implements CMSavable.SaveEnum{
		TMP(){
			public ByteBuffer save(DefaultEnvironmental E){ return (ByteBuffer)ByteBuffer.allocate(4).putInt(E.lastTemperature).rewind(); }
			public int size(){return 4;}
			public void load(DefaultEnvironmental E, ByteBuffer S){ E.lastTemperature = S.getInt(); } },
		ENV(){
			public ByteBuffer save(DefaultEnvironmental E){ return CMLib.coffeeMaker().savSubFull(E.baseEnvStats); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((DefaultEnvironmental)fromThis).baseEnvStats;}
			public void load(DefaultEnvironmental E, ByteBuffer S){
				E.setBaseEnvStats((EnvStats)CMLib.coffeeMaker().loadSub(S, E, this)); } },
		EFC(){
			public ByteBuffer save(DefaultEnvironmental E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(DefaultEnvironmental E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		;
		public abstract ByteBuffer save(DefaultEnvironmental E);
		public abstract void load(DefaultEnvironmental E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((DefaultEnvironmental)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((DefaultEnvironmental)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		ENVSTATS(){
			public String brief(DefaultEnvironmental E){return E.baseEnvStats.ID();}
			public String prompt(DefaultEnvironmental E){return "";}
			public void mod(DefaultEnvironmental E, MOB M){ CMLib.genEd().genMiscSet(M, E.baseEnvStats); E.recoverEnvStats();} },
		EFFECTS(){
			public String brief(DefaultEnvironmental E){return ""+E.affects.size();}
			public String prompt(DefaultEnvironmental E){return "";}
			public void mod(DefaultEnvironmental E, MOB M){CMLib.genEd().modAffectable(E, M);} }
		;
		public abstract String brief(DefaultEnvironmental fromThis);
		public abstract String prompt(DefaultEnvironmental fromThis);
		public abstract void mod(DefaultEnvironmental toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultEnvironmental)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultEnvironmental)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultEnvironmental)toThis, M);} }
}