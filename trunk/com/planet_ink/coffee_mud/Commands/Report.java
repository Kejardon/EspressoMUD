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
public class Report extends StdCommand
{
	public Report(){}

	private String[] access={"REPORT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			StringBuffer buf=new StringBuffer(
								"say \"I have "+mob.charStats().getPoints(CharStats.STAT_HITPOINTS)
							   +"/"+mob.charStats().getMaxPoints(CharStats.STAT_HITPOINTS)+" hit points, "
							   +mob.charStats().getPoints(CharStats.STAT_MANA)+"/"+mob.charStats().getMaxPoints(CharStats.STAT_MANA)
							   +" mana, "+mob.charStats().getPoints(CharStats.STAT_MOVE)
							   +"/"+mob.charStats().getMaxPoints(CharStats.STAT_MOVE)+" move");
			buf.append(".\"");
			Command C=CMClass.getCommand("Say");
			if(C!=null) C.execute(mob,CMParms.parse(buf.toString()),metaFlags);
		}
		else
		{
			String s=CMParms.combine(commands,1).toUpperCase();
			StringBuffer say=new StringBuffer("");
			if("AFFECTS".startsWith(s)||(s.equalsIgnoreCase("ALL")))
			{
				
				StringBuffer aff=new StringBuffer("\n\r^!I am affected by:^? ");
				Command C=CMClass.getCommand("Affect");
				if(C!=null) C.execute(mob,CMParms.makeVector(aff),metaFlags);
				say.append(aff.toString());
			}
			
			if(say.length()==0)
				mob.tell("'"+s+"' is unknown.  Try STATS, or ALL.");
			else
				CMLib.commands().postSay(mob,null,say.toString(),false,false);
		}
		return false;
	}
	public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
	public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
