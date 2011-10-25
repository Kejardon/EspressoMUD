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
 * This interface is implemented by any object which wishes to get periodic thread time from
 * the threads engine.  Almost all CoffeeMud objects implement this interface
 * @author Bo Zimmerman
 *
 */
public interface Tickable extends CMObject
{
	public Tickable.TickStat getTickStatus();
	public boolean tick(Tickable ticking, TickID tickID);
	public long lastAct();
	public long lastTick();
//	public int actTimer();
//	public void setActT(int i);

	/** the number of miliseconds for each tick/round.*/
	public final static long TIME_TICK=4000;
	/** the number of milliseconds for each game-mud-hour */
	public final static long TIME_MILIS_PER_MUDHOUR=10*60000;
	/** the number of game/rounds for each real minute of time */
	public final static long TICKS_PER_RLMIN=(int)Math.round(60000.0/(double)TIME_TICK);
	/** TIME_TICK as a double */
	public final static double TIME_TICK_DOUBLE=(double)TIME_TICK;

	public enum TickID
	{
		Time, Action
	}
	public enum TickStat
	{
		Not, Start, Listener, End
	}
/*
	public final static int TICKMASK_SOLITARY=65536;
	public final static int TICKID_MOB=0;
	public final static int TICKID_ITEM_BEHAVIOR=1;
	public final static int TICKID_EXIT_REOPEN=2;
	public final static int TICKID_DEADBODY_DECAY=3;
	public final static int TICKID_LIGHT_FLICKERS=4;
	public final static int TICKID_TRAP_RESET=5;
	public final static int TICKID_TRAP_DESTRUCTION=6;
	public final static int TICKID_ITEM_BOUNCEBACK=7;
	public final static int TICKID_ROOM_BEHAVIOR=8;
	public final static int TICKID_AREA=9;
	public final static int TICKID_ROOM_ITEM_REJUV=10;
	public final static int TICKID_EXIT_BEHAVIOR=11;
	public final static int TICKID_SPELL_AFFECT=12;
	public final static int TICKID_READYTOSTOP=17;
	public final static int TICKID_LONGERMASK=256;
*/
/*
	public static long STATUS_NOT=0;
	public static long STATUS_START=1;
	public static long STATUS_CLASS=2;
	public static long STATUS_RACE=3;
	public static long STATUS_FIGHT=4;
	public static long STATUS_WEATHER=5;
	public static long STATUS_DEAD=6;
	public static long STATUS_ALIVE=7;
	public static long STATUS_REBIRTH=8;
	public static long STATUS_OTHER=98;
	public static long STATUS_END=99;
	public static long STATUS_BEHAVIOR=512;
	public static long STATUS_AFFECT=1024;
	public static long STATUS_MISC=4096;
	public static long STATUS_MISC2=8192;
	public static long STATUS_MISC3=16384;
	public static long STATUS_MISC4=32768;
	public static long STATUS_MISC5=65536;
	public static long STATUS_MISC6=131072;
	public static long STATUS_MISC7=131072*2;
*/
}
