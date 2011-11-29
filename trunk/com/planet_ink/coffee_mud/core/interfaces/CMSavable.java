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
/**
Interface for objects savable with CoffeeMaker. This is basically everything.

Current format plan
Enums that have String save(CMSavable) and void load(CMSavable, String). These typically just call library functions
in coffeemaker to convert to/from strings.
The CMSavable itself will return Enum.values(), or the first of each Enum (to be checkable for .valueOf() and stuff) when asked
 */
@SuppressWarnings("unchecked")
public interface CMSavable extends CMObject
{
	public static interface SaveEnum
	{
		public String save(CMSavable fromThis);
		public void load(CMSavable toThis, String val);
	}

	public SaveEnum[] totalEnumS();
	public Enum[] headerEnumS();

/*
	//Non-saving class
	public SaveEnum[] totalEnumS(){return new SaveEnum[0];}
	public Enum[] headerEnumS(){return new Enum[0];}

	//Typical non-extended class (like DummyCMSav but with an actual SCode)
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	
	//Typical extended class
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
			headerEnumS=(Enum[])CMath.combineArrays(new Enum[] {SCode.values()[0]}, super.headerEnumS());
		return headerEnumS;
	}

	//The actual enum/code/parser
	private static enum SCode implements SaveEnum{
		DUMMY() {
			public String save(CMParticular fromThis) {return fromThis.x;}
			public void load(CMParticular toThis, String s) {toThis.x=s.intern();} },

		;
		public abstract String save(CMParticular fromThis);
		public abstract void load(CMParticular toThis, String S);
		public String save(CMSavable E){return save((CMParticular)E);}
		public void load(CMSavable E, String S){load((CMParticular)E, S);} }
*/
}
