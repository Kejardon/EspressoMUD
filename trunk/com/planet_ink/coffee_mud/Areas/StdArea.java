package com.planet_ink.coffee_mud.Areas;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
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
public class StdArea implements Area
{
	public String ID(){return "StdArea";}
	protected String name="the area";
	protected SortedVector<SortedList.SortableObject<Room>> properRooms=new SortedVector<SortedList.SortableObject<Room>>();
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected long lastPlayerTime=System.currentTimeMillis();
	protected Vector affects=new Vector(1);
	protected Vector<OkChecker> okCheckers=new Vector();
	protected Vector<ExcChecker> excCheckers=new Vector();
	protected Vector<TickActer> tickActers=new Vector();
	protected HashedList<Room> tickingRooms=new HashedList<Room>();
	protected Vector<Room> totalMetroRooms=null;

	protected Vector<Area> children=null;
	protected Vector<Area> parents=null;
	protected Vector<String> childrenToLoad=new Vector(1);
//	protected Vector<String> parentsToLoad=new Vector(1);
	protected Environmental myEnvironmental=new Environmental.DefaultEnv(E);

	protected String author="";

	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}
	
	public void initializeClass(){}

	protected Vector affects=new Vector(1);
//	protected Vector subOps=new Vector(1);
	protected TimeClock myClock=null;
	public void setTimeObj(TimeClock obj){myClock=obj;}
	public TimeClock getTimeObj()
	{
		if(myClock==null) myClock=CMLib.time().globalClock();
		return myClock;
	}

	public StdArea()
	{
		super();
	}
	protected void finalize(){}
	protected boolean amDestroyed=false;
	public void destroy()
	{
		amDestroyed=true;
		affects=null;
		behaviors=null;
		author=null;
//		currency=null;
		children=null;
		parents=null;
		childrenToLoad=null;
//		parentsToLoad=null;
//		blurbFlags=null;
//		subOps=null;
		properRooms=null;
		//metroRooms=null;
		myClock=null;
//		properRoomIDSet=null;
//		metroRoomIDSet=null;
	}
	public boolean amDestroyed(){return amDestroyed;}

	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return name;
	}

	public void setName(String newName){name=newName;}
	public String Name(){return name;}

	/*  A thought occurs to me. If this is relied on to make rooms, the method that calls it must either
	 *  a) make and claim the number IMMEDIATELY after calling this and/or
	 *  b) interpret a failure to make as the number it got being taken before it got to it (so call this again)
	 */
	public String getNewRoomID()
	{
		SortedList.SortableObject<Room> lastRoomObj=defaultIDRooms.lastElement();
		if(lastRoomObj==null) return name+"#1";
		return name+"#"+(1+lastRoomObj.myInt);
	}

	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdArea();
	}
	protected void cloneFix(StdArea E)
	{	//TODO
		parents=null;
		if(E.parents!=null)
			parents=(Vector)E.parents.clone();
		children=null;
		if(E.children!=null)
			children=(Vector)E.children.clone();
		affects=new Vector(1);
		behaviors=new Vector(1);
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				behaviors.addElement((Behavior)B.copyOf());
		}
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if(A!=null)
				affects.addElement((Ability)A.copyOf());
		}
	}
	public CMObject copyOf()
	{
		try
		{
			StdArea E=(StdArea)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

//	public int compareTo(Area A){ return name.compareTo(A.name());}
	public int compareTo(CMObject o)
	{
		if(o instanceof Area)
			 return name.compareTo(A.name());
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	public boolean okMessage(OkChecker myHost, CMMsg msg)
	{
		for(int i=0;i<msg.source().size();i++)
		{
			Interactable E = msg.source().get(i);
			if((E instanceof MOB)&&(!((MOB)E).isMonster()))
			{
				lastPlayerTime=System.currentTimeMillis();
//				if((flag==Area.STATE_PASSIVE)
//				&&((msg.sourceMinor()==CMMsg.TYP_ENTER)||(msg.sourceMinor()==CMMsg.TYP_LEAVE)||(msg.sourceMinor()==CMMsg.TYP_FLEE)))
//					flag=Area.STATE_ACTIVE;
			}
		}
		for(int i=0;i<okCheckers.size();i++)
			if(!okCheckers.get(i).okMessage(this,msg)))
				return false;
		if(parents!=null)
		for(int i=0;i<parents.size();i++)
			if(!parents.elementAt(i).okMessage(myHost,msg))
				return false;
		return true;
	}

	public void executeMsg(ExcChecker myHost, CMMsg msg)
	{
		for(int i=0;i<excCheckers.size();i++)
			excChecker.get(i).executeMsg(this, msg);

		if(parents!=null)
		for(int i=0;i<parents.size();i++)
			parents.elementAt(i).executeMsg(myHost,msg);
	}

	public Tickable.TickStat getTickStatus(){return tickStatus;}

	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
		if(tickID==Tickable.TickID.Action) return false;
		tickStatus=Tickable.TickStat.Start;
		getTimeObj().tick(this,tickID);
		tickStatus=Tickable.TickStat.Listener;
		for(int i=tickActers.size()-1;i>=0;i--)
		{
			TickActer T=tickActers.get(i);
			if(!T.tick(ticking, tickID))
				removeListener(T, EnumSet.of(ListenHolder.Flags.TICK));
		}
		for(Iterator<Room> I=tickingRooms.iterator(); I.hasNext();)
		{
			Room R=I.next();
			if(!R.tick(ticking, tickID))
				tickingRooms.remove(R);
		}
		tickStatus=Tickable.TickStat.Not;
		return true;
	}

	public void addEffect(Ability to)
	{
		if(to==null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		if(affects.removeElement(to))
			to.setAffectedOne(null);
	}
	public int numEffects(){return affects.size();}
	public Effect fetchEffect(int index)
	{
		try { return (Effect)affects.elementAt(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Effect> fetchEffect(String ID)
	{
		Vector<Effect> V=new Vector<Effect>();
		for(int a=0;a<affects.size();a++)
		{
			Effect A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
				V.add(A);
		}
		return V;
	}
	public Vector<Effect> allEffects() { return (Vector<Effect>)affects.clone(); }
	public boolean inMyMetroArea(Area A)
	{
		if(A==this) return true;
		if(getNumChildren()==0) return false;
		for(int i=0;i<getNumChildren();i++)
			if(getChild(i).inMyMetroArea(A))
				return true;
		return false;
	}

	public int properSize() { return properRooms.size(); }
	public void addProperRoom(Room R)
	{
		if(R==null) return;
		String roomID=R.roomID();
		SortedList.SortableObject<Room> contR=null;
		synchronized(properRooms)
		{
			if(roomID.startsWith(name+"#"))
			{
				contR=new SortedList.SortableObject<Room>(R, Integer.parseInt(roomID.substring(roomID.indexOf("#")+1)));
				if(properRooms.contains(contR))	//This might rename the room later, but for now just stop.
					return;
			}
			else
			{
				R.setArea(null);
				R.setRoomID(getNewRoomID());
				contR=new SortedList.SortableObject<Room>(R, Integer.parseInt(roomID.substring(roomID.indexOf("#")+1)));
			}
			properRooms.add(contR);
		}
		R.setAreaRaw(this);
		clearMetroMap();
	}
	public void addTickingRoom(Room R)
	{
		tickingRooms.add(R);
	}
	public void removeTickingRoom(Room R)
	{
		tickingRooms.remove(R);
	}

	//TODO: Update this for item rooms when they're done
	public boolean isRoom(Room R)
	{
		if(R==null) return false;
		String roomID=R.roomID();
		synchronized(properRooms)
		{
			return ((roomID.startsWith(name+"#"))&&
			  (properRooms.contains(new SortedList.SortableObject<Room>(R, Integer.parseInt(roomID.substring(roomID.indexOf("#")+1))))));
		}
	}
	public void delProperRoom(Room R)
	{
		if(R==null) return;
		String roomID=R.roomID();
		SortedList.SortableObject<Room> contR=null;
		if(roomID.startsWith(name+"#"))
		synchronized(properRooms)
		{
			if(properRooms.remove(new SortedList.SortableObject<Room>(R, Integer.parseInt(roomID.substring(roomID.indexOf("#")+1)))))
				clearMetroMap();
		}
	}

	public Room getRoom(String roomID)
	{
		if(roomID.startsWith(name+"#"))
		synchronized(properRooms)
		{
			contR=new SortedList.SortableObject<Room>(null, Integer.parseInt(roomID.substring(roomID.indexOf("#")+1)));
			return properRooms.get(properRooms.indexOf(contR)).myObj;
		}
	}

	public int metroSize()
	{
		int num=properSize();
		for(int c=getNumChildren()-1;c>=0;c--)
			num+=getChild(c).metroSize();
		return num;
	}
	public Room getRandomProperRoom()
	{
		if(properRooms.size()==0) return null;
		return properRooms.get(CMath.random(properRooms.size()));
	}
	public Room getRandomMetroRoom()
	{
		/*synchronized(metroRooms)
		{
			if(metroSize()==0) return null;
			Room R=(Room)metroRooms.elementAt(CMLib.dice().roll(1,metroRooms.size(),-1));
			return R;
		}*/
		String roomID=metroRoomIDSet.random();
		Room R=CMLib.map().getRoom(roomID);
		if(R==null) Log.errOut("StdArea","Unable to random-metro-find: "+roomID);
		return R;
	}

	protected void clearMetroMap()
	{
		totalMetroRooms=null;
		for(Enumeration<Area> e=getParents();e.hasMoreElements();)
			e.next().clearMetroMap();
	}

	public Enumeration<Room> getProperMap()
	{
		Vector<Room> V=(Vector<Room>)properRooms.clone();
		return V.elements();
	}

	public Vector<Room> getMetroCollection()
	{
		if(totalMetroRooms!=null)
			return totalMetroRooms;
		totalMetroRooms = Vector<Room>(1);
		Vector<Room> tempList=totalMetroRooms;
		Vector<Room>[] toAdd=new Vector<Room>[numChildren()];
		int totalSize=properRooms.size();
		for(int i=0;i<children.size();i++)
		{
			toAdd[i]=children.get(i).metroVector();
			totalSize+=toAdd[i].size();
		}
		tempList.ensureCapacity(totalSize);
		tempList.addAll(properRooms);
		for(int i=0;i<toAdd.length;i++)
			tempList.addAll(toAdd[i]);
		return tempList;

	}
	public Enumeration getMetroMap(){return getMetroCollection().elements();}
	public void addChildToLoad(String str) { childrenToLoad.addElement(str);}

	// Children
	public void initChildren()
	{
		if(children==null)
		{
			children=new Vector(1);
			for(int i=0;i<childrenToLoad.size();i++)
			{
				Area A=CMLib.map().getArea(childrenToLoad.elementAt(i));
				if(A==null)
					continue;
				children.addElement(A);
				A.addParent(this);
			}
		}
	}
	public Enumeration getChildren() { return children.elements(); }
	public String getChildrenList()
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration<Area> e=getChildren(); e.hasMoreElements();)
		{
			Area A=e.nextElement();
			if(str.length()>0) str.append(";");
			str.append(A.name());
		}
		return str.toString();
	}

	public int getNumChildren() { return children.size(); }
	public Area getChild(int num) { return children.elementAt(num); }
	public Area getChild(String named)
	{
		for(int i=0;i<children.size();i++)
		{
			Area A=children.elementAt(i);
			if(A.name().equalsIgnoreCase(named))
				return A;
		}
		return null;
	}
	public boolean isChild(Area named) { return children.contains(named); }
	public boolean isChild(String named)
	{
		for(int i=0;i<children.size();i++)
		{
			Area A=children.elementAt(i);
			if(A.name().equalsIgnoreCase(named))
				return true;
		}
		return false;
	}
	public void addChild(Area Adopted)
	{
		Adopted.addParent(this);
		for(int i=0;i<children.size();i++)
		{
			Area A=children.elementAt(i);
			if(A.name().equalsIgnoreCase(Adopted.name()))
			{
				children.setElementAt(Adopted, i);
				return;
			}
		}
		children.addElement(Adopted);
	}
	public void removeChild(Area Disowned) { children.removeElement(Disowned); Disowned.removeParent(this);}
	public void removeChild(int Disowned) { children.remove(Disowned).removeParent(this); }
	// child based circular reference check
	// Doesn't prevent having the same parent twice. NOTE: People making areas should take care about this!
	public boolean canChild(Area newChild)
	{
		initParents();
		if(parents.contains(newChild))
			return false;
		for(int i=0;i<parents.size();i++)
		{
			Area rent=parents.elementAt(i);
			if(!(rent.canChild(newChild)))
				return false;
		}
		return true;
	}

	// Parent
	public Enumeration<Area> getParents() { initParents(); return parents.elements(); }
	public Vector<Area> getParentsRecurse()
	{
		Vector<Area> V=new Vector<Area>();
		Area A=null;
		for(Enumeration<Area> e=getParents();e.hasMoreElements();)
		{
			A=e.nextElement();
			V.addElement(A);
			CMParms.addToVector(A.getParentsRecurse(),V);
		}
		return V;
	}

	public String getParentsList()
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration<Area> e=getParents(); e.hasMoreElements();) 
		{
			Area A=e.nextElement();
			if(str.length()>0) str.append(";");
			str.append(A.name());
		}
		return str.toString();
	}

	public int getNumParents() { return parents.size(); }
	public Area getParent(int num) { return parents.elementAt(num); }
	public Area getParent(String named)
	{
		for(int i=0;i<parents.size();i++)
		{
			Area A=parents.elementAt(i);
			if(A.name().equalsIgnoreCase(named))
				return A;
		}
		return null;
	}
	public boolean isParent(Area named)
	{
		return parents.contains(named);
	}
	public boolean isParent(String named)
	{
		for(int i=0;i<parents.size();i++)
		{
			Area A=parents.elementAt(i);
			if(A.name().equalsIgnoreCase(named))
				return true;
		}
		return false;
	}
	public void addParent(Area Adopted)
	{
		for(int i=0;i<parents.size();i++)
			if(Adopted.name().equalsIgnoreCase(parents.elementAt(i).name()))
			{
				parents.setElementAt(Adopted, i);
				return;
			}
		parents.addElement(Adopted);
	}
	public void removeParent(Area Disowned) { parents.removeElement(Disowned); }
	public void removeParent(int Disowned) { parents.removeElementAt(Disowned); }
	//Redundant with canChild
	public boolean canParent(Area newParent)
	{
		initChildren();
		if(children.contains(newParent))
			return false;
		for(int i=0;i<children.size();i++)
		{
			Area child=children.elementAt(i);
			if(!(child.canParent(newParent)))
				return false;
		}
		return true;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
//		NAM(){
//			public String save(StdArea E){ return E.name; }
//			public void load(StdArea E, String S){ E.name=S.intern(); } },
		CHL(){
			public String save(StdArea E){ return getChildrenList(); }
			public void load(StdArea E, String S){
				while(S.length()>0) {
					addChildToLoad(S.substring(0,S.indexOf(';')));
					S=S.substring(S.indexOf(';')+1); } } },
		ENV(){
			public String save(StdArea E){ return CMLib.coffeeMaker().getPropertiesStr(E.myEnvironmental); }
			public void load(StdArea E, String S){
				Environmental.DefaultEnv newEnv=new Environmental.DefaultEnv(E);
				CMLib.coffeeMaker().setPropertiesStr(newEnv, S);
				E.myEnvironmental.destroy();
				E.myEnvironmental=newEnv; } },
		EFC(){
			public String save(StdItem E){ return CMLib.coffeeMaker().getVectorStr(E.affects); }
			public void load(StdItem E, String S){
				Vector<Effect> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Effect A : V)
					E.addEffect(A);
				
				} },
		ATH(){
			public String save(StdArea E){ return E.author; }
			public void load(StdArea E, String S){ E.author=S.intern(); } }
		;	//Children/toload, parents/toload, blurbflags, properrooms, IDSets
		public abstract String save(StdArea E);
		public abstract void load(StdArea E, String S);
		public String save(CMSavable E){return save((StdArea)E);}
		public void load(CMSavable E, String S){load((StdArea)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		NAME(){
			public String brief(StdArea E){return E.name;}
			public String prompt(StdArea E){return E.name;}
			public void mod(StdArea E, MOB M){
				String newName=CMLib.genEd().stringPrompt(M, E.name, false);
				if(E.name.equals(newName)) return;
				for(Enumeration<Room> e=getProperMap();e.hasNext();)
				{
					Room R=e.next();
					R.setName(newName+R.name().substring(R.name().indexOf('#')));
				}
				E.name=newName;} },
		EFFECTS(){
			public String brief(StdArea E){return ""+E.affects.size();}
			public String prompt(StdArea E){return "";}
			public void mod(StdArea E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		ENVIRONMENTAL(){
			public String brief(StdArea E){return E.myEnvironmental.ID();}
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
					else M.session().rawPrintln("That would cause a circular reference."); } } }
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
					else M.session().rawPrintln("That would cause a circular reference."); } } }
		;
		public abstract String brief(StdArea fromThis);
		public abstract String prompt(StdArea fromThis);
		public abstract void mod(StdArea toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdArea)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdArea)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdArea)toThis, M);} }

/*
//	public void addParentToLoad(String str) { parentsToLoad.addElement(str);}
	public void addMetroRoom(Room R)
	{
		if(R!=null)
			addMetroRoomnumber(R.roomID());
	}
	public void delMetroRoom(Room R)
	{
		if(R!=null)
			delMetroRoomnumber(R.roomID());
	}
	public void addProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
		{
			getProperRoomnumbers().add(roomID);
			addMetroRoomnumber(roomID);
		}
	}
	public void delProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
		{
			getProperRoomnumbers().remove(roomID);
			delMetroRoomnumber(roomID);
		}
	}
	public void addMetroRoomnumber(String roomID)
	{
		if(metroRoomIDSet==null)
			metroRoomIDSet=(RoomnumberSet)getProperRoomnumbers().copyOf();
		if((roomID!=null)&&(roomID.length()>0)&&(!metroRoomIDSet.contains(roomID)))
		{
			metroRoomIDSet.add(roomID);
			if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			for(int p=getNumParents()-1;p>=0;p--)
				getParent(p).addMetroRoomnumber(roomID);
		}
	}
	public void delMetroRoomnumber(String roomID)
	{
		if((metroRoomIDSet!=null)
		&&(roomID!=null)
		&&(roomID.length()>0)
		&&(metroRoomIDSet.contains(roomID)))
		{
			metroRoomIDSet.remove(roomID);
			if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			for(int p=getNumParents()-1;p>=0;p--)
				getParent(p).delMetroRoomnumber(roomID);
		}
	}

	public int[] getAreaIStats()
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return new int[Area.AREASTAT_NUMBER];
		int[] statData=(int[])Resources.getResource("STATS_"+Name().toUpperCase());
		if(statData!=null) return statData;
		synchronized(("STATS_"+Name()).intern())
		{
			Resources.removeResource("HELP_"+Name().toUpperCase());
			Vector levelRanges=new Vector();
			statData=new int[Area.AREASTAT_NUMBER];
			statData[Area.AREASTAT_POPULATION]=0;
			statData[Area.AREASTAT_MINLEVEL]=Integer.MAX_VALUE;
			statData[Area.AREASTAT_MAXLEVEL]=Integer.MIN_VALUE;
			statData[Area.AREASTAT_AVGLEVEL]=0;
			statData[Area.AREASTAT_MEDLEVEL]=0;
			statData[Area.AREASTAT_TOTLEVEL]=0;
			statData[Area.AREASTAT_INTLEVEL]=0;
			statData[Area.AREASTAT_VISITABLEROOMS]=getProperRoomnumbers().roomCountAllAreas();
			Room R=null;
			MOB mob=null;
			for(Enumeration r=getProperMap();r.hasMoreElements();)
			{
				R=(Room)r.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					mob=R.fetchInhabitant(i);
					if((mob!=null)&&(mob.isMonster()))
					{
						int lvl=mob.baseEnvStats().level();
						levelRanges.addElement(Integer.valueOf(lvl));
						statData[Area.AREASTAT_POPULATION]++;
						statData[Area.AREASTAT_TOTLEVEL]+=lvl;
						if(!CMLib.flags().isAnimalIntelligence(mob))
							statData[Area.AREASTAT_INTLEVEL]+=lvl;
						if(lvl<statData[Area.AREASTAT_MINLEVEL])
							statData[Area.AREASTAT_MINLEVEL]=lvl;
						if(lvl>statData[Area.AREASTAT_MAXLEVEL])
							statData[Area.AREASTAT_MAXLEVEL]=lvl;
					}
				}
			}
			if((statData[Area.AREASTAT_POPULATION]==0)||(levelRanges.size()==0))
			{
				statData[Area.AREASTAT_MINLEVEL]=0;
				statData[Area.AREASTAT_MAXLEVEL]=0;
			}
			else
			{
				Collections.sort(levelRanges);
				statData[Area.AREASTAT_MEDLEVEL]=((Integer)levelRanges.elementAt((int)Math.round(Math.floor(CMath.div(levelRanges.size(),2.0))))).intValue();
				statData[Area.AREASTAT_AVGLEVEL]=(int)Math.round(CMath.div(statData[Area.AREASTAT_TOTLEVEL],statData[Area.AREASTAT_POPULATION]));
			}

			Resources.submitResource("STATS_"+Name().toUpperCase(),statData);
		}
		return statData;
	}
	public void setCurrency(String newCurrency)
	{
		if(currency.length()>0)
		{
			CMLib.beanCounter().unloadCurrencySet(currency);
			currency=newCurrency;
			for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				CMLib.beanCounter().getCurrencySet(((Area)e.nextElement()).getCurrency());
		}
		else
		{
			currency=newCurrency;
			CMLib.beanCounter().getCurrencySet(currency);
		}
	}
	public String getCurrency(){return currency;}
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
	public synchronized RoomnumberSet getProperRoomnumbers()
	{
		if(properRoomIDSet==null)
			properRoomIDSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		return properRoomIDSet;
	}
	public RoomnumberSet getCachedRoomnumbers()
	{
		RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		synchronized(properRooms)
		{
			Room R=null;
			for(int p=properRooms.size()-1;p>=0;p--)
			{
				R=properRooms.elementAt(p);
				if(R.roomID().length()>0)
					set.add(R.roomID());
			}
		}
		return set;
	}
	public void setAreaState(int newState)
	{
		if((newState==0)&&(!CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
			CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
		flag=newState;
	}
	public int getAreaState(){return flag;}
	public boolean amISubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				return true;
		}
		return false;
	}
	public String getSubOpList()
	{
		StringBuffer list=new StringBuffer("");
		for(int s=subOps.size()-1;s>=0;s--)
		{
			String str=(String)subOps.elementAt(s);
			list.append(str);
			list.append(";");
		}
		return list.toString();
	}
	public void setSubOpList(String list)
	{
		subOps=CMParms.parseSemicolons(list,true);
	}
	public void addSubOp(String username){subOps.addElement(username);}
	public void delSubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				subOps.removeElementAt(s);
		}
	}
	public synchronized StringBuffer getAreaStats()
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return new StringBuffer("");
		StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+Name().toUpperCase());
		if(s!=null) return s;
		s=new StringBuffer("");
		int[] statData=getAreaIStats();
		s.append(description()+"\n\r");
		if(author.length()>0)
			s.append("Author         : "+author+"\n\r");
		s.append("Number of rooms: "+statData[Area.AREASTAT_VISITABLEROOMS]+"\n\r");
		if(statData[Area.AREASTAT_POPULATION]==0)
		{
			if(getProperRoomnumbers().roomCountAllAreas()/2<properRooms.size())
				s.append("Population     : 0\n\r");
		}
		else
		{
			s.append("Population     : "+statData[Area.AREASTAT_POPULATION]+"\n\r");
			String currName=CMLib.beanCounter().getCurrency(this);
			if(currName.length()>0)
				s.append("Currency       : "+CMStrings.capitalizeAndLower(currName)+"\n\r");
			else
				s.append("Currency       : Gold coins (default)\n\r");
			s.append("Level range    : "+statData[Area.AREASTAT_MINLEVEL]+" to "+statData[Area.AREASTAT_MAXLEVEL]+"\n\r");
			s.append("Average level  : "+statData[Area.AREASTAT_AVGLEVEL]+"\n\r");
			s.append("Median level   : "+statData[Area.AREASTAT_MEDLEVEL]+"\n\r");
			try{
				String flag=null;
				int num=numAllBlurbFlags();
				boolean blurbed=false;
				for(int i=0;i<num;i++)
				{
					flag=this.getBlurbFlag(i);
					if(flag!=null) flag=getBlurbFlag(flag);
					if(flag!=null)
					{
						if(!blurbed){blurbed=true; s.append("\n\r");}
						s.append(flag+"\n\r");
					}
				}
				if(blurbed) s.append("\n\r");
			}catch(Exception e){}
		}
		//Resources.submitResource("HELP_"+Name().toUpperCase(),s);
		return s;
	}
	public int numberOfProperIDedRooms()
	{
		int num=0;
		for(Enumeration e=getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				num++;
		}
		return num;
	}

//	public void setProperRoomnumbers(RoomnumberSet set){ properRoomIDSet=set;}
/*	protected int getProperIndex(Room R)
	{
		if(properRooms.size()==0) return -1;
		if(R.roomID().length()==0) return 0;
		String roomID=R.roomID();
		synchronized(properRooms)
		{
			int start=0;
			int end=properRooms.size()-1;
			int mid=0;
			while(start<=end)
			{
				mid=(end+start)/2;
				int comp=properRooms.elementAt(mid).roomID().compareToIgnoreCase(roomID);
				if(comp==0) return mid;
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
			}
			if(end<0) return 0;
			if(start>=properRooms.size()) return properRooms.size()-1;
			return mid;
		}
	}
*/
*/
}
