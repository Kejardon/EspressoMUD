package com.planet_ink.coffee_mud.Libraries.interfaces;
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
@SuppressWarnings("unchecked")
public interface CommonCommands extends CMLibrary
{
	public boolean forceStandardCommand(MOB mob, String command, Vector parms);
	public StringBuilder getScore(MOB mob);
	public StringBuilder getEquipment(MOB viewer, MOB mob);
	public StringBuilder getInventory(MOB viewer, MOB mob);
	public void postChannel(MOB mob, String channelName, String message, boolean systemMsg);
	public void postChannel(String channelName, String message, boolean systemMsg);
	public boolean postDrop(MOB mob, Environmental dropThis, boolean quiet, boolean optimized);
	public boolean postGet(MOB mob, Item container, Item getThis, boolean quiet);
	public boolean postRemove(MOB mob, Item item, boolean quiet);
	public void postLook(MOB mob, boolean quiet);
	public void postFlee(MOB mob, String whereTo);
	public void postSheath(MOB mob, boolean ifPossible);
	public void postDraw(MOB mob, boolean doHold, boolean ifNecessary);
	public void postStand(MOB mob, boolean ifNecessary);
	public void postSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag);
	public void postSay(MOB mob, MOB target,String text);
	public void postSay(MOB mob, String text);
	public void handleBeingLookedAt(CMMsg msg);
//	public String examineItemString(MOB mob, Item item);
	public void handleBeingRead(CMMsg msg);
//	public void handleRecall(CMMsg msg);
	public void handleSit(CMMsg msg);
	public void handleLayDown(CMMsg msg);
	public void handleStand(CMMsg msg);
	public void handleSleep(CMMsg msg);
	public void handleBeingSniffed(CMMsg msg);
//	public void handleBeingGivenTo(CMMsg msg);
//	public void handleBeingGetted(CMMsg msg);
//	public void handleBeingDropped(CMMsg msg);
//	public void handleBeingRemoved(CMMsg msg);
//	public void handleBeingWorn(CMMsg msg);
//	public void handleBeingWielded(CMMsg msg);
//	public void handleBeingHeld(CMMsg msg);
	public void lookAtExits(Room room, MOB mob);
//	public void lookAtExitsShort(Room room, MOB mob);
	public boolean handleUnknownCommand(MOB mob, Vector command);
//	public void handleIntroductions(MOB speaker, MOB me, String said);
//	public void tickAging(MOB mob);
}
