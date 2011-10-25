package com.planet_ink.coffee_mud.core.threads;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

//import java.sql.*;
//import java.net.*;
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
@SuppressWarnings("unchecked")
public class Tick extends Thread implements TickableGroup//, Cloneable	//WHHHHYYY WOULD YOU CLONE THIS?!
{
	private final int tickObjectCounter;

	public volatile long lastStop=0;
	public volatile long milliTotal=0;
	public volatile long tickTotal=0;
	public volatile boolean solitaryTicker=false;
	public volatile boolean awake=false;

	private static volatile int tickObjReference=0;
	private volatile SortedList<TockClient> tickers=new SortedList<TockClient>();

	public Tick()
	{
		super("Tick."+(tickObjReference+1));
		tickObjectCounter=tickObjReference++;
		this.start();
	}
	public Tick(String a_name)
	{
		super("Tick."+ a_name + "." +(tickObjReference+1));
		setDaemon(true);
		tickObjectCounter=tickObjReference++;
		this.start();
	}
	public TockClient fetchTickerByIndex(int i)
	{
		int x=0;
		for(TockClient C : tickers)
			if(i==(x++))
				return C;
		return null;
	}

	public Iterator<TockClient> tickers(){return tickers.iterator();}
	public int numTickers(){return tickers.size();}

	public boolean contains(Tickable T)
	{
		return tickers.contains(new TockClient(T,0));
	}

	public int getCounter(){return tickObjectCounter;}

	public boolean delTicker(TockClient C)
	{
		boolean first=tickers.getFirst().equals(C);
		boolean found=tickers.remove(C);
		if(first) this.interrupt();
		return found;
	}
	public void addTicker(TockClient C)
	{
		if(tickers.add(C))
			this.interrupt();
	}

	public String getStatus()
	{
		if(!awake)
			return "Sleeping";
		return "Ticking";
	}

	public void shutdown()
	{
		tickers.clear();
		CMLib.killThread(this,10,1);
	}

	public static boolean tickTicker(TockClient C)
	{
		try
		{
			if(!C.clientObject.tick(C.clientObject,Tickable.TickID.Action))
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

	public void run()
	{
		while(true)
		{
			try
			{
				awake=false;
				TockClient next=tickers.getFirst();
				lastStop=System.currentTimeMillis();
				long timeToSleep=next.nextAction-lastStop;
				if(timeToSleep>0)
					Thread.sleep(timeToSleep);
				awake=true;
				if((CMProps.Bools.MUDSTARTED.property())&&(!CMLib.threads().isAllSuspended()))
				{
					tickTicker(next);
					delTicker(next);
				}
			}
			catch(InterruptedException ioe)
			{
				//If it was interrupted, it is most likely because we need to wake up for a new early ticker, or the previous new ticker got baleeted.
			}
			if(tickers.size()==0)
			{
				if(CMLib.threads() instanceof ServiceEngine)
					((ServiceEngine)CMLib.threads()).delTickGroup(this);
				break;
			}
		}
	}
}
