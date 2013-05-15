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
public class Female extends StdGender
{
	public String ID(){return "Female";}

	public String name(){return "female";}
//	public char letter();
	public String subject(){return "she";}
	public String object(){return "her";}
	public String possessive(){return "her";}
//	public String HeShe();
	public String sirmadam(){return "madam";}
}
