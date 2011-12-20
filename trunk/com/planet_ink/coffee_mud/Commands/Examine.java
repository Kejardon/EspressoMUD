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

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Examine extends StdCommand
{
	public Examine(){}

	private String[] access={"EXAMINE","EXAM","EXA","LONGLOOK","LLOOK","LL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
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
			if((mob.location().doMessage(CMClass.getMsg(mob,thisThang,null,EnumSet.of(CMMsg.MsgCode.EXAMINE),textMsg+name+" closely.")))
				&&(thisThang instanceof Room))
				CMLib.commands().lookAtExits((Room)thisThang,mob);
		}
		else
			mob.tell("You don't see that here!");
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return 1.0;}
	public boolean canBeOrdered(){return true;}
}