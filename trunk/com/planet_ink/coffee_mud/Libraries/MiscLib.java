package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.database.*;
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
import java.nio.ByteBuffer;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class MiscLib extends StdLibrary implements MiscLibrary
{
	private ThreadEngine.SupportThread thread=null;
	
	public String ID(){return "MiscLib";}
	public ThreadEngine.SupportThread getSupportThread() { return thread;}

	public boolean activate() {
		if(thread==null)
			thread=new ThreadEngine.SupportThread("THMiscSaver"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					DBManager.timeTillDoLast+2*DBManager.waitInterval, this, CMSecurity.isDebugging("SAVETHREAD"));
		if(!thread.started)
			thread.start();
		return true;
	}
	public CMObject newInstance() {return this;}

	public void run()
	{
		CMLib.database().saveObject(this);
	}

	public boolean shutdown()
	{
		CMLib.database().saveObject(this);
		return true;
	}

	public void destroy(){}//har

	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public int saveNum(){return 1;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(this);}

	private static enum SCode implements SaveEnum{
		SID() {
			public ByteBuffer save(MiscLib fromThis) {
				SIDLib.Objects[] SIDs=SIDLib.Objects.values();
				int[] values=new int[SIDs.length];
				for(int i=0;i<values.length;i++) values[i]=SIDs[i].currentNum();
				return CMLib.coffeeMaker().savAInt(values); }
			public void load(MiscLib toThis, ByteBuffer s) {
				SIDLib.Objects[] SIDs=SIDLib.Objects.values();
				int[] values=CMLib.coffeeMaker().loadAInt(s);
				for(int i=0;i<values.length;i++) SIDs[i].setNum(values[i]); }
			public int size(){return 4*SIDLib.Objects.values().length;} },
		GTC(){
			public ByteBuffer save(MiscLib E){ return CMLib.coffeeMaker().savSubFull(CoffeeTime.globalClock); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return CoffeeTime.globalClock;}
			public void load(MiscLib E, ByteBuffer S){ CoffeeTime.globalClock=(TimeClock)((Ownable)CMLib.coffeeMaker().loadSub(S, CMLib.time().globalClock())).setOwner(E); } },
		;
		public abstract ByteBuffer save(MiscLib fromThis);
		public abstract void load(MiscLib toThis, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((MiscLib)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((MiscLib)E, S);} }

}
