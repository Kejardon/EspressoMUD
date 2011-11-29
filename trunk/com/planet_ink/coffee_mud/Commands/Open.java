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
public class Open extends StdCommand
{
	public Open(){}

	private String[] access={"OPEN","OP","O"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String whatToOpen=CMParms.combine(commands,1);
		if(whatToOpen.length()==0)
		{
			mob.tell("Open what?");
			return false;
		}
		Interactable openThis=CMLib.english().fetchInteractable(whatToOpen, false, 1, mob, mob.location());
		if(openThis==null)
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,openThis,null,EnumSet.of(CMMsg.MsgCode.OPEN),("<S-NAME> open(s) <T-NAMESELF>.")+CMProps.msp("dooropen.wav",10));
/*		if(openThis instanceof Exit)
		{
			boolean open=((Exit)openThis).isOpen();
			if((mob.location().okMessage(msg.source(),msg))
			&&(!open))
			{
				mob.location().send(msg.source(),msg);

				if(dirCode<0)
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if(mob.location().getExitInDir(d)==openThis)
					{dirCode=d; break;}
				if((dirCode>=0)&&(mob.location().getRoomInDir(dirCode)!=null))
				{
					Room opR=mob.location().getRoomInDir(dirCode);
					Exit opE=mob.location().getPairedExit(dirCode);
					if(opE!=null)
					{
						CMMsg altMsg=CMClass.getMsg(msg.source(),opE,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
						opE.executeMsg(msg.source(),altMsg);
					}
					int opCode=Directions.getOpDirectionCode(dirCode);
					if((opE!=null)&&(opE.isOpen())&&(((Exit)openThis).isOpen()))
					   opR.showHappens(CMMsg.MSG_OK_ACTION,opE.name()+" "+Directions.getInDirectionName(opCode)+" opens.");
				}
			}
		}
		else
*/		mob.location().doMessage(msg);
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}