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
import com.planet_ink.coffee_mud.Libraries.CMMap;
import com.planet_ink.coffee_mud.Libraries.CoffeeUtensils;
import com.planet_ink.coffee_mud.Libraries.Sense;
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
public interface CMMiscUtils extends CMLibrary
{
	public static final int LOOTFLAG_RUIN=1;
	public static final int LOOTFLAG_LOSS=2;
	public static final int LOOTFLAG_WORN=4;
	public static final int LOOTFLAG_UNWORN=8;
	
	public String builtPrompt(MOB mob);
	
	public String getFormattedDate(Interactable E);
	public String niceCommaList(Vector V, boolean andTOrF);
	
	public boolean reachableItem(MOB mob, Interactable E);
//	public double memoryUse ( Interactable E, int number );
//	public void extinguish(MOB source, Environmental target, boolean mundane);
//	public boolean armorCheck(MOB mob, int allowedArmorLevel);
//	public boolean armorCheck(MOB mob, Item I, int allowedArmorLevel);
//	public void recursiveDropMOB(MOB mob, Room room, Item thisContainer, boolean bodyFlag);
//	public void confirmWearability(MOB mob);
	
//	public MOB getMobPossessingAnother(MOB mob);
//	public Vector getDeadBodies(Environmental container);
//	public boolean resurrect(MOB tellMob, Room corpseRoom, DeadBody body, int XPLevel);
	
//	public Item isRuinedLoot(DVector policies, Item I);
//	public DVector parseLootPolicyFor(MOB mob);

//	public void swapRaces(Race newR, Race oldR);
}
