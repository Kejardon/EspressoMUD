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
// Variables important for Player Characters, not applicable to NPCs

public interface PlayerStats extends CMCommon, CMModifiable, CMSavable, AccountStats
{
	public static final PlayerStats[] dummyPSArray=new PlayerStats[0];
	
	public MOB mob();
	public void setMOB(MOB m);
	public long lastUpdated();
	public void setLastUpdated(long time);
	// Returns a bitmask of channels turned on/off. (32 channels supported)
	public int getChannelMask();
	// Sets the bitmask of channels turned on/off. (32 channels supported)
	public void setChannelMask(int newMask);
	
	/* Returns a custom coded string detailing the changes to the official
	 * color code that apply to this player only.  The format is the Color
	 * Code Letter (the one after the ^ character) followed by the ansi color
	 * from the basic set, followed by a # character, repeated.
	 */
	public String getColorStr();
	/* Sets a custom coded string detailing the changes to the official
	 * color code that apply to this player only.  The format is the Color
	 * Code Letter (the one after the ^ character) followed by the ansi color
	 * from the basic set, followed by a # character, repeated.
	 */
	public void setColorStr(String color);

	public int getWrap();
	public void setWrap(int newWrap);
	public int getPageBreak();
	public void setPageBreak(int newBreak);

	// Returns the custom prompt, an encoded string, for this player.  "" means default is used.
	public String getPrompt();

	// Sets the custom prompt, an encoded string, for this player.  "" means default is used.
	public void setPrompt(String prompt);

	public LinkedList<String> getTellStack();
	public void addTellStack(String msg);
	//public Vector<String> getGTellStack();
	//public void addGTellStack(String msg);
	// Returns the last MOB player who sent this player a message.
	public MOB replyTo();
	// Sets the last MOB player who sent this player a message and how.
	public void setReplyTo(MOB mob, int replyType);
	// Returns the type of private message last sent to this player
	public int replyType();
	// Returns the last time a player sent this player a message.
	public long replyTime();

	public HashSet<String> getSecurityGroups();
//	public boolean hasVisited(Room R);
//	public boolean hasVisited(Area A);
//	public int percentVisited(MOB mob, Area A);
//	public void addRoomVisit(Room R);

	public String[] getAliasNames();
	public String getAlias(String named);
	public void delAliasName(String named);
	public void setAlias(String named, String value);

	public boolean isIntroducedTo(MOB someguy);
	public void introduceTo(MOB someguy);

	public boolean addFriend(MOB M);
	public boolean addIgnoredBy(AccountStats M);
	public boolean removeIgnoredBy(AccountStats M);
	public AccountStats[] getIgnoredBy();

	public PlayerAccount getAccount();
	public void setAccount(PlayerAccount account);

//	public boolean sameAs(PlayerStats E);

	public int getBitmap();
	public void setBitmap(int newmap);
	public void setBits(int bits, boolean set);
	public boolean hasBits(int bits);

	//Message type 'enum': Sayto, Yell, or Tell
	public static final int REPLY_SAY=0;
	public static final int REPLY_YELL=1;
	public static final int REPLY_TELL=2;

	//Up to 32 values allowed.
	public static final int ATT_ANSI=1;
	public static final int ATT_SOUND=ATT_ANSI*2;
	public static final int ATT_MXP=ATT_SOUND*2;
	public static final int ATT_SYSOPMSGS=ATT_MXP*2;
	public static final int ATT_GOAHEAD=ATT_SYSOPMSGS*2;
	public static final int ATT_DARKENDISPLAY=ATT_GOAHEAD*2;
	//6 used so far
}