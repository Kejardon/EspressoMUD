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

public class Say extends StdCommand
{
	public Say(){access=new String[]{"SAY", "`", "SAYTO", "ASK", "ASKTO", "YELL", "YELLTO"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String theWord="Say";
		boolean toFlag=false;
		{
			String firstWord=commands.get(0).toUpperCase();
			if(firstWord.startsWith("A"))
				theWord="Ask";
			else
			if(firstWord.startsWith("Y"))
				theWord="Yell";
			else
				theWord="Say";
			if(firstWord.indexOf("T")>=0)
				toFlag=true;
		}

		Room R=mob.location();
		if(commands.size()==1)
		{
			mob.tell(theWord+" what?");
			return false;
		}

		commands.remove(0);
		Interactable target=null;
		if(toFlag)
		{
			String whom=commands.get(0);
			if(commands.size()<2)
			{
				mob.tell(theWord+" what to them?");
				return false;
			}
			target=CMLib.english().fetchInteractable(whom, false, 1, R, mob);
			if(target==null)
			{
				mob.tell("You don't see "+whom+" here to speak to.");
				return false;
			}
			commands.remove(1);
		}
		String combinedCommands=CMParms.combineWithQuotes(commands,0);
		if(combinedCommands.equals(""))
		{
			mob.tell(theWord+" what?");
			return false;
		}

		String toTarget=null;
		if((target!=null)&&(!theWord.equals("Ask")))
		{
			if(theWord.equals("Say"))
				toTarget="^T^<SAY \""+mob.name()+"\"^><S-NAME> say(s) to <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
			else
				toTarget="^T^<SAY \""+mob.name()+"\"^><S-NAME> yell(s) at <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
		}
		else
			toTarget="^T^<SAY \""+mob.name()+"\"^><S-NAME> "+theWord.toLowerCase()+"(s) '"+combinedCommands+"'^</SAY^>^?";
		CMMsg msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.SPEAK),toTarget);
		R.doMessage(msg);
		msg.returnMsg();
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}	//TALKING IS A FREE ACTION, LENGTH BE DAMNED
	@Override public boolean canBeOrdered(){return true;}
}
