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
public class StdBehavior extends CMModifiable.DummyCMMod implements Behavior
{
	public String ID(){return "StdBehavior";}
	public String name(){return ID();}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return 0;}
	public boolean grantsAggressivenessTo(MOB M){return false;}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public void initializeClass(){}
	public int priority(){return Integer.MAX_VALUE;}
	public void registerListeners(Environmental forThis){}
	public void affectEnvStats(Environmental affectedEnv, EnvStats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	protected boolean isSavableBehavior=true;

	public StdBehavior()
	{
		super();
	}

	protected String parms="";

	/** return a new instance of the object*/
	public CMObject newInstance()
	{
		try
		{
			return (Behavior)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdBehavior();
	}
	public CMObject copyOf()
	{
		try
		{
			Behavior B=(Behavior)this.clone();
			B.setParms(getParms());
			return B;
		}
		catch(CloneNotSupportedException e)
		{
			return new StdBehavior();
		}
	}
	public void startBehavior(Environmental forMe){}
	protected void finalize(){}
	public void setSavable(boolean truefalse){isSavableBehavior=truefalse;}
	public boolean isSavable(){return isSavableBehavior;}
	protected MOB getBehaversMOB(Tickable ticking)
	{
		if(ticking==null) return null;

		if(ticking instanceof MOB)
			return (MOB)ticking;
		else
		if(ticking instanceof Item)
			if(((Item)ticking).owner() != null)
				if(((Item)ticking).owner() instanceof MOB)
					return (MOB)((Item)ticking).owner();

		return null;
	}

	protected Room getBehaversRoom(Tickable ticking)
	{
		if(ticking==null) return null;

		if(ticking instanceof Room)
			return (Room)ticking;

		MOB mob=getBehaversMOB(ticking);
		if(mob!=null)
			return mob.location();

		if(ticking instanceof Item)
			if(((Item)ticking).owner() != null)
				if(((Item)ticking).owner() instanceof Room)
					return (Room)((Item)ticking).owner();

		return null;
	}

	public String getParms(){return parms;}
	public void setParms(String parameters){parms=parameters;}
	public String parmsFormat(){return CMParms.FORMAT_UNDEFINED;}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public Vector externalFiles(){return null;}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		return;
	}

	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		return true;
	}

	public boolean canImprove(int can_code){return CMath.bset(canImproveCode(),can_code);}
	public boolean canImprove(Environmental E)
	{
		if((E==null)&&(canImproveCode()==0)) return true;
		if(E==null) return false;
		if((E instanceof MOB)&&((canImproveCode()&Ability.CAN_MOBS)>0)) return true;
		if((E instanceof Item)&&((canImproveCode()&Ability.CAN_ITEMS)>0)) return true;
		if((E instanceof Exit)&&((canImproveCode()&Ability.CAN_EXITS)>0)) return true;
		if((E instanceof Room)&&((canImproveCode()&Ability.CAN_ROOMS)>0)) return true;
		if((E instanceof Area)&&((canImproveCode()&Ability.CAN_AREAS)>0)) return true;
		return false;
	}
	public static boolean canActAtAll(Tickable affecting)
	{ return CMLib.flags().canActAtAll(affecting);}

	public static boolean canFreelyBehaveNormal(Tickable affecting)
	{ return CMLib.flags().canFreelyBehaveNormal(affecting);}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof Environmental) && (((Environmental)ticking).amDestroyed()))
			return false;
		return true;
	}

	public String[] modCodes() {return null;}

	public String modBrief(int code) {return null;}
	public modType modType(int code) {return null;}
	public String modDefault(int code) {return null;}

	public static enum SCode{
		SAV(savType.BOOLEAN), PRM(savType.STRING);
		@Override public String toString()
		{
			String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}
		private savType type;
		private SCode(savType myType) {type=myType;}
		public savType type(){return type;}
	}
	private static String[] parsedSCodes=null;
	private static savType[] parsedSTypes=null;
	private void parseSCodes()
	{
		SCode[] codes=SCode.values();
		parsedSCodes=new String[codes.length];
		parsedSTypes=new savType[codes.length];
		for(int i=0;i<codes.length;i++)
		{
			parsedSCodes[i]=codes[i].toString();
			parsedSTypes[i]=codes[i].type();
		}
	}
	public String[] savCodes()
	{
		if(parsedSCodes==null)
			parseSCodes();
		return parsedSCodes;
	}
	public savType savType(int code)
	{
		if(parsedSTypes==null)
			parseSCodes();
		return parsedSTypes[code];
	}

	public boolean savBoolean(int code, boolean val)
	{
		if(code>=0)
		{
			switch(SCode.values()[code])
			{
				case SAV: isSavableBehavior=val;
			}
		}
		else
		{
			code=(-code)-1;
			switch(SCode.values()[code])
			{
				case SAV: return isSavableBehavior;
			}
		}
		return false;
	}
	public String savString(int code, String val)
	{
		if(code>=0)
		{
			switch(SCode.values()[code])
			{
				case PRM: setParms(val); break;
			}
		}
		else
		{
			code=(-code)-1;
			switch(SCode.values()[code])
			{
				case PRM: return parms;
			}
		}
		return null;
	}

	public CMSavable savSub(int code, CMSavable val){return null;}
	public short savShort(int code, short val){return 0;}
	public int savInt(int code, int val){return 0;}
	public long savLong(int code, long val){return 0;}
	public double savDouble(int code, double val){return 0;}
	public char savChar(int code, char val){return 0;}
	public short[] savAShort(int code, short[] val){return null;}
	public int[] savAInt(int code, int[] val){return null;}
	public long[] savALong(int code, long[] val){return null;}
	public double[] savADouble(int code, double[] val){return null;}
	public boolean[] savABoolean(int code, boolean[] val){return null;}
	public String[] savAString(int code, String[] val){return null;}
	public char[] savAChar(int code, char[] val){return null;}
	public Vector savVector(int code, Vector val){return null;}

	public boolean sameAs(Behavior E)
	{
		if(!(E instanceof StdBehavior)) return false;
//		for(int i=0;i<CODES.length;i++)
//			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
//				return false;
		return true;
	}
}
