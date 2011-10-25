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
public class Group extends StdCommand
{
	public Group(){}

	private String[] access={"GROUP","GR"};
	public String[] getAccessWords(){return access;}
	
	public static StringBuffer showWhoLong(MOB who)
	{

		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		if(!CMSecurity.isDisabled("RACES"))
		{
			msg.append(CMStrings.padRight(who.charStats().raceName(),7)+" ");
		}
		if(!CMSecurity.isDisabled("CLASSES"))
		{
			msg.append(CMStrings.padRight(" ",7)+" ");
		}
		msg.append("] "+CMStrings.padRight(who.name(),13)+" ");
		msg.append(CMStrings.padRightPreserve("hp("+CMStrings.padRightPreserve(""+who.charStats().getPoints(CharStats.STAT_HITPOINTS),3)+"/"+CMStrings.padRightPreserve(""+who.charStats().getMaxPoints(CharStats.STAT_HITPOINTS),3)+")",12));
		msg.append(CMStrings.padRightPreserve("mn("+CMStrings.padRightPreserve(""+who.charStats().getPoints(CharStats.STAT_MANA),3)+"/"+CMStrings.padRightPreserve(""+who.charStats().getMaxPoints(CharStats.STAT_MANA),3)+")",12));
		msg.append(CMStrings.padRightPreserve("mv("+CMStrings.padRightPreserve(""+who.charStats().getPoints(CharStats.STAT_MOVE),3)+"/"+CMStrings.padRightPreserve(""+who.charStats().getMaxPoints(CharStats.STAT_MANA),3)+")",12));
		msg.append("\n\r");
		return msg;
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		mob.tell(mob.name()+"'s group:\n\r");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
