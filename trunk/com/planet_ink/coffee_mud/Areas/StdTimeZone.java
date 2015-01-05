package com.planet_ink.coffee_mud.Areas;
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

public class StdTimeZone extends StdArea
{
	@Override public String ID(){	return "StdTimeZone";}

	public StdTimeZone(){}
	public void destroy()
	{
		amDestroyed=true;
		if(myClock!=null) myClock.destroy();
		super.destroy();
	}

	public void cloneFix(StdArea E)
	{
		super.cloneFix(E);
		if(E.myClock!=null)
			myClock=(TimeClock)E.myClock.copyOf();
	}

	public void setTimeObj(TimeClock obj){myClock=obj; CMLib.database().saveObject(this);}
	public TimeClock getTimeObj()
	{
		if(myClock==null)
			synchronized(this){if(myClock==null) myClock=(TimeClock)((Ownable)CMClass.COMMON.getNew("DefaultTimeClock")).setOwner(this);}
		return myClock;
	}

	public void addChild(Area Adopted) {
		super.addChild(Adopted);
		Adopted.setTimeObj(getTimeObj());
	}
	//CMModifiable and CMSavable
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	@Override public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null)
		{
			ModEnum[] arrA=MCode.values();
			ModEnum[] arrB=super.totalEnumM();
			totalEnumM=CMParms.appendToArray(arrA, arrB, ModEnum[].class);
		}
		return totalEnumM;
	}
	@Override public Enum[] headerEnumM()
	{
		if(headerEnumM==null)
		{
			Enum[] arrA=new Enum[] {MCode.values()[0]};
			Enum[] arrB=super.headerEnumM();
			headerEnumM=CMParms.appendToArray(arrA, arrB, Enum[].class);
		}
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	@Override public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null)
		{
			SaveEnum[] arrA=SCode.values();
			SaveEnum[] arrB=super.totalEnumS();
			totalEnumS=CMParms.appendToArray(arrA, arrB, SaveEnum[].class);
		}
		return totalEnumS;
	}
	@Override public Enum[] headerEnumS()
	{
		if(headerEnumS==null)
		{
			Enum[] arrA=new Enum[] {SCode.values()[0]};
			Enum[] arrB=super.headerEnumS();
			headerEnumS=CMParms.appendToArray(arrA, arrB, Enum[].class);
		}
		return headerEnumS;
	}
	public void link()	//Hijack this to ensure children's timeobject
	{
		if(childrenToLoad!=null)
		{
			for(int SID : childrenToLoad)
			{
				Area Adopted = SIDLib.AREA.get(SID);
				if(Adopted==null) continue;
				Adopted.addParent(this);
				children.add(Adopted);
				Adopted.setTimeObj(getTimeObj());
			}
			childrenToLoad=null;
		}
		super.link();
	}

	private enum SCode implements SaveEnum<StdTimeZone>{
		TIM(){
			public ByteBuffer save(StdTimeZone E){
				if(E.myClock==null) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.myClock); }
			public int size(){return -1;}
			public CMSavable subObject(StdTimeZone fromThis){return fromThis.myClock;}
			public void load(StdTimeZone E, ByteBuffer S){
				TimeClock old=E.myClock;
				E.myClock=(TimeClock)((Ownable)CMLib.coffeeMaker().loadSub(S, E, this)).setOwner(E);
				if((old!=null)&&(old!=E.myClock)) old.destroy(); } }
		;
		public CMSavable subObject(StdTimeZone fromThis){return null;} }
	private enum MCode implements ModEnum<StdTimeZone>{
		TIMEOBJECT(){
			public String brief(StdTimeZone E){return E.myClock.ID();}
			public String prompt(StdTimeZone E){return "";}
			public void mod(StdTimeZone E, MOB M){CMLib.genEd().genMiscSet(M, E.myClock);} }
		; }

}
