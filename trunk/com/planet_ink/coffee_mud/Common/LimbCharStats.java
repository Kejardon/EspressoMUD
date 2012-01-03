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
import java.nio.ByteBuffer;

//import org.mozilla.javascript.Context;
//import org.mozilla.javascript.ScriptableObject;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class LimbCharStats implements CharStats
{
	public String ID(){return "LimbCharStats";}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new LimbCharStats();}}
	public void initializeClass(){}
	protected CMSavable parent=null;
	protected short[] stat={10, 10, 10};
	protected short[] save={};
	protected int[] points={10};
	protected int[] pointsMax={10};

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}
//	protected Body myBody=null;

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

	public LimbCharStats() { }
//	public CharStats setBody(Body newBody){myBody=newBody; return this;}

	public void copyInto(CharStats intoStats)
	{
		if(intoStats instanceof LimbCharStats)
		{
			LimbCharStats newStats=(LimbCharStats)intoStats;
			copyStatic(newStats);
			for(int i=0; i<points.length; i++)
				newStats.points[i]=points[i];
			CMLib.database().saveObject(newStats.parent);
		}
	}
	public void copyStatic(CharStats intoStats)
	{
		if(intoStats instanceof LimbCharStats)
		{
			LimbCharStats newStats=(LimbCharStats)intoStats;
			for(int i=0; i<stat.length; i++)
				newStats.stat[i]=stat[i];
			for(int i=0; i<pointsMax.length; i++)
				newStats.pointsMax[i]=pointsMax[i];
			for(int i=0; i<save.length; i++)
				newStats.save[i]=save[i];
			newStats.parent=parent;
		}
	}
	public void resetState()
	{
		for(int i=0; i<points.length; i++)
			points[i]=pointsMax[i];
		CMLib.database().saveObject(parent);
	}

	public CMObject copyOf()
	{
		//KINDA TODO
		LimbCharStats newOne=new LimbCharStats();
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
		if(i>=0) { save[i]=value; CMLib.database().saveObject(parent); }
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
		if(i>=0) { stat[i]=value; CMLib.database().saveObject(parent);}
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
		if(i>=0) {points[i]=newVal; CMLib.database().saveObject(parent); return newVal>pointsMax[i];}
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
				CMLib.database().saveObject(parent);
				return true;
			}
			else if(points[i]<0)
			{
				points[i]=0;
				CMLib.database().saveObject(parent);
				return true;
			}
			CMLib.database().saveObject(parent);
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
		if(i<0) return false;
		pointsMax[i]=newVal;
		CMLib.database().saveObject(parent);
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
				CMLib.database().saveObject(parent);
				return true;
			}
			CMLib.database().saveObject(parent);
			return change;
		}
		return false;
	}
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
		CMLib.database().saveObject(parent);
		sourceStats.saveThis();
	}

//	public void expendEnergy(MOB mob, boolean expendMovement)
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean sameAs(CharStats E)
	{
		if (E.equals(this)) return true;
		return false;
	}
	public void destroy(){}	//TODO?
	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(parent);}

	private enum SCode implements CMSavable.SaveEnum{
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
		public abstract ByteBuffer save(LimbCharStats E);
		public abstract void load(LimbCharStats E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((LimbCharStats)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((LimbCharStats)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
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
		;
		public abstract String brief(LimbCharStats fromThis);
		public abstract String prompt(LimbCharStats fromThis);
		public abstract void mod(LimbCharStats toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((LimbCharStats)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((LimbCharStats)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((LimbCharStats)toThis, M);} }
}
