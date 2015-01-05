package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/** 
 * The interface implemented by the main mud application.  Includes several timing constants.
 * @author Bo Zimmerman
 */

public interface MudHost
{
	public static class IPConnect
	{
		public final String IP;
		public final long time;
		public IPConnect(String s, long t){IP=s;time=t;}
	}
	/** the number of milliseconds between each savethread execution */
	public final static long TIME_SAVETHREAD_SLEEP=60*60000; // 60 minutes, right now.
	/** the number of milliseconds between each utilithread execution */
	public final static long TIME_UTILTHREAD_SLEEP=15*60000; //Tickable.TIME_MILIS_PER_MUDHOUR;

	public int getPort();

	public void shutdown(Session S, boolean keepItDown);

	public String getStatus();
	public long getUptimeSecs();
	public long getUptimeStart();

//	public Vector getOverdueThreads();

	public void setAcceptConnections(boolean truefalse);
	public boolean isAcceptingConnections();

	// Handles a connection from a user, and internal states
	public void acceptConnection(Socket sock) throws SocketException, IOException;
}
