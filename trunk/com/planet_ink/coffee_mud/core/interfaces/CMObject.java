package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;

import java.util.concurrent.CopyOnWriteArrayList;
/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * The general base interface which is implemented by every class
 * which the CoffeeMud ClassLoader (CMClass) handles.
 * @see com.planet_ink.coffee_mud.core.CMClass
 * @author Bo Zimmerman
 */
public interface CMObject extends Comparable<CMObject>, Cloneable
{
	public static final CMObject[] dummyCMOArray=new CMObject[0];

	public CMObject newInstance();
	public CMObject copyOf();
	public String ID();
	public void initializeClass();
}
