package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;


/*
EspressoMUD copyright 2015 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/


/*
Simple interface denoting code to handle an arbitrary action
	prereqs called before even doing the message, creates list of other things that need to be done first
	sendAction called to do the action (okMessage and response and executeMsg) (Note: ActionCode is basically locked to a single thread at this point)
	satisfiesPrereqs is called during the okMessage to double check prereqs and set up response if needed.
	handleAction is called during the executeMsg to resolve the action.

	Eat command starts the process. Calls eatPrereqs then maybe sendEat
		eatPrereqs figures out what needs to be done before eating the first item in the list, and starts it.
		sendEat creates and sends the Eat CMMsg, uses Race.getBiteSize
			Eat CMMsg triggers Body okMessage and response.
				okMessage calls satisfiesEatPrereqs again to confirm the MOB is still able to eat the object at the time the message gets room lock, and assigns response
				response calls handleEat to do the action of giving the PC nourishment and such? Perhaps make EatCode a msg responder..
					handleEat calls Race.diet to calculate PC nourishment and Race.applyDiet to reduce the consumed item.
	
*/
public interface ActionCode
{
	public enum Type
	{
		GET,
		GIVE,
		MOVE
		
	}
			
	/*
	-1 : Failed
	0 : No prereqs (or trivial?)
	1 : Must do prereqs first (Perhaps return number of prereq commands?)
	
	Redoing
	null : Failed
	Empty ArrayList : No prereqs(or trivial)
	Populated ArrayList : Must do prereqs first (do after last in the arraylist)
	*/
	public ArrayList<MOB.QueuedCommand> prereqs(MOB mob, Body body, CMMsg msg);
	//-1 : Failed
	//0 : Done successfully
	//>=1 : Done partially, long of next-time-to-continue-action? or 1 and use TIME_TICK... or have this return TIME_TICK most of the time...
	//Note: It is up to the ActionCode to return msg when it is done.
	public long sendAction(MOB mob, Body body, CMMsg msg);
	//okMessage call
	public boolean satisfiesPrereqs(CMMsg msg);
	//executeMsg call
	public void handleAction(CMMsg msg);
	//Respond to okMessage call. Not sure if this actually makes sense in a general case.
	//public boolean satisfiesReqs(CMMsg msg);
}
