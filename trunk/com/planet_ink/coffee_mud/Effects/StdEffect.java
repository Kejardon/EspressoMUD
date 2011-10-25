package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;

import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class StdEffect implements Effect
{
	protected int tickDown=-1;
	protected int ability=0;
	protected String text="";
	protected Vector affects=new Vector(1);
	protected boolean ticking=true;

	protected Affectable affected=null;
	protected boolean unInvoked=false;
	protected EnumSet myFlags=EnumSet.of(Flags.Natural);
	protected EnumSet myListens=EnumSet.of();

	public String ID() { return "StdEffect"; }
	public int priority(){return Integer.MAX_VALUE;}
	public void initializeClass() {}
	public void registerListeners(Affectable forThis)
	{
		if(forThis==affected)
			Affectable.addListener(this, myFlags);
	}
	public StdEffect(){}

	public CMObject newInstance()
	{
		try { return (CMObject)this.getClass().newInstance(); }
		catch(Exception e) { Log.errOut(ID(),e); }
		return new StdEffect();
	}
	public EnumSet effectFlags(){ return myFlags; }

	public int abilityCode(){return ability;}
	public void setAbilityCode(int newCode){ability=newCode;}

	public void startTickDown(Affectable affected, int tickTime)
	{
		if(affected.fetchEffect(ID())==null)
			affected.addEffect(this);
		tickDown=tickTime;
	}


	public int compareTo(CMObject o)
	{
		int i=ID().compareTo(o.ID())
		if(i!=0)
			return i;
		return CMLib.coffeeMaker().getPropertiesStr(this).compareTo(CMLib.coffeeMaker().getPropertiesStr(o));
	}
	protected void cloneFix(Effect E){}
	public CMObject copyOf()
	{
		try
		{
			StdEffect E=(StdEffect)this.clone();
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public String text(){return text;}
	public void setText(String newT){text=newT;}
	public Affectable affecting()
	{
		return affected;
	}
	public void setAffectedOne(Affectable being)
	{
		affected=being;
	}

	public void unInvoke()
	{
		unInvoked=true;

		if(affected==null) return;
		Affected being=affected;

		being.delEffect(this);
	}

	public boolean invoke(Environmental target, int asLevel)
	{
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		return;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(unInvoked)
			return false;

		if((tickID==Tickable.TICKID_MOB)
		&&(tickDown!=Integer.MAX_VALUE))
		{
			if(tickDown<0)
				return !unInvoked;
			if((--tickDown)<=0)
			{
				tickDown=-1;
				unInvoke();
				return false;
			}
		}
		return true;
	}
	public void addEffect(Effect to);
	public void delEffect(Effect to);
	public int numEffects();
	public Effect fetchEffect(int index);
	public Effect fetchEffect(String ID);
	public void setTicking(boolean ticking);
	public static enum SCode{
		TCK(savType.INT), ABL(savType.INT), LST(savType.LONG);
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
	public CMSavable savSub(int code, CMSavable val){return null;}
	public Vector savVector(int code, Vector val){return null;}
	public short savShort(int code, short val){return 0;}
	public int savInt(int code, int val)
	{
		if(code>=0)
		{
			switch(SCode.values()[code])
			{
				case PRO: proficiency=val; break;
				case TCK: tickDown=val; break;
			}
		}
		else
		{
			code=(-code)-1;
			switch(SCode.values()[code])
			{
				case PRO: return proficiency;
				case TCK: return tickDown;
			}
		}
		return 0;
	}
	public long savLong(int code, long val)
	{
		if(code>=0)
		{
			switch(SCode.values()[code])
			{
				case LST: lastCastHelp=val; break;
			}
		}
		else
		{
			code=(-code)-1;
			switch(SCode.values()[code])
			{
				case LST: return lastCastHelp;
			}
		}
		return 0;
	}
	public double savDouble(int code, double val){return 0;}
	public boolean savBoolean(int code, boolean val)
	{
		if(code>=0)
		{
			switch(SCode.values()[code])
			{
				case SAV: savable=val; break;
				case CBU: canBeUninvoked=val; break;
				case UNV: unInvoked=val; break;
			}
		}
		else
		{
			code=(-code)-1;
			switch(SCode.values()[code])
			{
				case SAV: return savable;
				case CBU: return canBeUninvoked;
				case UNV: return unInvoked;
			}
		}
		return true;
	}
	public String savString(int code, String val)
	{
		if(code>=0)
		{
			switch(SCode.values()[code])
			{
				case TXT: setMiscText(val); break;
				case INV: break;//Can't really do this right now D: Need some sort of global identifier for specific objects... mobs at least
				case AFF: break;
			}
		}
		else
		{
			code=(-code)-1;
			switch(SCode.values()[code])
			{
				case TXT: return text();
				case INV: break;
				case AFF: break;
			}
		}
		return null;
	}
	public char savChar(int code, char val){return 0;}
	public short[] savAShort(int code, short[] val){return null;}
	public int[] savAInt(int code, int[] val){return null;}
	public long[] savALong(int code, long[] val){return null;}
	public double[] savADouble(int code, double[] val){return null;}
	public boolean[] savABoolean(int code, boolean[] val){return null;}
	public String[] savAString(int code, String[] val){return null;}
	public char[] savAChar(int code, char[] val){return null;}

	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdEffect)) return false;
		return true;
	}
}
