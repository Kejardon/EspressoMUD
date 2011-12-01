package com.planet_ink.coffee_mud.Common.interfaces;
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
import java.io.*;

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
public interface Environmental extends Affectable, CMModifiable, CMSavable, CMCommon
{
	public static interface EnvHolder extends ListenHolder { public Environmental getEnvObject(); }

	public boolean sameAs(Environmental E);
	public EnvStats baseEnvStats();
	public void setBaseEnvStats(EnvStats newBaseEnvStats);
	public EnvStats envStats();
	public void recoverEnvStats();
	public void destroy();
	public static class O
	{
		public static Environmental getFrom(CMObject O)
		{
			if(O instanceof Environmental) return (Environmental)O;
			while(O instanceof Ownable) O=((Ownable)O).owner();
			if(O instanceof EnvHolder) return ((EnvHolder)O).getEnvObject();
			return null;
		}
	}
}
