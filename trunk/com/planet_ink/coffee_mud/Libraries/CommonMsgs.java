package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import com.planet_ink.coffee_mud.core.database.*;

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
public class CommonMsgs extends StdLibrary implements CommonCommands
{
	public String ID(){return "CommonMsgs";}

	protected final static int LOOK_LONG=0;
	protected final static int LOOK_NORMAL=1;
	protected final static int LOOK_BRIEFOK=2;
	//protected String unknownCommand(){return "Huh?";}
	//protected String unknownInvoke(){return "You don't know how to @x1 that.";}

	public boolean handleUnknownCommand(MOB mob, String command)
	{
		if(mob==null) return false;
		Room R=mob.location();
		//String msgStr=unknownCommand();
		mob.tell("Huh?"); return false;
		/*
		if(R==null){ mob.tell("Huh?"); return false;}
		CMMsg msg=CMClass.getMsg(mob,null,null,EnumSet.of(CMMsg.MsgCode.HUH),"Huh?",EnumSet.of(CMMsg.MsgCode.HUH),command,EnumSet.of(CMMsg.MsgCode.HUH),null);
		return R.doAndReturnMsg(msg);
		*/
	}

	//NOTE: Nothing that calls this should have a command with command.prompter() unless the thread is willing to wait indefinitely
	public boolean forceStandardCommand(MOB mob, String command, String parms)
	{
		Command C=CMClass.COMMAND.getCommand(command, true, mob);
		if(C!=null)
			return C.execute(mob,C.prepCommand(mob, parms, Command.METAFLAG_FORCED));
		return false;
	}

	public StringBuilder getScore(MOB mob){return getScore(mob,"");}
	public StringBuilder getScore(MOB mob, String parm)	//TODO later
	{
		StringBuilder msg=new StringBuilder("^.");
		msg.append("You are ").append(mob.name()).append("^..\r\n");
/*
		String genderName="neuter";
		if(mob.charStats().gender()=='M') genderName="male";
		else
		if(mob.charStats().gender()=='F') genderName="female";
		msg.append("You are a ");
		if(mob.baseCharStats().age()>0)
			msg.append("^!"+mob.baseCharStats().age()+"^? year old ");
		msg.append("^!"+genderName);
		if((!CMSecurity.isDisabled("RACES")))
			msg.append(" "+mob.charStats().getMyRace().name() + "^?");
		else
			msg.append("^?");
		msg.append(".\r\n");
*/
		msg.append("\r\nYour character stats are:\r\n");
		CharStats CT=mob.charStats();
		if(parm.equalsIgnoreCase("BASE")) CT=mob.baseCharStats();
		for(CharStats.Stat i : CharStats.Stat.values()) if(CT.getStat(i)!=-1)
			msg.append(CMStrings.padRight(i.friendlyName,15)).append(": ").append(CMStrings.padRight(Integer.toString(CT.getStat(i)),3)).append("\r\n");
		msg.append("\r\n");
		Body body=mob.body();
		if(body!=null)
		{
			msg.append("\r\n^NYour body stats are:\r\n");
			CT=body.charStats();
			if(parm.equalsIgnoreCase("BASE")) CT=body.baseCharStats();
			for(CharStats.Stat i : CharStats.Stat.values()) if(CT.getStat(i)!=-1)
				msg.append(CMStrings.padRight(i.friendlyName,15)).append(": ").append(
						CMStrings.padRight(Integer.toString(CT.getStat(i)),3)).append("\r\n");
			msg.append("\r\n");
			msg.append("You have ").append(body.charStats().getPoints(CharStats.Points.HIT)).append("/").append(body.charStats().getMaxPoints(CharStats.Points.HIT)).append(" hit points and ");
			msg.append(body.charStats().getPoints(CharStats.Points.MANA)).append("/").append(body.charStats().getMaxPoints(CharStats.Points.MANA)).append(" mana.\r\n");
			msg.append("You are ").append(body.getEnvObject().envStats().height()).append(" inches tall.\r\n");
		}
//		if(CMSecurity.isAllowed(mob,"CARRYALL"))
//			msg.append("You are carrying ^!"+mob.numItems()+"^? items weighing ^!"+mob.envStats().weight()+"^? pounds.\r\n");
//		msg.append("You have been online for ^!"+Math.round(CMath.div(mob.getAgeHours(),60.0))+"^? hours.\r\n");
//		msg.append("Your ^<HELP^>armored defence^</HELP^> is: ^H"+CMLib.combat().armorStr(mob)+"^?.\r\n");
//		msg.append("Your ^<HELP^>combat prowess^</HELP^> is : ^H"+CMLib.combat().fightingProwessStr(mob)+"^?.\r\n");
		//if(CMLib.flags().canSeeHidden(mob))
		//	msg.append("Your ^<HELP^>observation score^</HELP^> : ^H"+CMLib.flags().getDetectScore(mob)+"^?.\r\n");
		int pct=(int)(5.5*mob.charStats().getPointsPercent(CharStats.Points.FATIGUE));
		switch(pct)
		{
			case 0:
				msg.append("You are about to drop!\r\n");
				break;
			case 1:
				msg.append("You are exhausted.\r\n");
				break;
			case 2:
				msg.append("You are winded.\r\n");
				break;
			case 3:
				msg.append("You are tired.\r\n");
				break;
			case 4:
				msg.append("You are a bit tired.\r\n");
				break;
		}
		pct=(int)(4.5*mob.charStats().getPointsPercent(CharStats.Points.HUNGER));
		switch(pct)
		{
			case 0:
				msg.append("You are starving!\r\n");
				break;
			case 1:
				msg.append("You need to eat something.\r\n");
				break;
			case 2:
				msg.append("You are hungry.\r\n");
				break;
			case 3:
				msg.append("You could eat a snack.\r\n");
				break;
		}
		pct=(int)(4.5*mob.charStats().getPointsPercent(CharStats.Points.THIRST));
		switch(pct)
		{
			case 0:
				msg.append("You are dying of thirst!\r\n");
				break;
			case 1:
				msg.append("You need to drink something.\r\n");
				break;
			case 2:
				msg.append("You are parched.\r\n");
				break;
			case 3:
				msg.append("You could use a drink.\r\n");
				break;
		}
		return msg;
	}

	public StringBuilder getEquipment(MOB viewer, MOB mob)
	{
		/*Vector V=new Vector();
		V.addElement(viewer);
		forceStandardCommand(mob,"Equipment",V);
		if((V.size()>1)&&(V.elementAt(1) instanceof StringBuilder))
			return (StringBuilder)V.elementAt(1);*/
		return new StringBuilder("");
	}
	public StringBuilder getInventory(MOB viewer, MOB mob)
	{ return getInventory(viewer, mob, ""); }
	public StringBuilder getInventory(MOB viewer, MOB mob, String mask)
	{
		StringBuilder msg=new StringBuilder("");
		Iterator<Item> items = mob.getItemCollection().allItems();
		ArrayList<Item> list;//=new ArrayList();
		if((mask!=null)&&(mask.trim().length()>0))
		{
			list=new ArrayList();
			mask=mask.trim().toUpperCase();
//			if(!mask.startsWith("all")) mask="all "+mask;
			while(items.hasNext())
			{
				Item I = items.next();
				if(CMLib.english().isCalled(I, mask, false))
					list.add(I);
			}
		}
		else
			list=CMParms.toArrayList(items);
		if(list.size()==0)
		{
			if((mask!=null)&&(mask.length()>0))
				msg.append("(nothing like that you can see right now)");
			else
				msg.append("(nothing you can see right now)");
		}
		else
			msg.append(CMLib.lister().lister(viewer,list,true,"MItem","",false));
		return msg;
	}

	public void postChannel(MOB mob,
							String channelName,
							String message,
							boolean systemMsg)
	{
//TODO: Don't want to do it this way. Will find a better way to manually send messages.
//		forceStandardCommand(mob,"Channel",CMParms.makeVector(Boolean.valueOf(systemMsg),channelName,message));
	}

	public void postChannel(String channelName,
							String message,
							boolean systemMsg)
	{
		postChannel(null,channelName,message,systemMsg);
	}

	public void postLook(MOB mob)
	{
		forceStandardCommand(mob,"Look","LOOK");
	}

	public void postSay(MOB mob, MOB target,String text){ postSay(mob,target,text,false);}
	public void postSay(MOB mob, String text){ postSay(mob,null,text,false);}
	public void postSay(MOB mob,
						MOB target,
						String text,
						boolean isPrivate)
	{
		Room location=mob.location();
		if(target!=null)
			location=target.location();
		if(location==null) return;
		if((isPrivate)&&(target!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.SPEAK),"^[S-NAME] say^s '"+text+"^.' to ^[T-NAMESELF].");
			location.doAndReturnMsg(msg);
		}
		else if(!isPrivate)
		{
			String str="^[S-NAME] say^s '"+text+((target==null)?"^.'":"^.' to ^[T-NAMESELF].");
			CMMsg msg=CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.SPEAK),str);
			location.doAndReturnMsg(msg);
		}
	}

	public void handleBeingLookedAt(CMMsg msg)
	{
		//if(msg.target() instanceof Room)
		//	handleBeingRoomLookedAt(msg);
		//else
		//if(msg.target() instanceof Item)
		//	handleBeingItemLookedAt(msg);
		//else
		if(msg.target() instanceof MOB)
			handleBeingMobLookedAt(msg);
		else
		if(msg.target() instanceof Exit)
			handleBeingExitLookedAt(msg);
	}


	protected void handleBeingExitLookedAt(CMMsg msg)
	{
		Exit exit=(Exit)msg.target();
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB mob=(MOB)I;
			if(exit.description().length()>0)
				mob.tell(exit.description());
			else
				mob.tell("You don't see anything special.");
			if(mob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS))
			{
				mob.tell("Type   : "+exit.ID());
				mob.tell("SaveNum: "+exit.saveNum());
			}
		}
	}

	protected void handleBeingMobLookedAt(CMMsg msg)
	{
		MOB viewedmob=(MOB)msg.target();
		Body viewedbody=viewedmob.body();
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)||((MOB)I).session()==null) continue;
			MOB viewermob=(MOB)I;
			boolean longlook=msg.hasTargetCode(CMMsg.MsgCode.EXAMINE);
			StringBuilder myDescription=new StringBuilder();
			if(viewermob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS))
				myDescription.append("\r\nType :"+viewedmob.ID()).append(
									"\r\nDesc : "+viewedmob.description()).append(
									//"\r\nRoom :'"+((viewedmob.getStartRoom()==null)?"null":viewedmob.getStartRoom().roomID())).append(
									"\r\n");
			myDescription.append(viewedmob.displayName(viewermob)+" ");
			myDescription.append("is here.\r\n");
			//myDescription.append(viewedmob.healthText(viewermob)+"\r\n\r\n");
			myDescription.append(viewedmob.description()+"\r\n\r\n");

			StringBuilder eq=getEquipment(viewermob,viewedmob);
			if(eq.length() > 0)
				myDescription.append("It is wearing:\r\n"+eq.toString());
			viewermob.tell(myDescription.toString());
			/*if(longlook)
			{
				Command C=CMClass.getCommand("Consider");
				try{if(C!=null)C.execute(viewermob,CMParms.makeVector(viewedmob),0);}catch(java.io.IOException e){}
			}*/
		}
	}

	//TODO: Update when I know more how I want readables to work
	public void handleBeingRead(CMMsg msg)
	{
		//if((msg.targetMessage()!=null)&&(msg.targetMessage().equals("CANCEL"))) return;
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)||((MOB)I).session()==null) continue;
			MOB mob=(MOB)I;
			String text=null;
			if((msg.target() instanceof CMReadable)&&(((CMReadable)msg.target()).isReadable()))
				text=((CMReadable)msg.target()).readableText();
			if((text!=null)
			&&(text.length()>0))
				mob.tell("It says '"+text+"'.");
			else
				mob.tell("There is nothing written on "+msg.target().name()+".");
		}
	}

	public void lookAtExits(Room room, MOB mob)
	{
		if((mob==null)||(room==null)||(mob.isMonster()))
			return;
		StringBuilder buf=new StringBuilder("Obvious exits:\r\n");
		String Dir=null;
		for(Iterator<Room.REMap> iter=room.getAllExits();iter.hasNext();)
		{
			Room.REMap next=iter.next();
			Exit exit=next.exit;
			Room room2=next.room;
			StringBuilder Say=new StringBuilder();
			if(exit!=null)
				Say.append(exit.exitListLook(mob, room2));
			else if((room2!=null)&&(mob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS)))
				Say.append(room2.saveNum()+" via NULL");
			if(Say.length()>0)
			{
//				Dir=CMStrings.padRightPreserve(Directions.getDirectionName(d),5);
//				if((mob.playerStats()!=null)
//				&&(room2!=null)
//				&&(!mob.playerStats().hasVisited(room2)))
//					buf.append("^U^<EX^>" + Dir+"^</EX^>:^.^N ^u"+Say+"^.^N\r\n");
//				else
//					buf.append("^D^<EX^>" + Dir+"^</EX^>:^.^N ^d"+Say+"^.^N\r\n");
					buf.append(Say).append("\r\n");
			}
		}
/*		Item I=null;
		for(int i=0;i<room.numItems();i++)
		{
			I=room.getItem(i);
			if((I instanceof Exit)&&(((Exit)I).doorName().length()>0))
			{
				StringBuilder Say=((Exit)I).viewableText(mob, room);
				if(Say.length()>5)
					buf.append("^D^<MEX^>" + ((Exit)I).doorName()+"^</MEX^>:^.^N ^d"+Say+"^.^N\r\n");
				else
				if(Say.length()>0)
					buf.append("^D^<MEX^>" + CMStrings.padRight(((Exit)I).doorName(),5)+"^</MEX^>:^.^N ^d"+Say+"^.^N\r\n");
			}
		} */
		mob.tell(buf.toString());
	}

/*
	public void lookAtExitsShort(Room room, MOB mob)
	{
		if((mob==null)||(room==null)||(mob.isMonster())) return;

		StringBuilder buf=new StringBuilder("^D[Exits: ");
		for(int d=Directions.NUM_DIRECTIONS-1;d>=0;d--)
		{
			Exit exit=room.getExitInDir(d);
			if((exit!=null)&&(exit.viewableText(mob, room.getRoomInDir(d)).length()>0))
				buf.append("^<EX^>"+Directions.getDirectionName(d)+"^</EX^> ");
		}
		mob.tell(buf.toString().trim()+"]^.^N");
	}
	public void tickAging(MOB mob)
	{
		mob.setAgeHours(mob.getAgeHours()+1); // this is really minutes
		if((mob.baseCharStats().age()>0)
		&&(mob.playerStats()!=null)
		&&(mob.getBirthday()!=null)
		&&((mob.getAgeHours()%20)==0))
		{
			int tage=mob.baseCharStats().getMyRace().getAgingChart()[Race.AGE_YOUNGADULT]
					+CMLib.misc().globalClock().getYear()
					-mob.getBirthday()[2];
			int month=CMLib.misc().globalClock().getMonth();
			int day=CMLib.misc().globalClock().getDayOfMonth();
			int bday=mob.getBirthday()[0];
			int bmonth=mob.getBirthday()[1];
			while((tage>mob.baseCharStats().age())
			&&((month>bmonth)||((month==bmonth)&&(day>=bday))))
			{
				if(!CMSecurity.isAllowed(mob,"IMMORT"))
				{
					if((month==bmonth)&&(day==bday))
						mob.tell("Happy Birthday!");
					mob.baseCharStats().setAge(mob.baseCharStats().age()+1);
					mob.recoverCharStats();
					mob.recoverEnvStats();
				}
				else
				{
					mob.getBirthday()[2]++;
					tage--;
				}
			}
		}
	}
	public String examineItemString(MOB mob, Item item)
	{
		StringBuilder response=new StringBuilder("");
		String level=null;
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
		{
			int l=(int)Math.round(Math.floor(CMath.div(item.envStats().level(),10.0)));
			level=(l*10)+"-"+((l*10)+9);
		}
		else
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
		{
			int l=(int)Math.round(Math.floor(CMath.div(item.envStats().level(),5.0)));
			level=(l*5)+"-"+((l*5)+4);
		}
		else
			level=""+item.envStats().level();
		double divider=100.0;
		if(item.envStats().weight()<10)
			divider=4.0;
		else
		if(item.envStats().weight()<50)
			divider=10.0;
		else
		if(item.envStats().weight()<150)
			divider=20.0;
		String weight=null;
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
		{
			double l=Math.floor(CMath.div(item.envStats().weight(),divider));
			weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
		}
		else
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
		{
			divider=divider/2.0;
			double l=Math.floor(CMath.div(item.envStats().weight(),divider));
			weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
		}
		else
			weight=""+item.envStats().weight();
		if(item instanceof CagedAnimal)
		{
			MOB M=((CagedAnimal)item).unCageMe();
			if(M==null)
				response.append("\r\nLooks like some sort of lifeless thing.\r\n");
			else
			{
				if(M.envStats().height()>0)
					response.append("\r\n"+CMStrings.capitalizeFirstLetter(item.name())+" is "+M.envStats().height()+" inches tall and weighs "+weight+" pounds.\r\n");
				if((mob==null)||(!mob.isMonster()))
					response.append(CMProps.mxpImage(M," ALIGN=RIGHT H=70 W=70"));
				response.append(M.healthText(mob)+"\r\n\r\n");
				if(!M.description().equalsIgnoreCase(item.description()))
					response.append(M.description()+"\r\n\r\n");
			}
		}
		else
		{
			response.append("\r\n"+CMStrings.capitalizeFirstLetter(item.name())+" is a level "+level+" item, and weighs "+weight+" pounds.  ");
			if((item instanceof RawMaterial)
			&&(!CMLib.flags().isABonusItems(item))
			&&(item.rawSecretIdentity().length()>0)
			&&(item.baseEnvStats().weight()>1)
			&&((mob==null)||(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>3)))
				response.append("It appears to be a bundle of `"+item.rawSecretIdentity()+"`.  ");

			if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
				response.append("It is mostly made of a kind of "+RawMaterial.MATERIAL_NOUNDESCS[(item.material()&RawMaterial.MATERIAL_MASK)>>8].toLowerCase()+".  ");
			else
				response.append("It is mostly made of "+RawMaterial.CODES.NAME(item.material()).toLowerCase()+".  ");
			if((item instanceof Weapon)&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10)) {
				response.append("It is a ");
				if((item.rawLogicalAnd())&&CMath.bset(item.rawProperLocationBitmap(),Wearable.WORN_WIELD|Wearable.WORN_HELD))
					response.append("two handed ");
				else
					response.append("one handed ");
				response.append(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[((Weapon)item).weaponClassification()])+" class weapon that does "+CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[((Weapon)item).weaponType()])+" damage.  ");
			}
			else
			if((item instanceof Wearable)&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
			{
				if(item.envStats().height()>0)
					response.append(" It is a size "+item.envStats().height()+", and is ");
				else
					response.append(" It is your size, and is ");
				response.append(((item.rawProperLocationBitmap()==Wearable.WORN_HELD)||(item.rawProperLocationBitmap()==(Wearable.WORN_HELD|Wearable.WORN_WIELD)))
									 ?new StringBuilder("")
									 :new StringBuilder("worn on the "));
				Wearable.CODES codes = Wearable.CODES.instance();
				for(long wornCode : codes.all())
					if((wornCode != Wearable.IN_INVENTORY)
					&&(CMath.bset(item.rawProperLocationBitmap(),wornCode)))
					{
						String wornString=codes.name(wornCode);
						if(wornString.length()>0)
						{
							response.append(CMStrings.capitalizeAndLower(wornString)+" ");
							if(item.rawLogicalAnd())
								response.append("and ");
							else
								response.append("or ");
						}
					}
				if(response.toString().endsWith(" and "))
					response.delete(response.length()-5,response.length());
				else
				if(response.toString().endsWith(" or "))
					response.delete(response.length()-4,response.length());
				response.append(".  ");
			}
		}
		return response.toString();
	}
	protected void handleBeingItemLookedAt(CMMsg msg)
	{
		MOB mob=msg.source();
		Item item=(Item)msg.target();
		if(!CMLib.flags().canBeSeenBy(item,mob))
		{
			mob.tell("You can't see that!");
			return;
		}

		StringBuilder buf=new StringBuilder("");
		if(CMath.bset(mob.playerStats().getBitmap(),PlayerStats.ATT_SYSOPMSGS))
			buf.append(item.ID()+"\r\nRejuv :"+item.baseEnvStats().rejuv()
							+"\r\nType  :"+item.ID()
							+"\r\nUses  :"+item.usesRemaining()
							+"\r\nHeight:"+item.baseEnvStats().height()
							+"\r\nAbilty:"+item.baseEnvStats().ability()
							+"\r\nLevel :"+item.baseEnvStats().level()
							+"\r\nMisc  :'"+item.text());
		if(item.description().length()==0)
			buf.append("You don't see anything special about "+item.name());
		else
			buf.append(item.description());
		if((msg.targetMinor()==CMMsg.TYP_EXAMINE)&&(!item.ID().endsWith("Wallpaper")))
			buf.append(examineItemString(mob,item));
		if(item instanceof Container)
		{
			buf.append("\r\n");
			Container contitem=(Container)item;
			if((contitem.isOpen())
			&&((contitem.capacity()>0)
				||(contitem.getContents().size()>0)
				||((contitem instanceof Drink)&&(((Drink)contitem).liquidRemaining()>0))))
			{
				buf.append(item.name()+" contains:^<!ENTITY container \""+item.name()+"\"^>\r\n");
				Vector newItems=new Vector();
				if((item instanceof Drink)&&(((Drink)item).liquidRemaining()>0))
				{
					RawMaterial l=(RawMaterial)CMClass.getItem("GenLiquidResource");
					int myResource=((Drink)item).liquidType();
					l.setMaterial(myResource);
					((Drink)l).setLiquidType(myResource);
					l.setBaseValue(RawMaterial.CODES.VALUE(myResource));
					l.baseEnvStats().setWeight(1);
					String name=RawMaterial.CODES.NAME(myResource).toLowerCase();
					l.setName("some "+name);
					l.setDisplayText("some "+name+" sits here.");
					l.setDescription("");
					CMLib.materials().addEffectsToResource(l);
					l.recoverEnvStats();
					newItems.addElement(l);
				}

				if(item.owner() instanceof MOB)
				{
					MOB M=(MOB)item.owner();
					for(int i=0;i<M.numItems();i++)
					{
						Item item2=M.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CMItem","",false));
				}
				else
				if(item.owner() instanceof Room)
				{
					Room room=(Room)item.owner();
					if(room!=null)
					for(int i=0;i<room.numItems();i++)
					{
						Item item2=room.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CRItem","",false));
				}
			}
			else
			if((contitem.hasALid())&&((contitem.capacity()>0)||(contitem.getContents().size()>0)))
				buf.append(item.name()+" is closed.");
		}
		if(!msg.source().isMonster())
			buf.append(CMProps.mxpImage(item," ALIGN=RIGHT H=70 W=70"));
		mob.tell(buf.toString());
	}

	protected void handleBeingItemSniffed(CMMsg msg)
	{
		String s=null;
		Item item=(Item)msg.target();
		if(CMLib.flags().canSmell(msg.source()))
			s=RawMaterial.CODES.SMELL(item.material()).toLowerCase();
		if((s!=null)&&(s.length()>0))
			msg.source().tell(msg.source(),item,null,"^[T-NAME] has a "+s+" smell.");
	}
	public void handleIntroductions(MOB speaker, MOB me, String msg)
	{
		if((me.playerStats()!=null)
		&&(speaker!=me)
		&&(speaker.playerStats()!=null)
		&&(msg!=null)
		&&(!me.playerStats().isIntroducedTo(speaker))
		&&(CMLib.english().containsString(CMStrings.getSayFromMessage(msg),speaker.name())))
			me.playerStats().introduceTo(speaker.name());
	}
	protected void handleBeingRoomSniffed(CMMsg msg)
	{
		Room room=(Room)msg.target();
		StringBuilder smell=new StringBuilder("");
		switch(room.domainType())
		{
		case Room.DOMAIN_INDOORS_UNDERWATER:
		case Room.DOMAIN_INDOORS_WATERSURFACE:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			smell.append("It smells very WET here. ");
			break;
		case Room.DOMAIN_INDOORS_CAVE:
			smell.append("It smells very dank and mildewy here. ");
			break;
		case Room.DOMAIN_OUTDOORS_HILLS:
		case Room.DOMAIN_OUTDOORS_PLAINS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case TimeClock.SEASON_FALL:
			case TimeClock.SEASON_WINTER:
				smell.append("There is a faint grassy smell here. ");
				break;
			case TimeClock.SEASON_SPRING:
			case TimeClock.SEASON_SUMMER:
				smell.append("There is a floral grassy smell here. ");
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_WOODS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case TimeClock.SEASON_FALL:
			case TimeClock.SEASON_WINTER:
				smell.append("There is a faint woodsy smell here. ");
				break;
			case TimeClock.SEASON_SPRING:
			case TimeClock.SEASON_SUMMER:
				smell.append("There is a rich woodsy smell here. ");
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_JUNGLE:
			smell.append("There is a rich floral and plant aroma here. ");
			break;
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
		case Room.DOMAIN_OUTDOORS_ROCKS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case TimeClock.SEASON_FALL:
			case TimeClock.SEASON_WINTER:
			case TimeClock.SEASON_SUMMER:
				smell.append("It smells musty and rocky here. ");
				break;
			case TimeClock.SEASON_SPRING:
				smell.append("It smells musty, rocky, and a bit grassy here. ");
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_SWAMP:
			smell.append("It smells stinky and gassy here. ");
			break;
		}
		if(smell.length()>0)
			msg.source().tell(smell.toString());
	}
	public boolean postDrop(MOB mob, Environmental dropThis, boolean quiet, boolean optimized)
	{
		return forceStandardCommand(mob,"Drop",CMParms.makeVector(dropThis,Boolean.valueOf(quiet),Boolean.valueOf(optimized)));
	}
	public boolean postGet(MOB mob, Item container, Item getThis, boolean quiet)
	{
		if(container==null)
			return forceStandardCommand(mob,"Get",CMParms.makeVector(getThis,Boolean.valueOf(quiet)));
		return forceStandardCommand(mob,"Get",CMParms.makeVector(getThis,container,Boolean.valueOf(quiet)));
	}
	public boolean postRemove(MOB mob, Item item, boolean quiet)
	{
		if(quiet)
			return forceStandardCommand(mob,"Remove",CMParms.makeVector("REMOVE",item,"QUIETLY"));
		return forceStandardCommand(mob,"Remove",CMParms.makeVector("REMOVE",item));
	}
	public void postFlee(MOB mob, String whereTo)
	{
		forceStandardCommand(mob,"Flee",CMParms.makeVector("FLEE",whereTo));
	}
	public void postSheath(MOB mob, boolean ifPossible)
	{
		if(ifPossible)
			forceStandardCommand(mob,"Sheath",CMParms.makeVector("SHEATH","IFPOSSIBLE"));
		else
			forceStandardCommand(mob,"Sheath",CMParms.makeVector("SHEATH"));
	}
	public void postDraw(MOB mob, boolean doHold, boolean ifNecessary)
	{
		if(ifNecessary)
		{
			if(doHold)
				forceStandardCommand(mob,"Draw",CMParms.makeVector("DRAW","HELD","IFNECESSARY"));
			else
				forceStandardCommand(mob,"Draw",CMParms.makeVector("DRAW","IFNECESSARY"));
		}
		else
			forceStandardCommand(mob,"Draw",CMParms.makeVector("DRAW"));
	}
	public void postStand(MOB mob, boolean ifNecessary)
	{
		if(ifNecessary)
			forceStandardCommand(mob,"Stand",CMParms.makeVector("STAND","IFNECESSARY"));
		else
			forceStandardCommand(mob,"Stand",CMParms.makeVector("STAND"));
	}
	public void handleBeingSniffed(CMMsg msg)
	{
//		if(msg.target() instanceof Room)
//			handleBeingRoomSniffed(msg);
//		else
//		if(msg.target() instanceof Item)
//			handleBeingItemSniffed(msg);
//		else
//		if(msg.target() instanceof MOB)
//			handleBeingMobSniffed(msg);
	}
	public void handleBeingMobSniffed(CMMsg msg)
	{
	}

	public void handleSit(CMMsg msg)
	{
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB sittingmob=(MOB)I;
			int oldDisposition=sittingmob.getEnvObject().baseEnvStats().disposition();
			oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING-EnvStats.IS_LAYINGDOWN);
			sittingmob.getEnvObject().baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
			sittingmob.getEnvObject().recoverEnvStats();
			sittingmob.recoverCharStats();
			sittingmob.tell(sittingmob,msg.target(),msg.tool(),msg.sourceMessage());
		}
	}
	public void handleLayDown(CMMsg msg)
	{
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB layingdownmob=(MOB)I;
			int oldDisposition=layingdownmob.getEnvObject().baseEnvStats().disposition();
			oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING-EnvStats.IS_LAYINGDOWN);
			layingdownmob.getEnvObject().baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_LAYINGDOWN);
			layingdownmob.getEnvObject().recoverEnvStats();
			layingdownmob.recoverCharStats();
			layingdownmob.tell(layingdownmob,msg.target(),msg.tool(),msg.sourceMessage());
		}
	}

	public void handleSleep(CMMsg msg)
	{
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB sleepingmob=(MOB)I;
			int oldDisposition=sleepingmob.getEnvObject().baseEnvStats().disposition();
			oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING-EnvStats.IS_LAYINGDOWN);
			sleepingmob.getEnvObject().baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SLEEPING);
			sleepingmob.getEnvObject().recoverEnvStats();
			sleepingmob.recoverCharStats();
			sleepingmob.tell(sleepingmob,msg.target(),msg.tool(),msg.sourceMessage());
		}
	}
	public void handleStand(CMMsg msg)
	{
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB standingmob=(MOB)I;
			int oldDisposition=standingmob.getEnvObject().baseEnvStats().disposition();
			oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING-EnvStats.IS_LAYINGDOWN);
			standingmob.getEnvObject().baseEnvStats().setDisposition(oldDisposition);
			standingmob.getEnvObject().recoverEnvStats();
			standingmob.recoverCharStats();
			standingmob.tell(standingmob,msg.target(),msg.tool(),msg.sourceMessage());
		}
	}
	protected void handleBeingRoomLookedAt(CMMsg msg)
	{
		boolean longLook=msg.hasTargetCode(CMMsg.MsgCode.EXAMINE);
		Room room=(Room)msg.target();
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB mob=(MOB)I;
			if(mob.session()==null) continue; // no need for monsters to build all this data

			StringBuilder Say=new StringBuilder("");
			if(mob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS))
			{
				if(room.getArea()!=null)
					Say.append("Area  : "+room.getArea().name()+"\r\n");
				Say.append("SaveNum: "+room.saveNum()+"  ("+room.ID()+")\r\n");
			}
			Say.append(room.displayText()).append("\r\n").append(room.description()).append("\r\n\r\n");

			ArrayList<Item> viewItems=CMParms.toArrayList(room.getItemCollection().allItems());

			//NOTE: Will probably redo these tags sometimes.
			StringBuilder itemStr=CMLib.lister().lister(mob,viewItems,false,"RItem"," \"*\"",longLook);
			if(itemStr.length()>0)
				Say.append(itemStr);
	
			if(Say.length()==0)
				mob.tell("You can't see anything!");
			else
				mob.tell(Say.toString());
		}
	}
*/
}
