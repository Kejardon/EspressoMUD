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
public class Logoff extends StdCommand
{
	public Logoff(){access=new String[]{"LOGOFF","LOGOUT"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(!mob.isMonster())
		{
			Session session=mob.session();
/*
			if((session!=null)
			&&(session.getLastPKFight()>0)
			&&((System.currentTimeMillis()-session.getLastPKFight())<(5*60*1000)))
			{
				mob.tell("You must wait a few more minutes before you are allowed to logout.");
				return false;
			}
*/
			try
			{
				if ((session != null)&& (session.confirm("\r\nLogout -- are you sure (y/N)?","N")))
				{
					CMMsg msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.QUIT),null);
					Room R=mob.location();
					if((R!=null)&&(R.okMessage(mob,msg))) 
					{
						CMLib.map().sendGlobalMessage(mob,EnumSet.of(CMMsg.MsgCode.QUIT), msg);
						session.logout();
					}
					msg.returnMsg();
				}
			}
			catch(Exception e)
			{
				Log.errOut("Logoff",e.getMessage());
			}
		}
		return false;
	}

	public int prompter(){return 2;}
	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return false;}
}