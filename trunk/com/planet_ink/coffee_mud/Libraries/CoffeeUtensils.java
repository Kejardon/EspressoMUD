package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CoffeeUtensils extends StdLibrary implements CMMiscUtils
{
	public String ID(){return "CoffeeUtensils";}
	
	public String niceCommaList(Vector V, boolean endAnd)
	{
		StringBuilder id=new StringBuilder();
		Object[] array=V.toArray();
		for(int i=0;i<array.length;i++)
		{
			Object O=array[i];
			String s=null;
			if(O instanceof Interactable)
				s=((Interactable)O).name();
			else if(O instanceof String)
				s=(String)O;
			else
				continue;
			if(array.length==1)
				return s;
			else if(i==(array.length-1))
				id.append((endAnd)?"and ":"or ").append(s);
			else
				id.append(s).append(", ");
		}
		return id.toString();
	}
	
	public String getFormattedDate(Interactable E)
	{
		String date=CMStrings.padRight("Unknown",11);
		if(E!=null)
		{
			TimeClock C=(E instanceof Area)?((Area)E).getTimeObj():((CMLib.map().roomLocation(E)!=null)?CMLib.map().roomLocation(E).getArea().getTimeObj():null);
			if(C!=null)
				date=CMStrings.padRight(C.getDayOfMonth()+"-"+C.getMonth()+"-"+C.getYear(),11);
		}
		return date;
	}
	//NOTE: This will need to be heavily redone later.
	public boolean reachableItem(MOB mob, Interactable E)
	{
		if((E==null)||(!(E instanceof Item)))
			return true;
		Item I=(Item)E;
		CMObject O=I.container();
		if((mob.isMine(I))
		||(O==null)
		||((O instanceof Room)&&(!((Room)O).isContent(I, false))))
		   return true;
		return false;
	}
	//TODO when more mechanics are in place
	public String builtPrompt(MOB mob)
	{
		StringBuilder buf=new StringBuilder("\r\n");
		String prompt=mob.playerStats().getPrompt();
		//String promptUp=null;
		int c=0;
		while(c<prompt.length())
			/*if((prompt.charAt(c)=='%')&&(c<(prompt.length()-1)))
			{
				switch(prompt.charAt(++c))
				{
				case '-':
					break;
				case 'B': { buf.append("\r\n"); c++; break;}
				case 'c': { buf.append(mob.getItemCollection().numItems()); c++; break;}
				case 'C': { c++; break;}
				case 'd': { c++; break; }
				case 'e': { c++; break; }
				case 'E': { c++; break; }
//				case 'g': { buf.append((int)Math.round(Math.floor(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)/CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(mob))))); c++; break;}
//				case 'G': { buf.append(CMLib.beanCounter().nameCurrencyShort(mob,CMLib.beanCounter().getTotalAbsoluteNativeValue(mob))); c++; break;}
				case 'h': { buf.append("^<Hp^>"+mob.charStats().getPoints(CharStats.Points.HIT)+"^</Hp^>"); c++; break;}
				case 'H': { buf.append("^<MaxHp^>"+mob.charStats().getMaxPoints(CharStats.Points.HIT)+"^</MaxHp^>"); c++; break;}
				case 'K':
				case 'k': { MOB tank=mob;
							if((tank.getVictim() instanceof MOB)
							&&(((MOB)tank.getVictim()).getVictim() instanceof MOB)
							&&(((MOB)tank.getVictim()).getVictim()!=mob))
								tank=(MOB)((MOB)tank.getVictim()).getVictim();
							if(((c+1)<prompt.length())&&(tank!=null))
								switch(prompt.charAt(c+1))
								{
									case 'h': { buf.append(tank.charStats().getPoints(CharStats.Points.HIT)); c++; break;}
									case 'H': { buf.append(tank.charStats().getMaxPoints(CharStats.Points.HIT)); c++; break;}
									case 'm': { buf.append(tank.charStats().getPoints(CharStats.Points.MANA)); c++; break;}
									case 'M': { buf.append(tank.charStats().getMaxPoints(CharStats.Points.MANA)); c++; break;}
									case 'e': { buf.append(tank.displayName(mob)); c++; break;}
									case 'E': { c++; break; }
								}
							c++;
							break;
						  }
				case 'm': { buf.append("^<Mana^>"+mob.charStats().getPoints(CharStats.Points.MANA)+"^</Mana^>"); c++; break;}
				case 'M': { buf.append("^<MaxMana^>"+mob.charStats().getMaxPoints(CharStats.Points.MANA)+"^</MaxMana^>"); c++; break;}
				case 'r': {   if(mob.location()!=null)
							  buf.append(mob.location().displayText());
							  c++; break; }
				case 'R': {   if((mob.location()!=null)&&CMSecurity.isAllowed(mob,"SYSMSGS"))
							  buf.append(mob.location().saveNum());
							  c++; break; }
				case 'w': { buf.append(mob.body().getEnvObject().envStats().weight()); c++; break;}
				case 'z': {   if(mob.location()!=null)
								  buf.append(mob.location().getArea().name());
							  c++; break; }
				case 't': {   if(mob.location()!=null)
								  buf.append(CMStrings.capitalizeAndLower(TimeClock.TOD_DESC[mob.location().getArea().getTimeObj().getTODCode()].toLowerCase()));
							  c++; break;
						  }
				case 'T': {   if(mob.location()!=null)
								  buf.append(mob.location().getArea().getTimeObj().getTimeOfDay());
							  c++; break;
						  }
				default:{ buf.append("%"+prompt.charAt(c)); c++; break;}
				}
			}
			else */
				buf.append(prompt.charAt(c++));
		return buf.toString();
	}

/*
	public void outfit(MOB mob, Vector items)
	{
		if((mob==null)||(items==null)||(items.size()==0))
			return;
		for(int i=0;i<items.size();i++)
		{
			Item I=(Item)items.elementAt(i);
			if(mob.fetchInventory("$"+I.name()+"$")==null)
			{
				I=(Item)I.copyOf();
				I.text();
				I.recoverEnvStats();
				mob.addItem(I);
				if(I.whereCantWear(mob)<=0)
					I.wearIfPossible(mob);
				if(((I instanceof Wearable)||(I instanceof Weapon))
				&&(I.amWearingAt(Wearable.IN_INVENTORY)))
					I.destroy();
			}
		}
	}
	public double memoryUse ( Interactable E, int number )
	{
		double s=-1.0;
		try
		{
			int n = number;
			Object[] objs = new Object[n] ;
			Interactable cl = E;
			Runtime rt = Runtime.getRuntime() ;
			long m0 =rt.totalMemory() - rt.freeMemory() ;
			System.gc() ;
			Thread.sleep( 500 ) ;
			for (int i = 0 ; i < n ; ++i) objs[i] =
					E=(Interactable)cl.copyOf();
			System.gc() ;
			Thread.sleep( 1000 ) ;
			long m1 =rt.totalMemory() - rt.freeMemory() ;
			long dm = m1 - m0 ;
			s = (double)dm / (double)n ;
			if(s<0.0) return memoryUse(E,number);
		}
		catch(Exception e){return -1;}
		return s;
	}
	public void recursiveDropMOB(MOB mob,
								 Room room,
								 Item thisContainer)
	{
		// caller is responsible for recovering any env
		// stat changes!
//		mob.removeItem(thisContainer);
//		thisContainer.unWear();
		room.getItemCollection().addItem(thisContainer);
//		thisContainer.recoverEnvStats();
	}

	public MOB getMobPossessingAnother(MOB mob)
	{
		if(mob==null) return null;
		Session S=null;
		MOB M=null;
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			S=CMLib.sessions().elementAt(s);
			if(S!=null)
			{
				M=S.mob();
				if((M!=null)&&(M.soulMate()==mob))
					return M;
			}
		}
		return null;
	}
	public boolean armorCheck(MOB mob, Item I, int allowedArmorLevel)
	{
		return true;
	}
	public boolean armorCheck(MOB mob, int allowedArmorLevel)
	{
		return true;
	}
	public Vector getDeadBodies(Environmental E)
	{
		if(E instanceof DeadBody)
			return CMParms.makeVector(E);
		if(E instanceof Container)
		{
			Vector Bs=new Vector();
			Vector V=((Container)E).getContents();
			for(int v=0;v<V.size();v++)
				Bs.addAll(getDeadBodies((Environmental)V.elementAt(v)));
			return Bs;
		}
		return new Vector();
	}
	public DVector parseLootPolicyFor(MOB mob)
	{
		if(mob==null) return new DVector(3);
		Vector lootPolicy=(!mob.isMonster())?new Vector():CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_ITEMLOOTPOLICY),true);
		DVector policies=new DVector(3);
		for(int p=0;p<lootPolicy.size();p++)
		{
			String s=((String)lootPolicy.elementAt(p)).toUpperCase().trim();
			if(s.length()==0) continue;
			Vector compiledMask=null;
			int maskDex=s.indexOf("MASK=");
			if(maskDex>=0)
			{
				s=s.substring(0,maskDex).trim();
				compiledMask=CMLib.masking().maskCompile(((String)lootPolicy.elementAt(p)).substring(maskDex+5).trim());
			}
			else
				compiledMask=new Vector();
			Vector parsed=CMParms.parse(s);
			int pct=100;
			for(int x=0;x<parsed.size();x++)
				if(CMath.isInteger((String)parsed.elementAt(x)))
					pct=CMath.s_int((String)parsed.elementAt(x));
				else
				if(CMath.isPct((String)parsed.elementAt(x)))
					pct=(int)Math.round(CMath.s_pct((String)parsed.elementAt(x))*100.0);
			int flags=0;
			if(parsed.contains("RUIN")) flags|=CMMiscUtils.LOOTFLAG_RUIN;
			else
			if(parsed.contains("LOSS")) flags|=CMMiscUtils.LOOTFLAG_LOSS;
			if(flags==0) flags|=CMMiscUtils.LOOTFLAG_LOSS;
			if(parsed.contains("WORN")) flags|=CMMiscUtils.LOOTFLAG_WORN;
			else
			if(parsed.contains("UNWORN")) flags|=CMMiscUtils.LOOTFLAG_UNWORN;
			policies.addElement(Integer.valueOf(pct),Integer.valueOf(flags),compiledMask);
		}
		return policies;
	}
	public void confirmWearability(MOB mob)
	{
	}
	public Item isRuinedLoot(DVector policies, Item I)
	{
		if(I==null) return null;
		if((CMath.bset(I.envStats().disposition(),EnvStats.IS_UNSAVABLE))
		||(CMath.bset(I.envStats().sensesMask(), EnvStats.SENSE_ITEMNORUIN))
		||(I instanceof Coins))
			return I;
		if(I.name().toLowerCase().indexOf("ruined ")>=0)
			return I;
		for(int d=0;d<policies.size();d++)
		{
			if((((Vector)policies.elementAt(d,3)).size()>0)
			&&(!CMLib.masking().maskCheck((Vector)policies.elementAt(d,3),I,true)))
				continue;
			if(CMLib.dice().rollPercentage()>((Integer)policies.elementAt(d,1)).intValue())
				continue;
			int flags=((Integer)policies.elementAt(d,2)).intValue();
			if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_WORN)&&I.amWearingAt(Wearable.IN_INVENTORY))
				continue;
			else
			if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_UNWORN)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
				continue;
			if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_LOSS))
				return null;
			Item I2=CMClass.getItem("GenItem");
			I2.baseEnvStats().setWeight(I.baseEnvStats().weight());
			I2.setName(I.name());
			I2.setDisplayText(I.displayText());
			I2.setDescription(I2.description());
			I2.recoverEnvStats();
			I2.setMaterial(I.material());
			String ruinDescAdder=null;
			switch(I2.material()&RawMaterial.MATERIAL_MASK)
			{
				case RawMaterial.MATERIAL_LEATHER:
				case RawMaterial.MATERIAL_CLOTH:
				case RawMaterial.MATERIAL_VEGETATION:
				case RawMaterial.MATERIAL_FLESH:
				case RawMaterial.MATERIAL_PAPER:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is torn and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_MITHRIL:
				case RawMaterial.MATERIAL_WOODEN:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is battered and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_GLASS:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is shattered and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_ROCK:
				case RawMaterial.MATERIAL_PRECIOUS:
				case RawMaterial.MATERIAL_PLASTIC:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is cracked and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_UNKNOWN:
				case RawMaterial.MATERIAL_ENERGY:
				case RawMaterial.MATERIAL_LIQUID:
				default:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is ruined beyond repair."; 
					break;
			}
			I2.setDescription(CMStrings.endWithAPeriod(I2.description())+" "+ruinDescAdder);
			String oldName=I2.name();
			I2.setName(CMLib.english().insertUnColoredAdjective(I2.name(),"ruined"));
			int x=I2.displayText().toUpperCase().indexOf(oldName.toUpperCase());
			I2.setBaseValue(0);
			if(x>=0)
				I2.setDisplayText(I2.displayText().substring(0,x)+I2.name()+I2.displayText().substring(x+oldName.length()));
			return I2;
		}
		return I;
	}
	public void swapRaces(Race newR, Race oldR)
	{
		for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if(M==null) continue;
				if(M.baseCharStats().getMyRace()==oldR)
					M.baseCharStats().setMyRace(newR);
				if(M.charStats().getMyRace()==oldR)
					M.charStats().setMyRace(newR);
			}
			for(e=CMLib.players().players();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
				if(M.baseCharStats().getMyRace()==oldR)
					M.baseCharStats().setMyRace(newR);
				if(M.charStats().getMyRace()==oldR)
					M.charStats().setMyRace(newR);
			}
		}
	}
	public boolean resurrect(MOB tellMob, Room corpseRoom, DeadBody body, int XPLevel)
	{
		MOB rejuvedMOB=CMLib.players().getPlayer(((DeadBody)body).mobName());
		if(rejuvedMOB!=null)
		{
			rejuvedMOB.tell("You are being resurrected.");
			if(rejuvedMOB.location()!=corpseRoom)
			{
				rejuvedMOB.location().showOthers(rejuvedMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> disappears!");
				corpseRoom.bringMobHere(rejuvedMOB);
			}
			int it=0;
			while(it<rejuvedMOB.location().numItems())
			{
				Item item=rejuvedMOB.location().getItem(it);
				if((item!=null)&&(item.container()==body))
				{
					CMMsg msg2=CMClass.getMsg(rejuvedMOB,body,item,CMMsg.MSG_GET,null);
					rejuvedMOB.location().send(rejuvedMOB,msg2);
					CMMsg msg3=CMClass.getMsg(rejuvedMOB,item,null,CMMsg.MSG_GET,null);
					rejuvedMOB.location().send(rejuvedMOB,msg3);
					it=0;
				}
				else
					it++;
			}
			body.delEffect(body.fetchEffect("Age")); // so misskids doesn't record it
			body.destroy();
			rejuvedMOB.baseEnvStats().setDisposition(CMath.unsetb(rejuvedMOB.baseEnvStats().disposition(),EnvStats.IS_SITTING));
			rejuvedMOB.envStats().setDisposition(CMath.unsetb(rejuvedMOB.baseEnvStats().disposition(),EnvStats.IS_SITTING));
			rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
			corpseRoom.recoverRoomStats();
			return true;
		}
		else
			corpseRoom.show(tellMob,body,CMMsg.MSG_OK_VISUAL,"<T-NAME> twitch(es) for a moment, but the spirit is too far gone.");
		return false;
	}
*/
}