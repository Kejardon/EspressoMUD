package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import java.util.Vector;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public interface Behavior extends ListenHolder.AllListener, CMModifiable, CMSavable
{
	public void startBehavior(Behavable forMe);
	public Behavable behaver();

	@Override public Behavior copyOf();
	@Override public Behavior newInstance();
	
	public String getParms();
	public void setParms(String parameters);
}
