package com.planet_ink.coffee_mud.core.interfaces;
/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//A subcontainer object that holds and handles a common type of information in place of its owner
public interface Ownable extends CMObject
{
	public CMSavable owner();
	public Ownable setOwner(CMSavable newOwner);
	
	public static class O
	{
		public static CMObject getOwnerFrom(CMObject obj)
		{
			while(obj instanceof Ownable) obj=((Ownable)obj).owner();
			return obj;
		}
	}
}