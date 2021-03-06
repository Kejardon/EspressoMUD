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

public interface CMModifiable extends CMObject
{
	public static final ModEnum[] dummyMEArray=new ModEnum[0];

	public static interface ModEnum<U extends CMModifiable>
	{
		public String brief(U fromThis);
		public String prompt(U fromThis);
		public void mod(U toThis, MOB mob);
	}
	//@Override public CMModifiable newInstance();
	//@Override public CMModifiable copyOf();

	public ModEnum[] totalEnumM();
	public Enum[] headerEnumM();

/*
	//Non-modifiable class
	@Override public ModEnum[] totalEnumM(){return CMModifiable.dummyMEArray;}
	@Override public Enum[] headerEnumM(){return CMClass.dummyEnumArray;}

	//Typical non-extended CMModifiable class
	@Override public ModEnum[] totalEnumM(){return MCode.values();}
	@Override public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	//Typical extended CMModifiable class
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
