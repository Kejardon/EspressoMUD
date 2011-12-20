package com.planet_ink.coffee_mud.Libraries.interfaces;
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
@SuppressWarnings("unchecked")
public interface ChannelsLibrary extends CMLibrary
{
	public final int QUEUE_SIZE=100;
	
	public int getNumChannels();
	public String getChannelMask(int i);
	public HashSet<ChannelFlag> getChannelFlags(int i);
	public String getChannelName(int i);
	public Vector getChannelQue(int i);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i, boolean offlineOK);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, Session ses, int i);
	public boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly);
	public void channelQueUp(int i, CMMsg msg);
	public int getChannelIndex(String channelName);
	public int getChannelCodeNumber(String channelName);
	public String getChannelName(String channelName);
	public Vector getFlaggedChannelNames(ChannelFlag flag);
	public String getExtraChannelDesc(String channelName);
	public String[][] iChannelsArray();
	public String[] getChannelNames();
	public Vector clearInvalidSnoopers(Session mySession, int channelCode);
	public void restoreInvalidSnoopers(Session mySession, Vector invalid);
	public String parseOutFlags(String mask, HashSet<ChannelFlag> flags);
	public int loadChannels(String list, String ilist);
	public boolean channelTo(Session ses, boolean areareq, int channelInt, CMMsg msg, MOB sender);
	public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg);
	
	public static enum ChannelFlag {
		DEFAULT,SAMEAREA,READONLY,
		EXECUTIONS,LOGINS,LOGOFFS,BIRTHS,MARRIAGES, 
		DIVORCES,CHRISTENINGS,LEVELS,DETAILEDLEVELS,DEATHS,DETAILEDDEATHS,
		CONCEPTIONS,NEWPLAYERS,LOSTLEVELS,PLAYERPURGES,
		WARRANTS, PLAYERREADONLY
	};
}
