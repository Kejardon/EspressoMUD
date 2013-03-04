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
public class Whisper extends StdCommand
{
	public Whisper(){access=new String[]{"WHISPER"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
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
			String possibleTarget=commands.elementAt(1);
			target=R.fetchInhabitant(possibleTarget);
			if(target==null) target=CMLib.english().fetchInteractable(possibleTarget,false,1,mob,mob.location());
			if(target!=null)
				commands.removeElementAt(1);
		}
		String combinedCommands=CMParms.combine(commands,1);

		CMMsg msg=null;
		if(target==null)
			msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T<S-NAME> whisper(s) to <S-HIM-HERSELF> '"+combinedCommands+"'.^?",
										  EnumSet.noneOf(CMMsg.MsgCode.class),null,
										  EnumSet.of(CMMsg.MsgCode.SPEAK),"^T<S-NAME> whisper(s) to <S-HIM-HERSELF>.^?");
		else
			msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T^<WHISPER \""+target.name()+"\"^><S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'.^</WHISPER^>^?"
										   ,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T^<WHISPER \""+target.name()+"\"^><S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'^</WHISPER^>.^?"
										   ,EnumSet.of(CMMsg.MsgCode.SPEAK),"^T<S-NAME> whisper(s) something to <T-NAMESELF>.^</WHISPER^>^?");
		R.doMessage(msg);
		msg.returnMsg();
		return false;
	}
	//TODO: Check for proximity to target. If close enough this is free action, if too far must close to target first.
	public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public boolean canBeOrdered(){return true;}
}
