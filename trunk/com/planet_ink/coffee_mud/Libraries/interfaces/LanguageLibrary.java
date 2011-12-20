package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface LanguageLibrary extends CMLibrary
{
//	public void clear();
//	public DVector getLanguageParser(String parser);
//	public String finalTranslation(String item);
//	public String sessionTranslation(String item);
//	public String filterTranslation(String item);
//	public DVector getLanguageTranslator(String parser);
//	public void setLocale(String lang, String state);
	public Vector preCommandParser(Vector CMDS);
//	public String preItemParser(String item);
//	public String failedItemParser(String item);
	
	public String[][] ISO_LANG_CODES = {
		{"AA","Afar"},
		{"AB","Abkhazian"},
		{"AF","Afrikaans"},
		{"AM","Amharic"},
		{"AR","Arabic"},
		{"AS","Assamese"},
		{"AY","Aymara"},
		{"AZ","Azerbaijani"},
		{"BA","Bashkir"},
		{"BE","Byelorussian"},
		{"BG","Bulgarian"},
		{"BH","Bihari"},
		{"BI","Bislama"},
		{"BN","Bengali"},
		{"BO","Tibetan"},
		{"BR","Breton"},
		{"CA","Catalan"},
		{"CO","Corsican"},
		{"CS","Czech"},
		{"CY","Welsh"},
		{"DA","Danish"},
		{"DE","German"},
		{"DZ","Bhutani"},
		{"EL","Greek"},
		{"EN","English"},
		{"EO","Esperanto"},
		{"ES","Spanish"},
		{"ET","Estonian"},
		{"EU","Basque"},
		{"FA","Persian"},
		{"FI","Finnish"},
		{"FJ","Fiji"},
		{"FO","Faeroese"},
		{"FR","French"},
		{"FY","Frisian"},
		{"GA","Irish"},
		{"GD","Gaelic"},
		{"GL","Galician"},
		{"GN","Guarani"},
		{"GU","Gujarati"},
		{"HA","Hausa"},
		{"HI","Hindi"},
		{"HR","Croatian"},
		{"HU","Hungarian"},
		{"HY","Armenian"},
		{"IA","Interlingua"},
		{"IE","Interlingue"},
		{"IK","Inupiak"},
		{"IN","Indonesian"},
		{"IS","Icelandic"},
		{"IT","Italian"},
		{"IW","Hebrew"},
		{"JA","Japanese"},
		{"JI","Yiddish"},
		{"JW","Javanese"},
		{"KA","Georgian"},
		{"KK","Kazakh"},
		{"KL","Greenlandic"},
		{"KM","Cambodian"},
		{"KN","Kannada"},
		{"KO","Korean"},
		{"KS","Kashmiri"},
		{"KU","Kurdish"},
		{"KY","Kirghiz"},
		{"LA","Latin"},
		{"LN","Lingala"},
		{"LO","Laothian"},
		{"LT","Lithuanian"},
		{"LV","Latvian"},
		{"MG","Malagasy"},
		{"MI","Maori"},
		{"MK","Macedonian"},
		{"ML","Malayalam"},
		{"MN","Mongolian"},
		{"MO","Moldavian"},
		{"MR","Marathi"},
		{"MS","Malay"},
		{"MT","Maltese"},
		{"MY","Burmese"},
		{"NA","Nauru"},
		{"NE","Nepali"},
		{"NL","Dutch"},
		{"NO","Norwegian"},
		{"OC","Occitan"},
		{"OM","Oromo"},
		{"OR","Oriya"},
		{"PA","Punjabi"},
		{"PL","Polish"},
		{"PS","Pashto"},
		{"PT","Portuguese"},
		{"QU","Quechua"},
		{"RM","Rhaeto-Romance"},
		{"RN","Kirundi"},
		{"RO","Romanian"},
		{"RU","Russian"},
		{"RW","Kinyarwanda"},
		{"SA","Sanskrit"},
		{"SD","Sindhi"},
		{"SG","Sangro"},
		{"SH","Serbo-Croatian"},
		{"SI","Singhalese"},
		{"SK","Slovak"},
		{"SL","Slovenian"},
		{"SM","Samoan"},
		{"SN","Shona"},
		{"SO","Somali"},
		{"SQ","Albanian"},
		{"SR","Serbian"},
		{"SS","Siswati"},
		{"ST","Sesotho"},
		{"SU","Sudanese"},
		{"SV","Swedish"},
		{"SW","Swahili"},
		{"TA","Tamil"},
		{"TE","Tegulu"},
		{"TG","Tajik"},
		{"TH","Thai"},
		{"TI","Tigrinya"},
		{"TK","Turkmen"},
		{"TL","Tagalog"},
		{"TN","Setswana"},
		{"TO","Tonga"},
		{"TR","Turkish"},
		{"TS","Tsonga"},
		{"TT","Tatar"},
		{"TW","Twi"},
		{"UK","Ukrainian"},
		{"UR","Urdu"},
		{"UZ","Uzbek"},
		{"VI","Vietnamese"},
		{"VO","Volapuk"},
		{"WO","Wolof"},
		{"XH","Xhosa"},
		{"YO","Yoruba"},
		{"ZH","Chinese"},
		{"ZU","Zulu"}
	};
}