package com.planet_ink.coffee_mud.Items.Basic;
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
		desc="You can't tell what it\\`s to by looking at it.";

//		material=RawMaterial.RESOURCE_STEEL;
//		baseGoldValue=0;
//		recoverEnvStats();
	}

	public void setKey(String keyName){key=keyName;}
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

	private enum SCode implements CMSavable.SaveEnum{
		KEY(){
			public String save(StdKey E){ return E.key; }
			public void load(StdKey E, String S){ E.key=S.intern(); } },
		;
		public abstract String save(StdKey E);
		public abstract void load(StdKey E, String S);
		public String save(CMSavable E){return save((StdKey)E);}
		public void load(CMSavable E, String S){load((StdKey)E, S);} }
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
