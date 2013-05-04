package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

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
	public static interface EnvHolder extends ListenHolder
	{
		public static final EnvHolder[] dummyEHArray=new EnvHolder[0];
		public Environmental getEnvObject();
	}

	public boolean sameAs(Environmental E);
	public EnvStats baseEnvStats();
	public void setBaseEnvStats(EnvStats newBaseEnvStats);
	public EnvStats envStats();
	public int temperature();
	public void setTemperature(int newTemp);
	public void recoverEnvStats();
	public void destroy();
	public static class O
	{
		public static Environmental getFrom(CMObject O)
		{
			if(O instanceof Environmental) return (Environmental)O;
			if(O instanceof EnvHolder) return ((EnvHolder)O).getEnvObject();
			while((O instanceof Ownable)&&((Ownable)O).owner()!=O) {
				O=((Ownable)O).owner();
				if(O instanceof Environmental) return (Environmental)O;
				if(O instanceof EnvHolder) return ((EnvHolder)O).getEnvObject(); }
			return null;
		}
	}
}
