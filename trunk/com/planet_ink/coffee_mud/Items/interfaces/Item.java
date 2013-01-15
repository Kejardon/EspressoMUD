package com.planet_ink.coffee_mud.Items.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * The interface for all common items, and as a base for RawMaterial, armor, weapons, etc.
 * @author Bo Zimmerman
 */
public interface Item extends Interactable, CMSavable, CMModifiable
{
	public static final Item[] dummyItemArray=new Item[0];
	public boolean damagable();
	public void setDamagable(boolean bool);
	public int wornOut();	//Start at 0 and go up to 10000. At 10000 destroy, at say 2000 or so it shouldn't really be usable anyways
	public void setWornOut(int worn);
	public int value();
	public int baseGoldValue();
	public void setBaseValue(int newValue);
	public int recursiveWeight();
	public String stackableName();
	public void setStackableName(String S);
	
	public boolean isComposite();
	public BindCollection subItems();
	
	//public void setMiscText(String newMiscText);
	//public String text();
	public CMObject container();
	public void setContainer(CMObject E);
	//public int ridesNumber();
	public CMObject ride();
	public void setRide(CMObject R);
	public static class O
	{
		public static void handleBeingLookedAt(Item item, CMMsg msg, boolean longLook)
		{
			for(Interactable I : msg.sourceArr())
			{
				if(!(I instanceof MOB)) continue;
				MOB mob=(MOB)I;
				if(mob.session()==null||mob.playerStats()==null) continue; // no need for monsters to build all this data
	
				StringBuilder buf=new StringBuilder("");
				if(mob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS))
				{
					buf.append("\r\nType  :").append(item.ID());
					buf.append("\r\nSID   :").append(item.saveNum());
				}
				if(item.description().length()==0)
					buf.append("\r\nYou don't see anything special about "+item.name());
				else
					buf.append("\r\n").append(item.description());
				/*
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
				*/
		
				if(longLook)
				{
					if(mob.isMine(item))
					{
						int realWeight=item.getEnvObject().envStats().weight();
						int divider;
						if(realWeight<=1500)
							divider=4;
						else divider=realWeight/250+CMath.random(5)-3;
			
						int l=realWeight/divider;
						int result=l*divider+l/2;
						//String weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
			
						buf.append("\r\n")
							.append(CMStrings.capitalizeFirstLetter(item.name()))
							.append(" weighs about ")
							.append(result/1000.0)
							.append(" stones. ");
					}
					/*
					if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
						buf.append("It is mostly made of a kind of ").append(RawMaterial.MATERIAL_NOUNDESCS[(item.material()&RawMaterial.MATERIAL_MASK)>>8].toLowerCase()).append(". ");
					else
						buf.append("It is mostly made of ").append(RawMaterial.CODES.NAME(item.material()).toLowerCase()).append(". ");
					if((item instanceof Weapon)&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10)) {
						buf.append("It is a ");
							.append(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[((Weapon)item).weaponClassification()]))
							.append(" class weapon that does ")
							.append(CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[((Weapon)item).weaponType()]))
							.append(" damage. ");
					}
					else
					if((item instanceof Wearable)&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
					{
						if(item.envStats().height()>0)
							buf.append(" It is a size "+item.envStats().height()+", and is ");
						else
							buf.append(" It is your size, and is ");
						buf.append(((item.rawProperLocationBitmap()==Wearable.WORN_HELD)||(item.rawProperLocationBitmap()==(Wearable.WORN_HELD|Wearable.WORN_WIELD)))
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
									buf.append(CMStrings.capitalizeAndLower(wornString)+" ");
									if(item.rawLogicalAnd())
										buf.append("and ");
									else
										buf.append("or ");
								}
							}
						if(buf.toString().endsWith(" and "))
							buf.delete(buf.length()-5,buf.length());
						else
						if(buf.toString().endsWith(" or "))
							buf.delete(buf.length()-4,buf.length());
						buf.append(".  ");
					}
					*/
				}

				mob.tell(buf.toString());
			}
		}
	}
}
