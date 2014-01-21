package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.hadoop.NameNodeVar;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DataFlowElement;

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

	
	public static DataFlowElement createSrc_ID_VALUE_RAW(Workflow w, HDFSInterface hInt,
			String new_path1) throws RemoteException, Exception {

		hInt.delete(new_path1);
		PigTestUtils.create3INT_file(new Path(new_path1));
		
		String idSource = w.addElement((new Source()).getName());
		Source src = (Source) w.getElement(idSource);

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");

		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubtypeTree = src.getInteraction(
				Source.key_datasubtype).getTree();
		dataSubtypeTree.getFirstChild("list").getFirstChild("output")
				.add(new MapRedTextType().getTypeName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output")
				.add("property").add(MapRedTextType.key_delimiter).add(";");

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("INT");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		Tree<String> feat3 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat3.add("name").add("RAW");
		feat3.add("type").add("INT");
		
		
		String error = src.updateOut();
		assertTrue("source update: " + error, error == null);
		
		assertTrue("number of features in source should be 3 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 3);
		
		List<String> feats = new LinkedList<String>();
		feats.add("ID");
		feats.add("VALUE");
		feats.add("RAW");
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().containsAll(feats));
		
		return src;
	}
	
	public static DataFlowElement createSourceEmpty_ID_VALUE(Workflow w,String path) throws Exception{
		HDFSInterface hInt = new HDFSInterface();
		
		hInt.delete(path);
		assertTrue("create "+path,
				hInt.create(path, getEmptyProperties()) == null
				);
		String idSource = w.addElement((new Source()).getName());
		Source src = (Source)w.getElement(idSource);

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(new MapRedTextType().getTypeName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(path);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		

		FeatureList fl = new OrderedFeatureList();
		fl.addFeature("ID", FeatureType.STRING);
		fl.addFeature("VALUE", FeatureType.INT);
		src.getDFEOutput().get(Source.out_name).setFeatures(fl);
		
		assertTrue("number of features in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 2);
		
		return src;
	}
	
	public static DataFlowElement createSrc_ID_VALUE(
			Workflow w,
			HDFSInterface hInt, 
			String new_path1 ) throws RemoteException, Exception{
		
		String idSource = w.addElement((new Source()).getName());
		Source src = (Source)w.getElement(idSource);
		
		hInt.delete(new_path1);
		createStringInt_file(new Path(new_path1));
		
		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(new MapRedTextType().getTypeName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedTextType.key_delimiter).add(",");

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		
		assertTrue("number of features in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 2);
		
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().contains("ID"));
		assertTrue("Feature list " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getFeaturesNames().contains("VALUE"));
		
		return src;
	}
	
	
	
}
