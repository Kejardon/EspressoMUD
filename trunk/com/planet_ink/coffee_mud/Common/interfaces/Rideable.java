package com.planet_ink.coffee_mud.Common.interfaces;
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
@SuppressWarnings("unchecked")
public interface Rideable extends CMObject, CMModifiable, CMSavable, CMCommon //on second thought not Environmental. Let's have this mimic ItemCollection.
{
	public static interface RideHolder extends CMObject { public Rideable getRideObject(); }
	//TODO GENERAL NOTE:
	//When the ride moves it should move all riders on it automatically (late response).
	//When a mob moves the rider should have a (mid) response to remove the rider.
	public boolean isMobileRide();
	public void setMobileRide(boolean mob);
	public boolean canBeRidden(Item E);
	public boolean hasRider(Item mob);
	public void addRider(Item mob);
	public void removeRider(Item mob);
	public Item removeRider(int i);
	public Item getRider(int which);
	public Vector<Item> allRiders();
	public int numRiders();
	public int rideNumber();
	public void setRideNumber(int i);
	public int saveNumber();
	/* Returns a string grammatically correct for the given rider when
	 * they are mounted on this Rideable */
	public String stateString(Item R);
	public void setStateString(String S);
	/* Returns a string grammatically correct for the given rider when
	 * they are putting something on this Rideable */
	public String putString(Item R);
	public void setPutString(String S);
	/* Returns a string grammatically correct for this Rideable when
	 * Riders are mounted */
	public String stateStringSubject(Item R);
	public void setStateStringSubject(String S);
	/* Returns a string grammatically correct for the given rider when
	 * they are mounting this Rideable */
	public String mountString(Item R);
	public void setMountString(String S);
	/* Returns a string grammatically correct for the given rider when
	 * they are dismounting this Rideable */
	public String dismountString(Item R);
	public void setDismountString(String S);

	public static class RideThing
	{
		private static int rideNumber=1;
		private static boolean started=false;
		public synchronized static int getNumber()
		{
			//TODO: Finish this up once DB interface is better set up! Load from Misc, "Rideable", get an int.
			if(!started)
			{
				started=true;
				rideNumber=1;
			}
			return rideNumber++;
		}
//		public static void setNumber(int i){rideNumber=i;}
		public static void shutdown()
		{
			//TODO: Finish this up once DB interface is better set up! Write to Misc, "Rideable", set an int.
			//Also set up a library to call this on shutdown!
			
		}
		public static Rideable getFrom(CMObject O)
		{
			if(O instanceof Rideable) return (Rideable)O;
			while(O instanceof Ownable) O=((Ownable)O).owner();
			if(O instanceof RideHolder) return ((RideHolder)O).getRideObject();
			return null;
		}
	}
}
