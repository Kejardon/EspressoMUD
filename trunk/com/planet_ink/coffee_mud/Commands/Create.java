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

public class Create extends StdCommand
{
	public Create(){access=new String[]{"CREATE"};}

	public void exits(MOB mob, Vector<String> commands)
	{
		Room here=mob.location();
		if(here==null)
		{
			errorOut(mob);
			return;
		}
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is CREATE EXIT [EXIT TYPE]\r\n");
			return;
		}

		String Locale=commands.elementAt(2);
		Exit thisExit=CMClass.EXIT.getNew(Locale);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit type '"+Locale+"'.\r\n");
			return;
		}
		Room R=null;
		while(R==null)
		{
			String roomID=mob.session().prompt("Connect this exit to what room? ","");
			if(roomID.equals("")) break;
			R=SIDLib.ROOM.get(CMath.s_int(roomID));
		}
		if(R==null)
		{
			mob.tell("You must have a valid room for the exit to go to.\r\n");
			return;
		}
		boolean returnExit=mob.session().prompt("Make a return exit? (Y/n)","Y").substring(0,1).equalsIgnoreCase("Y");

		CMLib.genEd().genMiscSet(mob,thisExit);
		here.addExit(thisExit, R);
		if(returnExit) R.addExit(thisExit, here);

		here.show(null,"Suddenly a passage opens up.\r\n");
		Log.sysOut("Exits",mob.location().saveNum()+" exits changed by "+mob.name()+".");
	}

	public void items(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is CREATE ITEM [ITEM NAME](@ room/[MOB NAME])\r\n");
			return;
		}

		String itemID=CMParms.combine(commands,2);
		ItemCollection dest=mob.location().getItemCollection();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				Vector<MOB> V=mob.location().fetchInhabitants(rest);
				if(V.size()==0)
				{
					dest = ItemCollection.O.getFrom(mob.location().fetchItem(rest));
					if(dest==null)
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						return;
					}
				}
				else
					dest=V.get(0).getItemCollection();
			}
		}
		Item newItem=CMClass.ITEM.getNew(itemID);

		if(newItem==null)
		{
			mob.tell("There's no such thing as a '"+itemID+"'.\r\n");
			return;
		}

		CMLib.genEd().genMiscSet(mob,newItem);
		dest.addItem(newItem);
		mob.location().show(null,"Suddenly, "+newItem.name()+" drops from the sky.");
		Log.sysOut("Items",mob.name()+" created item "+newItem.ID()+".");
	}

	public void rooms(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is CREATE ROOM [ROOM TYPE]\r\n");
			return;
		}

		Room thisRoom=null;
		String str=(String)commands.get(2);
		thisRoom=CMClass.LOCALE.getNew(str);
		if(thisRoom==null)
		{
			mob.tell("You have failed to specify a valid room type '"+str+"'.\r\n");
			return;
		}
		Area area=null;
		if((commands.size()>=4)&&(commands.get(3).equalsIgnoreCase("HERE")))
			area=mob.location().getArea();
		else
			area=CMLib.genEd().areaPrompt(mob);
		if(area==null)
		{
			mob.tell("Without a valid area the room cannot function nor be saved.\r\n");
			return;
		}
		thisRoom.setArea(area);
		thisRoom.setDisplayText(CMClass.classID(thisRoom)+"-"+thisRoom.saveNum());
		thisRoom.setDescription("");
		thisRoom.recoverRoomStats();
		Log.sysOut("Rooms",mob.name()+" created room "+thisRoom.saveNum()+".");
		CMLib.genEd().genMiscSet(mob, thisRoom);
	}

	public void mobs(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is CREATE MOB [MOB TYPE]\r\n");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB newMOB=CMClass.CREATURE.getNew(mobID);

		if(newMOB==null)
		{
			mob.tell("There's no such thing as a '"+mobID+"'.\r\n");
			return;
		}

		newMOB.setLocation(mob.location());
		newMOB.setBody((Body)CMClass.ITEM.getNew("StdBody"));
		CMLib.genEd().genMiscSet(mob,newMOB.body());
		newMOB.body().bringToLife(mob.location(),true);
		mob.location().show(null,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
		Log.sysOut("Mobs",mob.name()+" created mob "+newMOB.name()+".");
	}

	public void areas(MOB mob, Vector<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\r\nThe format is CREATE AREA [AREA TYPE]\r\n");
			return;
		}
		String str=commands.get(2);
		Area thisArea=CMClass.AREA.getNew(str);
		if(thisArea==null)
		{
			mob.tell("'"+str+"' is not a valid area type.\r\n");
			return;
		}
		
//		thisArea.initChildren();
		String areaName="";
		while(areaName.length()==0)
		{
			areaName=mob.session().prompt("Enter a name for the new area: ");
			if(areaName.length()==0) return;
			if(CMLib.map().getArea(areaName)!=null)
			{
				mob.tell("An area with the name '"+areaName+"' already exists!");
				areaName="";
			}
		}
		thisArea.setName(areaName);
		CMLib.map().addArea(thisArea);
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
			commandType=commands.elementAt(1).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDEXITS")) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDAREAS")) return errorOut(mob);
			areas(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDITEMS")) return errorOut(mob);
			items(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDROOMS")) return errorOut(mob);
			rooms(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,"CMDMOBS")) return errorOut(mob);
			mobs(mob,commands);
		}
		else
			mob.tell("\r\nYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, MOB, AREA, or ROOM.");
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public int prompter(){return 1;}
	@Override public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,"CMD");}
}