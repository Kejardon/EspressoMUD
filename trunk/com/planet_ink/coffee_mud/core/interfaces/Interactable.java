package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//A thing that players can directly observe and interact with in the MUD
public interface Interactable extends Environmental.EnvHolder, Affectable, Behavable
{
	public String name();
	public void setName(String newName);
	public String displayText();
	public void setDisplayText(String newDisplayText);
	public String description();
	public void setDescription(String newDescription);
	public void destroy();
	public boolean amDestroyed();
	public boolean sameAs(Interactable E);
}