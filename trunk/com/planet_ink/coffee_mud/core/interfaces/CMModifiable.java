package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
// Interface for objects editable with CMGenEditor. This is basically everything.
@SuppressWarnings("unchecked")
public interface CMModifiable extends CMObject
{
	public static final ModEnum[] dummyMEArray=new ModEnum[0];

	public static interface ModEnum
	{
		public String brief(CMModifiable fromThis);
		public String prompt(CMModifiable fromThis);
		public void mod(CMModifiable toThis, MOB mob);
	}

	public ModEnum[] totalEnumM();
	public Enum[] headerEnumM();

/*
	//Non-modifiable class
	public ModEnum[] totalEnumM(){return CMModifiable.dummyMEArray;}
	public Enum[] headerEnumM(){return CMClass.dummyEnumArray;}

	//Typical non-extended CMModifiable class
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	//Typical extended CMModifiable class
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null)
		{
			ModEnum[] arrA=MCode.values();
			ModEnum[] arrB=super.totalEnumM();
			ModEnum[] total=new ModEnum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			totalEnumM=total;
		}
		return totalEnumM;
	}
	public Enum[] headerEnumM()
	{
		if(headerEnumM==null)
		{
			Enum[] arrA=new Enum[] {MCode.values()[0]};
			Enum[] arrB=super.headerEnumM();
			Enum[] total=new Enum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			headerEnumM=total;
		}
		return headerEnumM;
	}
	//The actual enum/code/parser
	public static enum MCode implements ModEnum{
		DUMMY() {
			public String brief(CMParticular E){return E.string();}
			public String prompt(CMParticular E){return E.string();}
			public String def(CMParticular E){return E.string();}
			public void mod(CMParticular E, MOB M){E.setString(S);} },

		;
		public abstract String brief(CMParticular fromThis);
		public abstract String prompt(CMParticular fromThis);
		public abstract String def(CMParticular fromThis);
		public abstract void write(CMParticular toThis, String S);
		public String brief(CMModifiable fromThis){return brief((CMParticular)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((CMParticular)fromThis);}
		public String def(CMModifiable fromThis){return def((CMParticular)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((CMParticular)toThis, M);} }
*/
}
