package com.planet_ink.coffee_mud.Behaviors.interfaces;
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
import java.util.Vector;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface Behavior extends ListenHolder.AllListener, CMModifiable, CMSavable
{
	public void startBehavior(Behavable forMe);
	public Behavable behaver();

	public String getParms();
	public void setParms(String parameters);
	public static class O
	{
		private static int saveNumber=1;
		private static boolean started=false;
		private static HashMap<Integer, Behavior> assignedNumbers=new HashMap<Integer, Behavior>();
		public synchronized static int getNumber()
		{
			if(!started)
			{
				String S=CMLib.database().DBReadData("BehavSNum");
				if(S==null)
				{
					saveNumber=1;
					CMLib.database().DBCreateData("BehavSNum","1");
				}
				else
					saveNumber=CMath.s_int(S);
				started=true;
			}
			if(assignedNumbers.containsKey(saveNumber))
			{
				int inc=1;
				while(assignedNumbers.containsKey(saveNumber+inc))
				{
					inc=inc*2;
					if(inc==1) saveNumber+=1580030169; //(2^32)/e ; optimal interval for poking around randomly
				}
				saveNumber+=inc;
			}
			return saveNumber++;
		}
		public static void save()
		{
			CMLib.database().DBUpdateData("BehavSNum",""+saveNumber);
		}
		public static void assignNumber(int i, Behavior A)
		{
			assignedNumbers.put(i, A);
		}
		public static void removeNumber(int i, Behavior A)
		{
			assignedNumbers.remove(i, A);
		}
	}

}
