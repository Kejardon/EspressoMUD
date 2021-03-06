package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/


/*
Simple interface denoting code to handle an eat action
	Eat command starts the process. Calls eatPrereqs then maybe sendEat
		eatPrereqs figures out what needs to be done before eating the first item in the list, and starts it.
		sendEat creates and sends the Eat CMMsg, uses Race.getBiteSize
			Eat CMMsg triggers Body okMessage and response.
				okMessage calls satisfiesEatPrereqs again to confirm the MOB is still able to eat the object at the time the message gets room lock, and assigns response
				response calls handleEat to do the action of giving the PC nourishment and such? Perhaps make EatCode a msg responder..
					handleEat calls Race.diet to calculate PC nourishment and Race.applyDiet to reduce the consumed item.
	
*/
public interface EatCode
{
	//-1 : Failed
	//0 : Done successfully
	//>=1 : Done partially, long of next-time-to-continue-eating? or 1 and use TIME_TICK... or have this return TIME_TICK most of the time...
	public long sendEat(MOB mob, Body body, Vector<Interactable> items);
	public boolean handleEat(CMMsg msg);
	/*
	Ready and confirm eatability of list of items. Change list of items (tell the mob why!) if some are uneatable.
	
	-1 : Failed
	0 : No prereqs (or trivial?)
	1 : Must do prereqs first (Perhaps return number of prereq commands?)
	
	Redoing
	null : Failed
	Empty ArrayList : No prereqs(or trivial)
	Populated ArrayList : Must do prereqs first (do after last in the arraylist)
	*/
	public ArrayList<MOB.QueuedCommand> eatPrereqs(MOB mob, Body body, Vector<Interactable> items); //Vector<Item> failed
	//Ok Message call
	public boolean satisfiesEatPrereqs(CMMsg msg);
	//Respondto message call
	public boolean satisfiesEatReqs(CMMsg msg);
}
