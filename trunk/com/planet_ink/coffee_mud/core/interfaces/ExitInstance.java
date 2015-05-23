package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;
import java.util.*;
import java.util.concurrent.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public interface ExitInstance extends Interactable, CMSavable, CMModifiable //Item?
{
	public Exit getExit();
	public void setExit(Exit e);
	public void setDestination(Room r);
	public Room getDestination();
	public void setInRoom(Room r);
	public Room getInRoom();
	@Override public ExitInstance newInstance();
	@Override public ExitInstance copyOf();
	public static class ComparableEI implements ExitInstance
	{
		protected static final ConcurrentLinkedQueue<ComparableEI> CEICache = new ConcurrentLinkedQueue();

		public Exit e;
		public Room r;
		@Override public String ID() {return "ComparableEI";}
		public static ComparableEI newCEI(Room R, Exit E)
		{
			ComparableEI map = CEICache.poll();
			if(map==null)
				map=new ComparableEI();
			map.r=R;
			map.e=E;
			return map;
		}
		public ComparableEI newInstance() {
			ComparableEI map = CEICache.poll();
			if(map==null)
				map=new ComparableEI();
			return map;
		}
		public ComparableEI copyOf() {
			ComparableEI map = CEICache.poll();
			if(map==null)
				map=new ComparableEI();
			map.r=r;
			map.e=e;
			return map;
		}
		public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
		public boolean equals(Object o) {
			if(!(o instanceof ExitInstance))
				return false;
			ExitInstance other=(ExitInstance)o;
			return other.getExit()==e && other.getDestination()==r;
		}
			/*
			if(!(o instanceof ExitInstance))
				return false;
			ExitInstance other=(ExitInstance)o;
			if(e!=other.getExit())
			{
				int diff=other.getExit().saveNum()-e.saveNum();
				if(diff==0) return 1;
				return diff;
			}
			if(r!=other.getDestination())
			{
				int diff=other.getDestination().saveNum()-r.saveNum();
				if(diff==0) return 1;
				return diff;
			}
			return 0;
			*/
		public void returnThis(){r=null; e=null; CEICache.offer(this);}
		
		public Exit getExit() { return e; }
		public Room getDestination() {return r;}
		public void setExit(Exit e){this.e = e;}
		public void setDestination(Room r){this.r = r;}
		public void setInRoom(Room r){}
		public Room getInRoom(){return null;}
		public String name() { return null; }
		public String plainName() { return null; }
		public void setName(String newName) { }
		public String displayText() { return null; }
		public String plainDisplayText() {return null;}
		public void setDisplayText(String newDisplayText) {}
		public String description() {return null;}
		public String plainDescription() {return null;}
		public void setDescription(String newDescription) {}
		public Environmental getEnvObject() {return null;}
		public void addListener(Listener newAffect, EnumSet<Flags> flags) {}
		public void removeListener(Listener oldAffect, EnumSet<Flags> flags) {}
		public CopyOnWriteArrayList<CharAffecter> charAffecters() {return null;}
		public CopyOnWriteArrayList<EnvAffecter> envAffecters() {return null;}
		public CopyOnWriteArrayList<OkChecker> okCheckers() {return null;}
		public CopyOnWriteArrayList<ExcChecker> excCheckers() {return null;}
		public CopyOnWriteArrayList<TickActer> tickActers() {return null;}
		public TickStat getTickStatus() {return null;}
		public boolean tick(int tickTo) {return false;}
		public int tickCounter() {return 0;}
		@Override public void initializeClass() {}
		public void addEffect(Effect to) {}
		public void delEffect(Effect to) {}
		public boolean hasEffect(Effect to) {return false;}
		public int numEffects() {return 0;}
		public Effect fetchEffect(int index) {return null;}
		public Vector<Effect> fetchEffect(String ID) {return null;}
		public Effect fetchFirstEffect(String ID) {return null;}
		public Iterator<Effect> allEffects() {return null;}
		@Override public boolean respondTo(CMMsg msg) {return true;}
		@Override public boolean respondTo(CMMsg msg, Object data) {return true;}
		@Override public void executeMsg(ExcChecker myHost, CMMsg msg) {}
		public void registerListeners(ListenHolder forThis) {}
		public void registerAllListeners() {}
		public void clearAllListeners() {}
		public int priority(ListenHolder forThis) {return 0;}
		public EnumSet<Flags> listenFlags() {return null;}
		@Override public boolean okMessage(OkChecker myHost, CMMsg msg) {return true;}
		public void addBehavior(Behavior to) {}
		public void delBehavior(Behavior to) {}
		public boolean hasBehavior(String ID) {return false;}
		public int numBehaviors() {return 0;}
		public Behavior fetchBehavior(int index) {return null;}
		public Behavior fetchBehavior(String ID) {return null;}
		public Iterator<Behavior> allBehaviors(){return null;}
		@Override public SaveEnum[] totalEnumS() {return null;}
		@Override public Enum[] headerEnumS() {return null;}
		public int saveNum() {return 0;}
		public void setSaveNum(int num) {}
		public boolean needLink() {return false;}
		public void link() {}
		public void saveThis() {}
		public void destroy() {}
		public boolean amDestroyed() {return false;}
		public void prepDefault() {}
		@Override public ModEnum[] totalEnumM() {return null;}
		@Override public Enum[] headerEnumM() {return null;}
	}
}
