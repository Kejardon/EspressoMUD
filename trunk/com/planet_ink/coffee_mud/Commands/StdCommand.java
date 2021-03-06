package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class StdCommand implements Command
{
	public StdCommand(){}
	protected String ID=null;
	@Override public String ID()
	{
		if(ID==null){
			ID=this.getClass().getName();
			int x=ID.lastIndexOf(".");
			if(x>=0) ID=ID.substring(x+1);
		}
		return ID;
	}

	//Importantish NOTE: Access strings should be in all caps, and not use `(' is ok) or ". Otherwise other normal ASCII values are fine.
	protected String[] access=new String[0];
	@Override public String[] getAccessWords(){return access;}
	@Override public void initializeClass(){}
	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		return true;
	}
	@Override public boolean execute(MOB mob, MOB.QueuedCommand commands)
	{
		return execute(mob, CMParms.parse(commands.cmdString, -1), commands.metaFlags);
	}
	@Override public MOB.QueuedCommand prepCommand(MOB mob, String commands, int metaflags)
	{
		MOB.QueuedCommand commandInstance=MOB.QueuedCommand.newQC();
		commandInstance.command=this;
		commandInstance.cmdString=commands;
		commandInstance.commandType=commandType(mob, commands);
		commandInstance.metaFlags=metaflags;
		return commandInstance;
	}
/*	public boolean preExecute(MOB mob, Vector<String> commands, int metaFlags, int secondsElapsed, double actionsRemaining)
	{
		return true;
	}
*/
	@Override public boolean interruptCommand(MOB.QueuedCommand thisCommand, MOB.QueuedCommand interruptingCommand){return true;}
	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return true;}
	@Override public CMObject newInstance(){return this;}
	@Override public CMObject copyOf() { return this; }
	@Override public int prompter(){return 0;}
	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
