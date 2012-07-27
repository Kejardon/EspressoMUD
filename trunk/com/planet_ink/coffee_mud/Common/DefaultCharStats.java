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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
@SuppressWarnings("unchecked")
public class DefaultCharStats implements CharStats
{
	public String ID(){return "DefaultCharStats";}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCharStats();}}
	public void initializeClass(){}
	protected CMSavable parent=null;
	protected short[] stat;
	protected short[] save;
	protected int[] points;
	protected int[] pointsMax;

	public Stat[] getStatOptions(){return dummyStatArray;}
	public Save[] getSaveOptions(){return dummySaveArray;}
	public Points[] getPointOptions(){return dummyPointsArray;}

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}
//	protected Body myBody=null;

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

	public void copyInto(CharStats intoStats)
	{
		if(intoStats.ID()==ID())
		{
			DefaultCharStats newStats=(DefaultCharStats)intoStats;
			copyStatic(newStats);
			for(int i=0; i<points.length; i++)
				newStats.points[i]=points[i];
			if(newStats.parent!=null)
				newStats.parent.saveThis();
		}
	}
	public void copyStatic(CharStats intoStats)
	{
		if(intoStats.ID()==ID())
		{
			DefaultCharStats newStats=(DefaultCharStats)intoStats;
			for(int i=0; i<stat.length; i++)
				newStats.stat[i]=stat[i];
			for(int i=0; i<pointsMax.length; i++)
				newStats.pointsMax[i]=pointsMax[i];
			for(int i=0; i<save.length; i++)
				newStats.save[i]=save[i];
			newStats.parent=parent;
			if(parent!=null) parent.saveThis();
		}
	}
	public void resetState()
	{
		for(int i=0; i<points.length; i++)
			points[i]=pointsMax[i];
		if(parent!=null) parent.saveThis();
	}

	public CMObject copyOf()
	{
		//KINDA TODO
		DefaultCharStats newOne=(DefaultCharStats)newInstance();
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
		if(i>=0) { save[i]=value; if(parent!=null) parent.saveThis(); }
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
		if(i>=0) {stat[i]=value; if(parent!=null) parent.saveThis();}
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
		if(i>=0) {points[i]=newVal; if(parent!=null) parent.saveThis(); return newVal>pointsMax[i];}
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
				if(parent!=null) parent.saveThis();
				return true;
			}
			else if(points[i]<0)
			{
				points[i]=0;
				if(parent!=null) parent.saveThis();
				return true;
			}
			if(parent!=null) parent.saveThis();
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
		if(parent!=null) parent.saveThis();
		return newVal<points[i];
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
				if(parent!=null) parent.saveThis();
				return true;
			}
			if(parent!=null) parent.saveThis();
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
		/*if(parent!=null) parent.saveThis();
		sourceStats.saveThis(); //*
	} */

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean sameAs(CharStats E)
	{
		if (E.equals(this)) return true;
		return false;
	}

	public void destroy(){}	//TODO?
	public boolean amDestroyed()
	{
		if(parent!=null)
			return parent.amDestroyed();
		return true;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return CMSavable.dummySEArray;}
	public Enum[] headerEnumS(){return CMClass.dummyEnumArray;}
	public ModEnum[] totalEnumM(){return CMModifiable.dummyMEArray;}
	public Enum[] headerEnumM(){return CMClass.dummyEnumArray;}
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){if(parent!=null) parent.saveThis();}
	public void prepDefault(){}

	/*
	private enum SCode implements CMSavable.SaveEnum{
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
	private enum MCode implements CMModifiable.ModEnum{
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
