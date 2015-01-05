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

public class Male extends StdGender
{
	@Override public String ID(){return "Male";}

	public String name(){return "male";}
//	public char letter();
	public String subject(){return "he";}
	public String object(){return "him";}
	public String possessive(){return "his";}
//	public String HeShe();
	public String sirmadam(){return "sir";}
}
