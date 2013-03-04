package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class DefaultEnvStats implements EnvStats, Ownable
{
	public String ID(){return "DefaultEnvStats";}
	protected double Speed=1.0;			// should be positive
	protected CopyOnWriteArrayList<String> ambiances=new CopyOnWriteArrayList();
	protected int width;
	protected int length;
	protected int height;
	protected int weight;
	protected long volume;
	protected int magic;
	protected CMSavable parent;
	protected RawMaterial.Resource material=null;//RawMaterial.Resource.NOTHING;
	//TODO: Material should probably be stored here? Still need to decide how

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public DefaultEnvStats(){}

	public int ability(){return magic;}
	public int weight(){return weight;}
	public int height(){return height;}
	public int length(){return length;}
	public int width(){return width;}
	public double speed(){return Speed;}
	public Iterator<String> ambiances(){ return ambiances.iterator();}
	public boolean isComposite(){return false;}
	public RawMaterial.Resource material()
	{
		if(material==null) synchronized(this) {
			if(material==null)
				material=RawMaterial.Resource.NOTHING;
		}
		return material;
	}
	public WVector<RawMaterial.Resource> materialSet(){return null;}
	public long volume(){return volume;}

	public void setWeight(int newWeight){weight=newWeight; if(parent!=null)parent.saveThis();}
	public void setSpeed(double newSpeed){Speed=newSpeed; if(parent!=null)parent.saveThis();}
	public void setAbility(int newAdjustment){magic=newAdjustment; if(parent!=null)parent.saveThis();}
	public void setHeight(int newHeight){height=newHeight; if(parent!=null)parent.saveThis();}
	public void setLength(int newLength){length=newLength; if(parent!=null)parent.saveThis();}
	public void setWidth(int newWidth){weight=newWidth; if(parent!=null)parent.saveThis();}
	public void setMaterial(RawMaterial.Resource newMaterial){material=newMaterial; if(parent!=null)parent.saveThis();}
	public void setMaterials(WVector<RawMaterial.Resource> newMats){}
	public void setVolume(long newVolume){volume=newVolume; if(parent!=null)parent.saveThis();}
	public void recalcLengthsFromVolume()
	{
		/*
		x1a / x2a = x1b / x2b = c1
			x2b = x1b * (x2a / x1a)
		x1a / x3a = x1b / x3b = c2
			x3b = x1b * (x3a / x1a)
		x1b * x2b * x3b = volume
		x1b^3 * (x2a * x3a / x1a^2) = volume
		
		x1b = (volume * (x1a^2) / (x2a * x3a))^(1/3)
			c1 = x1a / x2a
			c2 = x1a / x3a
			x1b = (volume * c1 * c2)^(1/3)
			x2b = x1b / c1
			x3b = x1b / c2
		*/
		if(height <= 0 || width <= 0 || length <= 0) return;
		if(volume == 0)
		{
			height = 0;
			width = 0;
			length = 0;
			return;
		}
		float c1 = ((float)height) / length;
		float c2 = ((float)height) / width;
		height = Math.round((float)Math.pow(volume * c1 * c2, 1.0/3));
		if(height == 0) height = 1;
		width = Math.round(height/c1);
		if(width == 0) width = 1;
		length = Math.round(height/c2);
		if(length == 0) length = 1;
	}
	public void recalcWeightFromVolume()
	{
		long tempVal=material.density * volume;
		if(tempVal>0 && tempVal<1000)
			weight=1;
		else
			weight=(int)(tempVal/1000);
	}
	public void addAmbiance(String ambiance)
	{
		synchronized(ambiances)
		{
			for(String S : ambiances)
				if(S.equalsIgnoreCase(ambiance))
					return;
			ambiances.add(ambiance);
		}
		if(parent!=null)parent.saveThis();
	}
	public void delAmbiance(String ambiance)
	{
		synchronized(ambiances)
		{
			for(String S : ambiances)
				if(S.equalsIgnoreCase(ambiance))
				{
					ambiances.remove(S);
					if(parent!=null)parent.saveThis();
					return;
				}
		}
	}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultEnvStats();}}
	public void initializeClass(){}
	public CMObject copyOf()
	{
		try
		{
			DefaultEnvStats E=(DefaultEnvStats)this.clone();
			E.ambiances=(CopyOnWriteArrayList<String>)ambiances.clone();
			return E;
		}
		catch(java.lang.CloneNotSupportedException e)
		{
			return new DefaultEnvStats();
		}
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void copyInto(EnvStats intoStats)
	{
		if(intoStats instanceof DefaultEnvStats)
		{
			DefaultEnvStats copy=(DefaultEnvStats)intoStats;
			copy.width=width;
			copy.length=length;
			copy.height=height;
			copy.weight=weight;
			copy.magic=magic;
			copy.Speed=Speed;
			copy.ambiances=(CopyOnWriteArrayList<String>)ambiances.clone();
			copy.material=material;
			copy.saveThis();
		}
	}

	public void destroy(){}
	public boolean amDestroyed()
	{
		if(parent!=null)
			return parent.amDestroyed();
		return true;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){if(parent!=null)parent.saveThis();}
	public void prepDefault(){}

	private enum SCode implements CMSavable.SaveEnum{
		SPD(){
			public ByteBuffer save(DefaultEnvStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[8]).putDouble(E.Speed).rewind(); }
			public int size(){return 8;}
			public void load(DefaultEnvStats E, ByteBuffer S){ E.Speed=S.getDouble(); } },
		INT(){
			public ByteBuffer save(DefaultEnvStats E){ return CMLib.coffeeMaker().savAInt(new int[] {E.width, E.length, E.height, E.weight, E.magic}); }
			public int size(){return 20;}
			public void load(DefaultEnvStats E, ByteBuffer S){
				int[] ints=CMLib.coffeeMaker().loadAInt(S);
				E.width=ints[0]; E.length=ints[1]; E.height=ints[2]; E.weight=ints[3]; E.magic=ints[4]; } },
		VOL(){
			public ByteBuffer save(DefaultEnvStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[8]).putLong(E.volume).rewind(); }
			public int size(){return 8;}
			public void load(DefaultEnvStats E, ByteBuffer S){ E.volume=S.getLong(); } },
		AMB(){
			public ByteBuffer save(DefaultEnvStats E){ return CMLib.coffeeMaker().savAString((String[])E.ambiances.toArray(CMClass.dummyStringArray)); }
			public int size(){return 0;}
			public void load(DefaultEnvStats E, ByteBuffer S){ E.ambiances=new CopyOnWriteArrayList(CMLib.coffeeMaker().loadAString(S)); } },
		MAT(){
			public ByteBuffer save(DefaultEnvStats E){ return CMLib.coffeeMaker().savString(E.material().name()); }
			public int size(){return 0;}
			public void load(DefaultEnvStats E, ByteBuffer S){
				RawMaterial.Resource newMat=CMClass.valueOf(RawMaterial.Resource.class, CMLib.coffeeMaker().loadString(S));
				if(newMat!=null) E.material=newMat; } },
		;
		public abstract ByteBuffer save(DefaultEnvStats E);
		public abstract void load(DefaultEnvStats E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((DefaultEnvStats)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((DefaultEnvStats)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		AMBIENCES(){
			public String brief(DefaultEnvStats E){ return ""+E.ambiances.size();}
			public String prompt(DefaultEnvStats E){ return "";}
			public void mod(DefaultEnvStats E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					String[] V=(String[])E.ambiances.toArray(CMClass.dummyStringArray);
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.length) {
						String S=CMLib.genEd().stringPrompt(M, "", false);
						if(S.length()>0) {
							E.delAmbiance(S);
							E.addAmbiance(S); } }
					else if(i<V.length) E.delAmbiance(V[i]); } } },
		SPEED(){
			public String brief(DefaultEnvStats E){return ""+E.Speed;}
			public String prompt(DefaultEnvStats E){return ""+E.Speed;}
			public void mod(DefaultEnvStats E, MOB M){E.Speed=CMLib.genEd().doublePrompt(M, ""+E.Speed);} },
		WIDTH(){
			public String brief(DefaultEnvStats E){return ""+E.width;}
			public String prompt(DefaultEnvStats E){return ""+E.width;}
			public void mod(DefaultEnvStats E, MOB M){E.width=CMLib.genEd().intPrompt(M, ""+E.width);} },
		LENGTH(){
			public String brief(DefaultEnvStats E){return ""+E.length;}
			public String prompt(DefaultEnvStats E){return ""+E.length;}
			public void mod(DefaultEnvStats E, MOB M){E.length=CMLib.genEd().intPrompt(M, ""+E.length);} },
		HEIGHT(){
			public String brief(DefaultEnvStats E){return ""+E.height;}
			public String prompt(DefaultEnvStats E){return ""+E.height;}
			public void mod(DefaultEnvStats E, MOB M){E.height=CMLib.genEd().intPrompt(M, ""+E.height);} },
		VOLUME(){
			public String brief(DefaultEnvStats E){return ""+E.volume;}
			public String prompt(DefaultEnvStats E){return ""+E.volume;}
			public void mod(DefaultEnvStats E, MOB M){
				E.volume=CMLib.genEd().longPrompt(M, ""+E.volume); 
				if(M.session().confirm("Recalculate lengths from volume? (Y/n)","Y")) E.recalcLengthsFromVolume();
				if(M.session().confirm("Recalculate weight from volume? (Y/n)","Y")) E.recalcWeightFromVolume(); } },
		WEIGHT(){
			public String brief(DefaultEnvStats E){return ""+E.weight;}
			public String prompt(DefaultEnvStats E){return ""+E.weight;}
			public void mod(DefaultEnvStats E, MOB M){E.weight=CMLib.genEd().intPrompt(M, ""+E.weight);} },
		MAGIC(){
			public String brief(DefaultEnvStats E){return ""+E.magic;}
			public String prompt(DefaultEnvStats E){return ""+E.magic;}
			public void mod(DefaultEnvStats E, MOB M){E.magic=CMLib.genEd().intPrompt(M, ""+E.magic);} },
		MATERIAL(){
			public String brief(DefaultEnvStats E){return E.material().toString();}
			public String prompt(DefaultEnvStats E){return E.material.toString();}
			public void mod(DefaultEnvStats E, MOB M){E.material=(RawMaterial.Resource)CMLib.genEd().enumPrompt(M, E.material.toString(), RawMaterial.Resource.values());} },
/*		DISPOSITION(){
			public String brief(DefaultEnvStats E){return ""+E.disposition;}
			public String prompt(DefaultEnvStats E){return ""+E.disposition;}
			public void mod(DefaultEnvStats E, MOB M){E.disposition=CMLib.genEd().intPrompt(M, ""+E.disposition);} }, */
		;
		public abstract String brief(DefaultEnvStats fromThis);
		public abstract String prompt(DefaultEnvStats fromThis);
		public abstract void mod(DefaultEnvStats toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultEnvStats)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultEnvStats)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultEnvStats)toThis, M);} }
/*
	public boolean sameAs(EnvStats E){ return true; }
*/}
