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
/*
	NOTE: This class should NEVER be instantiated except for CMClass's reference!
*/

public abstract class DefaultCharStats implements CharStats
{
	@Override public String ID(){return "DefaultCharStats";}

	@Override public DefaultCharStats newInstance(){try{return getClass().newInstance();}catch(Exception e){return null;}}
	@Override public void initializeClass(){}
	protected CMObject parent=null;
	protected short[] stat;
	protected short[] statTrain;
	protected short[] save;
	protected int[] points;
	protected int[] pointsMax;

	@Override public Stat[] getStatOptions(){return dummyStatArray;}
	@Override public Save[] getSaveOptions(){return dummySaveArray;}
	@Override public Points[] getPointOptions(){return dummyPointsArray;}

	//Ownable
	@Override public CMObject owner(){return parent;}
	@Override public Ownable setOwner(CMObject owner){parent=owner; return this;}
//	protected Body myBody=null;

	/*
	public int getStatIndex(Stat option)
	{
		return -1;
	}
	public int getPointsIndex(Points option)
	{
		return -1;
	}
	public int getSaveIndex(Save option)
	{
		return -1;
	}
	public DefaultCharStats() { }
	*/


	@Override public void copyInto(CharStats intoStats)
	{
		if(intoStats.ID()==ID())
		{
			DefaultCharStats newStats=(DefaultCharStats)intoStats;
			copyStatic(newStats);
			for(int i=0; i<points.length; i++)
				newStats.points[i]=points[i];
			newStats.saveThis();
		}
	}
	@Override public void copyStatic(CharStats intoStats)
	{
		if(intoStats.ID()==ID())
		{
			DefaultCharStats newStats=(DefaultCharStats)intoStats;
			for(int i=0; i<statTrain.length; i++)
				newStats.statTrain[i]=statTrain[i];
			for(int i=0; i<stat.length; i++)
				newStats.stat[i]=stat[i];
			for(int i=0; i<pointsMax.length; i++)
				newStats.pointsMax[i]=pointsMax[i];
			for(int i=0; i<save.length; i++)
				newStats.save[i]=save[i];
			newStats.parent=parent;
			saveThis();
		}
	}
	@Override public void resetState()
	{
		for(int i=0; i<points.length; i++)
			points[i]=pointsMax[i];
		saveThis();
	}

	@Override public DefaultCharStats copyOf()
	{
		//KINDA TODO
		DefaultCharStats newOne=(DefaultCharStats)newInstance();
		copyInto(newOne);
		return newOne;
	}

	@Override public short getSave(Save option)
	{
		int i=getSaveIndex(option);
		if(i>=0) return save[i];
		return -1;
	}
	@Override public void setSave(Save option, short value)
	{
		int i=getSaveIndex(option);
		if(i>=0) { save[i]=value; saveThis(); }
	}

	@Override public short getTrain(Stat option)
	{
		int i=getStatIndex(option);
		if(i>=0) return statTrain[i];
		return -1;
	}
	@Override public void setTrain(Stat option, short value)
	{
		int i=getStatIndex(option);
		if(i>=0) { statTrain[i]=value; saveThis(); }
	}

	@Override public short getStat(Stat option)
	{
		int i=getStatIndex(option);
		if(i>=0) return stat[i];
		return -1;
	}

	@Override public void setStat(Stat option, short value)
	{
		int i=getStatIndex(option);
		if(i>=0) {stat[i]=value; saveThis();}
	}

	@Override public int getPoints(Points option)
	{
		int i=getPointsIndex(option);
		if(i>=0) return points[i];
		return -1;
	}
	@Override public double getPointsPercent(Points option)
	{
		int i=getPointsIndex(option);
		if(i>=0) return ((double)points[i])/pointsMax[i];
		return -1.0;
	}
	@Override public boolean setPoints(Points option, int newVal)	//Return if it broke a min or max cap, do not cap yourself
	{
		int i=getPointsIndex(option);
		if(i>=0) {points[i]=newVal; saveThis(); return newVal>pointsMax[i];}
		return false;
	}
	@Override public boolean adjPoints(Points option, int byThisMuch)	//Cap, return if cap did something
	{
		int i=getPointsIndex(option);
		if(i>=0)
		{
			points[i]+=byThisMuch;
			if(points[i]>pointsMax[i])
			{
				points[i]=pointsMax[i];
				saveThis();
				return true;
			}
			else if(points[i]<0)
			{
				points[i]=0;
				saveThis();
				return true;
			}
			saveThis();
		}
		return false;
	}

	@Override public int getMaxPoints(Points option)
	{
		int i=getPointsIndex(option);
		if(i>=0) return pointsMax[i];
		return -1;
	}
	@Override public boolean setMaxPoints(Points option, int newVal)	//Return if reduced below current, do not cap yourself
	{
		int i=getPointsIndex(option);
		if(i<0) return false;
		pointsMax[i]=newVal;
		saveThis();
		return newVal<points[i];
	}
	@Override public boolean adjMaxPoints(Points option, int byThisMuch)	//Cap, return if cap did something
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
				saveThis();
				return true;
			}
			saveThis();
			return change;
		}
		return false;
	}
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
		/*saveThis();
		sourceStats.saveThis(); //*
	} */

	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	@Override public boolean sameAs(CharStats E)
	{
		if (E.equals(this)) return true;
		return false;
	}

	@Override public void destroy(){}	//TODO?
	@Override public boolean amDestroyed()
	{
		if(parent instanceof CMSavable)
			return ((CMSavable)parent).amDestroyed();
		return true;
	}

	//CMModifiable and CMSavable
	@Override public SaveEnum[] totalEnumS(){return CMSavable.dummySEArray;}
	@Override public Enum[] headerEnumS(){return CMClass.dummyEnumArray;}
	@Override public ModEnum[] totalEnumM(){return CMModifiable.dummyMEArray;}
	@Override public Enum[] headerEnumM(){return CMClass.dummyEnumArray;}
	@Override public int saveNum(){return 0;}
	@Override public void setSaveNum(int num){}
	@Override public boolean needLink(){return false;}
	@Override public void link(){}
	@Override public void saveThis(){if(parent instanceof CMSavable) ((CMSavable)parent).saveThis();}
	@Override public void prepDefault(){}

	/*
	private enum SCode implements SaveEnum{
		STT(){
			public ByteBuffer save(DefaultCharStats E){ return CMLib.coffeeMaker().savAShort(E.stat); }
			public int size(){return 2*6;}
			public void load(DefaultCharStats E, ByteBuffer S){ E.stat=CMLib.coffeeMaker().loadAShort(S); } },
		PNT(){
			public ByteBuffer save(DefaultCharStats E){ return CMLib.coffeeMaker().savAInt(E.points); }
			public int size(){return 4*1;}
			public void load(DefaultCharStats E, ByteBuffer S){ E.points=CMLib.coffeeMaker().loadAInt(S); } },
		MPT(){
			public ByteBuffer save(DefaultCharStats E){ return CMLib.coffeeMaker().savAInt(E.pointsMax); }
			public int size(){return 4*1;}
			public void load(DefaultCharStats E, ByteBuffer S){ E.pointsMax=CMLib.coffeeMaker().loadAInt(S); } },
			;
		public abstract ByteBuffer save(DefaultCharStats E);
		public abstract void load(DefaultCharStats E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((DefaultCharStats)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((DefaultCharStats)E, S);} }
	private enum MCode implements ModEnum{
		REACTIONS(){
			public String brief(DefaultCharStats E){return ""+E.stat[0];}
			public String prompt(DefaultCharStats E){return ""+E.stat[0];}
			public void mod(DefaultCharStats E, MOB M){E.stat[0]=CMLib.genEd().shortPrompt(M, ""+E.stat[0]);} },
		INTELLIGENCE(){
			public String brief(DefaultCharStats E){return ""+E.stat[1];}
			public String prompt(DefaultCharStats E){return ""+E.stat[1];}
			public void mod(DefaultCharStats E, MOB M){E.stat[1]=CMLib.genEd().shortPrompt(M, ""+E.stat[1]);} },
		STRENGTH(){
			public String brief(DefaultCharStats E){return ""+E.stat[2];}
			public String prompt(DefaultCharStats E){return ""+E.stat[2];}
			public void mod(DefaultCharStats E, MOB M){E.stat[2]=CMLib.genEd().shortPrompt(M, ""+E.stat[2]);} },
		PRECISION(){
			public String brief(DefaultCharStats E){return ""+E.stat[3];}
			public String prompt(DefaultCharStats E){return ""+E.stat[3];}
			public void mod(DefaultCharStats E, MOB M){E.stat[3]=CMLib.genEd().shortPrompt(M, ""+E.stat[3]);} },
		OBSERVATION(){
			public String brief(DefaultCharStats E){return ""+E.stat[4];}
			public String prompt(DefaultCharStats E){return ""+E.stat[4];}
			public void mod(DefaultCharStats E, MOB M){E.stat[4]=CMLib.genEd().shortPrompt(M, ""+E.stat[4]);} },
		WILLPOWER(){
			public String brief(DefaultCharStats E){return ""+E.stat[5];}
			public String prompt(DefaultCharStats E){return ""+E.stat[5];}
			public void mod(DefaultCharStats E, MOB M){E.stat[5]=CMLib.genEd().shortPrompt(M, ""+E.stat[5]);} },
		FOCUS(){
			public String brief(DefaultCharStats E){return ""+E.points[0];}
			public String prompt(DefaultCharStats E){return ""+E.points[0];}
			public void mod(DefaultCharStats E, MOB M){E.points[0]=CMLib.genEd().intPrompt(M, ""+E.points[0]);} },
		FOCUSTARGET(){
			public String brief(DefaultCharStats E){return ""+E.pointsMax[0];}
			public String prompt(DefaultCharStats E){return ""+E.pointsMax[0];}
			public void mod(DefaultCharStats E, MOB M){E.pointsMax[0]=CMLib.genEd().intPrompt(M, ""+E.pointsMax[0]);} },
		;
		public abstract String brief(DefaultCharStats fromThis);
		public abstract String prompt(DefaultCharStats fromThis);
		public abstract void mod(DefaultCharStats toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultCharStats)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultCharStats)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultCharStats)toThis, M);} }
	*/
}
