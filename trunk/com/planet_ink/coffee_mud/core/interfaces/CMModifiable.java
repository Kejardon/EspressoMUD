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
 * Interface for objects editable with CMGenEditor. This is basically everything.
 *
 */
@SuppressWarnings("unchecked")
public interface CMModifiable extends CMObject
{

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
	public ModEnum[] totalEnumM(){return new ModEnum[0];}
	public Enum[] headerEnumM(){return new Enum[0];}

	//Typical non-extended class
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	//Typical extended class
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null)
			totalEnumM=(ModEnum[])CMath.combineArrays(MCode.values(), super.totalEnumM());
		return totalEnumM;
	}
	public Enum[] headerEnumM()
	{
		if(headerEnumM==null)
			headerEnumM=(Enum[])CMath.combineArrays(new Enum[] {MCode.values()[0]}, super.headerEnumM());
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
