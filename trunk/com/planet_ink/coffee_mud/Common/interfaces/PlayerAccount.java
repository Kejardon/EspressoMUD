package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.Enumeration;
import java.util.Vector;

import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.interfaces.*;

/**
 * An interface for a base player account.  If this system is enabled, this
 * represents essentially a "container" for various characters, who
 * share a login and potentially an expiration date.
 */
public interface PlayerAccount extends CMCommon, AccountStats, CMModifiable, CMSavable
{
	public Enumeration<MOB> getLoadPlayers();
	public int numPlayers();
//	public Enumeration<String> getPlayers();
	public void addNewPlayer(MOB mob);
	public void delPlayer(MOB mob);
//	public MOB getAccountMob();
	
	/**
	 * Returns whether the name is a player on this account 
	 * @param name the name to check
	 * @return true if it exists and false otherwise
	 */
	public boolean isPlayer(String name);
	public String accountName();
	public void setAccountName(String name);
//	public void setPlayerNames(Vector<String> names);
	
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
	
	/** Constant for account flags that overrides number of characters limitation */
	public final static String FLAG_NUMCHARSOVERRIDE="NUMCHARSOVERRIDE";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_MAXCONNSOVERRIDE="MAXCONNSOVERRIDE";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_ANSI="ANSI";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_ACCOUNTMENUSOFF="ACCOUNTMENUSOFF";

	/** list of account flags */
	public final static String[] FLAG_DESCS = {FLAG_NUMCHARSOVERRIDE,FLAG_MAXCONNSOVERRIDE,FLAG_ANSI,FLAG_ACCOUNTMENUSOFF};
}
