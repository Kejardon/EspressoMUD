package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class StdBehavior implements Behavior
{
	protected Behavable behaver=null;
	protected String parms="";

	//protected long lastTick=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected int tickCount=0;
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	
	protected int saveNum=0;
	protected boolean amDestroyed=false;

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
	public void affectEnvStats(Environmental affectedEnv, EnvStats affectableStats){}
	public void affectCharStats(CMObject affected, CharStats affectableStats){}

	public StdBehavior() {}

	public Behavable behaver(){return behaver;}

	public StdBehavior newInstance() { try{return getClass().newInstance();}catch(Exception e){Log.errOut("StdBehavior", e);} return new StdBehavior(); }
	public void cloneFix(StdBehavior E)
	{
		//parms=E.getParms();	//Not actually necessary as Strings are immutable
	}
	public StdBehavior copyOf()
	{
		try
		{
			StdBehavior B=(StdBehavior)this.clone();
			B.saveNum=0;
			B.cloneFix(this);
			return B;
		}
		catch(CloneNotSupportedException e) { return newInstance(); }
	}
	public void startBehavior(Behavable forMe)
	{
		clearAllListeners();
		behaver=forMe;
		if(behaver==null)
			destroy();
		else
			registerAllListeners();
	}
	public String getParms(){return parms;}
	public void setParms(String parameters){parms=parameters; CMLib.database().saveObject(this); }
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		return;
	}
	public boolean respondTo(CMMsg msg, Object data){return true;}
	public boolean respondTo(CMMsg msg){return true;}
	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		return true;
	}

	public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {lFlags.remove(ListenHolder.Flags.TICK); tickCount=0; return false;}
		}
		return true;
	}
	public int tickCounter(){return tickCount;}
	public void tickAct(){}
	protected boolean doTick(){return false;}
//	public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	public void destroy()
	{
		amDestroyed=true;
		clearAllListeners();
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
		if((saveNum==0)&&(amDestroyed!=true))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.BEHAVIOR.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.BEHAVIOR.removeNumber(saveNum);
			saveNum=num;
			SIDLib.BEHAVIOR.assignNumber(num, this);
		}
	}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	private enum SCode implements SaveEnum<StdBehavior>{
		PRM(){
			public ByteBuffer save(StdBehavior E){ return CMLib.coffeeMaker().savString(E.parms); }
			public int size(){return 0;}
			public void load(StdBehavior E, ByteBuffer S){ E.parms=CMLib.coffeeMaker().loadString(S); } },
		;
		public CMSavable subObject(StdBehavior fromThis){return null;} }
	private enum MCode implements ModEnum<StdBehavior>{
		PARMS(){
			public String brief(StdBehavior E){return E.parms;}
			public String prompt(StdBehavior E){return E.parms;}
			public void mod(StdBehavior E, MOB M){E.setParms(CMLib.genEd().stringPrompt(M, ""+E.parms, false));} },
		; }
/*
	public boolean sameAs(Behavior E)
	{
		if(!(E instanceof StdBehavior)) return false;
		return true;
	}
*/
}
