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
public class Create extends StdCommand
{
	public Create(){}

	private String[] access={"CREATE"};
	public String[] getAccessWords(){return access;}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		Room here=mob.location();
		if(here==null)
		{
			errorOut(mob);
			return;
		}
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE EXIT [EXIT TYPE]\n\r");
			return;
		}

		String Locale=(String)commands.elementAt(2);
		Exit thisExit=(Exit)CMClass.Objects.EXIT.getNew(Locale);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit type '"+Locale+"'.\n\r");
			return;
		}
		Room R=null;
		while(R==null)
		{
			String roomID=mob.session().prompt("Connect this exit to what room? ","");
			if(roomID.equals("")) break;
			R=CMLib.map().getRoom(roomID);
		}
		if(R==null)
		{
			mob.tell("You must have a valid room for the exit to go to.\n\r");
			return;
		}
		boolean returnExit=mob.session().prompt("Make a return exit? (Y/n)","Y").substring(0,1).equalsIgnoreCase("Y");

		CMLib.genEd().genMiscSet(mob,thisExit);
		here.addExit(thisExit, R);
		if(returnExit) R.addExit(thisExit, here);

		here.showHappens(EnumSet.of(CMMsg.MsgCode.VISUAL),null,"Suddenly a passage opens up.\n\r");
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.name()+".");
	}

	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ITEM [ITEM NAME](@ room/[MOB NAME])\n\r");
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
					dest = ItemCollection.DefaultItemCol.getFrom(mob.location().fetchItem(rest));
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
		Item newItem=(Item)CMClass.Objects.ITEM.getNew(itemID);

		if(newItem==null)
		{
			mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
			return;
		}

		CMLib.genEd().genMiscSet(mob,newItem);
		dest.addItem(newItem);
		mob.location().showHappens(EnumSet.of(CMMsg.MsgCode.VISUAL),null,"Suddenly, "+newItem.name()+" drops from the sky.");
		Log.sysOut("Items",mob.name()+" created item "+newItem.ID()+".");
	}

	public void rooms(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [ROOM TYPE]\n\r");
			return;
		}

		Room thisRoom=null;
		String str=(String)commands.get(2);
		thisRoom=(Room)CMClass.Objects.LOCALE.getNew(str);
		if(thisRoom==null)
		{
			mob.tell("You have failed to specify a valid room type '"+str+"'.\n\r");
			return;
		}
		Area area=null;
		if((commands.size()>=4)&&(((String)commands.get(3)).equalsIgnoreCase("HERE")))
			area=mob.location().getArea();
		else
			area=CMLib.genEd().areaPrompt(mob);
		if(area==null)
		{
			mob.tell("Without a valid area the room cannot function nor be saved.\n\r");
			return;
		}
		thisRoom.setArea(area);
		thisRoom.setDisplayText(CMClass.classID(thisRoom)+"-"+thisRoom.roomID());
		thisRoom.setDescription("");
		thisRoom.recoverRoomStats();
		Log.sysOut("Rooms",mob.name()+" created room "+thisRoom.roomID()+".");
		CMLib.genEd().genMiscSet(mob, thisRoom);
	}

	public void mobs(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE MOB [MOB TYPE]\n\r");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB newMOB=(MOB)CMClass.Objects.MOB.getNew(mobID);

		if(newMOB==null)
		{
			mob.tell("There's no such thing as a '"+mobID+"'.\n\r");
			return;
		}

		newMOB.setLocation(mob.location());
		newMOB.setBody(new Body.DefaultBody());
		CMLib.genEd().genMiscSet(mob,newMOB.body());
		newMOB.body().bringToLife(mob.location(),true);
		mob.location().showHappens(EnumSet.of(CMMsg.MsgCode.VISUAL),null,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
		Log.sysOut("Mobs",mob.name()+" created mob "+newMOB.name()+".");
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE AREA [AREA TYPE]\n\r");
			return;
		}
		String str=(String)commands.get(2);
		Area thisArea=(Area)CMClass.Objects.AREA.getNew(str);
		if(thisArea==null)
		{
			mob.tell("'"+str+"' is not a valid area type.\n\r");
			return;
		}
		
		String areaName="";
		while(areaName.length()==0)
		{
			areaName=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
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
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			areas(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			items(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			rooms(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mobs(mob,commands);
		}
		else
			mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, MOB, AREA, or ROOM.");
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	
}
