package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//TODO: I want to rework how Copy works eventually and make a paste option as well

public class Copy extends StdCommand
{
	public Copy(){access=new String[]{"COPY"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.removeElementAt(0); // copy
		if(commands.size()<1)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is COPY (NUMBER) ([ITEM NAME]/[MOB NAME][ROOM ID] [DIRECTIONS]/[DIRECTIONS])\r\n");
			return false;
		}
		int number=1;
		if(commands.size()>1)
		{
			number=CMath.s_int((String)commands.firstElement());
			if(number<1)
				number=1;
			else
				commands.removeElementAt(0);
		}
		String name=CMParms.combine(commands,0);
		ItemCollection.ItemHolder dest=mob.location();
		Item srchContainer=null;
		int x=name.indexOf("@");
		if(x>0)
		{
			String rest=name.substring(x+1).trim();
			name=name.substring(0,x).trim();
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				Vector<MOB> V=mob.location().fetchInhabitants(rest);
				if(V.size()!=0)
					dest=V.get(0);
/*				else
				{
					Item I = mob.location().fetchItem(rest);
					if(I instanceof Container)
						srchContainer=(Container)I;
					else
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						return false;
					}
				}
*/
			}
		}
		Interactable E=null;
/*
		int dirCode=Directions.getGoodDirectionCode(name);
		if(dirCode>=0)
			E=mob.location();
		else
		if(commands.size()>1)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
			{
				commands.removeElementAt(commands.size()-1);
				name=CMParms.combine(commands,0);
				E=CMLib.map().getRoom(name);
				if(E==null)
				{
					mob.tell("Room ID '"+name+"' does not exist.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return false;
				}
			}
		}
*/
/*
		if(E==null) E=mob.location().fetchFromRoomFavorItems(srchContainer,name,Wearable.FILTER_UNWORNONLY);
		if(E==null) E=mob.location().fetchFromRoomFavorMOBs(srchContainer,name,Wearable.FILTER_UNWORNONLY);
		if(E==null)	E=mob.fetchInventory(name);
*/
		E=CMLib.english().fetchInteractable(name, false, 2, mob.location());
/*
		if(E==null)
		{
			try
			{
				E=CMLib.map().findFirstInhabitant(mob.location().getArea().getMetroMap(), mob, name, 500);
				if(E==null) 
					E=CMLib.map().findFirstRoomItem(mob.location().getArea().getMetroMap(), mob, name, true, 500);
				if(E==null) 
					E=CMLib.map().findFirstInventory(null, mob, name, 500);
				if(E==null) 
					E=CMLib.map().findFirstInventory(CMLib.map().rooms(), mob, name, 500);
			}catch(NoSuchElementException e){}
		}
*/
		if(E==null)
		{
			mob.tell("There's no such thing as '"+name+"' here.\r\n");
			return false;
		}
		Room room=mob.location();
		for(int i=0;i<number;i++)
		{
			if(E instanceof Body)
			{
				if(!CMSecurity.isAllowed(mob,"COPYMOBS"))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				Body newMOB=(Body)E.copyOf();
				newMOB.mob().setSession(null);
				newMOB.mob().setLocation(room);
				newMOB.bringToLife(room,true);
				if(i==0)
				{
					if(number>1)
						room.show(null,"Suddenly, "+number+" "+newMOB.name()+"s instantiate from the Java plain.");
					else
						room.show(null,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
					Log.sysOut("SysopUtils",mob.name()+" copied "+number+" mob "+newMOB.name()+".");
				}
			}
			else
			if(E instanceof Item)
			{
				if(!CMSecurity.isAllowed(mob,"COPYITEMS"))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				Item newItem=(Item)E.copyOf();
				String end="from the sky";
				if(dest instanceof MOB)
					end="into "+((MOB)dest).name()+"'s arms";
				dest.getItemCollection().addItem(newItem);
				newItem.saveThis();
				if(i==0)
				{
					if(number>1)
					{
						if(newItem.name().toLowerCase().endsWith("s"))
							room.show(null,"Suddenly, "+number+" "+newItem.name()+" falls "+end+".");
						else
							room.show(null,"Suddenly, "+number+" "+newItem.name()+"s falls "+end+".");
					}
					else
						room.show(null,"Suddenly, "+newItem.name()+" fall "+end+".");
					Log.sysOut("SysopUtils",mob.name()+" "+number+" copied "+newItem.ID()+" item.");
				}
			}
/*			else
			if((E instanceof Room)&&(dirCode>=0))
			{
				if(!CMSecurity.isAllowed(mob,"COPYROOMS"))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				if(room.getRoomInDir(dirCode)!=null)
				{
					mob.tell("A room already exists "+Directions.getInDirectionName(dirCode)+"!");
					return false;
				}
				synchronized(("SYNC"+room.roomID()).intern())
				{
					Room newRoom=(Room)E.copyOf();
					for(int d=Directions.NUM_DIRECTIONS-1;d>=0;d--)
					{
						newRoom.rawDoors()[d]=null;
						newRoom.setRawExit(d,null);
					}
					room.rawDoors()[dirCode]=newRoom;
					newRoom.rawDoors()[Directions.getOpDirectionCode(dirCode)]=room;
					if(room.getRawExit(dirCode)==null)
						room.setRawExit(dirCode,CMClass.getExit("Open"));
					newRoom.setRawExit(Directions.getOpDirectionCode(dirCode),(Exit)(room.getRawExit(dirCode).copyOf()));
					newRoom.setRoomID(room.getArea().getNewRoomID(room,dirCode));
					if(newRoom.roomID().length()==0)
					{
						mob.tell("A room may not be created in that direction.  Are you sure you havn't reached the edge of a grid?");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
					newRoom.setArea(room.getArea());
					CMLib.database().DBCreateRoom(newRoom);
					CMLib.database().DBUpdateExits(newRoom);
					CMLib.database().DBUpdateExits(room);
					if(newRoom.numInhabitants()>0)
						CMLib.database().DBUpdateMOBs(newRoom);
					if(newRoom.numItems()>0)
						CMLib.database().DBUpdateItems(newRoom);
					newRoom.getArea().fillInAreaRoom(newRoom);
					if(i==0)
					{
						if(number>1)
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+newRoom.roomTitle(mob)+"s fall "+Directions.getInDirectionName(dirCode)+".");
						else
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newRoom.roomTitle(mob)+" falls "+Directions.getInDirectionName(dirCode)+".");
						Log.sysOut("SysopUtils",mob.Name()+" copied "+number+" rooms "+newRoom.roomID()+".");
					}
					else
						room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newRoom.roomTitle(mob)+" falls "+Directions.getInDirectionName(dirCode)+".");
					room=newRoom;
				}
			}
*/
			else
			{
				mob.tell("I can't just make a copy of a '"+E.name()+"'.\r\n");
				break;
			}
		}
		return false;
	}
	
	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,"COPY");}
}