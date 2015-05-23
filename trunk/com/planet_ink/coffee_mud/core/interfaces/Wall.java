package com.planet_ink.coffee_mud.core.interfaces;

/*
EspressoMUD copyright 2015 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
	Walls are thick barriers on the perimeters of rooms. There are not other rooms behind the wall (otherwise it'd be an exit instead)
*/
public interface Wall extends Interactable, CMSavable, CMModifiable
{
	public Room room();
	public void setRoom(Room R);
}
