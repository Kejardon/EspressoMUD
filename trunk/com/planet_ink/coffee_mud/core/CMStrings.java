package com.planet_ink.coffee_mud.core;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//Most of this file is still TODO because of colors. Also String expressions are for what?
@SuppressWarnings("unchecked")
public class CMStrings
{
	private CMStrings(){super();}
	private static CMStrings inst=new CMStrings();
	public static CMStrings instance(){return inst;}
	
	public final static String SPACES="                                                                     ";

	//return index to next line after provided line
	public static int skipNextLine(CharSequence buf, int index)
	{
		if((buf==null)||(index>=buf.length())) return index;
		for(int stop=buf.length();index<stop;index++)
		{
			if((index<stop-1)&&
			   (((buf.charAt(index)=='\n')&&(buf.charAt(index+1)=='\r'))||((buf.charAt(index)=='\r')&&(buf.charAt(index+1)=='\n'))))
				return index+2;
			else if((buf.charAt(index)=='\r')||(buf.charAt(index)=='\n'))
				return index+1;
		}
		return index;
	}
	//Get the next line starting at index[0], set index[0] to the start of the next line, return the string
	public static String getNextLine(StringBuffer buf, int[] index)
	{
		if((buf==null)||(index[0]>=buf.length())) return null;
		int stop=buf.length();
		for(int i=index[0];i<stop;i++)
		{
			if((i<stop-1)&&
			   (((buf.charAt(i)=='\n')&&(buf.charAt(i+1)=='\r'))||((buf.charAt(i)=='\r')&&(buf.charAt(i+1)=='\n'))))
			{
				String S=buf.substring(index[0], i);
				index[0]=i+2;
				return S;
			}
			else
			if((buf.charAt(i)=='\r')||(buf.charAt(i)=='\n'))
			{
				String S=buf.substring(index[0], i);
				index[0]=i+1;
				return S;
			}
		}
		String S=buf.substring(index[0]);
		index[0]=buf.length();
		return S;
	}
	public static String repeat(String str1, int times)
	{
		if(times<=0) return "";
		StringBuffer str=new StringBuffer(str1.length()*times);
		for(int i=0;i<times;i++)
			str.append(str1);
		return str.toString();
	}
	
	public static boolean isUpperCase(String str) {
		for(int c=0;c<str.length();c++)
			if(!Character.isUpperCase(str.charAt(c)))
				return false;
		return true;
	}
	
	public static boolean isLowerCase(String str) {
		for(int c=0;c<str.length();c++)
			if(!Character.isLowerCase(str.charAt(c)))
				return false;
		return true;
	}
	
	public static String endWithAPeriod(String str)
	{
		if((str==null)||(str.length()==0)) return str;
		int x=str.length()-1;
		while((x>=0)
		&&((Character.isWhitespace(str.charAt(x)))
			||((x>0)&&((str.charAt(x)!='^')&&(str.charAt(x-1)=='^')&&((--x)>=0))))) 
				x--;
		if(x<0) return str;
		if((str.charAt(x)=='.')||(str.charAt(x)=='!')||(str.charAt(x)=='?')) 
			return str.trim()+" ";
		return str.substring(0,x+1)+". "+str.substring(x+1).trim();
	}
	
	public static String bytesToStr(byte[] b){ if(b==null) return ""; try{ return new String(b,CMProps.Strings.CHARSETINPUT.property());}catch(Exception e){return new String(b);}}
	public static byte[] strToBytes(String str){ try{ return str.getBytes(CMProps.Strings.CHARSETINPUT.property());}catch(Exception e){return str.getBytes();}}
	public static boolean isVowel(char c)
	{ return (("aeiou").indexOf(Character.toLowerCase(c))>=0);}
	
	//Scratch this. There is a String method that does it, no need to reinvent it (str.replaceAll(thisStr, withThisStr))
/*	public static String replaceAll(String str, String thisStr, String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		int i=str.lastIndexOf(thisStr);
		if(i<0) return str;
		StringBuilder newStr=new StringBuilder(str);
		while(i>=0)
		{
			newStr.
		}
		return str;
	} */
	//Nothing uses this thankfully. That's probably for the best.
	public static String replaceWord(String str, String thisStr, String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		withThisStr=withThisStr.toUpperCase();
		for(int i=str.length()-1;i>=0;i--)
		{
			if((str.charAt(i)==thisStr.charAt(0))
			&&((i==0)||(!Character.isLetter(str.charAt(i-1)))))
				if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
				&&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
				{
					String oldWord=str.substring(i,i+thisStr.length());
					if(oldWord.toUpperCase().equals(oldWord)) 
						str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
					else
					if(oldWord.toLowerCase().equals(oldWord))
						str=str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
					else
					if((oldWord.length()>0)&&(Character.isUpperCase(oldWord.charAt(0)))) 
						str=str.substring(0,i)+withThisStr.charAt(0)+withThisStr.substring(1).toLowerCase()+str.substring(i+thisStr.length());
					else
						str=str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
				}
		}
		return str;
	}
	//Not used
	public static String replaceFirstWord(String str, String thisStr, String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		withThisStr=withThisStr.toUpperCase();
		for(int i=str.length()-1;i>=0;i--)
		{
			if((str.charAt(i)==thisStr.charAt(0))
			&&((i==0)||(!Character.isLetter(str.charAt(i-1)))))
				if((str.substring(i).toLowerCase().startsWith(thisStr.toLowerCase()))
				&&((str.length()==i+thisStr.length())||(!Character.isLetter(str.charAt(i+thisStr.length())))))
				{
					String oldWord=str.substring(i,i+thisStr.length());
					if(oldWord.toUpperCase().equals(oldWord)) 
						return str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
					else
					if(oldWord.toLowerCase().equals(oldWord))
						return str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
					else
					if((oldWord.length()>0)&&(Character.isUpperCase(oldWord.charAt(0)))) 
						return str.substring(0,i)+withThisStr.charAt(0)+withThisStr.substring(1).toLowerCase()+str.substring(i+thisStr.length());
					else
						return str.substring(0,i)+withThisStr.toLowerCase()+str.substring(i+thisStr.length());
				}
		}
		return str;
	}
	//Not used
	public static String replaceFirst(String str, String thisStr, String withThisStr)
	{
		if((str==null)
		||(thisStr==null)
		||(withThisStr==null)
		||(str.length()==0)
		||(thisStr.length()==0))
			return str;
		for(int i=str.length()-1;i>=0;i--)
		{
			if(str.charAt(i)==thisStr.charAt(0))
				if(str.substring(i).startsWith(thisStr))
				{
					str=str.substring(0,i)+withThisStr+str.substring(i+thisStr.length());
					return str;
				}
		}
		return str;
	}
	//Debated for use in CharCreation. Not entirely safe for all Strings.
	public static String titleCase(String name)
	{
		if((name==null)||(name.length()==0)) return "";
		char[] c=name.toCharArray();
		int i=0;
		boolean cap=true;
		for(;i<c.length;i++)
			if(c[i]=='^')
				i++;
			else if(Character.isLetter(c[i]))
			{
				if(cap) { c[i]=Character.toUpperCase(c[i]); cap=false; }
				else c[i]=Character.toLowerCase(c[i]);
			}
			else if(Character.isWhitespace(c[i]))
				cap=true;
		return new String(c);
	}
	public static String capitalizeAndLower(String name)
	{
		if((name==null)||(name.length()==0)) return "";
		char[] c=name.toCharArray();
		int i=0;
		for(;i<c.length;i++)
			if(c[i]=='^')
				i++;
			else
			if(Character.isLetter(c[i]))
				break;
		if(i<c.length)
			c[i]=Character.toUpperCase(c[i]);
		i++;
		for(;i<c.length;i++)
			if(!Character.isLowerCase(c[i]))
				c[i]=Character.toLowerCase(c[i]);
		return new String(c);
	}
	public static String capitalizeFirstLetter(String name)
	{
		if((name==null)||(name.length()==0)) return "";
		char[] c=name.toCharArray();
		int i=0;
		for(;i<c.length;i++)
			if(c[i]=='^')
				i++;
			else
			if(Character.isLetter(c[i]))
				break;
		if(i<c.length)
			c[i]=Character.toUpperCase(c[i]);
		return new String(c).trim();
	}
	
	public static String lastWordIn(String thisStr)
	{
		int x=thisStr.lastIndexOf(' ');
		if(x>=0)
			return thisStr.substring(x+1);
		return thisStr;
	}
	
	public static String getSayFromMessage(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end);
		return null;
	}
	public static String substituteSayInMessage(String affmsg, String msg)
	{
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}

	public static boolean containsIgnoreCase(String[] strs, String str)
	{
		if((str==null)||(strs==null)) return false;
		for(int s=0;s<strs.length;s++)
			if(strs[s].equalsIgnoreCase(str))
				return true;
		return false;
	}
	
	//True if A2 has every element in A1 and is the same size.
	//This might want a flag for ignorecase, and duplicate values in A1 allow different values in A2
	public static boolean compareStringArrays(String[] A1, String[] A2)
	{
		if(((A1==null)||(A1.length==0))
		&&((A2==null)||(A2.length==0)))
			return true;
		if((A1==null)||(A2==null)) return false;
		if(A1.length!=A2.length) return false;
		for(int i=0;i<A1.length;i++)
		{
			boolean found=false;
			for(int i2=0;i2<A2.length;i2++)
				if(A1[i].equalsIgnoreCase(A2[i]))
				{ found=true; break;}
			if(!found) return false;
		}
		return true;
	}
	
	//May want a flag for ignore case
	public static boolean contains(String[] strs, String str)
	{
		if((str==null)||(strs==null)) return false;
		for(int s=0;s<strs.length;s++)
			if(strs[s].equals(str))
				return true;
		return false;
	}
	
	public static String removeColors(String str)
	{
		if(str==null) return "";
		StringBuilder buf=new StringBuilder(str.length());
		//int colorStart=-1;
		for(int i=0;i<str.length();i++)
		{
			char nextChar=str.charAt(i);
			if((nextChar=='\033')&&(i+1<str.length())&&(str.charAt(i+1)=='['))
			{
				int j=str.indexOf('m', i+2);
				if(j>0) {i=j; nextChar=(char)0;}
			}
			else if((nextChar=='^')&&(i+1<str.length()))
			{
				i++;
				switch(str.charAt(i))
				{
				case '.':
				case '0': case '1': case '2': case '3':
				case '4': case '5': case '6': case '7':
				case 'I': case 'i': case 'U': case 'u':
				case '?':
					nextChar=(char)0;
					break;
				case '[':
					int j=str.indexOf(']', i+2);
					if(j>0)
					{
						char subChar=str.charAt(i+1);
						if((subChar=='F')||(subChar=='f')||(subChar=='B')||(subChar=='b'))
						{
							i=j;
							nextChar=(char)0;
						}
					}
					break;
				case '^':
					buf.append('^');
				}
			}
			if(nextChar!=(char)0)
				buf.append(nextChar);
		}
		return buf.toString();
	}
	public static int lengthMinusColors(String str)
	{
		if(str==null) return 0;
		int size=0;
		for(int i=0;i+1<str.length();i++)
		{
			char nextChar=str.charAt(i);
			if((nextChar=='\033')&&(i+1<str.length())&&(str.charAt(i+1)=='['))
			{
				int j=str.indexOf('m', i+2);
				if(j>0) {i=j; nextChar=(char)0;}
			}
			else if((nextChar=='^')&&(i+1<str.length()))
			{
				i++;
				switch(str.charAt(i))
				{
				case '.':
				case '0': case '1': case '2': case '3':
				case '4': case '5': case '6': case '7':
				case 'I': case 'i': case 'U': case 'u':
				case '?':
					nextChar=(char)0;
					break;
				case '[':
					int j=str.indexOf(']', i+2);
					if(j>0)
					{
						char subChar=str.charAt(i+1);
						if((subChar=='F')||(subChar=='f')||(subChar=='B')||(subChar=='b'))
						{
							i=j;
							nextChar=(char)0;
						}
					}
					break;
				}
			}
			if(nextChar!=(char)0)
				size++;
		}
		return size;
	}
	public static String clipToIgnoreColors(String str, int clipTo, boolean keepEndColors)
	{
		if(str==null) return "";
		if(str.length()<clipTo) return str;
		int i=0;
		for(;i<str.length();i++)
		{
			char nextChar=str.charAt(i);
			if((nextChar=='\033')&&(i+1<str.length())&&(str.charAt(i+1)=='['))
			{
				int j=str.indexOf('m', i+2);
				if(j>0) {i=j; nextChar=(char)0;}
			}
			else if((nextChar=='^')&&(i+1<str.length()))
			{
				i++;
				switch(str.charAt(i))
				{
				case '.':
				case '0': case '1': case '2': case '3':
				case '4': case '5': case '6': case '7':
				case 'I': case 'i': case 'U': case 'u':
				case '?':
					nextChar=(char)0;
					break;
				case '[':
					int j=str.indexOf(']', i+2);
					if(j>0)
					{
						char subChar=str.charAt(i+1);
						if((subChar=='F')||(subChar=='f')||(subChar=='B')||(subChar=='b'))
						{
							i=j;
							nextChar=(char)0;
						}
					}
					break;
				}
			}
			if((nextChar!=(char)0)&&(--clipTo<=0))
			{
				if(keepEndColors)
				{
					StringBuilder buf=new StringBuilder(str.length());
					buf.append(str, 0, i);
					for(;i<str.length();i++)
					{
						nextChar=str.charAt(i);
						if((nextChar=='\033')&&(i+1<str.length())&&(str.charAt(i+1)=='['))
						{
							int j=str.indexOf('m', i+2);
							if(j>0) {buf.append(str, i, j+1); i=j;}
						}
						else if((nextChar=='^')&&(i+1<str.length()))
						{
							i++;
							switch(str.charAt(i))
							{
							case '.':
							case '0': case '1': case '2': case '3':
							case '4': case '5': case '6': case '7':
							case 'I': case 'i': case 'U': case 'u':
							case '?':
								buf.append(str, i-1, i+1);
								break;
							case '[':
								int j=str.indexOf(']', i+2);
								if(j>0)
								{
									i=j;
									buf.append(str, i-1, j+1);
								}
								break;
							}
						}
					}
				}
				else
					return str.substring(0, i);
			}
		}
		return str;
	}
	
	public static Hashtable makeNumericHash(Object[] obj)
	{
		Hashtable H=new Hashtable();
		for(int i=0;i<obj.length;i++)
			H.put(obj[i],Integer.valueOf(i));
		return H;
	}
	
	public static String padCenter(String thisStr, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return clipToIgnoreColors(thisStr, thisMuch, false);
		int size=(thisMuch-lenMinusColors)/2;
		int rest=thisMuch-lenMinusColors-size;
		if(rest<0) rest=0;
		return SPACES.substring(0,size)+thisStr+SPACES.substring(0,rest);
	}
	public static String padLeft(String thisStr, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return clipToIgnoreColors(thisStr, thisMuch, false);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}
	public static String padLeft(String thisStr, String colorPrefix, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return colorPrefix+clipToIgnoreColors(thisStr, thisMuch, false);
		return SPACES.substring(0,thisMuch-lenMinusColors)+colorPrefix+thisStr;
	}
	public static String padRight(String thisStr, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return clipToIgnoreColors(thisStr, thisMuch, false);
		return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
	}
	public static String limit(String thisStr, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return clipToIgnoreColors(thisStr, thisMuch, false);
		return thisStr;
	}
	public static String padRight(String thisStr, String colorSuffix, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return clipToIgnoreColors(thisStr, thisMuch, false)+colorSuffix;
		return thisStr+colorSuffix+SPACES.substring(0,thisMuch-lenMinusColors);
	}
	public static String padRightPreserve(String thisStr, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		return thisStr+SPACES.substring(0,thisMuch-lenMinusColors);
	}
	public static String centerPreserve(String thisStr, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		int left=(thisMuch-lenMinusColors)/2;
		int right=((left+left+lenMinusColors)<thisMuch)?left+1:left;
		return SPACES.substring(0,left)+thisStr+SPACES.substring(0,right);
	}
	public static String padLeftPreserve(String thisStr, int thisMuch)
	{
		int lenMinusColors=lengthMinusColors(thisStr);
		if(lenMinusColors>thisMuch)
			return removeColors(thisStr);
		return SPACES.substring(0,thisMuch-lenMinusColors)+thisStr;
	}
	
	public static String sameCase(String str, char c)
	{
		if(Character.isUpperCase(c))
			return str.toUpperCase();
		return str.toLowerCase();
	}

	// states: 0 = done after this one,-1 = done a char ago,-2 = eat & same state,-99 = error,
	// chars: 254 = digit, 253 = letter, 252 = digitNO0, 255=eof
	private static final int[][]	STRING_EXP_SM	= { { -1 }, // 0 == done after this one, 1 == real first state
			{ ' ', -2, '=', 2, '>', 4, '<', 5, '!', 2, '(', 0, ')', 0, '\"', 3, '+', 0, '-', 0, '*', 0, '/', 0, '&', 6, '?',0, '|', 7, '\'', 8, '`', 9, '$', 10, 253, 12, 252, 13, '0', 15, 255, 255, -99 }, // 1
			{ '=', 0, -1 }, // 2 -- starts with =
			{ '\"', 0, 255, -99, 3 }, // 3 -- starts with "
			{ '=', 0, '>', 0, -1 }, // 4 -- starts with <
			{ '=', 0, '<', 0, -1 }, // 5 -- starts with >
			{ '&', 0, -1 }, // 6 -- starts with &
			{ '|', 0, -1 }, // 7 -- starts with |
			{ '\'', 0, 255, -99, 8 }, // 8 -- starts with '
			{ '`', 0, 255, -99, 9 }, // 9 -- starts with `
			{ 253, 11, '_', 11, -99 }, // 10 == starts with $
			{ 253, 11, 254, 11, '_', 11, 255, -1, -1 }, // 11=starts $Letter
			{ 253, 12, 255, -1, -1 },				// 12=starts with letter
			{ 254, 13, '.', 14, -1}, // 13=starts with a digit
			{ 254, 14, '.', -99, -1}, // 14=continues a digit
			{ 254, -99, '.', 14, -1} // 15=starts with a 0
	};

	private static class StringExpToken
	{
		public int		type	= -1;
		public String	value	= "";
		public double numValue  = 0.0;

		public static StringExpToken token(int type, String value) throws Exception
		{
			StringExpToken token = new StringExpToken();
			token.type = type;
			token.value = value;
			if((value.length()>0)&&(Character.isDigit(value.charAt(0))))
				token.numValue = Double.parseDouble(value);
			return token;
		}
		private StringExpToken() { }
	}

	private static StringExpToken nextToken(Vector tokens, int[] index) {
		if(index[0]>=tokens.size()) return null;
		return (StringExpToken)tokens.elementAt(index[0]++);
	}
	
	private static final int	STRING_EXP_TOKEN_EVALUATOR	= 1;
	private static final int	STRING_EXP_TOKEN_OPENPAREN	= 2;
	private static final int	STRING_EXP_TOKEN_CLOSEPAREN	= 3;
	private static final int	STRING_EXP_TOKEN_WORD		= 4;
	private static final int	STRING_EXP_TOKEN_STRCONST	= 5;
	private static final int	STRING_EXP_TOKEN_COMBINER	= 6;
	private static final int	STRING_EXP_TOKEN_NOT		= 7;
	private static final int	STRING_EXP_TOKEN_NUMCONST	= 8;
	private static final int	STRING_EXP_TOKEN_UKNCONST	= 9;

	private static StringExpToken makeTokenType(String token, Hashtable variables, boolean emptyVars) throws Exception
	{
		if ((token == null)||(token.length()==0))
			return null;
		if (token.startsWith("\""))
			return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, token.substring(1, token.length() - 1));
		if (token.startsWith("\'"))
			return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, token.substring(1, token.length() - 1));
		if (token.startsWith("`"))
			return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, token.substring(1, token.length() - 1));
		if (token.equals("("))
			return StringExpToken.token(STRING_EXP_TOKEN_OPENPAREN, token);
		if (token.equals(")"))
			return StringExpToken.token(STRING_EXP_TOKEN_CLOSEPAREN, token);
		if (token.equalsIgnoreCase("IN"))
			return StringExpToken.token(STRING_EXP_TOKEN_EVALUATOR, token);
		if (token.equals("+")||token.equals("-")||token.equals("*")||token.equals("/")||token.equals("?"))
			return StringExpToken.token(STRING_EXP_TOKEN_COMBINER, token);
		if (token.equals("!")||token.equalsIgnoreCase("NOT"))
			return StringExpToken.token(STRING_EXP_TOKEN_NOT, token);
		if(Character.isDigit(token.charAt(0)))
			return StringExpToken.token(STRING_EXP_TOKEN_NUMCONST, token);
		if (token.startsWith("$"))
		{
			token = token.substring(1);
			Object value = variables.get(token);
			if(!(value instanceof String))
				value = variables.get(token.toUpperCase().trim());
			if((value == null)&&(emptyVars))
				value="";
			else
			if(!(value instanceof String))
				throw new Exception("Undefined variable found: $" + token);
			if((value.toString().length()>0)&&(!CMath.isNumber(value.toString())))
				return StringExpToken.token(STRING_EXP_TOKEN_STRCONST, value.toString());
			return StringExpToken.token(STRING_EXP_TOKEN_UKNCONST, value.toString());
		}
		if ((token.charAt(0) == '_') || (Character.isLetterOrDigit(token.charAt(0))) || (token.charAt(0) == '|') || (token.charAt(0) == '&'))
			return StringExpToken.token(STRING_EXP_TOKEN_WORD, token);
		return StringExpToken.token(STRING_EXP_TOKEN_EVALUATOR, token);
	}

	private static StringExpToken nextStringToken(String expression, int[] index, Hashtable variables, boolean emptyVars) throws Exception
	{
		int[] stateBlock = STRING_EXP_SM[1];
		StringBuffer token = new StringBuffer("");
		while (index[0] < expression.length())
		{
			char c = expression.charAt(index[0]);
			int nextState = stateBlock[stateBlock.length - 1];
			boolean match = false;
			for (int x = 0; x < stateBlock.length - 1; x += 2)
			{
				switch (stateBlock[x])
				{
					case 254:
						match = Character.isDigit(c);
						break;
					case 252:
						match = Character.isDigit(c)&&(c!='0');
						break;
					case 253:
						match = Character.isLetter(c);
						break;
					case 255:
						break; // nope, not yet
					default:
						match = (c == stateBlock[x]);
						break;
				}
				if (match)
				{
					nextState = stateBlock[x + 1];
					break;
				}
			}
			switch (nextState)
			{
				case 255:
					return null;
				case -99:
					throw new Exception("Illegal character in expression: " + c);
				case -2:
					index[0]++;
					break;
				case -1:
					return makeTokenType(token.toString(), variables, emptyVars);
				case 0:
				{
					token.append(c);
					index[0]++;
					return makeTokenType(token.toString(), variables, emptyVars);
				}
				default:
				{
					token.append(c);
					index[0]++;
					stateBlock = STRING_EXP_SM[nextState];
					break;
				}
			}
		}
		int finalState = stateBlock[stateBlock.length - 1];
		for (int x = 0; x < stateBlock.length - 1; x += 2)
			if (stateBlock[x] == 255)
			{
				finalState = stateBlock[x + 1];
				break;
			}
		switch (finalState)
		{
			case -99:
				throw new Exception("Expression ended prematurely");
			case -1:
			case 0:
				return makeTokenType(token.toString(), variables, emptyVars);
			default:
				return null;
		}
	}
	
	

	/*
	 * case STRING_EXP_TOKEN_EVALUATOR: case STRING_EXP_TOKEN_OPENPAREN: case STRING_EXP_TOKEN_CLOSEPAREN: case STRING_EXP_TOKEN_WORD: case
	 * STRING_EXP_TOKEN_CONST: case STRING_EXP_TOKEN_COMBINER:
	 */
	public static String matchSimpleConst(Vector tokens, int[] index, Hashtable variables) throws Exception
	{
		int[] i = (int[]) index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if((token.type != STRING_EXP_TOKEN_STRCONST)
		&& (token.type != STRING_EXP_TOKEN_UKNCONST))
			return null;
		index[0] = i[0];
		return token.value;
	}

	public static Double matchSimpleNumber(Vector tokens, int[] index, Hashtable variables) throws Exception
	{
		int[] i = (int[]) index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if((token.type != STRING_EXP_TOKEN_NUMCONST)
		&& (token.type != STRING_EXP_TOKEN_UKNCONST))
			return null;
		index[0] = i[0];
		return Double.valueOf(token.numValue);
	}
	
	public static String matchCombinedString(Vector tokens, int[] index, Hashtable variables) throws Exception
	{
		int[] i = (int[]) index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			String testInside = matchCombinedString(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = (int[]) index.clone();
		String leftValue = matchSimpleConst(tokens, i, variables);
		if (leftValue == null)
			return null;
		int[] i2 = (int[]) i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != STRING_EXP_TOKEN_COMBINER))
		{
			index[0] = i[0];
			return leftValue;
		}
		if(!token.value.equals("+")) 
			throw new Exception("Can't combine a string using '"+token.value+"'");
		i[0] = i2[0];
		String rightValue = matchCombinedString(tokens, i, variables);
		if (rightValue == null)
			return null;
		index[0] = i[0];
		return leftValue + rightValue;
	}

	public static Double matchCombinedNum(Vector tokens, int[] index, Hashtable variables) throws Exception
	{
		int[] i = (int[]) index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			Double testInside = matchCombinedNum(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = (int[]) index.clone();
		Double leftValue = matchSimpleNumber(tokens, i, variables);
		if (leftValue == null)
			return null;
		int[] i2 = (int[]) i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != STRING_EXP_TOKEN_COMBINER))
		{
			index[0] = i[0];
			return leftValue;
		}
		i[0] = i2[0];
		Double rightValue = matchCombinedNum(tokens, i, variables);
		if (rightValue == null)
			return null;
		index[0] = i[0];
		if(token.value.equals("+"))
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() + rightValue.doubleValue());
		}
		else
		if(token.value.equals("-")) 
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() - rightValue.doubleValue());
		}
		else
		if(token.value.equals("*")) 
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() * rightValue.doubleValue());
		}
		else
		if(token.value.equals("/")) 
		{
			index[0] = i[0];
			return Double.valueOf(leftValue.doubleValue() / rightValue.doubleValue());
		}
		else
		if(token.value.equals("?")) 
		{
			index[0] = i[0];
			return Double.valueOf(Math.round((Math.random() * (rightValue.doubleValue()-leftValue.doubleValue())) + leftValue.doubleValue())) ;
		}
		else
			throw new Exception("Unknown math combiner "+token.value);
	}
	
	public static Boolean matchStringEvaluation(Vector tokens, int[] index, Hashtable variables) throws Exception
	{
		int[] i = (int[]) index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if(token.type == STRING_EXP_TOKEN_NOT)
		{
			Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				return new Boolean(!testInside.booleanValue());
			}
		}
		else
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			Boolean testInside = matchStringEvaluation(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = (int[]) index.clone();
		String leftValue = matchCombinedString(tokens, i, variables);
		if (leftValue == null)
			return null;
		token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type != STRING_EXP_TOKEN_EVALUATOR)
			return null;
		String rightValue = matchCombinedString(tokens, i, variables);
		if (rightValue == null)
			return null;
		int compare = leftValue.compareToIgnoreCase(rightValue);
		Boolean result = null;
		if (token.value.equals(">"))
			result = new Boolean(compare > 0);
		else if (token.value.equals(">="))
			result = new Boolean(compare >= 0);
		else if (token.value.equals("<"))
			result = new Boolean(compare < 0);
		else if (token.value.equals("<="))
			result = new Boolean(compare <= 0);
		else if (token.value.equals("="))
			result = new Boolean(compare == 0);
		else if (token.value.equals("!="))
			result = new Boolean(compare != 0);
		else if (token.value.equals("<>"))
			result = new Boolean(compare != 0);
		else if (token.value.equals("><"))
			result = new Boolean(compare != 0);
		else
		if (token.value.equalsIgnoreCase("IN"))
			result = new Boolean(rightValue.toUpperCase().indexOf(leftValue.toUpperCase())>=0);
		else
			return null;
		index[0] = i[0];
		return result;
	}

	public static Boolean matchNumEvaluation(Vector tokens, int[] index, Hashtable variables) throws Exception
	{
		int[] i = (int[]) index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		if(token.type == STRING_EXP_TOKEN_NOT)
		{
			Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				return new Boolean(!testInside.booleanValue());
			}
		}
		else
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			Boolean testInside = matchNumEvaluation(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				return testInside;
			}
		}
		i = (int[]) index.clone();
		Double leftValue = matchCombinedNum(tokens, i, variables);
		if (leftValue == null)
			return null;
		token = nextToken(tokens, i);
		if (token == null)
			return null;
		if (token.type != STRING_EXP_TOKEN_EVALUATOR)
			return null;
		Double rightValue = matchCombinedNum(tokens, i, variables);
		if (rightValue == null)
			return null;
		Boolean result = null;
		if (token.value.equals(">"))
			result = new Boolean(leftValue.doubleValue() > rightValue.doubleValue());
		else if (token.value.equals(">="))
			result = new Boolean(leftValue.doubleValue() >= rightValue.doubleValue());
		else if (token.value.equals("<"))
			result = new Boolean(leftValue.doubleValue() < rightValue.doubleValue());
		else if (token.value.equals("<="))
			result = new Boolean(leftValue.doubleValue() <= rightValue.doubleValue());
		else if (token.value.equals("="))
			result = new Boolean(leftValue.doubleValue() == rightValue.doubleValue());
		else if (token.value.equals("!="))
			result = new Boolean(leftValue.doubleValue() != rightValue.doubleValue());
		else if (token.value.equals("<>"))
			result = new Boolean(leftValue.doubleValue() != rightValue.doubleValue());
		else if (token.value.equals("><"))
			result = new Boolean(leftValue.doubleValue() != rightValue.doubleValue());
		else
		if (token.value.equalsIgnoreCase("IN"))
			throw new Exception("Can't use IN operator on numbers.");
		else
			return null;
		index[0] = i[0];
		return result;
	}
	
	public static Boolean matchExpression(Vector tokens, int[] index, Hashtable variables) throws Exception
	{
		int[] i = (int[]) index.clone();
		StringExpToken token = nextToken(tokens, i);
		if (token == null)
			return null;
		Boolean leftExpression = null;
		if(token.type == STRING_EXP_TOKEN_NOT)
		{
			Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				index[0] = i[0];
				leftExpression = new Boolean(!testInside.booleanValue());
			}
		}
		else
		if (token.type == STRING_EXP_TOKEN_OPENPAREN)
		{
			Boolean testInside = matchExpression(tokens, i, variables);
			if (testInside != null)
			{
				token = nextToken(tokens, i);
				if (token.type != STRING_EXP_TOKEN_CLOSEPAREN)
					return null;
				index[0] = i[0];
				leftExpression = testInside;
			}
		}
		if(leftExpression == null)
		{
			i = (int[]) index.clone();
			leftExpression = matchStringEvaluation(tokens, i, variables);
			if(leftExpression == null) leftExpression = matchNumEvaluation(tokens, i, variables);
		}
		if (leftExpression == null) return null;
		int[] i2 = (int[]) i.clone();
		token = nextToken(tokens, i2);
		if ((token == null) || (token.type != STRING_EXP_TOKEN_WORD))
		{
			index[0] = i[0];
			return leftExpression;
		}
		i[0] = i2[0];
		Boolean rightExpression = matchExpression(tokens, i, variables);
		if (rightExpression == null)
			return null;
		Boolean result = null;
		if (token.value.equalsIgnoreCase("AND"))
			result = new Boolean(leftExpression.booleanValue() && rightExpression.booleanValue());
		else if (token.value.startsWith("&"))
			result = new Boolean(leftExpression.booleanValue() && rightExpression.booleanValue());
		else if (token.value.startsWith("|"))
			result = new Boolean(leftExpression.booleanValue() || rightExpression.booleanValue());
		else if (token.value.equalsIgnoreCase("OR"))
			result = new Boolean(leftExpression.booleanValue() || rightExpression.booleanValue());
		else if (token.value.equalsIgnoreCase("XOR"))
			result = new Boolean(leftExpression.booleanValue() != rightExpression.booleanValue());
		else
			throw new Exception("Parse Exception: Illegal expression evaluation combiner: " + token.value);
		index[0] = i[0];
		return result;
	}

	public static int versionCheck(String currentVer, int[] targetVer)
	{
		for(int i=0;i<targetVer.length;i++)
		{
			int index=currentVer.indexOf(".");
			if(index==-1) index=currentVer.length();
			int comp=targetVer[i]-CMath.s_int(currentVer.substring(0,index));
			if(comp>0) return -1;
			else if(comp<0) return 1;
			if(index==currentVer.length()) return (i+1==targetVer.length?0:-1);
			currentVer=currentVer.substring(index+1);
			if(currentVer.length()==0) return (i+1==targetVer.length?0:-1);
		}
		return -1;
	}

	public static boolean parseStringExpression(String expression, Hashtable variables, boolean emptyVarsOK) throws Exception
	{
		Vector tokens = new Vector();
		int[] i = { 0 };
		StringExpToken token = nextStringToken(expression,i,variables, emptyVarsOK);
		while(token != null) {
			tokens.addElement(token);
			token = nextStringToken(expression,i,variables, emptyVarsOK);
		}
		if(tokens.size()==0) return true;
		i = new int[]{ 0 };
		Boolean value = matchExpression(tokens, i, variables);
		if (value == null) throw new Exception("Parse error on following statement: " + expression);
		return value.booleanValue();
	}
}
