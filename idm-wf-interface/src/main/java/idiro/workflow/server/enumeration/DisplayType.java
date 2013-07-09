package idiro.workflow.server.enumeration;

public enum DisplayType {
	//Format in: list>[value>choice1,choice2...]/[output>choicei]
	list,
	
	//Format in: applist>[value>choice1,choice2...]/[output>[value>choicei]]
	appendList,
	
	//Browser
	//Format in: browse>[type>DataType.getName()]/[subtype>namei]
	//                  [output>[path>mypath]/[feature>[name>value]/[type>value]]/[property>namei>valuei]]
	browser,
	
	//Editor
	//Format in: editor>[keywords>features>feature>[name>value]]/[type>value]]/
	//                  [help>submenu>[name>value]/[suggestion>[word>value]/[input>value]/[return>value]]]/
	//                  [output>text]
	helpTextEditor,
	
	//Table
	//Format in: table>[columns>column>[title>value]/
	//                                 [constraint>[count>i(optional)]/
	//                                             [value>choice1,choice2...(optional)]]/
	//                                 [editor (see helpTextEditor)]/
	//                 [row>[col_title>value]]
	//                 [generator>operation>[title>value]/
	//                                      [row>col_title>value]]
	table,
}
