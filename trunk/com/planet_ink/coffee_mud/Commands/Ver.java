package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Ver extends StdCommand
{
	public Ver(){access=new String[]{"VERSION","VER"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		mob.tell("CoffeeMud v5.6.2");
		mob.tell("(C) 2000-2010 Bo Zimmerman");
		mob.tell("^<A HREF=\"mailto:bo@zimmers.net\"^>bo@zimmers.net^</A^>");
		mob.tell("^<A HREF=\"http://www.coffeemud.org\"^>http://www.coffeemud.org^</A^>");
		mob.tell("EspressoMUD v"+CMProps.Strings.MUDVER.property());
		mob.tell("(C) 2010-2012 Kejardon");
		mob.tell("^<A HREF=\"http://sourceforge.net/projects/espress/\"^>http://sourceforge.net/projects/espress/^</A^>");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}
