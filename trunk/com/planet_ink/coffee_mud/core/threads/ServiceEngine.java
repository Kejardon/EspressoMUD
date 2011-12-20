package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.http.ProcessHTTPrequest;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class ServiceEngine implements ThreadEngine
{
	public String ID(){return "ServiceEngine";}
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new ServiceEngine();}}
	public void initializeClass(){}
	public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	private ThreadEngine.SupportThread thread=null;
	public void propertiesLoaded(){}
	protected LinkedList<Tick> ticks=new LinkedList<Tick>();
	public Iterator<Tick> tickGroups(){return ticks.iterator();}
	protected LinkedList<TickArea> areas=new LinkedList<TickArea>();
	public Iterator<TickArea> areaGroups(){return areas.iterator();}
	private boolean isSuspended=false;

	public ThreadEngine.SupportThread getSupportThread() { return thread;}

	public void delTickGroup(Tick tock)
	{
		synchronized(ticks)
		{
			ticks.remove(tock);
		}
	}
	public void addTickGroup(Tick tock)
	{
		synchronized(ticks)
		{
			ticks.add(tock);
		}
	}
	public void delArea(TickArea tock)
	{
		synchronized(areas)
		{
			areas.remove(tock);
		}
	}
	public void addArea(TickArea tock)
	{
		synchronized(areas)
		{
			areas.add(tock);
		}
	}
	public Tick getAvailTickThread()
	{
		Tick tock=null;
		Tick almostTock=null;
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			if((almostTock!=null)&&
				(almostTock.numTickers()<TickableGroup.MAX_TICK_CLIENTS)&&(almostTock.getThreadGroup()!=null))
			{
				tock=almostTock;
				break;
			}
		}
		if(tock==null)
		{
			tock=new Tick();
			addTickGroup(tock);
		}
		return tock;
	}

	public void startTickDown(Tickable E, long TICK_TIME)
	{
		TockClient client=new TockClient(E,TICK_TIME);
		getAvailTickThread().addTicker(client);
		return;
	}
	public void startArea(Tickable E){startArea(E, Tickable.TIME_TICK);}
	public void startArea(Tickable E, long TICK_TIME)
	{
		addArea(new TickArea(TICK_TIME,E));
	}

	public boolean deleteTick(Tickable E)
	{
		TockClient equiv=new TockClient(E, 0);
		Iterator set=null;
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			if(e.next().delTicker(equiv))
				return true;
		}
		return false;
	}
	public boolean deleteArea(Tickable E)
	{
		TickArea almostTock=null;
		Iterator set=null;
		for(Iterator<TickArea> e=areaGroups();e.hasNext();)
		{
			almostTock=e.next();
			if(almostTock.clientObject==E)
			{
				delArea(almostTock);
				return true;
			}
		}
		return false;
	}

	public boolean isAllSuspended(){return isSuspended;}
	public void suspendAll(){isSuspended=true;}
	public void resumeAll(){isSuspended=false;}

	public String systemReport(String itemCode)
	{
		long totalMOBMillis=0;
		long totalMOBTicks=0;
		long topMOBMillis=0;
		long topMOBTicks=0;
		MOB topMOBClient=null;
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			totalMOBMillis+=S.getTotalMillis();
			totalMOBTicks+=S.getTotalTicks();
			if(S.getTotalMillis()>topMOBMillis)
			{
				topMOBMillis=S.getTotalMillis();
				topMOBTicks=S.getTotalTicks();
				topMOBClient=S.mob();
			}
		}

		if(itemCode.equalsIgnoreCase("totalMOBMillis"))
			return ""+totalMOBMillis;
		else
		if(itemCode.equalsIgnoreCase("totalMOBMillisTime"))
			return CMLib.english().returnTime(totalMOBMillis,0);
		else
		if(itemCode.equalsIgnoreCase("totalMOBMillisTimePlusAverage"))
			return CMLib.english().returnTime(totalMOBMillis,totalMOBTicks);
		else
		if(itemCode.equalsIgnoreCase("totalMOBTicks"))
			return ""+totalMOBTicks;
		else
		if(itemCode.equalsIgnoreCase("topMOBMillis"))
			return ""+topMOBMillis;
		else
		if(itemCode.equalsIgnoreCase("topMOBMillisTime"))
			return CMLib.english().returnTime(topMOBMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topMOBMillisTimePlusAverage"))
			return CMLib.english().returnTime(topMOBMillis,topMOBTicks);
		else
		if(itemCode.equalsIgnoreCase("topMOBTicks"))
			return ""+topMOBTicks;
		else
		if(itemCode.equalsIgnoreCase("topMOBClient"))
		{
			if(topMOBClient!=null)
				return topMOBClient.name();
			return "";
		}

		int totalTickers=0;
		long totalMillis=0;
		long totalTicks=0;
		int topGroupNumber=-1;
		long topGroupMillis=-1;
		long topGroupTicks=0;
		int num=0;
		Tick almostTock = null;
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			totalTickers+=almostTock.numTickers();
			totalMillis+=almostTock.milliTotal;
			totalTicks+=almostTock.tickTotal;
			if(almostTock.milliTotal>topGroupMillis)
			{
				topGroupMillis=almostTock.milliTotal;
				topGroupTicks=almostTock.tickTotal;
				topGroupNumber=num;
			}
			num++;
		}
		if(itemCode.equalsIgnoreCase("freeMemory"))
			return ""+(Runtime.getRuntime().freeMemory()/1000);
		else
		if(itemCode.equalsIgnoreCase("totalMemory"))
			return ""+(Runtime.getRuntime().totalMemory()/1000);
		else
		if(itemCode.equalsIgnoreCase("totalTime"))
			return ""+CMLib.english().returnTime(System.currentTimeMillis()-CMSecurity.getStartTime(),0);
		else
		if(itemCode.equalsIgnoreCase("startTime"))
			return CMLib.time().date2String(CMSecurity.getStartTime());
		else
		if(itemCode.equalsIgnoreCase("currentTime"))
			return CMLib.time().date2String(System.currentTimeMillis());
		else
		if(itemCode.equalsIgnoreCase("totalTickers"))
			return ""+totalTickers;
		else
		if(itemCode.equalsIgnoreCase("totalMillis"))
			return ""+totalMillis;
		else
		if(itemCode.equalsIgnoreCase("totalMillisTime"))
			return CMLib.english().returnTime(totalMillis,0);
		else
		if(itemCode.equalsIgnoreCase("totalMillisTimePlusAverage"))
			return CMLib.english().returnTime(totalMillis,totalTicks);
		else
		if(itemCode.equalsIgnoreCase("totalTicks"))
			return ""+totalTicks;
		else
		if(itemCode.equalsIgnoreCase("tickgroupsize"))
			return ""+ticks.size();
		else
		if(itemCode.equalsIgnoreCase("topGroupNumber"))
			return ""+topGroupNumber;
		else
		if(itemCode.equalsIgnoreCase("topGroupMillis"))
			return ""+topGroupMillis;
		else
		if(itemCode.equalsIgnoreCase("topGroupMillisTime"))
			return CMLib.english().returnTime(topGroupMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topGroupMillisTimePlusAverage"))
			return CMLib.english().returnTime(topGroupMillis,topGroupTicks);
		else
		if(itemCode.equalsIgnoreCase("topGroupTicks"))
			return ""+topGroupTicks;
		else
		if(itemCode.toLowerCase().startsWith("thread"))
		{
			int xstart="thread".length();
			int xend=xstart;
			while((xend<itemCode.length())&&(Character.isDigit(itemCode.charAt(xend))))
				xend++;
			int threadNum=CMath.s_int(itemCode.substring(xstart,xend));
			int curThreadNum=0;
			for(Enumeration e=CMLib.libraries();e.hasMoreElements();)
			{
				CMLibrary lib=(CMLibrary)e.nextElement();
				ThreadEngine.SupportThread thread=lib.getSupportThread();
				if(thread!=null) {
					if(curThreadNum==threadNum) {
						String instrCode=itemCode.substring(xend);
						if(instrCode.equalsIgnoreCase("miliTotal"))
							return ""+thread.milliTotal;
						if(instrCode.equalsIgnoreCase("milliTotal"))
							return ""+thread.milliTotal;
						if(instrCode.equalsIgnoreCase("status"))
							return ""+thread.status;
						if(instrCode.equalsIgnoreCase("name"))
							return ""+thread.getName();
						if(instrCode.equalsIgnoreCase("MilliTotalTime"))
							return CMLib.english().returnTime(thread.milliTotal,0);
						if(instrCode.equalsIgnoreCase("MiliTotalTime"))
							return CMLib.english().returnTime(thread.milliTotal,0);
						if(instrCode.equalsIgnoreCase("MilliTotalTimePlusAverage"))
							return CMLib.english().returnTime(thread.milliTotal,thread.tickTotal);
						if(instrCode.equalsIgnoreCase("MiliTotalTimePlusAverage"))
							return CMLib.english().returnTime(thread.milliTotal,thread.tickTotal);
						if(instrCode.equalsIgnoreCase("TickTotal"))
							return ""+thread.tickTotal;
						break;
					}
					curThreadNum++;
				}
			}
		}
		return "";
	}

	public String tickInfo(String which)
	{
		int grpstart=-1;
		for(int i=0;i<which.length();i++)
			if(Character.isDigit(which.charAt(i)))
			{
				grpstart=i;
				break;
			}
		if(which.equalsIgnoreCase("tickGroupSize"))
			return ""+ticks.size();
		else
		if(which.toLowerCase().startsWith("tickerssize"))
		{
			if(grpstart<0) return"";
			int group=CMath.s_int(which.substring(grpstart));
			if((group>=0)&&(group<ticks.size()))
				return ""+((Tick)ticks.get(group)).numTickers();
			return "";
		}
		int group=-1;
		int client=-1;
		int clistart=which.indexOf("-");
		if((grpstart>=0)&&(clistart>grpstart))
		{
			group=CMath.s_int(which.substring(grpstart,clistart));
			client=CMath.s_int(which.substring(clistart+1));
		}

		if((group<0)||(client<0)||(group>=ticks.size())) return "";
		Tick almostTock=(Tick)ticks.get(group);

		if(client>=almostTock.numTickers()) return "";
		TockClient C=almostTock.fetchTickerByIndex(client);
		if(C==null) return "";

		if(which.toLowerCase().startsWith("tickername"))
		{
			Tickable E=C.clientObject;
			if(E instanceof Room)
				return ((Room)E).roomID();
			if(E!=null) return E.ID();
			return "!NULL!";
		}
		else
		if(which.toLowerCase().startsWith("tickerstatus"))
			return ((C.clientObject==null)?"":(""+C.clientObject.getTickStatus()));
		return "";
	}

	public boolean shutdown() {
		//int numTicks=tickGroup.size();
		int which=0;
		while(ticks.size()>0)
		{
			//Log.sysOut("ServiceEngine","Shutting down all tick "+which+"/"+numTicks+"...");
			Tick tock=ticks.getFirst();
			if(tock!=null)
			{
				CMProps.Strings.MUDSTATUS.setProperty("Shutting down...shutting down Service Engine: killing Tick#" + tock.getCounter()+": "+tock.getStatus());
				tock.shutdown();
			}
			try{Thread.sleep(100);}catch(Exception e){}
			which++;
		}
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...shutting down Service Engine: "+ID()+": thread shutdown");
		thread.shutdown();
		Log.sysOut("ServiceEngine","Shutdown complete.");
		return true;
	}

	public Vector getNamedTickingObjects(String name)
	{
		Vector V=new Vector();
		Tick almostTock=null;
		TockClient C=null;
		name=name.toUpperCase().trim();
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			for(Iterator i=almostTock.tickers();i.hasNext();)
			{
				C=(TockClient)i.next();
				if((C.clientObject!=null)
				&&(C.clientObject.ID().toUpperCase().indexOf(name)>=0)
				&&(!V.contains(C.clientObject)))
					V.addElement(C.clientObject);
			}
		}
		return V;
	}

	public String getTickStatusSummary(Tickable obj)
	{
		if(obj==null) return "";
		return obj.getTickStatus().toString();
	}
	public String getServiceThreadSummary(Thread T)
	{
		if(T instanceof ThreadEngine.SupportThread)
			return " ("+((ThreadEngine.SupportThread)T).status+")";
		else
		if(T instanceof MudHost)
			return " ("+((MudHost)T).getStatus()+")";
		else
		if(T instanceof ExternalHTTPRequests)
			return " ("+((ExternalHTTPRequests)T).getHTTPstatus()+" - "+((ExternalHTTPRequests)T).getHTTPstatusInfo()+")";
		return "";

	}

	public void insertOrderDeathInOrder(DVector DV, long lastStart, String msg, Tick tock)
	{
		if(DV.size()>0)
		for(int i=0;i<DV.size();i++)
		{
			if(((Long)DV.elementAt(i,1)).longValue()>lastStart)
			{
				DV.insertElementAt(i,Long.valueOf(lastStart),msg,tock);
				return;
			}
		}
		DV.addElement(Long.valueOf(lastStart),msg,tock);
	}

	public void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis()-(5*TimeManager.MILI_MINUTE);
		long longerDateTime=System.currentTimeMillis()-(120*TimeManager.MILI_MINUTE);
		thread.status("checking");

		thread.status("checking tick groups.");
		DVector orderedDeaths=new DVector(3);
		try
		{
			Tick almostTock = null;
			for(Iterator<Tick> e=tickGroups();e.hasNext();)
			{
				almostTock=e.next();
				if((almostTock.awake)
				&&(almostTock.lastStop<lastDateTime))
				{
					insertOrderDeathInOrder(orderedDeaths,0,"LOCKED GROUP "+almostTock.getCounter()+"! No further information.",almostTock);
					// no isDEBUGGING check -- just always let her rip.
					thread.debugDumpStack(almostTock);
				}
			}
		}
		catch(java.util.NoSuchElementException e){}
		for(int i=0;i<orderedDeaths.size();i++)
			Log.errOut(thread.getName(),(String)orderedDeaths.elementAt(i,2));

		thread.status("killing tick groups.");
		for(int x=0;x<orderedDeaths.size();x++)
		{
			Tick almostTock=(Tick)orderedDeaths.elementAt(x,3);
			Vector objs=new Vector();
			try{
				for(Iterator e=almostTock.tickers();e.hasNext();)
					objs.addElement(e.next());
			}catch(NoSuchElementException e){}
			almostTock.shutdown();
			if(CMLib.threads() instanceof ServiceEngine)
				((ServiceEngine)CMLib.threads()).delTickGroup(almostTock);
			for(int i=0;i<objs.size();i++)
			{
				TockClient c=(TockClient)objs.elementAt(i);
				CMLib.threads().startTickDown(c.clientObject,c.nextAction);
			}
		}

		thread.status("Checking mud threads");
		for(int m=0;m<CMLib.hosts().size();m++)
		{
			Vector badThreads=((MudHost)CMLib.hosts().elementAt(m)).getOverdueThreads();
			if(badThreads.size()>0)
			{
				for(int b=0;b<badThreads.size();b++)
				{
					Thread T=(Thread)badThreads.elementAt(b);
					String threadName=T.getName();
					if(T instanceof Tickable)
						threadName=((Tickable)T).ID()+" ("+((Tickable)T).ID()+"): "+((Tickable)T).getTickStatus();
					thread.status("Killing "+threadName);
					Log.errOut("Killing stray thread: "+threadName);
					CMLib.killThread(T,100,1);
				}
			}
		}
		thread.status("Done checking threads");
	}

	public void run()
	{
		while(isAllSuspended())
			try{Thread.sleep(2000);}catch(Exception e){}

		if((!CMSecurity.isDisabled("UTILITHREAD"))
		&&(!CMSecurity.isDisabled("THREADTHREAD")))
		{
			checkHealth();
			Resources.removeResource("SYSTEM_HASHED_MASKS");
		}
	}

	public boolean activate() {
		if(thread==null)
			thread=new ThreadEngine.SupportThread("THThreads",
					MudHost.TIME_UTILTHREAD_SLEEP, this, CMSecurity.isDebugging("UTILITHREAD"));
		if(!thread.started)
			thread.start();
		return true;
	}
}