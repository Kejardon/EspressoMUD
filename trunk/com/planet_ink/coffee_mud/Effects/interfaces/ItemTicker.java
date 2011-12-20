package com.planet_ink.coffee_mud.Effects.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * The interface for affects which cause an Item
 * to rejuvinate after a particular amount of time.  This interface
 * also allows the system to "source" an item back to its originating
 * room.
 * Items do not usually have tick services, so this affect ticks in
 * an items stead to allow it to rejuvinate.
 */
public interface ItemTicker extends Effect
{
	/**
	 * Registers the given item as being from the given room.  It will
	 * read the items envStats().rejuv() value and use it as an interval
	 * for checking to see if this item is no longer in its originating
	 * room.  If so, it will create a copy of it in the originating room.
	 * @param item the item to rejuvinate
	 * @param room the room which the item is from
	 */
	public void loadMeUp(Item item, Room room);
	
	/**
	 * Removes the rejuvinating ticker from an item.  This
	 * is done when a room is resetting its content, and this
	 * item is no longer to be used as a source for rejuvination.
	 * @param item
	 */
	public void unloadIfNecessary(Item item);
	
	/**
	 * Returns the room where this item belongs
	 * @return a Room object
	 */
	public Room properLocation();
	
	/**
	 * Sets the room where this item belongs
	 * @param room a room object
	 */
	public void setProperLocation(Room room);
}
