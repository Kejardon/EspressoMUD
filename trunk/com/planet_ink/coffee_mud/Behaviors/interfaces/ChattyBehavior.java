package com.planet_ink.coffee_mud.Behaviors.interfaces;
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
/**
 * A LegalBehavior is a Behavior causes a mob to have a conversation,
 * or even just simply respond to a player or even another mob.
 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
 */
public interface ChattyBehavior extends Behavior
{
	/**
	 * Returns the last thing actually spoken by the wielder of this
	 * behavior, or null if nothing has been said yet.
	 * @return the last thing said.
	 */
	public String getLastThingSaid();

	/**
	 * Returns the last MOB object spoken to.
	 * @return the last MOB object spoken to.
	 */
	public MOB getLastRespondedTo();
}