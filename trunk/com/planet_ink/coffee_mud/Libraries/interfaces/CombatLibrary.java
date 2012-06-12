package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface CombatLibrary extends CMLibrary
{
	public static final int COMBAT_DEFAULT=0;
	public static final int COMBAT_QUEUE=1;
	public static final int COMBAT_MANUAL=2;
/*
	public void postPanic(MOB mob, CMMsg addHere);
	public void postDeath(MOB killerM, MOB deadM, CMMsg addHere);
	public boolean postAttack(MOB attacker, MOB target, Item weapon);
	public boolean postHealing(MOB healer, MOB target, Environmental tool, int messageCode, int healing, String allDisplayMessage);
	public String replaceDamageTag(String str, int damage, int damageType);
	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage);
	public void postWeaponDamage(MOB source, MOB target, Item item, boolean success);
	public HashSet getCombatDividers(MOB killer, MOB killed);
	public HashSet getCombatBeneficiaries(MOB killer, MOB killed);
	public DeadBody justDie(MOB source, MOB target);
	public String armorStr(MOB mob);
	public String standardHitWord(int type, int damage);
	public String fightingProwessStr(MOB mob);
	public String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString);
	public String standardHitString(int weaponClass, int damageAmount,  String weaponName);
	public String standardMobCondition(MOB viewer, MOB mob);
	public void resistanceMsgs(CMMsg msg, MOB source, MOB target);
	public void handleBeingHealed(CMMsg msg);
	public void handleBeingDamaged(CMMsg msg);
	public void handleBeingAssaulted(CMMsg msg);
	public void handleDeath(CMMsg msg);
	public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg);
	public boolean isKnockedOutUponDeath(MOB mob, MOB fighting);
	public boolean handleConsequences(MOB mob, MOB fighting, String[] commands, String message);
	public void tickCombat(MOB fighter);
*/
}
