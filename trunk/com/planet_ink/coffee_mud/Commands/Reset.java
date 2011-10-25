package com.planet_ink.coffee_mud.Commands;
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
public class Reset extends StdCommand
{
	public Reset(){}

	private String[] access={"RESET"};
	public boolean canBeOrdered(){return true;}
	public String[] getAccessWords(){return access;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"RESET");}

	public int resetAreaOramaManaI(MOB mob, Item I, Hashtable rememberI, String lead)
		throws java.io.IOException
	{
		int nochange=0;
		if(I instanceof Weapon)
		{
			Weapon W=(Weapon)I;
			if((W.requiresAmmunition())&&(W.ammunitionCapacity()>0))
			{
				String str=mob.session().prompt(lead+I.Name()+" requires ("+W.ammunitionType()+"): ");
				if(str.length()>0)
				{
					if((str.trim().length()==0)||(str.equalsIgnoreCase("no")))
					{
						W.setAmmunitionType("");
						W.setAmmoCapacity(0);
						W.setUsesRemaining(100);
						str=mob.session().prompt(lead+I.Name()+" new weapon type: ");
						W.setWeaponType(CMath.s_int(str));
					}
					else
						W.setAmmunitionType(str.trim());
					nochange=1;
				}
			}
		}
		Integer IT=(Integer)rememberI.get(I.Name());
		if(IT!=null)
		{
			if(IT.intValue()==I.material())
			{
				mob.tell(lead+I.Name()+" still "+RawMaterial.CODES.NAME(I.material()));
				return nochange;
			}
			I.setMaterial(IT.intValue());
			mob.tell(lead+I.Name()+" Changed to "+RawMaterial.CODES.NAME(I.material()));
			return 1;
		}
		while(true)
		{
			String str=mob.session().prompt(lead+I.Name()+"/"+RawMaterial.CODES.NAME(I.material()),"");
			if(str.equalsIgnoreCase("delete"))
				return -1;
			else
			if(str.length()==0)
			{
				rememberI.put(I.Name(),Integer.valueOf(I.material()));
				return nochange;
			}
			if(str.equals("?"))
				mob.tell(I.Name()+"/"+I.displayText()+"/"+I.description());
			else
			{
				int material=RawMaterial.CODES.FIND_CaseSensitive(str.toUpperCase());
				if(material>=0)
				{
					I.setMaterial(RawMaterial.CODES.GET(material));
					mob.tell(lead+"Changed to "+RawMaterial.CODES.NAME(material));
					rememberI.put(I.Name(),Integer.valueOf(I.material()));
					return 1;
				}
				int possMat=RawMaterial.CODES.FIND_StartsWith(str);
				String poss;
				if(possMat<0)
				{
					poss="?";
					for(String mat : RawMaterial.CODES.NAMES())
						if(mat.indexOf(str.toUpperCase())>=0)
						   poss=mat;
				}
				else
					poss=RawMaterial.CODES.NAME(possMat);
				mob.tell(lead+"'"+str+"' does not exist.  Try '"+poss+"'.");
			}
		}
	}


	public String resetWarning(MOB mob, Area A)
	{
		Room R=null;
		StringBuffer warning=new StringBuffer("");
		String roomWarning=null; 
		for(Enumeration e=A.getProperMap();e.hasMoreElements();)
		{
			R=(Room)e.nextElement();
			roomWarning=resetWarning(mob,R);
			if(roomWarning!=null)
				warning.append(roomWarning);
		}
		if(warning.length()==0) return null;
		return warning.toString();
	}
	public String resetWarning(MOB mob, Room R)
	{
		if((mob==null)||(R==null)) return null;
		StringBuffer warning=new StringBuffer("");
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			if((S!=null)&&(S.mob()!=null)&&(S.mob()!=mob)&&(S.mob().location()==R))
				warning.append("A player, '"+S.mob().Name()+"' is in "+CMLib.map().getExtendedRoomID(R)+"\n\r");
		}
		Item I=null;
		for(int i=0;i<R.numItems();i++)
		{
			I=R.getItem(i);
			if((I instanceof DeadBody)
			&&(((DeadBody)I).playerCorpse()))
				warning.append("A player corpse, '"+I.Name()+"' is in "+CMLib.map().getExtendedRoomID(R)+"\n\r");
		}
		if(warning.length()==0) return null;
		return warning.toString();
	}
	
	private void reportChangesDestroyNewM(MOB oldM, MOB newM, StringBuffer changes)
	{
		if((changes == null)||(oldM==null)) return;
		changes.append(newM.name()+":"+newM.baseEnvStats().level()+", ");
		changes.append("\n\r");
		newM.destroy(); // this was a copy
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			mob.tell("Reset this ROOM, the whole AREA, or REJUV?");
			return false;
		}
		String s=(String)commands.elementAt(0);
		String rest=(commands.size()>1)?CMParms.combine(commands,1):"";
		if(s.equalsIgnoreCase("rejuv"))
		{
			commands.removeElementAt(0);
			if(commands.size()<1)
			{
				mob.tell("Rejuv this ROOM, or the whole AREA?  You can also specify ITEMS or MOBS after ROOM/AREA.");
				return false;
			}
			s=(String)commands.elementAt(0);
			rest=(commands.size()>1)?CMParms.combine(commands,1):"";
			int tickID=0;
			if(rest.startsWith("MOB")) tickID=Tickable.TICKID_MOB;
			if(rest.startsWith("ITEM")) tickID=Tickable.TICKID_ROOM_ITEM_REJUV;
			if(s.equalsIgnoreCase("room"))
			{
				CMLib.threads().rejuv(mob.location(),tickID);
				mob.tell("Done.");
			}
			else
			if(s.equalsIgnoreCase("area"))
			{
				Area A=mob.location().getArea();
				for(Enumeration e=A.getProperMap();e.hasMoreElements();)
					CMLib.threads().rejuv((Room)e.nextElement(),tickID);
				mob.tell("Done.");
			}
			else
			{
				mob.tell("Rejuv this ROOM, or the whole AREA?");
				return false;
			}
		}
		else
		if(s.equalsIgnoreCase("room"))
		{
			String warning=resetWarning(mob, mob.location());
			if((mob.session()==null)||(warning==null)||(mob.session().confirm(warning + "\n\rReset the contents of the room '"+mob.location().roomTitle(mob)+"', OK (Y/n)?","Y")))
			{
				Session S=null;
				for(int x=0;x<CMLib.sessions().size();x++)
				{
					S=CMLib.sessions().elementAt(x);
					if((S!=null)&&(S.mob()!=null)&&(S.mob().location()!=null)&&(S.mob().location()==mob.location()))
						S.mob().tell(mob,null,null,"<S-NAME> order(s) this room to normalcy.");
				}
				CMLib.map().resetRoom(mob.location(), true);
				mob.tell("Done.");
			}
			else
				mob.tell("Cancelled.");
		}
		else
		if(s.equalsIgnoreCase("area"))
		{
			Area A=mob.location().getArea();
			if(A!=null)
			{
				String warning=resetWarning(mob, A);
				if(warning!=null) mob.tell(warning);
				if((mob.session()==null)||(mob.session().confirm("Reset the contents of the area '"+A.name()+"', OK (Y/n)?","Y")))
				{
					Session S=null;
					for(int x=0;x<CMLib.sessions().size();x++)
					{
						S=CMLib.sessions().elementAt(x);
						if((S!=null)&&(S.mob()!=null)&&(S.mob().location()!=null)&&(A.inMyMetroArea(S.mob().location().getArea())))
							S.mob().tell(mob,null,null,"<S-NAME> order(s) this area to normalcy.");
					}
					CMLib.map().resetArea(A);
					mob.tell("Done.");
				}
				else
					mob.tell("Cancelled.");
			}
		}
		else
		if(s.equalsIgnoreCase("arearoomids")&&(CMSecurity.isAllowed(mob, mob.location(), "CMDROOMS")))
		{
			Area A=mob.location().getArea();
			boolean somethingDone=false;
			for(Enumeration e=A.getCompleteMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					if((R.roomID().length()>0)
					&&(R.roomID().indexOf("#")>0)
					&&(!R.roomID().startsWith(A.Name())))
					{
						String oldID=R.roomID();
						R.setRoomID(R.getArea().getNewRoomID(R,-1));
						CMLib.database().DBReCreate(R,oldID);
						try
						{
							for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
							{
								Room R2=(Room)r.nextElement();
								R2=CMLib.map().getRoom(R2);
								if(R2!=R)
								for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
									if(R2.rawDoors()[d]==R)
									{
										CMLib.database().DBUpdateExits(R2);
										break;
									}
							}
						}catch(NoSuchElementException nse){}
						somethingDone=true;
						mob.tell("Room "+oldID+" changed to "+R.roomID()+".");
					}
				}
			}
			if(!somethingDone)
				mob.tell("No rooms were found which needed renaming.");
			else
				mob.tell("Done renumbering rooms.");
		}
		else
		if(!CMSecurity.isAllowed(mob,mob.location(),"RESETUTILS"))
		{
			mob.tell("'"+s+"' is an unknown reset.  Try ROOM, AREA, AREAROOMIDS *.\n\r * = Reset functions which may take a long time to complete.");
			return false;
		}
		else
		if(s.equalsIgnoreCase("genraceagingcharts")&&(CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")))
		{
			for(Enumeration e=CMClass.races();e.hasMoreElements();)
			{
				Race R=(Race)e.nextElement();
				Vector racesToBaseFrom=new Vector();
				Race human=CMClass.getRace("Human");
				Race halfling=CMClass.getRace("Halfling");
				if((R.isGeneric())&&(R.ID().length()>1)&&(!R.ID().endsWith("Race"))&&(Character.isUpperCase(R.ID().charAt(0))))
				{
					int lastStart=0;
					int c=1;
					while(c<=R.ID().length())
					{
						if((c==R.ID().length())||(Character.isUpperCase(R.ID().charAt(c))))
						{
							if((lastStart==0)&&(c==R.ID().length())&&(!R.ID().endsWith("ling"))&&(!R.ID().startsWith("Half")))
								break;
							String partial=R.ID().substring(lastStart,c);
							if(partial.equals("Half")&&(!racesToBaseFrom.contains(human)))
							{
								racesToBaseFrom.add(human);
								lastStart=c;
							}
							else
							{
								Race R2=CMClass.getRace(partial);
								if((R2!=null)&&(R2!=R))
								{
									racesToBaseFrom.add(R2);
									lastStart=c;
								}
								else
								if(partial.endsWith("ling"))
								{
									if(!racesToBaseFrom.contains(halfling))
										racesToBaseFrom.add(halfling);
									lastStart=c;
									R2=CMClass.getRace(partial.substring(0,partial.length()-4));
									if(R2!=null)
										racesToBaseFrom.add(R2);
								}
							}
							if(c==R.ID().length())
								break;
						}
						c++;
					}
					StringBuffer answer=new StringBuffer(R.ID()+": ");
					for(int i=0;i<racesToBaseFrom.size();i++)
						answer.append(((Race)racesToBaseFrom.elementAt(i)).ID()+" ");
					mob.tell(answer.toString());
					if(racesToBaseFrom.size()>0)
					{
						long[] ageChart=new long[Race.AGE_ANCIENT+1];
						for(int i=0;i<racesToBaseFrom.size();i++)
						{
							Race R2=(Race)racesToBaseFrom.elementAt(i);
							int lastVal=0;
							for(int x=0;x<ageChart.length;x++)
							{
								int val=R2.getAgingChart()[x];
								if(val>=Integer.MAX_VALUE)
									val=lastVal+(x*1000);
								ageChart[x]+=val;
								lastVal=val;
							}
						}
						for(int x=0;x<ageChart.length;x++)
							ageChart[x]=ageChart[x]/racesToBaseFrom.size();
						int lastVal=0;
						int thisVal=0;
						for(int x=0;x<ageChart.length;x++)
						{
							lastVal=thisVal;
							thisVal=(int)ageChart[x];
							if(thisVal<lastVal)
								thisVal+=lastVal;
							R.getAgingChart()[x]=thisVal;
						}
						CMLib.database().DBDeleteRace(R.ID());
						CMLib.database().DBCreateRace(R.ID(),R.racialParms());
					}
				}
			}
		}
		else
		if(s.equalsIgnoreCase("mobstats")&&(CMSecurity.isASysOp(mob)))
		{
			s="room";
			if(commands.size()>1) s=(String)commands.elementAt(1);
			if(mob.session()==null) return false;
			mob.session().print("working...");
			StringBuffer recordedChanges=null;
			for(int i=1;i<commands.size();i++)
				if(((String)commands.elementAt(i)).equalsIgnoreCase("NOSAVE"))
				{
					recordedChanges=new StringBuffer("");
					break;
				}
			Vector rooms=new Vector();
			if(s.toUpperCase().startsWith("ROOM"))
				rooms.addElement(mob.location());
			else
			if(s.toUpperCase().startsWith("AREA"))
			{
				try
				{
					for(Enumeration e=mob.location().getArea().getCompleteMap();e.hasMoreElements();)
						rooms.addElement(e.nextElement());
				}catch(NoSuchElementException nse){}
			}
			else
			if(s.toUpperCase().startsWith("WORLD"))
			{
				try
				{
					for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
					{
						Area A=(Area)e.nextElement();
						boolean skip=false;
						for(int i=1;i<commands.size();i++)
							if(((String)commands.elementAt(i)).equalsIgnoreCase(A.Name())||rest.equalsIgnoreCase(A.Name()))
							{
								skip=true;
								break;
							}
						if(skip) continue;
						for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
							rooms.addElement(r.nextElement());
					}
				}catch(NoSuchElementException nse){}
			}
			else
			{
				mob.tell("Try ROOM, AREA, CATALOG, or WORLD.");
				return false;
			}
			if(recordedChanges!=null)
				mob.session().println(".");
			for(Enumeration r=rooms.elements();r.hasMoreElements();)
			{
				Room R=CMLib.map().getRoom((Room)r.nextElement());
				if(R!=null)
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					if(R==null) continue;
					if((recordedChanges!=null)&&(recordedChanges.length()>0))
					{
						mob.session().rawOut(recordedChanges.toString());
						recordedChanges.setLength(0);
					}
					R.getArea().setAreaState(Area.STATE_FROZEN);
					CMLib.map().resetRoom(R, true);
					if(R.getArea().getAreaState()>Area.STATE_ACTIVE)
						R.getArea().setAreaState(Area.STATE_ACTIVE);
				}
				if(recordedChanges==null)
					mob.session().print(".");
			}
			if((recordedChanges!=null)&&(recordedChanges.length()>0))
				mob.session().rawOut(recordedChanges.toString());
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("groundlydoors")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			try
			{
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					boolean changed=false;
					if(R.roomID().length()>0)
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit E=R.getRawExit(d);
						if((E!=null)&&E.hasADoor()&&E.name().equalsIgnoreCase("the ground"))
						{
							E.setName("a door");
							E.setExitParams("door","close","open","a door, closed.");
							changed=true;
						}
					}
					if(changed)
					{
						Log.sysOut("Reset","Groundly doors in "+R.roomID()+" fixed.");
						CMLib.database().DBUpdateExits(R);
					}
					mob.session().print(".");
				}
			}catch(NoSuchElementException nse){}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("areainstall")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null) return false;
			if(commands.size()<2)
			{
				mob.tell("You need to specify a property or behavior to install.");
				return false;
			}
			String ID=(String)commands.elementAt(1);
			Object O=CMClass.getAbility(ID);
			if(O==null) O=CMClass.getBehavior(ID);
			if(O==null)
			{
				mob.tell("'"+ID+"' is not a known property or behavior.  Try LIST.");
				return false;
			}
			
			mob.session().print("working...");
			for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				boolean changed=false;
				if((O instanceof Behavior))
				{
					Behavior B=A.fetchBehavior(((Behavior)O).ID());
					if(B==null)
					{
						B=(Behavior)((Behavior)O).copyOf();
						B.setParms(CMParms.combine(commands,2));
						A.addBehavior(B);
						changed=true;
					}
					else
					if(!B.getParms().equals(CMParms.combine(commands,2)))
					{
						B.setParms(CMParms.combine(commands,2));
						changed=true;
					}
				}
				else
				if(O instanceof Ability)
				{
					Ability B=A.fetchEffect(((Ability)O).ID());
					if(B==null)
					{
						B=(Ability)((Ability)O).copyOf();
						B.setMiscText(CMParms.combine(commands,2));
						A.addNonUninvokableEffect(B);
						changed=true;
					}
					else
					if(!B.text().equals(CMParms.combine(commands,2)))
					{
						B.setMiscText(CMParms.combine(commands,2));
						changed=true;
					}
				}
				if(changed)
				{
					CMLib.database().DBUpdateArea(A.Name(),A);
					mob.session().print(".");
				}
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("worldmatconfirm")&&(CMSecurity.isASysOp(mob)))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				A.setAreaState(Area.STATE_FROZEN);
				for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.roomID().length()>0)
					{
						synchronized(("SYNC"+R.roomID()).intern())
						{
							R=CMLib.map().getRoom(R);
							CMLib.map().resetRoom(R, true);
							mob.session().print(".");
						}
					}
				}
				if(A.getAreaState()>Area.STATE_ACTIVE) A.setAreaState(Area.STATE_ACTIVE);
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("itemstats")&&(CMSecurity.isASysOp(mob)))
		{
			s="room";
			if(commands.size()>1) s=(String)commands.elementAt(1);
			
			if(mob.session()==null) return false;
			mob.session().print("working...");
			StringBuffer recordedChanges=null;
			for(int i=1;i<commands.size();i++)
				if(((String)commands.elementAt(i)).equalsIgnoreCase("NOSAVE"))
				{
					recordedChanges=new StringBuffer("");
					break;
				}
			
			Vector rooms=new Vector();
			if(s.toUpperCase().startsWith("ROOM"))
				rooms.addElement(mob.location());
			else
			if(s.toUpperCase().startsWith("AREA"))
			{
				try
				{
					for(Enumeration e=mob.location().getArea().getCompleteMap();e.hasMoreElements();)
						rooms.addElement(e.nextElement());
				}catch(NoSuchElementException nse){}
			}
			else
			if(s.toUpperCase().startsWith("WORLD"))
			{
				try
				{
					for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
					{
						Area A=(Area)e.nextElement();
						boolean skip=false;
						for(int i=1;i<commands.size();i++)
							if(((String)commands.elementAt(i)).equalsIgnoreCase(A.Name())||rest.equalsIgnoreCase(A.Name()))
							{
								skip=true;
								commands.removeElementAt(i);
								break;
							}
						if(skip) continue;
						for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
							rooms.addElement(r.nextElement());
					}
				}catch(NoSuchElementException nse){}
			}
			else
			{
				mob.tell("Try ROOM, AREA, CATALOG, or WORLD.");
				return false;
			}
			if(recordedChanges!=null)
				mob.session().println(".");
			for(Enumeration r=rooms.elements();r.hasMoreElements();)
			{
				Room R=CMLib.map().getRoom((Room)r.nextElement());
				if((R==null)||(R.getArea()==null)||(R.roomID().length()==0)) continue;
				Area A=R.getArea();
				A.setAreaState(Area.STATE_FROZEN);
				if((recordedChanges!=null)&&(recordedChanges.length()>0))
				{
					mob.session().rawOut(recordedChanges.toString());
					recordedChanges.setLength(0);
				}
				if(A.getAreaState()>Area.STATE_ACTIVE) A.setAreaState(Area.STATE_ACTIVE);
			}
			if((recordedChanges!=null)&&(recordedChanges.length()>0))
				mob.session().rawOut(recordedChanges.toString());
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("arearacemat")&&(CMSecurity.isASysOp(mob)))
		{
			// this is just utility code and will change frequently
			Area A=mob.location().getArea();
			CMLib.map().resetArea(A);
			A.setAreaState(Area.STATE_FROZEN);
			Hashtable rememberI=new Hashtable();
			Hashtable rememberM=new Hashtable();
			try{
			for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					CMLib.map().resetRoom(R, true);
					boolean somethingDone=false;
					mob.tell(R.roomID()+"/"+R.name()+"/"+R.displayText()+"--------------------");
					for(int i=R.numItems()-1;i>=0;i--)
					{
						Item I=R.getItem(i);
						if(I.ID().equalsIgnoreCase("GenWallpaper")) continue;
						int returned=resetAreaOramaManaI(mob,I,rememberI," ");
						if(returned<0)
						{
							R.removeItem(I);
							somethingDone=true;
							mob.tell(" deleted");
						}
						else
						if(returned>0)
							somethingDone=true;
					}
					if(somethingDone)
						CMLib.database().DBUpdateItems(R);
					somethingDone=false;
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if(M==mob) continue;
						if(!M.savable()) continue;
						Race R2=(Race)rememberM.get(M.Name());
						if(R2!=null)
						{
							if(M.charStats().getMyRace()==R2)
								mob.tell(" "+M.Name()+" still "+R2.name());
							else
							{
								M.baseCharStats().setMyRace(R2);
								R2.setHeightWeight(M.baseEnvStats(),(char)M.baseCharStats().gender());
								M.recoverCharStats();
								M.recoverEnvStats();
								mob.tell(" "+M.Name()+" Changed to "+R2.ID());
								somethingDone=true;
							}
						}
						else
						while(true)
						{
							String str=mob.session().prompt(" "+M.Name()+"/"+M.charStats().getMyRace().ID(),"");
							if(str.length()==0)
							{
								rememberM.put(M.name(),M.baseCharStats().getMyRace());
								break;
							}
							if(str.equals("?"))
								mob.tell(M.Name()+"/"+M.displayText()+"/"+M.description());
							else
							{
								R2=CMClass.getRace(str);
								if(R2==null)
								{
									String poss="";
									if(poss.length()==0)
									for(Enumeration e=CMClass.races();e.hasMoreElements();)
									{
										Race R3=(Race)e.nextElement();
										if(R3.ID().toUpperCase().startsWith(str.toUpperCase()))
										   poss=R3.name();
									}
									if(poss.length()==0)
									for(Enumeration e=CMClass.races();e.hasMoreElements();)
									{
										Race R3=(Race)e.nextElement();
										if(R3.ID().toUpperCase().indexOf(str.toUpperCase())>=0)
										   poss=R3.name();
									}
									if(poss.length()==0)
									for(Enumeration e=CMClass.races();e.hasMoreElements();)
									{
										Race R3=(Race)e.nextElement();
										if(R3.name().toUpperCase().startsWith(str.toUpperCase()))
										   poss=R3.name();
									}
									if(poss.length()==0)
									for(Enumeration e=CMClass.races();e.hasMoreElements();)
									{
										Race R3=(Race)e.nextElement();
										if(R3.name().toUpperCase().indexOf(str.toUpperCase())>=0)
										   poss=R3.name();
									}
									mob.tell(" '"+str+"' is not a valid race.  Try '"+poss+"'.");
									continue;
								}
								mob.tell(" Changed to "+R2.ID());
								M.baseCharStats().setMyRace(R2);
								R2.setHeightWeight(M.baseEnvStats(),(char)M.baseCharStats().gender());
								M.recoverCharStats();
								M.recoverEnvStats();
								rememberM.put(M.name(),M.baseCharStats().getMyRace());
								somethingDone=true;
								break;
							}
						}
						for(int i=M.numItems()-1;i>=0;i--)
						{
							Item I=M.getItem(i);
							int returned=resetAreaOramaManaI(mob,I,rememberI,"   ");
							if(returned<0)
							{
								M.removeItem(I);
								somethingDone=true;
								mob.tell("   deleted");
							}
							else
							if(returned>0)
								somethingDone=true;
						}
						ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
						if(SK!=null)
						{
							Vector V=SK.getShop().getStoreInventory();
							for(int i=V.size()-1;i>=0;i--)
							{
								Environmental E=(Environmental)V.elementAt(i);
								if(E instanceof Item)
								{
									Item I=(Item)E;
									int returned=resetAreaOramaManaI(mob,I,rememberI," - ");
									if(returned<0)
									{
										SK.getShop().delAllStoreInventory(I);
										somethingDone=true;
										mob.tell("   deleted");
									}
									else
									if(returned>0)
									{
										somethingDone=true;
										int numInStock=SK.getShop().numberInStock(I);
										int stockPrice=SK.getShop().stockPrice(I);
										SK.getShop().delAllStoreInventory(I);
										SK.getShop().addStoreInventory(I,numInStock,stockPrice);
									}
								}
							}
						}
						for(int i=0;i<M.numBehaviors();i++)
						{
							Behavior B=M.fetchBehavior(i);
							if((B.ID().equalsIgnoreCase("Mobile"))
							&&(B.getParms().trim().length()>0))
							{
								somethingDone=true;
								B.setParms("");
							}
						}
					}
					if(somethingDone)
						CMLib.database().DBUpdateMOBs(R);
				}
			}
			}
			catch(java.io.IOException e){}
			if(A.getAreaState()>Area.STATE_ACTIVE) A.setAreaState(Area.STATE_ACTIVE);
			mob.tell("Done.");
		}
		else
			mob.tell("'"+s+"' is an unknown reset.  Try ROOM, AREA, MOBSTATS ROOM, MOBSTATS AREA *, MOBSTATS WORLD *, MOBSTATS CATALOG *, ITEMSTATS ROOM, ITEMSTATS AREA *, ITEMSTATS WORLD *, ITEMSTATS CATALOG *, AREARACEMAT *, AREAROOMIDS *, AREAINSTALL.\n\r * = Reset functions which may take a long time to complete.");
		return false;
	}

}
