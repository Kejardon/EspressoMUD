package com.planet_ink.coffee_mud.Libraries.interfaces;
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

import com.planet_ink.coffee_mud.Libraries.MUDZapper;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface HelpLibrary extends CMLibrary
{
	public Vector getTopics(boolean archonHelp, boolean standardHelp);
	public String fixHelp(String tag, String str, MOB forMOB);
	public String getHelpText(String helpStr, MOB forMOB, boolean favorAHelp);
	public String getHelpText(String helpStr, MOB forMOB, boolean favorAHelp, boolean noFix);
	public String getHelpText(String helpStr, Properties rHelpFile, MOB forMOB);
	public String getHelpList(String helpStr,  Properties rHelpFile1, Properties rHelpFile2, MOB forMOB);
	public String getHelpText(String helpStr, Properties rHelpFile, MOB forMOB, boolean noFix);
	public Properties getArcHelpFile();
	public Properties getHelpFile();
	public void unloadHelpFile(MOB mob);
	public void addHelpEntry(String ID, String text, boolean archon);
}
