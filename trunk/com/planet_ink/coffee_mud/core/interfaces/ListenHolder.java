package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
A ListenHolder has objects it reports to when certain things happen.
A Listener is one of those objects reported to.
*/
@SuppressWarnings("unchecked")
public interface ListenHolder extends Tickable {
	public void addListener(Listener newAffect, EnumSet<Flags> flags);
	public void removeListener(Listener oldAffect, EnumSet<Flags> flags);
//	public void recheckListeners();	//actually uh, not sure if this will work. Maybe this should be used to check for bad listeners?
	public CopyOnWriteArrayList<CharAffecter> charAffecters();
	public CopyOnWriteArrayList<EnvAffecter> envAffecters();
	public CopyOnWriteArrayList<OkChecker> okCheckers();
	public CopyOnWriteArrayList<ExcChecker> excCheckers();
	public CopyOnWriteArrayList<TickActer> tickActers();

	public static enum Flags
	{
		ENV, CHAR, OK, EXC, TICK
	}
	public final static EnumSet<Flags> AllFlags=EnumSet.range(Flags.ENV,Flags.TICK);

	public static interface Listener
	{
		public void registerListeners(ListenHolder forThis);
		public void registerAllListeners();
		public void clearAllListeners();
		public int priority(ListenHolder forThis);
		public EnumSet<Flags> listenFlags();
	}
	public static interface CharAffecter extends Listener
	{ public void affectCharStats(CMObject affected, CharStats stats); }
	public static interface EnvAffecter extends Listener
	{ public void affectEnvStats(Environmental affected, EnvStats stats); }
	public static interface ExcChecker extends Listener
	{ public void executeMsg(ExcChecker myHost, CMMsg msg); }
	public static interface OkChecker extends Listener
	{ public boolean okMessage(OkChecker myHost, CMMsg msg); }
	public static interface MsgListener extends ListenHolder.ExcChecker, ListenHolder.OkChecker
	{ public boolean respondTo(CMMsg msg); }
	public static interface TickActer extends Tickable, Listener {}
	public static interface AllListener extends CharAffecter, EnvAffecter, MsgListener, TickActer {}

	public static class O
	{
		public static void removeListener(ListenHolder onThis, Listener oldAffect, EnumSet<Flags> flags)
		{
			if(flags.contains(Flags.CHAR))
			{
				CopyOnWriteArrayList<CharAffecter> V=onThis.charAffecters();
				if(V!=null)
					V.remove(oldAffect);
			}
			if(flags.contains(Flags.ENV))
			{
				CopyOnWriteArrayList<EnvAffecter> V=onThis.envAffecters();
				if(V!=null)
					V.remove(oldAffect);
			}
			if(flags.contains(Flags.OK))
			{
				CopyOnWriteArrayList<OkChecker> V=onThis.okCheckers();
				if(V!=null)
					V.remove(oldAffect);
			}
			if(flags.contains(Flags.EXC))
			{
				CopyOnWriteArrayList<ExcChecker> V=onThis.excCheckers();
				if(V!=null)
					V.remove(oldAffect);
			}
			if(flags.contains(Flags.TICK))
			{
				CopyOnWriteArrayList<TickActer> V=onThis.tickActers();
				if(V!=null)
					V.remove(oldAffect);
			}
		}
		public static void addListener(ListenHolder onThis, Listener newAffect, EnumSet<Flags> flags)
		{
//			EnumSet<Flags> lFlags=onThis.listenFlags();
			if(flags.contains(Flags.CHAR))
				addListenSub(onThis, onThis.charAffecters(), newAffect, newAffect.priority(onThis));
			if(flags.contains(Flags.ENV))
				addListenSub(onThis, onThis.envAffecters(), newAffect, newAffect.priority(onThis));
			if(flags.contains(Flags.OK))
				addListenSub(onThis, onThis.okCheckers(), newAffect, newAffect.priority(onThis));
			if(flags.contains(Flags.EXC))
				addListenSub(onThis, onThis.excCheckers(), newAffect, newAffect.priority(onThis));
			if(flags.contains(Flags.TICK))
				addListenSub(onThis, onThis.tickActers(), newAffect, newAffect.priority(onThis));
		}
		private static void addListenSub(ListenHolder onThis, CopyOnWriteArrayList V, Listener newAffect, int p)
		{
			if(V==null) return;
			int i=0;
			if(p==Integer.MAX_VALUE)
				V.addIfAbsent(newAffect);
			else synchronized(V)
			{
				while(true)
				{
					int sizeCheck=V.size();
					if(V.contains(newAffect)) return;
					for(;(i<V.size())&&(((Listener)V.get(i)).priority(onThis)<p);i++)
						{}
					V.add(i, newAffect);
					if(sizeCheck==V.size()+1) return;
					V.remove(newAffect);
					i=0;
				}
			}
		}
	}
}