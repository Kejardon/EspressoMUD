package com.planet_ink.coffee_mud.Effects.Languages;




import com.planet_ink.coffee_mud.Effects.StdEffect;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;


import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class StdLanguage extends StdEffect implements Language
{
	@Override public String ID() { return "StdLanguage"; }
	public String name(){ return "Languages";}
	@Override public String writtenName() { return name();}
	private static final String[] triggerStrings = {"SPEAK"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final Hashtable emptyHash=new Hashtable();
	private static final Vector emptyVector=new Vector();
	protected boolean spoken=false;
	protected int proficiency=1;
	private final static String consonants="bcdfghjklmnpqrstvwxz";
	private final static String vowels="aeiouy";
	@Override public boolean beingSpoken(String language){return spoken;}
	@Override public void setBeingSpoken(String language, boolean beingSpoken){spoken=beingSpoken;}
	@Override public Hashtable<String,String> translationHash(String language){ return emptyHash; }
	@Override public Vector<String[]> translationVector(String language){ return emptyVector; }
	
	@Override public Vector<String> languagesSupported() {return CMParms.makeVector(ID());}
	@Override public boolean translatesLanguage(String language) { return ID().equalsIgnoreCase(language);}
	@Override public int getProficiency(String language) { 
		if(ID().equalsIgnoreCase(language))
			return proficiency;
		return 0;
	}
	
	public String displayText()
	{
		if(beingSpoken(ID())) return "(Speaking "+name()+")";
		return "";
	}

	protected String fixCase(String like,String make)
	{
		StringBuilder s=new StringBuilder(make);
		char lastLike=' ';
		for(int x=0;x<make.length();x++)
		{
			if(x<like.length()) lastLike=like.charAt(x);
			s.setCharAt(x,fixCase(lastLike,make.charAt(x)));
		}
		return s.toString();
	}
	protected char fixCase(char like,char make)
	{
		if(Character.isUpperCase(like))
			return Character.toUpperCase(make);
		return Character.toLowerCase(make);
	}
	@Override public String translate(String language, String word)
	{
		if(translationHash(language).containsKey(word.toUpperCase()))
			return fixCase(word,(String)translationHash(language).get(word.toUpperCase()));
//Don't entiiiirely like this feature.
//		MOB M=CMLib.players().getPlayer(word);
//		if(M!=null) return word;
		if(translationVector(language).size()>0)
		{
			String[] choices=null;
			try{ choices=(String[])translationVector(language).elementAt(word.length()-1);}catch(Exception e){}
			if(choices==null) choices=(String[])translationVector(language).lastElement();
			return choices[CMLib.dice().roll(1,choices.length,-1)];
		}
		return word;
	}

	protected int numChars(String words)
	{
		int num=0;
		for(int i=0;i<words.length();i++)
		{
			if(Character.isLetter(words.charAt(i)))
				num++;
		}
		return num;
	}

	public String messChars(String language, String words, int numToMess)
	{
		numToMess=numToMess/2;
		if(numToMess==0) return words;
		StringBuilder w=new StringBuilder(words);
		while(numToMess>0)
		{
			int x=CMLib.dice().roll(1,words.length(),-1);
			char c=words.charAt(x);
			if(Character.isLetter(c))
			{
				if(vowels.indexOf(c)>=0)
					w.setCharAt(x,fixCase(c,vowels.charAt(CMLib.dice().roll(1,vowels.length(),-1))));
				else
					w.setCharAt(x,fixCase(c,consonants.charAt(CMLib.dice().roll(1,consonants.length(),-1))));
				numToMess--;
			}
		}
		return w.toString();
	}

	public String scrambleAll(String language, String str, int numToMess)
	{
		StringBuilder newStr=new StringBuilder("");
		int start=0;
		int end=0;
		int state=-1;
		while(start<=str.length())
		{
			char c;
			if(end>=str.length())
				c=' ';
			else
				c=str.charAt(end);
			switch(state)
			{
			case -1:
				if(Character.isLetter(c))
				{ state=0; end++;}
				else
				{ newStr.append(c); end++;start=end;}
				break;
			case 0:
				if(Character.isLetter(c))
				{ end++;}
				else
				if(Character.isDigit(c))
				{ newStr.append(str.substring(start,end+1)); end++; start=end; state=1; }
				else
				{ newStr.append(translate(language,str.substring(start,end))).append(c); end++; start=end; state=-1; }
				break;
			case 1:
				if(Character.isLetterOrDigit(c))
				{ newStr.append(c); end++; start=end;}
				else
				{ newStr.append(c); end++; start=end; state=-1; }
				break;
			}
		}
		return newStr.toString();
	}

	protected Language getMyTranslator(String id, MOB E) 
	{
		if(E==null) return null;
		Language winner=null;
		Effect A;
		for(int a=0;a<E.numEffects();a++) 
		{
			A=E.fetchEffect(a);
			if((A instanceof Language) 
			&& ((Language)A).translatesLanguage(id)
			&& ((winner==null)
					||((Language)A).getProficiency(id) > winner.getProficiency(id)))
				winner = (Language)A;
		}
		return winner;
	}
	
	protected boolean processSourceMessage(CMMsg msg, String str, int numToMess)
	{
		String smsg=CMStrings.getSayFromMessage(msg.sourceMessage());
		if(numToMess>0) smsg=messChars(ID(),smsg,numToMess);
		msg.addTool(this);
		msg.setSourceMessage(CMStrings.substituteSayInMessage(msg.sourceMessage(),smsg));
		return true;
	}
	
	protected boolean processNonSourceMessages(CMMsg msg, String str, int numToMess)
	{
		str=scrambleAll(ID(),str,numToMess);
		msg.addTool(this);
		msg.setTargetMessage(CMStrings.substituteSayInMessage(msg.targetMessage(),str));
		msg.setOthersMessage(CMStrings.substituteSayInMessage(msg.othersMessage(),str));
		return true;
	}

	@Override public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if((affected instanceof MOB)&&(beingSpoken(ID())))
			msg.addResponse(this, -2);	//Translated to the language before even being spoken
		return super.okMessage(myHost,msg);
	}
	@Override public boolean respondTo(CMMsg msg)
	{
		//hm. What if multiple sources are speaking different languages?
		if((msg.isSource((MOB)affected))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&(msg.hasSourceCode(CMMsg.MsgCode.SPEAK, CMMsg.MsgCode.CHANNEL)))
		{
			String str=CMStrings.getSayFromMessage(msg.othersMessage());
			if(str==null) str=CMStrings.getSayFromMessage(msg.targetMessage());
			if(str!=null)
			{
				int numToMess=numChars(str)*(100-getProficiency(ID()))/100;
				if(!processSourceMessage(msg, str, numToMess))
					return false;
				if(!processNonSourceMessages(msg,str,numToMess))
					return false;
			}
		}
		else
		if((msg.target()==affected)&&(!msg.isSource((Interactable)affected)))
		{
			for(Interactable I : msg.sourceArr())
			{
				if(!(I instanceof MOB)) continue;
				MOB source = (MOB)I;
				if((msg.hasTargetCode(CMMsg.MsgCode.SPEAK, CMMsg.MsgCode.ORDER))
					&&((!CMSecurity.isAllowed(source,"ORDER"))
					&&((!CMSecurity.isAllowed(source,"CMDMOBS"))||(!((MOB)affected).isMonster()))
					&&((!CMSecurity.isAllowed(source,"CMDROOMS"))||(!((MOB)affected).isMonster()))))
				{
					Language L=getMyTranslator(ID(),source);
					if((L==null)||(!L.beingSpoken(ID()))||((CMLib.dice().rollPercentage()*2)>(L.getProficiency(ID())+getProficiency(ID()))))
					{
//						msg.setTargetCode(CMMsg.TYP_SPEAK);
//						msg.setSourceCode(CMMsg.TYP_SPEAK);
//						msg.setOthersCode(CMMsg.TYP_SPEAK);
						String reply;
						if((L==null)||(!L.beingSpoken(ID())))
							reply="^[S-NAME] ^[S-IS-ARE] speaking "+name()+" and do^e not appear to understand ^[T-YOUPOSS] words.";
						else
							reply="^[S-NAME] ^[S-IS-ARE] having trouble understanding ^[T-YOUPOSS] pronunciation.";
						msg.addTrailerMsg(source.location(), CMClass.getMsg((MOB)affected,source,(Vector)null,EnumSet.of(CMMsg.MsgCode.VISUAL),reply));
					}
					break;
				}
			}
		}
		return true;
	}
/* TODO: Need some equivalent.
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!auto)
		{
			for(int a=0;a<mob.numEffects();a++)
			{
				Effect A=mob.fetchEffect(a);
				if((A!=null)&&(A instanceof Language))
				{
					if(mob.isMonster())
						A.setProficiency(100);
					if(A.ID().equals(ID()))
						((Language)A).setBeingSpoken(ID(),true);
					else
						((Language)A).setBeingSpoken(ID(),false);
				}
			}
			isAnAutoEffect=false;
			mob.tell("You are now speaking "+name()+".");
		}
		else
			setBeingSpoken(ID(),true);
		return true;
	}
*/
	protected boolean translateOthersMessage(CMMsg msg, String sourceWords)
	{
		if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf("'")>0))
		{
			String otherMes=msg.othersMessage();
			//TODO: This looks wrong. It should probably be handled differently, whatever it's doing.
			//For starters, this is passing a null session to fullOutFilter, which I do not want
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.firstSource(),msg.target(),msg.firstTool(),otherMes,false);
			msg.addTrailerMsg(((MOB)affected).location(), CMClass.getMsg(msg.source(),(Interactable)affected,(Vector)null,EnumSet.noneOf(CMMsg.MsgCode.class),null,msg.othersCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}
	
	protected boolean translateTargetMessage(CMMsg msg, String sourceWords)
	{
		if((affected instanceof Interactable)&&(msg.isTarget((Interactable)affected))&&(msg.targetMessage()!=null))
		{
			String otherMes=msg.targetMessage();
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.firstSource(),msg.target(),msg.firstTool(),otherMes,false);
			msg.addTrailerMsg(((MOB)affected).location(), CMClass.getMsg(msg.source(),(Interactable)affected,(Vector)null,CMMsg.NO_EFFECT,null,msg.targetCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	protected boolean translateChannelMessage(CMMsg msg, String sourceWords)
	{
		if(msg.hasSourceCode(CMMsg.MsgCode.CHANNEL))
		{
			msg.addTrailerMsg(((MOB)affected).location(), CMClass.getMsg(msg.source(),null,(Vector)null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,msg.othersCode(),CMStrings.substituteSayInMessage(msg.othersMessage(),sourceWords)+" (translated from "+name()+")"));
			return true;
		}
		return false;
	}
	
	@Override public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected instanceof MOB)
		&&(!msg.isSource((MOB)affected))
		&&(msg.hasSourceCode(CMMsg.MsgCode.SPEAK, CMMsg.MsgCode.CHANNEL))
		&&(msg.tool() !=null)
		&&(msg.sourceMessage()!=null))
		{
			for(CMObject O : msg.toolArr())
			{
				if((O instanceof Language)&&(O.ID().equals(ID())))
				{
					String str=CMStrings.getSayFromMessage(msg.sourceMessage());
					if(str!=null)
					{
						int numToMess=numChars(str)*(100-getProficiency(ID()))/100;
						if(numToMess>0)
							str=messChars(ID(),str,numToMess);
						if(!translateChannelMessage(msg,str))
							if(!translateTargetMessage(msg,str))
								translateOthersMessage(msg, str);
					}
					break;
				}
			}
		}
	}
}
