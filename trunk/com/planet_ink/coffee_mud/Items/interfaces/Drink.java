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
public interface Drink extends Item
{
/*
	TODO eventually.
	Also a note: The internal WVector should be synchronized when handled, and only give out clones of itself, not its actual self.
	public WVector ingredients();
	public void removeIngredient(Resource type, boolean andNourishment);
	public void addIngredient(Resource type, int amount);
	public void emptyDrink();
*/
	public int capacity();
	public void setCapacity(int amount);
	public int nourishment();
	public void setNourishment(int amount);
	public int bite();
	public void setBite(int amount);
}
