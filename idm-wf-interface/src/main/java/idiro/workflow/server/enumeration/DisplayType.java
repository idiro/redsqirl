package idiro.workflow.server.enumeration;

public enum DisplayType {

	//Format in: input>[regex>re]/[output>choice]
	input,
	
	//Format in: list>[values>[value>choice]]/[display>type]/[output>choicei]
	list,
	
	//Format in: applist>[values>[value>choice]]/[display>type]/[output>[value>choicei]]
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
	//                                             [values>[value>choice(optional)]]/
	//											   [regex>reg(optional)]]/
	//                                 [editor (see helpTextEditor)]/
	//                 [row>[col_title>value]]
	//                 [generator>operation>[title>value]/
	//                                      [row>col_title>value]]
	table,
	
}