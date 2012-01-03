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
import java.nio.ByteBuffer;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class StdBehavior implements Behavior
{
	protected Behavable behaver=null;
	protected String parms="";

	protected long lastTick=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	
	protected int saveNum=0;

	public String ID(){return "StdBehavior";}
	public String name(){return ID();}
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
			StdBehavior B=(StdBehavior)this.clone();
			B.saveNum=0;
			B.setParms(getParms());
			return B;
		}
		catch(CloneNotSupportedException e)
		{
			return new StdBehavior();
		}
	}
	public void startBehavior(Behavable forMe){behaver=forMe; if(behaver==null) CMLib.database().deleteObject(this);}
	protected void finalize(){}
	public String getParms(){return parms;}
	public void setParms(String parameters){parms=parameters; CMLib.database().saveObject(this); }
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

	public void destroy()
	{
		//TODO
		CMLib.database().deleteObject(this);
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(behaver!=null))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.Objects.BEHAVIOR.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.Objects.BEHAVIOR.removeNumber(saveNum);
			saveNum=num;
			SIDLib.Objects.BEHAVIOR.assignNumber(num, this);
		}
	}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(this);}

	private enum SCode implements CMSavable.SaveEnum{
		PRM(){
			public ByteBuffer save(StdBehavior E){ return CMLib.coffeeMaker().savString(E.parms); }
			public int size(){return 0;}
			public void load(StdBehavior E, ByteBuffer S){ E.parms=CMLib.coffeeMaker().loadString(S); } },
		;
		public abstract ByteBuffer save(StdBehavior E);
		public abstract void load(StdBehavior E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdBehavior)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdBehavior)E, S);} }
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
