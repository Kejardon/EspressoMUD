package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public interface Light extends Item
{
	public void setDuration(int duration);
	public int getDuration();
	public boolean destroyedWhenBurnedOut();
	public void setDestroyedWhenBurntOut(boolean truefalse);
	public boolean goesOutInTheRain();
	public boolean isLit();
	public void light(boolean isLit);
	
}
