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
public class Replay extends StdCommand
{
	public Replay(){access=new String[]{"REPLAY"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Session S=mob.session();
		if(S==null) return false;
		int num=Session.MAX_PREVMSGS;
		if(commands.size()>1) num=CMath.s_int(CMParms.combine(commands,1));
		if(num<=0) return false;
		LinkedList<String> last=S.getLastMsgs();
		if(num>last.size()) num=last.size();
		for(ListIterator<String> iter=last.listIterator(last.size()-num);iter.hasNext();)
			S.onlyPrint(iter.next()+"\r\n",true);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}
