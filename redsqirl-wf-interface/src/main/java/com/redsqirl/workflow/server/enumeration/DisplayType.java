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

package com.redsqirl.workflow.server.enumeration;

/**
 * Enumerations that depict how the tree should look for different display types
 * 
 * @author keith
 * 
 */
public enum DisplayType {

	// Format in: input>[regex>re]/[output>choice]
	/**
	 * <pre>
	 *  <code>
	 * input >[regex>re]
	 *        [output>choice]
	 * </code>
	 * </pre>
	 */
	input,

	// Format in: list>[values>[value>choice]]/[display>type]/[output>choicei]
	/**
	 * <pre>
	 *  <code>
	 * list >[values>[value>choice]]
	 *       [display>type]
	 *       [output>choicei]
	 * </code>
	 * </pre>
	 */
	list,

	// Format in:
	// applist>[values>[value>choice]]/[display>type]/[output>[value>choicei]]
	/**
	 * <pre>
	 * <code>
	 * applist >[values>[value>choice]]
	 * 		    [display>type]
	 * 			[output>[value>choicei]]
	 * </code>
	 * </pre>
	 */
	appendList,

	// Browser
	// Format in: browse>[type>DataType.getName()]/[subtype>namei]
	// [output>[name>myname]/[path>mypath]/[field>[name>value]/[type>value]]/[property>namei>valuei]]
	/**
	 * <pre>
	 * <code>
	 * browse >[type>DataType.getName()]
	 * 		   [subtype>namei]
	 * 		   [output>[name>myname]
	 * 				   [path>mypath]
	 * 				   [field>[name>value]
	 * 				   [type>value]]
	 * 	       		   [property>namei>valuei]]
	 * </code>
	 * </pre>
	 */
	browser,

	// Editor
	// Format in: editor>[keywords>fields>field>[name>value]]/[type>value]]/
	// [help>submenu>[name>value]/[suggestion>[word>value]/[input>value]/[return>value]]]/
	// [output>text]
	/**
	 * <pre>
	 * <code>
	 * editor>[keywords>fields>field>[name>value]]
	 * 							[type>value]]
	 * 							[help>submenu>[name>value]
	 * 										[suggestion>[word>value]
	 * 													[input>value]
	 * 													[return>value]]]
	 * 							[output>text]
	 * </code>
	 * </pre>
	 */
	helpTextEditor,

	// Table
	// Format in: table>[fields>field>[title>value]/
	// [constraint>[count>i(optional)]/
	// [values>[value>choice(optional)]]/
	// [regex>reg(optional)]]/
	// [editor (see helpTextEditor)]/
	// [row>[col_title>value]]
	// [generator>operation>[title>value]/
	// [row>col_title>value]]
	/**
	 * <pre>
	 * <code>
	 * table>[fields>field>[title>value]
	 * 						 [constraint>[count>i(optional)]
	 * 						 [values>[value>choice(optional)]]
	 * 						 [regex>reg(optional)]]
	 * 						 [editor (see helpTextEditor)]
	 * 		 [row>[col_title>value]] 
	 *       [generator>operation>[title>value]
	 *       					  [row>col_title>value]]
	 * </code>
	 * </pre>
	 */
	table,

}