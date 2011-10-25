package com.planet_ink.coffee_mud.Races;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class GenRace extends StdRace
{
	protected String ID="GenRace";
	public String ID(){	return ID; }
	protected String name="GenRace";
	public String name(){ return name; }
	public int availability=0;
	public int availabilityCode(){return availability;}
	public int[] agingChart=null;
	public int[] getAgingChart()
	{
		if(agingChart==null)
			agingChart=(int[])super.getAgingChart().clone();
		return agingChart;
	}

	public int shortestMale=24;
	public int shortestMale(){return shortestMale;}
	public int shortestFemale=24;
	public int shortestFemale(){return shortestFemale;}
	public int heightVariance=5;
	public int heightVariance(){return heightVariance;}
	public int lightestWeight=60;
	public int lightestWeight(){return lightestWeight;}
	public int weightVariance=10;
	public int weightVariance(){return weightVariance;}
	public long forbiddenWornBits=0;
	public long forbiddenWornBits(){return forbiddenWornBits;}
	public String racialCategory="Unknown";
	public String racialCategory(){return racialCategory;}
	public boolean isGeneric(){return true;}

	protected int disableFlags=0;
	public boolean classless(){return (disableFlags&Race.GENFLAG_NOCLASS)==Race.GENFLAG_NOCLASS;}
	public boolean leveless(){return (disableFlags&Race.GENFLAG_NOLEVELS)==Race.GENFLAG_NOLEVELS;}
	public boolean expless(){return (disableFlags&Race.GENFLAG_NOEXP)==Race.GENFLAG_NOEXP;}
	public boolean fertile(){return !((disableFlags&Race.GENFLAG_NOFERTILE)==Race.GENFLAG_NOFERTILE);}
	protected boolean uncharmable(){return ((disableFlags&Race.GENFLAG_NOCHARM)==Race.GENFLAG_NOCHARM);}

	//					 an ey ea he ne ar ha to le fo no gi mo wa ta wi
	protected int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected CharStats setStats=null;
	protected CharStats adjStats=null;
	protected EnvStats adjEStats=null;
	protected Vector resourceChoices=null;
	protected Race healthBuddy=null;
	protected Race eventBuddy=null;
	protected Race weaponBuddy=null;
	protected String helpEntry = "";

	protected String[] racialEffectNames=null;
	protected int[] racialEffectLevels=null;
	protected String[] racialEffectParms=null;
	protected String[] racialEffectNames(){return racialEffectNames;}
	protected int[] racialEffectLevels(){return racialEffectLevels;}
	protected String[] racialEffectParms(){return racialEffectParms;}

	protected boolean destroyBodyAfterUse=false;
	protected boolean destroyBodyAfterUse(){return destroyBodyAfterUse;}

	public GenRace()
	{
		super();
	}
	
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new GenRace();}}
	public CMObject copyOf()
	{
		GenRace E=new GenRace();
		E.setRacialParms(racialParms());
		return E;
	}
	public Weapon myNaturalWeapon()
	{
		if(weaponBuddy!=null)
			return weaponBuddy.myNaturalWeapon();
		if(naturalWeapon!=null)
			return naturalWeapon;
		return funHumanoidWeapon();
	}

	protected String arriveStr="arrives";
	public String arriveStr()
	{
		return arriveStr;
	}
	protected String leaveStr="leaves";
	public String leaveStr()
	{
		return leaveStr;
	}
	public String healthText(MOB viewer, MOB mob)
	{
		if((healthBuddy!=null)&&(healthBuddy!=this))
			return healthBuddy.healthText(viewer,mob);
		return CMLib.combat().standardMobCondition(viewer,mob);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(adjEStats!=null)
		{
			affectableStats.setAbility(affectableStats.ability()+adjEStats.ability());
			affectableStats.setArmor(affectableStats.armor()+adjEStats.armor());
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+adjEStats.attackAdjustment());
			affectableStats.setDamage(affectableStats.damage()+adjEStats.damage());
			affectableStats.setDisposition(affectableStats.disposition()|adjEStats.disposition());
			affectableStats.setHeight(affectableStats.height()+adjEStats.height());
			affectableStats.setLevel(affectableStats.level()+adjEStats.level());
			affectableStats.setSensesMask(affectableStats.sensesMask()|adjEStats.sensesMask());
			affectableStats.setSpeed(affectableStats.speed()+adjEStats.speed());
			affectableStats.setWeight(affectableStats.weight()+adjEStats.weight());
		}
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
	}
	public Vector myResources(){
		if(resourceChoices==null)
			return new Vector();
		return resourceChoices;
	}

	protected String getRaceLocatorID(Race R)
	{
		if(R==null) return "";
		if(R.isGeneric()) return R.ID();
		if(R==CMClass.getRace(R.ID()))
			return R.ID();
		return R.getClass().getName();
	}

	public String racialParms()
	{
		StringBuffer str=new StringBuffer("");
		str.append("<RACE><ID>"+ID()+"</ID>");
		str.append(CMLib.xml().convertXMLtoTag("NAME",name()));
		str.append(CMLib.xml().convertXMLtoTag("CAT",racialCategory()));
		str.append(CMLib.xml().convertXMLtoTag("MHEIGHT",""+shortestMale()));
		str.append(CMLib.xml().convertXMLtoTag("FHEIGHT",""+shortestFemale()));
		str.append(CMLib.xml().convertXMLtoTag("VHEIGHT",""+heightVariance()));
		str.append(CMLib.xml().convertXMLtoTag("BWEIGHT",""+lightestWeight()));
		str.append(CMLib.xml().convertXMLtoTag("VWEIGHT",""+weightVariance()));
		str.append(CMLib.xml().convertXMLtoTag("WEAR",""+forbiddenWornBits()));
		str.append(CMLib.xml().convertXMLtoTag("AVAIL",""+availability));
		str.append(CMLib.xml().convertXMLtoTag("DESTROYBODY",""+destroyBodyAfterUse()));
		StringBuffer bbody=new StringBuffer("");
		for(int i=0;i<bodyMask().length;i++)
			bbody.append((""+bodyMask()[i])+";");
		str.append(CMLib.xml().convertXMLtoTag("BODY",bbody.toString()));
		str.append(CMLib.xml().convertXMLtoTag("HEALTHRACE",getRaceLocatorID(healthBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("EVENTRACE",getRaceLocatorID(eventBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("WEAPONRACE",getRaceLocatorID(weaponBuddy)));
		str.append(CMLib.xml().convertXMLtoTag("ARRIVE",arriveStr()));
		str.append(CMLib.xml().convertXMLtoTag("LEAVE",leaveStr()));
		str.append(CMLib.xml().convertXMLtoTag("HELP",CMLib.xml().parseOutAngleBrackets(helpEntry)));
		str.append(CMLib.xml().convertXMLtoTag("AGING",CMParms.toStringList(getAgingChart())));
		str.append(CMLib.xml().convertXMLtoTag("DISFLAGS",""+disableFlags));

		if(myResources().size()==0)	str.append("<RESOURCES/>");
		else
		{
			str.append("<RESOURCES>");
			for(int i=0;i<myResources().size();i++)
			{
				Item I=(Item)myResources().elementAt(i);
				str.append("<RSCITEM>");
				str.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(I)));
				str.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.xml().parseOutAngleBrackets(I.text())));
				str.append("</RSCITEM>");
			}
			str.append("</RESOURCES>");
		}
		if((outfit(null)==null)||(outfit(null).size()==0))	str.append("<OUTFIT/>");
		else
		{
			str.append("<OUTFIT>");
			for(int i=0;i<outfit(null).size();i++)
			{
				Item I=(Item)outfit(null).elementAt(i);
				str.append("<OFTITEM>");
				str.append(CMLib.xml().convertXMLtoTag("OFCLASS",CMClass.classID(I)));
				str.append(CMLib.xml().convertXMLtoTag("OFDATA",CMLib.xml().parseOutAngleBrackets(I.text())));
				str.append("</OFTITEM>");
			}
			str.append("</OUTFIT>");
		}
		if(naturalWeapon==null) str.append("<WEAPON/>");
		else
		{
			str.append("<WEAPON>");
			str.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(naturalWeapon)));
			str.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.xml().parseOutAngleBrackets(naturalWeapon.text())));
			str.append("</WEAPON>");
		}

		if((racialEffectNames==null)||(racialEffectNames.length==0))
			str.append("<REFFECTS/>");
		else
		{
			str.append("<REFFECTS>");
			for(int r=0;r<racialEffectNames.length;r++)
			{
				str.append("<REFFECT>");
				str.append("<RFCLASS>"+racialEffectNames[r]+"</RFCLASS>");
				str.append("<RFLEVEL>"+racialEffectLevels[r]+"</RFLEVEL>");
				str.append("<RFPARM>"+racialEffectParms[r]+"</RFPARM>");
				str.append("</REFFECT>");
			}
			str.append("</REFFECTS>");
		}


		str.append("</RACE>");
		return str.toString();
	}
	public void setRacialParms(String parms)
	{
		if(parms.trim().length()==0)
		{
			Log.errOut("GenRace","Unable to parse empty xml");
			return;
		}
		Vector xml=CMLib.xml().parseAllXML(parms);
		if(xml==null)
		{
			Log.errOut("GenRace","Unable to parse xml: "+parms);
			return;
		}
		Vector raceData=CMLib.xml().getRealContentsFromPieces(xml,"RACE");
		if(raceData==null){	Log.errOut("GenRace","Unable to get RACE data: ("+parms.length()+"): "+CMStrings.padRight(parms,30)+"."); return;}
		String id=CMLib.xml().getValFromPieces(raceData,"ID");
		if(id.length()==0)
		{
			Log.errOut("GenRace","Unable to parse: "+parms);
			return;
		}
		ID=id;
		name=CMLib.xml().getValFromPieces(raceData,"NAME");
		if((name==null)||(name.length()==0))
		{
			Log.errOut("GenRace","Not able to parse: "+parms);
			return;
		}

		String rcat=CMLib.xml().getValFromPieces(raceData,"CAT");
		if((rcat==null)||(rcat.length()==0))
		{
			rcat=name;
			return;
		}

		racialCategory=rcat;
		forbiddenWornBits=CMLib.xml().getLongFromPieces(raceData,"WEAR");
		weightVariance=CMLib.xml().getIntFromPieces(raceData,"VWEIGHT");
		lightestWeight=CMLib.xml().getIntFromPieces(raceData,"BWEIGHT");
		heightVariance=CMLib.xml().getIntFromPieces(raceData,"VHEIGHT");
		shortestFemale=CMLib.xml().getIntFromPieces(raceData,"FHEIGHT");
		shortestMale=CMLib.xml().getIntFromPieces(raceData,"MHEIGHT");
		helpEntry=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(raceData,"HELP"));
		String playerval=CMLib.xml().getValFromPieces(raceData,"PLAYER").trim().toUpperCase();
		if(playerval.length()>0)
		{
			if(playerval.startsWith("T"))
				availability=Area.THEME_FANTASY;
			else
			if(playerval.startsWith("F"))
				availability=0;
			else
			switch(CMath.s_int(playerval))
			{
			case 0: availability=Area.THEME_FANTASY; break;
			case 1: availability=Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK; break;
			case 2: availability=0; break;
			}
		}
		String avail=CMLib.xml().getValFromPieces(raceData,"AVAIL").trim().toUpperCase();
		if((avail!=null)&&(avail.length()>0)&&(CMath.isNumber(avail)))
			availability=CMath.s_int(avail);
		destroyBodyAfterUse=CMLib.xml().getBoolFromPieces(raceData,"DESTROYBODY");
		leaveStr=CMLib.xml().getValFromPieces(raceData,"LEAVE");
		arriveStr=CMLib.xml().getValFromPieces(raceData,"ARRIVE");
		String body=CMLib.xml().getValFromPieces(raceData,"BODY");
		Vector V=CMParms.parseSemicolons(body,false);
		for(int v=0;v<V.size();v++)
			if(v<bodyMask().length)
				bodyMask()[v]=CMath.s_int((String)V.elementAt(v));
		adjEStats=null;
		adjStats=null;
		setStats=null;
		disableFlags=CMLib.xml().getIntFromPieces(raceData,"DISFLAGS");
		String aging=CMLib.xml().getValFromPieces(raceData,"AGING");
		Vector aV=CMParms.parseCommas(aging,true);
		for(int v=0;v<aV.size();v++)
			getAgingChart()[v]=CMath.s_int((String)aV.elementAt(v));
		clrStatChgDesc();
		// now RESOURCES!
		Vector xV=CMLib.xml().getRealContentsFromPieces(raceData,"RESOURCES");
		resourceChoices=null;
		if((xV!=null)&&(xV.size()>0))
		{
			resourceChoices=new Vector();
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("RSCITEM"))||(iblk.contents==null))
					continue;
				Item newOne=CMClass.getItem(CMLib.xml().getValFromPieces(iblk.contents,"ICLASS"));
				String idat=CMLib.xml().getValFromPieces(iblk.contents,"IDATA");
				newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
				newOne.recoverEnvStats();
				resourceChoices.addElement(newOne);
			}
		}

		// now OUTFIT!
		Vector oV=CMLib.xml().getRealContentsFromPieces(raceData,"OUTFIT");
		outfitChoices=null;
		if((oV!=null)&&(oV.size()>0))
		{
			outfitChoices=new Vector();
			for(int x=0;x<oV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)oV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("OFTITEM"))||(iblk.contents==null))
					continue;
				Item newOne=CMClass.getItem(CMLib.xml().getValFromPieces(iblk.contents,"OFCLASS"));
				if(newOne != null)
				{
					String idat=CMLib.xml().getValFromPieces(iblk.contents,"OFDATA");
					newOne.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
					newOne.recoverEnvStats();
					outfitChoices.addElement(newOne);
				}
				else
					Log.errOut("GenRace","Unknown newOne race: " + CMLib.xml().getValFromPieces(iblk.contents,"OFCLASS"));
			}
		}

		naturalWeapon=null;
		Vector wblk=CMLib.xml().getRealContentsFromPieces(raceData,"WEAPON");
		if(wblk!=null)
		{
			naturalWeapon=CMClass.getWeapon(CMLib.xml().getValFromPieces(wblk,"ICLASS"));
			String idat=CMLib.xml().getValFromPieces(wblk,"IDATA");
			if((idat!=null)&&(naturalWeapon!=null))
			{
				naturalWeapon.setMiscText(CMLib.xml().restoreAngleBrackets(idat));
				naturalWeapon.recoverEnvStats();
			}
		}

		xV=CMLib.xml().getRealContentsFromPieces(raceData,"REFFECTS");
		racialEffectNames=null;
		racialEffectParms=null;
		racialEffectLevels=null;
		if((xV!=null)&&(xV.size()>0))
		{
			racialEffectNames=new String[xV.size()];
			racialEffectParms=new String[xV.size()];
			racialEffectLevels=new int[xV.size()];
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("REFFECT"))||(iblk.contents==null))
					continue;
				racialEffectNames[x]=CMLib.xml().getValFromPieces(iblk.contents,"RFCLASS");
				racialEffectParms[x]=CMLib.xml().getValFromPieces(iblk.contents,"RFPARM");
				racialEffectLevels[x]=CMLib.xml().getIntFromPieces(iblk.contents,"RFLEVEL");
			}
		}
	}
	public boolean tick(Tickable myChar, int tickID)
	{
		if(eventBuddy!=null)
			if(!eventBuddy.tick(myChar,tickID))
				return false;
		return super.tick(myChar, tickID);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(eventBuddy!=null)
			eventBuddy.executeMsg(myHost, msg);
		super.executeMsg(myHost, msg);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((eventBuddy!=null)
		&&(!eventBuddy.okMessage(myHost, msg)))
			return false;
		return super.okMessage(myHost, msg);

	}

	public void startRacing(MOB mob, boolean verifyOnly)
	{
		super.startRacing(mob,verifyOnly);
	}
	public boolean sameAs(Race E)
	{
		if(!(E instanceof GenRace)) return false;
		if(((GenRace)E).racialParms().equals(racialParms()))
			return true;
		return false;
	}
}
