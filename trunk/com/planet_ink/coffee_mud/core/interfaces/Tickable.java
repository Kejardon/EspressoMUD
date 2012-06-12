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

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * This interface is implemented by any object which wishes to get periodic thread time from
 * the threads engine.  Almost all CoffeeMud objects implement this interface
 * @author Bo Zimmerman
 */
public interface Tickable extends CMObject
{
	public Tickable.TickStat getTickStatus();
	public boolean tick(int tickTo);
	public int tickCounter();
//	public long lastAct();
//	public int lastTick();
//	public int actTimer();
//	public void setActT(int i);

	/** the number of miliseconds for each tick/round.*/
	public final static long TIME_TICK=4000;

	public final static long TIME_TICKS_PER_MUDHOUR=150;
	public final static long TIME_MILIS_PER_MUDHOUR=TIME_TICKS_PER_MUDHOUR*TIME_TICK;
	//One set of these should be commented out
	//public final static long TIME_MILIS_PER_MUDHOUR=10*60000;
	//public final static long TIME_TICKS_PER_MUDHOUR=TIME_MILIS_PER_MUDHOUR/4000;

	/** the number of game/rounds for each real minute of time */
	public final static int TICKS_PER_RLMIN=(int)Math.round(60000.0/(double)TIME_TICK);
	/** TIME_TICK as a double */
	public final static double TIME_TICK_DOUBLE=(double)TIME_TICK;
/*	public enum TickID
	{
		Time, Action
	} */
	public enum TickStat
	{
		Not, Start, Listener, End
	}

/*	Typical Tickable code
	protected int tickCount=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;

	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		else if(tickTo>tickCount+10)
		{
			Log.errOut("Tick", ID()+((this instanceof CMSavable)?(" "+((CMSavable)this).saveNum()+" "):" ")+"is trying to tick from "+tickCount+" to "+tickTo);
			tickTo=tickCount+10;
		}
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {tickCount=0; return false;}	//Possibly also lFlags.remove(ListenHolder.Flags.TICK);
		}
		return true;
	}
	public int tickCounter(){return tickCount;}
	public void tickAct(){}
	protected boolean doTick(){return true;}
*/
}
