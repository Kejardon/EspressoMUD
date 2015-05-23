package com.planet_ink.coffee_mud.ExitInstance;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import java.nio.ByteBuffer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class StdExitInstance extends AbstractSaveInteractable implements ExitInstance
{
	@Override protected int AI_ENABLES(){return AI_ENV|AI_AFFECTS|AI_BEHAVES|AI_OK|AI_EXC|AI_TICK;}
	@Override public String ID(){return "StdExitInstance";}
	@Override protected SIDLib.Objects SID(){return SIDLib.EXITINSTANCE; }
	
	protected Exit myExit;
	protected Room leadsTo;
	protected Room container;
	
	protected int exitToLoad=0;
	protected int roomToLoad=0;
	
	public StdExitInstance(){}
	public StdExitInstance(StdExitInstance clone)
	{
		super(clone);
		
	}
	
	//@Override public void initializeClass(){}
	
	@Override public Exit getExit(){return myExit;}
	@Override public void setExit(Exit e){myExit = e;}
	@Override public Room getDestination(){return leadsTo;}
	@Override public void setDestination(Room r){leadsTo = r;}
	@Override public void setInRoom(Room r){container=r;}
	@Override public Room getInRoom(){return container;}
	
	@Override public String name(){return myExit.name();}
	@Override public String plainName(){return myExit.plainName();}
	//@Override public void setName(String newName){myExit.setName(newName);}
	@Override public String displayText(){return myExit.directLook(null, leadsTo);}
	@Override public String plainDisplayText(){return CMLib.coffeeFilter().toRawString(myExit.directLook(null, leadsTo));}
	//@Override public void setDisplayText(String newDisplayText){myExit.setDisplayText(newDisplayText);}
	@Override public String description(){return myExit.description();}
	@Override public String plainDescription(){return myExit.plainDescription();}
	//@Override public void setDescription(String newDescription){myExit.setDescription(newDescription);}
	
	@Override public StdExitInstance newInstance()
	{
		try{return this.getClass().newInstance();}catch(Exception e){Log.errOut(ID(),e);}
		return new StdExitInstance();
	}
	@Override public StdExitInstance copyOf()
	{
		return new StdExitInstance(this);
		/*
		try
		{
			StdExitInstance E=(StdExitInstance)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e){return this.newInstance();}
		*/
	}
	@Override public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK))&&(container!=null))
			container.removeListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	@Override public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK))&&(container!=null))
			container.addListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	
	@Override public boolean tick(int tickTo)
	{
		boolean result=super.tick(tickTo);
		if(!result) lFlags.remove(ListenHolder.Flags.TICK);
		return result;
	}
	
	@Override public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!myEnvironmental.okMessage(myHost, msg))
			return false;
		for(OkChecker O : okCheckers)
			if(!O.okMessage(this,msg))
				return false;
		return true;
	}
	@Override public boolean respondTo(CMMsg msg, Object data){return true;}
	@Override public boolean respondTo(CMMsg msg){return true;}
	@Override public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		myEnvironmental.executeMsg(myHost, msg);
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost, msg);
	}
	@Override public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	@Override public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	@Override public void registerAllListeners() { }
	@Override public void clearAllListeners() { }
	
	@Override public void destroy()
	{
		if(myExit!=null) myExit.removeInstance(this, false);
		if(container!=null) container.removeExit(this);
		super.destroy();
	}
	
	//@Override public boolean needLink(){return true;}
	public void link()
	{
		if(exitToLoad!=0)
		{
			myExit = SIDLib.EXIT.get(exitToLoad);
			if(myExit==null)
				Log.errOut(ID(),"Important exit not found: "+exitToLoad);
			exitToLoad=0;
		}
		if(roomToLoad!=0)
		{
			leadsTo = SIDLib.ROOM.get(roomToLoad);
			if(leadsTo==null)
				Log.errOut(ID(),"Important room not found: "+roomToLoad);
			roomToLoad=0;
		}
		super.link();
	}
	//@Override public void saveThis(){CMLib.database().saveObject(this);}
	//@Override public void prepDefault(){}

	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	@Override public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null) totalEnumM=CMParms.appendToArray(MCode.values(), super.totalEnumM(), ModEnum[].class);
		return totalEnumM;
	}
	@Override public Enum[] headerEnumM()
	{
		if(headerEnumM==null) headerEnumM=CMParms.appendToArray(new Enum[] {MCode.values()[0]}, super.headerEnumM(), Enum[].class);
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	@Override public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null) totalEnumS=CMParms.appendToArray(SCode.values(), super.totalEnumS(), SaveEnum[].class);
		return totalEnumS;
	}
	@Override public Enum[] headerEnumS()
	{
		if(headerEnumS==null) headerEnumS=CMParms.appendToArray(new Enum[] {SCode.values()[0]}, super.headerEnumS(), Enum[].class);
		return headerEnumS;
	}
	
	private enum SCode implements SaveEnum<StdExitInstance>{
		EXT(){
			public ByteBuffer save(StdExitInstance E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.myExit==null?0:E.myExit.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdExitInstance E, ByteBuffer S){ E.exitToLoad=S.getInt(); } },
		ROM(){
			public ByteBuffer save(StdExitInstance E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.leadsTo==null?0:E.leadsTo.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdExitInstance E, ByteBuffer S){ E.roomToLoad=S.getInt(); } },
		;
		public CMSavable subObject(StdExitInstance fromThis){return null;}}
	private enum MCode implements ModEnum<StdExitInstance>{
		DESTINATION() {
			public String brief(StdExitInstance E){return E.leadsTo==null?"null":(""+E.leadsTo.saveNum());}
			public String prompt(StdExitInstance E){return "";}
			public void mod(StdExitInstance E, MOB M){} },
		EXIT(){
			public String brief(StdExitInstance E){return E.myExit.ID();}
			public String prompt(StdExitInstance E){return "";}
			public void mod(StdExitInstance E, MOB M){CMLib.genEd().genMiscSet(M, E.myExit);} },
		;}
}
