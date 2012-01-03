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
public class DefaultItemCol implements ItemCollection, Ownable
{
	protected Vector<Item> inventory=new Vector<Item>(1);
	protected CMSavable parent=null;
	protected int maxweight=0;
	protected int maxsize=0;
	protected int[] itemsToLoad=null;
	protected int saveNum=0;
	
	//CMObject
	public String ID(){return "DefaultItemCol";}
	public CMObject newInstance(){return new DefaultItemCol();}
	public CMObject copyOf(){return null;}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public void destroy()
	{
		//TODO
		CMLib.database().deleteObject(this);
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.Objects.ITEMCOLLECTION.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.Objects.ITEMCOLLECTION.removeNumber(saveNum);
			saveNum=num;
			SIDLib.Objects.ITEMCOLLECTION.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(itemsToLoad!=null)
		{
			for(int SID : itemsToLoad)
			{
				Item item = (Item)SIDLib.Objects.ITEM.get(SID);
				if(item==null) continue;
				if(parent instanceof ListenHolder)
					item.registerListeners((ListenHolder)parent);
				item.setContainer(parent);
				inventory.addElement(item);
			}
			itemsToLoad=null;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}

	//ItemCollection
	public boolean canHold(Item item)
	{
		if((maxweight==0)&&(maxsize==0)) return true;
		int totalW=0;
		int totalS=0;
		for(int i=0;i<inventory.size();i++)
		{
			EnvStats E=inventory.get(i).getEnvObject().envStats();
			totalW+=E.weight();
			totalS+=E.height();
		}
		if((maxweight==0)||(totalW>maxweight))
			return false;
		if((maxsize==0)||(totalS>maxsize))
			return false;
		return true;
	}
	public boolean hasItem(Item item, boolean sub)
	{
		if(!sub) return inventory.contains(item);
		for(int i=inventory.size()-1;i>=0;i--)
		{
			if(item.equals(inventory.get(i))) return true;
			ItemCollection O=ItemCollection.O.getFrom(inventory.get(i));
			if((O!=null)&&(O.hasItem(item, true))) return true;
		}
		return false;
	}
	public void addItem(Item item)
	{
		synchronized(inventory)
		{
			if(inventory.contains(item)) return;
			if(parent instanceof ListenHolder)
				item.registerListeners((ListenHolder)parent);
			item.setContainer(parent);
			inventory.addElement(item);
		}
		CMLib.database().saveObject(this);
	}
	public void removeItem(Item item)
	{
		synchronized(inventory)
		{
			inventory.removeElement(item);
		}
		CMLib.database().saveObject(this);
	}
	public Item removeItem(int i)
	{
		synchronized(inventory)
		{
			Item item=inventory.remove(i);
			CMLib.database().saveObject(this);
			return item;
		}
	}
	public Item getItem(int index)
	{
		try { return inventory.elementAt(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public int numItems() { return inventory.size(); }
	public Vector<Item> allItems() {return (Vector<Item>)inventory.clone();}

	private enum SCode implements CMSavable.SaveEnum{
		INV(){
			public ByteBuffer save(DefaultItemCol E){
				if(E.inventory.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.inventory.toArray(new CMSavable[E.inventory.size()]));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(DefaultItemCol E, ByteBuffer S){ E.itemsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		WMX(){
			public ByteBuffer save(DefaultItemCol E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.maxweight).rewind(); }
			public int size(){return 4;}
			public void load(DefaultItemCol E, ByteBuffer S){ E.maxweight=S.getInt(); } },
		SMX(){
			public ByteBuffer save(DefaultItemCol E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.maxsize).rewind(); }
			public int size(){return 4;}
			public void load(DefaultItemCol E, ByteBuffer S){ E.maxsize=S.getInt(); } },
		;
		public abstract ByteBuffer save(DefaultItemCol E);
		public abstract void load(DefaultItemCol E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((DefaultItemCol)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((DefaultItemCol)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		INVENTORY() {
			public String brief(DefaultItemCol E){return ""+E.inventory.size();}
			public String prompt(DefaultItemCol E){return "";}
			public void mod(DefaultItemCol E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					Vector<Item> V=E.allItems();
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						Item I=CMLib.genEd().newAnyItem(M);
						if(I!=null)
							E.addItem((Item)CMLib.genEd().genMiscSet(M, I)); }
					else if(i<V.size()) {
						char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).name()+" (default M)? ","M").trim().toUpperCase().charAt(0);
						if(action=='D') { Item I = V.get(i); E.removeItem(I); I.destroy(); }
						else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i)); } } } },
		MAXWEIGHT() {
			public String brief(DefaultItemCol E){return ""+E.maxweight;}
			public String prompt(DefaultItemCol E){return ""+E.maxweight;}
			public void mod(DefaultItemCol E, MOB M){E.maxweight=CMLib.genEd().intPrompt(M, ""+E.maxweight);} },
		MAXSIZE() {
			public String brief(DefaultItemCol E){return ""+E.maxsize;}
			public String prompt(DefaultItemCol E){return ""+E.maxsize;}
			public void mod(DefaultItemCol E, MOB M){E.maxsize=CMLib.genEd().intPrompt(M, ""+E.maxsize);} },
		;
		public abstract String brief(DefaultItemCol fromThis);
		public abstract String prompt(DefaultItemCol fromThis);
		public abstract void mod(DefaultItemCol toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultItemCol)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultItemCol)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultItemCol)toThis, M);} }
}
