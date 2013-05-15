package com.planet_ink.coffee_mud.core.interfaces;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//Affectables are things that may be modified by Effects
public interface Affectable extends ListenHolder, ListenHolder.MsgListener, ListenHolder.TickActer //, ListenHolder.AllListener
{
	public static final Effect[] dummyEffectArray=new Effect[0];

	public void addEffect(Effect to);
	public void delEffect(Effect to);
	public boolean hasEffect(Effect to);
	public int numEffects();
	public Effect fetchEffect(int index);
	public Vector<Effect> fetchEffect(String ID);
	public Effect fetchFirstEffect(String ID);
	public Iterator<Effect> allEffects();
/*	Typical setup for Affectables below (Shared code and variables often varies! Make sure it's applicable to the object)
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected int[] effectsToLoad=null;

	//Overlapping code
	public void destroy()
	{
		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
	}
	protected void cloneFix(CMObject E)
	{
		affects=new CopyOnWriteArrayList();
		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
	}
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
	private enum SCode implements SaveEnum{
		EFC(){
			public ByteBuffer save(CMSavable E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(CMSavable E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
	}
	private enum MCode implements ModEnum{
		EFFECTS(){
			public String brief(CMModifiable E){return ""+E.affects.size();}
			public String prompt(CMModifiable E){return "";}
			public void mod(CMModifiable E, MOB M){CMLib.genEd().modAffectable(E, M);} },
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
	public Effect fetchFirstEffect(String ID)
	{
		for(Effect E : affects)
			if(E.ID().equals(ID))
				return E;
		return null;
	}
	public Iterator<Effect> allEffects() { return affects.iterator(); }
	//Affectable/Behavable shared
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
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
*/
}
