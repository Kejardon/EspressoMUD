package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


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
public class TickArea extends Thread implements TickableGroup, Cloneable
{
	private final int tickObjectCounter;
	private static volatile int tickObjReference=0;

	public final Tickable clientObject;
	public final Tickable.TickID tickID=Tickable.TickID.Time;
	public long TICK_TIME;
	public boolean suspended=false;
	public volatile long lastStart=0;
	public volatile long lastStop=0;
	public volatile long milliTotal=0;
	public volatile long tickTotal=0;
	public volatile boolean awake=false;

	public TickArea(long sleep, Tickable CO)
	{
		super("Tick."+(tickObjReference+1));
		clientObject=CO;
		tickObjectCounter=tickObjReference++;
		TICK_TIME=sleep;
		this.start();
	}
	public TickArea(String a_name, long sleep, Tickable CO)
	{
		super("Tick."+ a_name + "." +(tickObjReference+1));
		clientObject=CO;
		setDaemon(true);
		tickObjectCounter=tickObjReference++;
		TICK_TIME=sleep;
		this.start();
	}

	public static boolean tickTicker(Tickable C)
	{
		try
		{
			if(!C.tick(C,Tickable.TickID.Time))
			{
				return true;
			}
		}
		catch(Exception t)
		{
			Log.errOut("ServiceEngine",t);
		}
		return false;
	}

	public boolean equals(Object obj)
	{
		if(obj instanceof TickArea)
			return clientObject == ((TickArea)obj).clientObject;
		return false;
	}
	public void shutdown()
	{
		CMLib.killThread(this,10,1);
	}
	public void run()
	{
		lastStart=System.currentTimeMillis();
		boolean going=true;
		int SUBTRACT_TIME=0;
		while(going)
		{
			try
			{
				lastStop=System.currentTimeMillis();
				SUBTRACT_TIME+=(lastStop-lastStart);
				milliTotal+=SUBTRACT_TIME;
				tickTotal++;
				awake=false;
				long timeToSleep=TICK_TIME;
				if(SUBTRACT_TIME<timeToSleep)
				{
					timeToSleep-=SUBTRACT_TIME;
					SUBTRACT_TIME=0;
				}
				else
				{
					timeToSleep=0;
				}
				if(timeToSleep>0)
					Thread.sleep(timeToSleep);
				awake=true;
				lastStart=System.currentTimeMillis();
				if((CMProps.Bools.MUDSTARTED.property())
				&&(!CMLib.threads().isAllSuspended()))
				{
					if(tickTicker(clientObject))
					{
						if(CMLib.threads() instanceof ServiceEngine)
							((ServiceEngine)CMLib.threads()).delArea(this);
						going=false;
					}
				}
			}
			catch(InterruptedException ioe)
			{
				// a perfectly good and normal thing
			}
		}
	}
}
