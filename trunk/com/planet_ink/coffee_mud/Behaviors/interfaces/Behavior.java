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
 * something that is affected by, or affects
 * the environment around them.
 */
/**
 * A Behavior is a pro-active modifier of Environmental objects.  Behaviors
 * are expected to do their work in a Tickable.tick(Tickable,int) method which
 * is called periodically by either the host object, or the serviceengine.
 * Behaviors are also message listeners however, and can overlap Ability/properties
 * in that way.
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
 * @see com.planet_ink.coffee_mud.core.interfaces.Tickable
 */
@SuppressWarnings("unchecked")
public interface Behavior extends ListenHolder.AllListener, CMModifiable, CMSavable
{
	public void startBehavior(Behavable forMe);
	public Behavable behaver();

	public String getParms();
	public void setParms(String parameters);

}
