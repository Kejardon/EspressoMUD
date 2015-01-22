/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

public class OneToOneExitInstance implements ExitInstance
{
	protected Exit myExit;
	protected Room leadsTo;
	
	protected boolean destroyed=false;
	protected int saveNum=0;

	protected int exitToLoad=0;
	protected int roomToLoad=0;
	
	public OneToOneExitInstance(){}
	public OneToOneExitInstance(Exit exit, Room room){myExit=exit; leadsTo=room;}

	/*
	public boolean equals(Object O)
	{
		if(O instanceof OneToOneExitInstance)
			return (((OneToOneExitInstance)O).myExit==myExit)&&(((OneToOneExitInstance)O).leadsTo==leadsTo);
		return false;
	}
	*/
	public Exit getExit(){return myExit;}
	public Room getDestination(){return leadsTo;}
	@Override public String ID(){return "OneToOneExitInstance";}
	public Environmental getEnvObject() { return myExit.getEnvObject(); }
	@Override public OneToOneExitInstance newInstance(){return new OneToOneExitInstance();}
	@Override public OneToOneExitInstance copyOf(){return new OneToOneExitInstance(myExit, leadsTo);}
	@Override public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String name(){return myExit.name();}
	public String plainName(){return myExit.plainName();}
	public void setName(String newName){myExit.setName(newName);}
	public String displayText(){return myExit.directLook(null, leadsTo);}
	public String plainDisplayText(){return CMLib.coffeeFilter().toRawString(myExit.directLook(null, leadsTo));}
	public void setDisplayText(String newDisplayText){myExit.setDisplayText(newDisplayText);}
	public String description(){return myExit.description();}
	public String plainDescription(){return myExit.plainDescription();}
	public void setDescription(String newDescription){myExit.setDescription(newDescription);}

	public void addBehavior(Behavior to){myExit.addBehavior(to);}
	public void delBehavior(Behavior to){myExit.delBehavior(to);}
	public boolean hasBehavior(String ID){return myExit.hasBehavior(ID);}
	public int numBehaviors(){return myExit.numBehaviors();}
	public Behavior fetchBehavior(int index){return myExit.fetchBehavior(index);}
	public Behavior fetchBehavior(String ID){return myExit.fetchBehavior(ID);}
	public Iterator<Behavior> allBehaviors(){return myExit.allBehaviors();}

	public void addEffect(Effect to){myExit.addEffect(to);}
	public void delEffect(Effect to){myExit.delEffect(to);}
	public boolean hasEffect(Effect to){return myExit.hasEffect(to);}
	public int numEffects(){return myExit.numEffects();}
	public Effect fetchEffect(int index){return myExit.fetchEffect(index);}
	public Vector<Effect> fetchEffect(String ID){return myExit.fetchEffect(ID);}
	public Effect fetchFirstEffect(String ID){return myExit.fetchFirstEffect(ID);}
	public Iterator<Effect> allEffects(){return myExit.allEffects();}

	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return myExit.charAffecters();}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return myExit.envAffecters();}
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return myExit.okCheckers();}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return myExit.excCheckers();}
	public CopyOnWriteArrayList<TickActer> tickActers(){return myExit.tickActers();}
	public void removeListener(Listener oldAffect, EnumSet flags){myExit.removeListener(oldAffect, flags);}
	public void addListener(Listener newAffect, EnumSet flags){myExit.addListener(newAffect, flags);}
	public void registerListeners(ListenHolder forThis){myExit.registerListeners(forThis);}
	public void registerAllListeners(){myExit.registerAllListeners();}
	public void clearAllListeners(){myExit.clearAllListeners();}
	public int priority(ListenHolder forThis){return myExit.priority(forThis);}
	public EnumSet<ListenHolder.Flags> listenFlags() {return myExit.listenFlags();}
	//Special case for ticking. This will never tick. Exits have a special tick method.
	public Tickable.TickStat getTickStatus(){return myExit.getTickStatus();}
	public boolean tick(int tickTo){return false;}
	public int tickCounter(){return 0;}
	@Override public boolean respondTo(CMMsg msg){return myExit.respondTo(msg);}
	@Override public boolean respondTo(CMMsg msg, Object data){return myExit.respondTo(msg, data);}
	@Override public boolean okMessage(OkChecker myHost, CMMsg msg){return myExit.okMessage(myHost, msg);}
	@Override public void executeMsg(ExcChecker myHost, CMMsg msg){myExit.executeMsg(myHost, msg);}
	public boolean amDestroyed(){return destroyed;}
	public void destroy()
	{
		destroyed=true;
		if(saveNum!=0)
			CMLib.database().deleteObject(this);
	}
	

	//CMModifiable and CMSavable
	//CMModifiable and CMSavable
	@Override public SaveEnum[] totalEnumS(){return SCode.values();}
	@Override public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	@Override public ModEnum[] totalEnumM(){return MCode.values();}
	@Override public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)//&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.EXITINSTANCE.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.EXITINSTANCE.removeNumber(saveNum);
			saveNum=num;
			SIDLib.EXITINSTANCE.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(exitToLoad!=0)
		{
			myExit = SIDLib.EXIT.get(exitToLoad);
			if(myExit==null)
				Log.errOut("ExitInstance","Important exit not found: "+exitToLoad);
			exitToLoad=0;
		}
		if(roomToLoad!=0)
		{
			leadsTo = SIDLib.ROOM.get(roomToLoad);
			if(leadsTo==null)
				Log.errOut("ExitInstance","Important room not found: "+roomToLoad);
			roomToLoad=0;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	private enum SCode implements SaveEnum<OneToOneExitInstance>{
		EXT(){
			public ByteBuffer save(OneToOneExitInstance E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.myExit==null?0:E.myExit.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(OneToOneExitInstance E, ByteBuffer S){ E.exitToLoad=S.getInt(); } },
		ROM(){
			public ByteBuffer save(OneToOneExitInstance E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.leadsTo==null?0:E.leadsTo.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(OneToOneExitInstance E, ByteBuffer S){ E.roomToLoad=S.getInt(); } },
		;
		public CMSavable subObject(OneToOneExitInstance fromThis){return null;}}
	private enum MCode implements ModEnum<OneToOneExitInstance>{
		DESTINATION() {
			public String brief(OneToOneExitInstance E){return E.leadsTo==null?"null":(""+E.leadsTo.saveNum());}
			public String prompt(OneToOneExitInstance E){return "";}
			public void mod(OneToOneExitInstance E, MOB M){} },
		;}
}
