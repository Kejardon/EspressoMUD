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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import java.nio.ByteBuffer;

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
	protected CMSavable parent;
//	protected int saveNum=0;

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

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
/*		if((saveNum==0)&&(parent!=null))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.Objects.CLOSEABLE.getNumber(this);
		}
		return saveNum; */
		return 0;
	}
	public void setSaveNum(int num)
	{
/*		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.Objects.CLOSEABLE.removeNumber(saveNum);
			saveNum=num;
			SIDLib.Objects.CLOSEABLE.assignNumber(num, this);
		} */
	}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(parent);}

	//Closeable
	public String keyName(){return key;}
	public void setKeyName(String keyName){key=keyName; CMLib.database().saveObject(parent);}
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
		CMLib.database().saveObject(parent);
	}
	public void destroy(){open=true; locked=false; closeable=false; key=""; CMLib.database().deleteObject(this);}

	private enum SCode implements CMSavable.SaveEnum{
		KEY(){
			public ByteBuffer save(DefaultLid E){
				if(E.key=="skeleton") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.key); }
			public int size(){return 0;}
			public void load(DefaultLid E, ByteBuffer S){E.key=CMLib.coffeeMaker().loadString(S); } },
		BLS(){
			public ByteBuffer save(DefaultLid E){
				byte[] bools=new byte[1];
				if(E.haslock) bools[0]|=1<<0;
				if(E.open) bools[0]|=1<<1;
				if(E.closeable) bools[0]|=1<<2;
				if(E.locked) bools[0]|=1<<3;
				if(E.obvious) bools[0]|=1<<4;
				return ByteBuffer.wrap(bools); }
			public int size(){return 1;}
			public void load(DefaultLid E, ByteBuffer S){
				byte bools=S.get();
				E.haslock=((bools&(1<<0))!=0);
				E.open=((bools&(1<<1))!=0);
				E.closeable=((bools&(1<<2))!=0);
				E.locked=((bools&(1<<3))!=0);
				E.obvious=((bools&(1<<4))!=0);} },
		;
		public abstract ByteBuffer save(DefaultLid E);
		public abstract void load(DefaultLid E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((DefaultLid)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((DefaultLid)E, S);} }
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
