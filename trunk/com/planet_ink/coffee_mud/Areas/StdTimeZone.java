package com.planet_ink.coffee_mud.Areas;
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
public class StdTimeZone extends StdArea
{
	public String ID(){	return "StdTimeZone";}

	public StdTimeZone()
	{
		super();
		myClock = (TimeClock)CMClass.getCommon("DefaultTimeClock");
	}

	public CMObject copyOf()
	{
		CMObject O=super.copyOf();
		if(O instanceof Area) ((Area)O).setTimeObj((TimeClock)CMClass.getCommon("DefaultTimeClock"));
		return O;
	}

	public TimeClock getTimeObj(){return myClock;}
	public void setName(String newName)
	{
		super.setName(newName);
		myClock.setLoadName(newName);
	}

	public void addChild(Area Adopted) {
		super.addChild(Adopted);
		Adopted.setTimeObj(getTimeObj());
	}
	public void initChildren() {
		super.initChildren();
		if(children!=null)
			for(int i=0;i<children.size();i++)
				children.elementAt(i).setTimeObj(getTimeObj());
	}
	//CMModifiable and CMSavable
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	public SaveEnum[] totalEnumM()
	{
		if(totalEnumM==null)
			totalEnumM=(ModEnum[])CMath.combineArrays(MCode.values(), super.totalEnumM());
		return totalEnumM;
	}
	public Enum[] headerEnumM()
	{
		if(headerEnumM==null)
			headerEnumM=CMath.combineArrays(new Enum[] {MCode.values()[0]}, super.headerEnumM());
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null)
			totalEnumS=(SaveEnum[])CMath.combineArrays(SCode.values(), super.totalEnumS());
		return totalEnumS;
	}
	public Enum[] headerEnumS()
	{
		if(headerEnumS==null)
			headerEnumS=CMath.combineArrays(new Enum[] {SCode.values()[0]}, super.headerEnumS());
		return headerEnumS;
	}
	private enum SCode implements CMSavable.SaveEnum{
		TIM(){
			public String save(StdTimeZone E){ return CMLib.coffeeMaker().getPropertiesStr(E.myClock); }
			public void load(StdTimeZone E, String S){
				TimeClock newClock=(TimeClock)CMClass.getCommon("DefaultTimeClock");
				CMLib.coffeeMaker().setPropertiesStr(newClock, S);
				E.myClock.destroy();	//TODO: Is this line appropriate? More to the point, is this how clocks should be saved/loaded/modified?
				E.myClock=newClock; } }
		;
		public abstract String save(StdTimeZone E);
		public abstract void load(StdTimeZone E, String S);
		public String save(CMSavable E){return save((StdTimeZone)E);}
		public void load(CMSavable E, String S){load((StdTimeZone)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		TIMEOBJECT(){
			public String brief(StdTimeZone E){return E.myClock.ID();}
			public String prompt(StdTimeZone E){return "";}
			public void mod(StdTimeZone E, MOB M){CMLib.genEd().genMiscSet(M, E.myClock);} }
		;
		public abstract String brief(StdTimeZone fromThis);
		public abstract String prompt(StdTimeZone fromThis);
		public abstract void mod(StdTimeZone toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdTimeZone)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdTimeZone)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdTimeZone)toThis, M);} }

}
