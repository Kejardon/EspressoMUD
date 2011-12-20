package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//Behavables are things that may have some sort of AI-like behavior
public interface Behavable extends ListenHolder, ListenHolder.TickActer
{
	public void addBehavior(Behavior to);
	public void delBehavior(Behavior to);
	public int numBehaviors();
	public Behavior fetchBehavior(int index);
	public Vector<Behavior> fetchBehavior(String ID);
	public Vector<Behavior> allBehaviors();
}