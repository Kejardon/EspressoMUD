package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Modify extends StdCommand
{
	public Modify(){access=new String[]{"MODIFY","MOD"};}

	public void rooms(MOB mob, Vector<String> commands)
	{
		Room R=null;
		if(commands.size()>2)
		{
			String restStr=CMParms.combine(commands,2);
			R=SIDLib.ROOM.get(CMath.s_int(restStr));
			if(R==null)
			{
				mob.tell("Room '"+restStr+"' not found.");
				return;
			}
		}
		else R=mob.location();

//		Room oldRoom=(Room)R.copyOf();
		CMLib.genEd().genMiscSet(mob,R);
//		if((!oldRoom.sameAs(R))&&(!R.amDestroyed()))
//			Log.sysOut("Rooms",mob.Name()+" modified room "+R.roomID()+".");
//		oldRoom.destroy();
		return;
	}

	public void accounts(MOB mob, Vector<String> commands)
	{
		PlayerAccount theAccount = null;
		String oldName = null;
		if(commands.size()<3)
		{
			theAccount=mob.playerStats().getAccount();
			oldName=theAccount.accountName();
		}
		else
		{
			String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
			theAccount = CMLib.players().getAccount(accountName);
			if(theAccount==null)
			{
				mob.tell("There is no account called '"+accountName+"'!\r\n");
				return;
			}
			oldName=theAccount.accountName();
		}
		CMLib.genEd().genMiscSet(mob,theAccount);
/*		Log.sysOut("Modify",mob.Name()+" modified account "+theAccount.accountName()+".");
		if(!oldName.equals(theAccount.accountName()))
		{
			Vector<MOB> V=new Vector<MOB>();
			for(Enumeration<String> es=theAccount.getPlayers();es.hasMoreElements();)
			{
				String playerName=es.nextElement();
				MOB playerM=CMLib.players().getLoadPlayer(playerName);
				if((playerM!=null)&&(!CMLib.flags().isInTheGame(playerM,true)))
					V.addElement(playerM);
			}
			PlayerAccount acc = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
			acc.setAccountName(oldName);
			CMLib.database().DBDeleteAccount(acc);
			CMLib.database().DBCreateAccount(theAccount);
			for(MOB playerM : V)
				CMLib.database().DBUpdatePlayerPlayerStats(playerM);
		}
		CMLib.database().DBUpdateAccount(theAccount);
*/	}

	public void areas(MOB mob, Vector<String> commands)
	{
		Area myArea=null;

//		Vector allMyDamnRooms=new Vector();
//		for(Enumeration e=myArea.getCompleteMap();e.hasMoreElements();)
//			allMyDamnRooms.addElement(e.nextElement());

//		Resources.removeResource("HELP_"+myArea.name().toUpperCase());
		if(commands.size()<3)
			myArea=mob.location().getArea();
		else
		{
			String restStr=CMParms.combine(commands,2);
			myArea=CMLib.map().getArea(restStr);
		}
		String oldName=myArea.name();
		CMLib.genEd().genMiscSet(mob,myArea);

		if(!myArea.name().equals(oldName))
		{
			String newName=myArea.name();
			myArea.setName(oldName);
			CMLib.map().delArea(myArea);
			myArea.setName(newName);
			CMLib.map().addArea(myArea);
		}
/*		if(!myArea.name().equals(oldName))
			CMLib.map().renameRooms(myArea,oldName,allMyDamnRooms);
		}
		Log.sysOut("Rooms",mob.Name()+" modified area "+myArea.Name()+".");
*/	}

	public void exits(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("This command also needs an exit to modify.\r\n");
			return;
		}
		String exitName=CMParms.combine(commands,2);

		Exit exit=mob.location().getExit(exitName);
		if(exit==null)
			exit=SIDLib.EXIT.get(CMath.s_int(exitName));
		if(exit==null)
		{
			mob.tell("No exit called '"+exitName+"' was found.");
			return;
		}
		CMLib.genEd().genMiscSet(mob,exit);
		return;
	}

	public void players(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is MODIFY USER [PLAYER NAME]\r\n");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.players().getPlayer(mobID);
		if(M==null)
		{
			mob.tell("There is no such player as '"+mobID+"'!");
			return;
		}
//		MOB copyMOB=(MOB)M.copyOf();
		CMLib.genEd().genMiscSet(mob,M);
//		if(!copyMOB.sameAs(M))
//			Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+".");
//		copyMOB.setSession(null); // prevents logoffs.
//		copyMOB.setLocation(null);
//		copyMOB.destroy();
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDROOMS")) return errorOut(mob);
			rooms(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1))
		{
			if(!CMSecurity.isAllowed(mob,"CMDPLAYERS")) return errorOut(mob);
			accounts(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDAREAS")) return errorOut(mob);
			areas(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDEXITS")) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDPLAYERS")) return errorOut(mob);
			players(mob,commands);
		}
		else
		{
			commands.removeElementAt(0);
			String whatToModify=null;
			String inWhat=null;
			Interactable target=null;
			int partition=CMLib.english().getPartitionIndex(commands, "@");
			if(partition==-1)
			{
				whatToModify=CMParms.combine(commands,0);
				target=CMLib.english().fetchInteractable(whatToModify,false,1,mob.getItemCollection(),mob.location().getItemCollection());
			}
			else
			{
				whatToModify=CMParms.combine(commands,0,partition);
				inWhat=CMParms.combine(commands,partition);
				Interactable container=CMLib.english().fetchInteractable(inWhat,false,1,mob.getItemCollection(),mob.location().getItemCollection());
				if(container==null)
				{
					mob.tell("You don't see '"+inWhat+"' here.");
					return false;
				}
				ItemCollection col=ItemCollection.O.getFrom(container);
				if(col==null)
				{
					mob.tell("That does not hold anything.");
					return false;
				}
				target=CMLib.english().fetchInteractable(whatToModify,false,1,col);
			}
			if(target==null)
			{
				mob.tell("You don't see '"+whatToModify+"' here.");
				return false;
			}
			if(target instanceof Item)
			{
				if(!CMSecurity.isAllowed(mob,"CMDITEMS")) 
					return errorOut(mob);
//				Item copyItem=(Item)thang.copyOf();
				CMLib.genEd().genMiscSet(mob,(Item)target);
//				if(!copyItem.sameAs(thang))
//					Log.sysOut("CreateEdit",mob.Name()+" modified item "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(mob.location())+".");
//				copyItem.destroy();
			}
			else
			if(target instanceof MOB)
			{
				if(!CMSecurity.isAllowed(mob,"CMDMOBS")) 
					return errorOut(mob);
				if(((MOB)target).isMonster())
				{
//					MOB copyMOB=(MOB)thang.copyOf();
					CMLib.genEd().genMiscSet(mob,(MOB)target);
//					if(!copyMOB.sameAs(thang))
//						Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
//					copyMOB.destroy();
				}
				else
				{
					if(!CMSecurity.isAllowed(mob,"CMDPLAYERS")) return errorOut(mob);
					players(mob,CMParms.parse("MODIFY USER \""+target.name()+"\""));
				}
			}
			else
			if(target instanceof Exit)
			{
				if(!CMSecurity.isAllowed(mob,"CMDEXITS")) return errorOut(mob);
//				Exit copyExit=(Exit)thang.copyOf();
				CMLib.genEd().genMiscSet(mob,(Exit)target);
//				if(!copyExit.sameAs(thang))
//					Log.sysOut("CreateEdit",mob.Name()+" modified exit "+thang.ID()+".");
//				copyExit.destroy();
			}
			else
				mob.tell("\r\nYou cannot modify a '"+commandType+"'. However, you might try an ITEM, AREA, EXIT, MOB, USER, ACCOUNT, or ROOM.");
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public int prompter(){return 1;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,"CMD");}
}