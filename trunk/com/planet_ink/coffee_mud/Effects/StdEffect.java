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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

@SuppressWarnings("unchecked")
public class StdEffect implements Effect
{
	protected int tickDown=-1;
	protected Vector affects=new Vector(1);
	protected long lastTick=0;

	protected Affectable affected=null;
	protected boolean unInvoked=false;
	protected EnumSet<Flags> myFlags=EnumSet.noneOf(Flags.class);
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	protected Vector<OkChecker> okCheckers=null;
	protected Vector<ExcChecker> excCheckers=null;
	protected Vector<TickActer> tickActers=null;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;

	public String ID() { return "StdEffect"; }
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
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
	public void registerListeners(Affectable forThis)
	{
		if(forThis==affected)
			forThis.addListener(this, lFlags);
	}
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
		if(affected.fetchEffect(ID())==null)
			affected.addEffect(this);
		tickDown=tickTime;
	}

	public int compareTo(CMObject o)
	{
		return ID().compareTo(o.ID());
	}
	protected void cloneFix(Effect E){}
	public CMObject copyOf()
	{
		try
		{
			StdEffect E=(StdEffect)this.clone();
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
		affected=being;
	}

	public void unInvoke()
	{
		unInvoked=true;
		if(affected==null) return;
		affected.delEffect(this);
	}

	public boolean invoke(Affectable target, int asLevel)
	{
		return true;
	}
	public void affectCharStats(CMObject affected, CharStats stats){}
	public void affectEnvStats(Environmental affected, EnvStats stats){}

	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		for(int i=okCheckers.size();i>0;i--)
			if(!okCheckers.get(i-1).okMessage(myHost,msg))
				return false;
		return true;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		for(int i=excCheckers.size();i>0;i--)
			excCheckers.get(i-1).executeMsg(myHost,msg);
	}

	//Tickable
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
		if(tickID==Tickable.TickID.Action) return false;
		tickStatus=Tickable.TickStat.Listener;
		for(int i=tickActers.size()-1;i>=0;i--)
		{
			TickActer T=tickActers.get(i);
			if(!T.tick(ticking, tickID))
				removeListener(T, EnumSet.of(ListenHolder.Flags.TICK));
		}
		tickStatus=Tickable.TickStat.Not;
		lastTick=System.currentTimeMillis();
		if(tickDown!=Integer.MAX_VALUE)
		{
			if((--tickDown)<=0)
			{
				tickDown=-1;
				unInvoke();
				return false;
			}
		}
		return true;
	}
	public long lastAct(){return 0;}	//No Action ticks
	public long lastTick(){return lastTick;}
	//Affectable
	public void addEffect(Effect to)
	{
		if(to==null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Effect to)
	{
		if(affects.removeElement(to))
			to.setAffectedOne(null);
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

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
		TIC(){
			public String save(StdEffect E){ return ""+E.tickDown; }
			public void load(StdEffect E, String S){ E.tickDown=Integer.parseInt(S); } },
		EFC(){
			public String save(StdEffect E){ return CMLib.coffeeMaker().getVectorStr(E.affects); }
			public void load(StdEffect E, String S){
				Vector<Effect> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Effect A : V)
					E.addEffect(A);
				
				} },
		;
		public abstract String save(StdEffect E);
		public abstract void load(StdEffect E, String S);
		public String save(CMSavable E){return save((StdEffect)E);}
		public void load(CMSavable E, String S){load((StdEffect)E, S);} }
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
