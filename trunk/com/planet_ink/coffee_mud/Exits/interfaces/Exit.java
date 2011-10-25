package com.planet_ink.coffee_mud.Exits.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
 * An interface for all mechanisms or pathways through which a mob may
 * travel when trying to get from one Room to another.
 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
 */
public interface Exit extends Interactable, Closeable.CloseableHolder, CMModifiable, CMSavable
{
	public String directLook(MOB mob, Room destination);
	public String exitListLook(MOB mob, Room destination);
//	public String viewableText(MOB mob, Room destination);	//What was this supposed to be? Oh right it was from original code. Meh.
	public boolean visibleExit(MOB mob, Room destination);
	public void setVisible(boolean b);
	public String exitID();
	public void setExitID(String newID);
//	public boolean canGo(MOB mob, Room destination);

}
