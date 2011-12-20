package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public interface Coins extends Item
{
	public long getNumberOfCoins();
	public void setNumberOfCoins(long number);
	public boolean putCoinsBack();
	public long getDenomination();
	public void setDenomination(long valuePerCoin);
	public long getTotalValue();
	public String getCurrency();
	public void setCurrency(String named);
}
