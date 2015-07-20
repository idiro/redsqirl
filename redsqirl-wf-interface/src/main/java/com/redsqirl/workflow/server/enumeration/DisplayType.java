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