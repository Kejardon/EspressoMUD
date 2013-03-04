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
@SuppressWarnings("unchecked")
public class Examine extends StdCommand
{
	public Examine(){access=new String[]{"EXAMINE","EXAM","EXA","LONGLOOK","LLOOK","LL"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String textMsg="<S-NAME> examine(s) ";
		Interactable thisThang=null;
		if((commands!=null)&&(commands.size()>1))
		{
			String ID=CMParms.combine(commands,1);
			if(ID.length()==0)
				thisThang=mob.location();
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;
			if(thisThang==null)
				thisThang=CMLib.english().fetchInteractable(ID, false, 1, mob.getItemCollection(), mob.location());
		}
		else
			thisThang=mob.location();
		if(thisThang!=null)
		{
			String name="<T-NAMESELF>";
			if(thisThang==mob.location())
				name="around";
			CMMsg msg=CMClass.getMsg(mob,thisThang,null,EnumSet.of(CMMsg.MsgCode.EXAMINE),textMsg+name+" closely.");
			if((mob.location().doMessage(msg))
				&&(thisThang instanceof Room))
				CMLib.commands().lookAtExits((Room)thisThang,mob);
			msg.returnMsg();
		}
		else
			mob.tell("You don't see that here!");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	public boolean canBeOrdered(){return true;}
}