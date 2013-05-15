package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import java.util.concurrent.CopyOnWriteArrayList;

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
	//TODO: These no longer make sense with destination, they should probably have ExitInstance myInstance instead.
	public String directLook(MOB mob, Room destination);
	public String exitListLook(MOB mob, Room destination);
//	public String viewableText(MOB mob, Room destination);	//What was this supposed to be? Oh right it was from original code. Meh.
	public boolean visibleExit(MOB mob, Room destination);
	public void setVisible(boolean b);
	public ExitInstance makeInstance(Room source, Room destination);
	public void removeInstance(ExitInstance myInstance, boolean both);
	public ExitInstance oppositeOf(ExitInstance myInstance, Room destination);
	public void linkMe(ExitInstance myInstance);
	//public boolean removeExitFrom(Room source, Room destination, CopyOnWriteArrayList<ExitInstance> list);
//	public String exitID();
//	public void setExitID(String newID);
//	public void setSave(boolean b);
//	public boolean needSave();
//	public boolean canGo(MOB mob, Room destination);
}
