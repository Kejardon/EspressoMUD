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
public class Say extends StdCommand
{
	public Say(){}

	private String[] access={"SAY",
							 "`",
							 "SAYTO",
							 "ASK",
							 "ASKTO",
							 "YELL",
							 "YELLTO"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String theWord="Say";
		boolean toFlag=false;
		{
			String firstWord=((String)commands.get(0)).toUpperCase();
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
			String whom=(String)commands.get(0);
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
		R.doMessage(CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.SPEAK),toTarget));
		return false;
	}
	public boolean canBeOrdered(){return true;}
}
