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
public class DefaultItemCol implements ItemCollection, Ownable
{
	protected Vector<Item> inventory=new Vector<Item>(1);
	protected CMObject parent=null;
	protected int maxweight=0;
	protected int maxsize=0;
	
	
	//CMObject
	public String ID(){return "DefaultItemCol";}
	public CMObject newInstance(){return new DefaultItemCol();}
	public CMObject copyOf(){return null;}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Ownable
	public CMObject owner(){return parent;}
	public void setOwner(CMObject owner){parent=owner;}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

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
//				item.getEnvObject().recoverEnvStats();
		}
	}
	public void removeItem(Item item)
	{
		synchronized(inventory)
		{
			inventory.removeElement(item);
//				item.getEnvObject().recoverEnvStats();
		}
	}
	public Item removeItem(int i)
	{
		synchronized(inventory)
		{
			Item item=inventory.remove(i);
//				item.getEnvObject().recoverEnvStats();
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
			public String save(DefaultItemCol E){ return CMLib.coffeeMaker().getVectorStr(E.inventory); }
			public void load(DefaultItemCol E, String S){ E.inventory=CMLib.coffeeMaker().setVectorStr(S); } },
		WMX(){
			public String save(DefaultItemCol E){ return ""+E.maxweight; }
			public void load(DefaultItemCol E, String S){ E.maxweight=Integer.parseInt(S); } },
		SMX(){
			public String save(DefaultItemCol E){ return ""+E.maxsize; }
			public void load(DefaultItemCol E, String S){ E.maxsize=Integer.parseInt(S); } },
		;
		public abstract String save(DefaultItemCol E);
		public abstract void load(DefaultItemCol E, String S);
		public String save(CMSavable E){return save((DefaultItemCol)E);}
		public void load(CMSavable E, String S){load((DefaultItemCol)E, S);} }
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
