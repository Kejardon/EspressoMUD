package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.Libraries.CoffeeMaker;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.CMModifiable.ModEnum;
import com.planet_ink.coffee_mud.core.interfaces.CMSavable.SaveEnum;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Kevin Swanson
 */
public abstract class AbstractSaveInteractable extends AbstractInteractable implements CMSavable, CMModifiable
{
	protected int saveNum;
	protected boolean destroyed=false;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;

	abstract protected SIDLib.Objects SID();
	
	public AbstractSaveInteractable(){}
	protected AbstractSaveInteractable(AbstractSaveInteractable clone)
	{
		super(clone);
		
	}
	
	@Override public boolean needLink(){return true;}
	@Override public void link()
	{
		if(effectsToLoad!=null)
		{
			for(int SID : effectsToLoad)
			{
				Effect to = SIDLib.EFFECT.get(SID);
				if(to==null) continue;
				affects.add(to);
				to.setAffectedOne(this);
			}
			effectsToLoad=null;
		}
		if(behavesToLoad!=null)
		{
			for(int SID : behavesToLoad)
			{
				Behavior to = SIDLib.BEHAVIOR.get(SID);
				if(to==null) continue;
				to.startBehavior(this);
				behaviors.add(to);
			}
			behavesToLoad=null;
		}
	}
	@Override public void saveThis(){CMLib.database().saveObject(this);}
	@Override public void prepDefault(){} //TODO: Should this initialize Env?

	@Override public void destroy()
	{
		clearAllListeners();
		if(myEnvironmental!=null)
			myEnvironmental.destroy();

		destroyed=true;

		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		for(Behavior B : behaviors)
			B.startBehavior(null);
		behaviors.clear();
		ItemCollection inv=ItemCollection.O.getFrom(this);
		if(inv!=null)
		{
			for(Iterator<Item> iter=inv.allItems();iter.hasNext();)
				iter.next().destroy();
		}
		if(saveNum!=0)	//NOTE: I think this should be a standard destroy() check?
			CMLib.database().deleteObject(this);
	}
	@Override public boolean amDestroyed(){return destroyed;}
	
	@Override public int saveNum()
	{
		if((saveNum==0)&&(!destroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SID().getNumber(this);
		}
		return saveNum;
	}
	@Override public void setSaveNum(int num)
	{
		synchronized(this)
		{
			SIDLib.Objects SID = SID();
			if(saveNum!=0)
				SID.removeNumber(saveNum);
			saveNum=num;
			SID.assignNumber(num, this);
		}
	}
	
	@Override public SaveEnum[] totalEnumS(){
		int overrides=AI_ENABLES();
		SaveEnum[] base=SCode.values();
		if(overrides==0) return base;
		List<SaveEnum> list = Arrays.asList(base);
		if((overrides & AI_AFFECTS)!=AI_AFFECTS) list.remove(SCode.EFC);
		if((overrides & AI_BEHAVES)!=AI_BEHAVES) list.remove(SCode.BHV);
		if((overrides & AI_NAME)!=AI_NAME) list.remove(SCode.NAM);
		if((overrides & AI_DISP)!=AI_DISP) list.remove(SCode.DSP);
		if((overrides & AI_DESC)!=AI_DESC) list.remove(SCode.DSC);
		if((overrides & AI_ENV)!=AI_ENV) list.remove(SCode.ENV);
		return list.toArray(CMSavable.dummySEArray);
	}
	@Override public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	@Override public ModEnum[] totalEnumM(){
		int overrides=AI_ENABLES();
		ModEnum[] base=MCode.values();
		if(overrides==0) return base;
		List<ModEnum> list = Arrays.asList(base);
		if((overrides & AI_AFFECTS)!=AI_AFFECTS) list.remove(MCode.EFFECTS);
		if((overrides & AI_BEHAVES)!=AI_BEHAVES) list.remove(MCode.BEHAVIORS);
		if((overrides & AI_NAME)!=AI_NAME) list.remove(MCode.NAME);
		if((overrides & AI_DISP)!=AI_DISP) list.remove(MCode.DISPLAY);
		if((overrides & AI_DESC)!=AI_DESC) list.remove(MCode.DESCRIPTION);
		if((overrides & AI_ENV)!=AI_ENV) list.remove(MCode.ENVIRONMENTAL);
		return list.toArray(CMModifiable.dummyMEArray);
	}
	@Override public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	private enum SCode implements SaveEnum<AbstractSaveInteractable>{
		ENV(){
			public ByteBuffer save(AbstractSaveInteractable E){
				if(E.myEnvironmental==null) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savSubFull(E.getEnvObject()); }
			public int size(){return -1;}
			public CMSavable subObject(AbstractSaveInteractable fromThis){return fromThis.myEnvironmental;}
			public void load(AbstractSaveInteractable E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		DSP(){
			public ByteBuffer save(AbstractSaveInteractable E){ return CMLib.coffeeMaker().savString(E.display); }
			public int size(){return 0;}
			public void load(AbstractSaveInteractable E, ByteBuffer S){ E.display=CMLib.coffeeMaker().loadString(S); } },
		DSC(){
			public ByteBuffer save(AbstractSaveInteractable E){ return CMLib.coffeeMaker().savString(E.desc); }
			public int size(){return 0;}
			public void load(AbstractSaveInteractable E, ByteBuffer S){ E.desc=CMLib.coffeeMaker().loadString(S); } },
		EFC(){
			public ByteBuffer save(AbstractSaveInteractable E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(AbstractSaveInteractable E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(AbstractSaveInteractable E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(AbstractSaveInteractable E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		NAM(){
			public ByteBuffer save(AbstractSaveInteractable E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(AbstractSaveInteractable E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } };
		public CMSavable subObject(AbstractSaveInteractable fromThis){return null;} }
	private enum MCode implements ModEnum<AbstractSaveInteractable>{
		ENVIRONMENTAL(){
			public String brief(AbstractSaveInteractable E){return E.getEnvObject().ID();}
			public String prompt(AbstractSaveInteractable E){return "";}
			public void mod(AbstractSaveInteractable E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		DISPLAY(){
			public String brief(AbstractSaveInteractable E){return E.display;}
			public String prompt(AbstractSaveInteractable E){return ""+E.display;}
			public void mod(AbstractSaveInteractable E, MOB M){E.setDisplayText(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		DESCRIPTION(){
			public String brief(AbstractSaveInteractable E){return E.desc;}
			public String prompt(AbstractSaveInteractable E){return ""+E.desc;}
			public void mod(AbstractSaveInteractable E, MOB M){E.setDescription(CMLib.genEd().stringPrompt(M, ""+E.desc, false));} },
		EFFECTS(){
			public String brief(AbstractSaveInteractable E){return ""+E.affects.size();}
			public String prompt(AbstractSaveInteractable E){return "";}
			public void mod(AbstractSaveInteractable E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(AbstractSaveInteractable E){return ""+E.behaviors.size();}
			public String prompt(AbstractSaveInteractable E){return "";}
			public void mod(AbstractSaveInteractable E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		NAME(){
			public String brief(AbstractSaveInteractable E){return ""+E.name;}
			public String prompt(AbstractSaveInteractable E){return ""+E.name;}
			public void mod(AbstractSaveInteractable E, MOB M){E.setName(CMLib.genEd().stringPrompt(M, ""+E.name, false));} }
	}
}
