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

public class HiveDictionary extends JdbcDictionary{


	/** Instance */
	protected static HiveDictionary instance;

	//style=\" border-style: solid;border-width: 1px;\"
	protected static final String dateFormats = "<table>"
			+ "<tr bgcolor=\"#ccccff\">"
			+ "   <th align=left>Date and Time Pattern"
			+ "   <th align=left>Result"
			+ "<tr bgcolor=\"#eeeeff\">"
			+"    <td><code>\"EEE, MMM d, ''yy\"</code>"
			+"    <td><code>Wed, Jul 4, '01</code>"
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
	public static HiveDictionary getInstance() {
		if (instance == null) {
			instance = new HiveDictionary();
		}
		return instance;
	}
	
	private HiveDictionary(){
		super("hive");
	}
	
	protected void loadDefaultFunctions() {
		super.loadDefaultFunctions();
		functionsMap
				.put(castMethods,
						new String[][] { 
					new String[] { "CAST( AS INT)", "ANY", "INT",
					"@function:CAST@short:returns an integer value.@example: CAST('3' AS INT) returns 3"},
					new String[] { "CAST( AS BIGINT)", "ANY", "LONG",
					"@function:CAST@short:returns an integer value.@example: CAST('3' AS INT) returns 3"},
					new String[] { "CAST( AS FLOAT)", "ANY", "FLOAT",
					"@function:CAST@short:returns a double value.@example: CAST('3.1' AS FLOAT) returns 3.1" },
					new String[] { "CAST( AS DOUBLE)", "ANY", "DOUBLE",
					"@function:CAST@short:returns a double value.@example: CAST('3.1' AS DOUBLE) returns 3.1" },
					new String[] { "CAST( AS STRING)", "ANY", "STRING",
					"@function:CAST@short:returns a string value.@example: CAST(3 AS STRING) returns '3'"},
					new String[] { "CAST( AS DATE)", "ANY", "DATETIME",
					"@function:CAST@short:returns a date value.@example: CAST('2017-02-31' AS DATE) returns 2017-02-31"},
					new String[] { "CAST( AS TIMESTAMP)", "DATETIME", "TIMESTAMP",
					"@function:CAST@short:returns a string value.@example: CAST(MYDATE AS TIMESTAMP)"},
				});
		
		String[][] hiveStringMethods = new String[][] {
			new String[] { "INITCAP()", "STRING", "STRING",
					"@function:INITCAP@short: sets the first character in each word to uppercase and the rest to lowercase."},
			new String[] { "LOCATE()", "STRING,STRING", "INT",
					"@function:LOCATE@short:returns the location of a substring in a string."+
					"@param: STRING@param: SUBSTRING"+
					"@example: LOCATE('Hello world!', 'l') returns 3 (first l)"+
					"@example: LOCATE('Hello world!', 'ello') returns 2 (e)"
			},
			new String[] { "LOCATE()", "STRING,STRING,INT", "INT",
					"@function:LOCATE@short:returns the location of a substring in a string."+
					"@param: STRING@param: SUBSTRING@param: START INDEX"+
					"@example: LOCATE('Hello world!', 'l',4) returns 4 (second l)"
			},
		};
		addToFunctionsMap(stringMethods,hiveStringMethods);
		
		String[][] hiveDateMethods = new String[][] {
			new String[] { "FROM_UNIXTIME()", "LONG", "STRING",
			"@function:UNIX_TIMESTAMP@short:Converts a long to a string timestamp format.@description:Converts the number of seconds from unix epoch (1970-01-01 00:00:00 UTC) to a string representing the timestamp",
			},
			new String[] { "FROM_UNIXTIME()", "LONG,STRING", "STRING",
			"@function:UNIX_TIMESTAMP@short:Converts a long to a string timestamp format.@description:Converts the number of seconds from unix epoch (1970-01-01 00:00:00 UTC) to a string representing the timestamp",
			},
			new String[] { "UNIX_TIMESTAMP()", "", "LONG",
			"@function:UNIX_TIMESTAMP@short:returns the current date in a unix format.",
			},
			new String[] { "UNIX_TIMESTAMP()", "STRING", "LONG",
			"@function:UNIX_TIMESTAMP@short:returns the date in a unix format.@description:Converts time string in format yyyy-MM-dd HH:mm:ss to Unix timestamp (in seconds), using the default timezone and the default locale, 0 if it fails.@example:unix_timestamp('2009-03-20 11:30:01') = 1237573801",
			},
			new String[] { "UNIX_TIMESTAMP()", "TIMESTAMP", "LONG",
			"@function:UNIX_TIMESTAMP@short:returns the date in a unix format.@description:Converts time string in format yyyy-MM-dd HH:mm:ss to Unix timestamp (in seconds), using the default timezone and the default locale, 0 if it fails.@example:unix_timestamp('2009-03-20 11:30:01') = 1237573801",
			},
			new String[] { "UNIX_TIMESTAMP()", "STRING,STRING", "LONG",
			"@function:UNIX_TIMESTAMP@short:returns the date in a unix format.@description:Converts time string in given format to Unix timestamp (in seconds), using the default timezone and the default locale, 0 if it fails.@example:unix_timestamp('2009-03-20', 'yyyy-MM-dd') = 1237532400",
			},
			new String[] { "TO_DATE()", "STRING", "DATETIME",
			"@function:TO_DATE@short:Returns the date part of a timestamp string.@example:to_date('1970-01-01 00:00:00') = '1970-01-01'",
			},
			new String[] { "YEAR()", "STRING", "INT",
			"@function:YEAR@short:returns the year of a date."
			},
			new String[] { "YEAR()", "TIMESTAMP", "INT",
			"@function:YEAR@short:returns the year of a date."
			},
			new String[] { "QUARTER()", "STRING", "INT",
			"@function:YEAR@short:returns the quarter of the year for a date, timestamp, or string in the range 1 to 4."
			},
			new String[] { "QUARTER()", "TIMESTAMP", "INT",
			"@function:YEAR@short:returns the quarter of the year for a date, timestamp, or string in the range 1 to 4."
			},
			new String[] { "MONTH()", "STRING", "INT",
			"@function:MONTH@short:returns the month of a date."
			},
			new String[] { "MONTH()", "TIMESTAMP", "INT",
			"@function:MONTH@short:returns the month of a date."
			},
			new String[] { "DAY()", "STRING", "INT",
			"@function:DAY@short:returns the day of the month of a date."
			},
			new String[] { "DAY()", "TIMESTAMP", "INT",
			"@function:DAY@short:returns the day of the month of a date."
			},
			new String[] { "HOUR()", "STRING", "INT",
			"@function:DAY@short:returns the hour of the day of a timestamp."
			},
			new String[] { "HOUR()", "TIMESTAMP", "INT",
			"@function:DAY@short:returns the hour of the day of a timestamp."
			},
			new String[] { "MINUTE()", "STRING", "INT",
			"@function:DAY@short:returns the minute of the hour of a timestamp."
			},
			new String[] { "MINUTE()", "TIMESTAMP", "INT",
			"@function:DAY@short:returns the minute of the hour of a timestamp."
			},
			new String[] { "SECOND()", "STRING", "INT",
			"@function:DAY@short:returns the second of the minute of a timestamp."
			},
			new String[] { "SECOND()", "TIMESTAMP", "INT",
			"@function:DAY@short:returns the second of the minute of a timestamp."
			},
			new String[] { "WEEKOFYEAR()", "STRING", "INT",
			"@function:MONTH@short:returns the week of the year of a date."
			},
			new String[] { "WEEKOFYEAR()", "TIMESTAMP", "INT",
			"@function:MONTH@short:returns the week of the year of a date."
			},
			new String[] { "DATEDIFF()", "STRING,STRING", "INT",
			"@function:DATEDIFF@short:Returns the number of days from startdate to enddate.@example:datediff('2009-03-01', '2009-02-27') = 2."
			},
			new String[] { "DATE_ADD()", "STRING,INT", "DATETIME",
			"@function:DATE_ADD@short:Adds a number of days to startdate.@example: date_add('2008-12-31', 1) = '2009-01-01'."
			},
			new String[] { "DATE_ADD()", "TIMESTAMP,INT", "DATETIME",
			"@function:DATE_ADD@short:Adds a number of days to startdate.@example: date_add('2008-12-31', 1) = '2009-01-01'."
			},
			new String[] { "DATE_SUB()", "STRING,INT", "DATETIME",
			"@function:DATE_SUB@short:Subtracts a number of days to startdate.@example: date_sub('2008-12-31', 1) = '2008-12-30'."
			},
			new String[] { "DATE_SUB()", "TIMESTAMP,INT", "DATETIME",
			"@function:DATE_SUB@short:Subtracts a number of days to startdate.@example: date_sub('2008-12-31', 1) = '2008-12-30'."
			},
			new String[] { "TRUNC()", "STRING,STRING", "DATETIME",
			"@function:TRUNC@short:returns a date truncated to a specific unit of measure.@description: The unit can be 'YEAR','MONTH','DAY', 'HH' or 'MI'. "
			},
			new String[] { "TRUNC()", "TIMESTAMP,STRING", "DATETIME",
			"@function:TRUNC@short:returns a date truncated to a specific unit of measure.@description: The unit can be 'YEAR','MONTH','DAY', 'HH' or 'MI'. "
			},
			new String[] { "DATE_FORMAT()", "TIMESTAMP,STRING", "STRING",
			"@functionDATE_FORMAT@short:Converts a date/timestamp/string to a value of string in the format specified by the date format fmt."
			},
			new String[] { "DATE_FORMAT()", "STRING,STRING", "STRING",
			"@function:DATE_FORMAT@short:Converts a date/timestamp/string to a value of string in the format specified by the date format fmt."
			},
			
		};
		addToFunctionsMap(dateMethods,hiveDateMethods);
		
		String[][] hiveUtilMethods = new String[][] {
			new String[] { "DECODE()", "STRING,STRING,STRING,STRING...", "STRING",
			"@function:DECODE@param: expression@param:search@param: result@param: default@short:returns a value if a match is found."+
			"@description: Search in expression a value, if the value is found the corresponding result is given. You can give as "+
					"many as search and result you like. Optionally if none of them match, you can return a default value. If no default is given the function returns null.",
			},
			new String[] { "NVL()", "ANY,ANY", "ANY",
					"@function:NVL@short:if the first value is null, returns the second one.",
					},
			new String[] { "ISNULL()", "ANY", "BOOLEAN",
					"@function:ISNULL@param:a@short:Returns true if a is NULL and false otherwise.",
					},
			new String[] { "ISNOTNULL()", "ANY", "BOOLEAN",
					"@function:ISNOTNULL@param:a@short:Returns true if a is not NULL and false otherwise.",
					}, 
			new String[] {
					"RAND()",
					"",
					"DOUBLE",
			"@function:RAND()@short: Generate a random double@description:Generates a random double and returns it" },
			
		};
		addToFunctionsMap(utilsMethods,hiveUtilMethods);
		
		String[][] hiveAnalyticMethods = new String[][] {
			new String[] { "NTILE(10) OVER (ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE@short:separate the data into 10 subsets ordered by a criteria.",
			},
			new String[] { "RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the first non-null expression in the list.",
			},
			new String[] { "DENSE_RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the first non-null expression in the list.",
			},
			new String[] { "ROW_NUMBER() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the row number.",
			},
			new String[] { "NTILE(10) OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE@short:separate the data into 10 subsets ordered by a criteria.",
			},
			new String[] { "RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the first non-null expression in the list.",
			},
			new String[] { "DENSE_RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the first non-null expression in the list.",
			},
			new String[] { "ROW_NUMBER() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:NTILE@short:returns the row number.",
			},
		};
		addToFunctionsMap(analyticMethods,hiveAnalyticMethods);
		
		String[][] hiveAggregationsMethods = new String[][] {
			new String[] { "CORR()", "NUMBER,NUMBER", "DOUBLE",
			"@function:CORR@short:returns the correlation between two columns.",
			},
			new String[] { "VARIANCE()", "NUMBER", "DOUBLE",
			"@function:VARIANCE@short:returns the variance of a column.",
			},
			new String[] { "STDDEV()", "NUMBER", "DOUBLE",
			"@function:STDDEV@short:returns the standard deviation of a column.",
			}
		};
		addToFunctionsMap(agregationMethods,hiveAggregationsMethods);
	}
	
	
}
