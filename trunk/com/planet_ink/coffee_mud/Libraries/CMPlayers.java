package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
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
public class CMPlayers extends StdLibrary implements PlayerLibrary
{
	public String ID(){return "CMPlayers";}
	public Vector<MOB> playersList = new Vector<MOB>();
	public Vector<PlayerAccount> accountsList = new Vector<PlayerAccount>();

	private ThreadEngine.SupportThread thread=null;
	
	public ThreadEngine.SupportThread getSupportThread() { return thread;}
	
	public int numPlayers() { return playersList.size(); }
	public void addPlayer(MOB newOne)
	{
		synchronized(playersList)
		{
			if(getPlayer(newOne.name())!=null) return;
			if(playersList.contains(newOne)) return;
			PlayerAccount acct = null;
			if(newOne.playerStats()!=null)
				acct=newOne.playerStats().getAccount();
			playersList.add(newOne);
			if((acct != null)&&(getAccount(acct.accountName())==null)&&(!accountsList.contains(acct)))
				accountsList.add(acct);
		}
	}
	public void delPlayer(MOB oneToDel) { synchronized(playersList){playersList.remove(oneToDel);} }
	public PlayerAccount getLoadAccount(String calledThis)
	{
		if(calledThis=="") return null;
		PlayerAccount A = getAccount(calledThis);
		if(A!=null) return A;
		return CMLib.database().DBReadAccount(calledThis);
	}
	
	public PlayerAccount getAccount(String calledThis)
	{
		synchronized(playersList)
		{
			MOB M=null;
			for(PlayerAccount A : accountsList)
				if(A.accountName().equalsIgnoreCase(calledThis))
					return A;
			for (Enumeration p=players(); p.hasMoreElements();)
			{
				M = (MOB)p.nextElement();
				if((M.playerStats()!=null)
				&&(M.playerStats().getAccount()!=null)
				&&(M.playerStats().getAccount().accountName().equalsIgnoreCase(calledThis)))
				{
					accountsList.add(M.playerStats().getAccount());
					return M.playerStats().getAccount();
				}
			}
		}
		return null;
	}
	public MOB getPlayer(String calledThis)
	{
		MOB M = null;
		synchronized(playersList)
		{
			for (Enumeration p=players(); p.hasMoreElements();)
			{
				M = (MOB)p.nextElement();
				if (M.name().equalsIgnoreCase(calledThis))
					return M;
			}
		}
		return null;
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
				M=(MOB)CMClass.Objects.MOB.getNew("StdMOB");
				M.setName(CMStrings.capitalizeAndLower(last));
				CMLib.database().DBReadPlayer(M);
				if(M.playerStats()!=null)
					M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
				M.recoverCharStats();
			}
		}
		return M;
	}

	public boolean accountExists(String name)
	{
		if(name==null) return false;
		name=CMStrings.capitalizeAndLower(name);
		return getLoadAccount(name)!=null;
	}
	
	public boolean playerExists(String name)
	{
		if(name==null) return false;
		name=CMStrings.capitalizeAndLower(name);
		for(Enumeration<MOB> e=players();e.hasMoreElements();)
			if(e.nextElement().name().equals(name))
				return true;
		return false;
	}
	public Enumeration players() { return (Enumeration)DVector.s_enum(playersList); }
	public Enumeration accounts() { return (Enumeration)DVector.s_enum(accountsList); }

	public void obliteratePlayer(MOB deadMOB, boolean quiet)
	{
		if(getPlayer(deadMOB.name())!=null)
		{
		   deadMOB=getPlayer(deadMOB.name());
		   delPlayer(deadMOB);
		}
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			if((!S.killFlag())&&(S.mob()!=null)&&(S.mob().name().equals(deadMOB.name())))
			   deadMOB=S.mob();
		}
		CMMsg msg=CMClass.getMsg(deadMOB,null,null,EnumSet.of(CMMsg.MsgCode.RETIRE),(quiet)?null:"A horrible death cry is heard throughout the land.");
		Room deadLoc=deadMOB.location();
		if(deadLoc!=null)
			deadLoc.send(msg);
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R!=null)&&(R!=deadLoc))
				{
					if(R.okMessage(deadMOB,msg))
						R.send(msg);
					else
					{
						addPlayer(deadMOB);
						return;
					}
				}
			}
		}catch(NoSuchElementException e){}

		CMLib.database().DBDeleteMOB(deadMOB);
		if(deadMOB.session()!=null)
			deadMOB.session().kill(false,false,false);
		Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
		deadMOB.destroy();
	}
	
	public void obliterateAccountOnly(PlayerAccount deadAccount)
	{
		deadAccount = getLoadAccount(deadAccount.accountName());
		if(deadAccount==null) return;
		accountsList.remove(deadAccount);

		CMLib.database().DBDeleteAccount(deadAccount);
		Log.sysOut("Scoring",deadAccount.accountName()+" has been deleted.");
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

	public boolean activate() {
		if(thread==null)
			thread=new ThreadEngine.SupportThread("THPlayers"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging("SAVETHREAD"));
		if(!thread.started)
			thread.start();
		Vector<String> knownPlayers=CMLib.database().getUserList();
		for(String S : (String[])knownPlayers.toArray(new String[0]))
		{
			MOB M=(MOB)CMClass.Objects.MOB.getNew("StdMOB");
			M.setName(CMStrings.capitalizeAndLower(S));
			CMLib.database().DBReadPlayer(M);
//			if(M.playerStats()!=null)
//				M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
//			M.recoverCharStats();
		}
		return true;
	}
	
	public boolean shutdown() {
		playersList.clear();
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
*/
			thread.status("saving players");
			savePlayers();
			thread.status("not saving players");
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
*/
}
