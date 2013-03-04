package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//Behavables are things that may have some sort of AI-like behavior
public interface Behavable extends ListenHolder, ListenHolder.TickActer
{
	public static final Behavior[] dummyBehaviorArray=new Behavior[0];

	public void addBehavior(Behavior to);
	public void delBehavior(Behavior to);
	public boolean hasBehavior(String ID);
	public int numBehaviors();
	public Behavior fetchBehavior(int index);
	public Behavior fetchBehavior(String ID);
	public Iterator<Behavior> allBehaviors();

/*	Typical setup for Behavables below (Shared code and variables often varies! Make sure it's applicable to the object)
	protected CopyOnWriteArrayList<Behavior> behaviors=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected int[] behavesToLoad=null;

	//Overlapping code
	public void destroy()
	{
		for(Behavior B : behaviors)
			B.startBehavior(null);
		behaviors.clear();
	}
	protected void cloneFix(CMObject E)
	{
		behaviors=new CopyOnWriteArrayList();
		for(Behavior B : E.behaviors)
			addBehavior((Behavior)B.copyOf());
	}
	public void link()
	{
		if(behavesToLoad!=null)
		{
			for(int SID : behavesToLoad)
			{
				Behavior to = SIDLib.BEHAVIOR.get(SID);
				if(to==null) continue;
				B.startBehavior(this);
				behaviors.add(to);
			}
			behavesToLoad=null;
		}
	}
	private enum SCode implements CMSavable.SaveEnum{
		BHV(){
			public ByteBuffer save(CMSavable E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(CMSavable E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
	}
	private enum MCode implements CMModifiable.ModEnum{
		BEHAVIORS(){
			public String brief(CMModifiable E){return ""+E.behaviors.size();}
			public String prompt(CMModifiable E){return "";}
			public void mod(CMModifiable E, MOB M){CMLib.genEd().modBehavable(E, M);} },
	}

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