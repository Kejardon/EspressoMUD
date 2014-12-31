package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.core.exceptions.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import java.util.regex.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class CMGenEditor extends StdLibrary
{
	public String ID(){return "CMGenEditor";}

	public <T extends CMModifiable> T genMiscSet(MOB mob, T E)
	{
		if(E==null)
			return null;
		StringBuilder promptText = new StringBuilder();
		if(E instanceof CMSavable)
			promptText.append(((CMSavable)E).saveNum()).append('\n');
		if(E instanceof Ownable)
		{
			if(((Ownable)E).owner()==null)
				promptText.append("Null owner!!\n");
			else
				promptText.append(((Ownable)E).owner().ID()).append(": ").append(((Ownable)E).owner().saveNum()).append('\n');
		}
		if(E instanceof Item)
		{
			CMObject cont=((Item)E).container();
			if(cont==null)
				promptText.append("null");
			else
			{
				if(cont instanceof CMSavable)
					promptText.append(cont.ID()).append(": ").append(((CMSavable)cont).saveNum()).append('\n');
				else
					promptText.append(cont.ID()).append('\n');
			}
		}
		CMModifiable.ModEnum[] options=E.totalEnumM();
		String promptHeader=promptText.toString();
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			promptText.setLength(0);
			promptText.append(promptHeader);
			for(int i=0;i<options.length;i++)
				promptText.append(1+i).append(". ").append(options[i]).append(": '").append(options[i].brief(E)).append("'.\n");
			promptText.append("Edit which? ");
			int pickOption=CMath.s_int(mob.session().prompt(promptText.toString(),""));
			if(--pickOption<0) break;
			else if(pickOption<options.length)
			{
				mob.session().rawPrintln((1+pickOption)+". "+options[pickOption]+": '"+options[pickOption].prompt(E)+"'.");
				options[pickOption].mod(E, mob);
			}
		}
		if(E instanceof CMSavable) ((CMSavable)E).saveThis();
		return E;
	}
	
	public void ashortPrompt(MOB mob, short[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=shortPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public short shortPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new short: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isInteger(newName))
			return CMath.s_short(newName);
		return CMath.s_short(defaultTo);
	}
	public void aintPrompt(MOB mob, int[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=intPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public int intPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new integer: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isInteger(newName))
			return CMath.s_int(newName);
		return CMath.s_int(defaultTo);
	}
	public void alongPrompt(MOB mob, long[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=longPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public long longPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new long: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isLong(newName))
			return CMath.s_long(newName);
		return CMath.s_long(defaultTo);
	}
	public void adoublePrompt(MOB mob, double[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=doublePrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public double doublePrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new double: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isDouble(newName))
			return CMath.s_double(newName);
		return CMath.s_double(defaultTo);
	}
	public void abooleanPrompt(MOB mob, boolean[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=booleanPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public boolean booleanPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new boolean: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isBool(newName))
			return CMath.s_bool(newName);
		return CMath.s_bool(defaultTo);
	}
	public void astringPrompt(MOB mob, String[] values, boolean allowNull)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=stringPrompt(mob, values[pickOption], allowNull);
		}
	}
	public String stringPrompt(MOB mob, String defaultTo, boolean allowNull)
	{
		String newString=mob.session().prompt("Enter a new string: ",((defaultTo==null)?"":defaultTo));
		if("NULL".equalsIgnoreCase(newString))
		{
			if(allowNull) return null;
			else return "";
		}
		return newString;
	}
	//IMPORTANT: ALL CALLS TO THIS MUST HAVE UPPERCASE ENUM OPTIONS.
	public <T extends Enum> T enumPrompt(MOB M, String defaultTo, T[] options)
	{
		M.session().rawPrintln("Options: ");
		for(T E : options)
			M.session().rawPrint(E.toString()+" ");
		
		String newString=M.session().prompt("Enter a new value: ",((defaultTo==null)?"":defaultTo));
		T E=CMClass.valueOf((Class<T>)options[0].getClass(), newString);
		if((E==null)&&(defaultTo!=null))
			E=CMClass.valueOf((Class<T>)options[0].getClass(), defaultTo);
		return E;
	}
	public void acharPrompt(MOB mob, char[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=charPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public char charPrompt(MOB mob, String defaultTo)
	{
		return mob.session().prompt("Enter a new character: ",((defaultTo==null)?" ":defaultTo)).charAt(0);
	}

	public int promptVector(MOB mob, List V, boolean newOption)
	{
		int i=0;
		for(;i<V.size();i++)
		{
			Object O=V.get(i);
			boolean str=(O instanceof String);
			boolean inter=(O instanceof Interactable);
			mob.session().rawPrintln((1+i)+". "+(str?(String)O:(((CMObject)O).ID()+(inter?" "+((Interactable)O).name():""))));
		}
		if(newOption)
			mob.session().rawPrintln((1+i)+". New Element");
		return CMath.s_int(mob.session().prompt("Edit which? ",""));
	}
	public int promptVector(MOB mob, Object[] V, boolean newOption)
	{
		int i=0;
		for(;i<V.length;i++)
		{
			Object O=V[i];
			boolean str=(O instanceof String);
			boolean inter=(O instanceof Interactable);
			mob.session().rawPrintln((1+i)+". "+(str?(String)O:(((CMObject)O).ID()+(inter?" "+((Interactable)O).name():""))));
		}
		if(newOption)
			mob.session().rawPrintln((1+i)+". New Element");
		return CMath.s_int(mob.session().prompt("Edit which? ",""));
	}
	public int promptWVector(MOB mob, WVector V, boolean newOption)
	{
		int i=0;
		for(;i<V.size();i++)
		{
			Object O=V.get(i);
			boolean str=(O instanceof String);
			boolean inter=(O instanceof Interactable);
			mob.session().rawPrintln((1+i)+". ("+V.weight(i)+") "+(str?(((CMObject)O).ID()+(inter?" "+((Interactable)O).name():"")):(String)O));
		}
		if(newOption)
			mob.session().rawPrintln((1+i)+". New Element");
		return CMath.s_int(mob.session().prompt("Edit which? ",""));
	}
	public Effect newAnyEffect(MOB mob){return newAnyEffect(mob, new Vector());}
	public Effect newAnyEffect(MOB mob, Vector notThese)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String behave=mob.session().prompt("Enter an effect to add (?)\r\n:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.EFFECT.all()).toString());
				else
				{
					Effect chosenOne=null;
					chosenOne=CMClass.EFFECT.getNew(behave);
					if(chosenOne!=null)
					{
						boolean alreadyHasIt=false;
						behave=chosenOne.ID();
						for(int i=0;i<notThese.size();i++)
							if(((Effect)notThese.get(i)).ID()==behave)
								{alreadyHasIt=true; break;}
						if(!alreadyHasIt)
						{
							mob.tell("Adding "+chosenOne.ID());
							return chosenOne;
						}
						else
							mob.tell("That effect is already on the list");
					}
					else
					{
						mob.tell("'"+behave+"' is not recognized.  Try '?'.");
					}
				}
			}
			else
				return null;
		}
		return null;
	}
	public <T extends CMObject> T newObjectOfType(
			MOB M,
			CMClass.Objects group,
			Class<T> thisInterface,
			boolean newInstance,
			String type)
	{
		if(type==null)
			type=(thisInterface!=null?thisInterface.getCanonicalName():group.name);
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			String objectName=M.session().prompt("Enter a "+type+" type: ","");
			if(objectName.equals("")) return null;
			if(objectName.equals("?"))
			{
				for(Iterator<CMObject> e=group.all();e.hasNext();)
				{
					CMObject next=e.next();
					if(thisInterface==null||thisInterface.isInstance(next))
						M.session().rawPrintln(next.ID());
				}
				continue;
			}
			CMObject S=newInstance?group.getNew(objectName):group.get(objectName);
			if(S==null || (thisInterface!=null && !thisInterface.isInstance(S)))
			{
				M.session().rawPrintln("No "+type+" with that name found.");
				continue;
			}
			return (T)S;
		}
		return null;
	}
	public Behavior newAnyBehavior(MOB mob){return newAnyBehavior(mob, new Vector());}
	public Behavior newAnyBehavior(MOB mob, Vector notThese)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String behave=mob.session().prompt("Enter a behavior to add (?)\r\n:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.BEHAVIOR.all()).toString());
				else
				{
					Behavior chosenOne=null;
					chosenOne=CMClass.BEHAVIOR.getNew(behave);
					if(chosenOne!=null)
					{
						boolean alreadyHasIt=false;
						behave=chosenOne.ID();
						for(int i=0;i<notThese.size();i++)
							if(((Effect)notThese.get(i)).ID()==behave)
								{alreadyHasIt=true; break;}
						if(!alreadyHasIt)
						{
							mob.tell("Adding "+chosenOne.ID());
							return chosenOne;
						}
						else
							mob.tell("That behavior is already on the list");
					}
					else
					{
						mob.tell("'"+behave+"' is not recognized.  Try '?'.");
					}
				}
			}
			else
				return null;
		}
		return null;
	}
	public Item getOrMakeItem(MOB mob)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			int SID;
			String input=mob.session().prompt("Enter an item SID, nearby item's name, or 'new' to make a new item:\r\n","");
			if(input.length()==0) break;
			if(input.equalsIgnoreCase("new"))
			{
				Item item=(Item)genMiscSet(mob, newAnyItem(mob));
				if(item!=null) return item;
			}
			else if((SID=CMath.s_int(input))!=0)
			{
				Item item=SIDLib.ITEM.get(SID);
				if(item!=null) return item;
			}
			else
			{
				Vector<Interactable> items=CMLib.english().getTargets(mob, input, null, EnglishParser.SRCH_ALL, EnglishParser.SUB_ALL);
				if(items==null)
				{
					mob.tell("No items found of that name.");
					continue;
				}
				for(int i=items.size()-1;i>=0;i--)
					if(!(items.get(i) instanceof Item))
						items.remove(i);
				Item item=null;
				if(items.size()==0)
					mob.tell("No items found of that name.");
				else if(items.size()==1)
					item=(Item)items.get(0);
				else
				{
					mob.tell("Multiple items found, pick one:\r\n0: None.");
					for(int i=0;i<items.size();i++)
						mob.tell((i+1)+": "+items.get(i).name() + "(found in "+((Item)items.get(i)).container()+")");
					int i = CMath.s_int(mob.session().prompt("(0 - "+(items.size()+1)+")","0"))-1;
					if(i>=0 && i<items.size())
						item=(Item)items.get(i);
				}
				if(item!=null) return item;
			}
		}
		return null;
	}
	public Item newAnyItem(MOB mob)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String behave=mob.session().prompt("Enter an item type (? for options)\r\n:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
				{
//					mob.tell(CMLib.lister().reallyList(CMClass.WEARABLE.all()).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.WEAPON.all()).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.ITEM.all()).toString());
				}
				else
				{
					Item chosenOne=null;
					chosenOne=CMClass.ITEM.getNew(behave);
					if(chosenOne==null) chosenOne=CMClass.WEAPON.getNew(behave);
					if(chosenOne!=null)
					{
						mob.tell("Adding "+chosenOne.ID());
						return (Item)chosenOne.copyOf();
					}
					else
					{
						mob.tell("'"+behave+"' is not recognized.  Try '?'.");
					}
				}
			}
			else
				return null;
		}
		return null;
	}
	public Bind newAnyBind(MOB mob)
	{
		return new com.planet_ink.coffee_mud.Common.DefaultBind();
	}
	public Exit newAnyExit(MOB mob)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String exit=mob.session().prompt("Enter an exit ID (?)\r\n:","");
			if(exit.length()>0)
			{
				if(exit.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.EXIT.all()).toString());
				else
				{
					Exit chosenOne=null;
					chosenOne=CMClass.EXIT.getNew(exit);
					if(chosenOne!=null)
					{
						mob.tell("Adding "+chosenOne.ID());
						return chosenOne;
					}
					else
						mob.tell("'"+exit+"' is not recognized.  Try '?'.");
				}
			}
			else
				return null;
		}
		return null;
	}
	public Closeable newAnyCloseable(MOB mob)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String exit=mob.session().prompt("Enter a closeable ID (?)\r\n:","");
			if(exit.length()>0)
			{
				if(exit.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.CLOSEABLE.all()).toString());
				else
				{
					Closeable chosenOne=null;
					chosenOne=CMClass.CLOSEABLE.getNew(exit);
					if(chosenOne!=null)
					{
						mob.tell("Adding "+chosenOne.ID());
						return chosenOne;
					}
					else
						mob.tell("'"+exit+"' is not recognized.  Try '?'.");
				}
			}
			else
				return null;
		}
		return null;
	}

	public void modExits(CopyOnWriteArrayList<ExitInstance> exits, Room source, MOB M)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			Vector<ExitInstance> V=new Vector(exits);
			int i=0;
			for(;i<V.size();i++)
			{
				Exit e=V.get(i).getExit();
				M.session().rawPrintln((1+i)+". "+e.ID()+" "+e.saveNum()+" to "+V.get(i).getDestination().saveNum());
			}
			M.session().rawPrintln((1+i)+". New Element");
			i=CMath.s_int(M.session().prompt("Edit which? ",""));
			if(--i<0) break;
			else if(i==V.size())
			{
				Exit e=newAnyExit(M);
				if(e!=null)
				{
					CMLib.genEd().genMiscSet(M, e);
					Room R=null;
					while(R==null)
					{
						String roomID=M.session().prompt("Connect this exit to what room? ","");
						if(roomID.equals("")) break;
						R=SIDLib.ROOM.get(CMath.s_int(roomID));
					}
					if(R!=null)
						exits.add(e.makeInstance(source, R));
				}
			}
			/*
			else if(i<V.size())
			{
				char action=M.session().prompt("Edit (E)xit, target (R)oom, or (D)estroy link?"," ").trim().toUpperCase().charAt(0);
				if(action=='D') { exits.remove(V.get(i)); }
				else if(action=='E') CMLib.genEd().genMiscSet(M, V.get(i).exit);
				else if(action=='R')
				{
					Room R=null;
					while(R==null)
					{
						String roomID=M.session().prompt("Connect this exit to what room? ","");
						if(roomID.equals("")) break;
						R=SIDLib.ROOM.get(CMath.s_int(roomID));
					}
					if(R!=null)
					{
						Room.REMap old=V.get(i);
						if(exits.remove(old))
							exits.add(new Room.REMap(R, old.exit));
						else
							M.tell("Whoops, that exit disappeared when I wasn't looking!");
					}
				}
			}
			*/
		}
	}
	public void modAffectable(Affectable E, MOB M)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			Vector<Effect> V=CMParms.denumerate(E.allEffects());
			int i=CMLib.genEd().promptVector(M, V, true);
			if(--i<0) break;
			else if(i==V.size())
			{
				Effect I=CMLib.genEd().newAnyEffect(M);
				if(I!=null)
					E.addEffect((Effect)CMLib.genEd().genMiscSet(M, I));
			}
			else if(i<V.size())
			{
				char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).ID()+" (default M)? ","M").trim().toUpperCase().charAt(0);
				if(action=='D') E.delEffect(V.get(i));
				else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i));
			}
		}
	}
	public void modBehavable(Behavable E, MOB M)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			Vector<Behavior> V=CMParms.denumerate(E.allBehaviors());
			int i=CMLib.genEd().promptVector(M, V, true);
			if(--i<0) break;
			else if(i==V.size())
			{
				Behavior I=CMLib.genEd().newAnyBehavior(M);
				if(I!=null)
					E.addBehavior((Behavior)CMLib.genEd().genMiscSet(M, I));
			}
			else if(i<V.size())
			{
				char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).ID()+" (default M)? ","M").trim().toUpperCase().charAt(0);
				if(action=='D') E.delBehavior(V.get(i));
				else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i));
			}
		}
	}
	//IMPORTANT: ALL CALLS TO THIS MUST HAVE UPPERCASE ENUM OPTIONS.
	public void enumSetPrompt(MOB M, Enum[] options, EnumSet set)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			StringBuilder S=new StringBuilder("Options: ");
			for(Enum E : options)
				S.append(E.toString());
			M.session().rawPrintln(S.toString());
			S=new StringBuilder("Current: ");
			for(Enum E : (Enum[])set.toArray(CMClass.dummyEnumArray))
				S.append(E.toString());
			M.session().rawPrintln(S.toString());
			String newString=M.session().prompt("Enter an option to toggle: ","");
			if(newString.equals("")) break;
			Enum E=CMClass.valueOf(options[0].getClass(), newString);
			if(E!=null)
				if(!set.remove(E))
					set.add(E);
		}
	}
	public Area areaPrompt(MOB M)
	{
		Area A=null;
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			String areaName=M.session().prompt("Enter an area name: ","");
			if(areaName.equals("")) return null;
			if(areaName.equals("?"))
			{
				for(Iterator<Area> e=CMLib.map().areas();e.hasNext();)
					M.session().rawPrintln(e.next().name());
				
				continue;
			}
			A=CMLib.map().getArea(areaName);
			if(A==null)
			{
				A=CMLib.map().findAreaStartsWith(areaName);
				if(A==null)
				{
					M.session().rawPrintln("No area with that name found.");
					continue;
				}
				if(!M.session().prompt("Do you mean "+A.name()+"? (Y/n): ","Y").toUpperCase().startsWith("Y")) continue;
			}
			return A;
		}
		return null;
	}
	public Race racePrompt(MOB M)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			String raceName=M.session().prompt("Enter a race name: ","");
			if(raceName.equals("")) return null;
			if(raceName.equals("?"))
			{
				for(Iterator<Race> e=CMClass.RACE.all();e.hasNext();)
					M.session().rawPrintln(e.next().ID());
				
				continue;
			}
			Race R=CMClass.RACE.get(raceName);
			if(R==null)
			{
				M.session().rawPrintln("No race with that name found.");
				continue;
			}
			return R;
		}
		return null;
	}
	public Skill skillPrompt(MOB M)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			String skillName=M.session().prompt("Enter a skill name: ","");
			if(skillName.equals("")) return null;
			if(skillName.equals("?"))
			{
				for(Iterator<Skill> e=CMClass.SKILL.all();e.hasNext();)
					M.session().rawPrintln(e.next().ID());
				
				continue;
			}
			Skill S=CMClass.SKILL.get(skillName);
			if(S==null)
			{
				M.session().rawPrintln("No skill with that name found.");
				continue;
			}
			return S;
		}
		return null;
	}
	public EnvMap mapPrompt(MOB M)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			String skillName=M.session().prompt("Enter an EnvMap type: ","");
			if(skillName.equals("")) return null;
			if(skillName.equals("?"))
			{
				for(Iterator<CMCommon> e=CMClass.COMMON.all();e.hasNext();)
				{
					CMCommon next=e.next();
					if(next instanceof EnvMap)
						M.session().rawPrintln(next.ID());
				}
				continue;
			}
			CMCommon S=CMClass.COMMON.getNew(skillName);
			if(!(S instanceof EnvMap))
			{
				M.session().rawPrintln("No EnvMap with that name found.");
				continue;
			}
			return (EnvMap)S;
		}
		return null;
	}
}