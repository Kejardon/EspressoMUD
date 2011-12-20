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

import java.util.*;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Modify extends StdCommand
{
	public Modify(){}

	private String[] access={"MODIFY","MOD"};
	public String[] getAccessWords(){return access;}

	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		Room R=null;
		if(commands.size()>2)
		{
			String restStr=CMParms.combine(commands,2);
			R=CMLib.map().getRoom(restStr);
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

	public void accounts(MOB mob, Vector commands)
		throws IOException
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
			theAccount = CMLib.players().getLoadAccount(accountName);
			if(theAccount==null)
			{
				mob.tell("There is no account called '"+accountName+"'!\n\r");
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

	public void areas(MOB mob, Vector commands)
		throws IOException
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
			if(mob.session().confirm("Is changing the name of this area really necessary (y/N)?","N"))
			{
				int oldLength=oldName.length()+1;
				for(Enumeration<Room> r=myArea.getProperMap();r.hasMoreElements();)
				{
					Room R=r.nextElement();
					R.setRoomID(myArea.name()+"#"+R.roomID().substring(oldLength));
				}
			}
			else
				myArea.setName(oldName);
		}
/*		if(!myArea.name().equals(oldName))
			CMLib.map().renameRooms(myArea,oldName,allMyDamnRooms);
		}
		Log.sysOut("Rooms",mob.Name()+" modified area "+myArea.Name()+".");
*/	}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("This command also needs an exit to modify.\n\r");
			return;
		}
		String exitName=CMParms.combine(commands,2);

		Exit exit=mob.location().getExit(exitName);
		if(exit==null)
			exit=CMLib.map().getExit(exitName);
		if(exit==null)
		{
			mob.tell("No exit called '"+exitName+"' was found.");
			return;
		}
		CMLib.genEd().genMiscSet(mob,exit);
		return;
	}

	public void players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY USER [PLAYER NAME]\n\r");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.players().getLoadPlayer(mobID);
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
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			rooms(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.Ints.COMMONACCOUNTSYSTEM.property()>1))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			accounts(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			areas(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
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
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) 
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
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) 
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
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
					players(mob,CMParms.parse("MODIFY USER \""+target.name()+"\""));
				}
			}
			else
			if(target instanceof Exit)
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
//				Exit copyExit=(Exit)thang.copyOf();
				CMLib.genEd().genMiscSet(mob,(Exit)target);
//				if(!copyExit.sameAs(thang))
//					Log.sysOut("CreateEdit",mob.Name()+" modified exit "+thang.ID()+".");
//				copyExit.destroy();
			}
			else
				mob.tell("\n\rYou cannot modify a '"+commandType+"'. However, you might try an ITEM, AREA, EXIT, MOB, USER, ACCOUNT, or ROOM.");
		}
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}
}