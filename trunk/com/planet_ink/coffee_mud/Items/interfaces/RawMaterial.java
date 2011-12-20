package com.planet_ink.coffee_mud.Items.interfaces;
/*
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
*/
import java.util.*;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public interface RawMaterial extends Item
{
	
	public String domainSource();
	public void setDomainSource(String src);
	public boolean rebundle();
	public void quickDestroy();
	
	// item materials
	public enum Material
	{
		UNKNOWN("Unknown material"),
		CLOTH("Cloth"),
		LEATHER("Leather"),
		METAL("Metal"),
		MITHRIL("Metal"),
		WOODEN("Wood"),
		GLASS("Glass"),
		VEGETATION("Vegetation"),
		FLESH("Flesh"),
		PAPER("Paper"),
		ROCK("Rock"),
		LIQUID("Liquid"),
		PRECIOUS("Stone"),
		ENERGY("Energy"),
		PLASTIC("Plastic")
		;
		public final String nounDesc;
		Material(String name){nounDesc=name;}
	}
	public enum Resource
	{
		NOTHING(Material.UNKNOWN, 0, 0, 0, 0),
		MEAT(Material.FLESH, 4, 20, 1, 3000),
		BEEF(Material.FLESH, 6, 20, 1, 3000),
		PORK(Material.FLESH, 8, 20, 1, 2500),
		POULTRY(Material.FLESH, 3, 20, 1, 2000),
		MUTTON(Material.FLESH, 4, 20, 1, 2800),
		FISH(Material.FLESH, 5, 100, 1, 590, "strong fishy"),
		WHEAT(Material.VEGETATION, 1, 20, 1, 770),
		CORN(Material.VEGETATION, 1, 20, 1, 720),
		RICE(Material.VEGETATION, 1, 20, 1, 750),
		CARROTS(Material.VEGETATION, 1, 5, 1, 720),
		TOMATOES(Material.VEGETATION, 1, 5, 1, 640),
		PEPPERS(Material.VEGETATION, 1, 5, 1, 640, "spicy"),
		GREENS(Material.VEGETATION, 1, 5, 1, 540, "very mild"),
		FRUIT(Material.VEGETATION, 2, 10, 1, 720, "sweet and fruity"),
		APPLES(Material.VEGETATION, 2, 10, 1, 640, "sweet apply"),
		BERRIES(Material.VEGETATION, 2, 15, 1, 720, "sweet berry"),
		ORANGES(Material.VEGETATION, 2, 10, 1, 480, "citrusy"),
		LEMONS(Material.VEGETATION,2,10,1,480, "strong citrusy"),
		GRAPES(Material.VEGETATION,3,5,1,680, "mild sweet"),
		OLIVES(Material.VEGETATION,2,5,1,640, "pickly olive"),
		POTATOES(Material.VEGETATION,1,5,1,770),
		CACTUS(Material.VEGETATION,2,5,1,680),
		DATES(Material.VEGETATION,2,2,1,720, "sweet plumy"),
		SEAWEED(Material.VEGETATION,1,50,1,540),
		STONE(Material.ROCK,1,80,5,2500, "mild musty"),
		LIMESTONE(Material.ROCK,1,20,4,1550),
		FLINT(Material.ROCK,1,10,4,2600),
		GRANITE(Material.ROCK,2,10,6,2690),
		OBSIDIAN(Material.ROCK,10,5,6,2650),
		MARBLE(Material.ROCK,20,5,5,2560),
		SAND(Material.ROCK,1,50,1,1600),
		JADE(Material.PRECIOUS,50,2,5,3800),
		IRON(Material.METAL,20,10,6,7900),
		LEAD(Material.METAL,10,10,5,11300),
		BRONZE(Material.METAL,10,10,5,8100),
		SILVER(Material.METAL,30,2,5,10500),
		GOLD(Material.METAL,150,1,5,19320),
		ZINC(Material.METAL,10,5,5,7100),
		COPPER(Material.METAL,10,10,5,8900),
		TIN(Material.METAL,10,10,4,7300),
		MITHRIL(Material.MITHRIL,100,1,9,3990),
		ADAMANTITE(Material.MITHRIL,175,1,10,4500),
		STEEL(Material.METAL,75,0,8,7840),
		BRASS(Material.METAL,120,0,6,8500),
		WOOD(Material.WOODEN,2,10,3,920),
		PINE(Material.WOODEN,4,10,3,650, "fresh, clean piney"),
		BALSA(Material.WOODEN,1,5,2,130),
		OAK(Material.WOODEN,5,10,3,720, "rich oaky"),
		MAPLE(Material.WOODEN,10,5,3,689, "mild maply"),
		REDWOOD(Material.WOODEN,20,2,3,450),
		HICKORY(Material.WOODEN,5,5,3,830),
		SCALES(Material.LEATHER,10,20,4,1800),
		FUR(Material.CLOTH,20,20,2,890, "musky"),
		LEATHER(Material.LEATHER,10,20,2,945, "strong leathery"),
		HIDE(Material.CLOTH,4,20,1,920, "mild stinky"),
		WOOL(Material.CLOTH,10,20,1,1310),
		FEATHERS(Material.CLOTH,10,20,1,20),
		COTTON(Material.CLOTH,5,20,1,590),
		HEMP(Material.CLOTH,4,10,1,720, "grassy"),
		WATER(Material.LIQUID,0,100,0,1000),
		SALTWATER(Material.LIQUID,0,100,0,1030),
		LIQUID(Material.LIQUID,0,1,0,1000),
		GLASS(Material.GLASS,10,0,3,2800),
		PAPER(Material.PAPER,10,0,0,92),
		CLAY(Material.GLASS,1,50,1,1750, "mild dusty"),
		CHINA(Material.GLASS,30,0,3,2400),
		DIAMOND(Material.PRECIOUS,500,1,9,3510),
		CRYSTAL(Material.GLASS,10,5,3,2200),
		GEM(Material.PRECIOUS,100,1,3,3500),
		PEARL(Material.PRECIOUS,380,1,4,2000),
		PLATINUM(Material.METAL,150,1,6,21450),
		MILK(Material.LIQUID,2,10,0,1020, "mild milky"),
		EGGS(Material.FLESH,2,10,0,1120),
		HOPS(Material.VEGETATION,2,20,1,340, "mild grainy"),
		COFFEEBEANS(Material.VEGETATION,2,10,1,560, "mild coffee"),
		COFFEE(Material.LIQUID,0,10,0,430, "rich coffee"),
		OPAL(Material.PRECIOUS,80,2,5,2250),
		TOPAZ(Material.PRECIOUS,200,2,5,3570),
		AMETHYST(Material.PRECIOUS,300,2,5,2651),
		GARNET(Material.PRECIOUS,70,2,5,3870),
		AMBER(Material.PRECIOUS,80,5,5,2500),
		AQUAMARINE(Material.PRECIOUS,50,2,5,2800),
		CRYSOBERYL(Material.PRECIOUS,50,2,5,2800),
		IRONWOOD(Material.WOODEN,25,5,4,99),
		SILK(Material.CLOTH,200,5,1,160),
		COCOA(Material.VEGETATION,4,5,0,59),
		BLOOD(Material.LIQUID,1,100,0,102, "strong salty"),
		BONE(Material.ROCK,1,100,5,160),
		COAL(Material.ROCK,1,50,1,180, "chalky"),
		LAMPOIL(Material.LIQUID,"LAMP OIL",1,10,1,88, "light oily"),
		POISON(Material.LIQUID,1,1,1,100),
		LIQUOR(Material.LIQUID,10,1,1,79, "alcohol"),
		SUGAR(Material.VEGETATION,1,50,1,1600),
		HONEY(Material.LIQUID,1,50,1,1600),
		BARLEY(Material.VEGETATION,1,20,1,610),
		MUSHROOMS(Material.VEGETATION,1,20,1,50),
		HERBS(Material.VEGETATION,1,10,1,77, "fresh herbal"),
		VINE(Material.VEGETATION,1,10,1,88, "rich green"),
		FLOWERS(Material.VEGETATION,1,10,1,72, "nice floral"),
		PLASTIC(Material.PLASTIC,25,0,4,950),
		RUBBER(Material.PLASTIC,25,0,1,1506, "sour rubbery"),
		EBONY(Material.ROCK,5,5,5,2910),
		IVORY(Material.ROCK,5,5,3,1840),
		WAX(Material.FLESH,1,0,0,900),
		NUTS(Material.VEGETATION,0,20,0,640, "mild nutty"),
		BREAD(Material.VEGETATION,3,0,0,660),
		CRACKER(Material.VEGETATION,2,0,0,200),
		YEW(Material.WOODEN,15,2,5,850),
		DUST(Material.ROCK,0,20,0,1120, "dusty"),
		PIPEWEED(Material.VEGETATION,3,10,1,320, "strong grassy"),
		ENERGY(Material.ENERGY,30,0,4,0),
		STRAWBERRIES(Material.VEGETATION,10,1,1,75, "sweet berry"),
		BLUEBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry"),
		RASPBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry"),
		BOYSENBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry"),
		BLACKBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry"),
		SMURFBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry"),
		PEACHES(Material.VEGETATION,10,1,1,700, "peachy"),
		PLUMS(Material.VEGETATION,10,1,1,710, "sweey plumy"),
		ONIONS(Material.VEGETATION,10,1,1,760, "stinging oniony"),
		CHERRIES(Material.VEGETATION,10,1,1,810, "cherry"),
		GARLIC(Material.VEGETATION,10,1,1,815),
		PINEAPPLES(Material.VEGETATION,10,1,1,500, "fruity"),
		COCONUTS(Material.VEGETATION,10,1,2,250),
		BANANAS(Material.VEGETATION,10,1,1,790, "pungent banana"),
		LIMES(Material.VEGETATION,10,1,1,690, "citrusy"),
		SAP(Material.LIQUID,10,1,1,1600, "strong maply"),
		ONYX(Material.PRECIOUS,70,1,8,3300),
		TURQUIOSE(Material.PRECIOUS,70,1,8,3300),
		PERIDOT(Material.PRECIOUS,65,1,6,3300),
		QUARTZ(Material.PRECIOUS,25,1,5,3300),
		LAPIS(Material.PRECIOUS,70,1,6,3300),
		BLOODSTONE(Material.PRECIOUS,85,1,8,3300),
		MOONSTONE(Material.PRECIOUS,90,1,8,3300),
		ALEXANDRITE(Material.PRECIOUS,95,1,9,3300),
		TEAK(Material.WOODEN,20,2,3,1000),
		CEDAR(Material.WOODEN,15,2,3,900, "strong cedar"),
		ELM(Material.WOODEN,15,2,3,1100),
		CHERRYWOOD(Material.WOODEN,17,2,3,900),
		BEECHWOOD(Material.WOODEN,12,2,3,975),
		WILLOW(Material.WOODEN,12,2,1,1000),
		SYCAMORE(Material.WOODEN,11,2,2,1000),
		SPRUCE(Material.WOODEN,12,2,3,990),
		MESQUITE(Material.WOODEN,9,2,3,1150, "rich mesquite"),
		BASALT(Material.ROCK,10,2,4,3300),
		SHALE(Material.ROCK,5,2,2,1200),
		PUMICE(Material.ROCK,5,2,4,600),
		SANDSTONE(Material.ROCK,10,2,2,3500),
		SOAPSTONE(Material.ROCK,60,2,5,3600),
		SALMON(Material.FLESH,6,1,1,1000, "strong fishy"),
		CARP(Material.FLESH,6,1,1,1000, "strong fishy"),
		TROUT(Material.FLESH,6,1,1,1000, "strong fishy"),
		SHRIMP(Material.FLESH,6,1,1,1000, "mild fishy"),
		TUNA(Material.FLESH,6,1,1,1000, "strong fishy"),
		CATFISH(Material.FLESH,6,1,1,1000, "strong fishy"),
		BAMBOO(Material.WOODEN,15,10,4,120),
		SOAP(Material.VEGETATION,1,0,1,430, "light fragrant"),
		SPIDERSTEEL(Material.CLOTH,150,0,2,630),
		ASH(Material.VEGETATION,1,0,0,50, "dusty"),
		PERFUME(Material.LIQUID,1,1,1,100, "strong fragrant"),
		ATLANTITE(Material.MITHRIL,200,1,6,85),
		CHEESE(Material.VEGETATION,25,0,1,640, "mild "),
		BEANS(Material.VEGETATION,1,15,1,750),
		CRANBERRIES(Material.VEGETATION, 10, 1,  1,  75, "sweet berry"),
		DRAGONBLOOD(Material.LIQUID,40,20,1,3000, "mild salty"),
		DRAGONMEAT(Material.FLESH,10,100,0,102, "mild salty"),
		RUBY(Material.PRECIOUS,200,2,9,400),
		SAPPHIRE(Material.PRECIOUS,175,2,5,395),
		EMERALD(Material.PRECIOUS,150,2,5,276),
		;
		public final Material material;
		protected final String name;
		public final int value;
		public final int frequency;
		public final int hardness;
		public final int density;
		public final String smell;
		private Resource(Material m, int value, int frequency, int hardness, int density)
		{
			material=m;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.name=null;
			this.smell=null;
		}
		private Resource(Material m, String altName, int value, int frequency, int hardness, int density)
		{
			material=m;
			name=altName;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.smell=null;
		}
		private Resource(Material m, int value, int frequency, int hardness, int density, String smell)
		{
			material=m;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.smell=smell;
			this.name=null;
		}
		private Resource(Material m, String altName, int value, int frequency, int hardness, int density, String smell)
		{
			material=m;
			name=altName;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.smell=smell;
		}
		public String toString()
		{
			if(name!=null) return name;
			return super.toString();
		}
	}

	public static final EnumSet<Resource> DEFAULT_FISH=EnumSet.of(
	Resource.FISH,
	Resource.SALMON,
	Resource.CARP,
	Resource.TROUT,
	Resource.SHRIMP,
	Resource.TUNA,
	Resource.CATFISH
	);
	
	public static final EnumSet<Resource> DEFAULT_BERRIES=EnumSet.of(
	Resource.BERRIES,
	Resource.STRAWBERRIES,
	Resource.BLUEBERRIES,
	Resource.RASPBERRIES,
	Resource.BOYSENBERRIES,
	Resource.BLACKBERRIES,
	Resource.SMURFBERRIES,
	Resource.CRANBERRIES
	);
}