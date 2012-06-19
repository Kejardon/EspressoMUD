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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Go extends StdCommand
{
	public Go(){access=new String[]{"GO","WALK"};}

	public boolean move(MOB mob,
						Room.REMap exit,
						boolean nolook)
	{
		return move(mob,exit,nolook,false);
	}
	public boolean move(MOB mob,
						Room.REMap exit,
						boolean nolook,
						boolean always)
	{
		if(mob==null) return false;
		Room thisRoom=mob.location();
		Room destRoom=(exit==null?null:exit.room);

		always|=CMSecurity.isAllowed(mob,"GOTO");
		//EnumSet enterCode=EnumSet.of(CMMsg.MsgCode.LEAVE);
		//if(always)
		//	enterCode.add(CMMsg.MsgCode.ALWAYS);
		//TODO: Include specific Exit description. Also enterMsg should be a response to leaveMsg.
		EnumSet<CMMsg.MsgCode> code=always?EnumSet.of(CMMsg.MsgCode.LEAVE,CMMsg.MsgCode.ALWAYS):EnumSet.of(CMMsg.MsgCode.LEAVE);
		CMMsg leaveMsg=CMClass.getMsg(mob,null,exit,code,"<S-NAME> leave(s).");
		
		int gotDepart=thisRoom.getLock(0);
		int gotEntrance=(destRoom==null?0:destRoom.getLock(0));
		
		try{
			if((gotDepart!=2)&&(gotEntrance!=2))
				nolook|=!thisRoom.doMessage(leaveMsg);
			else
				nolook=true;
		}finally{
			if(gotEntrance==1) destRoom.returnLock();
			if(gotDepart==1) thisRoom.returnLock();
		}

		if(!nolook)
			CMLib.commands().postLook(mob);

		return true;
	}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
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
			for(int d=Directions.NUM_DIRECTIONS-1;d>=0;d--)
				if(R.getExitInDir(d)==E)
				{ direction=d; break;}
		}
*/
		Room.REMap map=R.getREMap(whereStr);
		if(map!=null)
			move(mob,map,false,false);
		else
			mob.tell("There is no exit like that.");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}