package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.Tick;
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
public interface ThreadEngine extends CMLibrary, Runnable
{
	// tick related
	public void startTickDown(Tickable E, long TICK_TIME);
	public void startArea(Tickable E);
	public void startArea(Tickable E, long TICK_TIME);
	public boolean deleteTick(Tickable E);
	public boolean deleteArea(Tickable E);
	public void suspendAll();
	public void resumeAll();
	public boolean isAllSuspended();
	public String tickInfo(String which);
	public String systemReport(String itemCode);
	public  Iterator<Tick> tickGroups();
	public String getTickStatusSummary(Tickable obj);
	public String getServiceThreadSummary(Thread T);
	public Vector getNamedTickingObjects(String name);
	
	public class SupportThread extends Thread {
		public boolean started=false;
		public boolean shutDown=false;
		public long lastStart=0;
		public long lastStop=0;
		public long milliTotal=0;
		public long tickTotal=0;
		public String status="";
		public boolean debugging=false;
		public long sleepTime=0;
		public Runnable engine=null;
		
		public SupportThread(String name, long sleep, Runnable engine, boolean debugging) {
			this.engine=engine;
			sleepTime=sleep;
			this.debugging=debugging;
			setName(name);
			setDaemon(true);
		}
		
		public void status(String s)
		{
			status=s;
			if(debugging) Log.debugOut(getName(),s);
		}
		
		public boolean shutdown() {
			shutDown=true;
			CMLib.killThread(this,500,30);
			return true;
		}
		
		public void debugDumpStack(Thread theThread)
		{
			// I wish Java had compiler directives.  Would be great to un-comment this for 1.5 JVMs
			java.lang.StackTraceElement[] s=(java.lang.StackTraceElement[])theThread.getStackTrace();
			StringBuffer dump = new StringBuffer("");
			for(int i=0;i<s.length;i++)
				dump.append("\n   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
			Log.debugOut("ThreadEngine",dump.toString());
		}
		
		public void run()
		{
			try {
				lastStart=System.currentTimeMillis();
				if(started)
				{
					Log.errOut(getName(),"DUPLICATE "+getName().toUpperCase()+" RUNNING!!");
					return;
				}
				started=true;
				shutDown=false;
	
				while(!CMProps.Bools.MUDSTARTED.property())
					try{Thread.sleep(1000);}catch(Exception e){}
				lastStart=System.currentTimeMillis();
				while(true)
				{
					try
					{
						while(CMLib.threads().isAllSuspended())
							try{Thread.sleep(2000);}catch(Exception e){}
						if(!CMSecurity.isDisabled(getName()))
						{
//							status("checking database health");
//							String ok=CMLib.database().errorStatus();
//							if((ok.length()!=0)&&(!ok.startsWith("OK")))
//								Log.errOut(getName(),"DB: "+ok);
//							else
							{
								lastStop=System.currentTimeMillis();
								milliTotal+=(lastStop-lastStart);
								tickTotal++;
								status("stopped at "+lastStop+", prev was "+(lastStop-lastStart)+"ms");
								status("sleeping");
								Thread.sleep(sleepTime);
								lastStart=System.currentTimeMillis();
								status("starting run at: "+lastStart);
								engine.run();
							}
						}
						else
						{
							status="sleeping";
							Thread.sleep(sleepTime);
						}
					}
					catch(InterruptedException ioe)
					{
						Log.sysOut(getName(),"Interrupted!");
						if(shutDown)
						{
							shutDown=false;
							started=false;
							break;
						}
					}
					catch(Exception e)
					{
						Log.errOut(getName(),e);
					}
				}
			} finally {
				started=false;
			}
			Log.sysOut(getName(),"Shutdown complete.");
		}
	}
}