package com.planet_ink.coffee_mud.core.interfaces;
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
public interface TickActer extends CMObject, Runnable
{
	public static final TickActer[] dummyTickActerArray=new TickActer[0];
	public Tickable.TickStat getActStatus();
	//public void tickAct();	//use public void run() instead. Not really ideal, but best option available basically
	public long nextAct();
	//public void setTickThread(Tick tick);
//	public long lastAct();
//	public int lastTick();
//	public int actTimer();
//	public void setActT(int i);

	/*public enum TickStat
	{
		Not, Start, Listener, End
	}*/

/*	Typical TickActer code
	protected long nextAct=0;
	protected Tickable.TickStat actStatus=Tickable.TickStat.Not;

	public Tickable.TickStat getActStatus(){return actStatus;}
	public void run()
	{
		
	}
	public long nextAct(){return nextAct;}
*/
}
