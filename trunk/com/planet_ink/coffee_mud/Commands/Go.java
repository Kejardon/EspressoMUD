package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Go extends StdCommand
{
	public Go(){}

	private String[] access={"GO","WALK"};
	public String[] getAccessWords(){return access;}

	public boolean move(MOB mob,
						Exit exit,
						Room destRoom,
						boolean nolook)
	{
		return move(mob,exit,destRoom,nolook,false);
	}
	public boolean move(MOB mob,
						Exit exit,
						Room destRoom,
						boolean nolook,
						boolean always)
	{
		if(mob==null) return false;
		Room thisRoom=mob.location();

//		Exit opExit=thisRoom.getReverseExit(directionCode);
//		String directionName=(directionCode==Directions.GATE)&&(exit!=null)?"through "+exit.name():Directions.getDirectionName(directionCode);
//		String otherDirectionName=(Directions.getOpDirectionCode(directionCode)==Directions.GATE)&&(exit!=null)?exit.name():Directions.getFromDirectionName(Directions.getOpDirectionCode(directionCode));

		EnumSet enterCode=EnumSet.of(CMMsg.MsgCode.ENTER);
		EnumSet leaveCode=EnumSet.of(CMMsg.MsgCode.LEAVE);
		if(always)
		{
			enterCode.add(CMMsg.MsgCode.ALWAYS);
			leaveCode.add(CMMsg.MsgCode.ALWAYS);
		}
		//TODO: Include specific Exit description
		CMMsg enterMsg=CMClass.getMsg(mob,destRoom,exit,enterCode,null,enterCode,null,enterCode,"<S-NAME> enter(s).");
		CMMsg leaveMsg=CMClass.getMsg(mob,thisRoom,exit,leaveCode,null,leaveCode,null,leaveCode,"<S-NAME> leave(s).");
		boolean gotoAllowed=CMSecurity.isAllowed(mob,destRoom,"GOTO");
		if((!exit.okMessage(destRoom,enterMsg))&&(!gotoAllowed))
			return false;
		else
		if(!thisRoom.okMessage(thisRoom,leaveMsg)&&(!gotoAllowed))
			return false;
		else
		if(!destRoom.okMessage(destRoom,enterMsg)&&(!gotoAllowed))
			return false;
//		else
//		if(!mob.okMessage(mob,enterMsg)&&(!gotoAllowed))
//			return false;

		leaveMsg.handleResponses();
		enterMsg.handleResponses();
		thisRoom.send(leaveMsg);
		destRoom.send(enterMsg);

//		mob.charStats().expendEnergy(mob,true);

		if(!nolook)
			CMLib.commands().postLook(mob,true);

		return true;
	}
/*
	protected Command stander=null;
	protected Vector ifneccvec=null;
	public void standIfNecessary(MOB mob, int metaFlags)
		throws java.io.IOException
	{
		if((ifneccvec==null)||(ifneccvec.size()!=2))
		{
			ifneccvec=new Vector();
			ifneccvec.addElement("STAND");
			ifneccvec.addElement("IFNECESSARY");
		}
		if(stander==null) stander=CMClass.getCommand("Stand");
		if((stander!=null)&&(ifneccvec!=null))
			stander.execute(mob,ifneccvec,metaFlags);
	}
*/
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
//		standIfNecessary(mob,metaFlags);
		if((commands.size()>3)
		&&(commands.firstElement() instanceof Exit))
		{
			return move(mob,
						(Exit)commands.elementAt(0),
						(Room)commands.elementAt(1),
						((Boolean)commands.elementAt(2)).booleanValue(),
						((Boolean)commands.elementAt(3)).booleanValue());

		}
		String whereStr=CMParms.combine(commands,1);
		Room R=mob.location();

//TODO: finish this when room grids are done.
/*
		Interactable E=null;
		if(R!=null)
			E=CMLib.english().fetchInteractable(whereStr, false, 1, R);
		if(E instanceof Rideable)
		{
			Command C=CMClass.getCommand("Enter");
			return C.execute(mob,commands,metaFlags);
		}
		if(E instanceof Exit)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(R.getExitInDir(d)==E)
				{ direction=d; break;}
		}
*/
		Room.REMap map=R.getREMap(whereStr);
		if(map!=null)
			move(mob,map.exit,map.room,false,false);
		else
			mob.tell("There is no exit like that.");
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){
		return DEFAULT_NONCOMBATACTION;
	}
	public boolean canBeOrdered(){return true;}
}
