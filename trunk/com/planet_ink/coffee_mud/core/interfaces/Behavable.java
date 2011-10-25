package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;

import java.util.*;
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
public interface Behavable extends ListenHolder, ListenHolder.TickActer{
	public void addBehavior(Behavior to);
	public void delBehavior(Behavior to);
	public int numBehaviors();
	public Behavior fetchBehavior(int index);
	public Vector<Behavior> fetchBehavior(String ID);
	public Vector<Behavior> allBehaviors();
	//The below miiiight be obsoleted by things having a list of flags for what messages they're concerned with, which includes ticking.
//	public void setTicking(boolean ticking);
//	public boolean ticking();
//	public void checkTicking();
}
