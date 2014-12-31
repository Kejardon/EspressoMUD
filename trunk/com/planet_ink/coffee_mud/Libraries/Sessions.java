package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Sessions extends StdLibrary implements Runnable
{
	public final static long sleepTime=50;	//Check if there is input from users 20 times a second

	public String ID(){return "Sessions";}
	
//	protected int lastSize=0;
	
	private SupportThread thread=null;
	public Vector<Session> all=new Vector();
	public Session[] sessArray=Session.dummySessionArray;
	
	public SupportThread getSupportThread() { return thread;}
	
	public Session elementAt(int x)
	{
		return (x<all.size())?all.get(x):null;
	}
	public int size()
	{
		return all.size();
	}
	public Session[] toArray()
	{
		synchronized(all)
		{ return ((sessArray==null)?(sessArray=(Session[])all.toArray(Session.dummySessionArray)):(sessArray)); }
	}
	public void addElement(Session S)
	{
		synchronized(all)
		{
			all.add(S);
			sessArray=null;
		}
	}
	public void removeElementAt(int x)
	{
		synchronized(all)
		{
			all.remove(x);
			sessArray=null;
		}
	}
	public void removeElement(Session S)
	{
		synchronized(all)
		{
			if(all.remove(S))
				sessArray=null;
		}
	}
	//public Enumeration sessions() { return ((Vector)all.clone()).elements();}	//Use toArray
	
	//This spawns/calls another thread to do the work so it doesn't have to pause with checking/updating other sessions
	public void stopSessionAtAllCosts(Session S)
	{
		if(S==null) return;
		removeElement(S);
		CMClass.threadPool.execute(new SessionStopper(S));
	}
	private static class SessionStopper implements Runnable
	{
		Session S;
		public SessionStopper(Session ses){S=ses;}
		public void run()
		{
			S.kill(false);
			try{Thread.sleep(1000);}catch(Exception e){}
			int tries=5;
			while((S.getStatus()!=Session.STATUS_LOGOUTFINAL)
			&&((--tries)>=0))
			{
				S.kill(true);
				try{Thread.sleep(100);}catch(Exception e){}
			}
		}
	}
	public boolean activate() {
		if(thread==null)
			thread=new SupportThread("THSessions", 60000, this, CMSecurity.isDebugging("UTILITHREAD"));
		if(!thread.started)
			thread.start();
		return true;
	}
	
	public boolean shutdown() {
		thread.shutdown();
		return true;
	}
	
	public Session findPlayerOnline(String srchStr, boolean exactOnly)
	{
		// then look for players
		for(Session thisSession : toArray())
		{
			if((thisSession.mob()!=null) && (!thisSession.killFlag())
			&&(thisSession.mob().location()!=null)
			&&(thisSession.mob().name().equalsIgnoreCase(srchStr)))
				return thisSession;
		}
		// keep looking for players
		if(!exactOnly)
			for(int s=0;s<size();s++)
			{
				Session thisSession=elementAt(s);
				if((thisSession.mob()!=null)&&(!thisSession.killFlag())
				&&(thisSession.mob().location()!=null)
				&&(CMLib.english().containsString(thisSession.mob().name(),srchStr)))
					return thisSession;
			}
		return null;
	}
	
	public void run()
	{
		thread.status("checking player sessions.");
		for(Session S : toArray())
		{
			if(S==null) continue;
			long time=System.currentTimeMillis()-S.lastLoopTime();
			if(time>60000)
			{
				String prev=S.previousCMD();
				MOB mob=S.mob();
				if(prev==null) prev="";
				if((mob!=null)||(S.getStatus()==Session.STATUS_ACCOUNTMENU))
				{
					long check=60000;

					if(time>(check*10))
					{
						if((prev.length()==0)||(S.getStatus()==Session.STATUS_LOGIN)||(S.getStatus()==Session.STATUS_ACCOUNTMENU))
							Log.errOut(thread.getName(),"Kicking out: "+((mob==null)?"Unknown":mob.name())+" who has spent "+time+" millis out-game.");
						else
						{
							int roomID=(mob!=null&&mob.location()!=null)?mob.location().saveNum():0;
							Log.errOut(thread.getName(),"KILLING DEAD Session: "+((mob==null)?"Unknown":mob.name())+" ("+roomID+"), out for "+time);
							Log.errOut(thread.getName(),"STATUS  was :"+S.getStatus()+", LASTCMD was :"+prev);
							if(S instanceof Thread)
								thread.debugDumpStack((Thread)S);
						}
						stopSessionAtAllCosts(S);
						continue;
					}
					else if(time>check)
					{
						if(mob==null)
						{
							stopSessionAtAllCosts(S);
							continue;
						}
						else if((S.isLockedUpWriting())&&(CMLib.flags().isInTheGame(mob,true)))
						{
							int roomID=(mob.location()!=null)?mob.location().saveNum():0;
							Log.errOut(thread.getName(),"LOGGED OFF Session: "+((mob==null)?"Unknown":mob.name())+" ("+roomID+"), out for "+time+": "+S.isLockedUpWriting());
							if((S.getStatus()!=1)||(prev.length()>0))
								Log.errOut(thread.getName(),"STATUS  is :"+S.getStatus()+", LASTCMD was :"+prev);
							else
								Log.errOut(thread.getName(),"STATUS  is :"+S.getStatus()+", no last command available.");
							stopSessionAtAllCosts(S);
							continue;
						}
					}
				}
				else
				{
					if(S.getStatus()==Session.STATUS_LOGIN)
						Log.errOut(thread.getName(),"Kicking out login session after "+time+" millis.");
					else
					{
						int roomID=(mob!=null&&mob.location()!=null)?mob.location().saveNum():0;
						Log.errOut(thread.getName(),"KILLING DEAD Session: "+((mob==null)?"Unknown":mob.name())+" ("+roomID+"), out for "+time);
						if(S instanceof Thread)
							thread.debugDumpStack((Thread)S);
					}
					if((S.getStatus()!=1)||(prev.length()>0))
						Log.errOut(thread.getName(),"STATUS  was :"+S.getStatus()+", LASTCMD was :"+prev);
					stopSessionAtAllCosts(S);
					continue;
				}
			}
			//S.checkInput();	//putting this back into the session's run() method
		}
	}
}