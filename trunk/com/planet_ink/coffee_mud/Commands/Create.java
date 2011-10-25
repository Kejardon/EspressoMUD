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
import java.io.IOException;

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
public class Create extends StdCommand
{
	public Create(){}

	private String[] access={"CREATE"};
	public String[] getAccessWords(){return access;}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE EXIT [DIRECTION] [EXIT TYPE]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String Locale=(String)commands.elementAt(3);
		Exit thisExit=CMClass.getExit(Locale);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit type '"+Locale+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		Exit opExit=mob.location().getRawExit(direction);
		Room opRoom=mob.location().rawDoors()[direction];

		Exit reverseExit=null;
		if(opRoom!=null)
			reverseExit=opRoom.getRawExit(Directions.getOpDirectionCode(direction));
		if(reverseExit!=null)
		{
			if((thisExit.isGeneric())&&(reverseExit.isGeneric()))
			{
				thisExit=(Exit)reverseExit.copyOf();
				CMLib.genEd().genMiscSet(mob,thisExit);
			}
		}


		mob.location().setRawExit(direction,thisExit);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a portal opens up "+Directions.getInDirectionName(direction)+".\n\r");
		CMLib.database().DBUpdateExits(mob.location());
		if((reverseExit!=null)&&(opExit!=null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if(opRoom.getRawExit(revDirCode)==reverseExit)
			{
				opRoom.setRawExit(revDirCode,(Exit)thisExit.copyOf());
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		else
		if((reverseExit==null)&&(opExit==null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if((opRoom.getRawExit(revDirCode)==null)&&(opRoom.rawDoors()[revDirCode]==mob.location()))
			{
				opRoom.setRawExit(revDirCode,(Exit)thisExit.copyOf());
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(opRoom!=null) opRoom.getArea().fillInAreaRoom(opRoom);
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.Name()+".");
	}

	
	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ITEM [ITEM NAME](@ room/[MOB NAME])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=CMParms.combine(commands,2);
		Environmental dest=mob.location();
		Item setContainer=null;
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				MOB M=mob.location().fetchInhabitant(rest);
				if(M==null)
				{
					Item I = mob.location().fetchItem(null, rest);
					if(I instanceof Container)
						setContainer=(Container)I;
					else
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return;
					}
				}
				else
					dest=M;
			}
		}
		Item newItem=CMClass.getItem(itemID);
		if((newItem==null)&&(CMLib.english().numPossibleGold(null,itemID)>0))
		{
			long numCoins=CMLib.english().numPossibleGold(null,itemID);
			String currency=CMLib.english().numPossibleGoldCurrency(mob,itemID);
			double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,itemID);
			if((numCoins>0)&&(denom>0.0))
				newItem=CMLib.beanCounter().makeCurrency(currency,denom,numCoins);
		}

		boolean doGenerica=true;
		if(newItem==null)
		{
			newItem=getNewCatalogItem(itemID);
			doGenerica=newItem==null;
		}
		
		if(newItem==null)
		{
			mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if((newItem instanceof ArchonOnly)
		&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell("NO!");
			return;
		}

		if(newItem.subjectToWearAndTear())
			newItem.setUsesRemaining(100);
		if(dest instanceof Room)
		{
			((Room)dest).addItem(newItem);
			newItem.setContainer(setContainer);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
		}
		else
		if(dest instanceof MOB)
		{
			((MOB)dest).addItem(newItem);
			newItem.setContainer(setContainer);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops into "+dest.name()+"'s arms.");
		}

		if((newItem.isGeneric())&&(doGenerica))
			CMLib.genEd().genMiscSet(mob,newItem);
		mob.location().recoverRoomStats();
		Log.sysOut("Items",mob.Name()+" created item "+newItem.ID()+".");
	}

	public void players(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE USER [PLAYER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
	
		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			mob.tell("There is already a player called '"+M.Name()+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!CMLib.login().isOkName(mobID))
		{
			mob.tell("'"+mobID+"' is not a valid name.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		mobID=CMStrings.capitalizeAndLower(mobID);
		CMLib.login().createCharacter(null,mobID,mob.session());
		mob.session().mob().setSession(null);
		mob.session().setMob(mob);
		M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			if(CMLib.flags().isInTheGame(M,true))
				M.removeFromGame(false);
			CMLib.genEd().genMiscSet(mob,M);
			Log.sysOut("Mobs",mob.Name()+" created player "+M.Name()+".");
		}
	}

	public void rooms(MOB mob, Vector commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Room thisRoom=null;
		String Locale=(String)commands.elementAt(3);
		thisRoom=CMClass.getLocale(Locale);
		if(thisRoom==null)
		{
			mob.tell("You have failed to specify a valid room type '"+Locale+"'.\n\rThe format is CREATE ROOM [DIRECTION] ([ROOM TYPE] / LINK [ROOM ID]) \n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		Room room=mob.location();
		thisRoom.setRoomID(room.getArea().getNewRoomID(room,direction));
		thisRoom.setArea(room.getArea());
		if(thisRoom.roomID().length()==0)
		{
			mob.tell("A room may not be created in that direction.  Are you sure you havn't reached the edge of a grid?");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		thisRoom.setDisplayText(CMClass.classID(thisRoom)+"-"+thisRoom.roomID());
		thisRoom.setDescription("");
		CMLib.database().DBCreateRoom(thisRoom);

		CMLib.map().createNewExit(mob.location(),thisRoom,direction);

		mob.location().recoverRoomStats();
		thisRoom.recoverRoomStats();
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a block of earth falls from the sky.\n\r");
		Log.sysOut("Rooms",mob.Name()+" created room "+thisRoom.roomID()+".");
	}
	
	public void accounts(MOB mob, Vector commands)
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ACCOUNT [NAME] [PASSWORD]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		PlayerAccount thisAcct=null;
		String AcctName=CMStrings.capitalizeAndLower((String)commands.elementAt(2));
		String password=CMStrings.capitalizeAndLower(CMParms.combine(commands,3));
		thisAcct=CMLib.players().getLoadAccount(AcctName);
		if(thisAcct!=null)
		{
			mob.tell("Account '"+AcctName+"' already exists!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!CMLib.login().isOkName(AcctName))
		{
			mob.tell("Name '"+AcctName+"' is not permitted.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		thisAcct=(PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		thisAcct.setAccountName(AcctName);
		thisAcct.setAccountExpiration(0);
		if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
			thisAcct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*((long)CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS))));
		thisAcct.setLastDateTime(System.currentTimeMillis());
		thisAcct.setLastUpdated(System.currentTimeMillis());
		thisAcct.setPassword(password);
		CMLib.database().DBCreateAccount(thisAcct);

		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A new soul descends from the heavens and dissipates.\n\r");
		Log.sysOut("Create",mob.Name()+" created account "+thisAcct.accountName()+".");
	}
	
	public MOB getNewCatalogMob(String mobID)
	{
		MOB newMOB=CMLib.catalog().getCatalogMob(mobID);
		if(newMOB!=null)
		{
			newMOB=(MOB)newMOB.copyOf();
			try { CMLib.catalog().changeCatalogUsage(newMOB,true);} catch(Throwable t){}
			newMOB.text();
		}
		return newMOB;
	}
	
	public Item getNewCatalogItem(String itemID)
	{
		Item newItem=CMLib.catalog().getCatalogItem(itemID);
		if(newItem!=null)
		{
			newItem=(Item)newItem.copyOf();
			try { CMLib.catalog().changeCatalogUsage(newItem,true);} catch(Throwable t){}
			newItem.text();
		}
		return newItem;
	}

	public void mobs(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB newMOB=CMClass.getMOB(mobID);

		boolean doGenerica=true;
		if(newMOB==null)
		{
			newMOB=getNewCatalogMob(mobID);
			doGenerica=newMOB==null;
		}
		
		if(newMOB==null)
		{
			mob.tell("There's no such thing as a '"+mobID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		
		if(newMOB.Name().length()==0)
			newMOB.setName("A Standard MOB");
		newMOB.setStartRoom(mob.location());
		newMOB.setLocation(mob.location());
		long rejuv=Tickable.TICKS_PER_RLMIN+Tickable.TICKS_PER_RLMIN+(Tickable.TICKS_PER_RLMIN/2);
		if(rejuv>(Tickable.TICKS_PER_RLMIN*20)) rejuv=(Tickable.TICKS_PER_RLMIN*20);
		newMOB.envStats().setRejuv((int)rejuv);
		newMOB.baseCharStats().getMyRace().setHeightWeight(newMOB.baseEnvStats(),(char)newMOB.baseCharStats().gender());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.resetToMaxState();
		newMOB.bringToLife(mob.location(),true);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
		if((newMOB.isGeneric())&&(doGenerica))
			CMLib.genEd().genMiscSet(mob,newMOB);
		Log.sysOut("Mobs",mob.Name()+" created mob "+newMOB.Name()+".");
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE AREA [AREA NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String areaName=CMParms.combine(commands,2);
		Area A=CMLib.map().getArea(areaName);
		if(A!=null)
		{
			mob.tell("An area with the name '"+A.name()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String areaType="";
		int tries=0;
		while((areaType.length()==0)&&((++tries)<10))
		{
			areaType=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
			if(CMClass.getAreaType(areaType)==null)
			{
				mob.session().println("Invalid area type! Valid ones are:");
				mob.session().println(CMLib.lister().reallyList(CMClass.areaTypes(),-1,null).toString());
				areaType="";
			}
		}
		if(areaType.length()==0) areaType="StdArea";
		A=CMClass.getAreaType(areaType);
		A.setName(areaName);
		CMLib.map().addArea(A);
		CMLib.database().DBCreateArea(A);
		
		Room R=CMClass.getLocale("StdRoom");
		R.setRoomID(A.getNewRoomID(R,-1));
		R.setArea(A);
		R.setDisplayText(CMClass.classID(R)+"-"+R.roomID());
		R.setDescription("");
		CMLib.database().DBCreateRoom(R);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The size of the world just increased!");
		mob.tell("You are now at "+R.roomID()+".");
		R.bringMobHere(mob);
	}
	public void titles(MOB mob, Vector commands)
	{
		if((commands.size()<3)||(CMParms.combine(commands,1).indexOf("=")<0))
		{
			mob.tell("You have failed to specify the proper fields.\n\rFormat: CREATE TITLE [TITLE]=[ZAPPER MASK] as follows: \n\r");
			StringBuffer buf=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true).text();
			StringBuffer inst=new StringBuffer("");
			Vector V=new Vector();
			if(buf!=null) V=Resources.getFileLineVector(buf);
			for(int v=0;v<V.size();v++)
				if(((String)V.elementAt(v)).startsWith("#"))
					inst.append(((String)V.elementAt(v)).substring(1)+"\n\r");
				else
				if(((String)V.elementAt(v)).length()>0) 
					break;
			if(mob.session()!=null) mob.session().wraplessPrintln(inst.toString());
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String parms=CMParms.combineWithQuotes(commands,2);
		String skillID=parms.substring(0,parms.indexOf("="));
		if(CMLib.titles().isExistingAutoTitle(skillID))
		{
			mob.tell("'"+skillID+"' already exists, you'll need to destroy it first.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String error=CMLib.titles().evaluateAutoTitle(parms,false);
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CMFile F=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true);
		F.saveText("\n"+parms,true);
		Resources.removeResource("titles.txt");
		CMLib.titles().reloadAutoTitles();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The prestige of the players just increased!");
	}
	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;
		
		if(commands.size()<3)
		{
			mob.tell("but fail to specify the proper fields.\n\rThe format is CREATE SOCIAL [NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		String stuff=CMParms.combine(commands,2);
		if(CMLib.socials().fetchSocial(stuff,false)!=null)
		{
			mob.tell("The social '"+stuff+"' already exists.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		CMLib.socials().modifySocialInterface(mob,stuff);
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
		}
		else
		if(commandType.equals("TITLE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"TITLES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			titles(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			areas(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			rooms(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			accounts(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			players(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			String lastWord=null;
			if(commands.size()>2)
				lastWord=(String)commands.lastElement();
			Environmental E=null;
			E=CMClass.getItem(allWord);
			if(((E!=null)&&(E instanceof Item))
			||(CMLib.english().numPossibleGold(null,allWord)>0)
			||(CMLib.catalog().getCatalogItem(allWord)!=null))
			{
				commands.insertElementAt("ITEM",1);
				execute(mob,commands,metaFlags);
			}
			else
			{
				E=CMClass.getMOB(allWord);
				if(((E!=null)&&(E instanceof MOB))
				||(CMLib.catalog().getCatalogMob(allWord)!=null))
				{
					commands.insertElementAt("MOB",1);
					execute(mob,commands,metaFlags);
				}
				else
				if((lastWord!=null)&&(Directions.getGoodDirectionCode(lastWord)>=0))
				{
					commands.removeElementAt(commands.size()-1);
					allWord=CMParms.combine(commands,1);

					E=CMClass.getLocale(allWord);
					if(E==null)
						E=CMClass.getExit(allWord);
					if(E==null)
						E=CMClass.getAreaType(allWord);
					if((E!=null)&&(E instanceof Room))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("ROOM");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if((E!=null)&&(E instanceof Exit))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("EXIT");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if((E!=null)&&(E instanceof Area))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("AREA");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);
					}
					else
						mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, COMPONENT, MOB, RACE, ABILITY, CLASS, USER, or ROOM.");
				}
				else
					mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, MOB, COMPONENT, RACE, ABILITY, CLASS, USER, or ROOM.");
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	
}
