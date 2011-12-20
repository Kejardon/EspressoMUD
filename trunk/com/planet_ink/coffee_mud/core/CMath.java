package com.planet_ink.coffee_mud.core;
import java.util.*;
import java.io.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
// Math library for common EspressoMUD needs
public class CMath
{
	private CMath(){super();}
	private static CMath inst=new CMath();
	public static CMath instance(){return inst;}
	private static final String[] ROMAN_HUNDREDS={"C","CC","CCC","CD","D","DC","DCC","DCCC","CM","P"};
	private static final String[] ROMAN_TENS={"X","XX","XXX","XL","L","LX","LXX","LXXX","XC","C"};
	private static final String[] ROMAN_ONES={"I","II","III","IV","V","VI","VII","VIII","IX","X"};
	private static final String   ROMAN_ALL="CDMPXLIV";
	private static final java.text.DecimalFormat twoPlaces = new java.text.DecimalFormat("0.#####%");
	private static Random rand = new Random(System.currentTimeMillis());

/*
	public static Object[] combineArrays(Object[] arrA, Object[] arrB, Object[] total)
	{
		Object[] total=new Object[arrA.length+arrB.length];
		System.arraycopy(arrA, 0, total, 0, arrA.length);
		System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
		return total;
	}
*/

	// Get a random number from a weighted curve. Returns a number between -1 and 1.
	//0 will always give -1 or 1, 1 will give an almost straight line, infinite will always give 0.
	//Something like 2 is probably ideal for most expected curves.
	public static double curvedRandom(double curve)
	{
		double val=Math.random()*Math.PI/2-Math.PI/4;
		return (val<0?-1:1)*Math.pow(Math.tan(val<0?-val:val),curve);
	}

	// Convert an integer to its Roman Numeral equivalent
	public static String convertToRoman(int i)
	{
		StringBuffer roman=new StringBuffer("");
		if(i>1000)
		{
			roman.append("Y");
			i=i%1000;
		}
		if(i>=100)
		{
			int x=i%100;
			int y=(i-x)/100;
			if(y>0)
				roman.append(ROMAN_HUNDREDS[y-1]);
			i=x;
		}
		if(i>=10)
		{
			int x=i%10;
			int y=(i-x)/10;
			if(y>0)
				roman.append(ROMAN_TENS[y-1]);
		}
		i=i%10;
		if(i>0)
			roman.append(ROMAN_ONES[i-1]);
		return roman.toString();
	}

	// Convert a number from roman numeral to integer.
	public static int convertFromRoman(String s)
	{
		int x=0;
		while(s.startsWith("Y"))
			x+=1000;
		for(int i=ROMAN_HUNDREDS.length-1;i>=0;i--)
			if(s.startsWith(ROMAN_HUNDREDS[i]))
			{
				x+=(100*(i+1));
				break;
			}
		for(int i=ROMAN_TENS.length-1;i>=0;i--)
			if(s.startsWith(ROMAN_TENS[i]))
			{
				x+=(10*(i+1));
				break;
			}
		for(int i=ROMAN_ONES.length-1;i>=0;i--)
			if(s.startsWith(ROMAN_ONES[i]))
			{
				x+=i+1;
				break;
			}
		return x;
	}

	// Return st,nd,rd,th for a number
	public static String numAppendage(int num)
	{
		if((num<11)||(num>13))
		{
			String strn=""+num;
			switch(strn.charAt(strn.length()-1))
			{
			case '1': return "st";
			case '2': return "nd";
			case '3': return "rd";
			}
		}
		return "th";
	}

	// Return true if the char is a roman numeral digit, must be capitalized
	public static boolean isRomanDigit(char c){ return ROMAN_ALL.indexOf(c)>=0;}

	// Returns true if the string is a roman numeral
	public static boolean isRomanNumeral(String s)
	{
		if(s==null) return false;
		s=s.toUpperCase().trim();
		if(s.length()==0) return false;
		for(int c=0;c<s.length();c++)
			if(!isRomanDigit(s.charAt(c)))
				return false;
		return true;
	}

	// Returns true if the string is a number (float or int)
	public static boolean isNumber(String s)
	{
		if(s==null) return false;
		s=s.trim();
		if(s.length()==0) return false;
		if((s.length()>1)&&(s.startsWith("-")))
			s=s.substring(1);
		for(int i=0;i<s.length();i++)
			if("0123456789.,".indexOf(s.charAt(i))<0)
				return false;
		return true;
	}

	/**
	 * Raises x to the y power, making sure both are cast to doubles
	 * and that the return is rounded off.
	 * @param x the base number
	 * @param y the power
	 * @return x to the y power, rounded off
	 */
	public static long pow(long x, long y)
	{
		return Math.round(Math.pow(((double)x),((double)y)));
	}
	// Returns true if the given string represents a percentage in the form X% where X is any real number.
	public static boolean isPct(String s)
	{
		if(s==null) return false;
		s=s.trim();
		if(!s.endsWith("%")) return false;
		return CMath.isNumber(s.substring(0,s.length()-1));
	}
	// Converts a string percentage to a floating point number
	public static double s_pct(String s)
	{
		if(s==null) return 0.0;
		while(s.trim().endsWith("%")) s=s.trim().substring(0,s.length()-1).trim();
		return s_double(s)/100.0;
	}
	
	// Converts a percentage 1>d>0 to a string.
	public static String toPct(double d)
	{
		String s=twoPlaces.format(d);
		if(s.endsWith("%%")) return s.substring(0,s.length()-1);
		return s;
	}

	/**
	 * Returns whether the given string is a valid
	 * math expression (5 + 7)/2, etc. Does this
	 * by evaluating the expression and returning
	 * false if an error is found.  No variables
	 * are allowed.
	 * @param st the possible math expression
	 * @return true if it is a math expression
	 */
	public static boolean isMathExpression(String st){
		if((st==null)||(st.length()==0)) return false;
		try{ parseMathExpression(st);}catch(Exception e){ return false;}
		return true;
	}
	/**
	 * Returns whether the given string is a valid
	 * math expression (@x1 + 7)/2, etc. Does this
	 * by evaluating the expression and returning
	 * false if an error is found.  All necessary
	 * variables MUST be included (@x1=vars[0])
	 * @param st the possible math expression
	 * @param vars the 0 based variables
	 * @return true if it is a math expression
	 */
	public static boolean isMathExpression(String st, double[] vars){
		if((st==null)||(st.length()==0)) return false;
		try{ parseMathExpression(st,vars);}catch(Exception e){ return false;}
		return true;
	}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variable @xx will refer to current computed value.
	 * Returns 0.0 on any parsing error
	 * @param st a full math expression string
	 * @return the result of the expression
	 */
	public static double s_parseMathExpression(String st){ try{ return parseMathExpression(st);}catch(Exception e){ return 0.0;}}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Returns 0.0 on any parsing error
	 * @param st a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public static double s_parseMathExpression(String st, double[] vars){ try{ return parseMathExpression(st,vars);}catch(Exception e){ return 0.0;}}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @return the result of the expression
	 */
	public static long s_parseLongExpression(String st){ try{ return parseLongExpression(st);}catch(Exception e){ return 0;}}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public static long s_parseLongExpression(String st, double[] vars){ try{ return parseLongExpression(st,vars);}catch(Exception e){ return 0;}}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.  
	 * Variable @xx will refer to current computed value.
	 * Round the result to an integer.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @return the result of the expression
	 */
	public static int s_parseIntExpression(String st){ try{ return parseIntExpression(st);}catch(Exception e){ return 0;}}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to an integer.
	 * Returns 0 on any parsing error
	 * @param st a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public static int s_parseIntExpression(String st, double[] vars){ try{ return parseIntExpression(st,vars);}catch(Exception e){ return 0;}}

	private static double parseMathExpression(StreamTokenizer st, boolean inParen, double[] vars, double previous)
		throws ArithmeticException
	{
		if(!inParen) {
			st.ordinaryChar('/');
			st.ordinaryChar('x');
			st.ordinaryChar('X');
		}
		double finalValue=0;
		try{
			int c=st.nextToken();
			char lastOperation='+';
			while(c!=StreamTokenizer.TT_EOF)
			{
				double curValue=0.0;
				switch(c)
				{
				case StreamTokenizer.TT_NUMBER:
					curValue=st.nval;
					break;
				case '(':
					curValue=parseMathExpression(st,true,vars,finalValue);
					break;
				case ')':
					if(!inParen)
						throw new ArithmeticException("')' is an unexpected token.");
					return finalValue;
				case '@':
				{
					c=st.nextToken();
					if((c!='x')&&(c!='X'))
						throw new ArithmeticException("'"+c+"' is an unexpected token after @.");
					c=st.nextToken();
					if((c=='x')||(c=='X'))
						curValue=previous;
					else
					{
						if(c!=StreamTokenizer.TT_NUMBER)
							throw new ArithmeticException("'"+c+"' is an unexpected token after @x.");
						if(vars==null)
							throw new ArithmeticException("vars have not been defined for @x"+st.nval);
						if((st.nval>vars.length)||(st.nval<1.0))
							throw new ArithmeticException("'"+st.nval+"/"+vars.length+"' is an illegal variable reference.");
						curValue=vars[((int)st.nval)-1];
					}
					break;
				}
				case '+':
				case '<':
				case '>':
				case '-':
				case '*':
				case '\\':
				case '/':
				case '?':
				{
					lastOperation=(char)c;
					c=st.nextToken();
					continue;
				}
				default:
					throw new ArithmeticException("'"+c+"' is an illegal expression.");
				}
				switch(lastOperation)
				{
				case '<': finalValue = finalValue < curValue? finalValue : curValue; break;
				case '>': finalValue = finalValue > curValue? finalValue : curValue; break;
				case '+': finalValue+=curValue; break;
				case '-': finalValue-=curValue; break;
				case '*': finalValue*=curValue; break;
				case '/':
				case '\\': finalValue/=curValue; break;
				case '?': finalValue= ((curValue-finalValue+0.5) * rand.nextDouble()) + finalValue; break;
				}
				c=st.nextToken();
			}
		}
		catch(IOException e){}
		if(inParen)
			throw new ArithmeticException("')' was missing from this expression");
		return finalValue;
	}

	/**
	 * A class representing a single piece of a compiled operation.  Optomized for
	 * speed of execution rather than the obvious wastefulness of storage.
	 */
	public static final class CompiledOperation
	{
		public static final int OPERATION_VARIABLE=0;
		public static final int OPERATION_VALUE=1;
		public static final int OPERATION_OPERATION=2;
		public static final int OPERATION_LIST=3;
		public static final int OPERATION_PREVIOUSVALUE=4;
		public int type = -1;
		public int variableIndex = 0;
		public double value = 0.0;
		public char operation = ' ';
		public LinkedList<CompiledOperation> list = null;
		public CompiledOperation(int variableIndex) { type = OPERATION_VARIABLE; this.variableIndex = variableIndex;}  
		public CompiledOperation(double value) { type = OPERATION_VALUE; this.value = value;}  
		public CompiledOperation(LinkedList<CompiledOperation> list) { type = OPERATION_LIST; this.list = list;}  
		public CompiledOperation(char operation) { type = OPERATION_OPERATION; this.operation = operation;}  
		public CompiledOperation() { type = OPERATION_PREVIOUSVALUE;}  
	}
	
	/**
	 * Pre-compiles an expression for faster evaluation later on.
	 * @see CMath#parseMathExpression(LinkedList, double[])
	 * @param st the math expression as a string
	 * @returns the pre-compiled expression
	 * @throws ArithmeticException
	 */
	public static LinkedList<CompiledOperation> compileMathExpression(String formula)
	{return compileMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false);}
	
	/**
	 * Pre-compiles an expression for faster evaluation later on.
	 * @see CMath#parseMathExpression(LinkedList, double[])
	 * @param st the tokenized expression
	 * @param inParen whether or not you are in parenthesis mode 
	 * @returns the pre-compiled expression
	 * @throws ArithmeticException
	 */
	private static LinkedList<CompiledOperation> compileMathExpression(StreamTokenizer st, boolean inParen)
		throws ArithmeticException
	{
		if(!inParen) {
			st.ordinaryChar('/');
			st.ordinaryChar('x');
			st.ordinaryChar('X');
		}
		LinkedList<CompiledOperation> list = new LinkedList<CompiledOperation>();
		
		try{
			int c=st.nextToken();
			char lastOperation='+';
			while(c!=StreamTokenizer.TT_EOF)
			{
				switch(c)
				{
				case StreamTokenizer.TT_NUMBER:
					list.add(new CompiledOperation(st.nval));
					break;
				case '(':
					list.add(new CompiledOperation(compileMathExpression(st,true)));
					break;
				case ')':
					if(!inParen)
						throw new ArithmeticException("')' is an unexpected token.");
					return list;
				case '@':
				{
					c=st.nextToken();
					if((c!='x')&&(c!='X'))
						throw new ArithmeticException("'"+c+"' is an unexpected token after @.");
					c=st.nextToken();
					if((c=='x')||(c=='X'))
						list.add(new CompiledOperation());
					else
					{
						if(c!=StreamTokenizer.TT_NUMBER)
							throw new ArithmeticException("'"+c+"' is an unexpected token after @x.");
						if((st.nval>11)||(st.nval<1.0))
							throw new ArithmeticException("'"+st.nval+"/11' is an illegal variable reference.");
						list.add(new CompiledOperation(((int)st.nval)-1));
					}
					break;
				}
				case '+':
				case '-':
				case '*':
				case '\\':
				case '/':
				case '?':
				case '<':
				case '>':
				{
					lastOperation=(char)c;
					c=st.nextToken();
					continue;
				}
				default:
					throw new ArithmeticException("'"+(char)c+"' ("+(int)c+") is an illegal expression.");
				}
				switch(lastOperation)
				{
				case '+':
				case '-':
				case '*':
				case '?':
				case '<':
				case '>':
					list.add(new CompiledOperation(lastOperation));
					break;
				case '/':
				case '\\':
					list.add(new CompiledOperation('/'));
					break;
				}
				c=st.nextToken();
			}
		}
		catch(IOException e){}
		if(inParen)
			throw new ArithmeticException("')' was missing from this expression");
		return list;
	}

	/**
	 * Parse a pre-compiled expression.  Requires a vars variable of at least 10 entries
	 * to ensure NO exceptions (other than /0).
	 * @see CMath#compileMathExpression(StreamTokenizer, boolean)
	 * @param list the pre-compiled expression
	 * @param vars the variable values
	 * @return the final value
	 */
	public static double parseMathExpression(LinkedList<CompiledOperation> list, double[] vars, double previous)
	{
		double finalValue=0.0;
		double curValue=0.0;
		for(CompiledOperation o : list)
			switch(o.type)
			{
				case CompiledOperation.OPERATION_VALUE: 
					curValue = o.value; break;
				case CompiledOperation.OPERATION_VARIABLE: 
					curValue = vars[o.variableIndex]; break;
				case CompiledOperation.OPERATION_LIST: 
					curValue = parseMathExpression(o.list,vars,finalValue); break;
				case CompiledOperation.OPERATION_PREVIOUSVALUE: 
					curValue = previous; break;
				case CompiledOperation.OPERATION_OPERATION:
					switch(o.operation)
					{
						case '+': finalValue+=curValue; break;
						case '-': finalValue-=curValue; break;
						case '*': finalValue*=curValue; break;
						case '/': finalValue/=curValue; break;
						case '?': finalValue= ((curValue-finalValue+0.5) * rand.nextDouble()) + finalValue; break;
						case '<': finalValue = finalValue < curValue? finalValue : curValue; break;
						case '>': finalValue = finalValue > curValue? finalValue : curValue; break;
					}
					break;
			}
		return finalValue;
	}
	
	
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @return the result of the expression
	 */
	public static long parseLongExpression(String formula)
	{return Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,null,0));}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to a long
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public static long parseLongExpression(String formula, double[] vars)
	{return Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,vars,0));}

	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to an integer.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @return the result of the expression
	 */
	public static int parseIntExpression(String formula) throws ArithmeticException
	{return (int)Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,null,0));}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Rounds the result to an integer.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public static int parseIntExpression(String formula, double[] vars) throws ArithmeticException
	{return (int)Math.round(parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,vars,0));}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variable @xx will refer to current computed value.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @return the result of the expression
	 */
	public static double parseMathExpression(String formula) throws ArithmeticException
	{return parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,null,0);}
	/**
	 * Returns the result of evaluating the given math
	 * expression.  An expression can be a double or int
	 * number, or a full expression using ()+-/*?<>.
	 * Variables are included as @x1, etc.. The given
	 * variable values list is 0 based, so @x1 = vars[0].
	 * Variable @xx will refer to current computed value.
	 * Throws an exception on any parsing error
	 * @param formula a full math expression string
	 * @param vars the 0 based variables
	 * @return the result of the expression
	 */
	public static double parseMathExpression(String formula, double[] vars) throws ArithmeticException
	{return parseMathExpression(new StreamTokenizer(new InputStreamReader(new ByteArrayInputStream(formula.getBytes()))),false,vars,0);}


	/**
	 * Returns the long value of a string without crashing
	 *
	 * <br><br><b>Usage:</b> lSize = WebIQBase.s_long(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param LONG String to convert
	 * @return long Long value of the string
	 */
	public static long s_long(String LONG)
	{
		long slong=0;
		try{ slong=Long.parseLong(LONG); }
		catch(Exception e){ return 0;}
		return slong;
	}

	/**
	 * Returns the floating point value of a string without crashing
	 *
	 * <br><br><b>Usage:</b> lSize = WebIQBase.s_float(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param FLOAT String to convert
	 * @return Float value of the string
	 */
	public static float s_float(String FLOAT)
	{
		float sfloat=(float)0.0;
		try{ sfloat=Float.parseFloat(FLOAT); }
		catch(Exception e){ return 0;}
		return sfloat;
	}

	/**
	 * Returns the double value of a string without crashing
	 *
	 * <br><br><b>Usage:</b> dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param DOUBLE String to convert
	 * @return double Double value of the string
	 */
	public static double s_double(String DOUBLE)
	{
		double sdouble=0;
		try{ sdouble=Double.parseDouble(DOUBLE); }
		catch(Exception e){ return 0;}
		return sdouble;
	}

	/**
	 * Returns the boolean value of a string without crashing
	 *
	 * <br><br><b>Usage:</b> int num=s_bool(CMD.substring(14));
	 * @param BOOL Boolean value of string
	 * @return int Boolean value of the string
	 */
	public static boolean s_bool(String BOOL)
	{
		return Boolean.valueOf(BOOL).booleanValue();
	}

	/**
	 * Returns whether the given string is a boolean value
	 *
	 * <br><br><b>Usage:</b> if(isBool(CMD.substring(14)));
	 * @param BOOL Boolean value of string
	 * @return whether it is a boolean
	 */
	public static boolean isBool(String BOOL)
	{
		return BOOL.equalsIgnoreCase("true")||BOOL.equalsIgnoreCase("false");
	}

	/**
	 * Returns the integer value of a string without crashing
	 *
	 * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	public static int s_int(String INT)
	{
		int sint=0;
		try{ sint=Integer.parseInt(INT); }
		catch(Exception e){ return 0;}
		return sint;
	}
	/**
	 * Returns the short value of a string without crashing
	 *
	 * <br><br><b>Usage:</b> int num=s_short(CMD.substring(14));
	 * @param SHORT Short value of string
	 * @return short Short value of the string
	 */
	public static short s_short(String SHORT)
	{
		short sint=0;
		try{ sint=Short.parseShort(SHORT); }
		catch(Exception e){ return 0;}
		return sint;
	}

	/**
	 * Returns whether the given string is a long value
	 *
	 * <br><br><b>Usage:</b> if(isLong(CMD.substring(14)));
	 * @param LONG Long value of string
	 * @return whether it is a long
	 */
	public static boolean isLong(String LONG){return isInteger(LONG);}
	
	/**
	 * Returns whether the given string is a int value
	 *
	 * <br><br><b>Usage:</b> if(isInteger(CMD.substring(14)));
	 * @param INT Integer value of string
	 * @return whether it is a int
	 */
	public static boolean isInteger(String INT)
	{
		if(INT==null) return false;
		if(INT.length()==0) return false;
		int i=0;
		if(INT.charAt(0)=='-')
			if(INT.length()>1)
				i++;
			else
				return false;
		for(;i<INT.length();i++)
			if(!Character.isDigit(INT.charAt(i)))
				return false;
		return true;
	}
	
	/**
	 * Returns whether the given string is a float value
	 *
	 * <br><br><b>Usage:</b> if(isFloat(CMD.substring(14)));
	 * @param DBL float value of string
	 * @return whether it is a float
	 */
	public static boolean isFloat(String DBL){return isDouble(DBL);}
	
	/**
	 * Returns a long representing either the given value, or 
	 * the index of the value in the order
	 * they appear in the given string list.
	 *
	 * <br><br><b>Usage:</b> if(s_parseListLongExpression(CMDS,CMD.substring(14)));
	 * @param descs the ordered string values from 0-whatever.
	 * @param val the expression, or list of string values
	 * @return the long value, or 0
	 */
	public static long s_parseListLongExpression(String[] descs, String val)
	{
		if((val==null)||(val.trim().length()==0)||(CMath.isMathExpression(val)))
			return CMath.s_parseLongExpression(val);
		for(int x=0;x<descs.length;x++)
			if(descs[x].equalsIgnoreCase(val))
				return x;
		return 0;
	}
	
	/**
	 * Returns a int representing either the given value, or 
	 * the index of the value in the order
	 * they appear in the given string list.
	 *
	 * <br><br><b>Usage:</b> if(s_parseListIntExpression(CMDS,CMD.substring(14)));
	 * @param descs the ordered string values from 0-whatever.
	 * @param val the expression, or list of string values
	 * @return the int value, or 0
	 */
	public static int s_parseListIntExpression(String[] descs, String val)
	{ return (int)s_parseListLongExpression(descs,val);}
	
	/**
	 * Returns whether the given string is a double value
	 *
	 * <br><br><b>Usage:</b> if(isDouble(CMD.substring(14)));
	 * @param DBL double value of string
	 * @return whether it is a double
	 */
	public static boolean isDouble(String DBL)
	{
		if(DBL==null) return false;
		if(DBL.length()==0) return false;
		int i=0;
		if(DBL.charAt(0)=='-')
			if(DBL.length()>1)
				i++;
			else
				return false;
		boolean alreadyDot=false;
		for(;i<DBL.length();i++)
			if(!Character.isDigit(DBL.charAt(i)))
			{
				if(DBL.charAt(i)=='.')
				{
					if(alreadyDot)
						return false;
					alreadyDot=true;
				}
				else
					return false;
			}
		return alreadyDot;
	}

	public static double random(){return rand.nextDouble();}
	public static int random(int max){return rand.nextInt(max);}
	public static int gcd(int a, int b)
	{
		int c;
		while(b!=0)
		{
			c=a;
			a=b;
			b=c%b;
		}
		return a;
	}
	public static long gcd(long a, long b)
	{
		long c;
		while(b!=0)
		{
			c=a;
			a=b;
			b=c%b;
		}
		return a;
	}
}