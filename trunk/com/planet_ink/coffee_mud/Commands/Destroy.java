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
	public Destroy(){}

	private String[] access={"DESTROY"};
	public String[] getAccessWords(){return access;}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean mobs(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY MOB [MOB NAME]\n\r");
			return false;
		}

		String mobID=CMParms.combine(commands,2);
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(mobID.toUpperCase().startsWith("ALL.")){ allFlag=true; mobID="ALL "+mobID.substring(4);}
		if(mobID.toUpperCase().endsWith(".ALL")){ allFlag=true; mobID="ALL "+mobID.substring(0,mobID.length()-4);}
		Vector<MOB> V=mob.location().fetchInhabitants(mobID);
		boolean doneSomething=false;
		while(V.size()>0)
		{
			MOB deadMOB=V.remove(0);
			if(!deadMOB.isMonster())
			{
				mob.tell(deadMOB.name()+" is a PLAYER!!\n\r");
				continue;
			}
			doneSomething=true;
			mob.location().showHappens(EnumSet.of(CMMsg.MsgCode.VISUAL),null,deadMOB.name()+" vanishes in a puff of smoke.");
			Log.sysOut("Mobs",mob.name()+" destroyed mob "+deadMOB.name()+".");
			deadMOB.destroy();
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			return false;
		}
		return true;
	}

	public void accounts(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3) 
		{ 
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ACCOUNT ([NAME])\n\r");
			return;
		}
		String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
		PlayerAccount theAccount = CMLib.players().getLoadAccount(accountName);
		if(theAccount==null)
		{
			mob.tell("There is no account called '"+accountName+"'!\n\r");
			return;
		}
		String playerList = CMParms.toInteractableStringList(theAccount.getLoadPlayers());
		if(mob.session().confirm("This will complete OBLITERATE the account '"+theAccount.accountName()+"' and players '"+playerList+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			for(Enumeration<MOB> p=theAccount.getLoadPlayers();p.hasMoreElements();)
			{
				MOB deadMOB=p.nextElement();
				CMLib.players().obliteratePlayer(deadMOB,false);
				mob.tell("The user '"+CMParms.combine(commands,2)+"' is no more!\n\r");
				Log.sysOut("Mobs",mob.name()+" destroyed user "+deadMOB.name()+".");
				deadMOB.destroy();
			}
			CMLib.players().obliterateAccountOnly(theAccount);
			mob.location().recoverRoomStats();
			Log.sysOut("Destroy",mob.name()+" destroyed account "+theAccount.accountName()+" and players '"+playerList+"'.");
		}
	}

	public static boolean players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY USER [USER NAME]\n\r");
			return false;
		}

		String name=CMStrings.capitalizeAndLower(CMParms.combine(commands,2));
		boolean found=CMLib.players().playerExists(name);

		if(!found)
		{
			mob.tell("The user '"+CMParms.combine(commands,2)+"' does not exist!\n\r");
			return false;
		}

		if(mob.session().confirm("This will complete OBLITERATE the user '"+name+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			MOB deadMOB=CMLib.players().getLoadPlayer(name);
			CMLib.players().obliteratePlayer(deadMOB,false);
			mob.tell("The user '"+CMParms.combine(commands,2)+"' is no more!\n\r");
			Log.sysOut("Mobs",mob.name()+" destroyed user "+deadMOB.name()+".");
			deadMOB.destroy();
			return true;
		}
		return true;
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

	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		String thecmd=((String)commands.elementAt(0)).toLowerCase();
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ROOM [ROOM ID]\n\r");
			return;
		}
		String roomName=CMParms.combine(commands,2);
		Room deadRoom=null;
		if(roomName.equalsIgnoreCase("HERE"))
			deadRoom=mob.location();
		else
			deadRoom=CMLib.map().getRoom(roomName);
		if(deadRoom==null)
		{
			mob.tell("You have failed to specify a room.  Try a VALID ROOM ID, or \"HERE\".\n\r");
			return;
		}
		if(!CMSecurity.isAllowed(mob,deadRoom,"CMDROOMS"))
		{
			mob.tell("Sorry Charlie! Not your room!");
			return;
		}

		if(!mob.session().confirm("You are fixing to permanantly destroy Room \""+deadRoom.roomID()+"\".  Are you ABSOLUTELY SURE (y/N)","N"))
			return;
		CMLib.map().obliterateRoom(deadRoom);
		mob.tell("The room has been destroyed.");
		Log.sysOut("Rooms",mob.name()+" destroyed room "+deadRoom.roomID()+".");
	}

	public void exits(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY EXIT [EXIT NAME]");
			return;
		}

		Room.REMap target=mob.location().getREMap((String)commands.elementAt(2));
		if(target==null)
		{
			mob.tell("No exit with that name was found here.\n\r");
			return;
		}
		if(mob.session().confirm("Remove this exit from the opposite room too?",""))
		{
			target.room.removeExit(target.exit, mob.location());
			Log.sysOut("Exits",mob.location().roomID()+" and "+target.room.roomID()+" exit, "+target.exit.exitID()+" destroyed by "+mob.name()+".");
		}
		else
			Log.sysOut("Exits",mob.location().roomID()+" exit "+target.exit.exitID()+" unlinked by "+mob.name()+".");
		mob.location().removeExit(target);
		mob.location().showHappens(EnumSet.of(CMMsg.MsgCode.VISUAL),null,"A wall of inhibition covers "+target.exit.directLook(mob, target.room)+".");
		
	}

	public boolean items(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ITEM [ITEM NAME](@[MOB NAME])\n\r");
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
		
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL.")){ allFlag=true; itemID="ALL "+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(".ALL")){ allFlag=true; itemID="ALL "+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Item deadItem=null;
		Vector<Item> V=(srchRoom==null)?null:srchRoom.fetchItems(itemID);
		if(V.size()==0) V=(srchMob==null)?null:srchMob.fetchInventories(itemID);
		while(V.size()>0)
		{
			deadItem=V.remove(0);
			mob.location().show(mob,null,null,EnumSet.of(CMMsg.MsgCode.VISUAL),deadItem.name()+" disintegrates!");
			doneSomething=true;
			Log.sysOut("Items",mob.name()+" destroyed item "+deadItem.name()+".");
			deadItem.destroy();
			deadItem=null;
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			return false;
		}
		return true;
	}


	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY AREA (HERE,[AREA NAME])\n\r");
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
		if((R!=null)&&(!CMSecurity.isAllowed(mob,R,"CMDAREAS")))
		{
			errorOut(mob);
			return;
		}
			
		if(mob.session().confirm("Area: \""+areaName+"\", OBLITERATE IT???","N"))
		{
			CMLib.map().obliterateArea(areaName);
			Log.sysOut("Rooms",mob.name()+" destroyed area "+areaName+".");
		}
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";

		if(commands.size()>1)
		{
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			items(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			areas(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			rooms(mob,commands);
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			players(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			accounts(mob,commands);
		}
		else
		if(commandType.equals("BAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"BAN")) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid ban number to delete.  Use List Banned for more information.");
			else
			{
				CMSecurity.unban(which);
				mob.tell("Ok.");
			}
		}
		else
		if(commandType.equals("THREAD"))
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
		else
		if(commandType.startsWith("SESSION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"BOOT")) return errorOut(mob);
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
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mobs(mob,commands);
		}
		else
		{
			mob.tell("\n\rYou cannot destroy a '"+commandType+
			"'. However, you might try an EXIT, ITEM, AREA, USER, MOB, SESSION, TICKS, THREAD, BAN, or a ROOM.");
		}
		return false;
	}
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}
}