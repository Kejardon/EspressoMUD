package com.planet_ink.coffee_mud.Common;
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
import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class BodyCharStats implements CharStats
{
	public String ID(){return "BodyCharStats";}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new BodyCharStats();}}
	public void initializeClass(){}
	protected short[] stat={10, 10, 10, 10, 10, 10};
	protected short[] save={};
	protected int[] points={10, 10, 10, 10, 10};
	protected int[] pointsMax={10, 10, 10, 10, 10};

//	protected Body myBody=null;

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

	public BodyCharStats() { }
//	public setBody(Body newBody){myBody=newBody;}

	public void copyInto(CharStats intoStats)
	{
		if(intoStats instanceof BodyCharStats)
		{
			BodyCharStats newStats=(BodyCharStats)intoStats;
			copyStatic(newStats);
			for(int i=0; i<points.length; i++)
				newStats.points[i]=points[i];
		}
	}
	public void copyStatic(CharStats intoStats)
	{
		if(intoStats instanceof BodyCharStats)
		{
			BodyCharStats newStats=(BodyCharStats)intoStats;
			for(int i=0; i<stat.length; i++)
				newStats.stat[i]=stat[i];
			for(int i=0; i<pointsMax.length; i++)
				newStats.pointsMax[i]=pointsMax[i];
			for(int i=0; i<save.length; i++)
				newStats.save[i]=save[i];
		}
	}
	public void resetState()
	{
		for(int i=0; i<points.length; i++)
			points[i]=pointsMax[i];
	}

	public CMObject copyOf()
	{
		//KINDA TODO
		BodyCharStats newOne=new BodyCharStats();
		copyInto(newOne);
		return newOne;
	}

	public short getSave(Save option)
	{
		int i=getSaveIndex(option);
		if(i>=0) return save[i];
		return -1;
	}
	public void setSave(Save option, short value)
	{
		int i=getSaveIndex(option);
		if(i>=0) save[i]=value;
	}

	public short getStat(Stat option)
	{
		int i=getStatIndex(option);
		if(i>=0) return stat[i];
		return -1;
	}

	public void setStat(Stat option, short value)
	{
		int i=getStatIndex(option);
		if(i>=0) stat[i]=value;
	}

	public int getPoints(Points option)
	{
		int i=getPointsIndex(option);
		if(i>=0) return points[i];
		return -1;
	}
	public double getPointsPercent(Points option)
	{
		int i=getPointsIndex(option);
		if(i>=0) return ((double)points[i])/pointsMax[i];
		return -1.0;
	}
	public boolean setPoints(Points option, int newVal)	//Return if it broke a min or max cap, do not cap yourself
	{
		int i=getPointsIndex(option);
		if(i>=0) {points[i]=newVal; return newVal>pointsMax[i];}
		return false;
	}
	public boolean adjPoints(Points option, int byThisMuch)	//Cap, return if cap did something
	{
		int i=getPointsIndex(option);
		if(i>=0)
		{
			points[i]+=byThisMuch;
			if(points[i]>pointsMax[i])
			{
				points[i]=pointsMax[i];
				return true;
			}
			else if(points[i]<0)
			{
				points[i]=0;
				return true;
			}
		}
		return false;
	}

	public int getMaxPoints(Points option)
	{
		int i=getPointsIndex(option);
		if(i>=0) return pointsMax[i];
		return -1;
	}
	public boolean setMaxPoints(Points option, int newVal)	//Return if reduced below current, do not cap yourself
	{
		int i=getPointsIndex(option);
		if(i>=0) pointsMax[i]=newVal;
		if(newVal<points[i])
			return true;
		return false;
	}
	public boolean adjMaxPoints(Points option, int byThisMuch)	//Cap, return if cap did something
	{
		int i=getPointsIndex(option);
		if(i>=0)
		{
			boolean change=false;
			pointsMax[i]+=byThisMuch;
			if(pointsMax[i]<0)
			{
				pointsMax[i]=0;
				change=true;
			}
			if(points[i]>pointsMax[i])
			{
				points[i]=pointsMax[i];
				return true;
			}
			return change;
		}
		return false;
	}
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
	}

//	public void expendEnergy(MOB mob, boolean expendMovement)
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean sameAs(CharStats E)
	{
		if (E.equals(this)) return true;
		return false;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
		STT(){
			public String save(BodyCharStats E){ return CMLib.coffeeMaker().savAShort(E.stat); }
			public int size(){return 2*E.stat.length;}
			public void load(BodyCharStats E, String S){ E.stat=CMLib.coffeeMaker().loadAShort(S); } },
		PNT(){
			public String save(BodyCharStats E){ return CMLib.coffeeMaker().savAInt(E.points); }
			public int size(){return 4*E.points.length;}
			public void load(BodyCharStats E, String S){ E.points=CMLib.coffeeMaker().loadAInt(S); } },
		MPT(){
			public String save(BodyCharStats E){ return CMLib.coffeeMaker().savAInt(E.pointsMax); }
			public int size(){return 4*E.pointsMax.length;}
			public void load(BodyCharStats E, String S){ E.pointsMax=CMLib.coffeeMaker().loadAInt(S); } },
		;
		public abstract String save(BodyCharStats E);
		public abstract void load(BodyCharStats E, String S);
		public String save(CMSavable E){return save((BodyCharStats)E);}
		public void load(CMSavable E, String S){load((BodyCharStats)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
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
		;
		public abstract String brief(BodyCharStats fromThis);
		public abstract String prompt(BodyCharStats fromThis);
		public abstract void mod(BodyCharStats toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((BodyCharStats)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((BodyCharStats)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((BodyCharStats)toThis, M);} }
}
