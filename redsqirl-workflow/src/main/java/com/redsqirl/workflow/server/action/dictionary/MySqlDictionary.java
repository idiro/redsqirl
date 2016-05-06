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

public class MySqlDictionary extends JdbcDictionary{


	/** Instance */
	protected static MySqlDictionary instance;

	protected static final String dateFormats = "<table>"
			+ "<tr bgcolor=\"#ccccff\">"
			+ "   <th align=left>Date and Time Pattern"
			+ "   <th align=left>Result"
			+ "<tr bgcolor=\"#eeeeff\">"
			+"    <td><code>\"%a, %b %e, %y\"</code>"
			+"    <td><code>Wed, Jul 4, 01</code>"
			+" <tr>"
			+"     <td><code>\"%h:%i %p\"</code>"
			+"     <td><code>12:08 PM</code>"
			+" <tr bgcolor=\"#eeeeff\">"
			+"     <td><code>\"%y%m%d%k%i%s\"</code>"
			+"     <td><code>010704120856</code>"
			+" <tr>"
			+"     <td><code>\"%Y-%m-%dT%T\"</code>"
			+"     <td><code>2001-07-04T12:08:56</code>"
			+" <tr>"
			+"     <td><code>\"%Y-W%U-%w\"</code>"
			+"     <td><code>2001-W27-3</code>"
			+"	</table>";
	
	/**
	 * Get an instance of the dictionary
	 * 
	 * @return instance
	 */
	public static MySqlDictionary getInstance() {
		if (instance == null) {
			instance = new MySqlDictionary();
		}
		return instance;
	}
	
	private MySqlDictionary(){
		super("mysql");
	}
	
	protected void loadDefaultFunctions() {
		super.loadDefaultFunctions();
		
		functionsMap
				.put(castMethods,
						new String[][] { 
					new String[] { "CAST( AS INTEGER)", "ANY", "INT",
							"@function:CAST@short:returns an integer value.@example: CAST('3' AS INT) returns 3"},
					new String[] { "CAST( AS DECIMAL(10,4))", "ANY", "DOUBLE",
							"@function:CAST@short:returns a double value.@example: CAST('3.1' AS NUMBER) returns 3.1" },
					new String[] { "CAST( AS CHAR(50))", "ANY", "STRING",
							"@function:CAST@short:returns a string value.@example: CAST(3 AS VARCHAR2(3)) returns '3'"},
					new String[] { "STR_TO_DATE()", "STRING,STRING", "DATETIME",
					"@function:STR_TO_DATE@short:returns the date value of the object following given in the format."+dateFormats+
					"@example: STR_TO_DATE('20160201','%y%M%d')"},
					new String[] { "TIMESTAMP()", "STRING", "DATETIME",
					"@function:TO_DATE@short:returns the date value of the object.@example: TIMESTAMP('2016-02-01')"}
				});
		
		String[][] extraMathFunctions = new String[][] {
			new String[] { "TRUNCATE()", "NUMBER,INT", "NUMBER",
			"@function:TRUNC@short:returns a the number truncated.@param:X the number to truncate@param:D the precision to keep.@description:Returns the number X, truncated to D decimal places."}
		};
		addToFunctionsMap(stringMethods,extraMathFunctions);
		
		String[][] mySqlStringMethods = new String[][] {
			new String[] { "INSTR()", "STRING,STRING", "INT",
					"@function:INSTR@short:returns the location of a substring in a string."+
					"@param: STRING@param: SUBSTRING"+
					"@example: INSTR('Hello world!', 'l') returns 3 (first l)"+
					"@example: INSTR('Hello world!', 'ello') returns 2 (e)"
			},
			new String[] {
					"YEAR()",
					"STRING",
					"INT",
					"@function:YEAR( STRING )@short:Extracts the year from String timestamps @param:STRING the string that contains a date@description:Returns the year part of a timestamp string@example: YEAR(\"1970-01-01\") returns \"1970\"" },
			new String[] {
					"MONTH()",
					"STRING",
					"INT",
					"@function:MONTH( STRING )@short:Extracts the MONTH from String timestamps @param:STRING the string that contains a date@description:Returns the MONTH part of a timestamp string@example: MONTH(\"1970-11-01 \") returns \"11\"" },
			new String[] {
					"DAY()",
					"STRING",
					"INT",
					"@function:DAY( STRING )@short:Extracts the DAY from String timestamps @param:STRING the string that contains a date@description:Returns the DAY part of a timestamp string@example: DAY(\"1970-11-15 \") returns \"15\"" },
		};
		addToFunctionsMap(stringMethods,mySqlStringMethods);
		
		String[][] mySqlDateMethods = new String[][] {
			new String[] {
					"FROM_UNIXTIME()",
					"LONG",
					"TIMESTAMP",
					"@function:FROM_UNIXTIME(UNIX_TIME)"
							+"@short:Converts the number of seconds from unix epoch to a datetime."
							+"@param: The unix time to convert "
							+"@description:Converts the number of seconds from unix epoch (1970-01-01 00:00:00 UTC) to a timestamp."
							+"@example: returns 1" },
			new String[] {
					"FROM_UNIXTIME()",
					"LONG,STRING",
					"TIMESTAMP",
					"@function:FROM_UNIXTIME(UNIX_TIME,FORMAT)"
							+"@short:Converts the number of seconds from unix epoch to a string."
							+"@param:TIMESTAMP The unix time to convert.@param:FORMAT The date format."+dateFormats
							+"@description: Converts the number of seconds from unix epoch (1970-01-01 00:00:00 UTC) to a string representing the timestamp of that moment in the current system time zone in the format of '1970-01-01 00:00:00'"
							+"@example: returns 1" },
			new String[] {
					"UNIX_TIMESTAMP()",
					"",
					"LONG",
					"@function:UNIX_TIMESTAMP()"
							+"@short:Gets current Unix timestamp in seconds."
							+"@description: Gets current Unix timestamp in seconds."
							+"@example: returns 1" },
			new String[] {
					"UNIX_TIMESTAMP()",
					"TIMESTAMP",
					"LONG",
					"@function:UNIX_TIMESTAMP(TIMESTAMP)"
							+"@short:Converts timestamp to the number of seconds from unix epoch (1970-01-01 00:00:00 UTC)"
							+"@param:TIMESTAMP The timestamp to convert."
							+"@description: Converts timestamp to Unix timestamp (in seconds), using the default timezone and the default locale, return 0 if fail: unix_timestamp('2009-03-20 11:30:01') = 1237573801"
							+"@example: returns 1" },
			new String[] {
					"UNIX_TIMESTAMP()",
					"STRING,STRING",
					"LONG",
					"@function:FROM_UNIXTIME(STR_DATE,FORMAT)"
							+"@short:Convert time string with given pattern."
							+"@param:STR_DATE The string date to convert.@param:FORMAT The date format."+dateFormats
							+"@description: Convert time string with given pattern to Unix time stamp (in seconds), return 0 if fail: unix_timestamp('2009-03-20', 'yyyy-MM-dd') = 1237532400"
							+"@example: returns 1" },
			new String[] {
					"YEAR()",
					"TIMESTAMP",
					"INT",
					"@function:YEAR(TIMESTAMP)"
							+"@short: Returns the year part of a date or a timestamp"
							+"@param:TIMESTAMP A timestamp "
							+"@description: Returns the year part of a date or a timestamp: year('1970-01-01 00:00:00') = 1970, year('1970-01-01') = 1970"
							+"@example: returns 1" },
			new String[] {
					"MONTH()",
					"TIMESTAMP",
					"INT",
					"@function:MONTH(TIMESTAMP)"
							+"@short:Returns the month part of a date or a timestamp."
							+"@param:TIMESTAMP A timestamp "
							+"@description: Returns the month part of a date or a timestamp: month('1970-11-01 00:00:00') = 11, month('1970-11-01') = 11"
							+"@example: returns 1" },
			new String[] {
					"DAY()",
					"TIMESTAMP",
					"INT",
					"@function:DAY(TIMESTAMP)"
							+"@short: Return the day part of a date or a timestamp."
							+"@param:TIMESTAMP A timestamp "
							+"@description: Return the day part of a date or a timestamp: day('1970-11-01 00:00:00') = 1, day('1970-11-01') = 1"
							+"@example: returns 1" },
			new String[] {
					"HOUR()",
					"TIMESTAMP",
					"INT",
					"@function:HOUR(TIMESTAMP)"
							+"@short: Returns the hour of the timestamp."
							+"@param:TIMESTAMP A timestamp "
							+"@description: Returns the hour of the timestamp: hour('2009-07-30 12:58:59') = 12, hour('12:58:59') = 12"
							+"@example: returns 1" },
			new String[] {
					"MINUTE()",
					"TIMESTAMP",
					"INT",
					"@function:MINUTE(TIMESTAMP)"
							+"@short: Returns the minute of the timestamp."
							+"@param:TIMESTAMP A timestamp "
							+"@description: Returns the minute of the timestamp."
							+"@example: returns 1" },
			new String[] {
					"SECOND()",
					"TIMESTAMP",
					"INT",
					"@function:SECOND(TIMESTAMP)"
							+"@short: Returns the second of the timestamp."
							+"@param:TIMESTAMP A timestamp "
							+"@description: Returns the second of the timestamp."
							+"@example: returns 1" },
			new String[] {
					"WEEKOFYEAR()",
					"TIMESTAMP",
					"INT",
					"@function:WEEKOFYEAR(TIMESTAMP)"
							+"@short: Return the week number of a timestamp string: weekofyear('1970-11-01 00:00:00') = 44, weekofyear('1970-11-01') = 44"
							+"@param:TIMESTAMP A timestamp "
							+"@description: "
							+"@example: returns 1" },
			new String[] {
					"DATEDIFF()",
					"TIMESTAMP,TIMESTAMP",
					"INT",
					"@function:DATEDIFF(TIMESTAMP,TIMESTAMP)"
							+"@short: Return the number of days from startdate to enddate."
							+"@param:TIMESTAMP A timestamp "
							+"@description: Return the number of days from startdate to enddate: datediff('2009-03-01', '2009-02-27') = 2."
							+"@example: returns 1" },
			new String[] {
					"DATE_ADD()",
					"DATE,INT",
					"DATE",
					"@function:DATE_ADD(DATE,DAYS_TO_ADD)"
							+"@short: Add a number of days to startdate."
							+"@param:DATE a date @param:DAYS_TO_ADD The number of days to add."
							+"@description: Add a number of days to startdate: date_add('2008-12-31', 1) = '2009-01-01'"
							+"@example: returns 1" },
			new String[] {
					"DATE_SUB()",
					"DATE,INT",
					"DATE",
					"@function:DATE_SUB(DATE,DAYS_TO_SUB)"
							+"@short: Substract a number of days to startdate."
							+"@param:DATE a date @param:DAYS_TO_SUB The number of days to substract."
							+"@description: Substract a number of days to startdate:  date_sub('2008-12-31', 1) = '2008-12-30'"
							+"@example: returns 1" }
		};
		addToFunctionsMap(dateMethods,mySqlDateMethods);
		
	}
}
