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

public class Transfer extends At
{
	public Transfer(){access=new String[]{"TRANSFER"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Room room=null;
		if(commands.size()<3)
		{
			mob.tell("Transfer whom/what to where? Try all or an item/mob name, followed by a Room ID, target player name, area name, or room text!");
			return false;
		}
		commands.remove(0);
		boolean mobFlag=false;
		boolean itemFlag=false;
		if(commands.get(0).equalsIgnoreCase("ITEM"))
		{
			itemFlag=true;
			commands.remove(0);
		}
		if(commands.get(0).equalsIgnoreCase("MOB"))
		{
			mobFlag=true;
			commands.remove(0);
		}
		if(commands.size()<2)
		{
			mob.tell("Be more specific, WHAT item/mob do you want to transfer?");
			return false;
		}

		int maxToDo=CMLib.english().calculateMaxToGive(mob,commands,null,true);
		if(maxToDo<0) return false;

		int partition=CMLib.english().getPartitionIndex(commands, "to", 1);
		String mobname=CMParms.combine(commands,0,partition);
		String cmd = CMParms.combine(commands,partition);
		Room curRoom=mob.location();
		Vector<Item> V=null;

		String whatToDo=mobname.toUpperCase();
		boolean allFlag=whatToDo.startsWith("ALL ")||whatToDo.equals("ALL");
		if(whatToDo.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDo="ALL "+whatToDo.substring(4);}
		if(whatToDo.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDo="ALL "+whatToDo.substring(0,whatToDo.length()-4);}

		done:
		if(allFlag)
		{
			V=(Vector)CMLib.english().fetchInteractables(whatToDo,false,1,Integer.MAX_VALUE,curRoom.getItemCollection());
			if(mobFlag) for(int i=V.size()-1;i>=0;i--)
			{
				Item I=V.get(i);
				if((I instanceof Body)&&(((Body)I).mob()!=null))
					V.remove(i);
			}
			else if(itemFlag) for(int i=V.size()-1;i>=0;i--)
			{
				Item I=V.get(i);
				if((I instanceof Body)&&(((Body)I).mob()!=null))
					V.remove(i);
			}
			if(V.size()>maxToDo)
				V.setSize(maxToDo);
		}
		else
		{
			if(itemFlag)
			{
				V=(Vector)CMLib.english().fetchInteractables(whatToDo,false,1,Integer.MAX_VALUE,curRoom.getItemCollection());
				for(int i=0;i<V.size();i++)
				{
					Item I=V.get(i);
					if(!(I instanceof Body)||(((Body)I).mob()==null))
					{
						V=new Vector();
						V.add(I);
						break;
					}
				}
			}
			else if(mobFlag)
			{
				V=(Vector)CMLib.english().fetchInteractables(whatToDo,false,1,Integer.MAX_VALUE,curRoom.getItemCollection());
				for(int i=0;i<V.size();i++)
				{
					Item I=V.get(i);
					if((I instanceof Body)&&(((Body)I).mob()!=null))
					{
						V=new Vector();
						V.add(I);
						break;
					}
				}
			}
			else
			{
				V=new Vector();
				for(Session S : CMLib.sessions().toArray())
				{
					MOB M=S.mob();
					if((M!=null)&&(M.name().equalsIgnoreCase(mobname)))
					{
						V.addElement(M.body());
						break done;
					}
				}
				V.add((Item)CMLib.english().fetchInteractable(whatToDo,false,1,curRoom.getItemCollection()));
			}
		}

		if((V.size()==0)||(V.get(0)==null))
		{
			mob.tell("Transfer what?  '"+mobname+"' is unknown to you.");
			return false;
		}

		if(cmd.equalsIgnoreCase("here")||cmd.equalsIgnoreCase("."))
			room=curRoom;
		else
			room=CMLib.map().findWorldRoomLiberally(mob,cmd,"RIPME",1000,120);

		if(room==null)
		{
			mob.tell("Transfer where? '"+cmd+"' is unknown.  Enter a Room ID, player name, area name, or room text!");
			return false;
		}
		for(int i=0;i<V.size();i++)
		{
			Item I=V.get(i);
			room.bringHere(I,false);
			if((I instanceof Body)&&(((Body)I).mob()!=null))
				CMLib.commands().postLook(((Body)I).mob());
		}
		mob.tell("Done.");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"TRANSFER");}
}
