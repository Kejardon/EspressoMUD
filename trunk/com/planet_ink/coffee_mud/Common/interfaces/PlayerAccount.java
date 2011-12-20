package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.Enumeration;
import java.util.Vector;

import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * An interface for a base player account.  If this system is enabled, this
 * represents essentially a "container" for various characters, who
 * share a login.
 * See also AccountStats
 */
public interface PlayerAccount extends CMCommon, AccountStats, CMModifiable, CMSavable
{
	public Enumeration<MOB> getLoadPlayers();
	public int numPlayers();
	public void addNewPlayer(MOB mob);
	public void delPlayer(MOB mob);
	public boolean isPlayer(String name);
	public String accountName();
	public void setAccountName(String name);
	
	/**
	 * Checks whether the given string flag is set for this account.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount#setFlag(String, boolean)
	 * @param flagName the flag name
	 * @return true if it is set, false if not
	 */
	public boolean isSet(String flagName);
	
	/**
	 * Sets or unsets an account-wide flag.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount#isSet(String)
	 * @param flagName the flag name
	 * @param setOrUnset true to set it, false to unset
	 */
	public void setFlag(String flagName, boolean setOrUnset);
	
	public final static String FLAG_NUMCHARSOVERRIDE="NUMCHARSOVERRIDE";
	public final static String FLAG_MAXCONNSOVERRIDE="MAXCONNSOVERRIDE";
	public final static String FLAG_ANSI="ANSI";
	public final static String FLAG_ACCOUNTMENUSOFF="ACCOUNTMENUSOFF";

	/** list of account flags */
	public final static String[] FLAG_DESCS = {FLAG_NUMCHARSOVERRIDE,FLAG_MAXCONNSOVERRIDE,FLAG_ANSI,FLAG_ACCOUNTMENUSOFF};
}
