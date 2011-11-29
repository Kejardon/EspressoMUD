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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
public class CharCreation extends StdLibrary implements CharCreationLibrary
{
	public String ID(){return "CharCreation";}
	public Hashtable<String,String> startRooms=new Hashtable();
	public Hashtable<String,String> deathRooms=new Hashtable();
	public Hashtable<String,String> bodyRooms=new Hashtable();

	public Vector<Race> raceQualifies()
	{
		Vector<Race> qualRaces = new Vector();
		for(Iterator<Race> r=(Iterator<Race>)CMClass.Objects.RACE.all();r.hasNext();)
		{
			Race R=r.next();
			if(R.availabilityCode()==1)
				qualRaces.add(R);
		}
		return qualRaces;
	}

	public boolean isOkName(String login)
	{
		if(login.length()>20) return false;
		if(login.length()<3) return false;

		login=login.toUpperCase().trim();
		Vector V=CMParms.parse(login);

		for(int c=0;c<login.length();c++)
		{
			char C=Character.toUpperCase(login.charAt(c));
			if(("ABCDEFGHIJKLMNOPQRSTUVWXYZ ").indexOf(C)<0)
				return false;
		}
		return !CMSecurity.isBanned(login);
	}

	public void reloadTerminal(MOB mob)
	{
		if(mob==null) return;

		Session S=mob.session();
		if(S==null) return;

		S.initTelnetMode(mob.playerStats().getBitmap());
		if(((mob.playerStats().getBitmap()&PlayerStats.ATT_MXP)!=0)
		&&(!CMSecurity.isDisabled("MXP")))
		{
			if(S.clientTelnetMode(Session.TELNET_MXP))
			{
				StringBuffer mxpText=Resources.getFileResource("text/mxp.txt",true);
				if(mxpText!=null)
					S.rawOut("\033[6z"+mxpText.toString()+"\n\r");
			}
			else
				mob.tell("MXP codes have been disabled for this session.");
		}
		else
		if(S.clientTelnetMode(Session.TELNET_MXP))
		{
			S.changeTelnetMode(Session.TELNET_MXP,false);
			S.setClientTelnetMode(Session.TELNET_MXP,false);
		}

		if(((mob.playerStats().getBitmap()&PlayerStats.ATT_SOUND)!=0)
		&&(!CMSecurity.isDisabled("MSP")))
		{
			if(!S.clientTelnetMode(Session.TELNET_MSP))
				mob.tell("MSP sounds have been disabled for this session.");
		}
		else
		if(S.clientTelnetMode(Session.TELNET_MSP))
		{
			S.changeTelnetMode(Session.TELNET_MSP,false);
			S.setClientTelnetMode(Session.TELNET_MSP,false);
		}
	}

	public LoginResult selectAccountCharacter(PlayerAccount acct, Session session) throws java.io.IOException
	{
		if((acct==null)||(session==null)||(session.killFlag()))
			return LoginResult.NO_LOGIN;
		session.setServerTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.FLAG_ANSI));
		session.setClientTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.FLAG_ANSI));
		boolean charSelected = false;
		boolean showList = acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF);
		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"selchar.txt",null,true).text();
		try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
		session.println(null,null,null,"\n\r\n\r"+introText.toString());
		while((!session.killFlag())&&(!charSelected))
		{
			StringBuffer buf = new StringBuffer("");
			if(showList)
			{
				showList = false;
				buf.append("^X");
				buf.append(CMStrings.padRight("Character",20));
//				buf.append(" " + CMStrings.padRight("Race",10));
//				buf.append(" " + CMStrings.padRight("Level",5));
				buf.append("^.^N\n\r");
				for(Enumeration<MOB> p = acct.getLoadPlayers(); p.hasMoreElements();)
				{
					MOB player = p.nextElement();
					buf.append("^H");
					buf.append(CMStrings.padRight(player.name(),20));
					buf.append("^.^N");
//					buf.append(" " + CMStrings.padRight(player.race,10));
//					buf.append(" " + CMStrings.padRight(""+player.level,5));
					buf.append("^.^N\n\r");
				}
				session.println(buf.toString());
				buf.setLength(0);
			}
			if(!acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
			{
				buf.append(" ^XAccount Menu^.^N\n\r");
				buf.append(" ^XL^.^w)^Hist characters\n\r");
				buf.append(" ^XN^.^w)^Hew character\n\r");
				buf.append(" ^XI^.^w)^Hmport character\n\r");
				buf.append(" ^XD^.^w)^Helete/Retire character\n\r");
				buf.append(" ^XH^.^w)^Help\n\r");
				buf.append(" ^XM^.^w)^Henu OFF\n\r");
				buf.append(" ^XQ^.^w)^Huit (logout)\n\r");
				buf.append("\n\r^H ^w(^HEnter your character name to login^w)^H");
				session.println(buf.toString());
				buf.setLength(0);
			}
			if(!session.killFlag())
				session.updateLoopTime();
			String s = session.prompt("\n\r^wCommand or Name ^H(?)^w: ^N", 3*60*1000);
			if(s==null) return LoginResult.NO_LOGIN;
			if(s.trim().length()==0) continue;
			if(s.equalsIgnoreCase("?")||(s.equalsIgnoreCase("HELP"))||s.equalsIgnoreCase("H"))
			{
				introText=new CMFile(Resources.buildResourcePath("help")+"accts.txt",null,true).text();
				try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
				session.println(null,null,null,"\n\r\n\r"+introText.toString());
				continue;
			}
			if(s.equalsIgnoreCase("LIST")||s.equalsIgnoreCase("L"))
			{
				showList = true;
				continue;
			}
			if(s.equalsIgnoreCase("QUIT")||s.equalsIgnoreCase("Q"))
			{
				if(session.confirm("Quit -- are you sure (y/N)?", "N"))
				{
					session.kill(false,false,false);
					return LoginResult.NO_LOGIN;
				}
				continue;
			}
			if(s.equalsIgnoreCase("NEW")||s.equalsIgnoreCase("N"))
			{
				if((CMProps.Ints.COMMONACCOUNTSYSTEM.property()<=acct.numPlayers())
				&&(!acct.isSet(PlayerAccount.FLAG_NUMCHARSOVERRIDE)))
				{
					session.println("You may only have "+CMProps.Ints.COMMONACCOUNTSYSTEM.property()+" characters.  Please retire one to create another.");
					continue;
				}
				s=session.prompt("\n\rPlease enter a name for your character: ","");
				if(s.length()==0) continue;
				if((!isOkName(s))
				||(CMLib.players().playerExists(s))
				||(CMLib.players().accountExists(s)&&(!s.equalsIgnoreCase(acct.accountName()))))
				{
					session.println("\n\rThat name is not available for new characters.\n\r  Choose another name!\n\r");
					continue;
				}
				if(newCharactersAllowed(s,session,true))
				{
					String login=CMStrings.capitalizeAndLower(s);
					if(session.confirm("Create a new character called '"+login+"' (y/N)?", "N"))
					{
						if(!session.killFlag())
							session.updateLoopTime();
						if(createCharacter(acct, login, session) == LoginResult.CCREATION_EXIT)
							return LoginResult.CCREATION_EXIT;
					}
				}
				continue;
			}
			if(s.equalsIgnoreCase("MENU")||s.equalsIgnoreCase("M"))
			{
				if(acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF)&&(session.confirm("Turn menus back on (y/N)?", "N")))
					acct.setFlag(PlayerAccount.FLAG_ACCOUNTMENUSOFF, false);
				else
				if(!acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF)&&(session.confirm("Turn menus off (y/N)?", "N")))
					acct.setFlag(PlayerAccount.FLAG_ACCOUNTMENUSOFF, true);
				continue;
			}
			if(s.toUpperCase().startsWith("RETIRE ")||s.toUpperCase().startsWith("DELETE ")||s.equalsIgnoreCase("D"))
			{
				if(s.length()>=7)
					s=s.substring(7).trim();
				else
					s=s.substring(1).trim();
				if(s.length()==0)
				{
					s=session.prompt("\n\rPlease enter the name of the character: ","");
					if(s.length()==0) continue;
				}
				MOB delMe = null;
				for(Enumeration<MOB> p = acct.getLoadPlayers(); p.hasMoreElements();)
				{
					MOB player = p.nextElement();
					if(player.name().equalsIgnoreCase(s))
					{
						delMe=player;
						break;
					}
				}
				if(delMe==null)
				{
					session.println("The character '"+s+"' is unknown.");
					continue;
				}
				if(session.confirm("Are you sure you want to retire and delete '"+delMe.name()+"' (y/N)?", "N"))
				{
					CMLib.players().obliteratePlayer(delMe, false);
					session.println(delMe.name()+" has been deleted.");
				}
				continue;
			}
			if(s.toUpperCase().startsWith("IMPORT ")||s.equalsIgnoreCase("I"))
			{
				if(s.length()>=7)
					s=s.substring(7).trim();
				else
					s=s.substring(1).trim();
				if(s.length()==0)
				{
					s=session.prompt("\n\rPlease enter the name of the character: ","");
					if(s.length()==0) continue;
				}
				if((CMProps.Ints.COMMONACCOUNTSYSTEM.property()<=acct.numPlayers())
				&&(!acct.isSet(PlayerAccount.FLAG_NUMCHARSOVERRIDE)))
				{
					session.println("You may only have "+CMProps.Ints.COMMONACCOUNTSYSTEM.property()+" characters.  Please delete one to create another.");
					continue;
				}
				MOB newCharT = CMLib.players().getLoadPlayer(s);
				if(newCharT==null)
				{
					session.println("Character not found.");
					continue;
				}
				PlayerStats ps=newCharT.playerStats();
				if(ps.getAccount()!=null)
				{
					session.println("Character already belongs to an account.");
					continue;
				}

				String password;
				password = session.prompt("Enter the password for this character: ");
				if((password==null)||(password.trim().length()==0))
					session.println("Aborted.");
				else
				if(!BCrypt.checkpw(password, ps.password()))
					session.println("Character password is incorrect.");
				else
				{
					acct.addNewPlayer(newCharT);
					ps.setAccount(acct);
					CMLib.database().DBUpdateAccount(acct);
					CMLib.database().DBUpdatePlayer(newCharT);
					session.println(newCharT.name()+" has been imported into your account.");
				}
				continue;
			}
			boolean wizi=s.trim().endsWith(" !");
			if(wizi) s=s.substring(0,s.length()-2).trim();
			MOB playMe = null;
			if(acct.isPlayer(s))
				playMe = CMLib.players().getLoadPlayer(s);
			if(playMe == null)
			{
				session.println("'"+s+"' is an unknown character or command.  Use ? for help.");
				continue;
			}
			int numAccountOnline=0;
			for(int si=0;si<CMLib.sessions().size();si++)
			{
				Session S=CMLib.sessions().elementAt(si);
				if((S!=null)
				&&(S.mob()!=null)
				&&(S.mob().playerStats()!=null)
				&&(S.mob().playerStats().getAccount()==acct))
					numAccountOnline++;
			}
			if((CMProps.Ints.MAXCONNSPERACCOUNT.property()>0)
			&&(numAccountOnline>=CMProps.Ints.MAXCONNSPERACCOUNT.property())
			&&(!CMSecurity.isDisabled("MAXCONNSPERACCOUNT"))
			&&(!acct.isSet(PlayerAccount.FLAG_MAXCONNSOVERRIDE)))
			{
				session.println("You may only have "+CMProps.Ints.MAXCONNSPERACCOUNT.property()+" of your characters on at one time.");
				continue;
			}
			LoginResult prelimResults = prelimChecks(session,playMe.name(),playMe);
			if(prelimResults!=null)
				return prelimResults;
			LoginResult completeResult=completeCharacterLogin(session,playMe.name());
			if(completeResult == LoginResult.NO_LOGIN)
				continue;
			charSelected=true;
		}
		return LoginResult.NORMAL_LOGIN;
	}
	
	public LoginResult createAccount(PlayerAccount acct, String login, Session session)
		throws java.io.IOException
	{
		Log.sysOut("FrontDoor","Creating account: "+acct.accountName());
		login=CMStrings.capitalizeAndLower(login.trim());
		if(session.confirm("\n\rDo you want ANSI colors (Y/n)?","Y"))
			acct.setFlag(PlayerAccount.FLAG_ANSI, true);
		else
		{
			acct.setFlag(PlayerAccount.FLAG_ANSI, false);
			session.setServerTelnetMode(Session.TELNET_ANSI,false);
			session.setClientTelnetMode(Session.TELNET_ANSI,false);
		}
		
		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newacct.txt",null,true).text();
		try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
		session.println(null,null,null,"\n\r\n\r"+introText.toString());
		
		String password = "";
		while((password.length()==0)&&(!session.killFlag()))
		{
			password=session.prompt("\n\rEnter an account password\n\r: ","");
			if(password.length()==0)
			{
				session.println("\n\rAborting account creation.");
				return LoginResult.NO_LOGIN;
			}
		}
		acct.setAccountName(login);
		acct.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
		acct.setLastIP(session.getAddress());
		acct.setLastDateTime(System.currentTimeMillis());
		CMLib.database().DBCreateAccount(acct);
		StringBuffer doneText=new CMFile(Resources.buildResourcePath("text")+"doneacct.txt",null,true).text();
		try { doneText = CMLib.httpUtils().doVirtualPage(doneText);}catch(Exception ex){}
		session.println(null,null,null,"\n\r\n\r"+doneText.toString());
		session.setAccount(acct);
		Log.sysOut("FrontDoor","Created account: "+acct.accountName());
		return LoginResult.ACCOUNT_LOGIN;
	}

	public LoginResult createCharacter(PlayerAccount acct, String login, Session session)
		throws java.io.IOException
	{
		login=CMStrings.capitalizeAndLower(login.trim());
		
		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newchar.txt",null,true).text();
		try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
		session.println(null,null,null,"\n\r\n\r"+introText.toString());

		String password=(acct!=null)?acct.password():"";
		if(password.length()==0)
			while((password.length()==0)&&(!session.killFlag()))
			{
				password=session.prompt("\n\rEnter a password: ","");
				if(password.length()==0)
					session.println("\n\rYou must enter a password to continue.");
			}
		MOB mob=(MOB)CMClass.Objects.MOB.getNew("StdMOB");
		mob.setPlayerStats((PlayerStats)CMClass.Objects.COMMON.getNew("DefaultPlayerStats"));
		mob.setName(login);
		boolean logoff=false;
		try
		{
			mob.setSession(session);
			session.setMob(mob);
			
			if((acct==null)||(acct.password().length()==0))
			{
				mob.playerStats().setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
			}
			mob.playerStats().setAccount(acct);
			Log.sysOut("FrontDoor","Creating user: "+mob.name());
	
			if(acct!=null)
			{
				if(acct.isSet(PlayerAccount.FLAG_ANSI))
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_ANSI);
				else
				{
					mob.playerStats().setBitmap(mob.playerStats().getBitmap()&~PlayerStats.ATT_ANSI);
					session.setServerTelnetMode(Session.TELNET_ANSI,false);
					session.setClientTelnetMode(Session.TELNET_ANSI,false);
				}
			}
			else
			if(session.confirm("\n\rDo you want ANSI colors (Y/n)?","Y"))
				mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_ANSI);
			else
			{
				mob.playerStats().setBitmap(mob.playerStats().getBitmap()&~PlayerStats.ATT_ANSI);
				session.setServerTelnetMode(Session.TELNET_ANSI,false);
				session.setClientTelnetMode(Session.TELNET_ANSI,false);
			}
			if((session.clientTelnetMode(Session.TELNET_MSP))
			&&(!CMSecurity.isDisabled("MSP")))
				mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_SOUND);
			if((session.clientTelnetMode(Session.TELNET_MXP))
			&&(!CMSecurity.isDisabled("MXP")))
				mob.playerStats().setBitmap(mob.playerStats().getBitmap()|PlayerStats.ATT_MXP);
			if(!CMSecurity.isDisabled("RACES"))
			{
				introText=new CMFile(Resources.buildResourcePath("text")+"races.txt",null,true).text();
				try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
				session.println(null,null,null,introText.toString());
			}

			Body newBody=new Body.DefaultBody();
			StringBuffer listOfRaces=new StringBuffer("[");
			boolean tmpFirst = true;
			Vector qualRaces = raceQualifies();
			for(Enumeration r=qualRaces.elements();r.hasMoreElements();)
			{
				Race R=(Race)r.nextElement();
				if (!tmpFirst)
					listOfRaces.append(", ");
				else
					tmpFirst = false;
				listOfRaces.append("^H"+R.name()+"^N");
			}
			listOfRaces.append("]");
			Race newRace=null;
			if(CMSecurity.isDisabled("RACES"))
			{
				newRace=(Race)CMClass.Objects.RACE.get("PlayerRace");
				if(newRace==null)
					newRace=(Race)CMClass.Objects.RACE.get("StdRace");
			}
			while(newRace==null)
			{
				session.print("\n\r^!Please choose from the following races (?):^N\n\r");
				session.print(listOfRaces.toString());
				String raceStr=session.prompt("\n\r: ","");
				if(raceStr.trim().equalsIgnoreCase("?"))
					session.println(null,null,null,"\n\r"+new CMFile(Resources.buildResourcePath("text")+"races.txt",null,true).text().toString());
				else
				{
					newRace=(Race)CMClass.Objects.RACE.get(raceStr);
					if((newRace!=null)&&(newRace.availabilityCode()!=1))
						newRace=null;
					if(newRace==null)
						for(Iterator<Race> r=(Iterator<Race>)CMClass.Objects.RACE.all();r.hasNext();)
						{
							Race R=r.next();
							if((R.name().equalsIgnoreCase(raceStr))
							&&(R.availabilityCode()==1))
							{
								newRace=R;
								break;
							}
						}
					if(newRace==null)
						for(Iterator<Race> r=(Iterator<Race>)CMClass.Objects.RACE.all();r.hasNext();)
						{
							Race R=r.next();
							if((R.name().toUpperCase().startsWith(raceStr.toUpperCase()))
							&&(R.availabilityCode()==1))
							{
								newRace=R;
								break;
							}
						}
					if(newRace!=null)
					{
						StringBuilder str=CMLib.help().getHelpText(newRace.ID().toUpperCase(),mob,false);
						if(str!=null) session.println("\n\r^N"+str.toString()+"\n\r");
						if(!session.confirm("^!Is ^H"+newRace.name()+"^N^! correct (Y/n)?^N","Y"))
							newRace=null;
					}
				}
			}
//			mob.baseCharStats().setMyRace(newRace);
/*
			mob.charStats().setMaxPoints(CharStats.STAT_HITPOINTS, CMProps.getIntVar(CMProps.SYSTEMI_STARTHP));
			mob.charStats().setPoints(CharStats.STAT_HITPOINTS, CMProps.getIntVar(CMProps.SYSTEMI_STARTHP));
			mob.charStats().setMaxPoints(CharStats.STAT_MOVE, CMProps.getIntVar(CMProps.SYSTEMI_STARTMOVE));
			mob.charStats().setPoints(CharStats.STAT_MOVE, CMProps.getIntVar(CMProps.SYSTEMI_STARTMOVE));
			mob.charStats().setMaxPoints(CharStats.STAT_MANA, CMProps.getIntVar(CMProps.SYSTEMI_STARTMANA));
			mob.charStats().setPoints(CharStats.STAT_MANA, CMProps.getIntVar(CMProps.SYSTEMI_STARTMANA));
*/
			String Gender="";
			while(Gender.length()==0)
				Gender=session.choose("\n\r^!What is your gender (M/F/N)?^N","MFN","");
	
//			mob.baseCharStats().setGender(Gender.toUpperCase().charAt(0));
//			mob.baseCharStats().getMyRace().startRacing(mob,false);
	
			introText=new CMFile(Resources.buildResourcePath("text")+"stats.txt",null,true).text();
			try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
			session.println(null,null,null,"\n\r\n\r"+introText.toString());
	
			boolean mayCont=true;
			mob.recoverCharStats();
			mob.charStats().resetState();

//			mob.setStartRoom(getDefaultStartRoom(mob));
//			TODO
//			mob.baseCharStats().setAge(mob.initializeBirthday(0,mob.baseCharStats().getMyRace()));

			introText=new CMFile(Resources.buildResourcePath("text")+"newchardone.txt",null,true).text();
			try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
			session.println(null,null,null,"\n\r\n\r"+introText.toString());
			session.prompt("");
			if(!session.killFlag())
			{
				if(mob==session.mob())
					reloadTerminal(mob);
				mob.body().bringToLife(getDefaultStartRoom(mob),true);
				mob.location().show(mob,mob.location(),null,EnumSet.of(CMMsg.MsgCode.ALWAYS,CMMsg.MsgCode.ENTER),"<S-NAME> appears!");
//				mob.playerStats().leveledDateTime(0);
				CMLib.database().DBCreateCharacter(mob);
				CMLib.players().addPlayer(mob);
	
				mob.playerStats().setLastIP(session.getAddress());
				Log.sysOut("FrontDoor","Created user: "+mob.name());
				CMProps.addNewUserByIP(session.getAddress());
				notifyFriends(mob,"^X"+mob.name()+" has just been created.^.^?");
				CMLib.database().DBUpdatePlayer(mob);
				Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.NEWPLAYERS);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel((String)channels.elementAt(i),mob.name()+" has just been created.",true);
			}
		}
		catch(Throwable t)
		{
			logoff=true;
			mob.playerStats().setAccount(null);
			mob.setPlayerStats(null);
			mob.setSession(null);
			session.setMob(null);
			mob.destroy();
			//if((t.getMessage()!=null)&&(t.getMessage().trim().length()>0))
				Log.errOut("CharCreation",t);
		}
		return logoff?LoginResult.NO_LOGIN:LoginResult.CCREATION_EXIT;
	}

	private boolean loginsDisabled(MOB mob)
	{
		if((CMSecurity.isDisabled("LOGINS"))&&(!CMSecurity.isASysOp(mob)))
		{
			StringBuffer rejectText=Resources.getFileResource("text/nologins.txt",true);
			try { rejectText = CMLib.httpUtils().doVirtualPage(rejectText);}catch(Exception ex){}
			if((rejectText!=null)&&(rejectText.length()>0))
				mob.session().println(rejectText.toString());
			try{Thread.sleep(1000);}catch(Exception e){}
			mob.session().kill(false,false,false);
			return true;
		}
		return false;
	}

	public LoginResult prelimChecks(Session session, String login, MOB player)
	{
		if(CMSecurity.isBanned(login))
		{
			session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
			session.kill(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		if(player!=null)
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session thisSession=CMLib.sessions().elementAt(s);
			MOB M=thisSession.mob();
			if((M!=null)
			&&(thisSession!=session)
			&&(M==player))
			{
				Room oldRoom=M.location();
				if(oldRoom!=null)
					oldRoom.getItemCollection().removeItem(M.body());
				session.setMob(M);
				M.setSession(session);
				thisSession.setMob(null);
				thisSession.kill(false,false,false);
				Log.sysOut("FrontDoor","Session swap for "+session.mob().name()+".");
				reloadTerminal(session.mob());
				session.mob().body().bringToLife(oldRoom,false);
				return LoginResult.SESSION_SWAP;
			}
		}
		return null;
	}
	
	public void notifyFriends(MOB mob, String message)
	{
		try {
			for(int s=0;s<CMLib.sessions().size();s++)
			{
				Session sessionS=CMLib.sessions().elementAt(s);
				if(sessionS!=null)
				{
					MOB listenerM=sessionS.mob();
					PlayerStats listenerPStats=listenerM.playerStats();
					if((listenerPStats!=null)
					&&((listenerPStats.getFriends().contains(mob.name())||listenerPStats.getFriends().contains("All"))))
						listenerM.tell(message);
				}
			}
		} catch(Exception e){}
	}

	private String getMSSPPacket()
	{
		StringBuffer rpt = new StringBuffer("\r\nMSSP-REPLY-START");
		rpt.append("\r\n"); rpt.append("PLAYERS");
		rpt.append("\t"); rpt.append(Integer.toString(CMLib.sessions().size()));
		rpt.append("\r\n"); rpt.append("STATUS");
		rpt.append("\t"); rpt.append("Alpha");
		
		MudHost host = null;
		if(CMLib.hosts().size()>0)
			host = (MudHost)CMLib.hosts().firstElement();
		if(host != null)
		{
			rpt.append("\r\n"); rpt.append("UPTIME");
			rpt.append("\t"); rpt.append(Long.toString(host.getUptimeSecs()));
			rpt.append("\r\n"); rpt.append("HOSTNAME");
			rpt.append("\t"); rpt.append(host.getHost());
			rpt.append("\r\n"); rpt.append("PORT");
			rpt.append("\t"); rpt.append(Integer.toString(host.getPort()));
			rpt.append("\r\n"); rpt.append("WEBSITE");
			rpt.append("\t"); rpt.append(("http://"+host.getHost()+":"+CMLib.httpUtils().getWebServerPort()));
			rpt.append("\r\n"); rpt.append("LANGUAGE");
			rpt.append("\t"); rpt.append("English");
		}
		rpt.append("\r\n"); rpt.append("FAMILY");
		rpt.append("\t"); rpt.append("CoffeeMUD");
		rpt.append("\r\n"); rpt.append("CODEBASE");
		rpt.append("\t"); rpt.append("EspressoMUD v"+CMProps.Strings.MUDVER.property());
		rpt.append("\r\n"); rpt.append("AREAS");
		rpt.append("\t"); rpt.append(Integer.toString(CMLib.map().numAreas()));
		rpt.append("\r\n"); rpt.append("HELPFILES");
		rpt.append("\t"); rpt.append(Integer.toString(CMLib.help().getHelpFile().size()));
		rpt.append("\r\n"); rpt.append("ROOMS");
		rpt.append("\t"); rpt.append(Long.toString(CMLib.map().numRooms()));
		rpt.append("\r\n"); rpt.append("RACES");
		int numRaces = 0;
		if(!CMSecurity.isDisabled("RACES"))
			numRaces=CMLib.login().raceQualifies().size();
		rpt.append("\t"); rpt.append(Long.toString(numRaces));
		rpt.append("\r\n"); rpt.append("ANSI");
		rpt.append("\t"); rpt.append((this!=null?"1":"0"));
		rpt.append("\r\n"); rpt.append("MCCP");
		rpt.append("\t"); rpt.append((!CMSecurity.isDisabled("MCCP")?"1":"0"));
		rpt.append("\r\n"); rpt.append("MSP");
		rpt.append("\t"); rpt.append((!CMSecurity.isDisabled("MSP")?"1":"0"));
		rpt.append("\r\n"); rpt.append("MXP");
		rpt.append("\t"); rpt.append((!CMSecurity.isDisabled("MXP")?"1":"0"));
		rpt.append("\r\nMSSP-REPLY-END\r\n");
		return rpt.toString();
	}
	
	public LoginResult login(Session session, int attempt)
		throws java.io.IOException
	{
		if(session==null) 
			return LoginResult.NO_LOGIN;

		session.setAccount(null);
		
		String login;
		if(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1)
			login=session.prompt("\n\raccount name: ");
		else
			login=session.prompt("\n\rname: ");
		if(login==null)
			return LoginResult.NO_LOGIN;
		login=login.trim();
		if(login.length()==0) 
			return LoginResult.NO_LOGIN;
		if(login.equalsIgnoreCase("MSSP-REQUEST")&&(!CMSecurity.isDisabled("MSSP")))
		{
			session.rawOut(getMSSPPacket());
			session.kill(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		login = CMStrings.capitalizeAndLower(login);
		PlayerAccount acct = null;
		MOB player = null;
		if(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1)
		{
			acct = CMLib.players().getLoadAccount(login);
			if(acct==null)
			{
				player=CMLib.players().getPlayer(login);
				if(player != null)
				{
					PlayerStats ps=player.playerStats();
					if(ps.getAccount()==null)
					{
						session.print("password for "+player.name()+": ");
						String password=session.blockingIn();
						boolean done = true;
						if(BCrypt.checkpw(password, ps.password()))
						{
							session.println("\n\rThis mud is now using an account system.  "
									+"Please create a new account and use the IMPORT command to add your character(s) to your account.");
							//TODO: Properly implement this.
							done = !session.confirm("Would you like to create your new master account and call it '"+player.name()+"' (y/N)? ", "N");
						}
						player = null;
						if(done)
							return LoginResult.NO_LOGIN;
					}
					else
					{
						session.println("\n\rAccount '"+CMStrings.capitalizeAndLower(login)+"' does not exist.");
						player=null;
						return LoginResult.NO_LOGIN;
					}
				}
			}
			else
			{
				session.print("password: ");
				String password=session.blockingIn();
				if(BCrypt.checkpw(password, acct.password()))
				{
					LoginResult prelimResults = prelimChecks(session,login,null);
					if(prelimResults!=null)
						return prelimResults;
					session.setAccount(acct);
					return LoginResult.ACCOUNT_LOGIN;
				}
				return LoginResult.NO_LOGIN;
			}
			if(session.confirm("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rIs this a new account you would like to create (y/N)?","N"))
			{
				acct = (PlayerAccount)CMClass.Objects.COMMON.getNew("DefaultPlayerAccount");
				return createAccount(acct,login,session);
			}
		}
		else
		{
			player=CMLib.players().getPlayer(login);
			if(player!=null)
			{
				session.print("password: ");
				String password=session.blockingIn();
				if(BCrypt.checkpw(password, player.playerStats().password()))
				{
					LoginResult prelimResults = prelimChecks(session,login,player);
					if(prelimResults!=null)
						return prelimResults;
					
					LoginResult completeResult=completeCharacterLogin(session,login);
					if(completeResult == LoginResult.NO_LOGIN)
						return completeResult;
				}
				else
				{
					Log.sysOut("FrontDoor","Failed login: "+player.name());
					session.println("\n\rInvalid password.\n\r");
					return LoginResult.NO_LOGIN;
				}
			}
			else
			{
				if(newCharactersAllowed(login,session,true))
				{
					if(session.confirm("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
					{
						LoginResult result = LoginResult.NO_LOGIN;
						if(createCharacter(acct,login,session)==LoginResult.CCREATION_EXIT)
							result = LoginResult.NORMAL_LOGIN;
						return result;
					}
				}
				return LoginResult.NO_LOGIN;
			}
		}
		if(session!=null)
			session.println("\n\r");
		return LoginResult.NORMAL_LOGIN;
	}

	public boolean newCharactersAllowed(String login, Session session, boolean checkPlayerName)
	{
		if(CMSecurity.isDisabled("NEWPLAYERS"))
		{
			session.print("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rThis server is not accepting new accounts.\n\r\n\r");
			return false;
		}
		else
		if((!isOkName(login))
		|| (checkPlayerName && CMLib.players().playerExists(login))
		|| (!checkPlayerName && CMLib.players().accountExists(login)))
		{
			session.println("\n\r'"+CMStrings.capitalizeAndLower(login)+"' is not available for new players.\n\r  Choose another name!\n\r");
			return false;
		}
		return true;
	}
	
	public LoginResult completeCharacterLogin(Session session, String login) throws java.io.IOException
	{
		// count number of multiplays
		int numAtAddress=0;
		try{
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			if((CMLib.sessions().elementAt(s)!=session)
			&&(session.getAddress().equalsIgnoreCase((CMLib.sessions().elementAt(s).getAddress()))))
				numAtAddress++;
		}
		}catch(Exception e){}

		if((CMProps.Ints.MAXCONNSPERIP.property()>0)
		&&(numAtAddress>=CMProps.Ints.MAXCONNSPERIP.property())
		&&(!CMSecurity.isDisabled("MAXCONNSPERIP")))
		{
			session.println("The maximum player limit has already been reached for your IP address.");
			return LoginResult.NO_LOGIN;
		}
		
		MOB mob=CMLib.players().getPlayer(login);
		if((mob!=null)&&(mob.session()!=null))
		{
			session.setMob(mob);
			mob.setSession(session);
			if(loginsDisabled(mob))
				return LoginResult.NO_LOGIN;
			mob.body().bringToLife(mob.location(),false);
			mob.location().show(mob,mob.location(),null,EnumSet.of(CMMsg.MsgCode.ALWAYS,CMMsg.MsgCode.ENTER),"<S-NAME> appears!");
		}
		else
		{
			mob=CMLib.players().getLoadPlayer(login);
			mob.setSession(session);
			session.setMob(mob);
			if(loginsDisabled(mob))
				return LoginResult.NO_LOGIN;
			mob.body().bringToLife(mob.location(),true);
			mob.location().show(mob,mob.location(),null,EnumSet.of(CMMsg.MsgCode.ALWAYS,CMMsg.MsgCode.ENTER),"<S-NAME> appears!");
		}
		PlayerStats pstats = mob.playerStats();
		if((session!=null)&&(mob.playerStats()!=null))
			mob.playerStats().setLastIP(session.getAddress());
		notifyFriends(mob,"^X"+mob.name()+" has logged on.^.^?");
		Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOGINS);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel((String)channels.elementAt(i),mob.name()+" has logged on.",true);
		return LoginResult.NORMAL_LOGIN;
	}

	public Room getDefaultStartRoom(MOB mob)
	{
/*		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race=race.replace(' ','_');
		String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
		realrace=realrace.replace(' ','_');
		String roomID=(String)startRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get(realrace);
		if((roomID==null)||(roomID.length()==0))
*/
		String roomID=startRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.length()>0))
			room=CMLib.map().getRoom(roomID);
		if(room==null)
			room=CMLib.map().getRoom("START");
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=(Room)CMLib.map().rooms().nextElement();
		return room;
	}

	public Room getDefaultDeathRoom(MOB mob)
	{
/*		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race=race.replace(' ','_');
		String roomID=(String)deathRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
*/
		String roomID=deathRooms.get("ALL");

		Room room=null;
//		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
//			room=mob.getStartRoom();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=CMLib.map().getRoom(roomID);
//		if(room==null)
//			room=mob.getStartRoom();
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=(Room)CMLib.map().rooms().nextElement();
		return room;
	}

	public Room getDefaultBodyRoom(MOB mob)
	{
/*
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race=race.replace(' ','_');
		String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
		realrace=realrace.replace(' ','_');
		String roomID=(String)bodyRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)bodyRooms.get(realrace);
		if((roomID==null)||(roomID.length()==0))
*/
		String roomID=(String)bodyRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.location();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=CMLib.map().getRoom(roomID);
		if(room==null)
			room=mob.location();
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=(Room)CMLib.map().rooms().nextElement();
		return room;
	}

	public void pageRooms(CMProps page, Hashtable table, String start)
	{
		for(Enumeration i=page.keys();i.hasMoreElements();)
		{
			String k=(String)i.nextElement();
			if(k.startsWith(start+"_"))
				table.put(k.substring(start.length()+1),page.getProperty(k));
		}
		String thisOne=page.getProperty(start);
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("ALL",thisOne);
	}

	public void initStartRooms(CMProps page)
	{
		startRooms=new Hashtable();
		pageRooms(page,startRooms,"START");
	}

	public void initDeathRooms(CMProps page)
	{
		deathRooms=new Hashtable();
		pageRooms(page,deathRooms,"DEATH");
	}

	public void initBodyRooms(CMProps page)
	{
		bodyRooms=new Hashtable();
		pageRooms(page,bodyRooms,"MORGUE");
	}

	public boolean shutdown() {
		bodyRooms=new Hashtable();
		startRooms=new Hashtable();
		deathRooms=new Hashtable();
		return true;
	}

}
