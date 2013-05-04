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
public class BodyCharStats extends DefaultCharStats
{
	public String ID(){return "BodyCharStats";}
	private static final Stat[] myStatOptions={Stat.CONSTITUTION, Stat.REACTIONS, Stat.INTELLIGENCE, Stat.STRENGTH, Stat.PRECISION, Stat.OBSERVATION};
	private static final Save[] mySaveOptions={};
	private static final Points[] myPointOptions={Points.FATIGUE, Points.HIT, Points.MANA, Points.HUNGER, Points.THIRST};

	{
		stat=new short[]{10, 10, 10, 10, 10, 10};
		save=new short[]{};
		points=new int[]{10, 10, 10, 10, 10};
		pointsMax=new int[]{10, 10, 10, 10, 10};
	}

	public Stat[] getStatOptions(){return myStatOptions;}
	public Save[] getSaveOptions(){return mySaveOptions;}
	public Points[] getPointOptions(){return myPointOptions;}

	public int getStatIndex(Stat option)
	{
		switch(option)
		{
			case CONSTITUTION: return 0;
			case REACTIONS: return 1;
			case INTELLIGENCE: return 2;
			case STRENGTH: return 3;
			case PRECISION: return 4;
			case OBSERVATION: return 5;
		}
		return -1;
	}
	public int getPointsIndex(Points option)
	{
		switch(option)
		{
			case FATIGUE: return 0;
			case HIT: return 1;
			case MANA: return 2;
			case HUNGER: return 3;
			case THIRST: return 4;
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
		BodyCharStats newOne=new BodyCharStats();
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
		BodyCharStats sourceStats=this;
		if((body!=null)&&(body.charStats() instanceof BodyCharStats)) sourceStats=(BodyCharStats)body.charStats();

		double hunger=sourceStats.getPointsPercent(Points.HUNGER);
		if(hunger>1) hunger=1; else if(hunger<0) hunger=0;
		double thirst=sourceStats.getPointsPercent(Points.THIRST);
		if(thirst>1) thirst=1; else if(thirst<0) thirst=0;
		double fatigue=getPointsPercent(Points.FATIGUE);
		if(fatigue>1) fatigue=1; else if(fatigue<0) fatigue=0;
		double damage=1-getPointsPercent(Points.HIT);
		if(damage>1) damage=1; else if(damage<0) damage=0;
		int con=getStat(Stat.CONSTITUTION);
		if(con<0) con=0;
		int wil=0;
		if(body.mob()!=null)
		{
			wil=body.mob().charStats().getStat(Stat.WILLPOWER);
			if(wil<0) wil=0;
		}
		int str=getStat(Stat.STRENGTH);
		if(str<0) str=0;

		int thirstCost=(int)(1+(thirst>hunger?10*damage:5*damage)+4*(1-fatigue));
		int hungerCost=(int)(1+(hunger>thirst?10*damage:5*damage)+4*(1-fatigue));
		int hpRegen=5+(int)Math.round(5*con*damage);
		hpRegen*=hunger;
		hpRegen*=thirst;
		int mpRegen=1+2*wil;
		int fatigueRegen=5+(int)Math.round((con+str)*(1-damage)*(2-fatigue));
		if(fatigue+0.2<sourceStats.getPointsPercent(Points.FATIGUE))	//only possible if one body is leaching off of another~
		{
			int temp=getMaxPoints(Points.FATIGUE)/200;
			fatigueRegen+=temp;
			sourceStats.adjPoints(Points.FATIGUE, -temp);
		}
		sourceStats.adjPoints(Points.THIRST, -thirstCost);
		sourceStats.adjPoints(Points.HUNGER, -hungerCost);
		adjPoints(Points.HIT, hpRegen);
		adjPoints(Points.MANA, mpRegen);
		adjPoints(Points.FATIGUE, fatigueRegen);
		//if(parent!=null) parent.saveThis();
		//sourceStats.saveThis();
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
	
	private enum SCode implements SaveEnum<BodyCharStats>{
		STT(){
			public ByteBuffer save(BodyCharStats E){ return CMLib.coffeeMaker().savAShort(E.stat); }
			public int size(){return 2*6;}
			public void load(BodyCharStats E, ByteBuffer S){ E.stat=CMLib.coffeeMaker().loadAShort(S); } },
		PNT(){
			public ByteBuffer save(BodyCharStats E){ return CMLib.coffeeMaker().savAInt(E.points); }
			public int size(){return 4*5;}
			public void load(BodyCharStats E, ByteBuffer S){ E.points=CMLib.coffeeMaker().loadAInt(S); } },
		MPT(){
			public ByteBuffer save(BodyCharStats E){ return CMLib.coffeeMaker().savAInt(E.pointsMax); }
			public int size(){return 4*5;}
			public void load(BodyCharStats E, ByteBuffer S){ E.pointsMax=CMLib.coffeeMaker().loadAInt(S); } },
		;
		public CMSavable subObject(BodyCharStats fromThis){return null;} }
	private enum MCode implements ModEnum<BodyCharStats>{
		CONSTITUTION(){
			public String brief(BodyCharStats E){return ""+E.stat[0];}
			public String prompt(BodyCharStats E){return ""+E.stat[0];}
			public void mod(BodyCharStats E, MOB M){E.stat[0]=CMLib.genEd().shortPrompt(M, ""+E.stat[0]);} },
		REACTIONS(){
			public String brief(BodyCharStats E){return ""+E.stat[1];}
			public String prompt(BodyCharStats E){return ""+E.stat[1];}
			public void mod(BodyCharStats E, MOB M){E.stat[1]=CMLib.genEd().shortPrompt(M, ""+E.stat[1]);} },
		INTELLIGENCE(){
			public String brief(BodyCharStats E){return ""+E.stat[2];}
			public String prompt(BodyCharStats E){return ""+E.stat[2];}
			public void mod(BodyCharStats E, MOB M){E.stat[2]=CMLib.genEd().shortPrompt(M, ""+E.stat[2]);} },
		STRENGTH(){
			public String brief(BodyCharStats E){return ""+E.stat[3];}
			public String prompt(BodyCharStats E){return ""+E.stat[3];}
			public void mod(BodyCharStats E, MOB M){E.stat[3]=CMLib.genEd().shortPrompt(M, ""+E.stat[3]);} },
		PRECISION(){
			public String brief(BodyCharStats E){return ""+E.stat[4];}
			public String prompt(BodyCharStats E){return ""+E.stat[4];}
			public void mod(BodyCharStats E, MOB M){E.stat[4]=CMLib.genEd().shortPrompt(M, ""+E.stat[4]);} },
		OBSERVATION(){
			public String brief(BodyCharStats E){return ""+E.stat[5];}
			public String prompt(BodyCharStats E){return ""+E.stat[5];}
			public void mod(BodyCharStats E, MOB M){E.stat[5]=CMLib.genEd().shortPrompt(M, ""+E.stat[5]);} },
		FATIGUE(){
			public String brief(BodyCharStats E){return ""+E.points[0];}
			public String prompt(BodyCharStats E){return ""+E.points[0];}
			public void mod(BodyCharStats E, MOB M){E.points[0]=CMLib.genEd().intPrompt(M, ""+E.points[0]);} },
		MAXFATIGUE(){
			public String brief(BodyCharStats E){return ""+E.pointsMax[0];}
			public String prompt(BodyCharStats E){return ""+E.pointsMax[0];}
			public void mod(BodyCharStats E, MOB M){E.pointsMax[0]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[0]);} },
		HITPOINTS(){
			public String brief(BodyCharStats E){return ""+E.points[1];}
			public String prompt(BodyCharStats E){return ""+E.points[1];}
			public void mod(BodyCharStats E, MOB M){E.points[1]=CMLib.genEd().intPrompt(M, ""+E.points[1]);} },
		MAXHITPOINTS(){
			public String brief(BodyCharStats E){return ""+E.pointsMax[1];}
			public String prompt(BodyCharStats E){return ""+E.pointsMax[1];}
			public void mod(BodyCharStats E, MOB M){E.pointsMax[1]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[1]);} },
		MANA(){
			public String brief(BodyCharStats E){return ""+E.points[2];}
			public String prompt(BodyCharStats E){return ""+E.points[2];}
			public void mod(BodyCharStats E, MOB M){E.points[2]=CMLib.genEd().intPrompt(M, ""+E.points[2]);} },
		MAXMANA(){
			public String brief(BodyCharStats E){return ""+E.pointsMax[2];}
			public String prompt(BodyCharStats E){return ""+E.pointsMax[2];}
			public void mod(BodyCharStats E, MOB M){E.pointsMax[2]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[2]);} },
		HUNGER(){
			public String brief(BodyCharStats E){return ""+E.points[3];}
			public String prompt(BodyCharStats E){return ""+E.points[3];}
			public void mod(BodyCharStats E, MOB M){E.points[3]=CMLib.genEd().intPrompt(M, ""+E.points[3]);} },
		MAXHUNGER(){
			public String brief(BodyCharStats E){return ""+E.pointsMax[3];}
			public String prompt(BodyCharStats E){return ""+E.pointsMax[3];}
			public void mod(BodyCharStats E, MOB M){E.pointsMax[3]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[3]);} },
		THIRST(){
			public String brief(BodyCharStats E){return ""+E.points[4];}
			public String prompt(BodyCharStats E){return ""+E.points[4];}
			public void mod(BodyCharStats E, MOB M){E.points[4]=CMLib.genEd().intPrompt(M, ""+E.points[4]);} },
		MAXTHIRST(){
			public String brief(BodyCharStats E){return ""+E.pointsMax[4];}
			public String prompt(BodyCharStats E){return ""+E.pointsMax[4];}
			public void mod(BodyCharStats E, MOB M){E.pointsMax[4]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[4]);} },
		; }
}
