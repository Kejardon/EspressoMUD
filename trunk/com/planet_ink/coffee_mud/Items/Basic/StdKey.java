package com.planet_ink.coffee_mud.Items.Basic;
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
public class StdKey extends StdItem implements Key
{
	protected String key="skeleton";
	public String ID(){	return "StdKey";}
	public StdKey()
	{
		super();
		name="a metal key";
		display="a small metal key sits here.";
		desc="You can't tell what it\\`s to by looking at it.";
		saveObj=new KeySave(this);
		modObj=new KeyMod(this);

//		material=RawMaterial.RESOURCE_STEEL;
//		baseGoldValue=0;
//		recoverEnvStats();
	}

	public void setKey(String keyName){key=keyName;}
	public String getKey(){return key;}
	private enum SCode implements CMSavable.SaveEnum{
		KEY(CMSavable.savType.STRING)
		;
		@Override public String toString()
		{
			String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}
		private CMSavable.savType type;
		private SCode(CMSavable.savType myType) {type=myType;}
		public CMSavable.savType type(){return type;}
	}
	private enum MCode implements CMModifiable.ModEnum{
		KEYNAME(CMModifiable.modType.STRING)
		;
		@Override public String toString()
		{
			String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}
		private CMModifiable.modType type;
		private MCode(CMModifiable.modType myType) {type=myType;}
		public CMModifiable.modType type(){return type;}
	}
	protected class KeySave extends StdItem.ItemSave
	{
		public String ID(){return "KeySave";}
		public KeySave(CMObject O){super(O);}
		public SaveEnum[] totalEnum(boolean first)
		{
			if((first)&&(savCodes(ID())!=null))
				return null;
			SaveEnum[] newE = (SaveEnum[])EnumSet.allOf(SCode.class).toArray();
			SaveEnum[] old = super.totalEnum(false);
			indexFix = old.length;
			SaveEnum[] total=new SaveEnum[indexFix+newE.length];
			System.arraycopy(old, 0, total, 0, indexFix);
			System.arraycopy(newE, 0, total, indexFix, newE.length);
			if(savCodes(ID())==null)
				parseSaves(ID(), total);
			return total;
		}

		private int indexFix=0;

		public String savString(int code, String val)
		{
			if((code<indexFix)&&(-code-1<indexFix))
				return super.savString(code, val);
			if(code>=0)
			{
				switch(SCode.values()[code-indexFix])
				{
					case KEY: key=val; break;
				}
			}
			else
			{
				code=(-code)-1;
				switch(SCode.values()[code-indexFix])
				{
					case KEY: return key;
				}
			}
			return "";
		}
	}
	protected class KeyMod extends StdItem.ItemMod
	{
		public String ID(){return "KeyMod";}
		public KeyMod(CMObject O){super(O);}
		public ModEnum[] totalEnum(boolean first)
		{
			if((first)&&(modCodes(ID())!=null))
				return null;
			ModEnum[] newE = (ModEnum[])EnumSet.allOf(MCode.class).toArray();
			ModEnum[] old = super.totalEnum(false);
			indexFix = old.length;
			ModEnum[] total=new ModEnum[indexFix+newE.length];
			System.arraycopy(old, 0, total, 0, indexFix);
			System.arraycopy(newE, 0, total, indexFix, newE.length);
			if(modCodes(ID())==null)
				parseMods(ID(), total);
			return total;
		}
		private int indexFix=0;
		public String modBrief(int code)
		{
			if(code<indexFix)
				return super.modBrief(code);
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case KEYNAME: return key;
			}
			return "";
		}
		public String modDefault(int code)
		{
			if(code<indexFix)
				return super.modDefault(code);
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case KEYNAME: return key;
			}
			return "";
		}
		public void modString(int code, String val)
		{
			if(code<indexFix)
			{
				super.modString(code, val);
				return;
			}
			code-=indexFix;
			switch(MCode.values()[code])
			{
				case KEYNAME: key=val; return;
			}
			return;
		}
	}
}
