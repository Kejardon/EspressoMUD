package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/**
 * The interface for all common items, and as a base for RawMaterial, armor, weapons, etc.
 *
 * @author Bo Zimmerman
 *
 */
public interface Item extends Interactable, CMSavable, CMModifiable
{
	public boolean damagable();
	public void setDamagable(boolean bool);
	public int wornOut();	//Start at 0 and go up to 10000. At 10000 destroy, at say 2000 or so it shouldn't really be usable anyways
	public void setWornOut(int worn);
	public int value();
	public int baseGoldValue();
	public void setBaseValue(int newValue);
	public int recursiveWeight();
	
	public void setMiscText(String newMiscText);
	public String text();
	public CMObject container();
	public void setContainer(CMObject E);
	public int ridesNumber();
	public Rideable ride();
	public void setRide(Rideable R);
}
