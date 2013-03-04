package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//A thing that players can directly observe and interact with in the MUD
public interface Interactable extends Environmental.EnvHolder, Affectable, Behavable
{
	public static final Interactable[] dummyInteractableArray=new Interactable[0];
	public String name();
	public String plainName();
	public void setName(String newName);
	public String displayText();
	public String plainDisplayText();
	public void setDisplayText(String newDisplayText);
	public String description();
	public String plainDescription();
	public void setDescription(String newDescription);
	//public void destroy();
	//public boolean amDestroyed();
	//public boolean sameAs(Interactable E);

/*
	protected String name="";
	protected String display="";
	protected String desc="";
	protected String plainName;
	protected String plainNameOf;
	protected String plainDisplay;
	protected String plainDisplayOf;
	protected String plainDesc;
	protected String plainDescOf;

	public String name(){ return name;}
	public String plainName()
	{
		if(name==plainNameOf)
			return plainName;
		String newName=name;
		String newPlain=CMLib.coffeeFilter().toRawString(newName);
		synchronized(this)
		{
			plainName=newPlain;
			plainNameOf=newName;
		}
		return newPlain;
	}
	public void setName(String newName){name=newName; CMLib.database().saveObject(this);}
	public String displayText(){return display;}
	public String plainDisplayText()
	{
		if(display==plainDisplayOf)
			return plainDisplay;
		String newDisplay=display;
		String newPlain=CMLib.coffeeFilter().toRawString(newDisplay);
		synchronized(this)
		{
			plainDisplay=newPlain;
			plainDisplayOf=newDisplay;
		}
		return newPlain;
	}
	public void setDisplayText(String newDisplayText){display=newDisplayText; CMLib.database().saveObject(this);}
	public String description(){return desc;}
	public String plainDescription()
	{
		if(desc==plainDescOf)
			return plainDesc;
		String newDesc=desc;
		String newPlain=CMLib.coffeeFilter().toRawString(newDesc);
		synchronized(this)
		{
			plainDesc=newPlain;
			plainDescOf=newDesc;
		}
		return newPlain;
	}
	public void setDescription(String newDescription){desc=newDescription; CMLib.database().saveObject(this);}

	private enum SCode implements CMSavable.SaveEnum{
		NAM(){
			public ByteBuffer save(Interactable E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(Interactable E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		DSP(){
			public ByteBuffer save(Interactable E){ return CMLib.coffeeMaker().savString(E.display); }
			public int size(){return 0;}
			public void load(Interactable E, ByteBuffer S){ E.display=CMLib.coffeeMaker().loadString(S); } },
		DSC(){
			public ByteBuffer save(Interactable E){ return CMLib.coffeeMaker().savString(E.desc); }
			public int size(){return 0;}
			public void load(Interactable E, ByteBuffer S){ E.desc=CMLib.coffeeMaker().loadString(S); } },
	}
	private enum MCode implements CMModifiable.ModEnum{
		NAME(){
			public String brief(Interactable E){return ""+E.name;}
			public String prompt(Interactable E){return ""+E.name;}
			public void mod(Interactable E, MOB M){E.setName(CMLib.genEd().stringPrompt(M, ""+E.name, false));} }
		DISPLAY(){
			public String brief(Interactable E){return E.display;}
			public String prompt(Interactable E){return ""+E.display;}
			public void mod(Interactable E, MOB M){E.setDisplayText(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		DESCRIPTION(){
			public String brief(Interactable E){return E.desc;}
			public String prompt(Interactable E){return ""+E.desc;}
			public void mod(Interactable E, MOB M){E.setDescription(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
	}


*/
}