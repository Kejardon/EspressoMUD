package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

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
public interface Weapon extends Item
{
	// weapon types
	public final static int TYPE_NATURAL=0;
	public final static int TYPE_SLASHING=1;
	public final static int TYPE_PIERCING=2;
	public final static int TYPE_BASHING=3;
	public final static int TYPE_BURNING=4;
	public final static int TYPE_BURSTING=5;
	public final static int TYPE_SHOOT=6;
	public final static int TYPE_FROSTING=7;
	public final static int TYPE_GASSING=8;
	public final static int TYPE_MELTING=9;
	public final static int TYPE_STRIKING=10;
	public final static String[] TYPE_DESCS={
	"NATURAL",
	"SLASHING",
	"PIERCING",
	"BASHING",
	"BURNING",
	"BURSTING",
	"SHOOTING",
	"FROSTING",
	"GASSING",
	"MELTING",
	"STRIKING"};


	// weapon classifications
	public final static int CLASS_AXE=0;
	public final static int CLASS_BLUNT=1;
	public final static int CLASS_EDGED=2;
	public final static int CLASS_FLAILED=3;
	public final static int CLASS_HAMMER=4;
	public final static int CLASS_NATURAL=5;
	public final static int CLASS_POLEARM=6;
	public final static int CLASS_RANGED=7;
	public final static int CLASS_SWORD=8;
	public final static int CLASS_DAGGER=9;
	public final static int CLASS_STAFF=10;
	public final static int CLASS_THROWN=11;
	public final static String[] CLASS_DESCS={
	"AXE",
	"BLUNT",
	"EDGED",
	"FLAILED",
	"HAMMER",
	"KARATE",
	"POLEARM",
	"RANGED",
	"SWORD",
	"DAGGER",
	"STAFF",
	"THROWN"};
	
        // shooting mode Classifications
	public final static int SHOOT_NONE=0;
	public final static int SHOOT_SINGLE=1;
	public final static int SHOOT_BURST=2;
	public final static int SHOOT_SINGLEBURST=3;
	public final static int SHOOT_AUTO=4;
	public final static int SHOOT_SINGLEAUTO=5;
	public final static int SHOOT_BURSTAUTO=6;
	public final static int SHOOT_ALLMODES=7;
	public final static String[] SHOOT_DESCS={
	"NO MODES",
	"SEMI-AUTO ONLY",
	"BURST FIRE ONLY",
	"SEMI-AUTO AND BURST",
	"AUTO FIRE ONLY",
	"SEMI-AUTO AND AUTO",
	"BURST AND AUTO",
	"ALL MODES"};
	
	public int weaponType();
	public int weaponClassification();
	public void setWeaponType(int newType);
	public void setWeaponClassification(int newClassification);
	public void setRanges(int min, int max);
	public boolean requiresAmmunition();
	public void setAmmunitionType(String ammo);
	public String ammunitionType();
	public int ammunitionRemaining();
	public void setAmmoRemaining(int amount);
	public int ammunitionCapacity();
	public void setAmmoCapacity(int amount);
	public String hitString(int damageAmount);
	public String missString();
	public int firingModes();
	public void setFiringModes(int newfiringmodes);
	public boolean checkFiringModes(int testfiringmodes);
	public void setFiringMode(int firingmode);
	public int currentFiringMode();
	public boolean isActive();
	public void activateWeapon();
	public void deactivateWeapon();
}
