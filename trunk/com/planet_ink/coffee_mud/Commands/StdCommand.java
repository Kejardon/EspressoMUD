package com.planet_ink.coffee_mud.Commands;
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
@SuppressWarnings("unchecked")
public class StdCommand implements Command
{
	public StdCommand(){}
	protected String ID=null;
	public String ID()
	{
		if(ID==null){
			ID=this.getClass().getName();
			int x=ID.lastIndexOf(".");
			if(x>=0) ID=ID.substring(x+1);
		}
		return ID;
	}

	//TODO: Reprogram subclasses to initialize access, not declare access.
	private String[] access=new String[0];
	public String[] getAccessWords(){return access;}
	public void initializeClass(){}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		// accepts the mob executing, and a Vector of Strings as a parm.
		// the return value is arbitrary, though false is conventional.
		return false;
	}
	public boolean preExecute(MOB mob, Vector commands, int metaFlags, int secondsElapsed, double actionsRemaining)
		throws java.io.IOException
	{
		return true;
	}

	public double actionsCost(MOB mob, Vector cmds){return 0.0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return true;}
	public boolean staffCommand(){return false;}
	public CMObject newInstance(){return this;}
	public CMObject copyOf()
	{
		try
		{
			Object O=this.clone();
			return (CMObject)O;
		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}
	
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
