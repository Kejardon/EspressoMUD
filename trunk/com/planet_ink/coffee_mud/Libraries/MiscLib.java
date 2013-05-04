package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import com.planet_ink.coffee_mud.core.database.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class MiscLib extends StdLibrary implements MiscLibrary
{
	//private ThreadEngine.SupportThread thread=null;
	protected TimeClock globalClock=null;

	public String ID(){return "MiscLib";}
	//public ThreadEngine.SupportThread getSupportThread() { return thread;}

	/*public boolean activate() {
		if(thread==null)
			thread=new ThreadEngine.SupportThread("THMiscSaver"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					DBManager.timeTillDoLast+2*DBManager.waitInterval, this, CMSecurity.isDebugging("SAVETHREAD"));
		if(!thread.started)
			thread.start();
		return true;
	}
	public void run() { CMLib.database().saveObject(this); }*/

	public void run(){}
	public CMObject newInstance() {return this;}

	public boolean shutdown()
	{
		CMLib.database().saveObject(this);
		return true;
	}
	public TimeClock globalClock()
	{
		if(globalClock==null) synchronized(this){
			if(globalClock==null) globalClock=(TimeClock)((Ownable)CMClass.COMMON.getNew("DefaultTimeClock")).setOwner(CMLib.misc());}
		return globalClock;
	}

	public void destroy(){}//har
	public boolean amDestroyed(){return false;}//no

	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public int saveNum(){return 1;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	private static enum SCode implements SaveEnum<MiscLib>{
		SID() {
			public ByteBuffer save(MiscLib fromThis) {
				SIDLib.Objects[] SIDs=SIDLib.Objects.values();
				int[] values=new int[SIDs.length];
				for(int i=0;i<values.length;i++) values[i]=SIDs[i].currentNum();
				return CMLib.coffeeMaker().savAInt(values); }
			public void load(MiscLib toThis, ByteBuffer s) {
				SIDLib.Objects[] SIDs=SIDLib.Objects.values();
				int[] values=CMLib.coffeeMaker().loadAInt(s);
				for(int i=0;i<values.length&&i<SIDs.length;i++) SIDs[i].setNum(values[i]); }
			public int size(){return 4*SIDLib.Objects.values().length;} },
		GTC(){
			public ByteBuffer save(MiscLib E){ return CMLib.coffeeMaker().savSubFull(E.globalClock()); }
			public int size(){return -1;}
			public CMSavable subObject(MiscLib E){return ((MiscLib)E).globalClock();}
			public void load(MiscLib E, ByteBuffer S){ E.globalClock=(TimeClock)((Ownable)CMLib.coffeeMaker().loadSub(S, E, this)).setOwner(E); } },
		;
		public CMSavable subObject(MiscLib fromThis){return null;} }

}
