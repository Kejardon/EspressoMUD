package com.planet_ink.coffee_mud.Behaviors;
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
public class StdBehavior implements Behavior
{
	protected Behavable behaver=null;
	protected String parms="";

	protected long lastTick=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);

	public String ID(){return "StdBehavior";}
	public String name(){return ID();}
//	protected int canImproveCode(){return Behavior.CAN_MOBS;}
//	public long flags(){return 0;}
//	public boolean grantsAggressivenessTo(MOB M){return false;}
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public void initializeClass(){}
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	public void registerAllListeners()
	{if(behaver instanceof ListenHolder) behaver.addListener(this, lFlags);}
	public void clearAllListeners()
	{if(behaver instanceof ListenHolder) behaver.removeListener(this, lFlags);}
	public void affectEnvStats(Environmental affectedEnv, EnvStats affectableStats)
	{}
	public void affectCharStats(CMObject affected, CharStats affectableStats)
	{}
//	protected boolean isSavableBehavior=true;

	public StdBehavior()
	{
		super();
	}

	public Behavable behaver(){return behaver;}

	/** return a new instance of the object*/
	public CMObject newInstance()
	{
		try
		{
			return (Behavior)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdBehavior();
	}
	public CMObject copyOf()
	{
		try
		{
			Behavior B=(Behavior)this.clone();
			B.setParms(getParms());
			return B;
		}
		catch(CloneNotSupportedException e)
		{
			return new StdBehavior();
		}
	}
	public void startBehavior(Behavable forMe){behaver=forMe;}
	protected void finalize(){}
	public String getParms(){return parms;}
	public void setParms(String parameters){parms=parameters;}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		return;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		return true;
	}

	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
		lastTick=System.currentTimeMillis();
		return true;
	}
	public long lastAct(){return 0;}	//No Action ticks
	public long lastTick(){return lastTick;}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
		PRM(){
			public String save(StdBehavior E){ return E.parms; }
			public void load(StdBehavior E, String S){ E.setParms(S.intern()); } },
		;
		public abstract String save(StdBehavior E);
		public abstract void load(StdBehavior E, String S);
		public String save(CMSavable E){return save((StdBehavior)E);}
		public void load(CMSavable E, String S){load((StdBehavior)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		PARMS(){
			public String brief(StdBehavior E){return E.parms;}
			public String prompt(StdBehavior E){return E.parms;}
			public void mod(StdBehavior E, MOB M){E.setParms(CMLib.genEd().stringPrompt(M, ""+E.parms, false));} },
		;
		public abstract String brief(StdBehavior fromThis);
		public abstract String prompt(StdBehavior fromThis);
		public abstract void mod(StdBehavior toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdBehavior)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdBehavior)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdBehavior)toThis, M);} }

	public boolean sameAs(Behavior E)
	{
		if(!(E instanceof StdBehavior)) return false;
//		for(int i=0;i<CODES.length;i++)
//			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
//				return false;
		return true;
	}
}
