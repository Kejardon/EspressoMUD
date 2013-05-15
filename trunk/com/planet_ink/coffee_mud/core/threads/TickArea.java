package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
Container object for areas to make sure they tick on time - every 4 seconds by default
*/
public class TickArea extends Thread implements TickableGroup, Cloneable
{
	public final int tickObjectCounter;
	private static volatile int tickObjReference=0;

	public final Tickable clientObject;
	//public final Tickable.TickID tickID=Tickable.TickID.Time;
	public long TICK_TIME;
	public boolean suspended=false;
	//public long lastStart=0;
	public long lastStop=0;
	public long milliTotal=0;
	public long tickTotal=0;
	public boolean awake=false;
	public boolean dead=false;

	public TickArea(long sleep, Tickable CO)
	{
		super("TickArea."+(tickObjReference+1));
		clientObject=CO;
		tickObjectCounter=tickObjReference++;
		TICK_TIME=sleep;
		this.start();
	}
/*	public TickArea(String a_name, long sleep, Tickable CO)
	{
		super("Tick."+ a_name + "." +(tickObjReference+1));
		clientObject=CO;
		setDaemon(true);
		tickObjectCounter=tickObjReference++;
		TICK_TIME=sleep;
		this.start();
	} */

	public static boolean tickTicker(Tickable C)
	{
		try
		{
			if(!C.tick(C.tickCounter()+1))
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
	public void shutdown()	//Important note: This may block for as long as 4 seconds if it is locked up.
	{
		dead=true;
		CMLib.killThread(this,100,40);
	}
	public void run()
	{
		lastStop=System.currentTimeMillis();
		long nextStart=lastStop;
		try
		{
			while(!dead)
			{
				awake=false;
				milliTotal+=(System.currentTimeMillis()-lastStop);
				tickTotal++;
				long timeToSleep=nextStart-System.currentTimeMillis();
				if(timeToSleep>10)
					Thread.sleep(timeToSleep);
				lastStop=System.currentTimeMillis();
				nextStart+=TICK_TIME;
				awake=true;
				if((CMProps.Bools.MUDSTARTED.property())
				&&(!CMLib.threads().isAllSuspended()))
				{
					if(tickTicker(clientObject))
					{
						CMLib.threads().delArea(this);
						dead=true;
					}
				}
			}
		}
		catch(InterruptedException ioe){}	//Means this thread is due to die.
	}
}
