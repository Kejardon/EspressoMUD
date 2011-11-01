package com.planet_ink.coffee_mud.Races.interfaces;
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
import java.util.*;

/*
Ok. Planning time. Bodies will have a weighted list of races and derive their actual characteristics from that list and saved scale modifiers.
Bodymap. There will be a set of limbs with some standardized naming scheme.
	Lowercase start (tail1) means not mirrored.
	Uppercase start (Arm1) means mirrored, there will always be two of them. Can prepend with L or R to specificy a specific one.
		Can table-check against itself, mirrored values will be the distance between each. Otherwise it's center-distance to target distance.
		If L or R is specified for both (LArm1 LLeg1), sqrt((difference of self-self)^2+(base-to-base)^2)
		If L or R is specified for one (LArm1, Leg1), sqrt((specified self)^2+(base-to-base)^2). Same with mirrored to not-mirrored.
*/
@SuppressWarnings("unchecked")
public interface Body extends Item	//, Rideable
{
	public MOB mob();
	public void setMob(MOB mob);

//	public int initializeBirthday(int ageHours, Race R);
	public int[] birthday();

	public CharStats baseCharStats();
	public CharStats charStats();
	public void recoverCharStats();
//	public void resetToMaxState();
	public void setBaseCharStats(CharStats newBaseCharStats);
	//public void randomizeSizeWeightFactor();
	//public void setSizeWeightFactors(int size, int weight);
	//public int sizeFactor();
	//public int weightFactor();

	public String healthText(MOB viewer);

	// Chance of impregnation(0-100) when having sex with the target. Can be negative- 0 means 'impossible naturally but possible with magic/science aid', -100 means 'completely impossible'
//	public int fertile(Body target);

	public boolean amDead();
	public Body killMeDead();
	public void bringToLife(Room newLocation, boolean resetStats);
	public void bringToLife();
	//public Race myRace();
	//public void setRace();

//	TODO: Make Gender object in this folder
//	public Gender gender();
//	public void setGender(Gender g);

	public enum Part	//Typically recognized parts. This will be part of the standardized naming scheme
	{
		Head,
		Neck,
		Torso,	//This should have more probably? :/
		Arm,
		ArmSegment,
		Hand,
		Palm,
		Finger,	//FingerSegments are kinda feasible but that would get too detailed IMO
		Leg,
		LegSegment,
		Foot,
		FootBridge,	//equivalent to palm
		Toe
	}
	
	public static class BodyPart
	{
		private Wearable.FitType type;
		private int[] nums;
		private Part part;
		private Vector<BodyPart> sub=new Vector<BodyPart>();
		private Vector<Wearable> equip=new Vector<Wearable>();
		public Wearable.FitType fitType(){return type;}
		public int[] nums(){return nums;}
		public Part part(){return part;}
		public Vector<BodyPart> subSegments() {return sub;}
		public Vector<Wearable> equipment() {return equip;}
		public void setFit(Wearable.FitType type){this.type=type;}
		public void setNums(int[] nums){this.nums=nums;}
		public void setPart (Part part){this.part=part;}
	}

	public static class DefaultBody implements Body
	{
		public String ID(){	return "DefaultBody";}

		protected String name="a generic body";
		protected String display="a nondescript person is here.";
		protected String desc="";
//		protected String miscText="";
//		protected boolean damagable=false;
//		protected int baseGoldValue=0;
//		protected int wornOut=0;
		protected int ridesNumber=0;
		protected Rideable ride=null;
		protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC,ListenHolder.Flags.TICK);
		protected Vector<CharAffecter> charAffecters=null;
		protected Vector<OkChecker> okCheckers=null;
		protected Vector<ExcChecker> excCheckers=null;
		protected Vector<TickActer> tickActers=null;
		protected Vector affects=new Vector(1);
		protected Vector behaviors=new Vector(1);
		protected long lastTick=0;
		protected int actTimer=0;
		protected CMObject container=null;
		protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
		protected boolean dead=false;
		protected Environmental myEnvironmental=new Environmental.DefaultEnv(this);
		protected MOB myMob=null;
		protected CharStats baseCharStats=(CharStats)CMClass.Objects.COMMON.getNew("BodyCharStats");
		protected CharStats charStats=(CharStats)CMClass.Objects.COMMON.getNew("BodyCharStats");
		protected int[] birthday={-1, -1, -1};
	
		public Environmental getEnvObject() {return myEnvironmental;}
	
		public MOB mob(){return myMob;}
		public void setMob(MOB mob){myMob=mob;}

		public int[] birthday(){return birthday;}
//		public int initializeBirthday(int ageHours, Race R) { }

		public CharStats baseCharStats(){return baseCharStats;}
		public CharStats charStats(){return charStats;}
		public void recoverCharStats()
		{
			baseCharStats.copyStatic(charStats);
			for(int a=charAffecters.size();a>0;a--)
				charAffecters.get(a-1).affectCharStats(this,charStats);
		}

//		public void resetToMaxState() { charStats.resetState(); }
		public void setBaseCharStats(CharStats newBaseCharStats)
		{
			baseCharStats=(CharStats)newBaseCharStats.copyOf();
			charStats=(CharStats)CMClass.Objects.COMMON.getNew(newBaseCharStats.ID());
			recoverCharStats();
			charStats.resetState();
		}

		public String healthText(MOB viewer)
		{
			String mxp="^<!ENTITY vicmaxhp \""+charStats().getMaxPoints(CharStats.Points.HIT)+"\"^>^<!ENTITY vichp \""+charStats().getPoints(CharStats.Points.HIT)+"\"^>^<Health^>^<HealthText \""+name()+"\"^>";
			//TODO: Race stuff for healthText
			return mxp+standardHealthText(viewer)+"^</HealthText^>";
		}
		public String standardHealthText(MOB viewer)
		{
			String[] healthDescs=CMProps.getSListVar("HEALTH_CHART");
			int num=healthDescs.length;
			int pct=(int)(charStats().getPointsPercent(CharStats.Points.HIT)*(num-1));
			if(pct<0) pct=0;
			if(pct>=num) pct=num-1;
			return CMStrings.replaceAll(healthDescs[pct],"<MOB>",myMob.displayName(viewer));
		}

		public boolean amDead(){return dead;}
		public Body killMeDead()
		{
			dead=true;
			charStats().setPoints(CharStats.Points.HIT, 0);
			return this;
		}
		public void bringToLife(Room newLocation, boolean resetStats)
		{
			newLocation.bringHere(this, false);
			bringToLife();
			if(resetStats)
			{
				recoverCharStats();
				charStats().resetState();
			}
		}
		public void bringToLife()
		{
			dead=false;
			if(baseCharStats().getPoints(CharStats.Points.HIT)<=0)
				if(baseCharStats().setPoints(CharStats.Points.HIT, 1))
					baseCharStats().setMaxPoints(CharStats.Points.HIT, 1);
			if(charStats().getPoints(CharStats.Points.HIT)<=0)
				if(charStats().setPoints(CharStats.Points.HIT, 1))
					charStats().setMaxPoints(CharStats.Points.HIT, 1);
		}

		public int priority(ListenHolder L){return Integer.MAX_VALUE;}
		public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
		public void registerAllListeners()
		{
			if(container instanceof ListenHolder)
				((ListenHolder)container).addListener(this, lFlags);
		}
		public void clearAllListeners()
		{
			if(container instanceof ListenHolder)
				((ListenHolder)container).removeListener(this, lFlags);
		}
		public DefaultBody() { }
		public void initializeClass(){}
		public void setName(String newName){name=newName;}
		public String name(){ return name;}
		public String displayText(){return display;}
		public void setDisplayText(String newDisplayText){display=newDisplayText;}
		public String description(){return desc;}
		public void setDescription(String newDescription){desc=newDescription;}
		public void setMiscText(String newMiscText){}
		public String text(){return "";}
		public int ridesNumber(){return ridesNumber;}
		public Rideable ride(){return ride;}
		public void setRide(Rideable R){ride=R;}
		public CMObject container(){return container;}
		public void setContainer(CMObject E)
		{
			if(E==container) return;
			clearAllListeners();
			container=E;
			registerAllListeners();
		}
	
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
			try { return (Behavior)behaviors.elementAt(index); }
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
		public Vector<CharAffecter> charAffecters(){if(charAffecters==null) charAffecters=new Vector(); return charAffecters;}
		public Vector<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
		public Vector<OkChecker> okCheckers(){if(okCheckers==null) okCheckers=new Vector(); return okCheckers;}
		public Vector<ExcChecker> excCheckers(){if(excCheckers==null) excCheckers=new Vector(); return excCheckers;}
		public Vector<TickActer> tickActers(){if(tickActers==null) tickActers=new Vector(); return tickActers;}
		public void removeListener(Listener oldAffect, EnumSet flags)
		{
			ListenHolder.O.removeListener(this, oldAffect, flags);
		}
		public void addListener(Listener newAffect, EnumSet flags)
		{
			ListenHolder.O.addListener(this, newAffect, flags);
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
	
		public CMObject newInstance()
		{
			try
			{
				return (CMObject)this.getClass().newInstance();
			}
			catch(Exception e)
			{
				Log.errOut(ID(),e);
			}
			return new DefaultBody();
		}
		protected void cloneFix(Item E)
		{
	//		destroyed=false;
			myEnvironmental=(Environmental)E.getEnvObject().copyOf();
	
			affects=null;
			behaviors=null;
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if(B!=null)	addBehavior((Behavior)B.copyOf());
			}
	
			for(int a=0;a<E.numEffects();a++)
			{
				Effect A=E.fetchEffect(a);
				if(A!=null)
					addEffect((Effect)A.copyOf());
			}
		}
		public CMObject copyOf()
		{
			try
			{
				DefaultBody E=(DefaultBody)this.clone();
				E.cloneFix(this);
				return E;
	
			}
			catch(CloneNotSupportedException e)
			{
				return this.newInstance();
			}
		}
	
		public int value() { return 0; }
		public int baseGoldValue(){return 0;}
		public void setBaseValue(int newValue) { }
		public int wornOut() { return 0; }
		public void setWornOut(int worn) { }
		public boolean damagable() {return false;}
		public void setDamagable(boolean bool){}

		//TODO?
		public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

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

		public int recursiveWeight()
		{
			int weight=myEnvironmental.envStats().weight();
			ItemCollection subItems=ItemCollection.DefaultItemCol.getFrom(this);
			if(subItems!=null)
			{
				for(int i=0;i<subItems.numItems();i++)
				{
					Item thisItem=subItems.getItem(i);
					if(thisItem!=null)
						weight+=thisItem.recursiveWeight();
				}
			}
			return weight;
		}
		
		public void destroy()
		{
			clearAllListeners();
			myEnvironmental.destroy();
	
			dead=true;
	
			ItemCollection owner=ItemCollection.DefaultItemCol.getFrom(container);
			if(owner!=null)
				owner.removeItem(this);
			
			ItemCollection inv=ItemCollection.DefaultItemCol.getFrom(this);
			if(inv!=null)
			{
				if(owner==null)
					for(int i=inv.numItems()-1;i>=0;i--)
						inv.getItem(i).destroy();
				else
					for(int i=owner.numItems()-1;i>=0;i--)
						owner.addItem(inv.getItem(i));
			}
		}
		public boolean amDestroyed(){return false;}

		//CMModifiable and CMSavable
		public SaveEnum[] totalEnumS(){return SCode.values();}
		public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
		public ModEnum[] totalEnumM(){return MCode.values();}
		public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

		private enum SCode implements CMSavable.SaveEnum{
			NAM(){
				public String save(DefaultBody E){ return ""+E.name; }
				public void load(DefaultBody E, String S){ E.name=S.intern(); } },
			DSP(){
				public String save(DefaultBody E){ return E.display; }
				public void load(DefaultBody E, String S){ E.display=S.intern(); } },
			DSC(){
				public String save(DefaultBody E){ return E.desc; }
				public void load(DefaultBody E, String S){ E.desc=S.intern(); } },
			ENV(){
				public String save(DefaultBody E){ return CMLib.coffeeMaker().getPropertiesStr(E.myEnvironmental); }
				public void load(DefaultBody E, String S){
					Environmental.DefaultEnv newEnv=new Environmental.DefaultEnv(E);
					CMLib.coffeeMaker().setPropertiesStr(newEnv, S);
					E.myEnvironmental.destroy();
					E.myEnvironmental=newEnv; } },
			RNM(){
				public String save(DefaultBody E){
					if((CMProps.Strings.MUDSTATUS.property().startsWith("Shutting"))&&(E.ride!=null))
						return ""+E.ride.saveNumber();
					return "0"; }
				public void load(DefaultBody E, String S){
					E.ridesNumber=Integer.parseInt(S);
					if(E.container instanceof ItemCollection) {
						ItemCollection sur=(ItemCollection)E.container;
						for(int i=0;i<sur.numItems();i++) if(sur.getItem(i) instanceof Rideable) {
							Rideable ride=(Rideable)sur.getItem(i);
							if(ride.rideNumber()==E.ridesNumber) {
								ride.addRider(E);
								break; } } } } },
			EFC(){
				public String save(DefaultBody E){ return CMLib.coffeeMaker().getVectorStr(E.affects); }
				public void load(DefaultBody E, String S){
					Vector<Effect> V=CMLib.coffeeMaker().setVectorStr(S);
					for(Effect A : V)
						E.addEffect(A);
					
					} },
			BCS(){
				public String save(DefaultBody E){ return CMLib.coffeeMaker().getSubStr(E.baseCharStats); }
				public void load(DefaultBody E, String S){ E.baseCharStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
			CHS(){
				public String save(DefaultBody E){ return CMLib.coffeeMaker().getSubStr(E.charStats); }
				public void load(DefaultBody E, String S){ E.charStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
			BDY(){
				public String save(DefaultBody E){ return CMLib.coffeeMaker().savAInt(E.birthday); }
				public void load(DefaultBody E, String S){ E.birthday=CMLib.coffeeMaker().loadAInt(S); } },
			DED(){
				public String save(DefaultBody E){ return ""+E.dead; }
				public void load(DefaultBody E, String S){ E.dead=Boolean.getBoolean(S); } },
			MOB(){
				public String save(DefaultBody E){ if(E.myMob==null||!E.myMob.isMonster()) return ""; return CMLib.coffeeMaker().getSubStr(E.myMob); }
				public void load(DefaultBody E, String S){ E.charStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
			;
			public abstract String save(DefaultBody E);
			public abstract void load(DefaultBody E, String S);
			public String save(CMSavable E){return save((DefaultBody)E);}
			public void load(CMSavable E, String S){load((DefaultBody)E, S);} }
		private enum MCode implements CMModifiable.ModEnum{
			NAME(){
				public String brief(DefaultBody E){return ""+E.name;}
				public String prompt(DefaultBody E){return ""+E.name;}
				public void mod(DefaultBody E, MOB M){E.name=CMLib.genEd().stringPrompt(M, ""+E.name, false);} },
			DISPLAY(){
				public String brief(DefaultBody E){return E.display;}
				public String prompt(DefaultBody E){return ""+E.display;}
				public void mod(DefaultBody E, MOB M){E.display=CMLib.genEd().stringPrompt(M, ""+E.display, false);} },
			DESCRIPTION(){
				public String brief(DefaultBody E){return E.display;}
				public String prompt(DefaultBody E){return ""+E.display;}
				public void mod(DefaultBody E, MOB M){E.display=CMLib.genEd().stringPrompt(M, ""+E.display, false);} },
			EFFECTS(){
				public String brief(DefaultBody E){return ""+E.affects.size();}
				public String prompt(DefaultBody E){return "";}
				public void mod(DefaultBody E, MOB M){CMLib.genEd().modAffectable(E, M);} },
			BEHAVIORS(){
				public String brief(DefaultBody E){return ""+E.behaviors.size();}
				public String prompt(DefaultBody E){return "";}
				public void mod(DefaultBody E, MOB M){CMLib.genEd().modBehavable(E, M);} },
			DEAD(){
				public String brief(DefaultBody E){return ""+E.dead;}
				public String prompt(DefaultBody E){return ""+E.dead;}
				public void mod(DefaultBody E, MOB M){E.dead=CMLib.genEd().booleanPrompt(M, ""+E.dead);} },
			ENVIRONMENTAL(){
				public String brief(DefaultBody E){return E.myEnvironmental.ID();}
				public String prompt(DefaultBody E){return "";}
				public void mod(DefaultBody E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
			MOB(){
				public String brief(DefaultBody E){return E.myMob==null?"null":E.myMob.ID();}
				public String prompt(DefaultBody E){return "";}
				public void mod(DefaultBody E, MOB M){CMLib.genEd().genMiscSet(M, E.myMob);} },
			CHARSTATS(){
				public String brief(DefaultBody E){return E.charStats.ID();}
				public String prompt(DefaultBody E){return "";}
				public void mod(DefaultBody E, MOB M){CMLib.genEd().genMiscSet(M, E.charStats);} },
			BASECHARSTATS(){
				public String brief(DefaultBody E){return E.baseCharStats.ID();}
				public String prompt(DefaultBody E){return "";}
				public void mod(DefaultBody E, MOB M){CMLib.genEd().genMiscSet(M, E.baseCharStats);} },
			BIRTHDAY(){	//TODO: Better birthday modification code
				public String brief(DefaultBody E){return ""+E.birthday;}
				public String prompt(DefaultBody E){return ""+E.birthday;}
				public void mod(DefaultBody E, MOB M){CMLib.genEd().aintPrompt(M, E.birthday);} },
			;
			public abstract String brief(DefaultBody fromThis);
			public abstract String prompt(DefaultBody fromThis);
			public abstract void mod(DefaultBody toThis, MOB M);
			public String brief(CMModifiable fromThis){return brief((DefaultBody)fromThis);}
			public String prompt(CMModifiable fromThis){return prompt((DefaultBody)fromThis);}
			public void mod(CMModifiable toThis, MOB M){mod((DefaultBody)toThis, M);} }
		public boolean sameAs(Interactable E)
		{
	/*TODO
			if(!(E instanceof DefaultBody)) return false;
			return true;
	*/
			return false;
		}
	}
}