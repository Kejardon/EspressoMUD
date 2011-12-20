package com.planet_ink.coffee_mud.Common;
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
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class DefaultLid implements Closeable, Ownable
{
	protected String key="skeleton";
	protected boolean locked=false;
	protected boolean haslock=false;
	protected boolean open=false;
	protected boolean closeable=true;
	protected boolean obvious=true;
	protected CMObject parent;
	protected int saveNum=0;


	//Ownable
	public CMObject owner(){return parent;}
	public void setOwner(CMObject owner){parent=owner;}

	//CMObject
	public String ID(){return "DefaultLid";}
	public CMObject newInstance(){return new DefaultLid();}
	public CMObject copyOf(){return null;}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)
		synchronized(saveNum)
		{
			if(saveNum==0)
				saveNum=Closeable.O.getNumber();
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(saveNum)
		{
			if(saveNum!=0)
				Closeable.O.removeNumber(saveNum, this);
			saveNum=num;
			Closeable.O.assignNumber(num, this);
		}
	}

	//Closeable
	public String keyName(){return key;}
	public void setKeyName(String keyName){key=keyName;}
	public boolean isLocked(){return locked;}
	public boolean hasALock(){return haslock;}
	public boolean isOpen(){return open;}
	public boolean hasALid(){return closeable;}
	public boolean obviousLock(){return obvious;}
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked, boolean newObvious)
	{
		closeable=newHasALid;
		open=newIsOpen;
		haslock=newHasALock;
		locked=newIsLocked;
		obvious=newObvious;
	}
	public void destroy(){open=true; locked=false; closeable=false; key="";}

	private enum SCode implements CMSavable.SaveEnum{
		KEY(){
			public String save(DefaultLid E){ return E.key; }
			public void load(DefaultLid E, String S){E.key=S.intern(); } },
		LCK(){
			public String save(DefaultLid E){ return ""+E.haslock; }
			public void load(DefaultLid E, String S){E.haslock=Boolean.getBoolean(S); } },
		OPN(){
			public String save(DefaultLid E){ return ""+E.open; }
			public void load(DefaultLid E, String S){E.open=Boolean.getBoolean(S); } },
		LID(){
			public String save(DefaultLid E){ return ""+E.closeable; }
			public void load(DefaultLid E, String S){E.closeable=Boolean.getBoolean(S); } },
		LKD(){
			public String save(DefaultLid E){ return ""+E.locked; }
			public void load(DefaultLid E, String S){E.locked=Boolean.getBoolean(S); } },
		OBV(){
			public String save(DefaultLid E){ return ""+E.obvious; }
			public void load(DefaultLid E, String S){E.obvious=Boolean.getBoolean(S); } }
		;
		public abstract String save(DefaultLid E);
		public abstract void load(DefaultLid E, String S);
		public String save(CMSavable E){return save((DefaultLid)E);}
		public void load(CMSavable E, String S){load((DefaultLid)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		KEYNAME(){
			public String brief(DefaultLid E){return E.key;}
			public String prompt(DefaultLid E){return E.key;}
			public void mod(DefaultLid E, MOB M){E.key=CMLib.genEd().stringPrompt(M, ""+E.key, false);} },
		LOCKED(){
			public String brief(DefaultLid E){return ""+E.locked;}
			public String prompt(DefaultLid E){return ""+E.locked;}
			public void mod(DefaultLid E, MOB M){E.locked=CMLib.genEd().booleanPrompt(M, ""+E.locked);} },
		OPEN(){
			public String brief(DefaultLid E){return ""+E.open;}
			public String prompt(DefaultLid E){return ""+E.open;}
			public void mod(DefaultLid E, MOB M){E.open=CMLib.genEd().booleanPrompt(M, ""+E.open);} },
		CLOSEABLE(){
			public String brief(DefaultLid E){return ""+E.closeable;}
			public String prompt(DefaultLid E){return ""+E.closeable;}
			public void mod(DefaultLid E, MOB M){E.closeable=CMLib.genEd().booleanPrompt(M, ""+E.closeable);} },
		HASLOCK(){
			public String brief(DefaultLid E){return ""+E.haslock;}
			public String prompt(DefaultLid E){return ""+E.haslock;}
			public void mod(DefaultLid E, MOB M){E.haslock=CMLib.genEd().booleanPrompt(M, ""+E.haslock);} },
		OBVIOUSKEY() {
			public String brief(DefaultLid E){return ""+E.obvious;}
			public String prompt(DefaultLid E){return ""+E.obvious;}
			public void mod(DefaultLid E, MOB M){E.obvious=CMLib.genEd().booleanPrompt(M, ""+E.obvious);} }
		;
		public abstract String brief(DefaultLid fromThis);
		public abstract String prompt(DefaultLid fromThis);
		public abstract void mod(DefaultLid toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultLid)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultLid)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultLid)toThis, M);} }
}
