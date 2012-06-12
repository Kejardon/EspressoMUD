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
public interface CMFlagLibrary extends CMLibrary
{
	public boolean isBusy(Interactable E);
	public boolean isAnimalIntelligence(MOB E);
//	public boolean isVegetable(MOB E);
	public boolean stillAffectedBy(Affectable obj, Vector<Effect> oneOf, boolean anyTallF);
	public boolean isInTheGame(Interactable E, boolean reqInhabitation);
	public Vector<Effect> flaggedAnyAffects(Affectable A, EnumSet<Effect.Flags> flags);
	public Vector<Effect> flaggedAffects(Affectable A, EnumSet<Effect.Flags> flag);
/*
	public final static int flag_arrives=0;
	public final static int flag_leaves=1;
	public final static int flag_is=2;
	
	public boolean canSee(MOB E);
	public boolean canBeLocated(Environmental E);
	public boolean isSavable(Environmental E);
	public boolean canSeeHidden(MOB E);
	public boolean canSeeInvisible(MOB E);
	public boolean canSeeEvil(MOB E);
	public boolean canSeeGood(MOB E);
	public boolean canSeeSneakers(MOB E);
	public boolean canSeeBonusItems(MOB E);
	public boolean canSeeInDark(MOB E);
	public boolean canSeeVictims(MOB E);
	public boolean canSeeInfrared(MOB E);
	public boolean canHear(MOB E);
	public boolean canMove(MOB E);
	public boolean allowsMovement(Room R);
	public boolean allowsMovement(Area A);
	public boolean canSmell(MOB E);
	public boolean canTaste(MOB E);
	public boolean canSpeak(MOB E);
	public boolean canBreathe(MOB E);
	public boolean canSeeMetal(MOB E);
	public boolean isReadable(Item I);
	public boolean isGettable(Item I);
	public boolean isDroppable(Item I);
	public boolean isCataloged(Environmental E);
	public boolean hasSeenContents(Environmental E);
	public boolean isSeen(Environmental E);
	public boolean isCloaked(Environmental E);
	public boolean isHidden(Environmental E);
	public boolean isInvisible(Environmental E);
	public boolean isSneaking(Environmental E);
	public boolean isABonusItems(Environmental E);
	public boolean isInDark(Environmental E);
	public boolean isLightSource(Environmental E);
	public boolean isGlowing(Environmental E);
	public boolean isGolem(Environmental E);
	public boolean isSleeping(Environmental E);
	public boolean isSitting(Environmental E);
	public boolean isLayingDown(Environmental E);
	public boolean isFlying(Environmental E);
	public boolean isClimbing(Environmental E);
	public boolean isSwimming(Environmental E);
	public boolean isSwimmingInWater(Environmental E);
	public boolean isFalling(Environmental E);
	public boolean canBeHeardBy(Environmental heard , MOB hearer);
	public boolean canSenseMoving(Environmental sensed, MOB sensor);
	public boolean aliveAwakeMobileUnbound(MOB mob, boolean quiet);
	public boolean aliveAwakeMobile(MOB mob, boolean quiet);
	public boolean isStanding(MOB mob);
	public boolean isBound(Environmental E);
	public boolean isBoundOrHeld(Environmental E);
	public boolean isOnFire(Environmental seen);
	public boolean canBeSeenBy(Environmental seen , MOB seer);
	public boolean canBarelyBeSeenBy(Environmental seen , MOB seer);
	public StringBuffer colorCodes(Environmental seen , MOB seer);
	public boolean seenTheSameWay(MOB seer, Environmental seen1, Environmental seen2);
	public String dispositionString(Environmental seen, int flag_msgType);
	public boolean isWaterWorthy(Environmental E);
	public boolean isInFlight(Environmental E);
	public boolean isMobile(Environmental E);
	public boolean isChild(Environmental E);
	public boolean isBaby(Environmental E);
	public Vector flaggedBehaviors(Environmental E, long flag);
	public Vector domainAnyAffects(Environmental E, int domain);
	public Vector domainAffects(Environmental E, int domain);
	public boolean canAccess(MOB mob, Area A);
	public boolean canAccess(MOB mob, Room R);
	public boolean isMetal(Environmental E);
	public int burnStatus(Environmental E);
	public boolean enchanted(Item I);
	public String dispositionList(int disposition, boolean useVerbs);
	public String sensesList(int disposition, boolean useVerbs);
	public int getDispositionCode(String name);
	public int getSensesCode(String name);
	public String getEffectType(Effect A);
	public String getEffectDomain(Effect A);
	public String describeSenses(MOB mob);
	public String describeDisposition(MOB mob);
	public int getEffectType(String name);
	public int getEffectDomain(String name);
	public int getDetectScore(MOB seer);
	public int getHideScore(Environmental seen);
	public boolean canActAtAll(Tickable affecting);
	public boolean canFreelyBehaveNormal(Tickable affecting);
	public boolean isAggressiveTo(MOB M, MOB toM);
	public boolean isPossiblyAggressive(MOB M);
	public boolean isATrackingMonster(Environmental E);
	public boolean isRemovable(Item I);
	public void setReadable(Item I, boolean truefalse);
	public void setGettable(Item I, boolean truefalse);
	public void setDroppable(Item I, boolean truefalse);
	public void setRemovable(Item I, boolean truefalse);
*/
}
