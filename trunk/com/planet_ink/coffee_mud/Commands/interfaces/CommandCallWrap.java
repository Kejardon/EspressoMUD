package com.planet_ink.coffee_mud.Commands.interfaces;
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
import java.util.concurrent.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CommandCallWrap implements Callable<Void>
{
//	public static final ExecutorService threadPool=Executors.newCachedThreadPool();
	/*
	Future F=CMClass.threadPool.submit(new CommandCallWrap(mob, commands, metaFlags, O));
	or more likely
	Vector<Future<Void>> pendingPrompts=new Vector();
	...
	pendingPrompts.add(CMClass.threadPool.submit(new CommandCallWrap(mob, commands, metaFlags, O)));
	*/

	protected final MOB mob;
	protected final MOB.QueuedCommand commands;
	public CommandCallWrap(MOB mob, MOB.QueuedCommand commands)
	{
		this.mob=mob;
		this.commands=commands;
	}
	public Void call()	//even though it returns null, the fact it returns when done is useful, so submit(), not execute()
	{
		commands.command.execute(mob, commands);
		return null;
	}
	
}