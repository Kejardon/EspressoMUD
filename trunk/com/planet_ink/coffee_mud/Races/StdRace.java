package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class StdRace implements Race
{
	public static final EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);

	public String ID(){	return "StdRace"; }
	public String name(){ return "StdRace"; }
	public String racialCategory(){return "Unknown";}

	public HashMap<String, Body.BodyPart> bodyMap(){return null;}

	public CMObject newInstance(){return this;}
	public void initializeClass(){}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { }
	public void registerAllListeners(){}
	public void clearAllListeners(){}

	protected static final Vector empty=new Vector();
	protected String baseStatChgDesc = "";
//	protected String sensesChgDesc = null;
//	protected String dispChgDesc = null;
//	protected String abilitiesDesc = null;
	protected String languagesDesc = "";

	public int availabilityCode(){return -1;}

	public int fertile(String S){return -100;}

	public CMObject copyOf()
	{
		try
		{
			StdRace E=(StdRace)this.clone();
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
	}
	public void affectCharStats(MOB affectedMob, CharStats charStats)
	{
	}
	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		return true;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String getStatAdjDesc()
	{
		return baseStatChgDesc;
	}
/*
	public String getSensesChgDesc()
	{
		makeStatChgDesc();
		return sensesChgDesc;
	}
	public String getDispositionChgDesc()
	{
		makeStatChgDesc();
		return dispChgDesc;
	}
	public String getAbilitiesDesc()
	{
		makeStatChgDesc();
		return abilitiesDesc;
	}
*/
	public String getLanguagesDesc()
	{
		return languagesDesc;
	}
	protected void makeStatChgDesc()
	{
	}

	public boolean sameAs(Race E)
	{
		if(!(E instanceof StdRace)) return false;
		return true;
	}
}
