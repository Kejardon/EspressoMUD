package com.planet_ink.coffee_mud.Exits;
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
public class StdExit implements Exit
{
	public String ID(){	return "StdExit";}

	protected String name="an ordinary pathway";
	protected String display="an open passage to another place.";
	protected String desc="";
	protected String exitID="";
	protected boolean visible=true;

	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	protected Vector<OkChecker> okCheckers=null;
	protected Vector<ExcChecker> excCheckers=null;
	protected Vector<TickActer> tickActers=null;
	protected Vector<Effect> affects=new Vector(1);
	protected Vector<Behavior> behaviors=new Vector(1);
	protected long lastTick=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected boolean amDestroyed=false;

	protected Environmental myEnvironmental=new Environmental.DefaultEnv(this);
	protected Closeable myDoor=null;

	public Environmental getEnvObject() {return myEnvironmental;}
	public Closeable getLidObject() {return myDoor;}

	public String exitID(){return exitID;}
	public void setExitID(String s){exitID=s;}

	public String directLook(MOB mob, Room destination)
	{
		String s=display;
		if((myDoor==null)||(myDoor.isOpen()))
			s+=" It leads to "+destination.displayText();
		return s;
	}
	public String exitListLook(MOB mob, Room destination)
	{
		if(myDoor==null)
			return destination.displayText();
		/*if (myDoor instanceof ExitDoor)
			return myDoor.exitListLook();
		*/
		if(myDoor.isOpen())
			return destination.displayText();
		return name;
	}
	public boolean visibleExit(MOB mob, Room destination) {return visible; }
	public void setVisible(boolean b){visible = b;}

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners() { }
	public void clearAllListeners() { }

//	protected void finalize(){}
	public void initializeClass(){}
	public String name(){ return name;}
	public void setName(String newName){name=newName;}
	public String displayText(){return display;}
	public void setDisplayText(String newDisplayText){display=newDisplayText;}
	public String description(){return desc;}
	public void setDescription(String newDescription){desc=newDescription;}

	public void destroy()
	{
		myEnvironmental.destroy();
		myDoor.destroy();
		affects=null;
		behaviors=null;
		amDestroyed=true;
	}
	public boolean amDestroyed(){return amDestroyed;}

	public CMObject newInstance()
	{
		try
		{
			return (Environmental)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdExit();
	} 
	protected void cloneFix(Exit E)
	{
		myEnvironmental=(Environmental)E.getEnvObject().copyOf();
		myDoor=E.getLidObject();
		if(myDoor!=null) myDoor=(Closeable)myDoor.copyOf();

		affects=null;
		behaviors=null;
		for(int b=0;b<E.numEffects();b++)
		{
			Effect B=E.fetchEffect(b);
			if(B!=null)
				addEffect((Effect)B.copyOf());
		}
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				addBehavior((Behavior)B.copyOf());
		}
	}
	public CMObject copyOf()
	{
		try
		{
			StdExit E=(StdExit)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	} 
/*	protected Rideable findALadder(MOB mob, Room room)
	{
		if(room==null) return null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.getItem(i);
			if((I!=null)
			   &&(I instanceof Rideable)
			   &&(CMLib.flags().canBeSeenBy(I,mob))
			   &&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_LADDER))
				return (Rideable)I;
		}
		return null;
	}
	protected void mountLadder(MOB mob, Rideable ladder)
	{
		CMMsg msg=CMClass.getMsg(mob,ladder,null,CMMsg.MSG_MOUNT,"<S-NAME> mounts <T-NAMESELF>.");
		Room room=(Room)((Item)ladder).owner();
		if(mob.location()==room) room=null;
		if((mob.location().okMessage(mob,msg))
		&&((room==null)||(room.okMessage(mob,msg))))
		{
			mob.location().send(mob,msg);
			if(room!=null)
				room.sendOthers(mob,msg);
		}
	} */

	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!myEnvironmental.okMessage(myHost, msg))
			return false;
		for(int i=okCheckers.size();i>0;i--)
			if(!okCheckers.get(i-1).okMessage(myHost,msg))
				return false;
		return true;
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		myEnvironmental.executeMsg(myHost, msg);
		for(int i=excCheckers.size();i>0;i--)
			excCheckers.get(i-1).executeMsg(myHost,msg);
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Affectable
	public void addEffect(Effect to)
	{
		if(to==null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Effect to)
	{
		if(affects.removeElement(to))
			to.setAffectedOne(null);
	}
	public int numEffects(){return affects.size();}
	public Effect fetchEffect(int index)
	{
		try { return affects.elementAt(index); }
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

	//Behavable
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		if(fetchBehavior(to.ID())!=null) return;
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		try { return behaviors.elementAt(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Behavior> fetchBehavior(String ID)
	{
		Vector<Behavior> V=new Vector<Behavior>();
		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				V.add(B);
		}
		return V;
	}
	public Vector<Behavior> allBehaviors(){ return (Vector<Behavior>)behaviors.clone(); }

	//Affectable/Behavable shared
	public Vector<CharAffecter> charAffecters(){return null;}
	public Vector<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	public Vector<OkChecker> okCheckers(){if(okCheckers==null) okCheckers=new Vector(); return okCheckers;}
	public Vector<ExcChecker> excCheckers(){if(excCheckers==null) excCheckers=new Vector(); return excCheckers;}
	public Vector<TickActer> tickActers(){if(tickActers==null) tickActers=new Vector(); return tickActers;}
	//TODO: Exits that tick. What do?
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
//		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
//			if(container instanceof ListenHolder)
//				((ListenHolder)container).removeListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
//		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
//			if(container instanceof ListenHolder)
//				((ListenHolder)container).addListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
		if(tickID==Tickable.TickID.Action) return false;
		tickStatus=Tickable.TickStat.Listener;
		for(int i=tickActers.size()-1;i>=0;i--)
		{
			TickActer T=tickActers.get(i);
			if(!T.tick(ticking, tickID))
				removeListener(T, EnumSet.of(ListenHolder.Flags.TICK));
		}
		tickStatus=Tickable.TickStat.Not;
		lastTick=System.currentTimeMillis();
		return true;
	}
	public long lastAct(){return 0;}	//No Action ticks
	public long lastTick(){return lastTick;}

	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
		ENV(){
			public String save(StdExit E){ return CMLib.coffeeMaker().getPropertiesStr(E.myEnvironmental); }
			public void load(StdExit E, String S){
				Environmental.DefaultEnv newEnv=new Environmental.DefaultEnv(E);
				CMLib.coffeeMaker().setPropertiesStr(newEnv, S);
				E.myEnvironmental.destroy();
				E.myEnvironmental=newEnv; } },
		EID(){
			public String save(StdExit E){ return E.exitID; }
			public void load(StdExit E, String S){ E.exitID=S.intern(); } },
		DSP(){
			public String save(StdExit E){ return E.display; }
			public void load(StdExit E, String S){ E.display=S.intern(); } },
		DSC(){
			public String save(StdExit E){ return E.desc; }
			public void load(StdExit E, String S){ E.desc=S.intern(); } },
		EFC(){
			public String save(StdExit E){ return CMLib.coffeeMaker().getVectorStr(E.affects); }
			public void load(StdExit E, String S){
				Vector<Effect> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Effect A : V)
					E.addEffect(A);
				
				} },
		BHV(){
			public String save(StdExit E){ return CMLib.coffeeMaker().getVectorStr(E.behaviors); }
			public void load(StdExit E, String S){
				Vector<Behavior> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Behavior A : V)
					E.addBehavior(A);
				
				} },
		VIS(){
			public String save(StdExit E){ return ""+E.visible; }
			public void load(StdExit E, String S){ E.visible=Boolean.getBoolean(S); } },
		NAM(){
			public String save(StdExit E){ return E.name; }
			public void load(StdExit E, String S){ E.name=S.intern(); } },
		CLS(){
			public String save(StdExit E){ return CMLib.coffeeMaker().getSubStr(E.myDoor); }
			public void load(StdExit E, String S){ E.myDoor=(Closeable)CMLib.coffeeMaker().loadSub(S); } }

		;
		public abstract String save(StdExit E);
		public abstract void load(StdExit E, String S);
		public String save(CMSavable E){return save((StdExit)E);}
		public void load(CMSavable E, String S){load((StdExit)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		ENVIRONMENTAL(){
			public String brief(StdExit E){return E.myEnvironmental.ID();}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		DISPLAY(){
			public String brief(StdExit E){return E.display;}
			public String prompt(StdExit E){return ""+E.display;}
			public void mod(StdExit E, MOB M){E.display=CMLib.genEd().stringPrompt(M, ""+E.display, false);} },
		DESCRIPTION(){
			public String brief(StdExit E){return E.display;}
			public String prompt(StdExit E){return ""+E.display;}
			public void mod(StdExit E, MOB M){E.display=CMLib.genEd().stringPrompt(M, ""+E.display, false);} },
		EFFECTS(){
			public String brief(StdExit E){return ""+E.affects.size();}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(StdExit E){return ""+E.behaviors.size();}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		VISIBLE(){
			public String brief(StdExit E){return ""+E.visible;}
			public String prompt(StdExit E){return ""+E.visible;}
			public void mod(StdExit E, MOB M){E.visible=CMLib.genEd().booleanPrompt(M, ""+E.visible);} },
		NAME(){
			public String brief(StdExit E){return E.name;}
			public String prompt(StdExit E){return E.name;}
			public void mod(StdExit E, MOB M){E.name=CMLib.genEd().stringPrompt(M, E.name, false);} },
		DOOR(){
			public String brief(StdExit E){return (E.myDoor==null)?("null"):(E.myDoor.ID());}
			public String prompt(StdExit E){return "";}
			public void mod(StdExit E, MOB M){
				if(E.myDoor==null) E.myDoor=(Closeable)CMLib.genEd().genMiscSet(M, CMLib.genEd().newAnyCloseable(M));
				else {
					char action=M.session().prompt("(E)dit or (D)estroy this closeable? (E)","E").trim().toUpperCase().charAt(0);
					if(action=='E') CMLib.genEd().genMiscSet(M, E.myDoor);
					else if(action=='D') {E.myDoor.destroy(); E.myDoor=null;} } } }
		;
		public abstract String brief(StdExit fromThis);
		public abstract String prompt(StdExit fromThis);
		public abstract void mod(StdExit toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdExit)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdExit)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdExit)toThis, M);} }

	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof StdExit)) return false;
		return true;
	}
}
