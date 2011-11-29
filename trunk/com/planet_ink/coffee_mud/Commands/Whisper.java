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
public class Whisper extends StdCommand
{
	public Whisper(){}

	private String[] access={"WHISPER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()==1)
		{
			mob.tell("Whisper what?");
			return false;
		}
		Interactable target=null;
		Room R = mob.location();
		if(commands.size()>2)
		{
			String possibleTarget=(String)commands.elementAt(1);
			target=R.fetchInhabitant(possibleTarget);
			if(target==null) target=CMLib.english().fetchInteractable(possibleTarget,false,1,mob,mob.location());
			if(target!=null)
				commands.removeElementAt(1);
		}
		String combinedCommands=CMParms.combine(commands,1);

		CMMsg msg=null;
		if(target==null)
			msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T<S-NAME> whisper(s) to <S-HIM-HERSELF> '"+combinedCommands+"'.^?"+CMProps.msp("whisper.wav",40),
										  EnumSet.noneOf(CMMsg.MsgCode.class),null,
										  EnumSet.of(CMMsg.MsgCode.SPEAK),"^T<S-NAME> whisper(s) to <S-HIM-HERSELF>.^?"+CMProps.msp("whisper.wav",40));
		else
			msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T^<WHISPER \""+target.name()+"\"^><S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'.^</WHISPER^>^?"+CMProps.msp("whisper.wav",40)
										   ,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T^<WHISPER \""+target.name()+"\"^><S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'^</WHISPER^>.^?"+CMProps.msp("whisper.wav",40)
										   ,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T<S-NAME> whisper(s) something to <T-NAMESELF>.^</WHISPER^>^?"+CMProps.msp("whisper.wav",40));
		R.doMessage(msg);
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
