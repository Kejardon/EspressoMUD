package com.planet_ink.coffee_mud.Common;
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
public class DefaultBind implements Bind
{
	protected CMSavable parent=null;
	protected Item ItemA;
	protected Item ItemB;
	protected int bindType;
	protected int strength;
	protected int saveNum=0;
	//protected boolean amDestroyed=false;
	protected int[] itemsToLoad=null;
	
	public String ID(){return "DefaultBind";}
	public CMObject newInstance(){return new DefaultBind();}
	public CMObject copyOf(){return null;}	//TODO
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public Item itemA(){return ItemA;}
	public Item itemB(){return ItemB;}
	public int bindType(){return bindType;}
	public void setType(int type){bindType=type;}
	public int strength(){return strength;}
	public void setStrength(int str){strength=str;}

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public void destroy()
	{
		//amDestroyed=true;
	}
	public boolean amDestroyed()
	{
		//return amDestroyed;
		return false;
	}

	//CMModifiable and CMSavable
	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)//&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.BIND.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.BIND.removeNumber(saveNum);
			saveNum=num;
			SIDLib.BIND.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		itemFailure:
		if(itemsToLoad!=null && itemsToLoad.length == 2)
		{
			Item a = SIDLib.ITEM.get(itemsToLoad[0]);
			Item b = SIDLib.ITEM.get(itemsToLoad[1]);
			itemsToLoad=null;
			if(a==null || b==null)
				break itemFailure;
			ItemA = a;
			ItemB = b;
			//if(parent instanceof ListenHolder)
				//item.registerListeners((ListenHolder)parent);
			a.setContainer(parent);
			b.setContainer(parent);
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	private enum SCode implements SaveEnum<DefaultBind>{
		INV(){
			public ByteBuffer save(DefaultBind E){
				ByteBuffer itemNums = ByteBuffer.wrap(new byte[8]);
				itemNums.putInt(E.ItemA==null?0:E.ItemA.saveNum());
				itemNums.putInt(E.ItemB==null?0:E.ItemB.saveNum());
				itemNums.rewind();
				return itemNums; }
			public int size(){return 8;}
			public void load(DefaultBind E, ByteBuffer S){ E.itemsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		TYP(){
			public ByteBuffer save(DefaultBind E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.bindType).rewind(); }
			public int size(){return 4;}
			public void load(DefaultBind E, ByteBuffer S){ E.bindType=S.getInt(); } },
		STR(){
			public ByteBuffer save(DefaultBind E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.strength).rewind(); }
			public int size(){return 4;}
			public void load(DefaultBind E, ByteBuffer S){ E.strength=S.getInt(); } },
		;
		public CMSavable subObject(DefaultBind fromThis){return null;} }
	private enum MCode implements ModEnum<DefaultBind>{
		ITEMA() {
			public String brief(DefaultBind E){return ((E.ItemA==null)?("null"):(""+E.ItemA.ID()));}
			public String prompt(DefaultBind E){return "";}
			public void mod(DefaultBind E, MOB M){
				char action;
				if(E.ItemA==null) action='R';
				else action=M.session().prompt("(R)eplace or (M)odify it (default M)? ","M").trim().toUpperCase().charAt(0);
				if(action=='R') {
					Item old=E.ItemA;
					E.ItemA=CMLib.genEd().getOrMakeItem(M);
					if(E.ItemA!=null) {
						ItemCollection col=ItemCollection.O.getFrom(E.ItemA.container());
						if(col!=null) col.removeItem(E.ItemA);
						E.ItemA.setContainer(E.parent); }
					if(old!=null) old.destroy();
					BindCollection parentCollection=BindCollection.O.getFrom(E.parent);
					if(parentCollection!=null) parentCollection.clearCache(); }
				else if(action=='M') CMLib.genEd().genMiscSet(M, E.ItemA); } },
		ITEMB() {
			public String brief(DefaultBind E){return ((E.ItemB==null)?("null"):(""+E.ItemB.ID()));}
			public String prompt(DefaultBind E){return "";}
			public void mod(DefaultBind E, MOB M){
				char action;
				if(E.ItemB==null) action='R';
				else action=M.session().prompt("(R)eplace or (M)odify it (default M)? ","M").trim().toUpperCase().charAt(0);
				if(action=='R') {
					Item old=E.ItemB;
					E.ItemB=CMLib.genEd().getOrMakeItem(M);
					if(E.ItemB!=null) {
						ItemCollection col=ItemCollection.O.getFrom(E.ItemB.container());
						if(col!=null) col.removeItem(E.ItemB);
						E.ItemB.setContainer(E.parent); }
					if(old!=null) old.destroy();
					BindCollection parentCollection=BindCollection.O.getFrom(E.parent);
					if(parentCollection!=null) parentCollection.clearCache(); }
				else if(action=='M') CMLib.genEd().genMiscSet(M, E.ItemB); } },
		TYPE() {
			public String brief(DefaultBind E){return ""+E.bindType;}
			public String prompt(DefaultBind E){return "";}
			public void mod(DefaultBind E, MOB M){E.bindType=CMLib.genEd().intPrompt(M, ""+E.bindType);} },
		STRENGTH() {
			public String brief(DefaultBind E){return ""+E.strength;}
			public String prompt(DefaultBind E){return "";}
			public void mod(DefaultBind E, MOB M){E.strength=CMLib.genEd().intPrompt(M, ""+E.strength);} },
		; }
}