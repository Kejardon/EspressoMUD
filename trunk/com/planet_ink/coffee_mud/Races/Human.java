package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Human extends CarbonBased
{
	public String ID(){	return "Human"; }
	public String name(){ return "Human"; }
	public String racialCategory(){return "Human";}
	public int availabilityCode(){return 1;}
	public Human()
	{
		myGenders=new Gender[2];
		myGenders[0]=CMClass.GENDER.get("Male");
		myGenders[1]=CMClass.GENDER.get("Female");
	}
}
