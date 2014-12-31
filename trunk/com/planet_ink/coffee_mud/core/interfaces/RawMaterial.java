package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public interface RawMaterial extends Item
{

	@Override public RawMaterial newInstance();
	@Override public RawMaterial copyOf();
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
	//TODO: Come up with non-placeholder, accurate data for this!
	public enum Resource
	{
		NOTHING(Material.UNKNOWN, 0, 0, 0, 0, Phases.OnlySolid, ThermalReaction.None),
		MEAT(Material.FLESH, 4, 20, 1, 3000, Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		BEEF(Material.FLESH, 6, 20, 1, 3000, Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		PORK(Material.FLESH, 8, 20, 1, 2500, Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		POULTRY(Material.FLESH, 3, 20, 1, 2000, Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		MUTTON(Material.FLESH, 4, 20, 1, 2800, Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		FISH(Material.FLESH, 5, 100, 1, 590, "strong fishy", Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		INSECT(Material.FLESH, 6, 20, 2, 1100, Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		WHEAT(Material.VEGETATION, 1, 20, 1, 770, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		CORN(Material.VEGETATION, 1, 20, 1, 720, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		RICE(Material.VEGETATION, 1, 20, 1, 750, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		CARROTS(Material.VEGETATION, 1, 5, 1, 720, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		TOMATOES(Material.VEGETATION, 1, 5, 1, 640, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		PEPPERS(Material.VEGETATION, 1, 5, 1, 640, "spicy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		GREENS(Material.VEGETATION, 1, 5, 1, 540, "very mild", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		FRUIT(Material.VEGETATION, 2, 10, 1, 720, "sweet and fruity", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		APPLES(Material.VEGETATION, 2, 10, 1, 640, "sweet apply", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BERRIES(Material.VEGETATION, 2, 15, 1, 720, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		ORANGES(Material.VEGETATION, 2, 10, 1, 480, "citrusy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		LEMONS(Material.VEGETATION,2,10,1,480, "strong citrusy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		GRAPES(Material.VEGETATION,3,5,1,680, "mild sweet", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		OLIVES(Material.VEGETATION,2,5,1,640, "pickly olive", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		POTATOES(Material.VEGETATION,1,5,1,770, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		CACTUS(Material.VEGETATION,2,5,1,680, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		DATES(Material.VEGETATION,2,2,1,720, "sweet plumy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		SEAWEED(Material.VEGETATION,1,50,1,540, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		STONE(Material.ROCK,1,80,5,2500, "mild musty", new Phases(100000, 150000), ThermalReaction.None), //Melt, boil
		LIMESTONE(Material.ROCK,1,20,4,1550, Phases.OnlySolid, ThermalReaction.ToQuickLime),
		FLINT(Material.ROCK,1,10,4,2600, new Phases(190000, 250000), ThermalReaction.ToSilica), //Melt, boil (Change to Silica?)
		GRANITE(Material.ROCK,2,10,6,2690, new Phases(150000, 220000), ThermalReaction.ToSilica), //Melt, boil (Change to Silica?)
		OBSIDIAN(Material.ROCK,10,5,6,2650, new Phases(190000, 250000), ThermalReaction.ToSilica), //Melt, boil (Change to Silica?)
		MARBLE(Material.ROCK,20,5,5,2560, Phases.OnlySolid, ThermalReaction.ToQuickLime), //Burn to quick lime
		SAND(Material.ROCK,1,50,1,1600, new Phases(190000, 250000), ThermalReaction.ToSilica), //Melt, boil (Change to Silica?)
		JADE(Material.PRECIOUS,50,2,5,3800, new Phases(150000, 200000), ThermalReaction.None), //Melt, boil (Largely guessing on this)
		IRON(Material.METAL,20,10,6,7900, new Phases(180000, 300000), ThermalReaction.None), //Melt, boil
		LEAD(Material.METAL,10,10,5,11300, new Phases(60000, 200000), ThermalReaction.None), //Melt, boil
		BRONZE(Material.METAL,10,10,5,8100, new Phases(120000, 285000), ThermalReaction.None), //Melt, boil
		SILVER(Material.METAL,30,2,5,10500, new Phases(123000, 244000), ThermalReaction.None), //Melt, boil
		GOLD(Material.METAL,150,1,5,19320, new Phases(134000, 313000), ThermalReaction.None), //Melt, boil
		ZINC(Material.METAL,10,5,5,7100, new Phases(69000, 118000), ThermalReaction.None), //Melt, boil
		COPPER(Material.METAL,10,10,5,8900, new Phases(136000, 283000), ThermalReaction.None), //Melt, boil
		TIN(Material.METAL,10,10,4,7300, new Phases(50000, 287000), ThermalReaction.None), //Melt, boil
		MITHRIL(Material.MITHRIL,100,1,9,3990, new Phases(200000, 380000), ThermalReaction.None), //Melt, boil
		ADAMANTITE(Material.MITHRIL,175,1,10,4500, new Phases(250000, 400000), ThermalReaction.None), //Melt, boil
		STEEL(Material.METAL,75,0,8,7840, new Phases(155000, 320000), ThermalReaction.None), //Melt, boil
		BRASS(Material.METAL,120,0,6,8500, new Phases(120000, 270000), ThermalReaction.None), //Melt, boil
		WOOD(Material.WOODEN,2,10,3,920, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		PINE(Material.WOODEN,4,10,3,650, "fresh, clean piney", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BALSA(Material.WOODEN,1,5,2,130, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		OAK(Material.WOODEN,5,10,3,720, "rich oaky", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		MAPLE(Material.WOODEN,10,5,3,689, "mild maply", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		REDWOOD(Material.WOODEN,20,2,3,450, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		HICKORY(Material.WOODEN,5,5,3,830, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		SCALES(Material.LEATHER,10,20,4,1800, Phases.OnlySolid, ThermalReaction.WoodBurn), //No ideas for this. Treating as leather.
		FUR(Material.CLOTH,20,20,2,890, "musky", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		LEATHER(Material.LEATHER,10,20,2,945, "strong leathery", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal when prolonged
		HIDE(Material.CLOTH,4,20,1,920, "mild stinky", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		WOOL(Material.CLOTH,10,20,1,1310, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		FEATHERS(Material.CLOTH,10,20,1,20, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		COTTON(Material.CLOTH,5,20,1,590, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		HEMP(Material.CLOTH,4,10,1,720, "grassy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		WATER(Material.LIQUID,0,100,0,1000, new Phases(27300, 37300), ThermalReaction.None), //Melt, boil
		SALTWATER(Material.LIQUID,0,100,0,1030, new Phases(26300, 37600), ThermalReaction.None), //Melt, boil
		//LIQUID(Material.LIQUID,0,1,0,1000),
		GLASS(Material.GLASS,10,0,3,2800, new Phases(200000, 250000), ThermalReaction.None), //Melt, boil (Change to Silica?)
		PAPER(Material.PAPER,10,0,0,92, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		CLAY(Material.GLASS,1,50,1,1750, "mild dusty", Phases.OnlySolid, new ThermalReaction(150000, 0, new ThermalReaction.TRResult(1000, "CHINA"))), //Change to China
		CHINA(Material.GLASS,30,0,3,2400, new Phases(200000, 250000), ThermalReaction.None), //Melt, boil
		DIAMOND(Material.PRECIOUS,500,1,9,3510, Phases.OnlySolid, new ThermalReaction(100000)), //Burn and evaporate
		//CRYSTAL(Material.GLASS,10,5,3,2200),
		//GEM(Material.PRECIOUS,100,1,3,3500),
		PEARL(Material.PRECIOUS,380,1,4,2000, Phases.OnlySolid, ThermalReaction.ToQuickLime), //Burns to quick lime
		PLATINUM(Material.METAL,150,1,6,21450, new Phases(204000, 410000), ThermalReaction.None), //Melt, boil
		MILK(Material.LIQUID,2,10,0,1020, "mild milky", new Phases(27000, 37400), ThermalReaction.None), //Melt, boil
		EGGS(Material.FLESH,2,10,0,1120, Phases.OnlySolid, ThermalReaction.None), //Very complicated, going to ignore
		HOPS(Material.VEGETATION,2,20,1,340, "mild grainy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		COFFEEBEANS(Material.VEGETATION,2,10,1,560, "mild coffee", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		COFFEE(Material.LIQUID,0,10,0,430, "rich coffee", new Phases(27000, 37400), ThermalReaction.None), //Melt, boil
		OPAL(Material.PRECIOUS,80,2,5,2250, new Phases(190000, 250000), ThermalReaction.ToSilica.scaleResult(900)), //Melt, boil. Change to Silica and lose 10% mass
		TOPAZ(Material.PRECIOUS,200,2,5,3570, Phases.OnlySolid, 
				new ThermalReaction(130000, 0,
					new ThermalReaction.TRResult(750, "MULLITE"),
					new ThermalReaction.TRResult(100, "QUARTZ")
				)), //Transform to Mullite and Silica (1 Topaz --> .75 Mullite + .10 Silica)
		AMETHYST(Material.PRECIOUS,300,2,5,2651, new Phases(190000, 250000), ThermalReaction.ToSilica), //Melt, boil. Change to Silica
		GARNET(Material.PRECIOUS,70,2,5,3870, Phases.OnlySolid, ThermalReaction.None), //I can't find any info
		AMBER(Material.PRECIOUS,80,5,5,2500, Phases.OnlySolid, new ThermalReaction(65000, ThermalReaction.SlowBurn)), //Burns slowly to ?
		AQUAMARINE(Material.PRECIOUS,50,2,5,2800, Phases.OnlySolid, ThermalReaction.None), //Beryl class. I can't find any info
		CRYSOBERYL(Material.PRECIOUS,50,2,5,2800, Phases.OnlySolid, ThermalReaction.None), //I can't find any info
		IRONWOOD(Material.WOODEN,25,5,4,99, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		SILK(Material.CLOTH,200,5,1,160, Phases.OnlySolid, ThermalReaction.WoodBurn), //Slow burn, turn to ash. Guessing on temperature
		COCOA(Material.VEGETATION,4,5,0,59, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BLOOD(Material.LIQUID,1,100,0,102, "strong salty", new Phases(27000, 37400), ThermalReaction.None), //Melt, boil
		BONE(Material.ROCK,1,100,5,160, Phases.OnlySolid, new ThermalReaction(130000, 0)), //Half dries out/evaporates, half changes to Hydroxylapatite. TODO
		COAL(Material.ROCK,1,50,1,180, "chalky", Phases.OnlySolid, new ThermalReaction(85000, new ThermalReaction.TRResult(10, "ASH"))), //Burns, tiny bit of ash remains (1%?)
		LAMPOIL(Material.LIQUID,"LAMP OIL",1,10,1,88, "light oily", new Phases(27000, 57000), new ThermalReaction(57000)), //Burns, no real remains
		//POISON(Material.LIQUID,1,1,1,100), //uh
		LIQUOR(Material.LIQUID,10,1,1,79, "alcohol", new Phases(15900, 35200), new ThermalReaction(63500)), //Ethanol. Burns as a gas
		SUGAR(Material.VEGETATION,1,50,1,1600, Phases.OnlySolid, new ThermalReaction(46000, 0, new ThermalReaction.TRResult(1000, "CARAMEL"))), //Decomposes to caramel liquid
		HONEY(Material.LIQUID,1,50,1,1600, new Phases(26500, Integer.MAX_VALUE), new ThermalReaction(33000, 0, new ThermalReaction.TRResult(1000, "CARAMEL"))), //Melts, then decomposes to caramel
		CARAMEL(Material.LIQUID,1,0,1,1600, new Phases(26500, 80000), ThermalReaction.None), //Pure guess on phases.
		BARLEY(Material.VEGETATION,1,20,1,610, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		MUSHROOMS(Material.VEGETATION,1,20,1,50, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		HERBS(Material.VEGETATION,1,10,1,77, "fresh herbal", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		VINE(Material.VEGETATION,1,10,1,88, "rich green", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		FLOWERS(Material.VEGETATION,1,10,1,72, "nice floral", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		//PLASTIC(Material.PLASTIC,25,0,4,950),
		//RUBBER(Material.PLASTIC,25,0,1,1506, "sour rubbery"), //cis-1,4-polyisoprene. Latex concentrate. Not sure how to handle best.
		EBONY(Material.WOODEN,5,5,5,2910, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		IVORY(Material.ROCK,5,5,3,1840, Phases.OnlySolid, new ThermalReaction(100000,0, new ThermalReaction.TRResult(1000,"LIMESTONE"))), //Dries out to calcium carbonate then burn to quicklime
		WAX(Material.FLESH,1,0,0,900, new Phases(34000, 40000), new ThermalReaction(38000,ThermalReaction.SlowBurn)), //Burn slowly to nothing
		NUTS(Material.VEGETATION,0,20,0,640, "mild nutty", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BREAD(Material.VEGETATION,3,0,0,660, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		CRACKER(Material.VEGETATION,2,0,0,200, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		YEW(Material.WOODEN,15,2,5,850, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		//DUST(Material.ROCK,0,20,0,1120, "dusty"),
		PIPEWEED(Material.VEGETATION,3,10,1,320, "strong grassy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		//ENERGY(Material.ENERGY,30,0,4,0),
		STRAWBERRIES(Material.VEGETATION,10,1,1,75, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BLUEBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		RASPBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BOYSENBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BLACKBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		SMURFBERRIES(Material.VEGETATION,10,1,1,750, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		PEACHES(Material.VEGETATION,10,1,1,700, "peachy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		PLUMS(Material.VEGETATION,10,1,1,710, "sweey plumy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		ONIONS(Material.VEGETATION,10,1,1,760, "stinging oniony", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		CHERRIES(Material.VEGETATION,10,1,1,810, "cherry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		GARLIC(Material.VEGETATION,10,1,1,815, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		PINEAPPLES(Material.VEGETATION,10,1,1,500, "fruity", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		COCONUTS(Material.VEGETATION,10,1,2,250, Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		BANANAS(Material.VEGETATION,10,1,1,790, "pungent banana", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		LIMES(Material.VEGETATION,10,1,1,690, "citrusy", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		//SAP(Material.LIQUID,10,1,1,1600, "strong maply"), //Water+Sugar+Others
		ONYX(Material.PRECIOUS,70,1,8,2650, new Phases(190000, 250000), ThermalReaction.ToSilica),
		/*
		TURQUIOSE(Material.PRECIOUS,70,1,8,2850),
		PERIDOT(Material.PRECIOUS,65,1,6,3300),
		
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
		BAMBOO(Material.WOODEN,15,10,4,120),
		SOAP(Material.VEGETATION,1,0,1,430, "light fragrant"),
		SPIDERSTEEL(Material.CLOTH,150,0,2,630),
		
		PERFUME(Material.LIQUID,1,1,1,100, "strong fragrant"),
		ATLANTITE(Material.MITHRIL,200,1,6,85),
		CHEESE(Material.VEGETATION,25,0,1,640, "mild "),
		BEANS(Material.VEGETATION,1,15,1,750),
		DRAGONBLOOD(Material.LIQUID,40,20,1,3000, "mild salty"),
		DRAGONMEAT(Material.FLESH,10,100,0,102, "mild salty"),
		RUBY(Material.PRECIOUS,200,2,9,400),
		SAPPHIRE(Material.PRECIOUS,175,2,5,395),
		EMERALD(Material.PRECIOUS,150,2,5,276),
		*/
		QUARTZ(Material.PRECIOUS,25,1,5,2650, new Phases(202000, 270000), ThermalReaction.None), //Guessing
		ASH(Material.VEGETATION,1,0,0,50, "dusty", Phases.OnlySolid, ThermalReaction.None), //TODO
		SALMON(Material.FLESH,6,1,1,1000, "strong fishy", Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		CARP(Material.FLESH,6,1,1,1000, "strong fishy", Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		TROUT(Material.FLESH,6,1,1,1000, "strong fishy", Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		SHRIMP(Material.FLESH,6,1,1,1000, "mild fishy", Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		TUNA(Material.FLESH,6,1,1,1000, "strong fishy", Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		CATFISH(Material.FLESH,6,1,1,1000, "strong fishy", Phases.OnlySolid, ThermalReaction.MeatBurn), //Burn to ash/charcoal
		CRANBERRIES(Material.VEGETATION, 10, 1,  1,  75, "sweet berry", Phases.OnlySolid, ThermalReaction.WoodBurn), //Burn to ash/charcoal
		QUICKLIME(Material.ROCK,8,0,2,3350, new Phases(284500, 312300), ThermalReaction.None), //Reacts with water. TODO
		MULLITE(Material.ROCK,100,0,6,3200, new Phases(210000, 270000), ThermalReaction.None), //Entirely guessing on a lot of this
		;
		public final Material material;
		protected final String name;
		public final int value;
		public final int frequency;
		public final int hardness;
		public final int density;
		//Temperatures roughly in Kelvins.
		public final Phases phases;
		public final ThermalReaction reaction;
		public final String smell;
		private Resource(Material m, int value, int frequency, int hardness, int density, Phases phases, ThermalReaction reaction)
		{
			material=m;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.name=null;
			this.smell=null;
			this.phases=phases;
			this.reaction=reaction;
		}
		private Resource(Material m, String altName, int value, int frequency, int hardness, int density, Phases phases, ThermalReaction reaction)
		{
			material=m;
			name=altName;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.smell=null;
			this.phases=phases;
			this.reaction=reaction;
		}
		private Resource(Material m, int value, int frequency, int hardness, int density, String smell, Phases phases, ThermalReaction reaction)
		{
			material=m;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.smell=smell;
			this.name=null;
			this.phases=phases;
			this.reaction=reaction;
		}
		private Resource(Material m, String altName, int value, int frequency, int hardness, int density, String smell, Phases phases, ThermalReaction reaction)
		{
			material=m;
			name=altName;
			this.value=value;
			this.frequency=frequency;
			this.hardness=hardness;
			this.density=density;
			this.smell=smell;
			this.phases=phases;
			this.reaction=reaction;
		}
		public String toString()
		{
			if(name!=null) return name;
			return super.toString();
		}
	}

	public static class Phases
	{
		public static final int PHASE_SOLID = 0;
		public static final int PHASE_LIQUID = 1;
		public static final int PHASE_GAS = 2;
		
		public final int melting;
		public final int boiling;
		public Phases(int melt, int boil)
		{
			melting = melt;
			boiling = boil;
		}
		
		public static Phases OnlySolid = new Phases(Integer.MAX_VALUE, Integer.MAX_VALUE);
		public static Phases OnlyLiquid = new Phases(0, Integer.MAX_VALUE);
		public static Phases OnlyGas = new Phases(0, 0);
	}
	public static class ThermalReaction
	{
		public static final int Explosion = 20000;
		public static final int TypicalBurn = 1000;
		public static final int SlowBurn = 200;
		public static final int Endothermic = -400;
		
		public static class TRResult
		{
			public static HashSet<TRResult> needsInit = new HashSet<TRResult>();
			public static void initAll()
			{
				for(TRResult result : needsInit)
					result.init();
				
				//Log.instance().sysOut("ThermalReaction","Compiled reactions.");
				needsInit.clear();
				needsInit = null;
			}
			
			public final int amount; //1000 = same volume as original
			public Object type; //String when started, Material when inited
			
			public TRResult(int amount, String type)
			{
				this.amount = amount;
				this.type = type;
				//Log.instance().sysOut("TRResult","New "+type+" reaction.");
				if(needsInit!=null)
					needsInit.add(this);
			}
			public void init()
			{
				this.type = Resource.valueOf((String)type);
			}
			//static { initAll(); }
		}
		
		public final int speed; //A combination of actual reaction speed and endo/exothermic
		public final int temperature;
		public final TRResult[] results;
		
		public ThermalReaction(int temperature, TRResult... results)
		{
			speed = TypicalBurn;
			this.temperature = temperature;
			this.results=results;
		}
		public ThermalReaction(int temperature, int speed, TRResult... results)
		{
			this.temperature = temperature;
			this.speed = speed;
			this.results=results;
		}
		public ThermalReaction scaleResult(int outOf1000)
		{
			TRResult[] newResults = new TRResult[results.length];
			for(int i=0; i<results.length; i++)
			{
				TRResult part = results[i];
				newResults[i] = new TRResult(part.amount * outOf1000/1000, (String)part.type);
			}
			return new ThermalReaction(temperature, speed, newResults);
		}
		
		public final static ThermalReaction None = new ThermalReaction(Integer.MAX_VALUE);
		public final static ThermalReaction WoodBurn = new ThermalReaction(62000, new TRResult(10, "ASH"));
		public final static ThermalReaction MeatBurn = new ThermalReaction(103000, ThermalReaction.SlowBurn, new TRResult(10, "ASH"));
		public final static ThermalReaction ToQuickLime = new ThermalReaction(110000, 0, new TRResult(453, "QUICKLIME")); //Calculated for CaCO3
		public final static ThermalReaction ToSilica = new ThermalReaction(190000, 0, new TRResult(1000, "QUARTZ"));
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