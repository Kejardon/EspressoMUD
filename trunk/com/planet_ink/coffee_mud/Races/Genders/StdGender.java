package com.planet_ink.coffee_mud.Races.Genders;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class StdGender implements Gender
{
	public String ID(){return "StdGender";}
	public CMObject newInstance(){return this;}
	public CMObject copyOf(){return this;}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String name(){return "NA";}
//	public char letter();
	public String subject(){return "std";}
	public String object(){return "std";}
	public String possessive(){return "stds";}
//	public String HeShe();
	public String sirmadam(){return "std";}
}