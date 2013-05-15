package com.planet_ink.coffee_mud.Common.Closeable;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
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
	protected boolean closed=false;
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
				saveNum=SIDLib.CLOSEABLE.getNumber(this);
		}
		return saveNum; */
		return 0;
	}
	public void setSaveNum(int num)
	{
/*		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.CLOSEABLE.removeNumber(saveNum);
			saveNum=num;
			SIDLib.CLOSEABLE.assignNumber(num, this);
		} */
	}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){if(parent!=null) parent.saveThis();}
	public void prepDefault(){}

	//Closeable
	public String keyName(){return key;}
	public void setKeyName(String keyName){key=keyName; if(parent!=null) parent.saveThis();}
	public void setLocked(boolean bool){locked=bool; if(parent!=null) parent.saveThis();}
	public void setLock(boolean bool){haslock=bool; if(parent!=null) parent.saveThis();}
	public void setClosed(boolean bool){closed=bool; if(parent!=null) parent.saveThis();}
	public void setClosable(boolean bool){closeable=bool; if(parent!=null) parent.saveThis();}
	public void setObvious(boolean bool){obvious=bool; if(parent!=null) parent.saveThis();}
	public boolean locked(){return locked;}
	public boolean hasLock(){return haslock;}
	public boolean closed(){return closed;}
	public boolean canClose(){return closeable;}
	public boolean obviousLock(){return obvious;}
	public void setLidsNLocks(boolean newHasALid, boolean newIsClosed, boolean newHasALock, boolean newIsLocked, boolean newObvious)
	{
		closeable=newHasALid;
		closed=newIsClosed;
		haslock=newHasALock;
		locked=newIsLocked;
		obvious=newObvious;
		if(parent!=null) parent.saveThis();
	}
	public void destroy()
	{
		closed=false;
		locked=false;
		closeable=false;
		key="";
		//if(saveNum!=0)
		//	CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed()
	{
		if(parent!=null)
			return parent.amDestroyed();
		return true;
	}

	private enum SCode implements SaveEnum<DefaultLid>{
		KEY(){
			public ByteBuffer save(DefaultLid E){
				if(E.key=="skeleton") return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.key); }
			public int size(){return 0;}
			public void load(DefaultLid E, ByteBuffer S){E.key=CMLib.coffeeMaker().loadString(S); } },
		BLS(){
			public ByteBuffer save(DefaultLid E){
				byte[] bools=new byte[1];
				if(E.haslock) bools[0]|=1<<0;
				if(E.closed) bools[0]|=1<<1;
				if(E.closeable) bools[0]|=1<<2;
				if(E.locked) bools[0]|=1<<3;
				if(E.obvious) bools[0]|=1<<4;
				return ByteBuffer.wrap(bools); }
			public int size(){return 1;}
			public void load(DefaultLid E, ByteBuffer S){
				byte bools=S.get();
				E.haslock=((bools&(1<<0))!=0);
				E.closed=((bools&(1<<1))!=0);
				E.closeable=((bools&(1<<2))!=0);
				E.locked=((bools&(1<<3))!=0);
				E.obvious=((bools&(1<<4))!=0);} },
		;
		public CMSavable subObject(DefaultLid fromThis){return null;} }
	private enum MCode implements ModEnum<DefaultLid>{
		KEYNAME(){
			public String brief(DefaultLid E){return E.key;}
			public String prompt(DefaultLid E){return E.key;}
			public void mod(DefaultLid E, MOB M){E.key=CMLib.genEd().stringPrompt(M, ""+E.key, false);} },
		LOCKED(){
			public String brief(DefaultLid E){return ""+E.locked;}
			public String prompt(DefaultLid E){return ""+E.locked;}
			public void mod(DefaultLid E, MOB M){E.locked=CMLib.genEd().booleanPrompt(M, ""+E.locked);} },
		CLOSED(){
			public String brief(DefaultLid E){return ""+E.closed;}
			public String prompt(DefaultLid E){return ""+E.closed;}
			public void mod(DefaultLid E, MOB M){E.closed=CMLib.genEd().booleanPrompt(M, ""+E.closed);} },
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
		; }
}
