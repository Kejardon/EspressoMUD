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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
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
			while((O instanceof Ownable)&&((Ownable)O).owner()!=O) O=((Ownable)O).owner();
			if(O instanceof EnvHolder) return ((EnvHolder)O).getEnvObject();
			return null;
		}
	}
}
