/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.planet_ink.coffee_mud.core;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class SupportThread extends Thread
{
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

	public static void debugDumpStack(Thread theThread)
	{
		Log.debugOut("SupportThread",getStack(theThread));
	}
	public static String getStack(Thread theThread)
	{
		java.lang.StackTraceElement[] s=(java.lang.StackTraceElement[])theThread.getStackTrace();
		StringBuffer dump = new StringBuffer("");
		for(int i=0;i<s.length;i++)
			dump.append("\n   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
		return dump.toString();
	}		
	public void run()
	{
		try
		{
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

			try{ while(true) {
				try{ while(true) {
					while(CMLib.threads().isAllSuspended())
						try{Thread.sleep(2000);}catch(Exception e){}
//					if(!CMSecurity.isDisabled(getName()))
					{
						lastStop=System.currentTimeMillis();
						long intermediate=(lastStop-lastStart);
						milliTotal+=intermediate;
						tickTotal++;
						status("sleeping");
						if(intermediate<sleepTime)
						{
							Thread.sleep(sleepTime-intermediate);
							lastStart+=sleepTime;
						}
						else
							lastStart=System.currentTimeMillis();
						status("running");
						engine.run();
					}
//					else
//					{
//						status="sleeping";
//						Thread.sleep(sleepTime);
//					}
				} }
				catch(Exception e)
				{
					if(e instanceof InterruptedException) throw (InterruptedException)e;
					Log.errOut(getName(),e);
				}
			} }
			catch(InterruptedException ioe)
			{
				Log.sysOut(getName(),"Interrupted!");
				if(shutDown)
				{
					shutDown=false;
					started=false;
				}
			}
		} finally { started=false; }
		Log.sysOut(getName(),"Shutdown complete.");
	}
}
