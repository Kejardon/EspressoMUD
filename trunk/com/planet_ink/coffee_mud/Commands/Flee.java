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
public class Flee extends Go
{
	public Flee(){}

	private String[] access={"FLEE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String direction="";
		if(commands.size()>1) direction=CMParms.combine(commands,1);
		if(mob==null) return false;
		Room R=mob.location();
		if(R==null) return false;
		
		MOB fighting=mob.getVictim();
		if((direction.length()==0))
		{
			mob.tell("You stop fighting.");
			direction="NOWHERE";
		}
		
		int directionCode=-1;
		if(!direction.equals("NOWHERE"))
		{
			if(direction.length()==0)
			{
				Vector directions=new Vector();
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Exit thisExit=R.getExitInDir(d);
					Room thisRoom=R.getRoomInDir(d);
					if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
						directions.addElement(Integer.valueOf(d));
				}
				// up is last resort
				if(directions.size()>1)
					directions.removeElement(Integer.valueOf(Directions.UP));
				if(directions.size()>0)
				{
					directionCode=((Integer)directions.elementAt(CMLib.dice().roll(1,directions.size(),-1))).intValue();
					direction=Directions.getDirectionName(directionCode);
				}
			}
			else
				directionCode=Directions.getGoodDirectionCode(direction);
			if(directionCode<0)
			{
				mob.tell("Flee where?!");
				return false;
			}
		}
		if((direction.equals("NOWHERE"))||((directionCode>=0)&&(move(mob,directionCode,true,false,false))))
		{
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
