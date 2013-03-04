package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

//import org.mozilla.javascript.Context;
//import org.mozilla.javascript.ScriptableObject;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//For a typical carbon-based lifeform
@SuppressWarnings("unchecked")
public class MOBCharStats extends DefaultCharStats
{
	public String ID(){return "MOBCharStats";}
	private static final Stat[] myStatOptions={Stat.REACTIONS, Stat.INTELLIGENCE, Stat.STRENGTH, Stat.PRECISION, Stat.OBSERVATION, Stat.WILLPOWER};
	private static final Save[] mySaveOptions={};
	private static final Points[] myPointOptions={Points.FOCUS};

	{
		stat=new short[]{10, 10, 10, 10, 10, 10};
		save=new short[]{};
		points=new int[]{1000};
		pointsMax=new int[]{1000};
	}

	public Stat[] getStatOptions(){return myStatOptions;}
	public Save[] getSaveOptions(){return mySaveOptions;}
	public Points[] getPointOptions(){return myPointOptions;}

	public int getStatIndex(Stat option)
	{
		switch(option)
		{
			case REACTIONS: return 0;
			case INTELLIGENCE: return 1;
			case STRENGTH: return 2;
			case PRECISION: return 3;
			case OBSERVATION: return 4;
			case WILLPOWER: return 5;
		}
		return -1;
	}
	public int getPointsIndex(Points option)
	{
		switch(option)
		{
			case FOCUS: return 0;
		}
		return -1;
	}
	public int getSaveIndex(Save option)
	{
//		return option.ordinal();
		return -1;
	}

	/*
	public CMObject copyOf()
	{
		//KINDA TODO
		MOBCharStats newOne=new MOBCharStats();
		copyInto(newOne);
		return newOne;
	}
	public void destroy(){}	//TODO?
	public boolean amDestroyed()
	{
		if(parent!=null)
			return parent.amDestroyed();
		return true;
	}
	*/

	/*
	public void recoverTick(Body body)
	{
		//On second thought, this sort of logic should be in race data! Or at least accessible/overwritable by it.
		BodyCharStats sourceStats=null;
		if((body!=null)&&(body.charStats() instanceof BodyCharStats)) sourceStats=(BodyCharStats)body.charStats();
		else return;

		double thirst=sourceStats.getPointsPercent(Points.THIRST)*4;	//Multiplying by 4 to make thirst only matter when dire.
		if(thirst>1) thirst=1; else if(thirst<0) thirst=0;
		double fatigue=sourceStats.getPointsPercent(Points.FATIGUE);
		if(fatigue>1) fatigue=1; else if(fatigue<0) fatigue=0;
		int current=getPoints(Points.FOCUS);
		int target=getMaxPoints(Points.FOCUS);

		current-=(1-thirst*fatigue)*200+(target-current)/4;
		if(current<target)
		{
			current+=5;
			if(current>target) current=target;
		}
		else if(current>target)
		{
			current-=5;
			if(current<target) current=target;
		}

		//TODO: Focus causing fatigue?
		setPoints(Points.FOCUS, current);
		/*if(parent!=null) parent.saveThis();
		sourceStats.saveThis(); //*
	}
	*/


	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
/*
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){if(parent!=null) parent.saveThis();}
	public void prepDefault(){}
*/

	private enum SCode implements CMSavable.SaveEnum{
		STT(){
			public ByteBuffer save(MOBCharStats E){ return CMLib.coffeeMaker().savAShort(E.stat); }
			public int size(){return 2*6;}
			public void load(MOBCharStats E, ByteBuffer S){ E.stat=CMLib.coffeeMaker().loadAShort(S); } },
		PNT(){
			public ByteBuffer save(MOBCharStats E){ return CMLib.coffeeMaker().savAInt(E.points); }
			public int size(){return 4*1;}
			public void load(MOBCharStats E, ByteBuffer S){ E.points=CMLib.coffeeMaker().loadAInt(S); } },
		MPT(){
			public ByteBuffer save(MOBCharStats E){ return CMLib.coffeeMaker().savAInt(E.pointsMax); }
			public int size(){return 4*1;}
			public void load(MOBCharStats E, ByteBuffer S){ E.pointsMax=CMLib.coffeeMaker().loadAInt(S); } },
			;
		public abstract ByteBuffer save(MOBCharStats E);
		public abstract void load(MOBCharStats E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((MOBCharStats)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((MOBCharStats)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		REACTIONS(){
			public String brief(MOBCharStats E){return ""+E.stat[0];}
			public String prompt(MOBCharStats E){return ""+E.stat[0];}
			public void mod(MOBCharStats E, MOB M){E.stat[0]=CMLib.genEd().shortPrompt(M, ""+E.stat[0]);} },
		INTELLIGENCE(){
			public String brief(MOBCharStats E){return ""+E.stat[1];}
			public String prompt(MOBCharStats E){return ""+E.stat[1];}
			public void mod(MOBCharStats E, MOB M){E.stat[1]=CMLib.genEd().shortPrompt(M, ""+E.stat[1]);} },
		STRENGTH(){
			public String brief(MOBCharStats E){return ""+E.stat[2];}
			public String prompt(MOBCharStats E){return ""+E.stat[2];}
			public void mod(MOBCharStats E, MOB M){E.stat[2]=CMLib.genEd().shortPrompt(M, ""+E.stat[2]);} },
		PRECISION(){
			public String brief(MOBCharStats E){return ""+E.stat[3];}
			public String prompt(MOBCharStats E){return ""+E.stat[3];}
			public void mod(MOBCharStats E, MOB M){E.stat[3]=CMLib.genEd().shortPrompt(M, ""+E.stat[3]);} },
		OBSERVATION(){
			public String brief(MOBCharStats E){return ""+E.stat[4];}
			public String prompt(MOBCharStats E){return ""+E.stat[4];}
			public void mod(MOBCharStats E, MOB M){E.stat[4]=CMLib.genEd().shortPrompt(M, ""+E.stat[4]);} },
		WILLPOWER(){
			public String brief(MOBCharStats E){return ""+E.stat[5];}
			public String prompt(MOBCharStats E){return ""+E.stat[5];}
			public void mod(MOBCharStats E, MOB M){E.stat[5]=CMLib.genEd().shortPrompt(M, ""+E.stat[5]);} },
		FOCUS(){
			public String brief(MOBCharStats E){return ""+E.points[0];}
			public String prompt(MOBCharStats E){return ""+E.points[0];}
			public void mod(MOBCharStats E, MOB M){E.points[0]=CMLib.genEd().intPrompt(M, ""+E.points[0]);} },
		FOCUSTARGET(){
			public String brief(MOBCharStats E){return ""+E.pointsMax[0];}
			public String prompt(MOBCharStats E){return ""+E.pointsMax[0];}
			public void mod(MOBCharStats E, MOB M){E.pointsMax[0]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[0]);} },
		;
		public abstract String brief(MOBCharStats fromThis);
		public abstract String prompt(MOBCharStats fromThis);
		public abstract void mod(MOBCharStats toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((MOBCharStats)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((MOBCharStats)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((MOBCharStats)toThis, M);} }
}
