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
import java.util.Vector;

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
/**
 * A Command is a thing entered on the command line by players.  It
 * performs some function, right?
 */
@SuppressWarnings("unchecked")
public interface Command extends CMObject
{
	/**
	 * Returns the set of command words, with the most public one first,
	 * that are entered by the user to initiate this command.
	 * @return the set of command words that the user enters
	 */
	public String[] getAccessWords();
	public double actionsCost(MOB mob, Vector cmds);
//	public double combatActionsCost(MOB mob, Vector cmds);
	public boolean canBeOrdered();
	// Whether this command is available to the given player
	public boolean securityCheck(MOB mob);
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException;
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
	public boolean preExecute(MOB mob, Vector commands, int metaFlags, int secondsElapsed, double actionsRemaining)
		throws java.io.IOException;

//	Bitmask options, basically if there is anything interesting about how/while the command was caused.
//	public static final int METAFLAG_MPFORCED=1;
	public static final int METAFLAG_ORDER=2;
//	public static final int METAFLAG_POSSESSED=4;
	public static final int METAFLAG_SNOOPED=8;
	public static final int METAFLAG_AS=16;
	public static final int METAFLAG_FORCED=32;

	public static final double DEFAULT_COMBATACTION=1.0;
	public static final double DEFAULT_NONCOMBATACTION=0.2;
}
