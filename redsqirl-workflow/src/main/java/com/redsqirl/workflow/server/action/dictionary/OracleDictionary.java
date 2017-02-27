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
		
		functionsMap
				.put(castMethods,
						new String[][] { 
					new String[] { "CAST( AS INT)", "ANY", "INT",
							"@function:CAST@short:returns an integer value.@example: CAST('3' AS INT) returns 3"},
					new String[] { "CAST( AS NUMBER)", "ANY", "DOUBLE",
							"@function:CAST@short:returns a double value.@example: CAST('3.1' AS NUMBER) returns 3.1" },
					new String[] { "CAST( AS VARCHAR2(50))", "ANY", "STRING",
							"@function:CAST@short:returns a string value.@example: CAST(3 AS VARCHAR2(3)) returns '3'"},
					new String[] { "TO_DATE()", "STRING", "DATETIME",
					"@function:TO_DATE@short:returns the date value of the object.@example: TO_DATE('2016-02-01')"},
					new String[] { "TO_DATE()", "STRING,STRING", "DATETIME",
					"@function:TO_DATE@short:returns the date value of the object given in the non default format."+dateFormats+
					"@example: TO_DATE('20160201','YYYYMMDD')"},
					new String[] { "TO_TIMESTAMP()", "STRING,STRING", "TIMESTAMP",
					"@function:TO_TIMESTAMP@short:returns the date value of the object given in the non default format."+dateFormats+
					"@example: TO_TIMESTAMP('10-SEP-0214:10:10.123000','DD-MON-RRHH24:MI:SS.FF')"},
					new String[] { "TO_CHAR()", "TIMESTAMP", "STRING",
							"@function:TO_CHAR@short:returns a string value fo the date in the default format."+
							"@example: TO_CHAR(MYDATE,'YYYYMMDD')"},
					new String[] { "TO_CHAR()", "TIMESTAMP,STRING", "STRING",
							"@function:TO_CHAR@short:returns a string value fo the date in the given format."+dateFormats+
							"@example: TO_CHAR(MYDATE,'YYYYMMDD')"},
				});
		
		String[][] oracleStringMethods = new String[][] {
			new String[] { "CHR()", "INT", "CHAR",
					"@function:CHR@short:returns the character based on the NUMBER code.@example: CHR(124) returns '|'"},
			new String[] { "INITCAP()", "STRING", "STRING",
					"@function:INITCAP@short: sets the first character in each word to uppercase and the rest to lowercase."},
			new String[] { "INSTR()", "STRING,STRING", "INT",
					"@function:INSTR@short:returns the location of a substring in a string."+
					"@param: STRING@param: SUBSTRING"+
					"@example: INSTR('Hello world!', 'l') returns 3 (first l)"+
					"@example: INSTR('Hello world!', 'ello') returns 2 (e)"
			},
			new String[] { "INSTR()", "STRING,STRING,INT", "INT",
					"@function:INSTR@short:returns the location of a substring in a string."+
					"@param: STRING@param: SUBSTRING@param: START INDEX"+
					"@example: INSTR('Hello world!', 'l',4) returns 4 (second l)"
			},
			new String[] { "INSTR()", "STRING,STRING,INT,INT", "INT",
					"@function:INSTR@short:returns the location of a substring in a string."+
					"@param: STRING@param: SUBSTRING@param: START INDEX@param: Number of occurrence"+
					"@example: INSTR('Hello world!', 'l',1,2) returns 4"
			},

			new String[] { "REGEXP_INSTR()", "STRING,STRING", "INT",
					"@function:REGEXP_INSTR@short:returns the location of a pattern in a string."+
					"@param: STRING@param: PATTERN"+
					"@example: REGEXP_INSTR('Hello world!', 'l|w') returns 3 (first l)"+
					"@example: REGEXP_INSTR('Hello world!', 'ello') returns 2 (e)"
			},
			new String[] { "REGEXP_INSTR()", "STRING,STRING,INT", "INT",
					"@function:REGEXP_INSTR@short:returns the location of a pattern in a string."+
					"@param: STRING@param: PATTERN@param: START INDEX"+
					"@example: REGEXP_INSTR('Hello world!', 'l',4) returns 4 (second l)"
			},
			new String[] { "REGEXP_INSTR()", "STRING,STRING,INT,INT", "INT",
					"@function:REGEXP_INSTR@short:returns the location of a pattern in a string."+
					"@param: STRING@param: PATTERN@param: START INDEX@param: Number of occurrence"+
					"@example: REGEXP_INSTR('Hello world!', 'l',1,2) returns 4"
			},
		};
		addToFunctionsMap(stringMethods,oracleStringMethods);
		
		String[][] oracleDateMethods = new String[][] {
			new String[] { "CURRENT_DATE()", "", "DATETIME",
			"@function:CURRENT_DATE@short:returns the current date in the time zone of your database.",
			},
			new String[] { "CURRENT_TIMESTAMP()", "", "TIMESTAMP",
			"@function:CURRENT_TIMESTAMP@short:returns the current timestamp in the time zone of the current SQL session.",
			},
			new String[] { "ROUND()", "DATETIME", "DATETIME",
			"@function:ROUND@short:returns a date rounded to the day."
			},
			new String[] { "ROUND()", "DATETIME,STRING", "DATETIME",
			"@function:ROUND@short:returns a date rounded to a specific unit of measure.@description: The unit can be 'YEAR','MONTH','DAY', 'HH' or 'MI'. "
			},
			new String[] { "TRUNC()", "DATETIME", "DATETIME",
			"@function:TRUNC@short:returns a date truncated to the day."
			},
			new String[] { "TRUNC()", "DATETIME,STRING", "DATETIME",
			"@function:TRUNC@short:returns a date truncated to a specific unit of measure.@description: The unit can be 'YEAR','MONTH','DAY', 'HH' or 'MI'. "
			}
		};
		addToFunctionsMap(dateMethods,oracleDateMethods);
		
		String[][] oracleUtilMethods = new String[][] {
			new String[] { "DECODE()", "STRING,STRING,STRING,STRING...", "STRING",
			"@function:DECODE@param: expression@param:search@param: result@param: default@short:returns a value if a match is found."+
			"@description: Search in expression a value, if the value is found the corresponding result is given. You can give as "+
					"many as search and result you like. Optionally if none of them match, you can return a default value. If no default is given the function returns null.",
			},
			new String[] { "NVL()", "ANY,ANY", "ANY",
					"@function:NVL@short:if the first value is null, returns the second one.",
					},
			new String[] { "NVL2()", "ANY,ANY,ANY", "ANY",
					"@function:NVL@short:if the first value is not null, returns the second one else returns the third one.",
					}
		};
		addToFunctionsMap(utilsMethods,oracleUtilMethods);
		
		String[][] oracleAnalyticMethods = new String[][] {
			new String[] { "NTILE(10) OVER (ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE@short:returns the first non-null expression in the list.",
			},
			new String[] { "RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:RANK@short:calculates the rank of a value in a group of values. The return type is NUMBER.",
			},
			new String[] { "DENSE_RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:DENSE_RANK@short: computes the rank of a row in an ordered group of rows and returns the rank as a NUMBER. The ranks are consecutive integers beginning with 1.",
			},
			new String[] { "ROW_NUMBER() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the row number.",
			},
			new String[] { "LEAD() OVER (ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LEAD(EXPR,OFFSET,DEFAULT)@short:computes an expression on the next rows.",
			},
			new String[] { "LAG() OVER (ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LAG(EXPR,OFFSET,DEFAULT)@short:computes an expression on the previous rows.",
			},
			new String[] { "FIRST_VALUE() OVER (ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:FIRST_VALUE(EXPR)@short:Picks the first record from the partition after doing the ORDER BY.",
			},
			new String[] { "LAST_VALUE() OVER (ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:LAST_VALUE(EXPR)@short:Picks the last record from the partition after doing the ORDER BY.",
			},
			new String[] { "NTILE(10) OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE@short:returns the first non-null expression in the list.",
			},
			new String[] { "RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:RANK@short:calculates the rank of a value in a group of values. The return type is NUMBER.",
			},
			new String[] { "DENSE_RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:DENSE_RANK@short: computes the rank of a row in an ordered group of rows and returns the rank as a NUMBER. The ranks are consecutive integers beginning with 1.",
			},
			new String[] { "ROW_NUMBER() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the row number.",
			},
			new String[] { "LEAD() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LEAD(EXPR,OFFSET,DEFAULT)@short:computes an expression on the next rows.",
			},
			new String[] { "LAG() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LAG(EXPR,OFFSET,DEFAULT)@short:computes an expression on the previous rows.",
			},
			new String[] { "FIRST_VALUE() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:FIRST_VALUE(EXPR)@short:Picks the first record from the partition after doing the ORDER BY.",
			},
			new String[] { "LAST_VALUE() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "ANY", "ANY",
			"@function:LAST_VALUE(EXPR)@short:Picks the last record from the partition after doing the ORDER BY.",
			},
		};
		addToFunctionsMap(analyticMethods,oracleAnalyticMethods);
		
		String[][] oracleAggregationsMethods = new String[][] {
			new String[] { "CORR()", "NUMBER,NUMBER", "DOUBLE",
			"@function:CORR@short:returns the correlation between two columns.",
			},
			new String[] { "VARIANCE()", "NUMBER", "DOUBLE",
			"@function:VARIANCE@short:returns the variance of a column.",
			},
			new String[] { "STDDEV()", "NUMBER", "DOUBLE",
			"@function:STDDEV@short:returns the standard deviation of a column.",
			},
			new String[] { "MEDIAN()", "NUMBER", "NUMBER",
			"@function:MEDIAN@short:returns the median value.",
			},
			new String[] { "MEDIAN()", "DATETIME", "DATETIME",
			"@function:MEDIAN@short:returns the median value.",
			}
		};
		addToFunctionsMap(agregationMethods,oracleAggregationsMethods);
	}
	
}
