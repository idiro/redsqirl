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
	
	@Override
	protected void loadDefaultFunctions() {
		super.loadDefaultFunctions();
		
		functionsMap.put(castMethods, new String[][] {
					new String[] { "CAST( AS INT)", "ANY", "INT",
					"@function:CAST(MYVALUE AS INT)"
					+ "@short:returns an integer value."
					+ "@param: the value to be converted"
					+ "@example: CAST('3' AS INT) returns 3"},
					new String[] { "CAST( AS FLOAT)", "ANY", "FLOAT",
					"@function:CAST(MYVALUE AS FLOAT)"
					+ "@short:returns a double value."
					+ "@param: the value to be converted"
					+ "@example: CAST('3.1' AS FLOAT) returns 3.1" },
					new String[] { "CAST( AS DOUBLE)", "ANY", "DOUBLE",
					"@function:CAST(MYVALUE AS DOUBLE)"
					+ "@short:returns a double value."
					+ "@param: the value to be converted"
					+ "@example: CAST('3.1' AS DOUBLE) returns 3.1" },
					new String[] { "CAST( AS STRING)", "ANY", "STRING",
					"@function:CAST(MYVALUE AS STRING)"
					+ "@short:returns a string value."
					+ "@param: the value to be converted"
					+ "@example: CAST(3 AS STRING) returns '3'"},
					new String[] { "CAST( AS DATE)", "ANY", "DATETIME",
					"@function:CAST(MYVALUE AS DATE)"
					+ "@short:returns a date value."
					+ "@param: the value to be converted"
					+ "@example: CAST('2017-02-31' AS DATE) returns 2017-02-31"},
					new String[] { "CAST( AS TIMESTAMP)", "DATETIME", "TIMESTAMP",
					"@function:CAST(MYVALUE AS TIMESTAMP)"
					+ "@short:returns a string value."
					+ "@param: the value to be converted"
					+ "@example: CAST(MYDATE AS TIMESTAMP)"},
					new String[] { "CAST( AS BIGINT)", "ANY", "LONG",
					"@function:CAST(MYVALUE AS BIGINT)"
					+ "@short:returns an integer value."
					+ "@param: the value to be converted"
					+ "@example: CAST('3' AS BIGINT) returns 3"}
				});
		
		String[][] hiveStringMethods = new String[][] {
			new String[] { "INITCAP()", "STRING", "STRING",
					"@function:INITCAP(MYSTRING)"
					+ "@short:Capitalize every word."
					+ "@param:MYSTRING"
					+ "@param:SUBSTRING"
					+ "@description:Sets the first character in each word to uppercase and the rest to lowercase."
					+ "@example:INITCAP(\"hello world\") returns \"Hello World\"" },
			new String[] { "LOCATE()", "STRING,STRING", "INT",
					"@function:LOCATE(MYSTRING,SUBSTRING)"
					+ "@short:Returns the location of a substring in a string."
					+ "@param:STRING"
					+ "@param:SUBSTRING"
					+ "@description:Return the index of the first occurrence of the substring first character."
					+ "@example:LOCATE('Hello world!', 'l') returns 3 (first l)"
					+ "@example:LOCATE('Hello world!', 'ello') returns 2 (starting at e)" },
			new String[] { "LOCATE()", "STRING,STRING,INT", "INT",
					"@function:LOCATE(MYSTRING,SUBSTRING,START_INDEX)"
					+ "@short:Returns the location of a substring in a string from a starting from index."
					+ "@param:STRING"
					+ "@param:SUBSTRING"
					+ "@param:START INDEX"
					+ "@description:Return the index of the first occurrence of the substring first character starting from index."
					+ "@example:LOCATE('Hello world!', 'l',4) returns 4 (second l)" },
		};
		addToFunctionsMap(stringMethods,hiveStringMethods);
		
		String[][] hiveDateMethods = new String[][] {
			new String[] { "FROM_UNIXTIME()", "LONG", "STRING",
			"@function:FROM_UNIXTIME(LONG)"
			+ "@short:Converts a long to a string timestamp format."
			+ "@param:LONG The number of seconds since 1970"
			+ "@description:Converts the number of seconds from unix epoch (1970-01-01 00:00:00 UTC) to a string representing the timestamp."
			+ "@example:FROM_UNIXTIME(1237573801) returns '2009-03-20 18:30:01'",
			},
			new String[] { "FROM_UNIXTIME()", "LONG,STRING", "STRING",
			"@function:FROM_UNIXTIME(LONG,MYSTRING)"
			+ "@short:Converts a long to a string timestamp format."
			+ "@param:LONG The number of seconds since 1970"
			+ "@param:MYSTRING The date format"
			+ "@description:Converts the number of seconds from unix epoch (1970-01-01 00:00:00 UTC) to a string representing the timestamp."
			+ "@example:FROM_UNIXTIME(1237573801,'yyyyMMdd') returns '20090320'",
			},
			new String[] { "UNIX_TIMESTAMP()", "", "LONG",
			"@function:UNIX_TIMESTAMP()"
			+ "@short:Returns the current date in a unix format.",
			},
			new String[] { "UNIX_TIMESTAMP()", "STRING", "LONG",
			"@function:UNIX_TIMESTAMP(MYSTRING)"
			+ "@short:returns the date in a unix format."
			+ "@param:STRING The date in a standard format"
			+ "@description:Converts time string in format yyyy-MM-dd HH:mm:ss to Unix timestamp (in seconds), using the default timezone and the default locale, 0 if it fails."
			+ "@example:unix_timestamp('2009-03-20 18:30:01') = 1237573801",
			},
			new String[] { "UNIX_TIMESTAMP()", "TIMESTAMP", "LONG",
			"@function:UNIX_TIMESTAMP(TIMESTAMP)"
			+ "@short:returns the date in a unix format."
			+ "@param:TIMESTAMP"
			+ "@description:Converts time string in format yyyy-MM-dd HH:mm:ss to Unix timestamp (in seconds), using the default timezone and the default locale, 0 if it fails."
			+ "@example:unix_timestamp('2009-03-20 18:30:01') = 1237573801",
			},
			new String[] { "UNIX_TIMESTAMP()", "STRING,STRING", "LONG",
			"@function:UNIX_TIMESTAMP(MYSTRING1,MYSTRING2)"
			+ "@short:returns the date in a unix format."
			+ "@param:MYSTRING1 The date as a string"
			+ "@param:MYSTRING 2The string format of the date"
			+ "@description:Converts time string in given format to Unix timestamp (in seconds), using the default timezone and the default locale, 0 if it fails."
			+ "@example:unix_timestamp('2009-03-20', 'yyyy-MM-dd') = 1237507200",
			},
			new String[] { "TO_DATE()", "STRING", "DATETIME",
			"@function:TO_DATE"
			+ "@short:Returns the date part of a timestamp string."
			+ "@param:STRING The date as a standard timestamp format"
			+ "@example:to_date('1970-01-01 00:00:00') = '1970-01-01'",
			},
			new String[] { "YEAR()", "STRING", "INT",
			"@function:YEAR(MYSTRING)"
			+ "@short:Returns the year of a date."
			+ "@param:STRING The date"
			+ "@example:YEAR('2017-04-01') returns 2017"
			+ "@example:YEAR('2009-03-20 18:30:01') returns 2009",
			},
			new String[] { "YEAR()", "TIMESTAMP", "INT",
			"@function:YEAR(MYTIMESTAMP)"
			+ "@short:Returns the year of a date."
			+ "@param:MYTIMESTAMP"
			+ "@example:YEAR('2017-04-01') returns 2017"
			+ "@example:YEAR('2009-03-20 18:30:01') returns 2009",
			},
			new String[] { "QUARTER()", "STRING", "INT",
			"@function:QUARTER(MYSTRING)"
			+ "@short:Returns the quarter of the year for a date, timestamp, or string in the range 1 to 4."
			+ "@param:MYSTRING The date"
			+ "@example:QUARTER('2017-03-01') returns 1"
			+ "@example:QUARTER('2009-03-20 18:30:01') returns 1",
			},
			new String[] { "QUARTER()", "TIMESTAMP", "INT",
			"@function:QUARTER(MYTIMESTAMP)"
			+ "@short:Returns the quarter of the year for a date, timestamp, or string in the range 1 to 4."
			+ "@param:MYTIMESTAMP"
			+ "@example:QUARTER('2017-03-01') returns 1"
			+ "@example:QUARTER('2009-03-20 18:30:01') returns 1",
			},
			new String[] { "MONTH()", "STRING", "INT",
			"@function:MONTH(MYSTRING)"
			+ "@short:Returns the month of a date."
			+ "@param:MYSTRING The date"
			+ "@example:MONTH('2017-06-01') returns 6"
			+ "@example:MONTH('2009-03-20 18:30:01') returns 3",
			},
			new String[] { "MONTH()", "TIMESTAMP", "INT",
			"@function:MONTH(MYTIMESTAMP)"
			+ "@short:Returns the month of a date."
			+ "@param:MYTIMESTAMP"
			+ "@example:MONTH('2017-06-01') returns 6"
			+ "@example:MONTH('2009-03-20 18:30:01') returns 3",
			},
			new String[] { "DAY()", "STRING", "INT",
			"@function:DAY(MYSTRING)"
			+ "@short:Returns the day of the month of a date."
			+ "@param:MYSTRING"
			+ "@example:DAY('2017-09-27') returns 27"
			+ "@example:DAY('2009-03-20 18:30:01') returns 20",
			},
			new String[] { "DAY()", "TIMESTAMP", "INT",
			"@function:DAY(MYTIMESTAMP)"
			+ "@short:Returns the day of the month of a date."
			+ "@param:MYTIMESTAMP"
			+ "@example:DAY('2017-09-27') returns 27"
			+ "@example:DAY('2009-03-20 18:30:01') returns 20",
			},
			new String[] { "HOUR()", "STRING", "INT",
			"@function:HOUR(MYSTRING)"
			+ "@short:Returns the hour of the day of a timestamp."
			+ "@param:MYSTRING"
			+ "@example:HOUR('2009-03-20 18:30:01') returns 18",
			},
			new String[] { "HOUR()", "TIMESTAMP", "INT",
			"@function:HOUR(MYTIMESTAMP)"
			+ "@short:Returns the hour of the day of a timestamp."
			+ "@param:MYTIMESTAMP"
			+ "@example:HOUR('2009-03-20 18:30:01') returns 18",
			},
			new String[] { "MINUTE()", "STRING", "INT",
			"@function:MINUTE(MYSTRING)"
			+ "@short:Returns the minute of the hour of a timestamp."
			+ "@param:MYSTRING"
			+ "@example:MINUTE('2009-03-20 18:30:01') returns 30",
			},
			new String[] { "MINUTE()", "TIMESTAMP", "INT",
			"@function:MINUTE(MYTIMESTAMP)"
			+ "@short:Returns the minute of the hour of a timestamp."
			+ "@param:MYTIMESTAMP"
			+ "@example:MINUTE('2009-03-20 18:30:01') returns 30",
			},
			new String[] { "SECOND()", "STRING", "INT",
			"@function:SECOND(MYSTRING)"
			+ "@short:Returns the second of the minute of a timestamp."
			+ "@param:MYSTRING"
			+ "@example:SECOND('2009-03-20 18:30:01') returns 1",
			},
			new String[] { "SECOND()", "TIMESTAMP", "INT",
			"@function:SECOND(MYTIMESTAMP)"
			+ "@short:Returns the second of the minute of a timestamp."
			+ "@param:MYTIMESTAMP"
			+ "@example:SECOND('2009-03-20 18:30:01') returns 1",
			},
			new String[] { "WEEKOFYEAR()", "STRING", "INT",
			"@function:WEEKOFYEAR(MYSTRING)"
			+ "@short:Returns the week of the year of a date."
			+ "@param:MYSTRING"
			+ "@example:WEEKOFYEAR('2009-03-20 18:30:01') returns 12",
			},
			new String[] { "WEEKOFYEAR()", "TIMESTAMP", "INT",
			"@function:WEEKOFYEAR(MYTIMESTAMP)"
			+ "@short:Returns the week of the year of a date."
			+ "@example:WEEKOFYEAR('2009-03-20 18:30:01') returns 12",
			},
			new String[] { "DATEDIFF()", "STRING,STRING", "INT",
			"@function:DATEDIFF(MYSTRING1,MYSTRING2)"
			+ "@short:Returns the number of days from startdate to enddate."
			+ "@example:DATEDIFF('2009-03-01', '2009-02-27') = 2."
			},
			new String[] { "DATE_ADD()", "STRING,INT", "DATETIME",
			"@function:DATE_ADD(MYSTRING,MYINT)"
			+ "@short:Adds a number of days to startdate."
			+ "@example:DATE_ADD('2008-12-31', 1) = '2009-01-01'."
			},
			new String[] { "DATE_ADD()", "TIMESTAMP,INT", "DATETIME",
			"@function:DATE_ADD(MYTIMESTAMP,MYINT)"
			+ "@short:Adds a number of days to startdate."
			+ "@example:DATE_ADD('2008-12-31', 1) = '2009-01-01'."
			},
			new String[] { "DATE_SUB()", "STRING,INT", "DATETIME",
			"@function:DATE_SUB(MYSTRING,MYINT)"
			+ "@short:Subtracts a number of days to startdate."
			+ "@example:DATE_SUB('2008-12-31', 1) = '2008-12-30'."
			},
			new String[] { "DATE_SUB()", "TIMESTAMP,INT", "DATETIME",
			"@function:DATE_SUB(MYTIMESTAMP,MYINT)"
			+ "@short:Subtracts a number of days to startdate."
			+ "@example:DATE_SUB('2008-12-31', 1) = '2008-12-30'."
			},
			new String[] { "TRUNC()", "STRING,STRING", "DATETIME",
			"@function:TRUNC(MYSTRING,MYSTRING)"
			+ "@short:Returns a date truncated to a specific unit of measure."
			+ "@description:The unit can be 'YEAR','MONTH'."
			+ "@example:TRUNC('2009-03-20 18:30:01','MONTH') returns '2009-03-01'"
			+ "@example:TRUNC('2009-03-20 18:30:01','YEAR') returns '2009-01-01'"
			},
			new String[] { "TRUNC()", "TIMESTAMP,STRING", "DATETIME",
			"@function:TRUNC(MYTIMESTAMP,MYSTRING)"
			+ "@short:Returns a date truncated to a specific unit of measure."
			+ "@description:The unit can be 'YEAR','MONTH'."
			+ "@example:TRUNC('2009-03-20 18:30:01','MONTH') returns '2009-03-01'"
			+ "@example:TRUNC('2009-03-20 18:30:01','YEAR') returns '2009-01-01'"
			},
			new String[] { "DATE_FORMAT()", "TIMESTAMP,STRING", "STRING",
			"@function:DATE_FORMAT(MYTIMESTAMP,MYSTRING)"
			+ "@short:Converts a timestamp to a value of string in the format specified by the date format fmt."
			+ "@example:DATE_FORMAT('2009-03-20 18:30:01','yyyyMMdd') returns '20090320'"
			},
			new String[] { "DATE_FORMAT()", "STRING,STRING", "STRING",
			"@function:DATE_FORMAT(MYSTRING,MYSTRING)"
			+ "@short:Converts a string to a value of string in the format specified by the date format fmt."
			+ "@example:DATE_FORMAT('2009-03-20 18:30:01','yyyyMMdd') returns '20090320'"
			},
			
		};
		addToFunctionsMap(dateMethods,hiveDateMethods);
		
		String[][] hiveUtilMethods = new String[][] {
			
			new String[] { "NVL()", "ANY,ANY", "ANY",
			"@function:NVL(MYVALUE,MYDEFAULT)"
			+ "@short:If the first value is null, returns the second one."
			+ "@param:Nullable Value "
			+ "@param:Default value"
			+ "@example:NVL(MYSTRING,'DEFAULT')"
			+ "@example:NVL(MYINT,0)",
			},
			new String[] { "ISNULL()", "ANY", "BOOLEAN",
			"@function:ISNULL(MYVALUE)"
			+ "@short:Returns true if a is NULL and false otherwise."
			+ "@param:MYVALUE"
			+ "@example:ISNULL(null) returns TRUE"
			+ "@example:ISNULL(1) returns FALSE",
			},
			new String[] { "ISNOTNULL()", "ANY", "BOOLEAN",
			"@function:ISNOTNULL(MYVALUE)"
			+ "@short:Returns true if a is not NULL and false otherwise."
			+ "@param:MYVALUE"
			+ "@example:ISNOTNULL(null) returns FALSE"
			+ "@example:ISNOTNULL('A') returns TRUE",
			}, 
			new String[] {"RAND()",	"",	"DOUBLE",
			"@function:RAND()"
			+ "@short:Generate a random double between 0 and 1."
			+ "@description:Generates a random double and returns it"
			+ "@example:RAND() can return 0.622535"},
			
		};
		addToFunctionsMap(utilsMethods,hiveUtilMethods);
		
		String[][] hiveAnalyticMethods = new String[][] {
			new String[] { "NTILE() OVER (ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE(X) OVER (ORDER BY MYCOLUMN ASC)"
			+ "@short:Divides an ordered partition into x groups called buckets and assigns a bucket number to each row in the partition."
			+ "@param:X the number of buckets"
			+ "@param:MYCOLUMN the order on which the bucket number is assigned"
			+ "@description:This allows easy calculation of tertiles, quartiles, deciles, percentiles and other common summary statistics."
			+ "@example:NTILE(10) OVER (ORDER BY AGE DESC)",
			},
			new String[] { "NTILE() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "INT", "INT",
			"@function:NTILE(NB_BUCKETS) OVER (PARTITION BY MYCOLUMN1 ORDER BY MYCOLUMN2 ASC)"
			+ "@short:Divides an ordered partition into x groups called buckets and assigns a bucket number to each row in the partition."
			+ "@param:X the number of buckets"
			+ "@param:MYCOLUMN1 Calculates buckets on sections that share a common attribute"
			+ "@param:MYCOLUMN2 The order on which the bucket number is assigned"
			+ "@description:This allows easy calculation of tertiles, quartiles, deciles, percentiles and other common summary statistics."
			+ "@example:NTILE(10) OVER (PARTITION BY city ORDER BY age DESC)",
			},
			new String[] { "RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:RANK() OVER (ORDER BY MYCOLUMN ASC)"
			+ "@short:Returns the rank of a row in a given order. Some rows may have the same rank."
			+ "@param:MYCOLUMN The order on which the rank is calculated"
			+ "@description:The rank function can cause non-consecutive rankings if the tested values are the same. For assigning a unique integer to a row please see ROW_NUMBER()."
			+ "@example:RANK() OVER (ORDER BY age ASC)",
			},
			new String[] { "RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:RANK() OVER (PARTITION BY MYCOLUMN1 ORDER BY MYCOLUMN2 ASC)"
			+ "@short:Returns the rank of a row in a given order for every partition. Some rows may have the same rank."
			+ "@param:MYCOLUMN1 Calculates ranks on sections that share a common attribute"
			+ "@param:MYCOLUMN2 The order on which the rank is calculated"
			+ "@description:The rank function can cause non-consecutive rankings. For assigning a unique integer to a row please see ROW_NUMBER()."
			+ "@example:RANK() OVER (PARTITION BY city ORDER BY age ASC)",
			},
			new String[] { "DENSE_RANK() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:DENSE_RANK() OVER (ORDER BY MYCOLUMN ASC)"
			+ "@short:Returns the rank of a row in a given order. Some rows may have the same rank."
			+ "@param:MYCOLUMN The order on which the rank is calculated"
			+ "@description:The ranks are consecutive integers beginning with 1. For assigning a unique integer to a row please see ROW_NUMBER()"
			+ "@example:DENSE_RANK() OVER (ORDER BY age ASC)",
			},
			new String[] { "DENSE_RANK() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:DENSE_RANK() OVER (PARTITION BY EXPR MYCOLUMN1 BY MYCOLUMN2  ASC)"
			+ "@short:Returns the rank of a row in a given order for every partition. Some rows may have the same rank."
			+ "@param:MYCOLUMN1 Ranks buckets on sections that share a common attribute"
			+ "@param:MYCOLUMN2 The order on which the rank is calculated"
			+ "@description:The ranks are consecutive integers beginning with 1. For assigning a unique integer to a row please see ROW_NUMBER()"
			+ "@example:DENSE_RANK() OVER (PARTITION BY city ORDER BY age ASC)",
			},
			new String[] { "ROW_NUMBER() OVER (ORDER BY EXPR ASC)", "", "INT",
			"@function:ROW_NUMBER() OVER (ORDER BY MYCOLUMN ASC)"
			+ "@short:Assigns a unique number to each row beginning with 1."
			+ "@param:MYCOLUMN1 The order on which the unique number is assigned"
			+ "@example:ROW_NUMBER() OVER (ORDER BY age ASC)",
			},
			new String[] { "ROW_NUMBER() OVER (PARTITION BY EXPR ORDER BY EXPR ASC)", "", "INT",
			"@function:ROW_NUMBER() OVER (PARTITION BY MYCOLUMN1 ORDER BY MYCOLUMN2 ASC)"
			+ "@short:Assigns a unique number to each row within a group beginning with 1."
			+ "@param:MYCOLUMN1 Calculates ids on sections that share a common attribute"
			+ "@param:MYCOLUMN2  The order on which the unique number is assigned"
			+ "@example:ROW_NUMBER() OVER (PARTITION BY city ORDER BY age ASC)",
			},
			
			//WindowMethods
			new String[] { "LEAD() OVER (ORDER BY EXPR ASC)", "ANY,INT,ANY", "ANY",
			"@function:LEAD(MYCOLUMN,MYOFFSET,MYDEFAULT) OVER (ORDER BY EXPR ASC)"
			+ "@short:Provides access to a row ahead"
			+ "@param:EXPR the expression to calculate on the target row"
			+ "@param:MYOFFSET The relative position of the row going onward"
			+ "@param:MYDEFAULT The default value for the last rows"
			+ "@param:MYEXPR The order of the rows"
			+ "@example:LEAD(age,1,null) OVER (ORDER BY age ASC)"
			+ "@example:LEAD(age,1,null) OVER (ORDER BY age DESC)",
			},
			new String[] { "LEAD() OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)", "ANY,INT,ANY", "ANY",
			"@function:LEAD(MYCOLUMN1,MYOFFSET,MYDEFAULT) OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)"
			+ "@short:Provides access to a row ahead within a group"
			+ "@param:MYCOLUMN1 The expression to calculate on the target row"
			+ "@param:MYOFFSET The relative position of the row going onward"
			+ "@param:MYDEFAULT The default value for the last rows"
			+ "@param:MYCOLUMN2 Calculates ids on sections that share something in common"
			+ "@param:MYCOLUMN3 The order of the rows"
			+ "@example:LEAD(age,1,null) OVER (PARTITION BY city ORDER BY age ASC)"
			+ "@example:LEAD(age,1,null) OVER (PARTITION BY city ORDER BY age DESC)",
			},
			new String[] { "LAG() OVER (ORDER BY MYCOLUMN2 ASC)", "ANY,INT,ANY", "ANY",
			"@function:LAG(MYCOLUMN1,MYOFFSET,MYDEFAULT) OVER (ORDER BY MYCOLUMN2 ASC)"
			+ "@short:Provides access to a row behind within a group"
			+ "@param:MYCOLUMN1 The expression to calculate on the target row"
			+ "@param:MYOFFSET The relative position of the row going backward"
			+ "@param:MYDEFAULT The default value for the first rows"
			+ "@param:MYCOLUMN2 Calculates lag on sections"
			+ "@param:MYCOLUMN3 The order of the rows"
			+ "@example:LAG(age,1,null) OVER (ORDER BY age ASC)"
			+ "@example:LAG(age,1,null) OVER (ORDER BY age DESC)",
			},
			new String[] { "LAG() OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)", "ANY,INT,ANY", "ANY",
			"@function:LAG(MYCOLUMN1,MYOFFSET,MYDEFAULT) OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)"
			+ "@short:Provides access to a row behind within a group"
			+ "@param:MYCOLUMN1 The expression to calculate on the target row"
			+ "@param:MYOFFSET The relative position of the row going backward"
			+ "@param:MYDEFAULT The default value for the first rows"
			+ "@param:EXPR Calculates lag on sections"
			+ "@param:EXPR The order of the rows"
			+ "@example:LAG(age,1,null) OVER (PARTITION BY city ORDER BY age ASC)"
			+ "@example:LAG(age,1,null) OVER (PARTITION BY city ORDER BY age DESC)",
			},
			new String[] { "FIRST_VALUE() OVER (ORDER BY MYCOLUMN2 ASC)", "ANY", "ANY",
			"@function:FIRST_VALUE(MYCOLUMN1) OVER (ORDER BY MYCOLUMN2 ASC)"
			+ "@short:Provides access to the first record"
			+ "@param:MYCOLUMN1 The expression to calculate of the first row"
			+ "@param:MYCOLUMN2 The order of the rows"
			+ "@example:FIRST_VALUE(age) OVER (ORDER BY age ASC)",
			},
			new String[] { "FIRST_VALUE() OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)", "ANY", "ANY",
			"@function:FIRST_VALUE(MYCOLUMN1) OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)"
			+ "@short:Provides access to the first record from the partition"
			+ "@param:MYCOLUMN1 The expression to calculate of the first row"
			+ "@param:MYCOLUMN2 The section for which it is calculated"
			+ "@param:MYCOLUMN3 The order of the rows"
			+ "@example:FIRST_VALUE(age) OVER (PARTITION BY city ORDER BY age DESC)",
			},
			new String[] { "LAST_VALUE() OVER (ORDER BY EXPR ASC)", "", "",
			"@function:FIRST_VALUE(MYCOLUMN1) OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)"
			+ "@short:Provides access to the last record"
			+ "@param:EXPR the expression to calculate of the first row"
			+ "@param:EXPR the order of the rows"
			+ "@example:LAST_VALUE(age) OVER (ORDER BY age ASC)",
			},
			new String[] { "LAST_VALUE() OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)", "ANY", "ANY",
			"@function:LAST_VALUE(MYCOLUMN1) OVER (PARTITION BY MYCOLUMN2 ORDER BY MYCOLUMN3 ASC)"
			+ "@short:Provides access to the last record from the partition"
			+ "@param:MYCOLUMN1 The expression to calculate of the first row"
			+ "@param:MYCOLUMN2 The section for which it is calculated"
			+ "@param:MYCOLUMN3 The order of the rows"
			+ "@example:LAST_VALUE(age) OVER (PARTITION BY city ORDER BY age DESC)",
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
		addToFunctionsMap(analyticMethods,hiveAnalyticMethods);
		
		String[][] hiveAggregationsMethods = new String[][] {
			new String[] { "CORR()", "NUMBER,NUMBER", "DOUBLE",
			"@function:CORR(MYNUMBER1,MYNUMBER2)"
			+ "@short:Returns the correlation between two columns."
			+ "@param:MYNUMBER1"
			+ "@param:MYNUMBER2"
			+ "@description:Returns the Pearson coefficient of correlation of a pair of a numeric columns in the group."
			+ "@example:CORR(nb_article,price)",
			},
			new String[] { "VARIANCE()", "NUMBER", "DOUBLE",
			"@function:VARIANCE(MYNUMBER)"
			+ "@short:Returns the variance of a column."
			+ "@param:MYNUMBER"
			+ "@description:Returns the variance of a numeric column in the group."
			+ "@example:VARIANCE(nb_article)",
			},
			new String[] { "STDDEV()", "NUMBER", "DOUBLE",
			"@function:STDDEV(MYNUMBER)"
			+ "@short:Returns the standard deviation of a column."
			+ "@param:MYNUMBER"
			+ "@description:Returns the standard deviation of a numeric column in the group."
			+ "@example:STDDEV(nb_article)",
			},
			new String[] { "PERCENTILE()", "", "",
			"@function:PERCENTILE(MYLONG, MYPERCENTILE))"
			+ "@short:Returns the value of the given percentile."
			+ "@param:MYLONG"
			+ "@param:MYPERCENTILE"
			+ "@description:Returns the exact pth percentile of a column in the group (does not work with floating point types). p must be between 0 and 1. NOTE: A true percentile can only be computed for integer values. Use PERCENTILE_APPROX if your input is non-integral."
			+ "@example:PERCENTILE(nb_article,0.9)",
			},
			new String[] { "PERCENTILE_APPROX()", "", "",
			"@function:PERCENTILE_APPROX(MYDOUBLE, MYPERCENTILE)"
			+ "@short:Returns the standard deviation of a column."
			+ "@param:MYDOUBLE"
			+ "@param:MYPERCENTILE"
			+ "@description:Returns an approximate pth percentile of a numeric column (including floating point types) in the group. p must be between 0 and 1."
			+ "@example:PERCENTILE(price,0.1)",
			}
			
		};
		addToFunctionsMap(agregationMethods,hiveAggregationsMethods);
	}
	
}