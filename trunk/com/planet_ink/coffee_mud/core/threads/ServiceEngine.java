package com.planet_ink.coffee_mud.core.threads;
//import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.http.ProcessHTTPrequest;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.*;
import java.util.concurrent.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class ServiceEngine implements CMLibrary, Runnable
{
	public String ID(){return "ServiceEngine";}
	private SupportThread thread=null;
	//protected HashedList<Tick> ticks=new HashedList<Tick>();
	protected HashedList<TickArea> areas=new HashedList<TickArea>();
	protected HashedList<Exit> exits=new HashedList<Exit>();
	protected HashedList<TimeClock> clocks=new HashedList<TimeClock>();
	protected static boolean isSuspended=false;
	protected GlobalTicker mainTime=new GlobalTicker();	//Also handles globalTickCount

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new ServiceEngine();}}
	public void initializeClass(){}
	public void finalInitialize(){}
	public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void propertiesLoaded(){}
	//public Iterator<Tick> tickGroups(){return ticks.iterator();}
	public Iterator<TickArea> areaGroups(){return areas.iterator();}
	public SupportThread getSupportThread() { return thread;}
	protected class GlobalTicker extends Thread //implements Tickable
	{
		protected boolean dead=false;
		public int globalTickCount=1;
		//public int globalTickCount(){return globalTickCount;}
		public void interrupt(){dead=true; super.interrupt();}
		public void run(){
			long nextStart=System.currentTimeMillis();
			try {
				while(!dead) {
					long timeToSleep=nextStart-System.currentTimeMillis();
					if(timeToSleep>10) Thread.sleep(timeToSleep);
					nextStart+=Tickable.TIME_TICK;
					if((CMProps.Bools.MUDSTARTED.property())
					&&(!CMLib.threads().isAllSuspended())) {
						globalTickCount++;
						for(Iterator<Exit> iter=exits.iterator();iter.hasNext();) {
							Exit exit=iter.next();
							try{ if(!exit.tick(globalTickCount)) exits.remove(exit); }
							catch(Exception e){Log.errOut("ServiceEngine",e.toString());} }
						for(Iterator<TimeClock> iter=clocks.iterator();iter.hasNext();) {
							TimeClock clock=iter.next();
							try{ if(!clock.tick(globalTickCount)) clocks.remove(clock); }
							catch(Exception e){Log.errOut("ServiceEngine",e.toString());} } } } }
			catch(InterruptedException e){} }
	}
	
	protected ConcurrentSkipListSet<TickActer> tickActQueue=new ConcurrentSkipListSet(new Comparator<TickActer>()
	{
		public int compare(TickActer a, TickActer b)
		{
			if(a==b) return 0;
			int i=(int)(a.nextAct()-b.nextAct());	//lower value == lower time to go == lower in queue == higher priority
			return (i==0?1:i);
		}
		public boolean equals(TickActer a, TickActer b)
		{
			return a==b;
		}
	});
	public void startTickDown(TickActer E)
	{
		if(E.nextAct()<=System.currentTimeMillis())
		{
			CMClass.threadPool.execute(E);
			return;
		}
		tickActQueue.add(E);
		if(tickActQueue.first()==E) tickActThread.interrupt();
	}
	public boolean deleteTick(TickActer E)
	{
		boolean interruptLater=(tickActQueue.first()==E);
		boolean found=tickActQueue.remove(E);
		if(interruptLater&&found) tickActThread.interrupt();
		return found;
	}
	protected Thread tickActThread=new TickActThread();
	protected class TickActThread extends Thread
	{
		protected boolean awake=false;
		protected boolean dead=false;
		public void shutdown() {
			tickActQueue.clear();
			dead=true;
			CMLib.killThread(this,10,1); }
		public boolean dead(){return dead;}
		public boolean awake(){return awake;}
		public void run() {
			while(true){
				try {
					while(!tickActQueue.isEmpty()) {
						awake=false;
						TickActer next=tickActQueue.first();
						long timeToSleep=next.nextAct()-System.currentTimeMillis();
						if(timeToSleep>0) Thread.sleep(timeToSleep);
						awake=true;
						nextTicker:
						if((CMProps.Bools.MUDSTARTED.property())&&(!isSuspended)) {
							if(!tickActQueue.remove(next)) break nextTicker;
							CMClass.threadPool.execute(next); } } }
							/*try { next.tickAct(); }
							catch(Exception t) { Log.errOut("ServiceEngine",t); }*/
				catch(InterruptedException ioe) { }
				//If it was interrupted, it is most likely because we need to wake up for a new early ticker, or the previous new ticker got baleeted.
				//Interruptions will only come if the thread is sleeping though.
				//NOTE: tickAct() should NEVER call a sleep (nor take any significant amount of time anyways)!
				if(dead) {
					awake=false;
					break; }
				synchronized(tickActQueue) {
					while(tickActQueue.isEmpty()) try{tickActQueue.wait();}catch(InterruptedException ioe){} } } }
	};
	public int globalTickCount(){return mainTime.globalTickCount;}
	/*public void delTickGroup(Tick tock)
	{
		synchronized(ticks) { ticks.remove(tock); }
	}
	public void addTickGroup(Tick tock)
	{
		synchronized(ticks) { ticks.add(tock); }
	}*/
	public void delArea(TickArea tock)
	{
		synchronized(areas) { areas.remove(tock); }
	}
	public void addArea(TickArea tock)
	{
		synchronized(areas) { areas.add(tock); }
	}
	public void delExit(Exit exit)
	{
		synchronized(exits) { exits.remove(exit); }
	}
	public void addExit(Exit exit)
	{
		synchronized(exits) { exits.add(exit); }
	}
	public void delClock(TimeClock clock)
	{
		synchronized(clocks) { clocks.remove(clock); }
	}
	public void addClock(TimeClock clock)
	{
		synchronized(clocks) { clocks.add(clock); }
	}
	/*public Tick getAvailTickThread()
	{
		Tick tock=null;
		Tick almostTock=null;
		for(Iterator<Tick> e=ticks.iterator();e.hasNext();)
		{
			almostTock=e.next();
			if((almostTock.numTickers()<TickableGroup.MAX_TICK_CLIENTS)&&(!almostTock.dead()))
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
	}*/

	/*public void startTickDown(TickActer E, long TICK_TIME)
	{
		//TockClient client=new TockClient(E,TICK_TIME);
		getAvailTickThread().addTicker(E);
		return;
	}*/
	public void startArea(Tickable E){startArea(E, Tickable.TIME_TICK);}
	public void startArea(Tickable E, long TICK_TIME)
	{
		addArea(new TickArea(TICK_TIME,E));
	}

	/*public boolean deleteTick(TickActer E)
	{
		//TockClient equiv=new TockClient(E, 0);
		for(Iterator<Tick> e=ticks.iterator();e.hasNext();)
		{
			if(e.next().delTicker(E))
				return true;
		}
		return false;
	}*/
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
				almostTock.dead=true;
				if(Thread.currentThread()!=almostTock) almostTock.shutdown();
				return true;
			}
		}
		return false;
	}

	public boolean isAllSuspended(){return isSuspended;}
	public void setSuspended(boolean b){isSuspended=b;}
	//public void suspendAll(){isSuspended=true;}
	//public void resumeAll(){isSuspended=false;}

	public String[] mobTimes()
	{
		long totalMOBMillis=0;
		long topMOBMillis=0;
		String topMOBClient="";
		for(Session S : CMLib.sessions().toArray())
		{
			totalMOBMillis+=S.getTotalMillis();
			if(S.getTotalMillis()>topMOBMillis)
			{
				topMOBMillis=S.getTotalMillis();
				MOB M=S.mob();
				topMOBClient=(M==null)?S.getAddress():M.name();
			}
		}
		return new String[]{CMLib.english().returnTime(totalMOBMillis,0), CMLib.english().returnTime(topMOBMillis,0), topMOBClient};
	}
	public long[] memoryUse()
	{
		return new long[]{Runtime.getRuntime().freeMemory(), Runtime.getRuntime().totalMemory()};
	}
	/*public int numTickGroups()
	{
		return ticks.size();
	}*/
	public String[][] threadInfo()
	{
		ArrayList<String[]> V=new ArrayList();
		for(Iterator<CMLibrary> e=CMLib.libraries();e.hasNext();)
		{
			CMLibrary lib=e.next();
			SupportThread thread=lib.getSupportThread();
			if(thread!=null) {
				String[] S=new String[3];
				S[0]=thread.getName();
				S[1]=CMLib.english().returnTime(thread.milliTotal, thread.tickTotal);
				S[2]=thread.status;
				V.add(S);
			}
		}
		return V.toArray(new String[V.size()][]);
	}
	
/*	//This is a stupid method, it needs to be recoded/removed
	public String systemReport(String itemCode)
	{
		long totalMOBMillis=0;
		//long totalMOBTicks=0;
		long topMOBMillis=0;
		//long topMOBTicks=0;
		MOB topMOBClient=null;
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			totalMOBMillis+=S.getTotalMillis();
			//totalMOBTicks+=S.getTotalTicks();
			if(S.getTotalMillis()>topMOBMillis)
			{
				topMOBMillis=S.getTotalMillis();
				//topMOBTicks=S.getTotalTicks();
				topMOBClient=S.mob();
			}
		}

		if(itemCode.equalsIgnoreCase("totalMOBMillis"))
			return ""+totalMOBMillis;
		else if(itemCode.equalsIgnoreCase("totalMOBMillisTime"))
			return CMLib.english().returnTime(totalMOBMillis,0);
/*		else if(itemCode.equalsIgnoreCase("totalMOBMillisTimePlusAverage"))
			return CMLib.english().returnTime(totalMOBMillis,totalMOBTicks);
		else if(itemCode.equalsIgnoreCase("totalMOBTicks"))
			return ""+totalMOBTicks; //*
		else if(itemCode.equalsIgnoreCase("topMOBMillis"))
			return ""+topMOBMillis;
		else if(itemCode.equalsIgnoreCase("topMOBMillisTime"))
			return CMLib.english().returnTime(topMOBMillis,0);
/*		else if(itemCode.equalsIgnoreCase("topMOBMillisTimePlusAverage"))
			return CMLib.english().returnTime(topMOBMillis,topMOBTicks);
		else if(itemCode.equalsIgnoreCase("topMOBTicks"))
			return ""+topMOBTicks; //*
		else if(itemCode.equalsIgnoreCase("topMOBClient"))
		{
			if(topMOBClient!=null)
				return topMOBClient.name();
			return "";
		}

/*		int totalTickers=0;
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
		} //*
		if(itemCode.equalsIgnoreCase("freeMemory"))
			return ""+(Runtime.getRuntime().freeMemory()/1000);
		else if(itemCode.equalsIgnoreCase("totalMemory"))
			return ""+(Runtime.getRuntime().totalMemory()/1000);
		else if(itemCode.equalsIgnoreCase("totalTime"))
			return ""+CMLib.english().returnTime(System.currentTimeMillis()-CMSecurity.getStartTime(),0);
		else if(itemCode.equalsIgnoreCase("startTime"))
			return CMLib.time().date2String(CMSecurity.getStartTime());
		else if(itemCode.equalsIgnoreCase("currentTime"))
			return CMLib.time().date2String(System.currentTimeMillis());
/*		else if(itemCode.equalsIgnoreCase("totalTickers"))
			return ""+totalTickers;
		else if(itemCode.equalsIgnoreCase("totalMillis"))
			return ""+totalMillis;
		else if(itemCode.equalsIgnoreCase("totalMillisTime"))
			return CMLib.english().returnTime(totalMillis,0);
		else if(itemCode.equalsIgnoreCase("totalMillisTimePlusAverage"))
			return CMLib.english().returnTime(totalMillis,totalTicks);
		else if(itemCode.equalsIgnoreCase("totalTicks"))
			return ""+totalTicks; //*
		else if(itemCode.equalsIgnoreCase("tickgroupsize")) 
			return ""+ticks.size();
/*		else if(itemCode.equalsIgnoreCase("topGroupNumber"))
			return ""+topGroupNumber;
		else if(itemCode.equalsIgnoreCase("topGroupMillis"))
			return ""+topGroupMillis;
		else if(itemCode.equalsIgnoreCase("topGroupMillisTime"))
			return CMLib.english().returnTime(topGroupMillis,0);
		else if(itemCode.equalsIgnoreCase("topGroupMillisTimePlusAverage"))
			return CMLib.english().returnTime(topGroupMillis,topGroupTicks);
		else if(itemCode.equalsIgnoreCase("topGroupTicks"))
			return ""+topGroupTicks; //*
		else if(itemCode.toLowerCase().startsWith("thread"))
		{
			int xstart=6;//"thread".length();
			int xend=xstart;
			while((xend<itemCode.length())&&(Character.isDigit(itemCode.charAt(xend))))
				xend++;
			int threadNum=CMath.s_int(itemCode.substring(xstart,xend));
			int curThreadNum=0;
			for(Enumeration e=CMLib.libraries();e.hasMoreElements();)
			{
				CMLibrary lib=(CMLibrary)e.nextElement();
				SupportThread thread=lib.getSupportThread();
				if(thread!=null) {
					if(curThreadNum==threadNum) {
						String instrCode=itemCode.substring(xend);
						if(instrCode.equalsIgnoreCase("miliTotal")||instrCode.equalsIgnoreCase("milliTotal"))
							return ""+thread.milliTotal;
						if(instrCode.equalsIgnoreCase("status"))
							return ""+thread.status;
						if(instrCode.equalsIgnoreCase("name"))
							return ""+thread.getName();
						if(instrCode.equalsIgnoreCase("MilliTotalTime")||instrCode.equalsIgnoreCase("MiliTotalTime"))
							return CMLib.english().returnTime(thread.milliTotal,0);
						if(instrCode.equalsIgnoreCase("MilliTotalTimePlusAverage")||instrCode.equalsIgnoreCase("MiliTotalTimePlusAverage"))
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
	} */

	/*public String tickReport(String request)
	{
		StringBuffer msg=new StringBuffer("\r\n");
		boolean activeOnly=false;
		String mask=null;
		if("ACTIVE".startsWith(request.toUpperCase())&&(request.length()>0))
		{
			activeOnly=true;
			request="";
		}
		msg.append("Grp Client ID             Due Status\r\n");
//		msg.append("Grp Client               ID Status  \r\n");
//		msg.append(CMStrings.padRight("Grp",4)+CMStrings.padRight("Client",20)+" "+CMStrings.padRight("ID",3)+CMStrings.padRight("Status",8)+"\r\n");
		int col=0;
		int whichTick=-1;
		if(CMath.isInteger(request)&&(request.length()>0))
			whichTick=CMath.s_int(request);
		else if(request.length()>0)
		{
			mask=request.toUpperCase().trim();
			if(mask.length()==0) mask=null;
		}
		int tickGroupCount=0;
		for(Iterator<Tick> e=ticks.iterator();e.hasNext();tickGroupCount++)
		{
			Tick group=e.next();
			if((whichTick<0)||(whichTick==tickGroupCount))
			for(TickActer E : group.tickers())
			{
				//long due=E.nextAct()-System.currentTimeMillis();
				if((!activeOnly)||(E.getActStatus()!=Tickable.TickStat.Not))
				{
					String name=(E instanceof CMSavable)?(E.ID()+((CMSavable)E).saveNum()):(E.ID());
					if((mask==null)||(name.toUpperCase().indexOf(mask)>=0))
					{
						if(((col++)>=2)||(activeOnly))
						{
							msg.append("\r\n");
							col=1;
						}
						msg.append(CMStrings.padRight(""+tickGroupCount,4)+
								   CMStrings.padRight(name,20)+
								   CMStrings.padRight("",6)+
								   CMStrings.padRight(""+E.getTickStatus(),6));
					}
				}
			}
			if(whichTick==tickGroupCount) break;
		}
		return msg.toString();
	}*/
/*
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
		else if(which.toLowerCase().startsWith("tickerssize"))
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
		TockClient C=null;
		almostTock.fetchTickerByIndex(client);
		if(C==null) return "";

		if(which.toLowerCase().startsWith("tickername"))
		{
			Tickable E=C.clientObject;
			if(E instanceof Room)
				return E.ID()+((Room)E).saveNum();
			if(E!=null) return E.ID();
			return "!NULL!";
		}
		else
		if(which.toLowerCase().startsWith("tickerstatus"))
			return ((C.clientObject==null)?"":(""+C.clientObject.getTickStatus()));
		return "";
	}
*/
	public boolean shutdown() {
		//int numTicks=tickGroup.size();
		/*while(ticks.size()>0)
		{
			//Log.sysOut("ServiceEngine","Shutting down all tick "+which+"/"+numTicks+"...");
			Tick tock=ticks.first();
			if(tock!=null)
			{
				CMProps.Strings.MUDSTATUS.setProperty("Shutting down...shutting down Service Engine: killing Tick#" + tock.tickObjectCounter+": "+tock.getStatus());
				tock.shutdown();
			}
			try{Thread.sleep(100);}catch(Exception e){}
		}*/
		while(areas.size()>0)
		{
			TickArea tock=areas.first();
			if(tock!=null)
			{
				CMProps.Strings.MUDSTATUS.setProperty("Shutting down...shutting down Service Engine: killing Area#" + tock.tickObjectCounter+": "+tock.clientObject.getTickStatus());
				tock.shutdown();
			}
			try{Thread.sleep(100);}catch(Exception e){}
		}
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...shutting down Service Engine: "+ID()+": thread shutdown");
		thread.shutdown();
		Log.sysOut("ServiceEngine","Shutdown complete.");
		return true;
	}
/*
	public Vector getNamedTickingObjects(String name)
	{
		Vector V=new Vector();
		Tick almostTock=null;
		TockClient C=null;
		name=name.toUpperCase().trim();
		for(Iterator<Tick> e=ticks.iterator();e.hasNext();)
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
*/
	public String getServiceThreadSummary(Thread T)
	{
		if(T instanceof SupportThread)
			return " ("+((SupportThread)T).status+")";
		else
		if(T instanceof MudHost)
			return " ("+((MudHost)T).getStatus()+")";
		else
		if(T instanceof ExternalHTTPRequests)
			return " ("+((ExternalHTTPRequests)T).getHTTPstatus()+" - "+((ExternalHTTPRequests)T).getHTTPstatusInfo()+")";
		return "";

	}
/*
	public void insertOrderDeathInOrder(DVector DV, long lastStart, String msg, Tick tock)
	{
		if(DV.size()>0)
		for(int i=0;i<DV.size();i++)
		{
			if(((Long)DV.elementAt(i,0)).longValue()>lastStart)
			{
				DV.insertRowAt(i,Long.valueOf(lastStart),msg,tock);
				return;
			}
		}
		DV.addRow(Long.valueOf(lastStart),msg,tock);
	}
*/
	public void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis()-(5*CoffeeTime.MILI_MINUTE);
		//long longerDateTime=System.currentTimeMillis()-(120*CoffeeTime.MILI_MINUTE);
		//thread.status("checking");

		ArrayList<TickArea> orderedDeaths=new ArrayList();
		thread.status("checking tick groups.");
		/*for(Iterator<Tick> e=ticks.iterator();e.hasNext();)
		{
			Tick almostTock=e.next();
			if((almostTock.awake)
			&&(almostTock.lastStop<lastDateTime))
			{
				//insertOrderDeathInOrder(orderedDeaths,0,"LOCKED GROUP "+almostTock.tickObjectCounter+"! No further information.",almostTock);
				delTickGroup(almostTock);
				orderedDeaths.add(almostTock);	//"LOCKED GROUP "+almostTock.tickObjectCounter+"! No further information."
				// no isDEBUGGING check -- just always let her rip.
				Log.errOut(thread.getName(),"LOCKED TICK GROUP "+almostTock.tickObjectCounter);
				thread.debugDumpStack(almostTock);
			}
		}
		if(orderedDeaths.size()>0)
		{
			thread.status("killing tick groups.");
			for(int x=0;x<orderedDeaths.size();x++)
			{
				Tick almostTock=orderedDeaths.get(x);
				ArrayList<TickActer> objs=new ArrayList();
				try{
					for(TickActer E : almostTock.tickers())
						objs.add(E);
				}catch(NoSuchElementException e){}
				almostTock.shutdown();
				//if(CMLib.threads() instanceof ServiceEngine)
				//	((ServiceEngine)CMLib.threads()).
				//delTickGroup(almostTock);
				for(int i=0;i<objs.size();i++)
					startTickDown(objs.get(i));
			}
			orderedDeaths.clear();
		}*/
		thread.status("checking areas.");
		for(Iterator<TickArea> e=areas.iterator();e.hasNext();)
		{
			TickArea almostTock=e.next();
			if((almostTock.awake)
			&&(almostTock.lastStop<lastDateTime))
			{
				delArea(almostTock);
				//orderedDeaths.add(almostTock);	//"LOCKED GROUP "+almostTock.tickObjectCounter+"! No further information."
				almostTock.shutdown();
				Log.errOut(thread.getName(),"LOCKED TICK GROUP "+almostTock.tickObjectCounter);
				thread.debugDumpStack(almostTock);
				startArea(almostTock.clientObject, almostTock.TICK_TIME);
			}
		}
/*
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
*/
	}

	public void run()
	{
		while(isSuspended)
			try{Thread.sleep(2000);}catch(Exception e){}

		if((!CMSecurity.isDisabled("UTILITHREAD"))
		&&(!CMSecurity.isDisabled("THREADTHREAD")))
		{
			checkHealth();
			//Resources.removeResource("SYSTEM_HASHED_MASKS");
			//NOTE: Could use a ReferenceQueue to clean up gced references in the SYSTEM_HASHED_MASKS hashtable still.
		}
	}

	public boolean activate() {
		if(thread==null)
			thread=new SupportThread("THThreads",
					MudHost.TIME_UTILTHREAD_SLEEP, this, CMSecurity.isDebugging("UTILITHREAD"));
		if(!thread.started)
			thread.start();
		mainTime.start();
		/* if(exitThread==null)
		{
			exitThread=
			exitThread.start();
		} */
		return true;
	}
}