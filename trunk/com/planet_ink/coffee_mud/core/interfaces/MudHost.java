package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
/** 
 * The interface implemented by the main mud application.  Includes several timing constants.
 * @author Bo Zimmerman
 *
 */
@SuppressWarnings("unchecked")
public interface MudHost
{
	/** the number of milliseconds between each savethread execution */
	public final static long TIME_SAVETHREAD_SLEEP=60*60000; // 60 minutes, right now.
	/** the number of milliseconds between each utilithread execution */
	public final static long TIME_UTILTHREAD_SLEEP=Tickable.TIME_MILIS_PER_MUDHOUR;

	// the hostname of the mud server
	// @return hostname or ip address 
	public String getHost();
	public int getPort();

	public void shutdown(Session S, boolean keepItDown);

	public String getStatus();
	public long getUptimeSecs();

	public Vector getOverdueThreads();

	public void setAcceptConnections(boolean truefalse);
	public boolean isAcceptingConnections();

	// Handles a connection from a user, and internal states
	public void acceptConnection(Socket sock) throws SocketException, IOException;
	
}
