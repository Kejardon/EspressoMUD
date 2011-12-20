package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Effects.interfaces.Effect;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//Affectables are things that may be modified by Effects
public interface Affectable extends ListenHolder, ListenHolder.MsgListener, ListenHolder.TickActer //, ListenHolder.AllListener
{
	public void addEffect(Effect to);
	public void delEffect(Effect to);
	public int numEffects();
	public Effect fetchEffect(int index);
	public Vector<Effect> fetchEffect(String ID);
	public Vector<Effect> allEffects();
}
