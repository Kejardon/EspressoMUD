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
public interface CMLibrary extends CMObject
{
	//This is called for all at the same time, just about the last thing. All classes will have been loaded, only thing
	//of interest after is setting up startup rooms and enabling login connections
	public boolean activate();
	public boolean shutdown();
	//This is called early early by CMLIB, before libraries are even registered... except since none are registered
	//none get called by it. Not that it matters as none of them do anything at the moment. TODO
	public void propertiesLoaded();
	public ThreadEngine.SupportThread getSupportThread();
	public void finalInitialize();
}
