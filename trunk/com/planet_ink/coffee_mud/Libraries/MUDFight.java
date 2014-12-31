package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

public class MUDFight extends StdLibrary
{
	public static final int COMBAT_DEFAULT=0;
	public static final int COMBAT_QUEUE=1;
	public static final int COMBAT_MANUAL=2;

	public String ID(){return "MUDFight";}

/*
	public String lastStr="";
	public long lastRes=0;
	public String[][] hitWordIndex=null;
	public String[][] hitWordsChanged=null;
	public void postDeath(MOB killerM, MOB deadM, CMMsg addHere)
	{
		if(deadM==null) return;
		Room deathRoom=deadM.location();
		if(deathRoom==null) return;

		// make sure he's not already dead, or with a pending death.
		if(deadM.amDead()) return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
		for(int i=0;i<addHere.trailerMsgs().size();i++)
		{
			CMMsg msg=(CMMsg)addHere.trailerMsgs().elementAt(i);
			if((msg.source()==deadM)
			&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
			   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
				return;
		}

		String msp=CMProps.msp("death"+CMLib.dice().roll(1,7,0)+".wav",50);
		CMMsg msg=null;
		if(isKnockedOutUponDeath(deadM,killerM))
			msg=CMClass.getMsg(deadM,null,killerM,
					CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!YOU ARE DEFEATED!!!!!!!!!!^</FIGHT^>^?^.\r\n"+msp,
					CMMsg.MSG_OK_VISUAL,null,
					CMMsg.MSG_DEATH,"^F^<FIGHT^><S-NAME> is DEFEATED!!!^</FIGHT^>^?\r\n"+msp);
		else
			msg=CMClass.getMsg(deadM,null,killerM,
				CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^</FIGHT^>^?^.\r\n"+msp,
				CMMsg.MSG_OK_VISUAL,null,
				CMMsg.MSG_DEATH,"^F^<FIGHT^><S-NAME> is DEAD!!!^</FIGHT^>^?\r\n"+msp);
		CMMsg msg2=CMClass.getMsg(deadM,null,killerM,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null);
		CMLib.map().sendGlobalMessage(deadM,CMMsg.TYP_DEATH, msg2);
		if(addHere!=null)
		{
			if(deathRoom.okMessage(deadM,msg2))
			{
				addHere.addTrailerMsg(msg);
				addHere.addTrailerMsg(msg2);
			}
		}
		else
		if(deathRoom.okMessage(deadM,msg))
		{
			deathRoom.send(deadM,msg);
			if(deathRoom.okMessage(deadM,msg2))
				deathRoom.send(deadM,msg2);
		}
	}

	public boolean postAttack(MOB attacker, MOB target, Item weapon)
	{
		if(attacker==null)
			return false;
		CMMsg msg=CMClass.getMsg(attacker,target,weapon,CMMsg.MSG_WEAPONATTACK,null);
		Room R=target.location();
		if(R!=null)
			if(R.okMessage(attacker,msg))
			{
				R.send(attacker,msg);
				return msg.value()>0;
			}
		return false;
	}

	public boolean postHealing(MOB healer,
							   MOB target,
							   Environmental tool,
							   int messageCode,
							   int healing,
							   String allDisplayMessage)
	{
		if(healer==null) healer=target;
		if((healer==null)||(target==null)||(target.location()==null)) return false;
		CMMsg msg=CMClass.getMsg(healer,target,tool,messageCode,CMMsg.MSG_HEALING,messageCode,allDisplayMessage);
		msg.setValue(healing);
		Room R=target.location();
		if(R!=null)
			if(R.okMessage(target,msg))
			{ R.send(target,msg); return true;}
		return false;
	}

	public String replaceDamageTag(String str, int damage, int damageType)
	{
		if(str==null) return null;
		int replace=str.indexOf("<DAMAGE>");
		if(replace>=0)
		{
			if(!CMProps.getVar(CMProps.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
				return str.substring(0,replace)+standardHitWord(damageType,damage)+str.substring(replace+8);
			return str.substring(0,replace)+standardHitWord(damageType,damage)+" ("+damage+")"+ str.substring(replace+8);
		}
		replace=str.indexOf("<DAMAGES>");
		if(replace>=0)
		{
			String hitWord=standardHitWord(damageType,damage);
			hitWord=hitWord.replace("(","");
			hitWord=hitWord.replace(")","");
			if(!CMProps.getVar(CMProps.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
				return str.substring(0,replace)+hitWord+str.substring(replace+9);
			return str.substring(0,replace)+hitWord+" ("+damage+")"+ str.substring(replace+9);
		}
		return str;
	}

	public void postDamage(MOB attacker,
						   MOB target,
						   Environmental weapon,
						   int damage,
						   int messageCode,
						   int damageType,
						   String allDisplayMessage)
	{
		if((attacker==null)||(target==null)||(target.location()==null)) return;
		if(allDisplayMessage!=null) allDisplayMessage="^F^<FIGHT^>"+allDisplayMessage+"^</FIGHT^>^?";

		CMMsg msg=CMClass.getMsg(attacker,target,weapon,messageCode,CMMsg.MSG_DAMAGE,messageCode,allDisplayMessage);
		msg.setValue(damage);
		CMLib.color().fixSourceFightColor(msg);
		Room R=(target==null)?null:target.location();
		if(R!=null)
			if(R.okMessage(target,msg))
			{
				if(damageType>=0)
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType),
							   msg.targetCode(),
							   replaceDamageTag(msg.targetMessage(),msg.value(),damageType),
							   msg.othersCode(),
							   replaceDamageTag(msg.othersMessage(),msg.value(),damageType));
				R.send(target,msg);
			}
	}
	
	public void postWeaponDamage(MOB source, MOB target, Item item, boolean success)
	{
		if(source==null) return;
		Weapon weapon=null;
		int damageInt=0;
		int damageType=Weapon.TYPE_BASHING;
		if(item instanceof Weapon)
		{
			weapon=(Weapon)item;
			damageType=weapon.weaponType();
		}
		if(success)
		{
			// calculate Base Damage (with Strength bonus)
			String oldHitString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.hitString(damageInt):
								standardHitString(Weapon.CLASS_BLUNT,damageInt,item.name()))+"^</FIGHT^>^?";
			CMMsg msg=CMClass.getMsg(source,
									target,
									item,
									CMMsg.MSG_OK_VISUAL,
									CMMsg.MSG_DAMAGE,
									CMMsg.MSG_OK_VISUAL,
									oldHitString);
			CMLib.color().fixSourceFightColor(msg);

			msg.setValue(damageInt);
			// why was there no okaffect here?
			Room room=source.location();
			if((room!=null)&&(room.okMessage(source,msg)))
			{
				if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
				{
					damageInt=msg.value();
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType),
							   msg.targetCode(),
							   replaceDamageTag(msg.targetMessage(),msg.value(),damageType),
							   msg.othersCode(),
							   replaceDamageTag(msg.othersMessage(),msg.value(),damageType));
				}
				if((source.location()==room)
				&&(target.location()==room))
					room.send(source,msg);
			}
		}
		else
		{
			String missString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.missString():
								standardMissString(Weapon.TYPE_BASHING,Weapon.CLASS_BLUNT,item.name(),false))+"^</FIGHT^>^?";
			CMMsg msg=CMClass.getMsg(source,
									target,
									weapon,
									CMMsg.MSG_NOISYMOVEMENT,
									missString);
			CMLib.color().fixSourceFightColor(msg);
			// why was there no okaffect here?
			Room R=source.location();
			if(R!=null)
			if(R.okMessage(source,msg) && (!source.amDead()) && (!source.amDestroyed()))
				R.send(source,msg);
		}
	}

	protected HashSet getCombatBeneficiaries(MOB killer, MOB killed, Room deathRoom, HashSet beneficiaries)
	{
		if(deathRoom!=null)
		{
			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				MOB mob=deathRoom.fetchInhabitant(m);
				if(!beneficiaries.contains(mob))
					beneficiaries.add(mob);
			}
		}
		if((killer!=null)&&(!beneficiaries.contains(killer))&&(killer!=killed)&&(CMLib.flags().isInTheGame(killer,true)))
			beneficiaries.add(killer);
		return beneficiaries;
	}

	public HashSet getCombatBeneficiaries(MOB killer, MOB killed)
	{
		if((killer==null)||(killed==null)) return new HashSet();
		HashSet beneficiaries=new HashSet();
		Room R=killer.location();
		if(R!=null) getCombatBeneficiaries(killer,killed,R,beneficiaries);
		R=killed.location();
		if((R!=null)&&(R!=killer.location())) getCombatBeneficiaries(killer,killed,R,beneficiaries);
		return beneficiaries;
	}


	protected HashSet getCombatDividers(MOB killer, MOB killed, Room deathRoom, HashSet dividers)
	{
		if(deathRoom!=null)
		{
			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				MOB mob=deathRoom.fetchInhabitant(m);
				if(!dividers.contains(mob))
					dividers.add(mob);
			}
		}
		if((killer!=null)&&(!dividers.contains(killer))&&(killer!=killed)&&(CMLib.flags().isInTheGame(killer,true)))
			dividers.add(killer);
		return dividers;
	}

	public HashSet getCombatDividers(MOB killer, MOB killed)
	{
		if((killer==null)||(killed==null)) return new HashSet();
		HashSet dividers=new HashSet();
		Room R=killer.location();
		if(R!=null) getCombatDividers(killer,killed,R,dividers);
		R=killed.location();
		if((R!=null)&&(R!=killer.location())) getCombatDividers(killer,killed,R,dividers);
		return dividers;
	}

	public Item justDie(MOB source, MOB target)
	{
		if(target==null) return null;
		Room deathRoom=target.location();

		HashSet beneficiaries=getCombatBeneficiaries(source,target);
		HashSet dividers=getCombatDividers(source,target);

		String currency=CMLib.beanCounter().getCurrency(target);
		double deadMoney=CMLib.beanCounter().getTotalAbsoluteValue(target,currency);
		double myAmountOfDeadMoney=0.0;

		String[] cmds=null;
		if((target.isMonster())||(target.soulMate()!=null))
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_MOBDEATH),true));
		else
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PLAYERDEATH),true));
		
		DeadBody body=null; //must be done before consequences because consequences could be purging
		if((!CMParms.containsIgnoreCase(cmds,"RECALL"))
		&&(!isKnockedOutUponDeath(target,source)))
			body=target.killMeDead(true);
		
		handleConsequences(target,source,cmds,"^*You lose @x1 experience points.^?^.");

		if(!isKnockedOutUponDeath(target,source))
		{
			Room bodyRoom=deathRoom;
			if((body!=null)&&(body.owner() instanceof Room)&&(((Room)body.owner()).isContent(body)))
				bodyRoom=(Room)body.owner();
			if((source!=null)&&(body!=null))
			{
				body.setKillerName(source.Name());
				body.setKillerPlayer(!source.isMonster());
			}


			if(target.soulMate()!=null)
			{
				Session s=target.session();
				s.setMob(target.soulMate());
				target.soulMate().setSession(s);
				target.setSession(null);
				target.soulMate().tell("^HYour spirit has returned to your body...\r\n\r\n^N");
				CMLib.commands().postLook(target.soulMate(),true);
				target.setSoulMate(null);
			}

			if((body!=null)&&(bodyRoom!=null)&&(body.destroyAfterLooting()))
			{
				for(int i=bodyRoom.numItems()-1;i>=0;i--)
				{
					Item item=bodyRoom.getItem(i);
					if((item!=null)&&(item.container()==body))
						item.setContainer(null);
				}
				body.destroy();
				bodyRoom.recoverEnvStats();
				return null;
			}
			return body;
		}
		return null;
	}

	private int[] damageThresholds(){return CMProps.getI1ListVar(CMProps.SYSTEML_DAMAGE_WORDS_THRESHOLDS);}
	private String[][] hitWords(){ return CMProps.getS2ListVar(CMProps.SYSTEML_DAMAGE_WORDS); }
	private String[] armorDescs(){return CMProps.getSListVar(CMProps.SYSTEML_ARMOR_DESCS);}
	private String[] prowessDescs(){return CMProps.getSListVar(CMProps.SYSTEML_PROWESS_DESCS);}
	private String[] missWeaponDescs(){return CMProps.getSListVar(CMProps.SYSTEML_WEAPON_MISS_DESCS);}
	private String[] missDescs(){return CMProps.getSListVar(CMProps.SYSTEML_MISS_DESCS);}

	public String standardHitWord(int type, int damage)
	{
		if((type<0)||(type>=Weapon.TYPE_DESCS.length))
			type=Weapon.TYPE_BURSTING;
		int[] thresholds=damageThresholds();
		int damnCode=thresholds.length-2;
		for(int i=0;i<thresholds.length;i++)
			if(damage<=thresholds[i]){ damnCode=i; break;}
		damnCode++; // always add 1 because index into hitwords is type=0, annoy=1;
		if(hitWords() != hitWordsChanged)
		{
			hitWordsChanged=hitWords();
			hitWordIndex=null;
		}
		if(hitWordIndex==null)
		{
			String[][] newWordIndex=new String[Weapon.TYPE_DESCS.length][];
			String[][] hitWords=hitWords();
			for(int w=0;w<Weapon.TYPE_DESCS.length;w++)
			{
				String[] ALL=null;
				String[] MINE=null;
				for(int i=0;i<hitWords.length;i++)
				{
					if(hitWords[i][0].equalsIgnoreCase("ALL"))
						ALL=hitWords[i];
					else
					if(hitWords[i][0].equalsIgnoreCase(Weapon.TYPE_DESCS[w]))
					{ MINE=hitWords[i]; break;}
				}
				if(MINE!=null)
					newWordIndex[w]=MINE;
				else
					newWordIndex[w]=ALL;
			}
			hitWordIndex=newWordIndex;
		}
		String[] HIT_WORDS=hitWordIndex[type];
		if(damnCode<1) damnCode=1;
		if(damnCode<HIT_WORDS.length) return HIT_WORDS[damnCode];
		return HIT_WORDS[HIT_WORDS.length-1];
	}

	public String armorStr(MOB mob)
	{
		int armor = 0;
		int ARMOR_CEILING=CMProps.getIListVar(CMProps.SYSTEML_ARMOR_DESCS_CEILING);
		return (armor<0)?armorDescs()[0]:(
			   (armor>=ARMOR_CEILING)?armorDescs()[armorDescs().length-1]+(CMStrings.repeat("!",(armor-ARMOR_CEILING)/100))+" ("+armor+")":(
					   armorDescs()[(int)Math.round(Math.floor(CMath.mul(CMath.div(armor,ARMOR_CEILING),armorDescs().length)))]+" ("+armor+")"));
	}
	
	public String fightingProwessStr(MOB mob)
	{
		int prowess = 0;
		int PROWESS_CEILING=CMProps.getIListVar(CMProps.SYSTEML_PROWESS_DESCS_CEILING);
		return (prowess<0)?prowessDescs()[0]:(
			   (prowess>=PROWESS_CEILING)?prowessDescs()[prowessDescs().length-1]+(CMStrings.repeat("!",(prowess-PROWESS_CEILING)/100))+" ("+prowess+")":(
				prowessDescs()[(int)Math.round(Math.floor(CMath.mul(CMath.div(prowess,PROWESS_CEILING),prowessDescs().length)))]+" ("+prowess+")"));
	}

	public String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString)
	{
		int dex=3;
		switch(weaponClassification)
		{
		case Weapon.CLASS_RANGED: dex=0; break;
		case Weapon.CLASS_THROWN: dex=1; break;
		default:
			switch(weaponType)
			{
			case Weapon.TYPE_SLASHING:
			case Weapon.TYPE_BASHING:
				dex=2; break;
			case Weapon.TYPE_PIERCING:
				dex=4; break;
			case Weapon.TYPE_SHOOT:
				dex=0; break;
			default:
				dex=3;
				break;
			}
			break;
		}
		if(!useExtendedMissString) return missDescs()[dex];
		return missWeaponDescs()[dex].replace("<TOOLNAME>",weaponName)+CMProps.msp("missed.wav",20);
	}


	public String standardHitString(int weaponClass, int damageAmount,  String weaponName)
	{
		if((weaponName==null)||(weaponName.length()==0))
			weaponClass=Weapon.CLASS_NATURAL;
		switch(weaponClass)
		{
		case Weapon.CLASS_RANGED:
			return "<S-NAME> fire(s) "+weaponName+" at <T-NAMESELF> and <DAMAGE> <T-HIM-HER>."+CMProps.msp("arrow.wav",20);
		case Weapon.CLASS_THROWN:
			return "<S-NAME> throw(s) "+weaponName+" at <T-NAMESELF> and <DAMAGE> <T-HIM-HER>."+CMProps.msp("arrow.wav",20);
		default:
			return "<S-NAME> <DAMAGE> <T-NAMESELF> with "+weaponName+"."+CMProps.msp("punch"+CMLib.dice().roll(1,7,0)+".wav",20);
		}
	}

	public String[] healthDescs(){return CMProps.getSListVar(CMProps.SYSTEML_HEALTH_CHART);}
	public String standardMobCondition(MOB viewer,MOB mob)
	{
		int pct=(int)Math.round(Math.floor((CMath.div(mob.charStats().getPoints(CharStats.STAT_HITPOINTS),mob.charStats().getMaxPoints(CharStats.STAT_HITPOINTS)))*10));
		if(pct<0) pct=0;
		if(pct>=healthDescs().length) pct=healthDescs().length-1;
		return healthDescs()[pct].replace("<MOB>",mob.displayName(viewer));
	}

	public void resistanceMsgs(CMMsg msg, MOB source, MOB target)
	{
		if(msg.value()>0) return;

		if(target.amDead()) return;

		String tool=null;
		String endPart=" from <T-NAME>.";
		if(source==target)
		{
			source=null;
			endPart=".";
		}
		if(msg.tool()!=null)
		{
			if(msg.tool() instanceof Ability)
				tool=((Ability)msg.tool()).name();
		}

		String tackOn=null;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_MIND: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"mental attack":tool)+endPart; break;
		case CMMsg.TYP_GAS: tackOn="<S-NAME> resist(s) the "+((tool==null)?"noxious fumes":tool)+endPart; break;
		case CMMsg.TYP_COLD: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"cold blast":tool)+endPart;  break;
		case CMMsg.TYP_ELECTRIC: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"electrical attack":tool)+endPart; break;
		case CMMsg.TYP_FIRE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"blast of heat":tool)+endPart; break;
		case CMMsg.TYP_WATER: tackOn="<S-NAME> dodge(s) the "+((tool==null)?"wet blast":tool)+endPart;  break;
		case CMMsg.TYP_UNDEAD:  tackOn="<S-NAME> shake(s) off the "+((tool==null)?"evil attack":tool)+endPart; break;
		case CMMsg.TYP_POISON:  tackOn="<S-NAME> shake(s) off the "+((tool==null)?"poison":tool)+endPart; break;
		case CMMsg.TYP_DISEASE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"disease":tool); break;
		case CMMsg.TYP_JUSTICE: tackOn="<S-NAME> avoid(s) the "+((tool==null)?"attempt":tool); break;
		case CMMsg.TYP_CAST_SPELL:  tackOn="<S-NAME> resist(s) the "+((tool==null)?"magical attack":tool)+endPart; break;
		case CMMsg.TYP_PARALYZE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"paralysis":tool)+endPart; break;
		}
		if(tackOn!=null)
		{
			String newStr=target+"/"+source+"/"+tool;
			if(!newStr.equals(lastStr)||((System.currentTimeMillis()-lastRes)>250))
				msg.addTrailerMsg(CMClass.getMsg(target,source,CMMsg.MSG_OK_ACTION,tackOn));
			lastStr=newStr;
			lastRes=System.currentTimeMillis();
		}
		msg.setValue(msg.value()+1);
	}

	public void handleBeingHealed(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB target=(MOB)msg.target();
		int amt=msg.value();
		if(amt>0) target.charStats().adjPoints(CharStats.STAT_HITPOINTS, amt);
	}

	public void postPanic(MOB mob, CMMsg addHere)
	{
		if(mob==null) return;

		// make sure he's not already dead, or with a pending death.
		if(mob.amDead()) return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
		for(int i=0;i<addHere.trailerMsgs().size();i++)
		{
			CMMsg msg=(CMMsg)addHere.trailerMsgs().elementAt(i);
			if((msg.source()==mob)
			&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
			   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
				return;
		}
		CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_PANIC,null);
		if(addHere!=null)
			addHere.addTrailerMsg(msg);
		else
		if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
			mob.location().send(mob,msg);
	}

	public void handleBeingDamaged(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB attacker=msg.source();
		MOB target=(MOB)msg.target();
		int dmg=msg.value();
		synchronized(("DMG"+target.Name().toUpperCase()).intern())
		{
			if((dmg>0)&&(target.charStats().getPoints(CharStats.STAT_HITPOINTS)>0))
			{
				if((!target.charStats().adjPoints(CharStats.STAT_HITPOINTS,-dmg))
				&&(target.charStats().getPoints(CharStats.STAT_HITPOINTS)<1)
				&&(target.location()!=null))
					postDeath(attacker,target,msg);
				else
				{
					if((target.charStats().getPoints(CharStats.STAT_HITPOINTS)<target.getWimpHitPoint())
					&&(target.getWimpHitPoint()>0))
						postPanic(target,msg);
					else
					if((CMProps.getIntVar(CMProps.SYSTEMI_INJPCTHP)>=(int)Math.round(CMath.div(target.charStats().getPoints(CharStats.STAT_HITPOINTS),target.charStats().getMaxPoints(CharStats.STAT_HITPOINTS))*100.0))
					&&(!CMLib.flags().isGolem(target))
					&&(target.fetchEffect("Injury")==null))
					{
						Ability A=CMClass.getAbility("Injury");
						if(A!=null) A.invoke(target,CMParms.makeVector(msg),target,true,0);
					}
				}
			}
		}
	}

	public void handleDeath(CMMsg msg)
	{
		MOB deadmob=msg.source();

		if(!deadmob.amDead())
		{
			if((!deadmob.isMonster())&&(deadmob.soulMate()==null))
			{
				Vector channels=CMLib.channels().getFlaggedChannelNames(CMChannels.ChannelFlag.DETAILEDDEATHS);
				Vector channels2=CMLib.channels().getFlaggedChannelNames(CMChannels.ChannelFlag.DEATHS);
				if(!CMLib.flags().isCloaked(deadmob))
				for(int i=0;i<channels.size();i++)
				if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
					CMLib.commands().postChannel((String)channels.elementAt(i),deadmob.Name()+" was just killed in "+CMLib.map().getExtendedRoomID(deadmob.location())+" by "+msg.tool().Name()+".",true);
				else
					CMLib.commands().postChannel((String)channels.elementAt(i),deadmob.Name()+" has just died at "+CMLib.map().getExtendedRoomID(deadmob.location()),true);
				if(!CMLib.flags().isCloaked(deadmob))
				for(int i=0;i<channels2.size();i++)
					if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
						CMLib.commands().postChannel((String)channels2.elementAt(i),deadmob.Name()+" was just killed.",true);
			}
			if(msg.tool() instanceof MOB)
			{
				MOB killer=(MOB)msg.tool();
				justDie(killer,deadmob);
			}
			else
				justDie(null,deadmob);
			deadmob.tell(deadmob,msg.target(),msg.tool(),msg.sourceMessage());
			if(CMLib.flags().isCataloged(deadmob))
				CMLib.catalog().bumpDeathPickup(deadmob);
		}
	}

	public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg)
	{
		Room R=observer.location();
		MOB deadmob=msg.source();
		if((fighting==deadmob)&&(R!=null))
		{
			MOB newTargetM=null;
			for(int r=0;r<R.numInhabitants();r++)
			{
				MOB M=R.fetchInhabitant(r);
				if((M!=observer)
				&&(M!=deadmob)
				&&(M!=null)
				&&(M.getVictim()==observer)
				&&(!M.amDead())
				&&(CMLib.flags().isInTheGame(M,true)))
				{
					newTargetM=M;
					break;
				}
			}
			observer.setVictim(newTargetM);
		}
	}

	public void handleBeingAssaulted(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB attacker=msg.source();
		MOB target=(MOB)msg.target();

		if((target.location()!=null)
		&&(target.location().isInhabitant(attacker))
		&&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS)))
		{
			target.setVictim(attacker);
		}
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				Item weapon=attacker.myNaturalWeapon();
				if((msg.tool()!=null)&&(msg.tool() instanceof Item))
					weapon=(Item)msg.tool();
				if(weapon!=null)
				{
					boolean isHit=false;
					postWeaponDamage(attacker,target,weapon,isHit);
					if(isHit) msg.setValue(1);
				}
				if((target.soulMate()==null)&&(target.playerStats()!=null)&&(target.location()!=null))
					target.playerStats().adjHygiene(PlayerStats.HYGIENE_FIGHTDIRTY);

				if(attacker.isMonster())
					attacker.setVictim(target);
			}
			else
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Item))
				postWeaponDamage(attacker,target,(Item)msg.tool(),true);
		}
		if(CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)||CMLib.flags().isLayingDown(target))
			CMLib.commands().postStand(target,true);
	}

	protected void subtickAttack(MOB fighter, Item weapon)
	{
		if((weapon!=null)&&(weapon.amWearingAt(Wearable.IN_INVENTORY)))
			weapon=fighter.fetchWieldedItem();
		postAttack(fighter,fighter.getVictim(),weapon);
	}

	protected void subtickBeforeAttack(MOB fighter)
	{
		// combat que system eats up standard commands
		// before using any attacks
		while((CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)==COMBAT_QUEUE)
		&&(!fighter.amDead())
		&&(fighter.dequeCommand()));
	}
	protected void subtickAfterAttack(MOB fighter)
	{
		// this code is for auto-retargeting of players
		// is mostly not handled by combatabilities in a smarter way
		MOB target=fighter.getVictim();
		if((target!=null)
		&&(fighter.isMonster())
		&&(target.isMonster())
		&&(CMLib.dice().rollPercentage()==1)
		&&(!target.amDead())
		&&(fighter.location()!=null))
		{
			MOB M=null;
			Room R=fighter.location();
			MOB nextVictimM=null;
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if((M!=null)
				&&(!M.isMonster())
				&&(M.getVictim()==fighter))
					nextVictimM=M;
			}
			if(nextVictimM!=null)
				fighter.setVictim(nextVictimM);
		}
	}

	public void tickCombat(MOB fighter)
	{
		Item weapon=fighter.fetchWieldedItem();

		subtickBeforeAttack(fighter);
		int combatSystem=CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM);
		int saveAction=(combatSystem!=COMBAT_DEFAULT)?0:1;

		if(CMLib.flags().aliveAwakeMobile(fighter,true))
		{
			if((combatSystem!=COMBAT_MANUAL)
			||(fighter.isMonster()))
			{
				int numAttacks=(int)Math.round(Math.floor(fighter.actions()))-saveAction;
				if((combatSystem==COMBAT_DEFAULT)
				&&(numAttacks>(int)Math.round(Math.floor(fighter.envStats().speed()+0.9))))
					numAttacks=(int)Math.round(Math.floor(fighter.envStats().speed()+0.9));
				for(int s=0;s<numAttacks;s++)
				{
					if((!fighter.amDead())
					&&(fighter.charStats().getPoints(CharStats.STAT_HITPOINTS)>0)
					&&(fighter.actions()>=1.0)
					&&((s==0)||(CMLib.flags().isStanding(fighter))))
					{
						fighter.setActions(fighter.actions()-1.0);
						subtickAttack(fighter,weapon);
					}
					else
						break;
				}
			}
			if(CMLib.dice().rollPercentage()>(fighter.charStats().getStat(CharStats.STAT_CONSTITUTION)*4))
				fighter.charStats().adjPoints(CharStats.STAT_MOVE, -1);
		}

		subtickAfterAttack(fighter);
	}

	public boolean isKnockedOutUponDeath(MOB mob, MOB fighting)
	{
		String whatToDo=null;
		if(((mob.isMonster())||(mob.soulMate()!=null)))
			whatToDo=CMProps.getVar(CMProps.SYSTEM_MOBDEATH).toUpperCase();
		else
			whatToDo=CMProps.getVar(CMProps.SYSTEM_PLAYERDEATH).toUpperCase();
		Vector whatsToDo=CMParms.parseCommas(whatToDo,true);
		double[] fakeVarVals={1.0,1.0,1.0};
		for(int w=0;w<whatsToDo.size();w++)
		{
			whatToDo=(String)whatsToDo.elementAt(w);
			if(whatToDo.startsWith("OUT ")&&(CMath.isMathExpression(whatToDo.substring(4).trim(),fakeVarVals)))
				return true;
		}
		return false;
	}

	public boolean handleConsequences(MOB mob, MOB fighting, String[] commands, String message)
	{
		if((commands==null)||(commands.length==0)) return false;
		int rejuv=mob.envStats().rejuv();
		if((rejuv==0)||(rejuv==Integer.MAX_VALUE)) rejuv=mob.envStats().level();
		if(((!mob.isMonster())&&(mob.soulMate()==null))) rejuv=1;
		double[] varVals={
				mob.baseEnvStats().level()>mob.envStats().level()?mob.baseEnvStats().level():mob.envStats().level(),
				(fighting!=null)?fighting.envStats().level():0,
				rejuv
		};
		for(int w=0;w<commands.length;w++)
		{
			String whatToDo=commands[w].toUpperCase();
			if(whatToDo.startsWith("RECALL"))
				mob.killMeDead(false);
			else
			if(whatToDo.startsWith("PUR"))
			{
				MOB deadMOB=CMLib.players().getLoadPlayer(mob.Name());
				if(deadMOB!=null)
				{
					CMLib.players().obliteratePlayer(deadMOB,false);
					return false;
				}
			}
		}
		return true;
	}
*/
}