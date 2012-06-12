package com.planet_ink.coffee_mud.Effects.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface Effect extends ListenHolder.AllListener, CMModifiable, CMSavable, Affectable
{
	public static final Flags[] dummyEFlagsArray=new Flags[0];
	public EnumSet<Flags> effectFlags();
	public Affectable affecting();
	public void setAffectedOne(Affectable being);
	public boolean invoke(Affectable target, int asLevel);
	public void unInvoke();
	public void startTickDown(Affectable affected, int tickTime);
	public Effect copyOnto(Affectable being);	//Should this have int asLevel? Not for now

	public enum Flags
	{ Blessing, Curse, Poison, Drug, Magic, Natural }
}
