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
public class Visible extends StdCommand
{
    public Visible(){}

    private String[] access={"VISIBLE","VIS"};
    public String[] getAccessWords(){return access;}
    
    public static Vector returnOffensiveAffects(Environmental fromMe)
    {
        MOB newMOB=CMClass.getMOB("StdMOB");
        Vector offenders=new Vector();
        for(int a=0;a<fromMe.numEffects();a++)
        {
            Ability A=fromMe.fetchEffect(a);
            if((A!=null)&&(A.canBeUninvoked()))
            {
                try
                {
                    newMOB.recoverEnvStats();
                    A.affectEnvStats(newMOB,newMOB.envStats());
                    if(CMLib.flags().isInvisible(newMOB)||CMLib.flags().isHidden(newMOB))
                      offenders.addElement(A);
                }
                catch(Exception e)
                {}
            }
        }
        newMOB.destroy();
        return offenders;
    }

    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        String str="Prop_WizInvis";
        Ability A=mob.fetchEffect(str);
        boolean didSomething=false;
        if(A!=null)
        {
            Command C=CMClass.getCommand("WizInv");
            if((C!=null)&&(C.securityCheck(mob)))
            {
                didSomething=true;
                C.execute(mob,CMParms.makeVector("WIZINV","OFF"),metaFlags);
            }
        }
        Vector V=returnOffensiveAffects(mob);
        if(V.size()==0)
        {
            if(!didSomething)
            mob.tell("You are not invisible or hidden!");
        }
        else
        for(int v=0;v<V.size();v++)
            ((Ability)V.elementAt(v)).unInvoke();
        return false;
    }
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
    public double combatActionsCost(MOB mob, Vector cmds){return 0.25;}
    public boolean canBeOrdered(){return true;}
    
}
