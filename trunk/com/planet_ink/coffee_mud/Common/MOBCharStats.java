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
public class MOBCharStats implements CharStats
{
	public String ID(){return "MOBCharStats";}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new MOBCharStats();}}
	public void initializeClass(){}
	protected short[] stat={10, 10, 10, 10, 10, 10};
	protected short[] save={};
	protected int[] points={1000};
	protected int[] pointsMax={1000};

//	protected Body myBody=null;

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

	public MOBCharStats() { }
//	public setBody(Body newBody){myBody=newBody;}

	public void copyInto(CharStats intoStats)
	{
		if(intoStats instanceof MOBCharStats)
		{
			MOBCharStats newStats=(MOBCharStats)intoStats;
			copyStatic(newStats);
			for(int i=0; i<points.length; i++)
				newStats.points[i]=points[i];
		}
	}
	public void copyStatic(CharStats intoStats)
	{
		if(intoStats instanceof MOBCharStats)
		{
			MOBCharStats newStats=(MOBCharStats)intoStats;
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
		MOBCharStats newOne=new MOBCharStats();
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
		BodyCharStats sourceStats=null;
		if((body!=null)&&(body.charStats() instanceof BodyCharStats)) sourceStats=(BodyCharStats)body.charStats();
		else return;

		double thirst=sourceStats.getPointsPercent(Points.THIRST)*4;	//Multiplying by 4 to make thirst only matter when dire.
		if(thirst>1) thirst=1; else if(thirst<0) thirst=0;
		double fatigue=sourceStats.getPointsPercent(Points.FATIGUE);
		if(fatigue>1) fatigue=1; else if(fatigue<0) fatigue=0;
		int current=getPoints(Points.FOCUS);
		int target=getPointsMax(Points.FOCUS);

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
		setPoints(Points.FOCUS, current+change);
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
			public String save(MOBCharStats E){ return CMLib.coffeeMaker().savAInt(E.stat); }
			public void load(MOBCharStats E, String S){ E.stat=CMLib.coffeeMaker().loadAInt(S); } },
		PNT(){
			public String save(MOBCharStats E){ return CMLib.coffeeMaker().savAInt(E.points); }
			public void load(MOBCharStats E, String S){ E.points=CMLib.coffeeMaker().loadAInt(S); } },
		MPT(){
			public String save(MOBCharStats E){ return CMLib.coffeeMaker().savAInt(E.pointsMax); }
			public void load(MOBCharStats E, String S){ E.pointsMax=CMLib.coffeeMaker().loadAInt(S); } },
		;
		public abstract String save(MOBCharStats E);
		public abstract void load(MOBCharStats E, String S);
		public String save(CMSavable E){return save((MOBCharStats)E);}
		public void load(CMSavable E, String S){load((MOBCharStats)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		REACTIONS(){
			public String brief(MOBCharStats E){return ""+E.stat[0];}
			public String prompt(MOBCharStats E){return ""+E.stat[0];}
			public void mod(MOBCharStats E, MOB M){E.stat[0]=CMLib.genEd().intPrompt(M, ""+E.stat[0]);} },
		INTELLIGENCE(){
			public String brief(MOBCharStats E){return ""+E.stat[1];}
			public String prompt(MOBCharStats E){return ""+E.stat[1];}
			public void mod(MOBCharStats E, MOB M){E.stat[1]=CMLib.genEd().intPrompt(M, ""+E.stat[1]);} },
		STRENGTH(){
			public String brief(MOBCharStats E){return ""+E.stat[2];}
			public String prompt(MOBCharStats E){return ""+E.stat[2];}
			public void mod(MOBCharStats E, MOB M){E.stat[2]=CMLib.genEd().intPrompt(M, ""+E.stat[2]);} },
		PRECISION(){
			public String brief(MOBCharStats E){return ""+E.stat[3];}
			public String prompt(MOBCharStats E){return ""+E.stat[3];}
			public void mod(MOBCharStats E, MOB M){E.stat[3]=CMLib.genEd().intPrompt(M, ""+E.stat[3]);} },
		OBSERVATION(){
			public String brief(MOBCharStats E){return ""+E.stat[4];}
			public String prompt(MOBCharStats E){return ""+E.stat[4];}
			public void mod(MOBCharStats E, MOB M){E.stat[4]=CMLib.genEd().intPrompt(M, ""+E.stat[4]);} },
		WILLPOWER(){
			public String brief(MOBCharStats E){return ""+E.stat[5];}
			public String prompt(MOBCharStats E){return ""+E.stat[5];}
			public void mod(MOBCharStats E, MOB M){E.stat[5]=CMLib.genEd().intPrompt(M, ""+E.stat[5]);} },
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
