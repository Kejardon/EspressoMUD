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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class Link extends At
{
	public Link(){}

	private String[] access={"LINK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is LINK [ROOM ID] [DIRECTION]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		String dirStr=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		int direction=Directions.getGoodDirectionCode(dirStr);
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		Room thisRoom=null;
		String RoomID=CMParms.combine(commands,1);
		thisRoom=CMLib.map().getRoom(RoomID);
		if(thisRoom==null)
		{
			thisRoom=CMLib.map().findWorldRoomLiberally(mob,RoomID,"R",100,120);
			if(thisRoom==null)
			{
				mob.tell("Room \""+RoomID+"\" is unknown.  Try again.");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return false;
			}
		}
		exitifyNewPortal(mob,thisRoom,direction);
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);

		mob.location().recoverRoomStats();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a portal opens up in the landscape.\n\r");
		Log.sysOut("Link",mob.Name()+" linked "+CMLib.map().getExtendedRoomID(mob.location())+" to room "+CMLib.map().getExtendedRoomID(thisRoom)+".");
		return false;
	}
	
    protected void exitifyNewPortal(MOB mob, Room room, int direction)
	{
		Room opRoom=mob.location().rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		int opDir=Directions.getOpDirectionCode(direction);
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[opDir];

		if((reverseRoom!=null)
		&&(reverseRoom==mob.location()))
			mob.tell("Opposite room already exists and heads this way.  One-way link created.");

		if(opRoom!=null)
			mob.location().rawDoors()[direction]=null;
		
		WorldMap.CrossExit CE=null;

		mob.location().rawDoors()[direction]=room;
		
		Exit thisExit=mob.location().getRawExit(direction);
		if(thisExit==null)
		{
			thisExit=CMClass.getExit("StdOpenDoorway");
			mob.location().setRawExit(direction,thisExit);
		}

		if(room.rawDoors()[opDir]==null)
		{
			room.rawDoors()[opDir]=mob.location();
			room.setRawExit(opDir,thisExit);
		}
		CMLib.database().DBUpdateExits(mob.location());
		CMLib.database().DBUpdateExits(room);
	}

	
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS");}

	
}
