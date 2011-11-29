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
public class Look extends StdCommand
{
	public Look(){}

	private String[] access={"LOOK","LOO","LO","L"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String textMsg="<S-NAME> look(s) ";
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
			String name="at <T-NAMESELF>";
			if(thisThang==mob.location())
				name="around";
			if((mob.location().doMessage(CMClass.getMsg(mob,thisThang,null,EnumSet.of(CMMsg.MsgCode.LOOK),textMsg+name+".")))
				&&(thisThang instanceof Room))
				CMLib.commands().lookAtExits((Room)thisThang,mob);
		}
		else
			mob.tell("You don't see that here!");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
