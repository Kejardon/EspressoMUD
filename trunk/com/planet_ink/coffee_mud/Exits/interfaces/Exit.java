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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
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
	public void setSave(boolean b);
	public boolean needSave();
//	public boolean canGo(MOB mob, Room destination);
}
