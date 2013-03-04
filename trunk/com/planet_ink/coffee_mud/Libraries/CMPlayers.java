package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMPlayers extends StdLibrary implements PlayerLibrary
{
	public String ID(){return "CMPlayers";}
	//Ideally, new ConcurrentHashMap(size, 0.75, 2);
	public ConcurrentHashMap<String, MOB> playersList = null;//new ConcurrentHashMap();
	public ConcurrentHashMap<String, PlayerAccount> accountsList = null;//new ConcurrentHashMap();
	public ArrayList<MOB> playersQueue = new ArrayList();
	public ArrayList<PlayerAccount> accountsQueue = new ArrayList();

	private ThreadEngine.SupportThread thread=null;
	public ThreadEngine.SupportThread getSupportThread() { return thread;}
	
	public int numPlayers() { return playersList.size(); }
	public void queuePlayer(MOB newOne)	//Should only be called by a single thread at MUD boot, so no synch stuff
	{ playersQueue.add(newOne); }
	public void queueAccount(PlayerAccount newOne)
	{ accountsQueue.add(newOne); }
	public void unqueuePlayers()
	{
		playersList=new ConcurrentHashMap(playersQueue.size()*11/10+10, (float)0.75, 4);
		accountsList=new ConcurrentHashMap(accountsQueue.size()*11/10+10, (float)0.75, 4);
		for(MOB M : playersQueue)
			playersList.put(M.name().toUpperCase(), M);
		for(PlayerAccount M : accountsQueue)
			accountsList.put(M.accountName().toUpperCase(), M);
		playersQueue=null;
		accountsQueue=null;
	}
	public void addPlayer(MOB newOne)
	{
		//if(getPlayer(newOne.name())!=null) return;
		if(playersList.putIfAbsent(newOne.name().toUpperCase(), newOne)==newOne)
		{
			PlayerAccount acct=newOne.playerStats().getAccount();
			if(acct!=null)
				accountsList.putIfAbsent(acct.accountName().toUpperCase(), acct);
		}
	}
	public void delPlayer(MOB oneToDel) { playersList.remove(oneToDel.name().toUpperCase()); }
	public void delPlayer(String oneToDel) { playersList.remove(oneToDel.toUpperCase()); }
	public boolean swapPlayer(MOB mob, String oldName)
	{
		if(playersList.putIfAbsent(mob.name().toUpperCase(), mob)==mob)
		{
			if(playersList.remove(oldName.toUpperCase(), mob))
				return true;
			playersList.remove(mob.name().toUpperCase(), mob);
		}
		return false;
	}
	public PlayerAccount getAccount(String calledThis)
	{
		return accountsList.get(calledThis.toUpperCase());
	}
	public MOB getPlayer(String calledThis)
	{
		return playersList.get(calledThis.toUpperCase());
	}
	public boolean accountExists(String name)
	{
		return (accountsList.get(name.toUpperCase())!=null);
	}
	
	public boolean playerExists(String name)
	{
		return (playersList.get(name.toUpperCase())!=null);
	}
	public Enumeration<MOB> players() { return playersList.elements(); }
	public Enumeration<PlayerAccount> accounts() { return accountsList.elements(); }

	public void obliteratePlayer(MOB deadMOB, boolean quiet)
	{
		delPlayer(deadMOB);
		/*for(Session S : CMLib.sessions().toArray())
		{
			MOB M=S.mob();
			if((M!=null)&&(!S.killFlag())&&(M.name().equals(deadMOB.name())))
				deadMOB=S.mob();
		}*/
		CMMsg msg=CMClass.getMsg(deadMOB,null,null,EnumSet.of(CMMsg.MsgCode.RETIRE),(quiet)?null:"A horrible death cry is heard throughout the land.");
		CMLib.map().sendGlobalMessage(deadMOB, EnumSet.of(CMMsg.MsgCode.RETIRE), msg);
		//TODO: If this is done it should use a different message
		//Room deadLoc=deadMOB.location();
		//if(deadLoc!=null)
		//	deadLoc.send(msg);

		//CMLib.database().deleteObject(deadMOB);	//in the destroy()
		if(deadMOB.session()!=null)
			deadMOB.session().kill(false);
		Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
		deadMOB.destroy();
		msg.returnMsg();
	}
	
	public void obliterateAccountOnly(PlayerAccount deadAccount)
	{
		if(!accountsList.remove(deadAccount.accountName().toUpperCase(), deadAccount))
			return;

		deadAccount.destroy();
		//CMLib.database().deleteObject(deadAccount);
		Log.sysOut("Scoring",deadAccount.accountName()+" has been deleted.");
	}
	public boolean activate() {
		if(thread==null)
			thread=new ThreadEngine.SupportThread("THPlayers"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging("SAVETHREAD"));
		if(!thread.started)
			thread.start();
/*
		Vector<String> knownPlayers=CMLib.database().getUserList();
		for(String S : (String[])knownPlayers.toArray(new String[0]))
		{
			MOB M=CMClass.CREATURE.getNew("StdMOB");
			M.setName(CMStrings.capitalizeAndLower(S));
			CMLib.database().DBReadPlayer(M);
//			if(M.playerStats()!=null)
//				M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
//			M.recoverCharStats();
		}
*/		return true;
	}
	
	public boolean shutdown() {
		//playersList.clear();
		thread.shutdown();
		return true;
	}
	
	public void forceTick()
	{
		if(thread.status.equalsIgnoreCase("sleeping"))
		{
			thread.interrupt();
			return;
		}
	}

	public void run()
	{
		if((!CMSecurity.isDisabled("SAVETHREAD"))
		&&(!CMSecurity.isDisabled("PLAYERTHREAD")))
		{
/*
			thread.status("checking player titles.");
			for(Enumeration e=players();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
				if(M.playerStats()!=null)
				{
					if((CMLib.titles().evaluateAutoTitles(M))&&(!CMLib.flags().isInTheGame(M,true)))
						CMLib.database().DBUpdatePlayerMOBOnly(M);
				}
			}
			thread.status("saving players");
			savePlayers();
			thread.status("not saving players");
*/
		}
	}
/*
	public Enumeration<ThinPlayer> thinPlayers(String sort, Hashtable cache)
	{
		List<PlayerLibrary.ThinPlayer> V=(cache==null)?null:(List<PlayerLibrary.ThinPlayer>)cache.get("PLAYERLISTVECTOR"+sort);
		if(V==null)
		{
			V=CMLib.database().getExtendedUserList();
			int code=getCharThinSortCode(sort,false);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				List<PlayerLibrary.ThinPlayer> unV=V;
				V=new Vector();
				while(unV.size()>0)
				{
					ThinPlayer M=unV.get(0);
					String loweStr=getThinSortValue(M,code);
					ThinPlayer lowestM=M;
					for(int i=1;i<unV.size();i++)
					{
						M=unV.get(i);
						String val=getThinSortValue(M,code);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestM=M;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestM=M;
						}
					}
					unV.remove(lowestM);
					V.add(lowestM);
				}
			}
			if(cache!=null)
				cache.put("PLAYERLISTVECTOR"+sort,V);
		}
		return DVector.s_enum(V);
	}
	public String getThinSortValue(ThinPlayer player, int code) 
	{
		switch(code) {
		case 0: return player.name;
		case 1: return player.charClass;
		case 2: return player.race;
		case 3: return Integer.toString(player.level);
		case 4: return Integer.toString(player.age);
		case 5: return Long.toString(player.last);
		case 7: return player.ip;
		}
		return player.name;
	}
	public MOB getLoadPlayer(String last)
	{
		if(!CMProps.Bools.MUDSTARTED.property())
			return null;
		MOB M=null;
		synchronized(playersList)
		{
			M=getPlayer(last);
			if(M!=null) return M;

			for(Enumeration<MOB> p=players();p.hasMoreElements();)
			{
				MOB mob2=p.nextElement();
				if(mob2.name().equalsIgnoreCase(last))
				{ return mob2;}
			}

			if(playerExists(last))
			{
				M=CMClass.CREATURE.getNew("StdMOB");
				M.setName(CMStrings.capitalizeAndLower(last));
				CMLib.database().DBReadPlayer(M);
				if(M.playerStats()!=null)
					M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
				M.recoverCharStats();
			}
		}
		return M;
	}
	public PlayerAccount getLoadAccount(String calledThis)
	{
		if(calledThis=="") return null;
		PlayerAccount A = getAccount(calledThis);
		if(A!=null) return A;
		return CMLib.database().DBReadAccount(calledThis);
	}
	public int savePlayers()
	{
		int processed=0;
		for(Enumeration p=players();p.hasMoreElements();)
		{
			MOB mob=(MOB)p.nextElement();
			if(!mob.isMonster())
			{
				thread.status("just saving "+mob.name());
				CMLib.database().DBUpdatePlayerMOBOnly(mob);
				if((mob.name().length()==0)||(mob.playerStats()==null))
					continue;
				PlayerAccount account = mob.playerStats().getAccount();
				mob.playerStats().setLastUpdated(System.currentTimeMillis());
				if(account!=null)
				{
					thread.status("saving account "+account.accountName()+" for "+mob.name());
					CMLib.database().DBUpdateAccount(account);
					account.setLastUpdated(System.currentTimeMillis());
				}
				processed++;
			}
			else
			if((mob.playerStats()!=null)
			&&((mob.playerStats().lastUpdated()==0)
			   ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
			{
				thread.status("just saving "+mob.name());
				CMLib.database().DBUpdatePlayerMOBOnly(mob);
				if((mob.name().length()==0)||(mob.playerStats()==null))
					continue;
				mob.playerStats().setLastUpdated(System.currentTimeMillis());
				processed++;
			}
		}
		return processed;
	}
*/
}