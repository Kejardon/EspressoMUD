package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import com.planet_ink.coffee_mud.application.MUD;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CharCreation extends StdLibrary
{
	public enum LoginResult
	{
		NO_LOGIN, NORMAL_LOGIN, ACCOUNT_LOGIN, SESSION_SWAP, CCREATION_EXIT
	}
	
	public String ID(){return "CharCreation";}
	public HashMap<String,Integer> startRooms=new HashMap();
	public HashMap<String,Integer> deathRooms=new HashMap();
	public HashMap<String,Integer> bodyRooms=new HashMap();
	public final static String menuString=
				 " ^XAccount Menu^.^N\r\n"
				+" ^XL^.^w)^Hist characters\r\n"
				+" ^XN^.^w)^Hew character\r\n"
				+" ^XI^.^w)^Hmport character\r\n"
				+" ^XD^.^w)^Helete/Retire character\r\n"
				+" ^XH^.^w)^Help\r\n"
				+" ^XM^.^w)^Henu OFF\r\n"
				+" ^XQ^.^w)^Huit (logout)\r\n"
				+"\r\n^H ^w(^HEnter your character name to login^w)^H";

	public ArrayList<Race> raceQualifies()
	{
		ArrayList<Race> qualRaces = (ArrayList)Resources.getResource("NEWCHARRACES");
		if(qualRaces==null)
		{
			qualRaces = new ArrayList();
			for(Iterator<Race> r=CMClass.RACE.all();r.hasNext();)
			{
				Race R=r.next();
				if(R.availabilityCode()==1)
					qualRaces.add(R);
			}
			Resources.submitResource("NEWCHARRACES", qualRaces);
		}
		return qualRaces;
	}

	public void reloadTerminal(MOB mob)
	{
		if(mob==null) return;

		Session S=mob.session();
		reloadTerminal(mob.playerStats(), S);
	}
	public void reloadTerminal(AccountStats stats, Session S)
	{
		if(S==null || stats==null) return;

		//TODO
		S.initTelnetMode(stats.getBitmap());
		if(stats.hasBits(PlayerStats.ATT_MXP))
		{
			if(S.clientTelnetMode(Session.TELNET_MXP))
			{
				StringBuffer mxpText=Resources.getFileResource("text/mxp.txt",true);
				if(mxpText!=null)
					S.addOut("\033[7z"+mxpText.toString()+"\r\n");
			}
			else
				S.rawPrint("MXP codes have been disabled for this session.\n");
		}
		else if(S.clientTelnetMode(Session.TELNET_MXP))
		{
			S.changeTelnetMode(Session.TELNET_MXP,false);
			S.setClientTelnetMode(Session.TELNET_MXP,false);
		}
		if(stats.hasBits(PlayerStats.ATT_SOUND))
		{
			if(!S.clientTelnetMode(Session.TELNET_MSP))
				S.rawPrint("MSP sounds have been disabled for this session.\n");
		}
		else if(S.clientTelnetMode(Session.TELNET_MSP))
		{
			S.changeTelnetMode(Session.TELNET_MSP,false);
			S.setClientTelnetMode(Session.TELNET_MSP,false);
		}
	}

	public LoginResult selectAccountCharacter(PlayerAccount acct, Session session) throws java.io.IOException
	{
		if((acct==null)||(session==null)||(session.killFlag()))
			return LoginResult.NO_LOGIN;
		//session.setServerTelnetMode(Session.TELNET_ANSI,acct.hasAccBits(PlayerAccount.FLAG_ANSI));
		session.setClientTelnetMode(Session.TELNET_ANSI,acct.hasBits(PlayerStats.ATT_ANSI));
		boolean charSelected = false;
		boolean showList = acct.hasAccBits(PlayerAccount.FLAG_ACCOUNTMENUSOFF);
		StringBuffer introTextBuf;
		String introText=(String)Resources.getResource("CHARSELECTTEXT");
		if(introText==null)
		{
			introTextBuf=new CMFile("resources/text/selchar.txt",null,true).text();
			introText="\r\n\r\n"+introText.toString();
			Resources.submitResource(introText, "CHARSELECTTEXT");
		}
		session.println(null,null,null,introText);
		while((!session.killFlag())&&(!charSelected))
		{
			StringBuffer buf = new StringBuffer("");
			if(showList)
			{
				showList = false;
				buf.append("^X");
				buf.append(CMStrings.padRight("Character",20));
				buf.append(" " + CMStrings.padRight("Race",10));
//				buf.append(" " + CMStrings.padRight("Level",5));
				buf.append("^.^N\r\n");
				for(Iterator<MOB> p = acct.getLoadPlayers(); p.hasNext();)
				{
					MOB player = p.next();
					buf.append("^H");
					buf.append(CMStrings.padRight(player.name(),20));
					buf.append("^.^N " + CMStrings.padRight(player.body().raceName(),10));
//					buf.append(" " + CMStrings.padRight(""+player.level,5));
					buf.append("^.^N\r\n");
				}
				session.println(buf.toString());
				buf.setLength(0);
			}
			if(!acct.hasAccBits(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
				session.println(menuString);
			if(!session.killFlag())
				session.updateLoopTime();
			String s = session.prompt("\r\n^wCommand or Name ^H(?)^w: ^N", 3*60*1000);
			//if(s==null) return LoginResult.NO_LOGIN;	//cannot happen
			if(s.trim().length()==0) continue;
			if(s.equalsIgnoreCase("?")||(s.equalsIgnoreCase("HELP"))||s.equalsIgnoreCase("H"))
			{
				introText=(String)Resources.getResource("CHARACCTTEXT");
				if(introText==null)
				{
					introTextBuf=new CMFile("resources/help/accts.txt",null,true).text();
					introText="\r\n\r\n"+introText.toString();
					Resources.submitResource(introText, "CHARACCTTEXT");
				}
				session.println(null,null,null,introText);
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
					session.kill(false);
					return LoginResult.NO_LOGIN;
				}
				continue;
			}
			if(s.equalsIgnoreCase("NEW")||s.equalsIgnoreCase("N"))
			{
				if(!acct.hasAccBits(PlayerAccount.FLAG_NUMCHARSOVERRIDE))
				{
					if(CMSecurity.isDisabled("NEWPLAYERS"))
					{
						session.println("New characters are currently disabled for the mud.");
						continue;
					}
					if(CMProps.Ints.COMMONACCOUNTSYSTEM.property()<=acct.numPlayers())
					{
						session.println("You may only have "+CMProps.Ints.COMMONACCOUNTSYSTEM.property()+" characters.  Please retire one to create another.");
						continue;
					}
				}
				s=session.prompt("\r\nPlease enter a name for your character: ","");
				if(s.length()==0) continue;
				if((!validChars(s))
				||(CMLib.players().playerExists(s))
				||(CMLib.players().accountExists(s)&&(!s.equalsIgnoreCase(acct.accountName())))
				||(CMSecurity.isBanned(s)))
				{
					session.println("\r\nThat name is not available for new characters.\r\n  Choose another name!\r\n");
					continue;
				}
				//String login=CMStrings.titleCase(s);
				if(session.confirm("Create a new character called '"+s+"' (y/N)?", "N"))
				{
					if(!session.killFlag())
						session.updateLoopTime();
					if(createCharacter(acct, s, session) == LoginResult.CCREATION_EXIT)
						return LoginResult.CCREATION_EXIT;
				}
				continue;
			}
			if(s.equalsIgnoreCase("MENU")||s.equalsIgnoreCase("M"))
			{
				if(acct.hasAccBits(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
				{
					if(session.confirm("Turn menus back on (y/N)?", "N"))
						acct.setAccBits(PlayerAccount.FLAG_ACCOUNTMENUSOFF, false);
				}
				else
					if(session.confirm("Turn menus off (y/N)?", "N"))
						acct.setAccBits(PlayerAccount.FLAG_ACCOUNTMENUSOFF, true);
				continue;
			}
			if(s.equalsIgnoreCase("RETIRE")||s.equalsIgnoreCase("DELETE")||s.equalsIgnoreCase("D")||s.equalsIgnoreCase("R"))
			{
				s=session.prompt("\r\nPlease enter the name of the character: ","");
				if(s.length()==0) continue;
				MOB delMe = null;
				for(Iterator<MOB> p = acct.getLoadPlayers(); p.hasNext();)
				{
					MOB player = p.next();
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
			if(s.equalsIgnoreCase("IMPORT")||s.equalsIgnoreCase("I"))
			{
				if((CMProps.Ints.COMMONACCOUNTSYSTEM.property()<=acct.numPlayers())
				&&(!acct.hasAccBits(PlayerAccount.FLAG_NUMCHARSOVERRIDE)))
				{
					session.println("You may only have "+CMProps.Ints.COMMONACCOUNTSYSTEM.property()+" characters.  Please delete one to create another.");
					continue;
				}
				s=session.prompt("\r\nPlease enter the name of the character: ","");
				if(s.length()==0) continue;
				MOB newCharT = CMLib.players().getPlayer(s);
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
					session.println(newCharT.name()+" has been imported into your account.");
				}
				continue;
			}
			MOB playMe = acct.getPlayer(s);
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
			&&(!acct.hasAccBits(PlayerAccount.FLAG_MAXCONNSOVERRIDE)))
			{
				session.println("You may only have "+CMProps.Ints.MAXCONNSPERACCOUNT.property()+" of your characters on at one time.");
				continue;
			}
			LoginResult prelimResults = prelimChecks(session,playMe);
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
		if(session.confirm("\r\nDo you want ANSI colors (Y/n)?","Y"))
			acct.setBits(PlayerStats.ATT_ANSI, true);
		else
		{
			acct.setBits(PlayerStats.ATT_ANSI, false);
			//session.setServerTelnetMode(Session.TELNET_ANSI,false);
			session.setClientTelnetMode(Session.TELNET_ANSI,false);
		}
		
		StringBuffer introText=new CMFile("resources/text/newacct.txt",null,true).text();
		try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
		session.println(null,null,null,"\r\n\r\n"+introText.toString());
		
		String password = "";
		while((password.length()==0)&&(!session.killFlag()))
		{
			password=session.prompt("\r\nEnter an account password\r\n: ","");
			if(password.length()==0)
			{
				session.println("\r\nAborting account creation.");
				return LoginResult.NO_LOGIN;
			}
		}
		acct.setAccountName(login);
		acct.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
		acct.setLastIP(session.getByteAddress());
		acct.setLastDateTime(System.currentTimeMillis());
//		CMLib.database().DBCreateAccount(acct);
		StringBuffer doneText=new CMFile("resources/text/doneacct.txt",null,true).text();
		try { doneText = CMLib.httpUtils().doVirtualPage(doneText);}catch(Exception ex){}
		session.println(null,null,null,"\r\n\r\n"+doneText.toString());
		session.setAccount(acct);
		Log.sysOut("FrontDoor","Created account: "+acct.accountName());
		return LoginResult.ACCOUNT_LOGIN;
	}

	public LoginResult createCharacter(PlayerAccount acct, String login, Session session)
		throws java.io.IOException
	{
		//login=CMStrings.capitalizeAndLower(login.trim());
		StringBuffer introText=new CMFile("resources/text/newchar.txt",null,true).text();
		//try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
		session.println(null,null,null,"\r\n\r\n"+introText.toString());

		String password=(acct!=null)?acct.password():"";
		if(password.length()==0)
			while((password.length()==0)&&(!session.killFlag()))
			{
				password=session.prompt("\r\nEnter a password: ","");
				if(password.length()==0)
					session.println("\r\nYou must enter a password to continue.");
			}
		MOB mob=CMClass.CREATURE.getNew("StdMOB");
		mob.setName(login);
		mob.setPlayerStats((PlayerStats)CMClass.COMMON.getNew("DefaultPlayerStats"));
		boolean logoff=false;
		try
		{
			mob.setSession(session);
			session.setMob(mob);

			if((acct==null)||(acct.password().length()==0))
				mob.playerStats().setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
			mob.playerStats().setAccount(acct);
			Log.sysOut("FrontDoor","Creating user: "+mob.name());

			if(acct!=null)
			{
				if(acct.hasBits(PlayerStats.ATT_ANSI))
					mob.playerStats().setBits(PlayerStats.ATT_ANSI,true);
				else
				{
					mob.playerStats().setBits(PlayerStats.ATT_ANSI,false);
					//session.setServerTelnetMode(Session.TELNET_ANSI,false);
					session.setClientTelnetMode(Session.TELNET_ANSI,false);
				}
			}
			else
			if(session.confirm("\r\nDo you want ANSI colors (Y/n)?","Y"))
				mob.playerStats().setBits(PlayerStats.ATT_ANSI,true);
			else
			{
				mob.playerStats().setBits(PlayerStats.ATT_ANSI,false);
				//session.setServerTelnetMode(Session.TELNET_ANSI,false);
				session.setClientTelnetMode(Session.TELNET_ANSI,false);
			}
			if(session.clientTelnetMode(Session.TELNET_MSP))
				mob.playerStats().setBits(PlayerStats.ATT_SOUND, true);
			if(session.clientTelnetMode(Session.TELNET_MXP))
				mob.playerStats().setBits(PlayerStats.ATT_MXP, true);

			introText=new CMFile("resources/text/races.txt",null,true).text();
			//try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
			session.println(null,null,null,introText.toString());

			Body newBody=(Body)CMClass.ITEM.getNew("StdBody");
			String raceOptions;
			{
				StringBuffer listOfRaces=new StringBuffer("[");
				boolean tmpFirst = true;
				for(Race R : raceQualifies())
				{
					if(tmpFirst)
						tmpFirst = false;
					else
						listOfRaces.append(", ");
					listOfRaces.append("^H"+R.name()+"^N");
				}
				listOfRaces.append("]");
				raceOptions=listOfRaces.toString();
			}
			Race newRace=null;
			while(newRace==null)
			{
				session.print("\r\n^!Please choose from the following races (?):^N\r\n"+raceOptions);
				String raceStr=session.prompt("\r\n: ","");
				if(raceStr.trim().equalsIgnoreCase("?"))
					session.println(null,null,null,"\r\n"+new CMFile("resources/text/races.txt",null,true).text().toString());
				else
				{
					Race possibleRace=null;
					for(Race R : raceQualifies())
					{
						if(R.name().equalsIgnoreCase(raceStr))
						{
							newRace=R;
							break;
						}
						if(possibleRace==null&&R.name().toUpperCase().startsWith(raceStr.toUpperCase()))
							possibleRace=R;
					}
					if(newRace==null) newRace=possibleRace;
					
					if(newRace!=null)
					{
						String str=CMLib.help().getHelpText(newRace.ID().toUpperCase(),mob,false);
						if(str!=null) session.println("\r\n^N"+str.toString()+"\r\n");
						if(!session.confirm("^!Is ^H"+newRace.name()+"^N^! correct (Y/n)?^N","Y"))
							newRace=null;
					}
				}
			}
			newBody.setRace(newRace);
/*
			mob.charStats().setMaxPoints(CharStats.STAT_HITPOINTS, CMProps.getIntVar(CMProps.SYSTEMI_STARTHP));
			mob.charStats().setPoints(CharStats.STAT_HITPOINTS, CMProps.getIntVar(CMProps.SYSTEMI_STARTHP));
			mob.charStats().setMaxPoints(CharStats.STAT_MOVE, CMProps.getIntVar(CMProps.SYSTEMI_STARTMOVE));
			mob.charStats().setPoints(CharStats.STAT_MOVE, CMProps.getIntVar(CMProps.SYSTEMI_STARTMOVE));
			mob.charStats().setMaxPoints(CharStats.STAT_MANA, CMProps.getIntVar(CMProps.SYSTEMI_STARTMANA));
			mob.charStats().setPoints(CharStats.STAT_MANA, CMProps.getIntVar(CMProps.SYSTEMI_STARTMANA));
*/
			Gender gender=null;
			{
				Gender[] options=newRace.possibleGenders();
				if(options.length==1)
					gender=options[0];
				else
				{
					StringBuilder gPrompt=new StringBuilder("\r\n^!What is your gender (");
					for(Gender g : options)
						gPrompt.append(g.name()+", ");
					gPrompt.setLength(gPrompt.length()-2);
					gPrompt.append(")?^N");
					String gPromptDone=gPrompt.toString();
					found:
					while(gender==null)
					{
						String S=session.prompt(gPromptDone);
						for(Gender g : options)
							if(g.name().equals(S))
							{
								gender=g;
								break found;
							}
						for(Gender g : options)
							if(g.name().startsWith(S))
							{
								gender=g;
								break found;
							}
					}
				}
			}

			newBody.setGender(gender);
			mob.setBody(newBody);

			introText=new CMFile("resources/text/stats.txt",null,true).text();
			//try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
			session.println(null,null,null,"\r\n\r\n"+introText.toString());
	
			boolean mayCont=true;
			mob.recoverCharStats();
			mob.charStats().resetState();

//			mob.setStartRoom(getDefaultStartRoom(mob));
//			TODO
//			mob.baseCharStats().setAge(mob.initializeBirthday(0,mob.baseCharStats().getMyRace()));

			introText=new CMFile("resources/text/newchardone.txt",null,true).text();
			//try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
			session.println(null,null,null,"\r\n\r\n"+introText.toString());
			session.prompt("");
			if(!session.killFlag())
			{
				if(mob==session.mob())
					reloadTerminal(mob);
				mob.body().bringToLife(getDefaultStartRoom(mob),true);
				mob.location().show(mob,"^[S-NAME] appear^s!");
				CMLib.players().addPlayer(mob);
	
				mob.playerStats().setLastIP(session.getByteAddress());
				Log.sysOut("FrontDoor","Created user: "+mob.name());
				//notifyFriends(mob,"^X"+mob.name()+" has just been created.^.^?");
				Vector channels=CMLib.channels().getFlaggedChannelNames(CMChannels.ChannelFlag.NEWPLAYERS);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel((String)channels.get(i),mob.name()+" has just been created.",true);
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
			mob.session().kill(false);
			return true;
		}
		return false;
	}

	public LoginResult prelimChecks(Session session, MOB player)
	{
		Session thisSession=player.session();
		if((thisSession!=null)&&(thisSession!=session))
		{
			//Room oldRoom=M.location();
			//if(oldRoom!=null)
			//	oldRoom.getItemCollection().removeItem(M.body());
			session.setMob(player);
			player.setSession(session);
			thisSession.copySnoops(session);
			thisSession.setMob(null);
			thisSession.kill(false);
			Log.sysOut("FrontDoor","Session swap for "+player.name()+".");
			//reloadTerminal(player);
			//session.mob().body().bringToLife(oldRoom,false);
			CMLib.commands().postLook(player);
			return LoginResult.SESSION_SWAP;
		}
		return null;
	}
	
	public void notifyFriends(MOB mob, String message)
	{
		for(AccountStats friend : mob.playerStats().getFriends())
		{
			if(friend instanceof PlayerStats)
			{
				MOB M=((PlayerStats)friend).mob();
				M.tell(M, M, message);
			}
			else for(Iterator<MOB> iter=((PlayerAccount)friend).getLoadPlayers();iter.hasNext();)
			{
				MOB M=iter.next();
				M.tell(M, M, message);
			}
		}
	}

	protected static boolean validChars(String S)
	{
		if(S.length()<4) return false;
		for(int i=0;i<S.length();i++)
		{
			char ch=S.charAt(i);
			if(((ch>='a')&&(ch<='z'))||
			   ((ch>='A')&&(ch<='Z'))||
			   ((ch==' ')&&(i>=3)&&(S.charAt(i-1)!=' ')))
				continue;
			return false;
		}
		return true;
	}
	
	public LoginResult login(Session session)
		throws java.io.IOException
	{
		if(session==null) 
			return LoginResult.NO_LOGIN;

		session.setAccount(null);
		
		String login;
		if(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1)
			login=session.prompt("\r\naccount name: ");
		else
			login=session.prompt("\r\nname: ");
		if(login==null)
			return LoginResult.NO_LOGIN;
		login=login.trim();
		if(login.length()==0) 
			return LoginResult.NO_LOGIN;
		if(login.equalsIgnoreCase("MSSP-REQUEST")&&(!CMSecurity.isDisabled("MSSP")))
		{
			session.addOut(CMProps.getMSSPPacket());
//			session.kill(false);
			return LoginResult.NO_LOGIN;
		}
		//login = CMStrings.titleCase(login);
		if(!validChars(login))
		{
			session.println("Please use only A-Z, a-z, and spaces in your name, with at least 3 letters for a first name.");
			return LoginResult.NO_LOGIN;
		}
		if(CMSecurity.isBanned(login))
		{
			session.println("\r\nThis is a banned name. Choose a different one.\r\n");
			return LoginResult.NO_LOGIN;
		}
		PlayerAccount acct = null;
		MOB player = null;
		if(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1)
		{
			acct = CMLib.players().getAccount(login);
			if(acct==null)
			{
				player=CMLib.players().getPlayer(login);
				if(player != null)
				{
					PlayerStats ps=player.playerStats();
					if(ps.getAccount()==null)
					{
						String password=session.prompt("password for "+player.name()+": ");
						boolean done = true;
						if(BCrypt.checkpw(password, ps.password()))
						{
							session.println("\r\nThis mud is now using an account system.  "
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
						session.println("\r\nAccount '"+login+"' does not exist.");
						player=null;
						return LoginResult.NO_LOGIN;
					}
				}
			}
			else
			{
				String password=session.prompt("password: ");
				if(BCrypt.checkpw(password, acct.password()))
				{
					session.setAccount(acct);
					return LoginResult.ACCOUNT_LOGIN;
				}
				return LoginResult.NO_LOGIN;
			}
			if(session.confirm("\r\n'"+login+"' does not exist.\r\nIs this a new account you would like to create (y/N)?","N"))
			{
				acct = (PlayerAccount)CMClass.COMMON.getNew("DefaultPlayerAccount");
				return createAccount(acct,login,session);
			}
		}
		else
		{
			player=CMLib.players().getPlayer(login);
			if(player!=null)
			{
				String password=session.prompt("password: ");
				if(BCrypt.checkpw(password, player.playerStats().password()))
				{
					LoginResult prelimResults = prelimChecks(session,player);
					if(prelimResults!=null)
						return prelimResults;
					
					LoginResult completeResult=completeCharacterLogin(session,login);
					if(completeResult == LoginResult.NO_LOGIN)
						return completeResult;
				}
				else
				{
					Log.sysOut("FrontDoor","Failed login: "+player.name());
					session.println("\r\nInvalid password.\r\n");
					return LoginResult.NO_LOGIN;
				}
			}
			else
			{
				if(newCharactersAllowed(login,session,true))
				{
					if(session.confirm("\r\n'"+login+"' does not exist.\r\nIs this a new character you would like to create (y/N)?","N"))
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
			session.println("\r\n");
		return LoginResult.NORMAL_LOGIN;
	}

	public boolean newCharactersAllowed(String login, Session session, boolean checkPlayerName)
	{
		if(CMSecurity.isDisabled("NEWPLAYERS"))
		{
			session.print("\r\n'"+login+"' does not exist.\r\nThis server is not accepting new accounts.\r\n\r\n");
			return false;
		}
		else
		if((checkPlayerName && (CMLib.players().getPlayer(login)!=null))
		|| (!checkPlayerName && (CMLib.players().accountExists(login))))
		{
			session.println("\r\n'"+login+"' is not available for new players.\r\n  Choose another name!\r\n");
			return false;
		}
		return true;
	}
	
	public LoginResult completeCharacterLogin(Session session, String login) throws java.io.IOException
	{
		// count number of multiplays
		int numAtAddress=0;
		try{ for(int s=0;s<CMLib.sessions().size();s++)
		{
			if((CMLib.sessions().elementAt(s)!=session)
			&&(session.getAddress().equals(CMLib.sessions().elementAt(s).getAddress())))
				numAtAddress++;
		} }catch(Exception e){}

		if((CMProps.Ints.MAXCONNSPERIP.property()>0)
		&&(numAtAddress>=CMProps.Ints.MAXCONNSPERIP.property()))
		{
			session.println("The maximum player limit has already been reached for your IP address.");
			return LoginResult.NO_LOGIN;
		}
		
		MOB mob=CMLib.players().getPlayer(login);
		boolean showMessage=(mob.session()!=null);
		session.setMob(mob);
		mob.setSession(session);
		if(loginsDisabled(mob))
			return LoginResult.NO_LOGIN;
		//mob.body().bringToLife(mob.location(),false);	//NOTE: Chars stats will remain whatever they were before because of this
		if(showMessage) mob.location().show(mob,"^[S-NAME] stirs!");
		mob.playerStats().setLastIP(session.getByteAddress());
		notifyFriends(mob,"^X"+mob.name()+" has logged on.^.^?");
		Vector<String> channels=CMLib.channels().getFlaggedChannelNames(CMChannels.ChannelFlag.LOGINS);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(channels.get(i),mob.name()+" has logged on.",true);
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
		Integer roomID=startRooms.get("ALL");

		Room room=null;
		if(roomID!=null)
			room=SIDLib.ROOM.get(roomID.intValue());
		else
			room=SIDLib.ROOM.get(10000);
//		if(room==null)
//			room=CMLib.map().getRoom("START");
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=(Room)CMLib.map().rooms().next();
		return room;
	}

	public Room getDefaultDeathRoom(MOB mob)
	{
/*		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race=race.replace(' ','_');
		String roomID=(String)deathRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
*/
		Integer roomID=deathRooms.get("ALL");

		Room room=null;
//		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
//			room=mob.getStartRoom();
		if(roomID!=null)
			room=SIDLib.ROOM.get(roomID.intValue());
		else
			room=SIDLib.ROOM.get(10000);
//		if(room==null)
//			room=mob.getStartRoom();
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=(Room)CMLib.map().rooms().next();
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
		Integer roomID=bodyRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.intValue()==0))
			room=mob.location();
		if((room==null)&&(roomID!=null))
			room=SIDLib.ROOM.get(roomID.intValue());
		if(room==null)
			room=mob.location();
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=CMLib.map().rooms().next();
		return room;
	}

	public void pageRooms(CMProps page, HashMap<String,Integer> table, String start)
	{
		for(Enumeration<String> i=(Enumeration)page.keys();i.hasMoreElements();)
		{
			String k=i.nextElement();
			if(k.startsWith(start+"_"))
				table.put(k.substring(start.length()+1),CMath.s_int(page.getProperty(k)));
		}
		String thisOne=page.getProperty(start);
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("ALL",CMath.s_int(thisOne));
	}

	public void initStartRooms(CMProps page)
	{
		HashMap<String, Integer> tempMap=new HashMap();
		pageRooms(page,tempMap,"START");
		startRooms=tempMap;
	}

	public void initDeathRooms(CMProps page)
	{
		HashMap<String, Integer> tempMap=new HashMap();
		pageRooms(page,tempMap,"DEATH");
		deathRooms=tempMap;
	}

	public void initBodyRooms(CMProps page)
	{
		HashMap<String, Integer> tempMap=new HashMap();
		pageRooms(page,tempMap,"MORGUE");
		bodyRooms=tempMap;
	}

	public boolean shutdown() {
		/*bodyRooms=new HashMap();
		startRooms=new HashMap();
		deathRooms=new HashMap();*/
		return true;
	}
/*
	public boolean isOkName(String login)
	{
		if(login.length()>20) return false;
		if(login.length()<3) return false;
		login=login.toUpperCase().trim();
		for(int c=0;c<login.length();c++)
		{
			char C=Character.toUpperCase(login.charAt(c));
			if(("ABCDEFGHIJKLMNOPQRSTUVWXYZ ").indexOf(C)<0)
				return false;
		}
		return !CMSecurity.isBanned(login);
	}
*/
}
