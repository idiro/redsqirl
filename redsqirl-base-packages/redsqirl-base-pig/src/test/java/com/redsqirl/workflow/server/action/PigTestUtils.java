package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class PigTestUtils {

	private static Logger logger = Logger.getLogger(PigTestUtils.class);
	
	public static Map<String, String> getEmptyProperties() {
		Map<String, String> ans = new HashMap<String, String>();
		return ans;
	}
	

	public static void createHDFSFile(Path p, String containt) throws IOException {
		FileSystem fileSystem = NameNodeVar.getFS();

		// Check if the file already exists
		if (fileSystem.exists(p)) {
			logger.warn("File " + p.toString() + " already exists");
			return;
		}

		// Create a new file and write data to it.
		fileSystem.mkdirs(p);
		FSDataOutputStream out = fileSystem.create(new Path(p, "part-0000"));
		out.write(containt.getBytes());
		out.close();
		fileSystem.close();
	}
	
	public static void createDistinctValueAuditFile(Path p) throws IOException {
		String ctrA = new String(new char[]{'\001'});
		String value="Distinct values"+ctrA+"{(0),(1)}"+ctrA+"\n";
		createHDFSFile(p, value);
	}

	public static void create3INT_file(Path p) throws IOException {

		String training = "1;44;10\n";
		training += "6;8;4\n";
		training += "3;5;8\n";
		training += "7;3;9\n";
		training += "9;4;5\n";

		createHDFSFile(p, training);
	}
	
	public static void createStringInt_file(Path p) throws IOException {
		String content = "A,1\n";
		content += "B,2\n";
		content += "C,3\n";
		content += "D,4\n";
		content += "E,5\n";
		content += "F,6\n";
		content += "G,7\n";

		createHDFSFile(p, content);
	}
	
	public static void createIntString_file(Path p) throws IOException {
		String content = "1,A\n";
		content += "2,B\n";
		content += "3,C\n";
		content += "4,D\n";
		content += "5,E\n";
		content += "6,F\n";
		content += "7,G\n";

		createHDFSFile(p, content);
	}
	
	public static void createStringIntString_file(Path p) throws IOException {
		String content = "A,1,A\n";
		content += "B,2,B\n";
		content += "C,3,C\n";
		content += "D,4,D\n";
		content += "E,5,E\n";
		content += "F,6,F\n";
		content += "G,7,G\n";

		createHDFSFile(p, content);
	}

	
	public static DataFlowElement createSrc_ID_VALUE_RAW(Workflow w, HDFSInterface hInt,
			String new_path1) throws RemoteException, Exception {

		hInt.delete(new_path1);
		PigTestUtils.create3INT_file(new Path(new_path1));
		
		String idSource = w.addElement((new PigTextSource()).getName());
		PigTextSource src = (PigTextSource) w.getElement(idSource);

		src.update(src.getInteraction(PigTextSource.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(PigTextSource.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output")
				.add("property").add(MapRedTextType.key_delimiter).add(";");
		
		src.getDFEOutput().get("").addProperty(MapRedTextType.key_header, "ID INT, VALUE INT, RAW INT");

		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("ID");
		field1.add("type").add("INT");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("VALUE");
		field2.add("type").add("INT");
		Tree<String> field3 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field3.add("name").add("RAW");
		field3.add("type").add("INT");
		
		
		String error = src.updateOut();
		assertTrue("source update: " + error, error == null);
		
		assertTrue("number of fields in source should be 3 instead of " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize() == 3);
		
		List<String> fields = new LinkedList<String>();
		fields.add("ID");
		fields.add("VALUE");
		fields.add("RAW");
		assertTrue("field list " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().containsAll(fields));
		
		return src;
	}
	
	public static DataFlowElement createSourceEmpty_ID_VALUE(Workflow w,String path) throws Exception{
		HDFSInterface hInt = new HDFSInterface();
		
		hInt.delete(path);
		assertTrue("create "+path,
				hInt.create(path, getEmptyProperties()) == null
				);
		String idSource = w.addElement((new PigTextSource()).getName());
		PigTextSource src = (PigTextSource)w.getElement(idSource);

		src.update(src.getInteraction(PigTextSource.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(PigTextSource.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(path);
		
		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("ID");
		field1.add("type").add("STRING");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("VALUE");
		field2.add("type").add("INT");
		
		logger.info(dataSetTree.toString());
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		

		FieldList fl = new OrderedFieldList();
		fl.addField("ID", FieldType.STRING);
		fl.addField("VALUE", FieldType.INT);
		src.getDFEOutput().get(PigTextSource.out_name).setFields(fl);
		
		assertTrue("number of fields in source should be 2 instead of " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize() == 2);
		
		return src;
	}
	
	public static DataFlowElement createSrc_ID_VALUE(
			Workflow w,
			HDFSInterface hInt, 
			String new_path1 ) throws RemoteException, Exception{
		
		String idSource = w.addElement((new PigTextSource()).getName());
		PigTextSource src = (PigTextSource)w.getElement(idSource);
		
		hInt.delete(new_path1);
		createStringInt_file(new Path(new_path1));
		
		src.update(src.getInteraction(PigTextSource.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(PigTextSource.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedTextType.key_delimiter).add(",");

		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("ID");
		field1.add("type").add("STRING");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("VALUE");
		field2.add("type").add("INT");
		
		String error = src.updateOut();
		
		
		assertTrue("source update: "+error,error == null);
		
		assertTrue("number of fields in source should be 2 instead of " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize() == 2);
		
		assertTrue("field list " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().contains("ID"));
		assertTrue("field list " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().contains("VALUE"));
		
		return src;
	}
	
	public static DataFlowElement createSrc_INDEX_VALUE(
			Workflow w,
			HDFSInterface hInt, 
			String new_path1 ) throws RemoteException, Exception{
		
		String idSource = w.addElement((new Source()).getName());
		Source src = (Source)w.getElement(idSource);
		
		hInt.delete(new_path1);
		createIntString_file(new Path(new_path1));
		
		MapRedCtrlATextType type = new MapRedCtrlATextType();
		
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add(type.getBrowser());
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").removeAllChildren();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(type.getTypeName());
		
		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedTextType.key_delimiter).add(",");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("Value");
		field2.add("type").add("STRING");
		
		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("Index");
		field1.add("type").add("STRING");
		
		String error = src.updateOut();
		
		
		assertTrue("source update: "+error,error == null);
		
		assertTrue("number of fields in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFields().getSize(),
				src.getDFEOutput().get(Source.out_name).getFields().getSize() == 2);
		
		assertTrue("field list " + 
				src.getDFEOutput().get(Source.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields().getFieldNames().contains("Value"));
		assertTrue("field list " + 
				src.getDFEOutput().get(Source.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(Source.out_name).getFields().getFieldNames().contains("Index"));
		
		return src;
	}
	
	public static DataFlowElement createSrc_ID_2VALUE(
			Workflow w,
			HDFSInterface hInt, 
			String new_path1 ) throws RemoteException, Exception{
		
		String idSource = w.addElement((new PigTextSource()).getName());
		PigTextSource src = (PigTextSource)w.getElement(idSource);
		
		hInt.delete(new_path1);
		createStringIntString_file(new Path(new_path1));
		
		src.update(src.getInteraction(PigTextSource.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(PigTextSource.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedTextType.key_delimiter).add(",");

		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("ID");
		field1.add("type").add("STRING");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("VALUE");
		field2.add("type").add("INT");
		
		Tree<String> field3 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field3.add("name").add("VALUE2");
		field3.add("type").add("STRING");
		
		String error = src.updateOut();
		
		
		assertTrue("source update: "+error,error == null);
		
		assertTrue("number of fields in source should be 3 instead of " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getSize() == 3);
		
		assertTrue("field list " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().contains("ID"));
		assertTrue("field list " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().contains("VALUE"));
		assertTrue("field list " + 
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
				src.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().contains("VALUE2"));
		
		return src;
	}
	
	
	
}
