package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Destroy extends StdCommand
{
	public Destroy(){access=new String[]{"DESTROY"};}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean mobs(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is DESTROY MOB [MOB NAME]\r\n");
			return false;
		}

		String mobID=CMParms.combine(commands,2);
		boolean allFlag=commands.elementAt(2).equalsIgnoreCase("all");
		if(mobID.toUpperCase().startsWith("ALL.")){ allFlag=true; mobID="ALL "+mobID.substring(4);}
		if(mobID.toUpperCase().endsWith(".ALL")){ allFlag=true; mobID="ALL "+mobID.substring(0,mobID.length()-4);}
		Vector<MOB> V=mob.location().fetchInhabitants(mobID);
		boolean doneSomething=false;
		while(V.size()>0)
		{
			MOB deadMOB=V.remove(0);
			if(!deadMOB.isMonster())
			{
				mob.tell(deadMOB.name()+" is a PLAYER!!\r\n");
				continue;
			}
			doneSomething=true;
			mob.location().show(null,deadMOB.name()+" vanishes in a puff of smoke.");
			Log.sysOut("Mobs",mob.name()+" destroyed mob "+deadMOB.name()+".");
			deadMOB.destroy();
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+mobID+" here.\r\n");
			return false;
		}
		return true;
	}

	public void accounts(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3) 
		{ 
			mob.tell("You have failed to specify the proper fields.\r\nThe format is DESTROY ACCOUNT ([NAME])\r\n");
			return;
		}
		String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
		PlayerAccount theAccount = CMLib.players().getAccount(accountName);
		if(theAccount==null)
		{
			mob.tell("There is no account called '"+accountName+"'!\r\n");
			return;
		}
		ArrayList<MOB> playerList = CMParms.toArrayList(theAccount.getLoadPlayers());
		String playerListString = CMParms.toInteractableStringList(playerList.iterator());
//		if(mob.session().confirm("This will complete OBLITERATE the account '"+theAccount.accountName()+"' and players '"+playerList+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			for(MOB deadMOB : playerList)
			{
				CMLib.players().obliteratePlayer(deadMOB,false);
				mob.tell("The user '"+CMParms.combine(commands,2)+"' is no more!\r\n");
				Log.sysOut("Mobs",mob.name()+" destroyed user "+deadMOB.name()+".");
				deadMOB.destroy();
			}
			CMLib.players().obliterateAccountOnly(theAccount);
			mob.location().recoverRoomStats();
			Log.sysOut("Destroy",mob.name()+" destroyed account "+theAccount.accountName()+" and players '"+playerListString+"'.");
		}
	}

	public static boolean players(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is DESTROY USER [USER NAME]\r\n");
			return false;
		}

		String name=CMStrings.capitalizeAndLower(CMParms.combine(commands,2));
		boolean found=CMLib.players().playerExists(name);

		if(!found)
		{
			mob.tell("The user '"+CMParms.combine(commands,2)+"' does not exist!\r\n");
			return false;
		}

		//if(mob.session().confirm("This will complete OBLITERATE the user '"+name+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			MOB deadMOB=CMLib.players().getPlayer(name);
			CMLib.players().obliteratePlayer(deadMOB,false);
			mob.tell("The user '"+CMParms.combine(commands,2)+"' is no more!\r\n");
			Log.sysOut("Mobs",mob.name()+" destroyed user "+deadMOB.name()+".");
			deadMOB.destroy();
			return true;
		}
		//return true;
	}

	public Thread findThreadGroup(String threadName,ThreadGroup tGroup)
	{
		int ac = tGroup.activeCount();
		int agc = tGroup.activeGroupCount();
		Thread tArray[] = new Thread [ac+1];
		ThreadGroup tgArray[] = new ThreadGroup [agc+1];

		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);

		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null)
			{
				if(tArray[i] instanceof TickableGroup)
//				&&(((TickableGroup)tArray[i]).lastTicked()!=null)
//				&&(((TickableGroup)tArray[i]).lastTicked().getTickStatus()==Tickable.TickStat.Not))
					continue;
				if((tArray[i] instanceof Tickable)
				&&(((Tickable)tArray[i]).getTickStatus()==Tickable.TickStat.Not))
					continue;
				if(tArray[i].getName().equalsIgnoreCase(threadName))
					return tArray[i];
			}
		}

		if (agc > 0)
		{
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
				{
					Thread t=findThreadGroup(threadName,tgArray[i]);
					if(t!=null) return t;
				}
			}
		}
		return null;
	}

	public Thread findThread(String threadName)
	{
		Thread t=null;
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				t=findThreadGroup(threadName,topTG);
		}
		catch (Exception e){}
		return t;
	}

	public void rooms(MOB mob, Vector<String> commands)
	{
		String thecmd=(commands.elementAt(0)).toLowerCase();
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is DESTROY ROOM [ROOM ID]\r\n");
			return;
		}
		String roomName=CMParms.combine(commands,2);
		Room deadRoom=null;
		if(roomName.equalsIgnoreCase("HERE"))
			deadRoom=mob.location();
		else
			deadRoom=SIDLib.ROOM.get(CMath.s_int(roomName));
		if(deadRoom==null)
		{
			mob.tell("You have failed to specify a room.  Try a VALID ROOM ID, or \"HERE\".\r\n");
			return;
		}
		if(!CMSecurity.isAllowed(mob,"CMDROOMS"))
		{
			mob.tell("Sorry Charlie! Not your room!");
			return;
		}

//		if(!mob.session().confirm("You are fixing to permanantly destroy Room \""+deadRoom.saveNum()+"\".  Are you ABSOLUTELY SURE (y/N)","N"))
//			return;
		CMLib.map().obliterateRoom(deadRoom);
		mob.tell("The room has been destroyed.");
		Log.sysOut("Rooms",mob.name()+" destroyed room "+deadRoom.saveNum()+".");
	}

	public void exits(MOB mob, Vector<String> commands, boolean bothSides)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is DESTROY EXIT(S) [EXIT NAME]");
			return;
		}

		Room.REMap target=mob.location().getREMap(commands.elementAt(2));
		if(target==null)
		{
			mob.tell("No exit with that name was found here.\r\n");
			return;
		}
		if(bothSides)
		{
			target.room.removeExit(target.exit, mob.location());
			Log.sysOut("Exits",mob.location().saveNum()+" and "+target.room.saveNum()+" exit, "+target.exit.saveNum()+" destroyed by "+mob.name()+".");
		}
		else
			Log.sysOut("Exits",mob.location().saveNum()+" exit "+target.exit.saveNum()+" unlinked by "+mob.name()+".");
		mob.location().removeExit(target);
		mob.location().show(null,"A wall of inhibition covers "+target.exit.directLook(mob, target.room)+".");
		
	}
	public void lock(MOB mob, Vector<String> commands)
	{
		Room R;
		if(commands.size()<3)
			R=mob.location();
		else
		{
			String roomName=CMParms.combine(commands,2);
			if(roomName.equalsIgnoreCase("HERE"))
				R=mob.location();
			else
				R=SIDLib.ROOM.get(CMath.s_int(roomName));
			if(R==null)
			{
				mob.tell("You have failed to specify a room.  Try a VALID ROOM ID, or \"HERE\".\r\n");
				return;
			}
		}

		mob.tell("Room lock cleared. It was previously "+R.undoLock());
	}

	public boolean items(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is DESTROY ITEM [ITEM NAME](@[MOB NAME])\r\n");
			return false;
		}
		
		String itemID=CMParms.combine(commands,2);
		MOB srchMob=mob;
		Room srchRoom=mob.location();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.length()>0)
			{
				Vector<MOB> V=srchRoom.fetchInhabitants(rest);
				if(V.size()==0)
				{
					mob.tell("MOB '"+rest+"' not found.");
					return false;
				}
				else
				{
					srchMob=V.get(0);
					srchRoom=null;
				}
			}
		}
		
		boolean allFlag=commands.elementAt(2).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL.")){ allFlag=true; itemID="ALL "+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(".ALL")){ allFlag=true; itemID="ALL "+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Vector<Item> V=(srchRoom==null)?null:srchRoom.fetchItems(itemID);
		if(V.size()==0) V=(srchMob==null)?null:srchMob.fetchInventories(itemID);
		while(V.size()>0)
		{
			Item deadItem=V.remove(0);
			if((deadItem instanceof Body)&&(((Body)deadItem).mob()!=null)) continue;
			mob.location().show(mob,deadItem.name()+" disintegrates!");
			doneSomething=true;
			Log.sysOut("Items",mob.name()+" destroyed item "+deadItem.name()+".");
			deadItem.destroy();
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+itemID+"' here.\r\n");
			return false;
		}
		return true;
	}


	public void areas(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is DESTROY AREA (HERE,[AREA NAME])\r\n");
			return;
		}

		String areaName=CMParms.combine(commands,2);
		Area A=CMLib.map().getArea(areaName);
		if((A==null)&&(areaName.equals("HERE")))
			A=mob.location().getArea();
		if(A==null)
		{
			mob.tell("There is no such area as '"+areaName+"'");
			return;
		}

		Room R=A.getRandomProperRoom();
		if((R!=null)&&(!CMSecurity.isAllowed(mob,"CMDAREAS")))
		{
			errorOut(mob);
			return;
		}
			
//		if(mob.session().confirm("Area: \""+areaName+"\", OBLITERATE IT???","N"))
		{
			CMLib.map().obliterateArea(areaName);
			Log.sysOut("Rooms",mob.name()+" destroyed area "+areaName+".");
		}
	}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String commandType=null;

		if(commands.size()>1)
		{
			commandType=commands.elementAt(1).toUpperCase();
		}
		if(commandType.equals("EXIT")||commandType.equals("EXITS"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDEXITS")) return errorOut(mob);
			exits(mob,commands,commandType.equals("EXIT"));
		}
		else if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDITEMS")) return errorOut(mob);
			items(mob,commands);
		}
		else if(commandType.equals("AREA"))
		{
			areas(mob,commands);
		}
		else if(commandType.equals("ROOM"))
		{
			rooms(mob,commands);
		}
		else if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDPLAYERS")) return errorOut(mob);
			players(mob,commands);
		}
		else if((commandType.equals("ACCOUNT"))&&(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1))
		{
			if(!CMSecurity.isAllowed(mob,"CMDPLAYERS")) return errorOut(mob);
			accounts(mob,commands);
		}
		else if(commandType.equals("BAN"))
		{
			//TODO: This is terrible interface for removing bans
			if(!CMSecurity.isAllowed(mob,"BAN")) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int(commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid ban number to delete.  Use List Banned for more information.");
			else
			{
				CMSecurity.unban(which);
				mob.tell("Ok.");
			}
		}
		else if(commandType.equals("THREAD"))
		{
			if(!CMSecurity.isASysOp(mob)) return errorOut(mob);
			String which=CMParms.combine(commands,2);
			Thread whichT=null;
			if(which.length()>0)
				whichT=findThread(which);
			if(whichT==null)
				mob.tell("Please enter a valid thread name to destroy.  Use List threads for a list.");
			else
			{
				CMLib.killThread(whichT,500,1);
				Log.sysOut("CreateEdit",mob.name()+" destroyed thread "+whichT.getName()+".");
				mob.tell("Stop sent to: "+whichT.getName()+".");
			}
		}
		else if(commandType.startsWith("SESSION"))
		{
			if(!CMSecurity.isAllowed(mob,"BOOT")) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int((String)commands.elementAt(2));
			if((which<0)||(which>=CMLib.sessions().size()))
				mob.tell("Please enter a valid session number to delete.  Use SESSIONS for more information.");
			else
			{
				Session S=CMLib.sessions().elementAt(which);
				CMLib.sessions().stopSessionAtAllCosts(S);
				if(S.getStatus()==Session.STATUS_LOGOUTFINAL)
					mob.tell("Ok.");
				else
					mob.tell("Failed to gracefully shutdown: "+Session.STATUS_STR[S.getStatus()]+", but a forcable stop was issued.");
			}
		}
		else if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDMOBS")) return errorOut(mob);
			mobs(mob,commands);
		}
		else if(commandType.equals("LOCK"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDROOMS")) return errorOut(mob);
			lock(mob,commands);
		}
		else
		{
			mob.tell("\r\nYou cannot destroy a '"+commandType+
			"'. However, you might try an EXIT, ITEM, AREA, USER, MOB, SESSION, TICKS, THREAD, BAN, LOCK or a ROOM.");
		}
		return false;
	}
	public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,"CMD");}
}