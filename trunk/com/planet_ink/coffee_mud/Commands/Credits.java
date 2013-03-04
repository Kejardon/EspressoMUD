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
public class Credits extends StdCommand
{
	public Credits(){access=new String[]{"CREDITS"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		StringBuffer credits=new CMFile("resources/text/credits.txt",null,true).text();
		try { credits = CMLib.httpUtils().doVirtualPage(credits);}catch(Exception ex){}
		if((credits!=null)&&(mob.session()!=null)&&(credits.length()>0))
			mob.session().colorOnlyPrintln(credits.toString());
		else
		{
			mob.tell("CoffeeMud 5.6.2 is (C)2000-2010 by Bo Zimmerman");
			mob.tell("EspressoMUD is (C)2010-2012 by Kejardon");
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}
