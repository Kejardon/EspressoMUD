package com.planet_ink.coffee_mud.core.interfaces;
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
import java.nio.ByteBuffer;
/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
Interface for objects savable with CoffeeMaker. This is basically everything.

Current format plan
Enums that have ByteBuffer save(CMSavable) and void load(CMSavable, ByteBuffer). These typically just call library functions
in coffeemaker to convert to/from ByteBuffers.
The CMSavable itself will return Enum.values(), or the first of each Enum (to be checkable for .valueOf() and stuff) when asked
	Subobjects really don't have any need to be saved seperately. Their enum data should be included automatically somehow...
 */
@SuppressWarnings("unchecked")
public interface CMSavable extends CMObject
{
	//IMPORTANT NOTE: ALL SAVEENUMS MUST HAVE 3-LETTER NAMES
	//ALL BYTEBUFFERS MUST BE REWOUND BEFORE BEING RETURNED OR SENT
	public static interface SaveEnum
	{
		//save() will be called on CMSubSavables (SaveEnums with size==-1) iff they have no fixed data;
		//in other words save() on CMSubSavables needs to get ALL data for the subobject(including ID in case format mismatch!), as var data for the superobject, and start with a 2
		public ByteBuffer save(CMSavable fromThis);
		public void load(CMSavable toThis, ByteBuffer val);
		public int size();
		public CMSubSavable subObject(CMSavable fromThis);
//		public ByteBuffer namePrefix();
		public String name();	//to autofind enum's name()
	}
	public static interface CMSubSavable extends CMSavable, Ownable
	{
		public ByteBuffer fixedSave();	//This should start its data with a 0
		public ByteBuffer varSave();	//This should start its data with a 1
//		public void loadVar(ByteBuffer val);
//		public void loadFixed(ByteBuffer val);
	}

	public SaveEnum[] totalEnumS();
	public Enum[] headerEnumS();
	public int saveNum();
	public void setSaveNum(int num);

/*
	//Non-saving class
	public SaveEnum[] totalEnumS(){return new SaveEnum[0];}
	public Enum[] headerEnumS(){return new Enum[0];}

	//Typical non-extended CMSavable class
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	
	//Typical extended CMSavable class
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null)
		{
			SaveEnum[] arrA=SCode.values();
			SaveEnum[] arrB=super.totalEnumS();
			SaveEnum[] total=new SaveEnum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			totalEnumS=total;
		}
		return totalEnumS;
	}
	public Enum[] headerEnumS()
	{
		if(headerEnumS==null)
		{
			Enum[] arrA=new Enum[] {SCode.values()[0]};
			Enum[] arrB=super.headerEnumS();
			Enum[] total=new Enum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			headerEnumS=total;
		}
		return headerEnumS;
	}

	//The actual enum/code/parser
	//IMPORTANT NOTES: ALL SAVEENUMS MUST HAVE 3-LETTER NAMES
	//NO MORE THAN 32 INLINED SUBOBJECTS SUPPORTED (things with size==-1)
	private static enum SCode implements SaveEnum{
		DMY() {
			public String save(CMParticular fromThis) {return fromThis.x;}
			public void load(CMParticular toThis, String s) {toThis.x=s.intern();} }
			public int size(){return 0;},
		;
		public abstract String save(CMParticular fromThis);
		public abstract void load(CMParticular toThis, String S);
		public String save(CMSavable E){return save((CMParticular)E);}
		public void load(CMSavable E, String S){load((CMParticular)E, S);} }
*/
}
