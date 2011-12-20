package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
A small container object to hold things that require action ticks - ticks that are expected to
happen at a specific time rather than a typical time tick interval
*/
public class TockClient implements Comparable<TockClient>
{
	public final Tickable clientObject;
	public final long nextAction;

	public TockClient(Tickable newClientObject,
						long next)
	{
		clientObject=newClientObject;
		nextAction=next;
	}

	public boolean equals(Object obj)
	{
		if(obj instanceof TockClient)
			return compareTo((TockClient)obj)==0;
		return false;
	}

	public int compareTo(TockClient arg0)
	{
		if(clientObject != arg0.clientObject)
			return (int)(nextAction - arg0.nextAction);
		return 0;
	}
}