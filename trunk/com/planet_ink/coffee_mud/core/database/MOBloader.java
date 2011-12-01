package com.planet_ink.coffee_mud.core.database;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.sql.*;
import java.util.*;

/*
 * Copyright 2000-2010 Bo Zimmerman Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
@SuppressWarnings("unchecked")
public class MOBloader
{
	//TODO: There should probably be a cache of thinPlayer data so the mud doesn't have to parse everything every time.
	//Scratch that: CMPlayers caches it already it looks like, just make sure cacheing in CMPlayers is good.
	//Also note to self: Don't have mobs tell their owner rooms that they're in there until the mobs are brought to life.
	protected DBConnector DB=null;

	public MOBloader(DBConnector newDB)
	{
		DB=newDB;
	}
//	protected Room emptyRoom=null;

	public boolean DBReadUserOnly(MOB mob)
	{
		if(mob.name().length()==0) return false;
		boolean found=false;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+mob.name()+"'");
			if(R.next())
			{
				CMLib.coffeeMaker().setPropertiesStr(mob, DBConnections.getRes(R,"CMDATA"));
				/*
				CharStats stats=mob.baseCharStats();
				PlayerStats pstats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
				mob.setPlayerStats(pstats);
				mob.setName(username);
				mob.setDescription(DBConnections.getRes(R,"CMDESC"));
				mob.setAgeHours(CMath.s_long(DBConnections.getRes(R,"CMAGEH")));
				mob.setWimpHitPoint(CMath.s_int(DBConnections.getRes(R,"CMWIMP")));
				mob.setLiegeID(DBConnections.getRes(R,"CMLEIG"));
				String roomID=DBConnections.getRes(R,"CMROID");
				if(roomID==null) roomID="";
				int x=roomID.indexOf("||");
				if(x>=0)
				{
					mob.setLocation(CMLib.map().getRoom(roomID.substring(x+2)));
					roomID=roomID.substring(0,x);
				}
				mob.setStartRoom(CMLib.map().getRoom(roomID));
				String username=DBConnections.getRes(R,"CMUSERID");
				String password=DBConnections.getRes(R,"CMPASS");
				pstats.setPassword(password);
				pstats.setBitmap(CMath.s_int(DBConnections.getRes(R,"CMBTMP")));
				pstats.setLastDateTime(CMath.s_long(DBConnections.getRes(R,"CMDATE")));
				pstats.setChannelMask((int)DBConnections.getLongRes(R,"CMCHAN"));
				pstats.setPrompt(DBConnections.getRes(R,"CMPRPT"));
				String colorStr=DBConnections.getRes(R,"CMCOLR");
				if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL"))) pstats.setColorStr(colorStr);
				pstats.setLastIP(DBConnections.getRes(R,"CMLSIP"));
				String buf=DBConnections.getRes(R,"CMPFIL");
				pstats.setXML(buf);
				mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));
				stats.setMyRace(CMClass.getRace(DBConnections.getRes(R,"CMRACE")));
				stats.setGender(DBConnections.getRes(R,"CMGEND").charAt(0));
				stats.setStat(CharStats.STAT_STRENGTH,CMath.s_int(DBConnections.getRes(R,"CMSTRE")));
				stats.setStat(CharStats.STAT_DEXTERITY,CMath.s_int(DBConnections.getRes(R,"CMDEXT")));
				stats.setStat(CharStats.STAT_CONSTITUTION,CMath.s_int(DBConnections.getRes(R,"CMCONS")));
				stats.setStat(CharStats.STAT_WISDOM,CMath.s_int(DBConnections.getRes(R,"CMWISD")));
				stats.setStat(CharStats.STAT_INTELLIGENCE,CMath.s_int(DBConnections.getRes(R,"CMINTE")));
				stats.setStat(CharStats.STAT_CHARISMA,CMath.s_int(DBConnections.getRes(R,"CMCHAR")));
				stats.setMaxPoints(CharStats.STAT_HITPOINTS, CMath.s_int(DBConnections.getRes(R,"CMHITP")));
				stats.setMaxPoints(CharStats.STAT_MANA, CMath.s_int(DBConnections.getRes(R,"CMMANA")));
				stats.setMaxPoints(CharStats.STAT_MOVE, CMath.s_int(DBConnections.getRes(R,"CMMOVE")));
				stats.setPoints(CharStats.STAT_HITPOINTS, stats.getMaxPoints(CharStats.STAT_HITPOINTS));
				stats.setPoints(CharStats.STAT_MANA, stats.getMaxPoints(CharStats.STAT_MANA));
				stats.setPoints(CharStats.STAT_MOVE, stats.getMaxPoints(CharStats.STAT_MOVE));
				if(mob.getBirthday()==null)
					stats.setAge(mob.initializeBirthday((int)Math.round(CMath.div(mob.getAgeHours(),60.0)),stats.getMyRace()));
				mob.baseEnvStats().setAttackAdjustment(CMath.s_int(DBConnections.getRes(R,"CMATTA")));
				mob.baseEnvStats().setArmor(CMath.s_int(DBConnections.getRes(R,"CMAMOR")));
				mob.baseEnvStats().setDamage(CMath.s_int(DBConnections.getRes(R,"CMDAMG")));
				mob.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
				mob.baseEnvStats().setWeight((int)DBConnections.getLongRes(R,"CMWEIT"));
				*/
				R.close();
				DB.DBDone(D);
				D=null;
				found=true;
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			if(D!=null) 
				DB.DBDone(D);
		}
		return found;
	}

	public void DBRead(MOB mob)
	{
		if(mob.name().length()==0) return;
//		if(emptyRoom==null) emptyRoom=CMClass.getLocale("StdRoom");
		DBReadUserOnly(mob);
		if((mob.playerStats())!=null)
			CMLib.players().addPlayer(mob);
		else
			Log.errOut("MOBLoader","No player stats for "+mob.name());
		/*
		int oldDisposition=mob.baseEnvStats().disposition();
		mob.baseEnvStats().setDisposition(EnvStats.IS_NOT_SEEN|EnvStats.IS_SNEAKING);
		mob.envStats().setDisposition(EnvStats.IS_NOT_SEEN|EnvStats.IS_SNEAKING);
		Room oldLoc=mob.location();
		boolean inhab=false;
		if(oldLoc!=null) inhab=oldLoc.isInhabitant(mob);
		mob.setLocation(emptyRoom);
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.name()+"'");
			Hashtable itemNums=new Hashtable();
			Hashtable itemLocs=new Hashtable();
			while(R.next())
			{
				String itemNum=DBConnections.getRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("MOB","Couldn't find item '"+itemID+"'");
				else
				{
					itemNums.put(itemNum,newItem);
					newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
					String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						Item container=(Item)itemNums.get(loc);
						if(container!=null)
							newItem.setContainer(container);
						else
							itemLocs.put(newItem,loc);
					}
					newItem.wearAt((int)DBConnections.getLongRes(R,"CMITWO"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
					newItem.recoverEnvStats();
					mob.addItem(newItem);
				}
			}
			for(Enumeration e=itemLocs.keys();e.hasMoreElements();)
			{
				Item keyItem=(Item)e.nextElement();
				String location=(String)itemLocs.get(keyItem);
				Item container=(Item)itemNums.get(location);
				if(container!=null)
				{
					keyItem.setContainer(container);
					keyItem.recoverEnvStats();
					container.recoverEnvStats();
				}
			}
		}catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
		D=null;
		if(oldLoc!=null)
		{
			mob.setLocation(oldLoc);
			if(inhab&&(!oldLoc.isInhabitant(mob))) 
				oldLoc.addInhabitant(mob);
		}
		// now grab the abilities
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.name()+"'");
			while(R.next())
			{
				String abilityID=DBConnections.getRes(R,"CMABID");
				int proficiency=(int)DBConnections.getLongRes(R,"CMABPF");
				if((proficiency==Integer.MIN_VALUE)||(proficiency==Integer.MIN_VALUE+1))
				{
					Behavior newBehavior=CMClass.getBehavior(abilityID);
					if(newBehavior==null)
						Log.errOut("MOB","Couldn't find behavior '"+abilityID+"'");
					else
					{
						newBehavior.setParms(DBConnections.getRes(R,"CMABTX"));
						mob.addBehavior(newBehavior);
					}
				}
				else
				{
					Ability newAbility=CMClass.getAbility(abilityID);
					if(newAbility==null)
						Log.errOut("MOB","Couldn't find ability '"+abilityID+"'");
					else
					{
						if((proficiency<0)||(proficiency==Integer.MAX_VALUE))
						{
							if(proficiency==Integer.MAX_VALUE)
							{
								newAbility.setProficiency(100);
								mob.addNonUninvokableEffect(newAbility);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							}else
							{
								proficiency=proficiency+200;
								newAbility.setProficiency(proficiency);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
								mob.addNonUninvokableEffect(newAbility);
							}
						}
					}
				}
			}
		}catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
		D=null;
		mob.baseEnvStats().setDisposition(oldDisposition);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.resetToMaxState();
		if(mob.baseCharStats()!=null)
		{
			int oldWeight=mob.baseEnvStats().weight();
			int oldHeight=mob.baseEnvStats().height();
			mob.baseCharStats().getMyRace().startRacing(mob,true);
			if(oldWeight>0) mob.baseEnvStats().setWeight(oldWeight);
			if(oldHeight>0) mob.baseEnvStats().setHeight(oldHeight);
		}
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.resetToMaxState();
		*/
		// wont add if same name already exists
	}

	public Vector<String> getUserList()
	{
		DBConnection D=null;
		Vector V=new Vector();
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				V.addElement(username);
			}
		}catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
		return V;
	}

	public void DBUpdate(MOB mob)
	{
		PlayerStats pStats = mob.playerStats();
		if((mob.name().length()==0)||(pStats==null)) return;
		pStats.setLastUpdated(System.currentTimeMillis());
		DBUpdateJustMOB(mob);
		PlayerAccount account = pStats.getAccount();
		if(account != null)
		{
			account.setLastUpdated(System.currentTimeMillis());
			DBUpdateAccount(account);
		}
	}

	public void DBUpdateJustMOB(MOB mob)
	{
		DB.update("UPDATE CMCHAR SET CMDATA='"+CMLib.coffeeMaker().getPropertiesStr(mob)
				/* CMPASS='"+pstats.password()+"'"
				+", CMRACE='"+mob.baseCharStats().getMyRace().ID()+"'"
				+", CMGEND='"+((char)mob.baseCharStats().gender())+"'"
				+", CMSTRE="+mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)
				+", CMINTE="+mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)
				+", CMDEXT="+mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)
				+", CMCONS="+mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)
				+", CMCHAR="+mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)
				+", CMWISD="+mob.baseCharStats().getStat(CharStats.STAT_WISDOM)
				+", CMHITP="+mob.baseCharStats().getPoints(CharStats.STAT_HITPOINTS)
				+", CMMANA="+mob.baseCharStats().getPoints(CharStats.STAT_MANA)
				+", CMMOVE="+mob.baseCharStats().getPoints(CharStats.STAT_MOVE)
				+", CMAGEH="+mob.getAgeHours()
				+", CMWIMP="+mob.getWimpHitPoint()
				+", CMROID='"+strStartRoomID+"||"+strOtherRoomID+"'"
				+", CMDATE='"+pstats.lastDateTime()+"'"
				+", CMCHAN="+pstats.getChannelMask()
				+", CMATTA="+mob.baseEnvStats().attackAdjustment()
				+", CMAMOR="+mob.baseEnvStats().armor()
				+", CMDAMG="+mob.baseEnvStats().damage()
				+", CMBTMP="+mob.playerStats().getBitmap()
				+", CMLEIG='"+mob.getLiegeID()+"'"
				+", CMHEIT="+mob.baseEnvStats().height()
				+", CMWEIT="+mob.baseEnvStats().weight()
				+", CMPRPT='"+pstats.getPrompt()+"'"
				+", CMCOLR='"+pstats.getColorStr()+"'"
				+", CMLSIP='"+pstats.lastIP()+"'"
				+", CMPFIL='"+pfxml.toString()+"'"
				+", CMMXML='"+cleanXML.toString()+"'" */
				+"'  WHERE CMUSERID='"+mob.name()+"'");
//		DB.update("UPDATE CMCHAR SET CMDESC='"+mob.description()+"' WHERE CMUSERID='"+mob.name()+"'");
	}


	public void DBDelete(MOB mob)
	{
		if(mob.name().length()==0) return;
		Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.PLAYERPURGES);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel((String)channels.elementAt(i),mob.name()+" has just been deleted.",true);
		DB.update("DELETE FROM CMCHAR WHERE CMUSERID='"+mob.name()+"'");
/*		while(mob.numItems()>0)
		{
			Item thisItem=mob.getItem(0);
			if(thisItem!=null)
			{
				thisItem.setContainer(null);
				mob.removeItem(thisItem);
			}
		}
		CMLib.database().DBDeletePlayerData(mob.name()); */
		PlayerStats pstats = mob.playerStats();
		if(pstats!=null)
		{
			PlayerAccount account = pstats.getAccount();
			if(account != null)
			{
				account.delPlayer(mob);
				account.setLastUpdated(System.currentTimeMillis());
				DBUpdateAccount(account);
			}
		}
	}

	//TODO: Fix stuff when save options are finalized
	public void DBCreateCharacter(MOB mob)
	{
		if(mob.name().length()==0) return;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		DB.update("INSERT INTO CMCHAR (CMUSERID, CMDATA ) VALUES ('"
				+mob.name()+"','"+CMLib.coffeeMaker().getPropertiesStr(mob)+"')");
		PlayerAccount account = pstats.getAccount();
		if(account != null)
		{
			account.addNewPlayer(mob);
			account.setLastUpdated(System.currentTimeMillis());
			DBUpdateAccount(account);
		}
	}

	public void DBUpdateAccount(PlayerAccount account)
	{
		if(account == null) return;
		DB.update("UPDATE CMACCT SET CMDATA='"+account.accountName()+"','"+CMLib.coffeeMaker().getPropertiesStr(account)+"',  WHERE CMANAM='"+account.accountName()+"'");
	}

	public void DBDeleteAccount(PlayerAccount account)
	{
		if(account == null) return;
		DB.update("DELETE FROM CMACCT WHERE CMANAM='"+account.accountName()+"'");
	}

	public void DBCreateAccount(PlayerAccount account)
	{
		if(account == null) return;
		account.setAccountName(CMStrings.capitalizeAndLower(account.accountName()));
		DB.update("INSERT INTO CMACCT (CMANAM, CMDATA) VALUES ('"+account.accountName()+"','"+CMLib.coffeeMaker().getPropertiesStr(account)+"')");
	}
	
	public PlayerAccount MakeAccount(String username, ResultSet R) throws SQLException
	{
		PlayerAccount account = null;
		account = (PlayerAccount)CMClass.Objects.COMMON.getNew("DefaultPlayerAccount");
		CMLib.coffeeMaker().setPropertiesStr(account, DB.getRes(R,"CMDATA"));
/*
		String password=DB.getRes(R,"CMPASS");
		String chrs=DB.getRes(R,"CMCHRS");
		String xml=DB.getRes(R,"CMAXML");
		Vector<String> names = new Vector<String>();
		if(chrs!=null) names.addAll(CMParms.parseSemicolons(chrs,true));
		account.setAccountName(CMStrings.capitalizeAndLower(username));
		account.setPassword(password);
		account.setPlayerNames(names);
		account.setXML(xml); */
		return account;
	}

	public PlayerAccount DBReadAccount(String Login)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb 
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMACCT WHERE CMANAM='"+CMStrings.capitalizeAndLower(Login)+"'");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMANAM");
				if(Login.equalsIgnoreCase(username))
					account = MakeAccount(username,R);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
		return account;
	}
	
	public Vector<PlayerAccount> DBListAccounts(String mask)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		Vector<PlayerAccount> accounts = new Vector<PlayerAccount>();
		if(mask!=null) mask=mask.toLowerCase();
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMACCT");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMANAM");
				if((mask==null)||(mask.length()==0)||(username.toLowerCase().indexOf(mask)>=0))
				{
					account = MakeAccount(username,R);
					accounts.add(account);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
		return accounts;
	}
/*
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login)
	{
		DBConnection D=null;
		String buf=null;
		PlayerLibrary.ThinnerPlayer thinPlayer = null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+CMStrings.capitalizeAndLower(Login)+"'");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMUSERID");
				thinPlayer = new PlayerLibrary.ThinnerPlayer();
				String password=DB.getRes(R,"CMPASS");
				thinPlayer.name=username;
				thinPlayer.password=password;
//				buf=DBConnections.getRes(R,"CMPFIL");
			}
		}catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
//		if((buf!=null)&&(thinPlayer!=null))
//			thinPlayer.accountName = CMLib.xml().returnXMLValue(buf,"ACCOUNT");
		return thinPlayer;
	}
	private Vector getDBItemUpdateStrings(MOB mob)
	{
		HashSet done=new HashSet();
		Vector strings=new Vector();
		for(int i=0;i<mob.numItems();i++)
		{
			Item thisItem=mob.getItem(i);
			if((thisItem!=null)&&(!done.contains(""+thisItem))&&(thisItem.savable()))
			{
				CMLib.catalog().updateCatalogIntegrity(thisItem);
				String str="INSERT INTO CMCHIT (CMUSERID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
				+"CMITUR, CMITLV, CMITAB, CMHEIT"
				+") values ('"+mob.name()+"','"+(thisItem)+"','"+thisItem.ID()+"','"+thisItem.text()+" ','"
				+((thisItem.container()!=null)?(""+thisItem.container()):"")+"',"+thisItem.rawWornCode()+","
				+thisItem.usesRemaining()+","+thisItem.baseEnvStats().level()+","+thisItem.baseEnvStats().ability()+","
				+thisItem.baseEnvStats().height()+")";
				strings.addElement(str);
				done.add(""+thisItem);
			}
		}
		return strings;
	}

	public void DBUpdateItems(MOB mob)
	{
		if(mob.name().length()==0) return;
		Vector statements=new Vector();
		statements.addElement("DELETE FROM CMCHIT WHERE CMUSERID='"+mob.name()+"'");
		statements.addAll(getDBItemUpdateStrings(mob));
		DB.update(CMParms.toStringArray(statements));
	}

	public void DBUpdatePassword(String name, String password)
	{
		name=CMStrings.capitalizeAndLower(name);
		DB.update("UPDATE CMCHAR SET CMPASS='"+password+"' WHERE CMUSERID='"+name+"'");
	}
	private String getPlayerStatsXML(MOB mob)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return "";
		StringBuffer pfxml=new StringBuffer(pstats.getXML());
		pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));
		return pfxml.toString();
	}
	public void DBUpdateJustPlayerStats(MOB mob)
	{
		if(mob.name().length()==0)
			return;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		String pfxml=getPlayerStatsXML(mob);
		DB.update("UPDATE CMCHAR SET CMPFIL='"+pfxml.toString()+"' WHERE CMUSERID='"+mob.name()+"'");
	}

	protected PlayerLibrary.ThinPlayer parseThinUser(ResultSet R)
	{
		try
		{
			PlayerLibrary.ThinPlayer thisUser=new PlayerLibrary.ThinPlayer();
			thisUser.name=DBConnections.getRes(R,"CMUSERID");
			String rrace=DBConnections.getRes(R,"CMRACE");
			Race R2=CMClass.getRace(rrace);
			if(R2!=null)
				thisUser.race=(R2.name());
			else
				thisUser.race=rrace;
			thisUser.age=(int)DBConnections.getLongRes(R,"CMAGEH");
			MOB M=CMLib.players().getPlayer((String)thisUser.name);
			if((M!=null)&&(M.lastTickedDateTime()>0))
				thisUser.last=M.lastTickedDateTime();
			else
				thisUser.last=DBConnections.getLongRes(R,"CMDATE");
			String lsIP=DBConnections.getRes(R,"CMLSIP");
			thisUser.ip=lsIP;
			return thisUser;
		}catch(Exception e)
		{
			Log.errOut("MOBloader",e);
		}
		return null;
	}
	
	public PlayerLibrary.ThinPlayer getThinUser(String name)
	{
		DBConnection D=null;
		PlayerLibrary.ThinPlayer thisUser=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R!=null) while(R.next())
				thisUser=parseThinUser(R);
		}catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
		return thisUser;
	}
	
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{
		DBConnection D=null;
		Vector allUsers=new Vector();
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				PlayerLibrary.ThinPlayer thisUser=parseThinUser(R);
				if(thisUser != null)
					allUsers.addElement(thisUser);
			}
		}catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
		return allUsers;
	}

	public void vassals(MOB mob, String liegeID)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMLEIG='"+liegeID+"'");
			StringBuffer head=new StringBuffer("");
			head.append("[");
			head.append(CMStrings.padRight("Race",8)+" ");
			head.append(CMStrings.padRight("Class",10)+" ");
			head.append(CMStrings.padRight("Lvl",4)+" ");
			head.append(CMStrings.padRight("Exp/Lvl",17));
			head.append("] Character name\n\r");
			HashSet done=new HashSet();
			if(R!=null) while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				MOB M=CMLib.players().getPlayer(username);
				if(M==null)
				{
					done.add(username);
					String race=(CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
					head.append("[");
					head.append(CMStrings.padRight(race,18)+" ");
					head.append("] "+CMStrings.padRight(username,15));
					head.append("\n\r");
				}
			}
			for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
				if((M.getLiegeID().equals(liegeID))&&(!done.contains(M.name())))
				{
					head.append("[");
					head.append(CMStrings.padRight(M.charStats().getMyRace().name(),8)+" ");
					head.append(CMStrings.padRight(""+M.envStats().level(),4)+" ");
					head.append("] "+CMStrings.padRight(M.name(),15));
					head.append("\n\r");
				}
			}
			mob.tell(head.toString());
		}catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DB.DBDone(D);
	}
*/
}
