package com.planet_ink.coffee_mud.Commands;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")

public class Skills extends StdCommand
{
	public Skills(){access=new String[]{"SKILLS"};}

	public static char[] learnColors;
	public static String[] learnSymbols;
	static {
		learnColors=new char[3];
		learnColors[MOB.MOBSkill.LEARNING] = '2';
		learnColors[MOB.MOBSkill.MAINTAINING] = '3';
		learnColors[MOB.MOBSkill.FORGETTING] = '1';
		learnSymbols=new String[3];
		learnSymbols[MOB.MOBSkill.LEARNING] = "^^";
		learnSymbols[MOB.MOBSkill.MAINTAINING] = "*";
		learnSymbols[MOB.MOBSkill.FORGETTING] = "-";
	}

	protected Skill findSkill(MOB mob, Vector<String> commands)
	{
		String skillName = CMParms.combine(commands, 2);
		Skill S = CMClass.SKILL.getSkill(skillName);
		if(S==null)
		{
			mob.tell("There is no skill called " + skillName + "!");
		}
		return S;
	}
	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		end:
		if(commands.size()>1)
		{
			if(commands.get(1).equalsIgnoreCase("learn"))
			{
				Skill skill=findSkill(mob, commands);
				if(skill==null) break end;
				if(!mob.changeSkillState(skill, MOB.MOBSkill.LEARNING))
				{
					mob.tell("You're already learning that skill!");
				}
				else
				{
					mob.tell("You are now learning "+skill.playerFriendlyName()+".");
					return false;
				}
			}
			else if(commands.get(1).equalsIgnoreCase("maintain"))
			{
				Skill skill=findSkill(mob, commands);
				if(skill==null) break end;
				if(!mob.changeSkillState(skill, MOB.MOBSkill.MAINTAINING))
				{
					mob.tell("You're not practicing that skill!");
				}
				else
				{
					mob.tell("You will no longer practice nor forget "+skill.playerFriendlyName()+".");
					return false;
				}
			}
			else if(commands.get(1).equalsIgnoreCase("forget"))
			{
				Skill skill=findSkill(mob, commands);
				if(skill==null) break end;
				if(!mob.changeSkillState(skill, MOB.MOBSkill.FORGETTING))
				{
					mob.tell("You're already neglecting that skill!");
				}
				else
				{
					mob.tell("You start neglecting "+skill.playerFriendlyName()+".");
					return false;
				}
			}
			else if(commands.get(1).equalsIgnoreCase("qualify"))
			{
				MOB.MOBSkill hasAlready=null;
				StringBuilder tellSkills=new StringBuilder("You can start learning the following skills:\n");
				for(Iterator<Skill> iter = CMClass.SKILL.all();iter.hasNext();)
				{
					Skill next = iter.next();
					if((hasAlready=mob.getSkill(next))!=null && hasAlready.learningState==MOB.MOBSkill.LEARNING)
						continue;
					if(next.satisfiesPrerequisites(mob))
					{
						tellSkills.append(next.playerFriendlyName()).append("\n");
					}
				}
				mob.tell(tellSkills.toString());
			}
			else
			{
				mob.tell("Command not recognized. Make sure you spell the whole word accurately.");
			}
		} else {
			PlayerStats stats=mob.playerStats();
			if(stats == null) return false;
			
			StringBuilder tellSkills=new StringBuilder();
			Set<Skill> skills = mob.knownSkills();
			boolean colorCodes = stats.hasBits(PlayerStats.ATT_ANSI);
			if(colorCodes)
			{
				tellSkills.append("^2Learning ^3Maintaining ^1Forgetting^.\n");
			}	
			else
			{
				tellSkills.append("Learning:^^ Maintaining:* Forgetting:-\n");
			}
			tellSkills.append("You know the following skills:\n");
			for(Skill skill : skills)
			{
				MOB.MOBSkill instance = mob.getSkill(skill);
				//TODO: Formatting in columns or something?
				if(colorCodes)
					tellSkills.append('^').append(learnColors[instance.learningState]);
				tellSkills.append(skill.playerFriendlyName());
				tellSkills.append(": ");
				tellSkills.append(instance.level()).append('(').append(instance.EXP).append(" EXP)");
				if(!colorCodes)
					tellSkills.append(learnSymbols[instance.learningState]);
				else
					tellSkills.append("^.");
				tellSkills.append('\n');
			}
			mob.tell(tellSkills.toString());
		}
		mob.tell("Use \"skills (learn/maintain/forget) [skill]\" to adjust your exp settings, or \"skills qualify\" to see what skills you may learn.");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public boolean canBeOrdered(){return true;}
}
