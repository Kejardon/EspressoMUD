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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class DefaultItemCol implements ItemCollection, Ownable
{
	protected CopyOnWriteArrayList<Item> inventory=new CopyOnWriteArrayList();
	protected CMSavable parent=null;
	protected int maxweight=0;
	protected int maxsize=0;
	protected int[] itemsToLoad=null;
	protected int saveNum=0;
	protected boolean amDestroyed=false;
	
	//CMObject
	public String ID(){return "DefaultItemCol";}
	public CMObject newInstance(){return new DefaultItemCol();}
	public CMObject copyOf(){return null;}	//TODO
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public void destroy()	//TODO/NOTE: Item contents should be dumped onto the ground when a container is broken before this is called.
	{
		amDestroyed=true;

		Room limbo=SIDLib.ROOM.get(1);
		if(limbo!=null)
			for(Item I : inventory)
				limbo.bringHere(I, false);
		else
			for(Item I : inventory)
				I.destroy();
		if(saveNum!=0)
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.ITEMCOLLECTION.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.ITEMCOLLECTION.removeNumber(saveNum);
			saveNum=num;
			SIDLib.ITEMCOLLECTION.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(itemsToLoad!=null)
		{
			for(int SID : itemsToLoad)
			{
				Item item = SIDLib.ITEM.get(SID);
				if(item==null) continue;
				//if(parent instanceof ListenHolder)
					//item.registerListeners((ListenHolder)parent);
				item.setContainer(parent);
				inventory.add(item);
			}
			itemsToLoad=null;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	//ItemCollection
	public boolean canHold(Item item)
	{
		if((maxweight==0)&&(maxsize==0)) return true;
		int totalW=0;
		int totalS=0;
		for(Item I : inventory)
		{
			EnvStats E=I.getEnvObject().envStats();
			totalW+=E.weight();
			totalS+=E.height();
		}
		EnvStats E=item.getEnvObject().envStats();
		if((maxweight!=0)&&(totalW+E.weight()>maxweight))
			return false;
		if((maxsize!=0)&&(totalS+E.height()>maxsize))
			return false;
		return recurseCheck(parent, item);
	}
	public boolean recurseCheck(CMObject E, Item I)
	{
		while(true)
		{
			if((E==null)||(E==I))
				return false;
			if(E instanceof Room)	//TODO: Expand this for item rooms?
				return true;
			E=CMLib.map().goUpOne(E);
		}
	}
	public boolean hasItem(Item item, boolean sub)
	{
		if(!sub) return inventory.contains(item);
		for(Item I : inventory)
		{
			if(item.equals(I)) return true;
			ItemCollection O=ItemCollection.O.getFrom(I);
			if((O!=null)&&(O.hasItem(item, true))) return true;
		}
		return false;
	}
	public void addItem(Item item)
	{
		if(recurseCheck(parent, item) && inventory.addIfAbsent(item))
		{
			//if(parent instanceof ListenHolder)
				//item.registerListeners((ListenHolder)parent);
			item.setContainer(parent);
			CMLib.database().saveObject(this);
		}
	}
	public void removeItem(Item item)
	{
		inventory.remove(item);
		CMLib.database().saveObject(this);
	}
	public Item removeItem(int i)
	{
		Item item=inventory.remove(i);
		CMLib.database().saveObject(this);
		return item;
	}
	public Item getItem(int index)
	{
		try { return inventory.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public int numItems() { return inventory.size(); }
	public Iterator<Item> allItems() {return inventory.iterator();}

	private enum SCode implements CMSavable.SaveEnum{
		INV(){
			public ByteBuffer save(DefaultItemCol E){
				if(E.inventory.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.inventory.toArray(CMSavable.dummyCMSavableArray));
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
					Vector<Item> V=CMParms.denumerate(E.allItems());
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
