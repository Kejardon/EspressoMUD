/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface ExitInstance extends Interactable, CMSavable, CMModifiable //Item?
{
	public Exit getExit();
}
