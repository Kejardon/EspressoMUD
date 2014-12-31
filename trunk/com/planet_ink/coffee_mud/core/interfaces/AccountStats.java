package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * An interface for a base player account. Shared by PlayerAccount
 * and PlayerStats (since the account system is optional)  
 */

public interface AccountStats extends CMCommon, CMSavable, CMModifiable
{
	public final static AccountStats[] dummyASArray=new AccountStats[0];

	@Override public AccountStats copyOf();
	@Override public AccountStats newInstance();
	public long lastDateTime();
	public void setLastDateTime(long C);
	public long lastUpdated();
	public void setLastUpdated(long time);
	public String password();
	public void setPassword(String newPassword);
	public byte[] lastIP();
	public void setLastIP(byte[] ip);
	public boolean hasFriend(AccountStats M);
	public boolean addFriend(AccountStats M);
	public boolean removeFriend(AccountStats M);
	public AccountStats removeFriend(String M);
	public AccountStats[] getFriends();
	public boolean hasIgnored(MOB M);
	public boolean addIgnored(MOB M);
	public boolean removeIgnored(MOB M);
	public MOB[] getIgnored();
	/*public HashSet<MOB> getFriends();
	public HashSet<MOB> getIgnored();
	public Vector<AccountStats> getIgnoredBy(); */
	
	public int getBitmap();
	public void setBitmap(int newmap);
	public void setBits(int bits, boolean set);
	public boolean hasBits(int bits);
}
