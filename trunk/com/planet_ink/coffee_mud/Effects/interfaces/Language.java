package com.planet_ink.coffee_mud.Effects.interfaces;
import com.planet_ink.coffee_mud.Effects.StdEffect;
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
   Copyright 2008-2010 Bo Zimmerman

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
 * A Language ability represents both the ability to understand one or more
 * spoken or written languages, and the ability to speak one or more spoken
 * languages.  A single ability usually represents a single language, but 
 * may support multiple simultaneously.
 */
 /*
 TODO: Make languages their own thing instead of an effect. Have them like commands, where there's a
 single instance of the language that everything references. Need to repoint CMClass's loader from
 Effects\Languages to new folder, otherwise nothing besides languages point to these atm.
 */
@SuppressWarnings("unchecked")
public interface Language extends Effect
{
	public String writtenName();
	public Vector<String> languagesSupported();
	public boolean translatesLanguage(String language);
	public int getProficiency(String language);
	public boolean beingSpoken(String language);
	public void setBeingSpoken(String language, boolean beingSpoken);
	// Returns the direct word<->word translation hashtable
	public Hashtable translationHash(String language);
	// Returns the word-length rough-translation vector of string arrays for the given language
	public Vector translationVector(String language);
	// Returns a language translation of the given word in the given language
	public String translate(String language, String word);
}
