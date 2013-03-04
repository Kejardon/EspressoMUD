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
@SuppressWarnings("unchecked")
public interface SessionsList extends CMLibrary, Runnable
{
	public final static long sleepTime=50;	//Check if there is input from users 20 times a second

	public Vector all=new Vector();
	public Session elementAt(int x);
	public int size();
	public void addElement(Session S);
	public void removeElementAt(int x);
	public void removeElement(Session S);
	public Session[] toArray();
	public void stopSessionAtAllCosts(Session S);
	public Session findPlayerOnline(String srchStr, boolean exactOnly);
	//public Enumeration sessions();
}