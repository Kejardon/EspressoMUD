package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import java.util.Vector;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface Command extends CMObject
{
	//Flag for commands that may call a session prompt. These commands should be called via a new thread, see CommandCallWrap.
	public boolean prompter();
	public String[] getAccessWords();
	//public double actionsCost(MOB mob, Vector<String> cmds);
	public int commandType(MOB mob, String cmds);
	public boolean canBeOrdered();
	// Whether this command is available to the given player
	public boolean securityCheck(MOB mob);
	//public boolean execute(MOB mob, String commands, int metaFlags);
	public boolean execute(MOB mob, MOB.QueuedCommand commands);
	public boolean interruptCommand(MOB.QueuedCommand interrupted, MOB.QueuedCommand interrupter);
	public MOB.QueuedCommand prepCommand(MOB mob, String commands, int metaflags);
	/* This method is only called when the mob invoking this command
	 * does not have enough actions to complete it immediately.  The
	 * method is called when the command is entered, and every second
	 * afterwards until the invoker has enough actions to complete it.
	 * At completion time, execute is called.
	 * @see Command#execute(MOB, Vector, int)
	 * @param mob the player or mob invoking the command
	 * @param commands the parameters entered for the command (including the trigger word)
	 * @param metaFlags flags denoting how the command is being executed
	 * @param secondsElapsed 0 at first, and increments every second
	 * @param actionsRemaining number of free actions the player is defficient.
	 * @return whether the command should be allowed to go forward. false cancels altogether.
	 */
	//public boolean preExecute(MOB mob, Vector<String> commands, int metaFlags, int secondsElapsed, double actionsRemaining);

//	Bitmask options, basically if there is anything interesting about how/while the command was caused.
//	public static final int METAFLAG_MPFORCED=1;
//	public static final int METAFLAG_POSSESSED=4;
	public static final int METAFLAG_ORDER=1;
	public static final int METAFLAG_SNOOPED=METAFLAG_ORDER*2;
	public static final int METAFLAG_AS=METAFLAG_SNOOPED*2;
	public static final int METAFLAG_FORCED=METAFLAG_AS*2;

	//public static final double DEFAULT_COMBATACTION=1.0;
	//public static final double DEFAULT_NONCOMBATACTION=0.2;
	
	//CommandTypes
	public static final int CT_SYSTEM=0;	//System actions, have no direct relevance to gameplay.
	public static final int CT_LOW_P_ACTION=CT_SYSTEM+1;	//Go to end of current queue of actions.
	public static final int CT_HIGH_P_ACTION=CT_LOW_P_ACTION+1;	//Go to start of current queue of actions. (Things like flee)
	public static final int CT_NON_ACTION=CT_HIGH_P_ACTION+1;	//'Free' actions. Talking, looking, (not examine), this sort of thing.
}
