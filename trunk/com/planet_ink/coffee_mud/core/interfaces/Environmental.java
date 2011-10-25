package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.*;

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
public interface Environmental extends Affectable, CMModifiable, CMSavable
{
	public static interface EnvHolder extends ListenHolder { public Environmental getEnvObject(); }

	public boolean sameAs(Environmental E);
	public EnvStats baseEnvStats();
	public void setBaseEnvStats(EnvStats newBaseEnvStats);
	public EnvStats envStats();
	public void recoverEnvStats();
	public void destroy();

	public enum SCode implements CMSavable.SaveEnum{
		ENV(){
			public String save(DefaultEnv E){ return CMLib.coffeeMaker().getPropertiesStr(E.baseEnvStats); }
			public void load(DefaultEnv E, String S){
				EnvStats newEnv=(EnvStats)CMClass.Objects.COMMON.get("DefaultEnvStats");
				CMLib.coffeeMaker().setPropertiesStr(newEnv, S);
				E.setBaseEnvStats(newEnv); } },
		EFC(){
			public String save(DefaultEnv E){ return CMLib.coffeeMaker().getVectorStr(E.affects); }
			public void load(DefaultEnv E, String S){
				Vector<Effect> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Effect A : V)
					E.addEffect(A);
				
				} },
		;
		public abstract String save(DefaultEnv E);
		public abstract void load(DefaultEnv E, String S);
		public String save(CMSavable E){return save((DefaultEnv)E);}
		public void load(CMSavable E, String S){load((DefaultEnv)E, S);} }
	public enum MCode implements CMModifiable.ModEnum{
		ENVSTATS(){
			public String brief(DefaultEnv E){return E.baseEnvStats.ID();}
			public String prompt(DefaultEnv E){return "";}
			public void mod(DefaultEnv E, MOB M){ CMLib.genEd().genMiscSet(M, E.baseEnvStats); E.recoverEnvStats();} },
		EFFECTS(){
			public String brief(DefaultEnv E){return ""+E.affects.size();}
			public String prompt(DefaultEnv E){return "";}
			public void mod(DefaultEnv E, MOB M){CMLib.genEd().modAffectable(E, M);} }
		;
		public abstract String brief(DefaultEnv fromThis);
		public abstract String prompt(DefaultEnv fromThis);
		public abstract void mod(DefaultEnv toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultEnv)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultEnv)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultEnv)toThis, M);} }
	public static class DefaultEnv implements Environmental, Ownable
	{
		public static Environmental getFrom(CMObject O)
		{
			if(O instanceof Environmental) return (Environmental)O;
			while(O instanceof Ownable) O=((Ownable)O).getOwner();
			if(O instanceof EnvHolder) return ((EnvHolder)O).getEnvObject();
			return null;
		}

		protected CMObject parent;
		protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
		protected EnvStats baseEnvStats=(EnvStats)CMClass.Objects.COMMON.get("DefaultEnvStats");
		protected EnvStats envStats=(EnvStats)CMClass.Objects.COMMON.get("DefaultEnvStats");
		protected Vector<EnvAffecter> envAffecters=null;
		protected Vector<OkChecker> okCheckers=null;
		protected Vector<ExcChecker> excCheckers=null;
		protected Vector<TickActer> tickActers=null;
		protected Vector affects=new Vector(1);
//		protected Vector behaviors=new Vector(1);
		protected long lastTick=0;
//		protected int actTimer=0;
		protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);

		public DefaultEnv(CMObject O){parent=O;}

		//Ownable
		public CMObject getOwner(){return parent;}

		//Affectable
		public void addEffect(Effect to)
		{
			if(to==null) return;
			affects.addElement(to);
			to.setAffectedOne(this);
		}
		public void delEffect(Effect to)
		{
			int size=affects.size();
			affects.removeElement(to);
			if(affects.size()<size)
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
		public Vector<CharAffecter> charAffecters(){return null;}
		public Vector<EnvAffecter> envAffecters(){if(envAffecters==null) envAffecters=new Vector(); return envAffecters;}
		public Vector<OkChecker> okCheckers(){if(okCheckers==null) okCheckers=new Vector(); return okCheckers;}
		public Vector<ExcChecker> excCheckers(){if(excCheckers==null) excCheckers=new Vector(); return excCheckers;}
		public Vector<TickActer> tickActers(){if(tickActers==null) tickActers=new Vector(); return tickActers;}
		public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
		//Tickable
		public Tickable.TickStat getTickStatus(){return tickStatus;}
		public boolean tick(Tickable ticking, Tickable.TickID tickID)
		{
			if(tickID==Tickable.TickID.Action) return false;
			tickStatus=Tickable.TickStat.Listener;
			for(int i=tickActers.size()-1;i>=0;i--)
			{
				Tickable T=tickActers.get(i);
				if(!T.tick(ticking, tickID))
					tickActers.remove(i-1);
			}
			tickStatus=Tickable.TickStat.Not;
			lastTick=System.currentTimeMillis();
			return true;
		}
		public long lastAct(){return 0;}	//No Action ticks
		public long lastTick(){return lastTick;}
//		public int actTimer(){return actTimer;}
//		public void setActT(int i){actTimer=i;}

		//CMObject. Not meant for use, get the owner!
		public String ID(){return "DefaultEnv";}
		public CMObject newInstance(){return null;}
		public CMObject copyOf(){return null;}
		public void initializeClass(){}
		public int compareTo(CMObject O){return 0;}

		//MsgListener
		public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
		{
			for(int i=excCheckers.size();i>0;i--)
				excCheckers.get(i-1).executeMsg(this,msg);
		}
		public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
		{
			for(int i=okCheckers.size();i>0;i--)
				if(!okCheckers.get(i-1).okMessage(this,msg))
					return false;
			return true;
		}
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

		//Environmental
		public EnvStats baseEnvStats(){return baseEnvStats;}
		public void setBaseEnvStats(EnvStats newBaseEnvStats){baseEnvStats=newBaseEnvStats; recoverEnvStats();}
		public EnvStats envStats(){return envStats;}
		public void recoverEnvStats()
		{
			baseEnvStats.copyInto(envStats);
			for(int i=envAffecters.size();i>0;i--)
				envAffecters.get(i-1).affectEnvStats(this,envStats);
		}
		public void destroy()
		{
			while(affects.size()>0)
				delEffect(fetchEffect(0));
			clearAllListeners();
		}
		public boolean sameAs(Environmental E)
		{
			if(ID().equals(E.ID()))
				return true;
			return false;
		}
	}
}
