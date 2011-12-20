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
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary;


import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class StdCoins extends StdItem implements Coins
{
	public String ID(){	return "StdCoins";}
//	public int value(){	return envStats().ability();}
	protected long denomination=1;
	protected long amount=0;
	protected String currency="";
	protected MoneyLibrary.MoneyDenomination denom=null; 

	public StdCoins()
	{
		super();
		damagable=false;
//		baseEnvStats.setWeight(0);
//		recoverEnvStats();
	}

	public String Name()
	{
		if(denom==null)
		{
			MoneyLibrary.CMCurrency set=CMLib.beanCounter().getCurrencySet(currency);
			if(set==null) return "";
			denom=set.get(denomination);
		}
		if(denom==null) return "";
		return ""+amount+" "+denom.name();
	}
	public String displayText()
	{
		return Name()+((amount==1)?" lies here.":" lie here.");
	}
	public long getNumberOfCoins(){return amount;}
	public void setNumberOfCoins(long number)
	{
		if(number<=Long.MAX_VALUE/2)
			number=Long.MAX_VALUE/2-1;
		amount=number;
	}
	public long getDenomination(){return denomination;}
	public void setDenomination(long valuePerCoin) { denomination=valuePerCoin; }
	public long getTotalValue(){return denomination*amount;}
	public String getCurrency(){ return currency;}
	public void setCurrency(String named) { currency=named; }

	public boolean putCoinsBack()
	{
		Coins alternative=null;
		ItemCollection O=ItemCollection.O.getFrom(container);
		if(O!=null)
		{
			for(int i=0;i<O.numItems();i++)
			{
				Item I=O.getItem(i);
				if((I!=null)
				&&(I!=this)
				&&(I instanceof Coins)
				&&(((Coins)I).getDenomination()==denomination)
				&&(((Coins)I).getCurrency().equals(currency)))
				{
					((Coins)I).setNumberOfCoins(alternative.getNumberOfCoins()+amount);
					destroy();
					return true;
				}
			}
		}
		return false;
	}

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
		CUR(){
			public String save(StdCoins E){ return E.currency; }
			public void load(StdCoins E, String S){ E.currency=S.intern(); } },
		AMT(){
			public String save(StdCoins E){ return ""+E.amount; }
			public void load(StdCoins E, String S){ E.amount=Long.parseLong(S); } },
		DEN(){
			public String save(StdCoins E){ return ""+E.denomination; }
			public void load(StdCoins E, String S){ E.denomination=Long.parseLong(S); } },
		;
		public abstract String save(StdCoins E);
		public abstract void load(StdCoins E, String S);
		public String save(CMSavable E){return save((StdCoins)E);}
		public void load(CMSavable E, String S){load((StdCoins)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		CURRENCY(){
			public String brief(StdCoins E){return E.currency;}
			public String prompt(StdCoins E){return E.currency;}
			public void mod(StdCoins E, MOB M){E.currency=CMLib.genEd().stringPrompt(M, E.currency, false);} },
		AMOUNT(){
			public String brief(StdCoins E){return ""+E.amount;}
			public String prompt(StdCoins E){return ""+E.amount;}
			public void mod(StdCoins E, MOB M){E.amount=CMLib.genEd().longPrompt(M, ""+E.amount);} },
		DENOMINATION(){
			public String brief(StdCoins E){return ""+E.denomination;}
			public String prompt(StdCoins E){return ""+E.denomination;}
			public void mod(StdCoins E, MOB M){E.denomination=CMLib.genEd().longPrompt(M, ""+E.denomination);} },
		;
		public abstract String brief(StdCoins fromThis);
		public abstract String prompt(StdCoins fromThis);
		public abstract void mod(StdCoins toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdCoins)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdCoins)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdCoins)toThis, M);} }
}
