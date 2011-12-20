package com.planet_ink.coffee_mud.Libraries.interfaces;

import java.util.Enumeration;
import java.util.Vector;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface SessionsList extends CMLibrary, Runnable
{
    public Vector all=new Vector();
    public Session elementAt(int x);
    public int size();
    public void addElement(Session S);
    public void removeElementAt(int x);
    public void removeElement(Session S);
    public void stopSessionAtAllCosts(Session S);
    public Session findPlayerOnline(String srchStr, boolean exactOnly);
    public Enumeration sessions();
}
