package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class StdCoins extends StdItem implements Coins
{
	@Override public String ID(){	return "StdCoins";}
//	public int value(){	return envStats().ability();}
	protected long denomination=1;
	protected long amount=0;
	protected String currency="";
	protected BeanCounter.MoneyDenomination denom=null; 

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
			BeanCounter.CMCurrency set=CMLib.beanCounter().getCurrencySet(currency);
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
		CMLib.database().saveObject(this);
	}
	public long getDenomination(){return denomination;}
	public void setDenomination(long valuePerCoin) { denomination=valuePerCoin; CMLib.database().saveObject(this);}
	public long getTotalValue(){return denomination*amount;}
	public String getCurrency(){ return currency;}
	public void setCurrency(String named) { currency=named; CMLib.database().saveObject(this);}

	public boolean putCoinsBack()
	{
		Coins alternative=null;
		ItemCollection O=ItemCollection.O.getFrom(container);
		if(O!=null)
		{
			for(Iterator<Item> iter=O.allItems();iter.hasNext();)
			{
				Item I=iter.next();
				if((I!=this)
				&&(I instanceof Coins)
				&&(((Coins)I).getDenomination()==denomination)
				&&(((Coins)I).getCurrency().equals(currency)))
				{
					((Coins)I).setNumberOfCoins(alternative.getNumberOfCoins()+amount);
					//CMLib.database().saveObject(I);
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
	@Override public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null)
		{
			ModEnum[] arrA=MCode.values();
			ModEnum[] arrB=super.totalEnumM();
			totalEnumM=CMParms.appendToArray(arrA, arrB, ModEnum[].class);
		}
		return totalEnumM;
	}
	@Override public Enum[] headerEnumM()
	{
		if(headerEnumM==null)
		{
			Enum[] arrA=new Enum[] {MCode.values()[0]};
			Enum[] arrB=super.headerEnumM();
			headerEnumM=CMParms.appendToArray(arrA, arrB, Enum[].class);
		}
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	@Override public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null)
		{
			SaveEnum[] arrA=SCode.values();
			SaveEnum[] arrB=super.totalEnumS();
			totalEnumS=CMParms.appendToArray(arrA, arrB, SaveEnum[].class);
		}
		return totalEnumS;
	}
	@Override public Enum[] headerEnumS()
	{
		if(headerEnumS==null)
		{
			Enum[] arrA=new Enum[] {SCode.values()[0]};
			Enum[] arrB=super.headerEnumS();
			headerEnumS=CMParms.appendToArray(arrA, arrB, Enum[].class);
		}
		return headerEnumS;
	}

	private enum SCode implements SaveEnum<StdCoins>{
		CUR(){
			public ByteBuffer save(StdCoins E){ return CMLib.coffeeMaker().savString(E.currency); }
			public int size(){return 0;}
			public void load(StdCoins E, ByteBuffer S){ E.currency=CMLib.coffeeMaker().loadString(S); } },
		AMT(){
			public ByteBuffer save(StdCoins E){ return (ByteBuffer)ByteBuffer.wrap(new byte[8]).putLong(E.amount).rewind(); }
			public int size(){return 8;}
			public void load(StdCoins E, ByteBuffer S){ E.amount=S.getLong(); } },
		DEN(){
			public ByteBuffer save(StdCoins E){ return (ByteBuffer)ByteBuffer.wrap(new byte[8]).putLong(E.denomination).rewind(); }
			public int size(){return 8;}
			public void load(StdCoins E, ByteBuffer S){ E.denomination=S.getLong(); } },
		;
		public CMSavable subObject(StdCoins fromThis){return null;} }
	private enum MCode implements ModEnum<StdCoins>{
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
		; }
}
