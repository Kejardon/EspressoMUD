package com.planet_ink.coffee_mud.core.interfaces;
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
public interface Rideable extends CMObject, CMModifiable, CMSavable //on second thought not Environmental. Let's have this mimic ItemCollection.
{
	public static interface RideHolder extends CMObject { public Rideable getRideObject(); }
	//TODO GENERAL NOTE:
	//When the ride moves it should move all riders on it automatically (late response).
	//When a mob moves the rider should have a (mid) response to remove the rider.
	public boolean isMobileRide();
	public void setMobileRide(boolean mob);
	public boolean canBeRidden(Item E);
	public boolean hasRider(Item mob);
	public void addRider(Item mob);
	public void removeRider(Item mob);
	public Item removeRider(int i);
	public Item getRider(int which);
	public Vector<Item> allRiders();
	public int numRiders();
	public int rideNumber();
	public void setRideNumber(int i);
	public int saveNumber();
	/* Returns a string grammatically correct for the given rider when
	 * they are mounted on this Rideable */
	public String stateString(Item R);
	public void setStateString(String S);
	/* Returns a string grammatically correct for the given rider when
	 * they are putting something on this Rideable */
	public String putString(Item R);
	public void setPutString(String S);
	/* Returns a string grammatically correct for this Rideable when
	 * Riders are mounted */
	public String stateStringSubject(Item R);
	public void setStateStringSubject(String S);
	/* Returns a string grammatically correct for the given rider when
	 * they are mounting this Rideable */
	public String mountString(Item R);
	public void setMountString(String S);
	/* Returns a string grammatically correct for the given rider when
	 * they are dismounting this Rideable */
	public String dismountString(Item R);
	public void setDismountString(String S);

	public static class RideThing
	{
		private static int rideNumber=1;
		private static boolean started=false;
		public synchronized static int getNumber()
		{
			//TODO: Finish this up once DB interface is better set up! Load from Misc, "Rideable", get an int.
			if(!started)
			{
				started=true;
				rideNumber=1;
			}
			return rideNumber++;
		}
//		public static void setNumber(int i){rideNumber=i;}
		public static void shutdown()
		{
			//TODO: Finish this up once DB interface is better set up! Write to Misc, "Rideable", set an int.
			//Also set up a library to call this on shutdown!
			
		}
	}

	public enum SCode implements CMSavable.SaveEnum{
		RID(){
			public String save(DefaultRideable E){
				if(CMProps.Strings.MUDSTATUS.property().startsWith("Shutting"))	//A little hackish but probably the best option
					return ""+E.saveNumber();
				return "0";}
			public void load(DefaultRideable E, String S){E.setRideNumber(Integer.parseInt(S));} },
		MBL(){
			public String save(DefaultRideable E){return ""+E.isMobileRide(); }
			public void load(DefaultRideable E, String S){E.setMobileRide(Boolean.getBoolean(S));} },
		PUT(){
			public String save(DefaultRideable E){return E.putString(null);}
			public void load(DefaultRideable E, String S){E.setPutString(S.intern());} },
		STA(){
			public String save(DefaultRideable E){return E.stateString(null);}
			public void load(DefaultRideable E, String S){E.setStateString(S.intern());} },
		MNT(){
			public String save(DefaultRideable E){return E.mountString(null);}
			public void load(DefaultRideable E, String S){E.setMountString(S.intern());} },
		DIS(){
			public String save(DefaultRideable E){return E.dismountString(null);}
			public void load(DefaultRideable E, String S){E.setDismountString(S.intern());} },
		SUB(){
			public String save(DefaultRideable E){return E.stateStringSubject(null);}
			public void load(DefaultRideable E, String S){E.setStateStringSubject(S.intern());} }
		;
		public abstract String save(DefaultRideable E);
		public abstract void load(DefaultRideable E, String S);
		public String save(CMSavable E){return save((DefaultRideable)E);}
		public void load(CMSavable E, String S){load((DefaultRideable)E, S);} }
	public static enum MCode implements ModEnum{
		RIDERS() {
			public String brief(DefaultRideable E){return ""+E.numRiders();}
			public String prompt(DefaultRideable E){return "";}
			public void mod(DefaultRideable E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done))
				{
					Vector<Item> V=E.allRiders();
					int i=CMLib.genEd().promptVector(M, V, false);
					if(--i<0) done=true;
					else if(i<V.size())
					{
						char action=M.session().prompt("(E)ject or (M)odify "+V.get(i).name()+" (default E)? ","E").trim().toUpperCase().charAt(0);
						if(action=='E') E.removeRider(V.get(i));
						else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i));
					}
				} } },
		MOBILE() {
			public String brief(DefaultRideable E){return ""+E.isMobileRide();}
			public String prompt(DefaultRideable E){return ""+E.isMobileRide();}
			public void mod(DefaultRideable E, MOB M){E.setMobileRide(CMLib.genEd().booleanPrompt(M, ""+E.isMobileRide()));} },
		PUT() {
			public String brief(DefaultRideable E){return E.putString(null);}
			public String prompt(DefaultRideable E){return E.putString(null);}
			public void mod(DefaultRideable E, MOB M){E.setPutString(CMLib.genEd().stringPrompt(M, E.putString(null), false));} },
		STATE() {
			public String brief(DefaultRideable E){return E.stateString(null);}
			public String prompt(DefaultRideable E){return E.stateString(null);}
			public void mod(DefaultRideable E, MOB M){E.setStateString(CMLib.genEd().stringPrompt(M, E.stateString(null), false));} },
		MOUNT() {
			public String brief(DefaultRideable E){return E.mountString(null);}
			public String prompt(DefaultRideable E){return E.mountString(null);}
			public void mod(DefaultRideable E, MOB M){E.setMountString(CMLib.genEd().stringPrompt(M, E.mountString(null), false));} },
		DISMOUNT() {
			public String brief(DefaultRideable E){return E.dismountString(null);}
			public String prompt(DefaultRideable E){return E.dismountString(null);}
			public void mod(DefaultRideable E, MOB M){E.setDismountString(CMLib.genEd().stringPrompt(M, E.dismountString(null), false));} },
		SUBJECT() {
			public String brief(DefaultRideable E){return E.stateStringSubject(null);}
			public String prompt(DefaultRideable E){return E.stateStringSubject(null);}
			public void mod(DefaultRideable E, MOB M){E.setStateStringSubject(CMLib.genEd().stringPrompt(M, E.stateStringSubject(null), false));} },
		;
		public abstract String brief(DefaultRideable fromThis);
		public abstract String prompt(DefaultRideable fromThis);
		public abstract void mod(DefaultRideable toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultRideable)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultRideable)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultRideable)toThis, M);} }

	public static class DefaultRideable implements Rideable, Ownable
	{
		public static Rideable getFrom(CMObject O)
		{
			if(O instanceof Rideable) return (Rideable)O;
			while(O instanceof Ownable) O=((Ownable)O).getOwner();
			if(O instanceof RideHolder) return ((RideHolder)O).getRideObject();
			return null;
		}

		protected Vector<Item> riders=new Vector<Item>(1);
		protected CMObject parent=null;
		protected int rideNumber=0;
		protected int saveNumber=0;
		protected boolean mobile=false;
		protected String putString="on";
		protected String stateString="sitting on";
		protected String mountString="sit(s) on";
		protected String dismountString="stand(s) from";
		protected String stateStringSubject="sat on by";
		
		public DefaultRideable(CMObject O){parent=O;}
		
		//CMObject
		public String ID(){return "DefaultRideable";}
		public CMObject newInstance(){return null;}
		public CMObject copyOf(){return null;}
		public void initializeClass(){}
		public int compareTo(CMObject O){return 0;}

		//Ownable
		public CMObject getOwner() { return parent; }

		//CMModifiable and CMSavable
		public SaveEnum[] totalEnumS(){return SCode.values();}
		public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
		public ModEnum[] totalEnumM(){return MCode.values();}
		public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

		//Rideable
		public boolean isMobileRide(){return mobile;}
		public void setMobileRide(boolean mob){mobile=mob;}
		public boolean canBeRidden(Item E) { return true; }
		public boolean hasRider(Item E) { return riders.contains(E); }
		public void addRider(Item E)
		{
			E.setRide(this);
			riders.addElement(E);
			E.getEnvObject().recoverEnvStats();
		}
		public void removeRider(Item E)
		{
			riders.removeElement(E);
			E.setRide(null);
			E.getEnvObject().recoverEnvStats();
		}
		public Item removeRider(int i)
		{
			Item E=riders.remove(i);
			E.setRide(null);
			E.getEnvObject().recoverEnvStats();
			return E;
		}
		public Item getRider(int index)
		{
			try { return riders.elementAt(index); }
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			return null;
		}
		public Vector<Item> allRiders(){return (Vector<Item>)riders.clone();}
		public int numRiders() { return riders.size(); }
		public int rideNumber() { return rideNumber;}
		public void setRideNumber(int newNum)
		{
			rideNumber=newNum;
			if(this instanceof Item)
			{
				Item E=(Item)this;
				if(E.container() instanceof ItemCollection)
				{
					ItemCollection sur=(ItemCollection)E.container();
					for(int i=0;i<sur.numItems();i++)
					{
						Item I=sur.getItem(i);
						if(I.ridesNumber()==rideNumber)
							addRider(I);
					}
				}
			}
		}
		public int saveNumber()
		{
			if((numRiders()>0)&&(saveNumber==0))
				saveNumber=Rideable.RideThing.getNumber();
			return saveNumber;
		}
		public String putString(Item R) { return putString; }
		public String stateString(Item R) { return stateString; }
		public String mountString(Item R) { return mountString; }
		public String dismountString(Item R) { return dismountString; }
		public String stateStringSubject(Item R) { return stateStringSubject; }
		public void setPutString(String S) {putString=S; }
		public void setStateString(String S) {stateString=S; }
		public void setMountString(String S) {mountString=S; }
		public void setDismountString(String S) {dismountString=S; }
		public void setStateStringSubject(String S) {stateStringSubject=S; }
	}
}