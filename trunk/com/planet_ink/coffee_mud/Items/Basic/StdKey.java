package com.planet_ink.coffee_mud.Items.Basic;
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

public class StdKey extends StdItem implements Key
{
	protected String key="skeleton";
	@Override public String ID(){ return "StdKey"; }
	public StdKey()
	{
		super();
//		material=RawMaterial.RESOURCE_STEEL;
//		baseGoldValue=0;
//		recoverEnvStats();
	}

	public void setKey(String keyName){key=keyName; CMLib.database().saveObject(this);}
	public String getKey(){return key;}
	
	
	@Override public String name(){ return name==""?"a metal key":name;}

	@Override public String displayText(){return display==""?"a small metal key sits here.":display;}
	
	@Override public String description(){return desc==""?"You can't tell what it's to by looking at it.":desc;}

	//CMModifiable and CMSavable
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	@Override public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null) totalEnumM=CMParms.appendToArray(MCode.values(), super.totalEnumM(), ModEnum[].class);
		return totalEnumM;
	}
	@Override public Enum[] headerEnumM()
	{
		if(headerEnumM==null) headerEnumM=CMParms.appendToArray(new Enum[] {MCode.values()[0]}, super.headerEnumM(), Enum[].class);
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	@Override public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null) totalEnumS=CMParms.appendToArray(SCode.values(), super.totalEnumS(), SaveEnum[].class);
		return totalEnumS;
	}
	@Override public Enum[] headerEnumS()
	{
		if(headerEnumS==null) headerEnumS=CMParms.appendToArray(new Enum[] {SCode.values()[0]}, super.headerEnumS(), Enum[].class);
		return headerEnumS;
	}

	private enum SCode implements SaveEnum<StdKey>{
		KEY(){
			public ByteBuffer save(StdKey E){
				if(E.key=="skeleton") return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.key); }
			public int size(){return 0;}
			public void load(StdKey E, ByteBuffer S){ E.key=CMLib.coffeeMaker().loadString(S); } },
		;
		public CMSavable subObject(StdKey fromThis){return null;} }
	private enum MCode implements ModEnum<StdKey>{
		KEYNAME(){
			public String brief(StdKey E){return E.key;}
			public String prompt(StdKey E){return E.key;}
			public void mod(StdKey E, MOB M){E.key=CMLib.genEd().stringPrompt(M, E.key, false);} }
		; }
}
