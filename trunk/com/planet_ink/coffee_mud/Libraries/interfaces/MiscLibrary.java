package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
Tasks assigned to this library:
	Handle saving and/or loading data for special things
		SIDLib
*/
public interface MiscLibrary extends CMLibrary, CMSavable, Runnable
{
	public TimeClock globalClock();
}
