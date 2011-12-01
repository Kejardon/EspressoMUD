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
public class MUD extends Thread implements MudHost
{
	private static final float HOST_VERSION_MAJOR=(float)5.6;
	private static final long  HOST_VERSION_MINOR=2;

	protected static boolean bringDown=false;
	private static String execExternalCommand=null;
	private static Vector webServers=new Vector();
	private static DVector accessed=new DVector(2);
	private static Vector autoblocked=new Vector();
	private static Vector databases=new Vector();

	private static boolean serverIsRunning = false;

	protected static boolean isOK = false;
	protected boolean acceptConnections=false;
	protected String host="MyHost";
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

		DBConnector currentDBconnector=null;
		String dbClass=page.getStr("DBCLASS");
		if(dbClass.length()>0)
		{
			String dbService=page.getStr("DBSERVICE");
			String dbUser=page.getStr("DBUSER");
			String dbPass=page.getStr("DBPASS");
			int dbConns=page.getInt("DBCONNECTIONS");
			boolean dbReuse=page.getBoolean("DBREUSE");
			boolean useQue=!CMSecurity.isDisabled("DBERRORQUE");
			boolean useQueStart=!CMSecurity.isDisabled("DBERRORQUESTART");
			CMProps.Strings.MUDSTATUS.setProperty("Booting: connecting to database");
			currentDBconnector=new DBConnector(dbClass,dbService,dbUser,dbPass,dbConns,dbReuse,useQue,useQueStart);
			currentDBconnector.reconnect();
			CMLib.registerLibrary(new DBInterface(currentDBconnector));

			DBConnection DBTEST=currentDBconnector.DBFetch();
			if(DBTEST!=null) currentDBconnector.DBDone(DBTEST);
			if((currentDBconnector.amIOk())&&(CMLib.database().isConnected()))
			{
				Log.sysOut(Thread.currentThread().getName(),"Connected to "+currentDBconnector.service());
				databases.addElement(currentDBconnector);
			}
			else
			{
				String DBerrors=currentDBconnector.errorStatus().toString();
				Log.errOut(Thread.currentThread().getName(),"Fatal database error: "+DBerrors);
				System.exit(-1);
			}
		}
		else
		if(CMLib.database()==null)
		{
			Log.errOut(Thread.currentThread().getName(),"No registered database!");
			System.exit(-1);
		}

		// test the database
		try {
			CMFile F = new CMFile("/test.the.database",null,false);
			if(F.exists())
				Log.sysOut(Thread.currentThread().getName(),"Test file found .. hmm.. that was unexpected.");

		} catch(Throwable e) {
			Log.errOut(Thread.currentThread().getName(),e.getMessage());
			Log.errOut(Thread.currentThread().getName(),"Database error! Panic shutdown!");
			System.exit(-1);
		}

	/*
		String webServersList=page.getPrivateStr("RUNWEBSERVERS");
		if(webServersList.equalsIgnoreCase("true"))
			webServersList="pub,admin";
		if((webServersList.length()>0)&&(!webServersList.equalsIgnoreCase("false")))
		{
			Vector serverNames=CMParms.parseCommas(webServersList,true);
			for(int s=0;s<serverNames.size();s++)
			{
				String serverName=(String)serverNames.elementAt(s);
				HTTPserver webServerThread = new HTTPserver(CMLib.mud(0),serverName,0);
				webServerThread.start();
				webServers.addElement(webServerThread);
				int numToDo=webServerThread.totalPorts();
				while((--numToDo)>0)
				{
					webServerThread = new HTTPserver(CMLib.mud(0),"pub",numToDo);
					webServerThread.start();
					webServers.addElement(webServerThread);
				}
			}
			CMLib.registerLibrary(new ProcessHTTPrequest(null,(webServers.size()>0)?(HTTPserver)webServers.firstElement():null,null,true));
		}
	*/

		CMProps.Strings.MUDSTATUS.setProperty("Booting: loading base classes");
		if(!CMClass.loadClasses())
		{
			fatalStartupError(t,0);
			return false;
		}
//		CMLib.lang().setLocale(CMLib.props().getStr("LANGUAGE"),CMLib.props().getStr("COUNTRY"));
		CMLib.time().globalClock().initializeINIClock(page);
		CMSecurity.setSysOp(page.getStr("SYSOPMASK")); // requires all classes be loaded
		CMSecurity.parseGroups(page);
		int numChannelsLoaded=0;
		numChannelsLoaded=CMLib.channels().loadChannels(page.getStr("CHANNELS"),
														page.getStr("ICHANNELS"));
		Log.sysOut(Thread.currentThread().getName(),"Channels loaded   : "+(numChannelsLoaded));

		Log.sysOut(Thread.currentThread().getName(),"Loading map...");
		CMProps.Strings.MUDSTATUS.setProperty("Booting: loading rooms....");
		CMLib.database().DBReadAllRooms();
/*		for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			CMProps.Strings.MUDSTATUS.setProperty("Booting: filling map ("+A.name()+")");
			A.fillInAreaRooms();
		}
*/
		Log.sysOut(Thread.currentThread().getName(),"Mapped rooms      : "+CMLib.map().numRooms()+" in "+CMLib.map().numAreas()+" areas");

		if(CMLib.map().numAreas()==0)
		{
			Log.sysOut("NO MAPPED ROOM?!  I'll make ya one!");
			String id="START";
			Area newArea=(Area)CMClass.Objects.AREA.getNew("StdArea");
			newArea.setName("New Area");
			newArea.initChildren();
			CMLib.map().addArea(newArea);
			CMLib.database().DBCreateArea(newArea);
			Room room=(Room)CMClass.Objects.LOCALE.getNew("StdRoom");
			room.setRoomID(id);
			room.setArea(newArea);
			room.setDisplayText("New Room");
			room.setDescription("Brand new database room! You need to change this text with the MODIFY ROOM command.");
			CMLib.database().DBCreateRoom(room);
		}

		CMLib.login().initStartRooms(page);
		CMLib.login().initDeathRooms(page);
		CMLib.login().initBodyRooms(page);
		CMProps.Strings.MUDSTATUS.setProperty("Booting: readying for connections.");
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


		for(int i=0;i<CMLib.hosts().size();i++)
			((MudHost)CMLib.hosts().elementAt(i)).setAcceptConnections(true);
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
			int numAtThisAddress=0;
			long ConnectionWindow=(180*1000);
			long LastConnectionDelay=(5*60*1000);
			boolean anyAtThisAddress=false;
			int maxAtThisAddress=6;
			if(!CMSecurity.isDisabled("CONNSPAMBLOCK"))
			{
				try{
					for(int a=accessed.size()-1;a>=0;a--)
					{
						if((((Long)accessed.elementAt(a,2)).longValue()+LastConnectionDelay)<System.currentTimeMillis())
							accessed.removeElementAt(a);
						else
						if(((String)accessed.elementAt(a,1)).trim().equalsIgnoreCase(address))
						{
							anyAtThisAddress=true;
							if((((Long)accessed.elementAt(a,2)).longValue()+ConnectionWindow)>System.currentTimeMillis())
								numAtThisAddress++;
						}
					}
					if(autoblocked.contains(address.toUpperCase()))
					{
						if(!anyAtThisAddress)
							autoblocked.remove(address.toUpperCase());
						else
							proceed=2;
					}
					else
					if(numAtThisAddress>=maxAtThisAddress)
					{
						autoblocked.addElement(address.toUpperCase());
						proceed=2;
					}
				}catch(java.lang.ArrayIndexOutOfBoundsException e){}

				accessed.addElement(address,Long.valueOf(System.currentTimeMillis()));
			}

			if(proceed!=0)
			{
				Log.sysOut(Thread.currentThread().getName(),"Blocking a connection from "+address);
				PrintWriter out = new PrintWriter(sock.getOutputStream());
				out.println("\n\rOFFLINE: Blocked\n\r");
				out.flush();
				if(proceed==2)
					out.println("\n\rYour address has been blocked temporarily due to excessive invalid connections.  Please try back in " + (LastConnectionDelay/60000) + " minutes, and not before.\n\r\n\r");
				else
					out.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
				out.flush();
				try{Thread.sleep(250);}catch(Exception e){}
				out.close();
				sock = null;
			}
			else
			{
				state=2;
				// also the intro page
				CMFile introDir=new CMFile(Resources.makeFileResourceName("text"),null,false,true);
				String introFilename="text/intro.txt";
				if(introDir.isDirectory())
				{
					CMFile[] files=introDir.listFiles();
					Vector choices=new Vector();
					for(int f=0;f<files.length;f++)
						if(files[f].getName().toLowerCase().startsWith("intro")
						&&files[f].getName().toLowerCase().endsWith(".txt"))
							choices.addElement("text/"+files[f].getName());
					if(choices.size()>0) introFilename=(String)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
				}
				StringBuffer introText=Resources.getFileResource(introFilename,true);
				try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
				Session S=(Session)CMClass.Objects.COMMON.getNew("DefaultSession");
				S.initializeSession(sock, introText != null ? introText.toString() : null);
				S.start();
				CMLib.sessions().addElement(S);
				sock = null;
			}
		}
		else
		if((CMLib.database()!=null)&&(CMLib.database().isConnected()))
		{
			StringBuffer rejectText;

			try { rejectText = Resources.getFileResource("text/offline.txt",true);
			} catch(java.lang.NullPointerException npe) { rejectText=new StringBuffer("");}

			PrintWriter out = new PrintWriter(sock.getOutputStream());
			out.println("\n\rOFFLINE: " + CMProps.Strings.MUDSTATUS.property()+"\n\r");
			out.println(rejectText);
			out.flush();

			try{Thread.sleep(1000);}catch(Exception e){}
			out.close();
			sock = null;
		}
		else
		{
			sock.close();
			sock = null;
		}
	}

	public String getLanguage()
	{
		String lang = CMProps.instance().getStr("LANGUAGE").toUpperCase().trim();
		if(lang.length()==0) return "English";
		for(int i=0;i<LanguageLibrary.ISO_LANG_CODES.length;i++)
			if(lang.equals(LanguageLibrary.ISO_LANG_CODES[i][0]))
				return LanguageLibrary.ISO_LANG_CODES[i][1];
		return "English";
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
		{
			try
			{
				bindAddr = InetAddress.getByName(CMProps.Strings.MUDBINDADDRESS.property());
			}
			catch (UnknownHostException e)
			{
				Log.errOut(Thread.currentThread().getName(),"ERROR: MUD Server could not bind to address " + CMProps.Strings.MUDBINDADDRESS.property());
			}
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
			{
				Log.errOut(Thread.currentThread().getName(),t);
			}

			if (!serverIsRunning)
				isOK = false;
		}

		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud Server cleaning up.");

		try
		{
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		}
		catch(IOException e)
		{
		}

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
		CMLib.threads().suspendAll();
		if(S!=null)S.print("Closing MUD listeners to new connections...");
		for(int i=0;i<CMLib.hosts().size();i++)
			((MudHost)CMLib.hosts().elementAt(i)).setAcceptConnections(false);
		Log.sysOut(Thread.currentThread().getName(),"New Connections are now closed");
		if(S!=null)S.println("Done.");

		if(S!=null)S.print("Saving players...");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Saving players...");
		if(CMLib.sessions()!=null)
			for(int s=0;s<CMLib.sessions().size();s++)
			{
				Session S2=CMLib.sessions().elementAt(s);
				if(S2!=null)
				{
					MOB M = S2.mob();
					if((M!=null)&&(M.playerStats()!=null))
						M.playerStats().setLastDateTime(System.currentTimeMillis());
				}
			}
		CMLib.players().savePlayers();
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"All users saved.");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut(Thread.currentThread().getName(),"Notifying all objects of shutdown...");
		if(S!=null)S.print("Notifying all objects of shutdown...");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Notifying Objects");
		MOB mob=null;
		if(S!=null) mob=S.mob();
		CMMsg msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.SHUTDOWN),null);
		Vector roomSet=new Vector();
		try
		{
			for(Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=r.nextElement();
				R.send(msg);
				roomSet.addElement(R);
			}
		}catch(NoSuchElementException e){}
		if(S!=null)S.println("done");
		if(S!=null)S.println("Save thread stopped");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Session Thread");
		CMLib.sessions().shutdown();

/*		if(S!=null)S.print("Saving room data...");
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Rejuving the dead");
		CMLib.threads().tickAllTickers(null);
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Map Update");
		for(Enumeration e=CMLib.libraries(CMLib.LIBRARY_MAP);e.hasMoreElements();)
		{
			WorldMap map=((WorldMap)e.nextElement());
			for(Enumeration a=map.areas();a.hasMoreElements();)
				((Area)a.nextElement()).setAreaState(Area.STATE_STOPPED);
		}
		int roomCounter=0;
		Room R=null;
		for(Enumeration e=roomSet.elements();e.hasMoreElements();)
		{
			if(((++roomCounter)%200)==0)
			{
				if(S!=null) S.print(".");
				CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Map Update ("+roomCounter+")");
			}
			R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
		}
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"Map data saved.");
*/
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
				S2.kill(true,true,true);
				CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Done stopping session "+S2.getAddress());
			}
			if(S!=null)S.print(".");
		}
		if(S!=null)S.println("All users logged off");
		try{Thread.sleep(3000);}catch(Exception e){/* give sessions a few seconds to inform the map */}
		Log.sysOut(Thread.currentThread().getName(),"All users logged off.");

		if(S!=null)S.print("Stopping all threads...");
		CMLib.threads().shutdown();
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"Map Threads Stopped.");

		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...closing db connections");
		for(int d=0;d<databases.size();d++)
			((DBConnector)databases.elementAt(d)).killConnections();
		if(S!=null)S.println("Database connections closed");
		Log.sysOut(Thread.currentThread().getName(),"Database connections closed.");

		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...Clearing channels, help");

		CMLib.channels().shutdown();
		CMLib.help().shutdown();

//		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...unloading classes");
//		CMClass.shutdown();
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

//		CMProps.Strings.MUDSTATUS.setProperty("Shutting down...unloading macros");
//		CMLib.lang().clear();
		CMProps.Strings.MUDSTATUS.setProperty("Shutting down" + (keepItDown? "..." : " and restarting..."));

		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud shutdown complete.");
		if(S!=null)S.println("CoffeeMud shutdown complete.");
		bringDown=keepItDown;
		CMLib.threads().resumeAll();
		if(!keepItDown)
			if(S!=null)S.println("Restarting...");
		if(S!=null)S.kill(true,true,false);
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		CMProps.Strings.MUDSTATUS.setProperty("Shutdown: you are the special lucky chosen one!");
		for(int m=CMLib.hosts().size()-1;m>=0;m--)
			if(CMLib.hosts().elementAt(m) instanceof Thread)
			{
				try{
					CMLib.killThread((Thread)CMLib.hosts().elementAt(m),100,1);
				} catch(Throwable t){}
			}
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
			}
			catch(IOException e)
			{
			}
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
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: Session status "+S.getStatus()+"-"+CMParms.combine(S.previousCMD(),0) + "\n\r");
				}
				else
				if(tArray[i] instanceof Tickable)
				{
					Tickable T=(Tickable)tArray[i];
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+T.ID()+"-"+T.getTickStatus() + "\n\r");
				}
/*				else
				if((tArray[i] instanceof Tick)
				&&(((Tick)tArray[i]).lastClient!=null)
				&&(((Tick)tArray[i]).lastClient.clientObject!=null))
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+tArray[i].getName()+" "+((Tick)tArray[i]).lastClient.clientObject.ID()+"-"+((Tick)tArray[i]).lastClient.clientObject.name()+"-"+((Tick)tArray[i]).lastClient.clientObject.getTickStatus() + "\n\r");
*/				else
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+tArray[i].getName() + "\n\r");
			}
		}
	}

	public String getHost()
	{
		return host;
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
		public HostGroup(ThreadGroup G, String mudName, String iniFileName)
		{
			super(G,"HOST"+grpid);
			synchronized("HostGroupInit".intern()) {
				logName="mud"+((grpid>0)?("."+grpid):"");
				grpid++;
				iniFile=iniFileName;
				name=mudName;
				setDaemon(true);
			}
		}

		public void run()
		{
					try
					{
			new CMLib(); // initialize the lib
			new CMClass(); // initialize the classes

			// wait for ini to be loaded, and for other matters
			CMProps page=CMProps.loadPropPage("//"+iniFile);
			if ((page==null)||(!page.loaded))
			{
				Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
				System.out.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
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

			DBConnector currentDBconnector=new DBConnector();
			CMLib.registerLibrary(new DBInterface(currentDBconnector));
			CMLib.registerLibrary(new ProcessHTTPrequest(null,null,null,true));
			CMProps.Strings.MUDVER.setProperty(HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);

			// an arbitrary dividing line. After threadCode 0
			CMLib.registerLibrary(new ServiceEngine());
			CMProps.Strings.INIPATH.setProperty(iniFile);
			CMProps.Strings.MUDNAME.setProperty(name.replace('\'','`'));
//			CMProps.setUpLowVar(CMProps.SYSTEM_MUDNAME,name.replace('\'','`'));
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
						CMLib.hosts().addElement(mud);
						pdex=ports.indexOf(",");
					}
					MUD mud=new MUD("MUD@"+ports);
					mud.acceptConnections=false;
					mud.port=CMath.s_int(ports);
					mud.start();
					CMLib.hosts().addElement(mud);
				}

				StringBuffer str=new StringBuffer("");
				for(int m=0;m<CMLib.hosts().size();m++)
				{
					MudHost mud=(MudHost)CMLib.hosts().elementAt(m);
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
					Thread joinable=null;
					for(int i=0;i<CMLib.hosts().size();i++)
						if(CMLib.hosts().elementAt(i) instanceof Thread)
						{
							joinable=(Thread)CMLib.hosts().elementAt(i);
							break;
						}
					if(joinable!=null)
						joinable.join();
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

	public Vector getOverdueThreads()
	{
		Vector V=new Vector();
//		for(int w=0;w<webServers.size();w++)
//			V.addAll(((HTTPserver)webServers.elementAt(w)).getOverdueThreads());
		return V;
	}

	public static void main(String a[])
	{
		String nameID="";
		Vector iniFiles=CMParms.makeVector();
		if(a.length>0)
		{
			for(int i=0;i<a.length;i++)
				nameID+=" "+a[i];
			nameID=nameID.trim();
			Vector V=CMParms.paramParse(nameID);
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				if(s.toUpperCase().startsWith("BOOT=")&&(s.length()>5))
				{
					iniFiles.addElement(s.substring(5));
					V.removeElementAt(v);
					v--;
				}
			}
			nameID=CMParms.combine(V,0);
		}
		new CMLib(); // initialize this threads libs

		if(iniFiles.size()==0) iniFiles.addElement("coffeemud.ini");
		if(nameID.length()==0) nameID="Unnamed CoffeeMud";
		String iniFile=(String)iniFiles.firstElement();
		CMProps page=CMProps.loadPropPage("//"+iniFile);
		if ((page==null)||(!page.loaded))
		{
			Log.instance().startLogFiles("mud",1);
			Log.instance().setLogOutput("BOTH","BOTH","BOTH","BOTH","BOTH","BOTH","BOTH");
			Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
			System.out.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
			CMProps.Strings.MUDSTATUS.setProperty("A terminal error has occured!");
			System.exit(-1);
			return;
		}
		Log.instance().startLogFiles("mud",page.getInt("NUMLOGS"));
		Log.instance().setLogOutput(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("WRNMSGS"),page.getStr("DBGMSGS"),page.getStr("HLPMSGS"),page.getStr("KILMSGS"),page.getStr("CBTMSGS"));
		while(!bringDown)
		{
			System.out.println();
			Log.sysOut(Thread.currentThread().getName(),"CoffeeMud v K1");
			Log.sysOut(Thread.currentThread().getName(),"(C) 2000-2010 Bo Zimmerman");
			Log.sysOut(Thread.currentThread().getName(),"http://www.coffeemud.org");
			Log.sysOut(Thread.currentThread().getName(),"(C) 2010-2011 Kejardon");
			HostGroup joinable=null;
			CMLib.hosts().clear();
			for(int i=0;i<iniFiles.size();i++)
			{
				Log.sysOut(Thread.currentThread().getName(),"Starting Group "+i);
				iniFile=(String)iniFiles.elementAt(i);
				ThreadGroup G=new ThreadGroup(i+"-MUD");
				HostGroup H=new HostGroup(G,nameID,iniFile);
				H.start();
				if(joinable==null) joinable=H;
			}
			if(joinable!=null)
				try{joinable.join();}catch(Exception e){e.printStackTrace(); Log.errOut(Thread.currentThread().getName(),e); }
			System.gc();
			try{Thread.sleep(1000);}catch(Exception e){}
			System.runFinalization();
			try{Thread.sleep(1000);}catch(Exception e){}

			if(activeThreadCount(Thread.currentThread().getThreadGroup())>1)
			{
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

	public String executeCommand(String cmd)
		throws Exception
	{
		Vector V=CMParms.parse(cmd);
		if(V.size()==0) throw new CMException("Unknown command!");
		String word=(String)V.firstElement();
		throw new CMException("Unknown command: "+word);
		//return "OK";
	}
}
