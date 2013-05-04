package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

import com.planet_ink.coffee_mud.Items.Basic.MixedItem;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class DefaultBindCollection implements BindCollection, Ownable
{
	protected CopyOnWriteArrayList<Bind> inventory=new CopyOnWriteArrayList();
	protected CMSavable parent=null;
	protected int[] bindsToLoad=null;
	protected int saveNum=0;
	protected boolean amDestroyed=false;
	protected Item[] itemCache=null;
	
	public DefaultBindCollection(){}
	public DefaultBindCollection(HashSet<Bind> binds)
	{
		inventory.addAll(binds);
		for(Bind B : binds)
		{
			B.setOwner(this);
		}
	}
	
	//CMObject
	public String ID(){return "DefaultBindCollection";}
	public CMObject newInstance(){return new DefaultBindCollection();}
	public CMObject copyOf(){return null;}	//TODO
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public void destroy()
	{
		amDestroyed=true;

		for(Bind I : inventory)
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
				saveNum=SIDLib.BINDCOLLECTION.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.BINDCOLLECTION.removeNumber(saveNum);
			saveNum=num;
			SIDLib.BINDCOLLECTION.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(bindsToLoad!=null)
		{
			for(int SID : bindsToLoad)
			{
				Bind bind = SIDLib.BIND.get(SID);
				if(bind==null) continue;
				//if(parent instanceof ListenHolder)
					//bind.registerListeners((ListenHolder)parent);
				bind.setOwner(parent);
				inventory.add(bind);
			}
			bindsToLoad=null;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	public ArrayList<Bind> bindsTo(Item subItem)
	{
		ArrayList<Bind> list=new ArrayList();
		for(Bind B : inventory)
			if(subItem.equals(B.itemA())||subItem.equals(B.itemB()))
				list.add(B);
		return list;
	}
	public Iterator<Bind> allBinds()
	{
		return inventory.iterator();
	}
	public boolean hasBind(Bind bind, boolean checkSubItems)
	{
		if(inventory.contains(bind)) return true;
		if(checkSubItems)
			for(Bind B : inventory)
			{
				Item I=B.itemA();
				if(I.isComposite() && I.subItems().hasBind(bind, true))
					return true;
				I=B.itemB();
				if(I.isComposite() && I.subItems().hasBind(bind, true))
					return true;
			}
		return false;
	}
	public Bind getBind(int i)
	{
		try{ return inventory.get(i); }
		catch(ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public int numBinds()
	{
		return inventory.size();
	}
	public void addBind(Bind bind)
	{
		if(inventory.addIfAbsent(bind))
		{
			//if(parent instanceof ListenHolder)
				//bind.registerListeners((ListenHolder)parent);
			//bind.setContainer(parent);
			itemCache=null;
			CMLib.database().saveObject(this);
		}
	}
	public void removeBind(Bind bind)
	{
		Item[] lastStatus=itemArray();
		if(inventory.remove(bind))
		{
			itemCache=null;
			recalcMainItem(lastStatus);
			CMLib.database().saveObject(this);
		}
	}
	public Bind removeBind(int i)
	{
		Item[] lastStatus=itemArray();
		try{
			Bind B=inventory.remove(i);
			itemCache=null;
			recalcMainItem(lastStatus);
			CMLib.database().saveObject(this);
			return B;
		} catch (ArrayIndexOutOfBoundsException e){}
		return null;
	}
	/*
	This needs to check if current structure is whole. If not
		break unbound parts off completely
		create new MixedItems of remaining parts
		Place all of both of above in the room?
	*/
	protected void recalcMainItem(Item[] lastStatus)
	{
		Iterator<Bind> binds = inventory.iterator();
		Room toHere=CMLib.map().roomLocation(parent);
		if(toHere == null)
		{
			toHere = SIDLib.ROOM.get(SIDLib.LimboRoomSaveNum);
			Log.errOut("DBC recalc","No room location found for MixedItem parent!");
			if(toHere == null) return;
		}
		
		if(!binds.hasNext())
		{
			for(Item I : lastStatus)
				toHere.bringHere(I, true);
			return;
		}
		ArrayList<HashSet<Bind>> bindGroups=new ArrayList<HashSet<Bind>>();
		ArrayList<HashSet<Item>> itemGroups=new ArrayList<HashSet<Item>>();
		while(binds.hasNext())
		{
			Bind nextBind=binds.next();
			Item A=nextBind.itemA();
			Item B=nextBind.itemB();

			HashSet<Item> foundItems=null;
			int i;
			for(i=0;i<bindGroups.size();i++)
			{
				foundItems=itemGroups.get(i);
				if(foundItems.contains(A) || foundItems.contains(B))
				{
					break;
				}
			}
			HashSet<Bind> foundBinds;
			if(i==bindGroups.size())
			{
				foundItems=new HashSet<Item>();
				foundBinds=new HashSet<Bind>();
				itemGroups.add(foundItems);
				bindGroups.add(foundBinds);
			} else {
				foundBinds=bindGroups.get(i);
			}
			foundItems.add(A);
			foundItems.add(B);
			foundBinds.add(nextBind);
			for(int j=i+1;j<bindGroups.size();j++)
			{
				HashSet<Item> extraItems=itemGroups.get(j);
				if(extraItems.contains(A) || extraItems.contains(B))
				{
					itemGroups.remove(j);
					HashSet<Bind> extraBinds=bindGroups.remove(j);
					foundItems.addAll(extraItems);
					foundBinds.addAll(extraBinds);
					break;
				}
			}
		}
		
		if(itemGroups.get(0).size()==lastStatus.length) return;
		
		ArrayList<Item> leftoverItems = new ArrayList(lastStatus.length);
		for(Item I : lastStatus)
			leftoverItems.add(I);
		
		for(int i=0;i<bindGroups.size();i++)
		{
			DefaultBindCollection newBindCollection = new DefaultBindCollection(bindGroups.get(i));
			MixedItem newItem;
			if(parent instanceof MixedItem)
				newItem = new MixedItem((MixedItem)parent, newBindCollection);
			else
				newItem = new MixedItem(newBindCollection);
			leftoverItems.removeAll(itemGroups.get(i));
			
			toHere.bringHere(newItem, true);
		}
		
		for(Item oldItem : leftoverItems)
			toHere.bringHere(oldItem, true);
	}

	public void clearCache()
	{ itemCache=null; }
	public Item[] itemArray()
	{
		if(itemCache==null) synchronized(this)
		{
			if(itemCache==null)
			{
				HashSet<Item> checked=new HashSet<Item>();
				for(Bind B : inventory)
				{
					checked.add(B.itemA());
					checked.add(B.itemB());
				}
				checked.remove(null);
				itemCache=checked.toArray(Item.dummyItemArray);
			}
		}
		return itemCache;
	}
	public boolean hasItem(Item item, boolean sub)
	{
		Item[] items=itemArray();
		if(sub) for(Item I : items)
		{
			if(item.equals(I)) return true;
			ItemCollection O=ItemCollection.O.getFrom(I);
			if((O!=null)&&(O.hasItem(item, true))) return true;
		}
		else for(Item I : items)
			if(item.equals(I)) return true;
		return false;
	}
	public void removeItem(Item I)
	{
		boolean found=false;
		Item[] lastStatus=itemArray();
		for(Bind B : inventory)
			if(I.equals(B.itemA()) || I.equals(B.itemB()))
			{
				inventory.remove(B);
				found=true;
			}
		if(found)
		{
			itemCache=null;
			recalcMainItem(lastStatus);
			CMLib.database().saveObject(this);
		}
	}
	public int numItems()
	{
		return itemArray().length;
	}
	public Iterator<Item> allItems()
	{
		return new CMParms.IteratorWrapper<Item>(itemArray());
	}

	private enum SCode implements SaveEnum<DefaultBindCollection>{
		INV(){
			public ByteBuffer save(DefaultBindCollection E){
				if(E.inventory.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.inventory.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 4;}
			public void load(DefaultBindCollection E, ByteBuffer S){ E.bindsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		;
		public CMSavable subObject(DefaultBindCollection fromThis){return null;} }
	private enum MCode implements ModEnum<DefaultBindCollection>{
		INVENTORY() {
			public String brief(DefaultBindCollection E){return ""+E.inventory.size();}
			public String prompt(DefaultBindCollection E){return "";}
			public void mod(DefaultBindCollection E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					Vector<Bind> V=new Vector<Bind>(E.inventory);
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						Bind I=CMLib.genEd().newAnyBind(M);
						if(I!=null) {
							I.setOwner(E);
							E.addBind((Bind)CMLib.genEd().genMiscSet(M, I)); } }
					else if(i<V.size()) {
						char action=M.session().prompt("(D)estroy or (M)odify it (default M)? ","M").trim().toUpperCase().charAt(0);
						if(action=='D') { Bind I = V.get(i); E.removeBind(I); I.destroy(); }
						else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i)); } } } },
		; }
}
