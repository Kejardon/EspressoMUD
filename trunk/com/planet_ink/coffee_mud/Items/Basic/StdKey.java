package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class StdKey extends StdItem implements Key
{
	protected String key="skeleton";
	public String ID(){	return "StdKey";}
	public StdKey()
	{
		super();
		name="a metal key";
		display="a small metal key sits here.";
		desc="You can't tell what it's to by looking at it.";

//		material=RawMaterial.RESOURCE_STEEL;
//		baseGoldValue=0;
//		recoverEnvStats();
	}

	public void setKey(String keyName){key=keyName; CMLib.database().saveObject(this);}
	public String getKey(){return key;}

	//CMModifiable and CMSavable
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null)
		{
			ModEnum[] arrA=MCode.values();
			ModEnum[] arrB=super.totalEnumM();
			totalEnumM=CMParms.appendToArray(arrA, arrB, ModEnum[].class);
		}
		return totalEnumM;
	}
	public Enum[] headerEnumM()
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
	public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null)
		{
			SaveEnum[] arrA=SCode.values();
			SaveEnum[] arrB=super.totalEnumS();
			totalEnumS=CMParms.appendToArray(arrA, arrB, SaveEnum[].class);
		}
		return totalEnumS;
	}
	public Enum[] headerEnumS()
	{
		if(headerEnumS==null)
		{
			Enum[] arrA=new Enum[] {SCode.values()[0]};
			Enum[] arrB=super.headerEnumS();
			headerEnumS=CMParms.appendToArray(arrA, arrB, Enum[].class);
		}
		return headerEnumS;
	}

	private enum SCode implements CMSavable.SaveEnum{
		KEY(){
			public ByteBuffer save(StdKey E){
				if(E.key=="skeleton") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.key); }
			public int size(){return 0;}
			public void load(StdKey E, ByteBuffer S){ E.key=CMLib.coffeeMaker().loadString(S); } },
		;
		public abstract ByteBuffer save(StdKey E);
		public abstract void load(StdKey E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdKey)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdKey)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		KEYNAME(){
			public String brief(StdKey E){return E.key;}
			public String prompt(StdKey E){return E.key;}
			public void mod(StdKey E, MOB M){E.key=CMLib.genEd().stringPrompt(M, E.key, false);} }
		;
		public abstract String brief(StdKey fromThis);
		public abstract String prompt(StdKey fromThis);
		public abstract void mod(StdKey toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdKey)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdKey)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdKey)toThis, M);} }
}
