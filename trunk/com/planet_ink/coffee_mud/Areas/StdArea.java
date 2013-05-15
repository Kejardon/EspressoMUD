package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Libraries.*;

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
public class StdArea implements Area
{
	public String ID(){return "StdArea";}
	protected String name="the area";
	protected SortedVector<Room> properRooms=new SortedVector<Room>();
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected int tickCount=0;
	
//	protected long lastPlayerTime=System.currentTimeMillis();
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected HashedList<Room> tickingRooms=new HashedList<Room>();
	protected Room[] totalMetroRooms=null;
	//protected int lastTick=0;
	protected int saveNum=0;

	protected CopyOnWriteArrayList<Area> children=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Area> parents=new CopyOnWriteArrayList();
	protected Environmental myEnvironmental;//=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);

	protected String author="";
	protected TimeClock myClock=null;
	protected int lastHour=0;

	protected volatile boolean interruptGMC=false;

	protected int[] childrenToLoad=null;
	protected int[] effectsToLoad=null;

	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}

	public void initializeClass(){}

	public int ambientTemperature()
	{
		TimeClock clock=getTimeObj();
		int time=clock.getTimeOfDay();
		int[] times=clock.getDawnToDusk();
		int TOD=clock.getTODCode();
		int nextTOD = TOD==TimeClock.TIME_NIGHT?TimeClock.TIME_DAWN:(TOD+1);
		float TODLength;
		if(TOD==TimeClock.TIME_NIGHT)
			TODLength=clock.getHoursInDay()+times[TimeClock.TIME_DAWN]-times[TOD];
		else
			TODLength=times[nextTOD]-times[TOD];
		float percent = ((time>times[TOD]?time:(clock.getHoursInDay()+time))-times[TOD])/TODLength;
		//percent is now between 0 and 1 how far the current TOD is.
		int temperature=(int)
			((TimeClock.defaultTemperatures[clock.getSeasonCode()][TOD]*(1-percent)
			  +TimeClock.defaultTemperatures[clock.getSeasonCode()][nextTOD]*percent)
			 /2);
		return temperature;
	}
	public void setTimeObj(TimeClock obj){myClock=obj;}
	public TimeClock getTimeObj()
	{
		if(myClock==null) myClock=CMLib.misc().globalClock();
		return myClock;
	}
	public Environmental getEnvObject(){
		if(myEnvironmental==null)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners() { }
	public void clearAllListeners() { }

	public StdArea() { }
	protected void finalize(){}
	protected boolean amDestroyed=false;
	public void destroy()
	{
		amDestroyed=true;
		CMLib.threads().deleteArea(this);
		if(myEnvironmental!=null) myEnvironmental.destroy();
		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		for(Area A : children)
			A.removeParent(this);
		children.clear();
		for(Area A : parents)	//TODO ish: This doesn't make sure metroRooms doesn't end up with obliterated rooms
			A.removeChild(this);
		parents.clear();
		CMLib.map().finishObliterateArea(this, getProperMap());
		clearMetroMap();
		if(saveNum!=0)
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed(){return amDestroyed;}

	public String name() { return name; }

	public void setName(String newName){name=newName; CMLib.database().saveObject(this);}
	public String Name(){return name;}

//	public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	public CMObject newInstance() { return new StdArea(); }
	protected void cloneFix(StdArea E)
	{
		okCheckers=new CopyOnWriteArrayList();
		excCheckers=new CopyOnWriteArrayList();
		tickActers=new CopyOnWriteArrayList();
		lFlags=lFlags.clone();
		if(E.myEnvironmental!=null)
			myEnvironmental=(Environmental)((Ownable)E.myEnvironmental.copyOf()).setOwner(this);
		properRooms=new SortedVector(); //ROOOOOOOMS AGH. Going to say no for now.
		tickingRooms=new HashedList();
		tickStatus=Tickable.TickStat.Not;
		parents=new CopyOnWriteArrayList();
		children=new CopyOnWriteArrayList();
		affects=new CopyOnWriteArrayList();
		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
	}
	public CMObject copyOf()
	{
		try
		{
			StdArea E=(StdArea)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e) { return newInstance(); }	//To be safe for class extension
	}

	public int compareTo(CMObject o)
	{
		if(o instanceof Area)
			 return name.compareTo(((Area)o).name());
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}
	public void removeListener(Listener oldAffect, EnumSet<ListenHolder.Flags> flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.OK))&&(okCheckers.isEmpty()))
			lFlags.remove(ListenHolder.Flags.OK);
		if((flags.contains(ListenHolder.Flags.EXC))&&(excCheckers.isEmpty()))
			lFlags.remove(ListenHolder.Flags.EXC);
	}
	public void addListener(Listener newAffect, EnumSet<ListenHolder.Flags> flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.OK))&&(!okCheckers.isEmpty()))
			lFlags.add(ListenHolder.Flags.OK);
		if((flags.contains(ListenHolder.Flags.EXC))&&(!excCheckers.isEmpty()))
			lFlags.add(ListenHolder.Flags.EXC);
	}
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}

	public boolean okMessage(OkChecker myHost, CMMsg msg)
	{
/*
		for(int i=0;i<msg.source().size();i++)
		{
			Interactable E = msg.source().get(i);
			if((E instanceof MOB)&&(!((MOB)E).isMonster()))
			{
				lastPlayerTime=System.currentTimeMillis();
			}
		}
*/
		for(OkChecker O : okCheckers)
			if(!O.okMessage(this,msg))
				return false;
		for(Area A : parents)
			if(!A.okMessage(myHost,msg))
				return false;
		/*
		if(myHost==this)
		for(Room R : getMetroCollection())
			if(!R.okMessage(this,msg))	//TODO: This and executeMsg have to handle room locks somehow...
				return false;
		*/
		return true;
	}
	public boolean respondTo(CMMsg msg, Object data){return true;}
	public boolean respondTo(CMMsg msg){return true;}

	public void sendMessageEverywhere(CMMsg msg)
	{
		for(Room R : getMetroCollection())
			R.send(msg);
	}
	public void showMessageEverywhere(Interactable source, Interactable target, CMObject tool, String message)
	{
		for(Room R : getMetroCollection())
			R.show(source, target, tool, message);
	}
	public void showMessageEverywhere(Interactable source, Interactable target, CMObject tool, String srcMessage, String tarMessage, String othMessage)
	{
		for(Room R : getMetroCollection())
			R.show(source, target, tool, srcMessage, tarMessage, othMessage);
	}

	public void executeMsg(ExcChecker myHost, CMMsg msg)
	{
		for(ExcChecker O : excCheckers)
			O.executeMsg(this, msg);
		for(Area A : parents)
			A.executeMsg(myHost,msg);
		if(myHost==this)
		for(Room R : getMetroCollection())
			R.executeMsg(this, msg);
	}

	public Tickable.TickStat getTickStatus(){return tickStatus;}

	public void tickAct(){}
	public int tickCounter(){return tickCount;}
	public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {tickCount=0; return false;}
		}
		return true;
	}
	protected boolean doTick()
	{
		tickStatus=Tickable.TickStat.Start;
		int newTime;
		if(lastHour!=(newTime=getTimeObj().getTimeOfDay()))
		{
			int newTemp=ambientTemperature();
			getEnvObject().setTemperature(newTemp);
			Effect E=CMClass.EFFECT.getNew("Temperature");
			if(E!=null) for(Room R : getMetroCollection())
				if(R.getEnvObject().temperature()!=newTemp)
					E.invoke(R.getEnvObject(), -1);
			//TODO: 
			lastHour=newTime;
		}
		tickStatus=Tickable.TickStat.Listener;
		for(TickActer T : tickActers)
			if(!T.tick(tickCount))
				tickActers.remove(T);
		tickStatus=Tickable.TickStat.End;
		for(Iterator<Room> I=tickingRooms.iterator(); I.hasNext();)
		{
			Room R=I.next();
			if(!R.tick(tickCount))
				tickingRooms.remove(R);
		}
		tickStatus=Tickable.TickStat.Not;
		return true;
	}

	public void addEffect(Effect to)
	{
		affects.add(to);
		to.setAffectedOne(this);
		CMLib.database().saveObject(this);
	}
	public void delEffect(Effect to)
	{
		if(affects.remove(to))
		{
			to.setAffectedOne(null);
			CMLib.database().saveObject(this);
		}
	}
	public boolean hasEffect(Effect to)
	{
		return affects.contains(to);
	}
	public int numEffects(){return affects.size();}
	public Effect fetchEffect(int index)
	{
		try { return affects.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Effect> fetchEffect(String ID)
	{
		Vector<Effect> V=new Vector(1);
		for(Effect E : affects)
			if(E.ID().equals(ID))
				V.add(E);
		return V;
	}
	public Effect fetchFirstEffect(String ID)
	{
		for(Effect E : affects)
			if(E.ID().equals(ID))
				return E;
		return null;
	}
	public Iterator<Effect> allEffects() { return affects.iterator(); }
	public boolean inMyMetroArea(Area A)
	{
		if(A==this) return true;
		if(getNumChildren()==0) return false;
		for(Area child : children)
			if(child.inMyMetroArea(A))
				return true;
		return false;
	}

	public void addTickingRoom(Room R) { tickingRooms.add(R); }
	public void removeTickingRoom(Room R) { tickingRooms.remove(R); }
	public int properSize() { return properRooms.size(); }
	public void addProperRoom(Room R)
	{
		synchronized(properRooms)
		{
			if(properRooms.contains(R))
				return;
			properRooms.add(R);
		}
		clearMetroMap();
	}
	//TODO: Update this for item rooms when they're done?
	public boolean isRoom(Room R)
	{
		if(R==null) return false;
		synchronized(properRooms)
		{
			return properRooms.contains(R);
		}
	}
	public void delProperRoom(Room R)
	{
		synchronized(properRooms)
		{
			if(properRooms.remove(R))
				clearMetroMap();
		}
	}

	public int metroSize()
	{
		if(totalMetroRooms!=null) return totalMetroRooms.length;
		int num=properSize();
		for(Area A : children)
			num+=A.metroSize();
		return num;
	}
	public Room getRandomProperRoom()
	{
		while(!properRooms.isEmpty()) try{
			return properRooms.get(CMath.random(properRooms.size()));
		}catch (Exception e){}
		return null;
	}
	public Room getRandomMetroRoom()
	{
		Room[] V=getMetroCollection();
		if(V.length>0) return V[CMath.random(V.length)];
		return null;
	}

	public void clearMetroMap()
	{
		interruptGMC=true;
		totalMetroRooms=null;
		for(Area A : parents)
			A.clearMetroMap();
	}

	public Room[] getProperMap()
	{
		return (Room[])properRooms.toArray(Room.dummyRoomArray);
	}

	public Room[] getMetroCollection()
	{
		if(totalMetroRooms!=null)
			return totalMetroRooms;
		interruptGMC=false;
		ArrayList<Room> tempList=new ArrayList(0);
		ArrayList<Room[]> toAdd=new ArrayList(children.size());
		int totalSize=0;
		for(Area child : children)
		{
			Room[] childRooms=child.getMetroCollection();
			toAdd.add(childRooms);
			totalSize+=childRooms.length;
		}
		synchronized(properRooms)
		{
			totalSize+=properRooms.size();
			tempList.ensureCapacity(totalSize);
			tempList.addAll(properRooms);
		}
		for(Room[] roomGroup : toAdd)
			for(Room roomToAdd : roomGroup)
				tempList.add(roomToAdd);
		Room[] finalList=tempList.toArray(Room.dummyRoomArray);
		if(!interruptGMC)	totalMetroRooms=finalList;
		return finalList;
	}
	//public Enumeration<Room> getMetroMap(){return getMetroCollection().elements();}

	// Children
	public Iterator<Area> getChildren() { return children.iterator(); }
	public String getChildrenList()
	{
		StringBuffer str=new StringBuffer("");
		for(Area A : children)
		{
			if(str.length()>0) str.append(";");
			str.append(A.name());
		}
		return str.toString();
	}

	public int getNumChildren() { return children.size(); }
	public Area getChild(int num) { return children.get(num); }
	public Area getChild(String named)
	{
		for(Area A : children)
			if(A.name().equalsIgnoreCase(named))
				return A;
		return null;
	}
	public boolean isChild(Area named) { return children.contains(named); }
	public boolean isChild(String named)
	{
		for(Area A : children)
			if(A.name().equalsIgnoreCase(named))
				return true;
		return false;
	}
	public void addChild(Area Adopted)
	{
		if(children.addIfAbsent(Adopted))
		{
			Adopted.addParent(this);
			CMLib.database().saveObject(this);
		}
	}
	public void removeChild(Area Disowned) { children.remove(Disowned); Disowned.removeParent(this); clearMetroMap(); CMLib.database().saveObject(this);}
	public void removeChild(int Disowned) { children.remove(Disowned).removeParent(this); clearMetroMap(); CMLib.database().saveObject(this); }
	// child based circular reference check
	// Doesn't prevent having the same parent twice. NOTE: People making areas should take care about this!
	public boolean canChild(Area newChild)
	{
		if(newChild==this) return false;
		for(Area A : parents)
			if(!(A.canChild(newChild)))
				return false;
		return true;
	}

	// Parent
	public Iterator<Area> getParents() { return parents.iterator(); }
	public Vector<Area> getParentsRecurse()
	{
		Vector<Area> V=new Vector<Area>();
		for(Area A : parents)
		{
			V.add(A);
			CMParms.addToVector(A.getParentsRecurse(),V);
		}
		return V;
	}

	public String getParentsList()
	{
		StringBuffer str=new StringBuffer("");
		for(Area A : parents) 
		{
			if(str.length()>0) str.append(";");
			str.append(A.name());
		}
		return str.toString();
	}

	public int getNumParents() { return parents.size(); }
	public Area getParent(int num) { return parents.get(num); }
	public Area getParent(String named)
	{
		for(Area A : parents)
			if(A.name().equalsIgnoreCase(named))
				return A;
		return null;
	}
	public boolean isParent(Area named)
	{
		return parents.contains(named);
	}
	public boolean isParent(String named)
	{
		for(Area A : parents)
			if(A.name().equalsIgnoreCase(named))
				return true;
		return false;
	}
	public void addParent(Area Adopted) { parents.add(Adopted); }
	public void removeParent(Area Disowned) { parents.remove(Disowned); }
	public void removeParent(int Disowned) { parents.remove(Disowned); }

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.AREA.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.AREA.removeNumber(saveNum);
			saveNum=num;
			SIDLib.AREA.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(childrenToLoad!=null)
		{
			for(int SID : childrenToLoad)
			{
				Area Adopted = SIDLib.AREA.get(SID);
				if(Adopted==null) continue;
				Adopted.addParent(this);
				children.add(Adopted);
			}
			childrenToLoad=null;
		}
		if(effectsToLoad!=null)
		{
			for(int SID : effectsToLoad)
			{
				Effect to = SIDLib.EFFECT.get(SID);
				if(to==null) continue;
				affects.add(to);
				to.setAffectedOne(this);
			}
			effectsToLoad=null;
		}
		CMLib.map().addArea(this);
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){getEnvObject();}

	private enum SCode implements SaveEnum<StdArea>{
		NAM(){
			public ByteBuffer save(StdArea E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(StdArea E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		CHL(){
			public ByteBuffer save(StdArea E){
				if(E.children.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.children.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdArea E, ByteBuffer S){ E.childrenToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		ENV(){
			public ByteBuffer save(StdArea E){
				if(E.myEnvironmental==null) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.myEnvironmental); }
			public int size(){return -1;}
			public CMSavable subObject(StdArea fromThis){return fromThis.myEnvironmental;}
			public void load(StdArea E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		EFC(){
			public ByteBuffer save(StdArea E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdArea E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		ATH(){
			public ByteBuffer save(StdArea E){ return CMLib.coffeeMaker().savString(E.author); }
			public int size(){return 0;}
			public void load(StdArea E, ByteBuffer S){ E.author=CMLib.coffeeMaker().loadString(S); } }
		;
		public CMSavable subObject(StdArea fromThis){return null;} }
	private enum MCode implements ModEnum<StdArea>{
		NAME(){
			public String brief(StdArea E){return E.name;}
			public String prompt(StdArea E){return E.name;}
			public void mod(StdArea E, MOB M){E.name=CMLib.genEd().stringPrompt(M, E.name, false);} },
		EFFECTS(){
			public String brief(StdArea E){return ""+E.affects.size();}
			public String prompt(StdArea E){return "";}
			public void mod(StdArea E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		ENVIRONMENTAL(){
			public String brief(StdArea E){return E.getEnvObject().ID();}
			public String prompt(StdArea E){return "";}
			public void mod(StdArea E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		AUTHOR(){
			public String brief(StdArea E){return E.author;}
			public String prompt(StdArea E){return E.author;}
			public void mod(StdArea E, MOB M){E.author=CMLib.genEd().stringPrompt(M, E.author, false);} },
		CHILDREN(){
			public String brief(StdArea E){return E.getChildrenList();}
			public String prompt(StdArea E){return E.getChildrenList();}
			public void mod(StdArea E, MOB M){
				while((M.session()!=null)&&(!M.session().killFlag())){
					M.session().rawPrintln(E.getParentsList());
					Area A=CMLib.genEd().areaPrompt(M);
					if(A==null) return;
					if(E.isChild(A)) E.removeChild(A);
					else if(E.canChild(A)) E.addChild(A);
					else M.session().rawPrintln("That would cause a circular reference."); } } },
		PARENTS(){
			public String brief(StdArea E){return E.getParentsList();}
			public String prompt(StdArea E){return E.getParentsList();}
			public void mod(StdArea E, MOB M){
				while((M.session()!=null)&&(!M.session().killFlag())){
					M.session().rawPrintln(E.getParentsList());
					Area A=CMLib.genEd().areaPrompt(M);
					if(A==null) return;
					if(A.isChild(E)) A.removeChild(E);
					else if(A.canChild(E)) A.addChild(E);
					else M.session().rawPrintln("That would cause a circular reference."); } } },
		; }

/*
	protected Vector allBlurbFlags()
	{
		Vector V=(Vector)blurbFlags.clone();
		String flag=null;
		Area A=null;
		int num=0;
		for(Enumeration e=getParents();e.hasMoreElements();)
		{
			A=(Area)e.nextElement();
			num=A.numBlurbFlags();
			for(int x=0;x<num;x++)
			{
				flag=A.getBlurbFlag(x);
				V.addElement(flag+" "+A.getBlurbFlag(flag));
			}
		}
		return V;
	}
	public String getBlurbFlag(String flag)
	{
		if((flag==null)||(flag.trim().length()==0))
			return null;
		flag=flag.toUpperCase().trim()+" ";
		Vector V=allBlurbFlags();
		for(int i=0;i<V.size();i++)
			if(((String)V.elementAt(i)).startsWith(flag))
				return ((String)V.elementAt(i)).substring(flag.length());
		return null;
	}
	public int numBlurbFlags(){return blurbFlags.size();}
	public int numAllBlurbFlags(){return allBlurbFlags().size();}
	public String getBlurbFlag(int which)
	{
		if(which<0) return null;
		Vector V=allBlurbFlags();
		if(which>=V.size()) return null;
		try{
			String s=(String)V.elementAt(which);
			int x=s.indexOf(' ');
			return s.substring(0,x).trim();
		}catch(Exception e){}
		return null;
	}
	public void addBlurbFlag(String flagPlusDesc)
	{
		if(flagPlusDesc==null) return;
		flagPlusDesc=flagPlusDesc.trim();
		if(flagPlusDesc.length()==0) return;
		int x=flagPlusDesc.indexOf(' ');
		String flag=null;
		if(x>=0)
		{
			flag=flagPlusDesc.substring(0,x).toUpperCase();
			flagPlusDesc=flagPlusDesc.substring(x).trim();
		}
		else
		{
			flag=flagPlusDesc.toUpperCase().trim();
			flagPlusDesc="";
		}
		if(getBlurbFlag(flag)==null)
			blurbFlags.addElement((flag+" "+flagPlusDesc).trim());
	}
	public void delBlurbFlag(String flagOnly)
	{
		if(flagOnly==null) return;
		flagOnly=flagOnly.toUpperCase().trim();
		if(flagOnly.length()==0) return;
		flagOnly+=" ";
		try{
			for(int v=0;v<blurbFlags.size();v++)
				if(((String)blurbFlags.elementAt(v)).startsWith(flagOnly))
				{
					blurbFlags.removeElementAt(v);
					return;
				}
		}catch(Exception e){}
	}
*/
}
