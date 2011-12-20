package com.planet_ink.coffee_mud.Behaviors;
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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class ActiveTicker extends StdBehavior
{
	public String ID(){return "ActiveTicker";}

	protected int minTicks=10;
	protected int maxTicks=30;
	protected int chance=100;
	protected int tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;

	protected void tickReset()
	{
		tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;
	}

	public void setParms(String newParms)
	{
		parms=newParms;
		minTicks=CMParms.getParmInt(parms,"min",minTicks);
		maxTicks=CMParms.getParmInt(parms,"max",maxTicks);
		chance=CMParms.getParmInt(parms,"chance",chance);
		tickReset();
	}

	public String rebuildParms()
	{
		StringBuffer rebuilt=new StringBuffer("");
		rebuilt.append(" min="+minTicks);
		rebuilt.append(" max="+maxTicks);
		rebuilt.append(" chance="+chance);
		return rebuilt.toString();
	}
	
	public String getParmsNoTicks()
	{
		String parms=getParms();
		char c=';';
		int x=parms.indexOf(c);
		if(x<0){ c='/'; x=parms.indexOf(c);}
		if(x>0)
		{
			if((x+1)>parms.length())
				return "";
			parms=parms.substring(x+1);
		}
		else
		{
			return "";
		}
		return parms;
	}

	protected boolean canAct(Tickable ticking, int tickID)
	{
		return ((--tickDown<1)&&(CMLib.dice().rollPercentage()<=chance));
	}
}
