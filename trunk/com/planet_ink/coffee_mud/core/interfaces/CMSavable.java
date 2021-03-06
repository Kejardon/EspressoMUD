package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

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

public interface CMSavable extends CMObject
{
	//IMPORTANT NOTE: ALL SAVEENUMS MUST HAVE 3-LETTER NAMES
	//ALL BYTEBUFFERS MUST BE REWOUND BEFORE BEING RETURNED OR SENT
	public static final CMSavable[] dummyCMSavableArray=new CMSavable[0];
	public static final SaveEnum[] dummySEArray=new SaveEnum[0];
	public static interface SaveEnum<U extends CMSavable>
	{
		//save() will be called on CMSubSavables (SaveEnums with size==-1) iff they have no fixed data;
		//in other words save() on CMSubSavables needs to get ALL data for the subobject(including ID in case format mismatch!), as var data for the superobject, and start with a 2
		public ByteBuffer save(U fromThis);
		public void load(U toThis, ByteBuffer val);
		//If positive, this is a fixed size field that will always take a certain amount of space.
		//If -1, this is intended to be saved as part of a parent object. ONLY USABLE FOR TYPES THAT NEVER CHANGE
		//If 0, this may have variable size
		public int size();
		public CMSavable subObject(U fromThis);
//		public ByteBuffer namePrefix();
		public String name();	//to autofind enum's name()
		//public boolean isActive();
		//Probably TODO. A useful routine to check if this object exists in a meaningful manner after being created.
		//i.e. Effects having an affected, items having a container, MOBs having a body...
		//OTOH unusual, interesting things may break such a routine...
	}
/*	public static interface CMSubSavable extends CMSavable, Ownable
	{
		public ByteBuffer fixedSave();	//This should start its data with a <s>0</s>3 ? 0==null, 1=var, 2=full, 3=fixed?
		public ByteBuffer varSave();	//This should start its data with a 1
//		public void loadVar(ByteBuffer val);
//		public void loadFixed(ByteBuffer val);
	}
*/
	//@Override public CMSavable newInstance();
	//@Override public CMSavable copyOf();
	public SaveEnum[] totalEnumS();
	public Enum[] headerEnumS();
	public int saveNum();
	public void setSaveNum(int num);
	public boolean needLink();	//True if this object saves references to other objects via SIDs
	public void link();	//Parse saved SID references and find other objects
	public void saveThis();
	//This should do its best to delete ALL references to the object other than the database's.
	//Ownable objects' destroy should be called only by their owner's destroy - hence it's ok to leave references between the ownable and its owner.
	//If an ownable is destroyed but not its owner, it should still be handled by the owner, and the owner should clean up the ownable's references.
	public void destroy();
	public boolean amDestroyed();
	public void prepDefault();	//For the database; initialize subobjects to most common/expected default value


/*
	//Non-saving class
	@Override public SaveEnum[] totalEnumS(){return CMSavable.dummySEArray;}
	@Override public Enum[] headerEnumS(){return CMClass.dummyEnumArray;}

	//Typical non-extended CMSavable class
	@Override public SaveEnum[] totalEnumS(){return SCode.values();}
	@Override public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	
	//Typical extended CMSavable class
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
	//The actual enum/code/parser
	//IMPORTANT NOTES: ALL SAVEENUMS MUST HAVE 3-LETTER NAMES
	private static enum SCode implements SaveEnum{
		DMY() {
			public ByteBuffer save(CMParticular fromThis) {return fromThis.x;}
			public void load(CMParticular toThis, ByteBuffer s) {toThis.x=s.intern();}
			public int size(){return 0;} },
		;
		public abstract ByteBuffer save(CMParticular fromThis);
		public abstract void load(CMParticular toThis, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((CMParticular)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((CMParticular)E, S);} }
*/
}
