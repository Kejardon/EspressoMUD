package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
public interface GenericEditor extends CMLibrary
{
	public CMObject genMiscSet(MOB mob, CMModifiable E);

	public void ashortPrompt(MOB mob, short[] values);
	public short shortPrompt(MOB mob, String defaultTo);
	public void aintPrompt(MOB mob, int[] values);
	public int intPrompt(MOB mob, String defaultTo);
	public void alongPrompt(MOB mob, long[] values);
	public long longPrompt(MOB mob, String defaultTo);
	public void adoublePrompt(MOB mob, double[] values);
	public double doublePrompt(MOB mob, String defaultTo);
	public void abooleanPrompt(MOB mob, boolean[] values);
	public boolean booleanPrompt(MOB mob, String defaultTo);
	public void astringPrompt(MOB mob, String[] values, boolean allowNull);
	public String stringPrompt(MOB mob, String defaultTo, boolean allowNull);
	public void acharPrompt(MOB mob, char[] values);
	public char charPrompt(MOB mob, String defaultTo);
	public Enum enumPrompt(MOB M, String defaultTo, Enum[] options); //IMPORTANT: ALL CALLS TO THIS MUST HAVE UPPERCASE ENUM OPTIONS.
	public void enumSetPrompt(MOB M, Enum[] options, EnumSet set); //IMPORTANT: ALL CALLS TO THIS MUST HAVE UPPERCASE ENUM OPTIONS.

	public void modBehavable(Behavable E, MOB M);
	public void modAffectable(Affectable E, MOB M);
	public int promptVector(MOB mob, Vector V, boolean newOption);
	public Area areaPrompt(MOB M);
	public Effect newAnyEffect(MOB mob);
	public Effect newAnyEffect(MOB mob, Vector<Effect> notThese);
	public Behavior newAnyBehavior(MOB mob);
	public Behavior newAnyBehavior(MOB mob, Vector<Behavior> notThese);
	public Item newAnyItem(MOB mob);
	public Closeable newAnyCloseable(MOB mob);
}
