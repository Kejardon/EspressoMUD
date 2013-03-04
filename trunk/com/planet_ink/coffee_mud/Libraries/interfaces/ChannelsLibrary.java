package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

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
	public boolean mayReadThisChannel(MOB M, int i);
	//public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i, boolean offlineOK);
	public boolean mayReadThisChannel(Session ses, int i);
	public void channelQueUp(int i, String msg);
	public int getChannelIndex(String channelName);
	public int getChannelCodeNumber(String channelName);
	public String getChannelName(String channelName);
	public Vector getFlaggedChannelNames(ChannelFlag flag);
	public String getExtraChannelDesc(String channelName);
	public String[][] iChannelsArray();
	public String[] getChannelNames();
	//public Vector clearInvalidSnoopers(Session mySession, int channelCode);
	//public void restoreInvalidSnoopers(Session mySession, Vector invalid);
	public String parseOutFlags(String mask, HashSet<ChannelFlag> flags);
	public int loadChannels(String list, String ilist);
	public boolean channelTo(Session ses, int channelInt, String msg);
	public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg);
	
	public static enum ChannelFlag {
		DEFAULT,READONLY,
		EXECUTIONS,LOGINS,LOGOFFS,BIRTHS,MARRIAGES, 
		DIVORCES,CHRISTENINGS,LEVELS,DETAILEDLEVELS,DEATHS,DETAILEDDEATHS,
		CONCEPTIONS,NEWPLAYERS,LOSTLEVELS,PLAYERPURGES,
		WARRANTS, PLAYERREADONLY
	};
}
