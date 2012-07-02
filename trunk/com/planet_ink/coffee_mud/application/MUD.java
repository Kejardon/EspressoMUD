package com.planet_ink.coffee_mud.application;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.database.*;
import com.planet_ink.coffee_mud.core.http.*;
import com.planet_ink.coffee_mud.core.threads.*;
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

import java.io.PrintWriter; // for writing to sockets
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.sql.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class MUD extends Thread implements MudHost
{
	private static final float HOST_VERSION_MAJOR=(float)1.0;
	private static final long  HOST_VERSION_MINOR=0;
	private static final long LastConnectionDelay=(5*60*1000);

	protected static boolean bringDown=false;
	private static DVector accessed=new DVector(2);	//List of connection attempts
	private static Vector<String> autoblocked=new Vector();	//List of connections to ignore
	public static Vector<MUD> hosts=new Vector();	//List of ports, really.

	private static volatile boolean serverIsRunning = false;

	protected static boolean isOK = false;
	protected boolean acceptConnections=false;
	protected int port=5555;
	protected final long startupTime = System.currentTimeMillis();

	private final static String[] STATE_STRING={"waiting","accepting","allowing"};
	private int state=0;
	ServerSocket servsock=null;

	public MUD(String name)
	{
		super(name);
	}

	public static void fatalStartupError(Thread t, int type)
	{
		String str=null;
		switch(type)
		{
		case 1:
			str="ERROR: initHost() will not run without properties. Exiting.";
			break;
		case 2:
			str="Map is empty?! Exiting.";
			break;
		case 3:
			str="Database init failed. Exiting.";
			break;
		case 4:
			str="Fatal exception. Exiting.";
			break;
		case 5:
			str="MUD Server did not start. Exiting.";
			break;
		default:
			str="Fatal error loading classes.  Make sure you start up coffeemud from the directory containing the class files.";
			break;
		}
		Log.errOut(Thread.currentThread().getName(),str);
		bringDown=true;
		CMProps.Bools.MUDSHUTTINGDOWN.setProperty(true);
		CMLib.killThread(t,100,1);
	}

	protected static boolean initHost(Thread t)
	{
		if (!isOK)
		{
			CMLib.killThread(t,100,1);
			return false;
		}

		CMProps page=CMProps.instance();

		if ((page == null) || (!page.loaded))
		{
			fatalStartupError(t,1);
			return false;
		}

		long startWait=System.currentTimeMillis();
		while (!serverIsRunning && isOK && ((System.currentTimeMillis() - startWait)< 90000))
		{ try{ Thread.sleep(500); }catch(Exception e){ isOK=false;} }

		if((!isOK)||(!serverIsRunning))
		{
			fatalStartupError(t,5);
			return false;
		}

		if(CMLib.database()==null)
		{
			Log.errOut(Thread.currentThread().getName(),"No registered database!");
			System.exit(-1);
		}

		CMProps.Strings.MUDSTATUS.setProperty("Booting: loading base classes");
		if(!CMClass.loadClasses())
		{
			fatalStartupError(t,0);
			return false;
		}
		CMSecurity.setSysOp(page.getStr("SYSOPMASK")); // requires all classes be loaded
		CMSecurity.parseGroups(page);
		int numChannelsLoaded=0;
		numChannelsLoaded=CMLib.channels().loadChannels(page.getStr("CHANNELS"),
														page.getStr("ICHANNELS"));
		Log.sysOut(Thread.currentThread().getName(),"Channels loaded   : "+(numChannelsLoaded));

		Log.sysOut(Thread.currentThread().getName(),"Loading map...");
		CMProps.Strings.MUDSTATUS.setProperty("Booting: loading rooms....");
		try
		{
			CMLib.activateLibraries();
			Log.sysOut(Thread.currentThread().getName(),"Utility threads started");
		}
		catch (Throwable th)
		{
			Log.errOut(Thread.currentThread().getName(),"CoffeeMud Server initHost() failed");
			Log.errOut(Thread.currentThread().getName(),th);
			fatalStartupError(t,4);
			return false;
		}
		while(!CMLib.database().doneLoading())
		try{Thread.sleep(500);}catch(Exception e){}

		Log.sysOut(Thread.currentThread().getName(),"Mapped rooms      : "+CMLib.map().numRooms()+" in "+CMLib.map().numAreas()+" areas");

		if(CMLib.map().numAreas()==0)
		{
			Log.sysOut("NO MAPPED ROOM?!  I'll make ya one!");
			Area newArea=CMClass.AREA.getNew("StdArea");
			newArea.setName("New Area");
			CMLib.map().addArea(newArea);
			Room room=CMClass.LOCALE.getNew("StdRoom");
			room.setArea(newArea);
			room.setDisplayText("New Room");
			room.setDescription("Brand new database room! You need to change this text with the MODIFY ROOM command.");
		}

		CMLib.login().initStartRooms(page);
		CMLib.login().initDeathRooms(page);
		CMLib.login().initBodyRooms(page);

		CMLib.players().unqueuePlayers();

		CMClass.COMMAND.compileCommands();
		CMProps.Strings.MUDSTATUS.setProperty("Booting: readying for connections.");

		for(int i=0;i<hosts.size();i++)
			hosts.get(i).setAcceptConnections(true);
		Log.sysOut(Thread.currentThread().getName(),"Initialization complete.");
		CMProps.Bools.MUDSTARTED.setProperty(true);
		CMProps.Strings.MUDSTATUS.setProperty("OK");
		return true;
	}

	public void acceptConnection(Socket sock)
		throws SocketException, IOException
	{
		sock.setSoLinger(true,3);
		state=1;

		if (acceptConnections)
		{
			String address="unknown";
			try{address=sock.getInetAddress().getHostAddress().trim();}catch(Exception e){}
			Log.sysOut(Thread.currentThread().getName(),"Connection from "+address);
			int proceed=0;
			if(CMSecurity.isBanned(address))
				proceed=1;
			else
			{
				int numAtThisAddress=0;
				long ConnectionWindow=(180*1000);
				boolean anyAtThisAddress=false;
				int maxAtThisAddress=6;
				if(!CMSecurity.isDisabled("CONNSPAMBLOCK"))
				{
					try{
						for(int a=accessed.size()-1;a>=0;a--)
						{
							if((((Long)accessed.elementAt(a,1)).longValue()+LastConnectionDelay)<System.currentTimeMillis())
								accessed.removeRow(a);
							else
							if(((String)accessed.elementAt(a,0)).equalsIgnoreCase(address))
							{
								anyAtThisAddress=true;
								if((((Long)accessed.elementAt(a,1)).longValue()+ConnectionWindow)>System.currentTimeMillis())
									numAtThisAddress++;
							}
						}
						String upper=address.toUpperCase();
						if(autoblocked.contains(upper))
						{
							if(!anyAtThisAddress)
								autoblocked.remove(upper);
							else
								proceed=2;
						}
						else if(numAtThisAddress>=maxAtThisAddress)
						{
							autoblocked.add(upper);
							proceed=2;
						}
					}catch(java.lang.ArrayIndexOutOfBoundsException e){}

					accessed.addRow(address,Long.valueOf(System.currentTimeMillis()));
				}
			}

			if(proceed!=0)
			{
				Log.sysOut(Thread.currentThread().getName(),"Blocking a connection from "+address);
				PrintWriter out = new PrintWriter(sock.getOutputStream());
				out.println("\r\nOFFLINE: Blocked\r\n");
				out.flush();
				if(proceed==2)
					out.println("\r\nYour address has been blocked temporarily due to excessive connection attempts.  Please try back in " + (LastConnectionDelay/60000) + " minutes, and not before.\r\n\r\n");
				else
					out.println("\r\nYou are currently banned from this server.\r\n\r\n");
				out.flush();
				try{Thread.sleep(250);}catch(Exception e){}
				out.close();
				sock = null;
			}
			else
			{
				state=2;
				// also the intro page
				String[] introOptions=(String[])Resources.getResource("IntroOptions");
				String introFilename="text/intro.txt";
				if(introOptions==null)
				{
					introOptions=CMClass.dummyStringArray;
					CMFile introDir=new CMFile("resources/text",null,false,true);
					if(introDir.isDirectory())
					{
						CMFile[] files=introDir.listFiles();
						ArrayList<String> V=new ArrayList();
						for(int f=0;f<files.length;f++)
							if(files[f].getName().toLowerCase().startsWith("intro")
							&&files[f].getName().toLowerCase().endsWith(".txt"))
								V.add("text/"+files[f].getName());
						introOptions=V.toArray(introOptions);
					}
					Resources.submitResource("IntroOptions", introOptions);
				}
				if(introOptions.length>0) introFilename=introOptions[CMath.random(introOptions.length)];
				StringBuffer introText=Resources.getFileResource(introFilename,true);
				Session S=(Session)CMClass.COMMON.getNew("DefaultSession");
				S.initializeSession(sock, introText != null ? introText.toString() : null);
				S.start();
				CMLib.sessions().addElement(S);
				sock = null;
			}
		}
		else
		{
			StringBuffer rejectText;

			try { rejectText = Resources.getFileResource("text/offline.txt",true);
			} catch(java.lang.NullPointerException npe) { rejectText=new StringBuffer("");}

			PrintWriter out = new PrintWriter(sock.getOutputStream());
			out.println("\r\nOFFLINE: " + CMProps.Strings.MUDSTATUS.property()+"\r\n");
			out.println(rejectText);
			out.flush();

			try{Thread.sleep(1000);}catch(Exception e){}
			out.close();
			sock = null;
		}
	}

	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		serverIsRunning = false;

		if (!isOK)	return;

		InetAddress bindAddr = null;

		if (CMProps.Ints.BACKLOG.property() > 0)
			q_len = CMProps.Ints.BACKLOG.property();

		if (CMProps.Strings.MUDBINDADDRESS.property().length() > 0)
		try {
			bindAddr = InetAddress.getByName(CMProps.Strings.MUDBINDADDRESS.property());
		} catch (UnknownHostException e) {
			Log.errOut(Thread.currentThread().getName(),"ERROR: MUD Server could not bind to address " + CMProps.Strings.MUDBINDADDRESS.property());
		}

		try
		{
			servsock=new ServerSocket(port, q_len, bindAddr);

			Log.sysOut(Thread.currentThread().getName(),"MUD Server started on port: "+port);
			if (bindAddr != null)
				Log.sysOut(Thread.currentThread().getName(),"MUD Server bound to: "+bindAddr.toString());
			serverIsRunning = true;

			while(true)
			{
				state=0;
				if(servsock==null) break;
				sock=servsock.accept();
				acceptConnection(sock);
			}
		}
		catch(Throwable t)
		{
			if((!(t instanceof java.net.SocketException))
			||(t.getMessage()==null)
			||(t.getMessage().toLowerCase().indexOf("socket closed")<0))
				Log.errOut(Thread.currentThread().getName(),t);

			if (!serverIsRunning)
				isOK = false;
		}

		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud Server cleaning up.");

		try {
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		} catch(IOException e) { }

		Log.sysOut(Thread.currentThread().getName(),"MUD on port "+port+" stopped!");
	}
	public String getStatus()
	{
		if(CMProps.Bools.MUDSHUTTINGDOWN.property())
			return CMProps.Strings.MUDSTATUS.property();
		if(!CMProps.Bools.MUDSTARTED.property())
			return CMProps.Strings.MUDSTATUS.property();
		return STATE_STRING[state];
	}

	public void shutdown(Session S, boolean keepItDown)
	{
		globalShutdown(S,keepItDown);
		interrupt(); // kill the damn archon thread.
	}

	public static void defaultShutdown()
	{
		globalShutdown(null,true);
	}
	public static void globalShutdown(Session S, boolean keepItDown)
	{
		CMProps.Bools.MUDSTARTED.setProperty(false);
		CMProps.Bools.MUDSHUTTINGDOWN.setProperty(true);
		//TODO: This should work on getting the MUD to a fixed state, and when accomplished report it.
		CMLib.threads().setSuspended(true);
		if(S!=null)S.print("Closing MUD listeners to new connections...");
		for(int i=0;i<hosts.size();i++)
			(hosts.get(i)).setAcceptConnections(false);
		Log.sysOut(Thread.currentThread().getName(),"New Connections are now closed");
		if(S!=null)S.println("Done.");

		if(S!=null)S.print("Saving players...");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Saving players...");
		if(CMLib.sessions()!=null)
			for(Session S2 : CMLib.sessions().toArray())
			{
				MOB M = S2.mob();
				if((M!=null)&&(M.playerStats()!=null))
					M.playerStats().setLastDateTime(System.currentTimeMillis());
			}
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"All users saved.");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut(Thread.currentThread().getName(),"Notifying all objects of shutdown...");
		if(S!=null)S.print("Notifying all objects of shutdown...");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Notifying Objects");
		MOB mob=null;
		if(S!=null) mob=S.mob();
		//CMMsg msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.SHUTDOWN),null);
		//for(Iterator<Room> r=CMLib.map().rooms();r.hasNext();)
		//	r.next().send(msg);
		//msg.returnMsg();
		if(S!=null)S.println("done");
		if(S!=null)S.println("Save thread stopped");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Session Thread");
		CMLib.sessions().shutdown();

		if(S!=null)S.print("Stopping player Sessions...");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Stopping sessions");
		SessionsList list=CMLib.sessions();
		while(list.size()>0)
		{
			Session S2=list.elementAt(0);
			if((S!=null)&&(S2==S))
				list.removeElementAt(0);
			else
			{
				CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Stopping session "+S2.getAddress());
				S2.kill(true);
				CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Done stopping session "+S2.getAddress());
			}
			if(S!=null)S.print(".");
		}
		if(S!=null)S.println("All users logged off");
		//Why does this line exist? There should be nothing to inform...
//		try{Thread.sleep(3000);}catch(Exception e){/* give sessions a few seconds to inform the map */}
		Log.sysOut(Thread.currentThread().getName(),"All users logged off.");

		if(S!=null)S.print("Stopping all threads...");
		CMLib.threads().shutdown();
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"Map Threads Stopped.");

		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...closing db connections");
		CMLib.misc().shutdown();
		CMLib.database().shutdown();
		while(!CMLib.database().doneLoading())
		try{Thread.sleep(500);}catch(Exception e){}

		//TODO: Most thing past this probably aren't really needed. Re-evaluate later
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Clearing channels, help");

		CMLib.channels().shutdown();
		CMLib.help().shutdown();

		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...unloading map");
		CMLib.map().shutdown();
		CMLib.players().shutdown();
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...unloading resources");
		Resources.clearResources();
		Log.sysOut(Thread.currentThread().getName(),"Resources Cleared.");
		if(S!=null)S.println("All resources unloaded");

/*
		for(int i=0;i<webServers.size();i++)
		{
			HTTPserver webServerThread=(HTTPserver)webServers.elementAt(i);
			CMProps.Strings.MUDSTATUS.setProperty("Shutting down web server "+webServerThread.getName()+"...");
			webServerThread.shutdown(S);
			Log.sysOut(Thread.currentThread().getName(),"Web server "+webServerThread.getName()+" stopped.");
			if(S!=null)S.println("Web server "+webServerThread.getName()+" stopped");
		}
		webServers.clear();
*/

		CMProps.Strings.MUDSTATUS.setProperty("Shutting down" + (keepItDown? "..." : " and restarting..."));

		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud shutdown complete.");
		if(S!=null)S.println("CoffeeMud shutdown complete.");
		bringDown=keepItDown;
		CMLib.threads().setSuspended(false);
		if(!keepItDown)
			if(S!=null)S.println("Restarting...");
		if(S!=null)S.kill(false);
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		CMProps.Strings.MUDSTATUS.setProperty("Shutdown: you are the special lucky chosen one!");
		for(int m=hosts.size()-1;m>=0;m--)
			try{
				CMLib.killThread(hosts.get(m),100,1);
			} catch(Throwable t){}
		if(!keepItDown)
			CMProps.Bools.MUDSHUTTINGDOWN.setProperty(false);
	}


	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				servsock = null;
			} catch(IOException e) { }
		}
		super.interrupt();
	}

	public static int activeThreadCount(ThreadGroup tGroup)
	{
		int realAC=0;
		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive())
				realAC++;
		}
		return realAC;
	}

	public static int killCount(ThreadGroup tGroup, Thread thisOne)
	{
		int killed=0;

		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive() && (tArray[i] != thisOne))
			{
				CMLib.killThread(tArray[i],500,1);
				killed++;
			}
		}
		return killed;
	}

	public static void threadList(ThreadGroup tGroup)
	{
		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive())
			{
				if(tArray[i] instanceof Session)
				{
					Session S=(Session)tArray[i];
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: Session status "+S.getStatus()+"-"+S.previousCMD() + "\r\n"+CMClass.getStackTrace((Thread)S));
				}
				else
				if(tArray[i] instanceof Tickable)
				{
					Tickable T=(Tickable)tArray[i];
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+T.ID()+"-"+T.getTickStatus() + "\r\n"+CMClass.getStackTrace((Thread)T));
				}
/*				else
				if((tArray[i] instanceof Tick)
				&&(((Tick)tArray[i]).lastClient!=null)
				&&(((Tick)tArray[i]).lastClient.clientObject!=null))
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+tArray[i].getName()+" "+((Tick)tArray[i]).lastClient.clientObject.ID()+"-"+((Tick)tArray[i]).lastClient.clientObject.name()+"-"+((Tick)tArray[i]).lastClient.clientObject.getTickStatus() + "\r\n");
*/				else
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+tArray[i].getName() + "\r\n"+CMClass.getStackTrace(tArray[i]));
			}
		}
	}

	public int getPort()
	{
		return port;
	}

	private static class HostGroup extends Thread
	{
		private static int grpid=0;
		private String name=null;
		private String iniFile=null;
		private String logName=null;
		public HostGroup(ThreadGroup G, String mudName)
		{
			super(G,"HOST"+grpid);
			logName="mud"+((grpid>0)?("."+grpid):"");
			grpid++;
			name=mudName;
			setDaemon(true);
		}

		public void run()
		{
					try
					{
			new CMLib(); // initialize the lib
			new CMClass(); // initialize the classes

			// wait for ini to be loaded, and for other matters
			CMProps page=CMProps.loadPropPage("espressomud.ini");
			if ((page==null)||(!page.loaded))
			{
				Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read espressomud.ini file.");
				System.out.println("MUD/ERROR: Unable to read espressomud.ini file.");
				CMProps.Strings.MUDSTATUS.setProperty("A terminal error has occured!");
				return;
			}
			page.resetSystemVars();
			CMProps.Bools.MUDSTARTED.setProperty(false);

			if(page.getStr("DISABLE").trim().length()>0)
				Log.sysOut(Thread.currentThread().getName(),"Disabled subsystems: "+page.getStr("DISABLE"));
			if(page.getStr("DEBUG").trim().length()>0)
			{
				Log.sysOut(Thread.currentThread().getName(),"Debugging messages: "+page.getStr("DEBUG"));
				if(!Log.debugChannelOn())
					Log.errOut(Thread.currentThread().getName(),"Debug logging is disabled! Check your DBGMSGS flag!");
			}

			CMLib.registerLibrary(new DBManager());
			CMLib.registerLibrary(new ProcessHTTPrequest(null,null,null,true));
			CMProps.Strings.MUDVER.setProperty(HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);

			CMLib.registerLibrary(new ServiceEngine());
			CMProps.Strings.MUDNAME.setProperty(name.replace('\'','`'));
			try
			{
				isOK = true;
				CMProps.Strings.MUDSTATUS.setProperty("Booting");
				CMProps.Strings.MUDBINDADDRESS.setProperty(page.getStr("BIND"));
				CMProps.Ints.BACKLOG.setProperty(page.getInt("BACKLOG"));

				if(MUD.isOK)
				{
					String ports=page.getProperty("PORT");
					int pdex=ports.indexOf(",");
					while(pdex>0)
					{
						MUD mud=new MUD("MUD@"+ports.substring(0,pdex));
						mud.acceptConnections=false;
						mud.port=CMath.s_int(ports.substring(0,pdex));
						ports=ports.substring(pdex+1);
						mud.start();
						hosts.add(mud);
						pdex=ports.indexOf(",");
					}
					MUD mud=new MUD("MUD@"+ports);
					mud.acceptConnections=false;
					mud.port=CMath.s_int(ports);
					mud.start();
					hosts.add(mud);
				}

				StringBuffer str=new StringBuffer("");
				for(int m=0;m<hosts.size();m++)
				{
					MudHost mud=hosts.get(m);
					str.append(" "+mud.getPort());
				}
				CMProps.Strings.MUDPORTS.setProperty(str.toString());

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						if(!CMProps.Bools.MUDSHUTTINGDOWN.property())
							MUD.globalShutdown(null,true);
					}
				});

				if(initHost(Thread.currentThread()))
				{
					if(hosts.size()>0)
						hosts.get(0).join();
					else
						System.exit(-1);
				}
			}
			catch(InterruptedException e)
			{
				Log.errOut(Thread.currentThread().getName(),e);
			}
					} catch(Throwable e) { Log.errOut(Thread.currentThread().getName(),e); return;}
		}
	}
	public static void main(String a[])
	{
		String nameID="";
		if(a.length>0)
		{
			for(int i=0;i<a.length;i++)
				nameID+=" "+a[i];
			//nameID=nameID.trim();
			nameID=CMParms.combine(CMParms.paramParse(nameID),0);
		}
		new CMLib(); // initialize this threads libs

		if(nameID.length()==0) nameID="Unnamed EspressoMUD";
		CMProps page=CMProps.loadPropPage("espressomud.ini");
		if ((page==null)||(!page.loaded))
		{
			Log.instance().startLogFiles(1);
			Log.instance().setLogOutput("BOTH","BOTH","BOTH","BOTH","BOTH","BOTH","BOTH","BOTH");
			Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read espressomud.ini file.");
			System.out.println("MUD/ERROR: Unable to read espressomud.ini file.");
			CMProps.Strings.MUDSTATUS.setProperty("A terminal error has occured!");
			System.exit(-1);
			return;
		}
		Log.instance().startLogFiles(page.getInt("NUMLOGS"));
		Log.instance().setLogOutput(
		  page.getStr("SYSMSGS"),
		  page.getStr("INFMSGS"),
		  page.getStr("ERRMSGS"),
		  page.getStr("WRNMSGS"),
		  page.getStr("DBGMSGS"),
		  page.getStr("HLPMSGS"),
		  page.getStr("KILMSGS"),
		  page.getStr("CBTMSGS"));
		if(!bringDown)	//Disabling ability to restart without full shutdown
		{
			System.out.println();
			Log.sysOut(Thread.currentThread().getName(),"CoffeeMud v K1");
			Log.sysOut(Thread.currentThread().getName(),"(C) 2000-2010 Bo Zimmerman");
			Log.sysOut(Thread.currentThread().getName(),"http://www.coffeemud.org");
			Log.sysOut(Thread.currentThread().getName(),"(C) 2010-2012 Kejardon");
			Log.sysOut(Thread.currentThread().getName(),"Starting MUD");
			ThreadGroup G=new ThreadGroup("MUD");
			HostGroup H=new HostGroup(G,nameID);
			H.start();
			try{H.join();}catch(Exception e){e.printStackTrace(); Log.errOut(Thread.currentThread().getName(),e); }
			CMClass.threadPool.shutdown();
			System.gc();
			try{Thread.sleep(1000);}catch(Exception e){}
			System.runFinalization();
			try{Thread.sleep(1000);}catch(Exception e){}

			if(activeThreadCount(Thread.currentThread().getThreadGroup())>1)
			{
				CMClass.threadPool.shutdownNow();
				try{ Thread.sleep(1000);}catch(Exception e){}
				killCount(Thread.currentThread().getThreadGroup(),Thread.currentThread());
				try{ Thread.sleep(1000);}catch(Exception e){}
				if(activeThreadCount(Thread.currentThread().getThreadGroup())>1)
				{
					Log.sysOut(Thread.currentThread().getName(),"WARNING: " + activeThreadCount(Thread.currentThread().getThreadGroup()) +" other thread(s) are still active!");
					threadList(Thread.currentThread().getThreadGroup());
				}
			}
		}
	}

	public void setAcceptConnections(boolean truefalse){ acceptConnections=truefalse;}
	public boolean isAcceptingConnections(){ return acceptConnections;}
	public long getUptimeSecs() { return (System.currentTimeMillis()-startupTime)/1000;}
	public long getUptimeStart() { return startupTime;}
}
