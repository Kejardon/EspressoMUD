package com.planet_ink.coffee_mud.Common.interfaces;
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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * An interface for a base player account. Shared by PlayerAccount
 * and PlayerStats (since the account system is optional)  
 */
@SuppressWarnings("unchecked")
public interface AccountStats extends CMCommon, CMSavable, CMModifiable
{
	public final static AccountStats[] dummyASArray=new AccountStats[0];

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
}
