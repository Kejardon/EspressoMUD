package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

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
public class MixedEnvStats implements EnvStats, Ownable
{
	public String ID(){return "MixedEnvStats";}
	protected double Speed=1.0;			// should be positive
	protected CopyOnWriteArrayList<String> ambiances=new CopyOnWriteArrayList();
	protected int width;
	protected int length;
	protected int height;
	protected int weight;
	protected long volume;
	protected int magic;
	protected CMSavable parent;
	protected WVector<RawMaterial.Resource> materials=new WVector();

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public MixedEnvStats(){}

	public EnvShape shape(){return null;}
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
		int minimum=materials.weight()/2 + 1;
		for(int i=0;i<materials.size();i++)
			if(materials.weight(i)>=minimum)
				return materials.get(i);
		return null;
	}
	public WVector<RawMaterial.Resource> materialSet(){return materials;}
	public long volume(){return volume;}

	public void setShape(EnvShape newShape){}
	public void setWeight(int newWeight){weight=newWeight; if(parent!=null)parent.saveThis();}
	public void setSpeed(double newSpeed){Speed=newSpeed; if(parent!=null)parent.saveThis();}
	public void setAbility(int newAdjustment){magic=newAdjustment; if(parent!=null)parent.saveThis();}
	public void setHeight(int newHeight){height=newHeight; if(parent!=null)parent.saveThis();}
	public void setLength(int newLength){length=newLength; if(parent!=null)parent.saveThis();}
	public void setWidth(int newWidth){weight=newWidth; if(parent!=null)parent.saveThis();}
	public void setMaterial(RawMaterial.Resource newMaterial)
	{
		materials.clear();
		materials.add(newMaterial);
		if(parent!=null) parent.saveThis();
	}
	public void setMaterials(WVector<RawMaterial.Resource> newMaterials){materials = newMaterials; if(parent!=null)parent.saveThis();}
	public void setVolume(long newVolume){volume=newVolume;}
	public void recalcLengthsFromVolume()
	{
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
		long density=0;
		RawMaterial.Resource[] mats=new RawMaterial.Resource[materials.size()];
		int[] weights=new int[mats.length];
		materials.toArrays(mats, weights);
		int totalWeights=materials.weight();
		for(int i=0;i<mats.length;i++)
			density += mats[i].density * weights[i];
		density /= totalWeights;	//Density now contains the average density of all material parts
		long tempVal=density * volume;
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

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new MixedEnvStats();}}
	public void initializeClass(){}
	public CMObject copyOf()
	{
		try
		{
			MixedEnvStats E=(MixedEnvStats)this.clone();
			E.ambiances=(CopyOnWriteArrayList<String>)ambiances.clone();
			E.materials=(WVector)materials.clone();
			return E;
		}
		catch(java.lang.CloneNotSupportedException e)
		{
			return new MixedEnvStats();
		}
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void copyInto(EnvStats intoStats)
	{
		if(intoStats instanceof MixedEnvStats)
		{
			MixedEnvStats copy=(MixedEnvStats)intoStats;
			copy.width=width;
			copy.length=length;
			copy.height=height;
			copy.weight=weight;
			copy.magic=magic;
			copy.Speed=Speed;
			copy.ambiances=(CopyOnWriteArrayList<String>)ambiances.clone();
			copy.materials=(WVector)materials.clone();
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

	private enum SCode implements SaveEnum<MixedEnvStats>{
		SPD(){
			public ByteBuffer save(MixedEnvStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[8]).putDouble(E.Speed).rewind(); }
			public int size(){return 8;}
			public void load(MixedEnvStats E, ByteBuffer S){ E.Speed=S.getDouble(); } },
		INT(){
			public ByteBuffer save(MixedEnvStats E){ return CMLib.coffeeMaker().savAInt(new int[] {E.width, E.length, E.height, E.weight, E.magic}); }
			public int size(){return 20;}
			public void load(MixedEnvStats E, ByteBuffer S){
				int[] ints=CMLib.coffeeMaker().loadAInt(S);
				E.width=ints[0]; E.length=ints[1]; E.height=ints[2]; E.weight=ints[3]; E.magic=ints[4]; } },
		VOL(){
			public ByteBuffer save(MixedEnvStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[8]).putLong(E.volume).rewind(); }
			public int size(){return 8;}
			public void load(MixedEnvStats E, ByteBuffer S){ E.volume=S.getLong(); } },
		AMB(){
			public ByteBuffer save(MixedEnvStats E){ return CMLib.coffeeMaker().savAString((String[])E.ambiances.toArray(CMClass.dummyStringArray)); }
			public int size(){return 0;}
			public void load(MixedEnvStats E, ByteBuffer S){ E.ambiances=new CopyOnWriteArrayList(CMLib.coffeeMaker().loadAString(S)); } },
		MAT(){
			public ByteBuffer save(MixedEnvStats E){ return CMLib.coffeeMaker().getEnumWVector(E.materials); }
			public int size(){return 0;}
			public void load(MixedEnvStats E, ByteBuffer S){ E.materials=CMLib.coffeeMaker().setEnumWVector(RawMaterial.Resource.class, S); } },
		;
		public CMSavable subObject(MixedEnvStats fromThis){return null;} }
	private enum MCode implements ModEnum<MixedEnvStats>{
		AMBIENCES(){
			public String brief(MixedEnvStats E){ return ""+E.ambiances.size();}
			public String prompt(MixedEnvStats E){ return "";}
			public void mod(MixedEnvStats E, MOB M){
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
			public String brief(MixedEnvStats E){return ""+E.Speed;}
			public String prompt(MixedEnvStats E){return ""+E.Speed;}
			public void mod(MixedEnvStats E, MOB M){E.Speed=CMLib.genEd().doublePrompt(M, ""+E.Speed);} },
		WIDTH(){
			public String brief(MixedEnvStats E){return ""+E.width;}
			public String prompt(MixedEnvStats E){return ""+E.width;}
			public void mod(MixedEnvStats E, MOB M){E.width=CMLib.genEd().intPrompt(M, ""+E.width);} },
		LENGTH(){
			public String brief(MixedEnvStats E){return ""+E.length;}
			public String prompt(MixedEnvStats E){return ""+E.length;}
			public void mod(MixedEnvStats E, MOB M){E.length=CMLib.genEd().intPrompt(M, ""+E.length);} },
		HEIGHT(){
			public String brief(MixedEnvStats E){return ""+E.height;}
			public String prompt(MixedEnvStats E){return ""+E.height;}
			public void mod(MixedEnvStats E, MOB M){E.height=CMLib.genEd().intPrompt(M, ""+E.height);} },
		VOLUME(){
			public String brief(MixedEnvStats E){return ""+E.volume;}
			public String prompt(MixedEnvStats E){return ""+E.volume;}
			public void mod(MixedEnvStats E, MOB M){
				E.volume=CMLib.genEd().longPrompt(M, ""+E.volume); 
				if(M.session().confirm("Recalculate lengths from volume? (Y/n)","Y")) E.recalcLengthsFromVolume();
				if(M.session().confirm("Recalculate weight from volume? (Y/n)","Y")) E.recalcWeightFromVolume(); } },
		WEIGHT(){
			public String brief(MixedEnvStats E){return ""+E.weight;}
			public String prompt(MixedEnvStats E){return ""+E.weight;}
			public void mod(MixedEnvStats E, MOB M){E.weight=CMLib.genEd().intPrompt(M, ""+E.weight);} },
		MAGIC(){
			public String brief(MixedEnvStats E){return ""+E.magic;}
			public String prompt(MixedEnvStats E){return ""+E.magic;}
			public void mod(MixedEnvStats E, MOB M){E.magic=CMLib.genEd().intPrompt(M, ""+E.magic);} },
		MATERIALS(){
			public String brief(MixedEnvStats E){return ""+E.materials.size();}
			public String prompt(MixedEnvStats E){return E.materials.toString();}
			public void mod(MixedEnvStats E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					WVector<RawMaterial.Resource> V=(WVector)E.materials.clone();
					int i=CMLib.genEd().promptWVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						RawMaterial.Resource S=CMLib.genEd().enumPrompt(M, null, RawMaterial.Resource.values());
						if(S==null) continue;
						int weight=CMLib.genEd().intPrompt(M, "1");
						if(!E.materials.replace(S, S, weight)) E.materials.add(S, weight); }
					else if(i<V.size()) {
						char action=M.session().prompt("(D)elete material or edit (W)eight or (M)aterial?"," ").trim().toUpperCase().charAt(0);
						if(action=='D') E.materials.remove(V.get(i));
						else if(action=='W') { 
							int weight=CMLib.genEd().intPrompt(M, ""+V.weight(i));
							if(!E.materials.replace(V.get(i), V.get(i), weight)) E.materials.add(V.get(i), weight); }
						else if(action=='M') {
							RawMaterial.Resource S=CMLib.genEd().enumPrompt(M, null, RawMaterial.Resource.values());
							if(!E.materials.replace(S, S, V.weight(i))) E.materials.add(S, V.weight(i)); } } } } },
/*		DISPOSITION(){
			public String brief(MixedEnvStats E){return ""+E.disposition;}
			public String prompt(MixedEnvStats E){return ""+E.disposition;}
			public void mod(MixedEnvStats E, MOB M){E.disposition=CMLib.genEd().intPrompt(M, ""+E.disposition);} }, */
		; }
/*
	public boolean sameAs(EnvStats E){ return true; }
*/}
