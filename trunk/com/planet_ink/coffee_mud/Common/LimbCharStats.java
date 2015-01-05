package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

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

public class LimbCharStats extends DefaultCharStats
{
	@Override public String ID(){return "LimbCharStats";}
	private static final Stat[] myStatOptions={Stat.CONSTITUTION, Stat.PRECISION, Stat.STRENGTH};
	private static final Save[] mySaveOptions={};
	private static final Points[] myPointOptions={Points.FATIGUE};

	{
		stat=new short[]{10, 10, 10};
		statTrain=new short[]{0, 0, 0};
		save=new short[]{};
		points=new int[]{10};
		pointsMax=new int[]{10};
	}

	public Stat[] getStatOptions(){return myStatOptions;}
	public Save[] getSaveOptions(){return mySaveOptions;}
	public Points[] getPointOptions(){return myPointOptions;}

	public int getStatIndex(Stat option)
	{
		switch(option)
		{
			case CONSTITUTION: return 0;
			case PRECISION: return 1;
			case STRENGTH: return 2;
		}
		return -1;
	}
	public int getPointsIndex(Points option)
	{
		switch(option)
		{
			case FATIGUE: return 0;
		}
		return -1;
	}
	public int getSaveIndex(Save option)
	{
		return -1;
	}

	/*
	public CMObject copyOf()
	{
		//KINDA TODO
		LimbCharStats newOne=new LimbCharStats();
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

		double hunger=sourceStats.getPointsPercent(Points.HUNGER);
		if(hunger>1) hunger=1; else if(hunger<0) hunger=0;
		double thirst=sourceStats.getPointsPercent(Points.THIRST);
		if(thirst>1) thirst=1; else if(thirst<0) thirst=0;
		double fatigue=getPointsPercent(Points.FATIGUE);
		if(fatigue>1) fatigue=1; else if(fatigue<0) fatigue=0;
		int con=getStat(Stat.CONSTITUTION);
		if(con<0) con=0;
		int str=getStat(Stat.STRENGTH);
		if(str<0) str=0;

		int thirstCost=(int)(2.5*(1-fatigue));
		int hungerCost=(int)(2.5*(1-fatigue));
		int fatigueRegen=5+(int)Math.round((con+str)*(2-fatigue));
		if(fatigue+0.2<sourceStats.getPointsPercent(Points.FATIGUE))
		{
			int temp=getMaxPoints(Points.FATIGUE)/200;
			fatigueRegen+=temp;
			sourceStats.adjPoints(Points.FATIGUE, -temp);
		}
		sourceStats.adjPoints(Points.THIRST, -thirstCost);
		sourceStats.adjPoints(Points.HUNGER, -hungerCost);
		adjPoints(Points.FATIGUE, fatigueRegen);
		/*if(parent!=null) parent.saveThis();
		sourceStats.saveThis(); //*
	}
	*/

	//CMModifiable and CMSavable
	@Override public SaveEnum[] totalEnumS(){return SCode.values();}
	@Override public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	@Override public ModEnum[] totalEnumM(){return MCode.values();}
	@Override public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	/*
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){if(parent!=null) parent.saveThis();}
	public void prepDefault(){}
	*/

	private enum SCode implements SaveEnum<LimbCharStats>{
		STT(){
			public ByteBuffer save(LimbCharStats E){ return CMLib.coffeeMaker().savAShort(E.stat); }
			public int size(){return 2*3;}
			public void load(LimbCharStats E, ByteBuffer S){ E.stat=CMLib.coffeeMaker().loadAShort(S); } },
		PNT(){
			public ByteBuffer save(LimbCharStats E){ return CMLib.coffeeMaker().savAInt(E.points); }
			public int size(){return 4*1;}
			public void load(LimbCharStats E, ByteBuffer S){ E.points=CMLib.coffeeMaker().loadAInt(S); } },
		MPT(){
			public ByteBuffer save(LimbCharStats E){ return CMLib.coffeeMaker().savAInt(E.pointsMax); }
			public int size(){return 4*1;}
			public void load(LimbCharStats E, ByteBuffer S){ E.pointsMax=CMLib.coffeeMaker().loadAInt(S); } },
		;
		public CMSavable subObject(LimbCharStats fromThis){return null;} }
	private enum MCode implements ModEnum<LimbCharStats>{
		CONSTITUTION(){
			public String brief(LimbCharStats E){return ""+E.stat[0];}
			public String prompt(LimbCharStats E){return ""+E.stat[0];}
			public void mod(LimbCharStats E, MOB M){E.stat[0]=CMLib.genEd().shortPrompt(M, ""+E.stat[0]);} },
		PRECISION(){
			public String brief(LimbCharStats E){return ""+E.stat[1];}
			public String prompt(LimbCharStats E){return ""+E.stat[1];}
			public void mod(LimbCharStats E, MOB M){E.stat[1]=CMLib.genEd().shortPrompt(M, ""+E.stat[1]);} },
		STRENGTH(){
			public String brief(LimbCharStats E){return ""+E.stat[2];}
			public String prompt(LimbCharStats E){return ""+E.stat[2];}
			public void mod(LimbCharStats E, MOB M){E.stat[2]=CMLib.genEd().shortPrompt(M, ""+E.stat[2]);} },
		FATIGUE(){
			public String brief(LimbCharStats E){return ""+E.points[0];}
			public String prompt(LimbCharStats E){return ""+E.points[0];}
			public void mod(LimbCharStats E, MOB M){E.points[0]=CMLib.genEd().intPrompt(M, ""+E.points[0]);} },
		MAXFATIGUE(){
			public String brief(LimbCharStats E){return ""+E.pointsMax[0];}
			public String prompt(LimbCharStats E){return ""+E.pointsMax[0];}
			public void mod(LimbCharStats E, MOB M){E.pointsMax[0]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[0]);} },
		; }
}
