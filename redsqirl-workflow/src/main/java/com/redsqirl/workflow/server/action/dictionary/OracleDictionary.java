/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server.action.dictionary;

import java.util.Set;

import com.redsqirl.utils.FieldList;

public class OracleDictionary extends JdbcDictionary{


	/** Instance */
	protected static OracleDictionary instance;

	//style=\" border-style: solid;border-width: 1px;\"
	protected static final String dateFormats = "<table>"
			+ "<tr bgcolor=\"#ccccff\">"
			+ "   <th align=left>Date and Time Pattern"
			+ "   <th align=left>Result"
			+ "<tr bgcolor=\"#eeeeff\">"
			+"    <td><code>\"EEE, MMM d, yy\"</code>"
			+"    <td><code>Wed, Jul 4, 01</code>"
			+" <tr>"
			+"     <td><code>\"h:mm a\"</code>"
			+"     <td><code>12:08 PM</code>"
			+" <tr bgcolor=\"#eeeeff\">"
			+"     <td><code>\"yyMMddHHmmssZ\"</code>"
			+"     <td><code>010704120856-0700</code>"
			+" <tr>"
			+"     <td><code>\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\"</code>"
			+"     <td><code>2001-07-04T12:08:56.235-0700</code>"
			+" <tr>"
			+"     <td><code>\"YYYY-'W'ww-u\"</code>"
			+"     <td><code>2001-W27-3</code>"
			+"	</table>";
	
	/**
	 * Get an instance of the dictionary
	 * 
	 * @return instance
	 */
	public static OracleDictionary getInstance() {
		if (instance == null) {
			instance = new OracleDictionary();
		}
		return instance;
	}
	
	private OracleDictionary(){
		super("oracle");
	}
	
	protected void loadDefaultFunctions() {
		super.loadDefaultFunctions();
		

		functionsMap.put(arithmeticOperators,
				new String[][] { new String[] { "+", "NUMBER,NUMBER...", "NUMBER" },
				new String[] { "-", "NUMBER,NUMBER,NUMBER...", "NUMBER" },
				new String[] { "-", "DATETIME,DATETIME", "NUMBER" },
				new String[] { "*", "NUMBER,NUMBER,NUMBER...", "NUMBER" },
				new String[] { "/", "NUMBER,NUMBER,NUMBER...", "NUMBER" },
				new String[] { "%", "NUMBER,NUMBER,NUMBER...", "NUMBER" } });
		
		functionsMap.put(castMethods, new String[][] {
			new String[] { "CAST( AS INT)", "ANY", "INT",
			"@function:CAST(MYVALUE AS INT)"
			+ "@short:returns an integer value."
			+ "@example: CAST('3' AS INT) returns 3"
			},
			new String[] { "CAST( AS NUMBER)", "ANY", "DOUBLE",
			"@function:CAST(MYVALUE AS NUMBER)"
			+ "@short:returns a double value."
			+ "@example: CAST('3.1' AS NUMBER) returns 3.1" 
			},
			new String[] { "CAST( AS VARCHAR2(50))", "ANY", "STRING",
			"@function:CAST(MYVALUE AS VARCHAR2(MYLENGTH))"
			+ "@short:returns a string value."
			+ "@example: CAST(3 AS VARCHAR2(3)) returns '3'"
			},
			new String[] { "TO_DATE()", "STRING", "DATETIME",
			"@function:TO_DATE"
			+ "@short:returns the date value of the object."
			+ "@example: TO_DATE('2016-02-01')"
			},
			new String[] { "TO_DATE()", "STRING,STRING", "DATETIME",
			"@function:TO_DATE"
			+ "@short:returns the date value of the object given in the non default format."+dateFormats
			+ "@example: TO_DATE('20160201','YYYYMMDD')"
			},
			new String[] { "TO_TIMESTAMP()", "STRING,STRING", "TIMESTAMP",
			"@function:TO_TIMESTAMP"
			+ "@short:returns the date value of the object given in the non default format."+dateFormats
			+ "@example: TO_TIMESTAMP('10-SEP-0214:10:10.123000','DD-MON-RRHH24:MI:SS.FF')"
			},
			new String[] { "TO_CHAR()", "TIMESTAMP", "STRING",
			"@function:TO_CHAR"
			+ "@short:returns a string value fo the date in the default format."
			+ "@example: TO_CHAR(MYDATE,'YYYYMMDD')"
			},
			new String[] { "TO_CHAR()", "TIMESTAMP,STRING", "STRING",
			"@function:TO_CHAR"
			+ "@short:returns a string value fo the date in the given format."+dateFormats
			+ "@example: TO_CHAR(MYDATE,'YYYYMMDD')"
			},
		});
		
		String[][] oracleStringMethods = new String[][] {
			new String[] { "INITCAP()", "STRING", "STRING",
			"@function:INITCAP"
			+ "@short:Capitalize every word."
			+ "@param:MYSTRING The String to transform."
			+ "@description:Sets the first character in each word to uppercase and the rest to lowercase."
			+ "@example:INITCAP(\"hello world\") returns \"Hello World\""
			},
			new String[] { "INSTR()", "STRING,STRING", "INT",
			"@function:INSTR"
			+ "@short:Returns the location of the first occurrence of substring in a string."
			+ "@param:MYSTRING"
			+ "@param:SUBSTRING"
			+ "@example:INSTR('Hello world!', 'l') returns 3 (first l)"
			+ "@example:INSTR('Hello world!', 'ello') returns 2 (e)"
			},
			new String[] { "INSTR()", "STRING,STRING,INT", "INT",
			"@function:INSTR"
			+ "@short:Returns the location of the first occurrence of substring in a string, starting from"
			+ "@param:MYSTRING"
			+ "@param:SUBSTRING"
			+ "@param:START INDEX The index starts at 1."
			+ "@example:INSTR('Hello world!', 'l',4) returns 4 (second l)"
			},
			new String[] { "INSTR()", "STRING,STRING,INT,INT", "INT",
			"@function:INSTR"
			+ "@short:Returns the location of the nth occurrence of substring in a string, starting from start index."
			+ "@param:MYSTRING"
			+ "@param:SUBSTRING"
			+ "@param:START INDEX The index starts at 1."
			+ "@param:Number of occurrence"
			+ "@example:INSTR('Hello world!', 'l',1,2) returns 4"
			},
			new String[] { "REGEXP_INSTR()", "STRING,STRING", "INT",
			"@function:REGEXP_INSTR"
			+ "@short:Returns the location of the first pattern found in a string."
			+ "@param:MYSTRING"
			+ "@param:PATTERN The pattern can use SQL regular expression."
			+ "@example:REGEXP_INSTR('Hello world!', 'l|w') returns 3 (first l)"
			+ "@example:REGEXP_INSTR('Hello world!', 'ello') returns 2 (e)"
			},
			new String[] { "REGEXP_INSTR()", "STRING,STRING,INT", "INT",
			"@function:REGEXP_INSTR"
			+ "@short:returns the location of a pattern in a string."
			+ "@param:STRING"
			+ "@param:PATTERN"
			+ "@param:START INDEX"
			+ "@example: REGEXP_INSTR('Hello world!', 'l',4) returns 4 (second l)"
			},
			new String[] { "REGEXP_INSTR()", "STRING,STRING,INT,INT", "INT",
			"@function:REGEXP_INSTR"
			+ "@short:Returns the location of the first occurrence of pattern in a string, starting from start index."
			+ "@param:MYSTRING"
			+ "@param:PATTERN The pattern can use SQL regular expression."
			+ "@param:START INDEX The index starts at 1."
			+ "@example: REGEXP_INSTR('Hello world!', 'l',4) returns second l"
			},
			new String[] { "REGEXP_INSTR()", "STRING,STRING,INT,INT", "INT",
			"@function:REGEXP_INSTR"
			+ "@short:Returns the location of the nth occurrence of pattern in a string, starting from start index."
			+ "@param:MYSTRING"
			+ "@param:PATTERN The pattern can use SQL regular expression."
			+ "@param:START INDEX The index starts at 1."
			+ "@param:OCCURRENCE Number of occurrence."
			+ "@example:REGEXP_INSTR('Hello world!', 'l',1,2) returns 4 (second l)"
			},
			new String[] { "CONCAT()", "STRING,STRING", "STRING",
			"@function:CONCAT( STRING , OTHERSTRING)"
			+ "@short:Adds strings together."
			+ "@param:STRING the string that is added to"
			+ "@param:OTHERSTRING the string that is added to STRING"
			+ "@description:Adds several strings together to make a larger on. The function takes at least two arguments."
			+ "@example: CONCAT(\"hello\", \"world\") returns \"helloworld\""}
		};
		addToFunctionsMap(stringMethods,oracleStringMethods);
		
		String[][] oracleDateMethods = new String[][] {
			new String[] { "CURRENT_DATE()", "", "DATETIME",
			"@function:CURRENT_DATE"
			+ "@short:returns the current date in the time zone of your database.",
			},
			new String[] { "CURRENT_TIMESTAMP()", "", "TIMESTAMP",
			"@function:CURRENT_TIMESTAMP"
			+ "@short:returns the current timestamp in the time zone of the current SQL session.",
			},
			new String[] { "TRUNC()", "DATETIME", "DATETIME",
			"@function:TRUNC"
			+ "@short:returns a date truncated to the day."
			},
			new String[] { "TRUNC()", "STRING,STRING", "DATETIME",
			"@function:TRUNC(MYSTRING,MYSTRING)"
			+ "@short:Returns a date truncated to a specific unit of measure."
			+ "@description:The unit can be 'YEAR','MONTH'."
			+ "@example:TRUNC('2009-03-20 18:30:01','MONTH') returns '2009-03-01'"
			+ "@example:TRUNC('2009-03-20 18:30:01','YEAR') returns '2009-01-01'"
			},
			new String[] { "TRUNC()", "DATETIME,STRING", "DATETIME",
			"@function:TRUNC(MYTIMESTAMP,MYSTRING)"
			+ "@short:Returns a date truncated to a specific unit of measure."
			+ "@description:The unit can be 'YEAR','MONTH'."
			+ "@example:TRUNC('2009-03-20 18:30:01','MONTH') returns '2009-03-01'"
			+ "@example:TRUNC('2009-03-20 18:30:01','YEAR') returns '2009-01-01'"
			},
			new String[] { "ROUND()", "DATETIME", "DATETIME",
			"@function:ROUND(MYDATE)"
			+ "@short:Returns a date rounded to the day."
			+ "@example:ROUND('2009-03-20 18:30:01') result would be '2009-03-20'."
			},
			new String[] { "ROUND()", "DATETIME,STRING", "DATETIME",
			"@function:ROUND(MYDATE,MYUNIT)"
			+ "@short:Returns a date rounded to a specific unit of measure."
			+ "@description:The unit can be 'YEAR','MONTH','DAY', 'HH' or 'MI'. "
			+ "@example:ROUND('2009-03-20 18:30:01', 'YEAR') result would be '2009-03-20'."
			}
			
		};
		addToFunctionsMap(dateMethods,oracleDateMethods);
		
		String[][] oracleUtilMethods = new String[][] {
			new String[] { "NVL()", "ANY,ANY", "ANY",
			"@function:NVL"
			+ "@short:If the first value is null, returns the second one."
			+ "@param:Nullable Value "
			+ "@param:Default value"
			+ "@example:NVL(MYSTRING,'DEFAULT')"
			+ "@example:NVL(MYINT,0)",
			},
			new String[] { "NVL2()", "ANY,ANY,ANY", "ANY",
			"@function:NVL"
			+ "@short:if the first value is not null, returns the second one else returns the third one.",
			}
		};
		addToFunctionsMap(utilsMethods,oracleUtilMethods);
		
		String[][] oracleAnalyticMethods = new String[][] {
			new String[] { "NTILE(10) OVER (ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE"
			+ "@short:separate the data into 10 subsets ordered by a criteria.",
			},
			new String[] { "RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:RANK"
			+ "@short:calculates the rank of a value in a group of values. The return type is NUMBER.",
			},
			new String[] { "DENSE_RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:DENSE_RANK"
			+ "@short: computes the rank of a row in an ordered group of rows and returns the rank as a NUMBER. The ranks are consecutive integers beginning with 1.",
			},
			new String[] { "ROW_NUMBER() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE"
			+ "@short:returns the row number.",
			},
			new String[] { "LEAD() OVER (ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LEAD(EXPR,OFFSET,DEFAULT)"
			+ "@short:computes an expression on the next rows.",
			},
			new String[] { "LAG() OVER (ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LAG(EXPR,OFFSET,DEFAULT)"
			+ "@short:computes an expression on the previous rows.",
			},
			new String[] { "FIRST_VALUE() OVER (ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:FIRST_VALUE(EXPR)"
			+ "@short:Picks the first record from the partition after doing the ORDER BY.",
			},
			new String[] { "LAST_VALUE() OVER (ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:LAST_VALUE(EXPR)"
			+ "@short:Picks the last record from the partition after doing the ORDER BY.",
			},
			new String[] { "NTILE(10) OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE"
			+ "@short:separate the data into 10 subsets ordered by a criteria.",
			},
			new String[] { "RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:RANK"
			+ "@short:calculates the rank of a value in a group of values. The return type is NUMBER.",
			},
			new String[] { "DENSE_RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:DENSE_RANK"
			+ "@short: computes the rank of a row in an ordered group of rows and returns the rank as a NUMBER. The ranks are consecutive integers beginning with 1.",
			},
			new String[] { "ROW_NUMBER() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE"
			+ "@short:returns the row number.",
			},
			new String[] { "LEAD() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LEAD(EXPR,OFFSET,DEFAULT)"
			+ "@short:computes an expression on the next rows.",
			},
			new String[] { "LAG() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LAG(EXPR,OFFSET,DEFAULT)"
			+ "@short:computes an expression on the previous rows.",
			},
			new String[] { "FIRST_VALUE() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:FIRST_VALUE(EXPR)"
			+ "@short:Picks the first record from the partition after doing the ORDER BY.",
			},
			new String[] { "LAST_VALUE() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:LAST_VALUE(EXPR)"
			+ "@short:Picks the last record from the partition after doing the ORDER BY.",
			},
			new String[] { "COUNT() OVER()", "ANY", "INT",
			"@function:COUNT(MYCOLUMN) OVER()"
			+ "@short:Provides the overall number of non-null element"
			+ "@param:MYCOLUMN The expression to count"
			+ "@example:COUNT(FIELD1) OVER()",
			},
			new String[] { "COUNT() OVER(PARTITION BY MYCOLUMN2)", "ANY", "INT",
			"@function:COUNT(MYCOLUMN1) OVER(PARTITION BY MYCOLUMN2)"
			+ "@short:Provides the number of non-null element over a window"
			+ "@param:MYCOLUMN1 The expression to count"
			+ "@param:MYCOLUMN2 The section for which it is calculated"
			+ "@example:COUNT(FIELD1) OVER(PARTITION BY MY_DATE)",
			},
			new String[] { "SUM() OVER()", "NUMBER", "NUMBER",
			"@function:SUM(MYCOLUMN) OVER()"
			+ "@short:Provides the overall sum of an expression"
			+ "@param:MYCOLUMN The expression to sum"
			+ "@example:SUM(FIELD1) OVER()",
			},
			new String[] { "SUM() OVER(PARTITION BY MYCOLUMN2)", "NUMBER", "NUMBER",
			"@function:SUM(MYCOLUMN1) OVER(PARTITION BY MYCOLUMN2)"
			+ "@short:Provides the sum of an expression over a window"
			+ "@param:MYCOLUMN1 The expression to sum"
			+ "@param:MYCOLUMN2 The section for which it is calculated"
			+ "@example:SUM(FIELD1) OVER(PARTITION BY MY_DATE)",
			},
			new String[] { "MIN() OVER()", "ANY", "ANY",
			"@function:MIN(MYCOLUMN) OVER()"
			+ "@short:Provides the overall minimum of an expression"
			+ "@param:MYCOLUMN The expression for which the minimum value is calculated"
			+ "@example:MIN(FIELD1) OVER()",
			},
			new String[] { "MIN() OVER(PARTITION BY MYCOLUMN2)", "ANY", "ANY",
			"@function:MIN(MYCOLUMN1) OVER(PARTITION BY MYCOLUMN2)"
			+ "@short:Provides the minimum of an expression over a window"
			+ "@param:MYCOLUMN1 The expression for which the minimum value is calculated"
			+ "@param:MYCOLUMN2 The section for which it is calculated"
			+ "@example:MIN(FIELD1) OVER(PARTITION BY MY_DATE)",
			},
			new String[] { "MAX() OVER()", "ANY", "ANY",
			"@function:MAX(MYCOLUMN) OVER()"
			+ "@short:Provides the overall maximum of an expression"
			+ "@param:MYCOLUMN The expression for which the maximum value is calculated"
			+ "@example:MAX(FIELD1) OVER()",
			},
			new String[] { "MAX() OVER(PARTITION BY MYCOLUMN2)", "ANY", "ANY",
			"@function:MAX(MYCOLUMN1) OVER(PARTITION BY MYCOLUMN2)"
			+ "@short:Provides the maximum of an expression over a window"
			+ "@param:MYCOLUMN1 The expression for which the maximum value is calculated"
			+ "@param:MYCOLUMN2 The section for which it is calculated"
			+ "@example:MAX(FIELD1) OVER(PARTITION BY MY_DATE)",
			},
			new String[] { "AVG() OVER()", "NUMBER", "NUMBER",
			"@function:AVG(MYCOLUMN) OVER()"
			+ "@short:Provides the overall average of an expression"
			+ "@param:MYCOLUMN The expression for which the average value is calculated"
			+ "@param:EXPR The expression to count"
			+ "@example:AVG(FIELD1) OVER()",
			},
			new String[] { "AVG() OVER(PARTITION BY MYCOLUMN2)", "NUMBER", "NUMBER",
			"@function:AVG(MYCOLUMN1) OVER(PARTITION BY MYCOLUMN2)"
			+ "@short:Provides the average of an expression over a window"
			+ "@param:MYCOLUMN1 The expression for which the average value is calculated"
			+ "@param:MYCOLUMN2 The section for which it is calculated"
			+ "@example:AVG(FIELD1) OVER(PARTITION BY MY_DATE)",
			}
			
		};	
		addToFunctionsMap(analyticMethods,oracleAnalyticMethods);
		
		String[][] oracleAggregationsMethods = new String[][] {
			new String[] { "CORR()", "NUMBER,NUMBER", "DOUBLE",
			"@function:CORR"
			+ "@short:returns the correlation between two columns."
			+ "@param:MYNUMBER1"
			+ "@param:MYNUMBER2"
			+ "@description:Returns the Pearson coefficient of correlation of a pair of a numeric columns in the group."
			+ "@example:CORR(nb_article,price)",
			},
			new String[] { "VARIANCE()", "NUMBER", "DOUBLE",
			"@function:VARIANCE"
			+ "@short:Returns the variance of a column."
			+ "@param:MYNUMBER"
			+ "@description:Returns the variance of a numeric column in the group."
			+ "@example:VARIANCE(nb_article)",
			},
			new String[] { "STDDEV()", "NUMBER", "DOUBLE",
			"@function:STDDEV"
			+ "@short:Returns the standard deviation of a column."
			+ "@param:MYNUMBER"
			+ "@description:Returns the standard deviation of a numeric column in the group."
			+ "@example:STDDEV(nb_article)",
			},
			new String[] { "MEDIAN()", "NUMBER", "NUMBER",
			"@function:MEDIAN(MYNUMBER)"
			+ "@short:returns the median value."
			+ "@example:MEDIAN(MYNUMBER)",
			},
			new String[] { "MEDIAN()", "DATETIME", "DATETIME",
			"@function:MEDIAN(MYDATE)"
			+ "@short:returns the median value."
			+ "@example:MEDIAN(MYDATE)",
			}
		};
		addToFunctionsMap(agregationMethods,oracleAggregationsMethods);
	}
	
}
