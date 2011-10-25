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


import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdCoins extends StdItem implements Coins
{
	public String ID(){	return "StdCoins";}
//	public int value(){	return envStats().ability();}
	protected double denomination=1.0;
	protected long amount=0;
	protected String currency="";

	public StdCoins()
	{
		super();
		saveObj=new CoinsSave(this);
		modObj=new CoinsMod(this);
		damagable=false;
//		baseEnvStats.setWeight(0);
//		recoverEnvStats();
	}

	public String Name()
	{
		return CMLib.beanCounter().getDenominationName(currency,denomination,amount);
	}
	public String displayText()
	{
		return CMLib.beanCounter().getDenominationName(currency,denomination,amount)+((amount==1)?" lies here.":" lie here.");
	}
	public long getNumberOfCoins(){return amount;}
	public void setNumberOfCoins(long number)
	{
		if(number<=Long.MAX_VALUE/2)
			number=Long.MAX_VALUE/2-1;
		amount=number;
	}
	public double getDenomination(){return denomination;}
	public void setDenomination(double valuePerCoin) { denomination=valuePerCoin; }
	public double getTotalValue(){return denomination*amount;}
	public String getCurrency(){ return currency;}
	public void setCurrency(String named) { currency=named; }

	public boolean putCoinsBack()
	{
		Coins alternative=null;
		ItemCollection O=ItemCollection.DefaultItemCol.getFrom(container);
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
	private enum SCode implements CMSavable.SaveEnum{
		CUR(CMSavable.savType.STRING), AMT(CMSavable.savType.LONG), DEN(CMSavable.savType.DOUBLE)
		;
		@Override public String toString()
		{
			String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}
		private CMSavable.savType type;
		private SCode(CMSavable.savType myType) {type=myType;}
		public CMSavable.savType type(){return type;}
	}
	private enum MCode implements CMModifiable.ModEnum{
		CURRENCY(CMModifiable.modType.STRING), AMOUNT(CMModifiable.modType.LONG), DENOMINATION(CMModifiable.modType.DOUBLE)
		;
		@Override public String toString()
		{
			String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}
		private CMModifiable.modType type;
		private MCode(CMModifiable.modType myType) {type=myType;}
		public CMModifiable.modType type(){return type;}
	}
	protected class CoinsSave extends StdItem.ItemSave
	{
		public String ID(){return "CoinsSave";}
		public CoinsSave(CMObject O){super(O);}
		public SaveEnum[] totalEnum(boolean first)
		{
			if((first)&&(savCodes(ID())!=null))
				return null;
			SaveEnum[] newE = (SaveEnum[])EnumSet.allOf(SCode.class).toArray();
			SaveEnum[] old = super.totalEnum(false);
			indexFix = old.length;
			SaveEnum[] total=new SaveEnum[indexFix+newE.length];
			System.arraycopy(old, 0, total, 0, indexFix);
			System.arraycopy(newE, 0, total, indexFix, newE.length);
			if(savCodes(ID())==null)
				parseSaves(ID(), total);
			return total;
		}

		private int indexFix=0;

		public String savString(int code, String val)
		{
			if((code<indexFix)&&(-code-1<indexFix))
				return super.savString(code, val);
			if(code>=0)
			{
				switch(SCode.values()[code-indexFix])
				{
					case CUR: currency=val; break;
				}
			}
			else
			{
				code=(-code)-1;
				switch(SCode.values()[code-indexFix])
				{
					case CUR: return currency;
				}
			}
			return "";
		}
		public long savLong(int code, long val)
		{
			if((code<indexFix)&&(-code-1<indexFix))
				return super.savLong(code, val);
			if(code>=0)
			{
				switch(SCode.values()[code-indexFix])
				{
					case CUR: amount=val; break;
				}
			}
			else
			{
				code=(-code)-1;
				switch(SCode.values()[code-indexFix])
				{
					case CUR: return amount;
				}
			}
			return 0;
		}
		public double savDouble(int code, double val)
		{
			if((code<indexFix)&&(-code-1<indexFix))
				return super.savDouble(code, val);
			if(code>=0)
			{
				switch(SCode.values()[code-indexFix])
				{
					case DEN: denomination=val; break;
				}
			}
			else
			{
				code=(-code)-1;
				switch(SCode.values()[code-indexFix])
				{
					case DEN: return denomination;
				}
			}
			return 0;
		}
	}
	protected class CoinsMod extends StdItem.ItemMod
	{
		public String ID(){return "CoinsMod";}
		public CoinsMod(CMObject O){super(O);}
		public ModEnum[] totalEnum(boolean first)
		{
			if((first)&&(modCodes(ID())!=null))
				return null;
			ModEnum[] newE = (ModEnum[])EnumSet.allOf(MCode.class).toArray();
			ModEnum[] old = super.totalEnum(false);
			indexFix = old.length;
			ModEnum[] total=new ModEnum[indexFix+newE.length];
			System.arraycopy(old, 0, total, 0, indexFix);
			System.arraycopy(newE, 0, total, indexFix, newE.length);
			if(modCodes(ID())==null)
				parseMods(ID(), total);
			return total;
		}
		private int indexFix=0;
		public String modBrief(int code)
		{
			if(code<indexFix)
				return super.modBrief(code);
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case CURRENCY: return currency;
				case AMOUNT: return ""+amount;
				case DENOMINATION: return ""+denomination;
			}
			return "";
		}
		public String modDefault(int code)
		{
			if(code<indexFix)
				return super.modDefault(code);
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case CURRENCY: return currency;
				case AMOUNT: return ""+amount;
				case DENOMINATION: return ""+denomination;
			}
			return "";
		}
		public void modString(int code, String val)
		{
			if(code<indexFix)
			{
				super.modString(code, val);
				return;
			}
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case CURRENCY: currency=val; return;
			}
			return;
		}
		public void modLong(int code, long val)
		{
			if(code<indexFix)
			{
				super.modLong(code, val);
				return;
			}
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case AMOUNT: amount=val; return;
			}
			return;
		}
		public void modDouble(int code, double val)
		{
			if(code<indexFix)
			{
				super.modDouble(code, val);
				return;
			}
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case DENOMINATION: denomination=val; return;
			}
			return;
		}
	}
}
