package idiro.workflow.server.action;

import idiro.workflow.server.interaction.SampleInteraction;

import java.rmi.RemoteException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEOutput;


public class ActionSample extends DemoAction {
	
	public SampleInteraction sample;
	private Page page1;

	public ActionSample() throws RemoteException {
		super();

		page1 = addPage("Sample",
				"Sample Data with Pig", 1);

		sample = new SampleInteraction("sample",
				"Sample Interaction",
				"Set the rate to sample the data", 0,
				0);

		page1.addInteraction(sample);
		page1.addInteraction(delimiterOutputInt);
		page1.addInteraction(savetypeOutputInt);
	}

	public String getName() throws RemoteException {
		return "sample";
	}

	@Override
	public String getQuery() throws RemoteException {
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			// Output
			DFEOutput out = output.values().iterator().next();
			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";
			String loader = getCurrentName();
			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";
			String sampleval = getNextName() + " = "
					+ sample.getQueryPiece(loader) + "\n\n";
			String store = getStoreQueryPiece(out, getCurrentName());

			if (sampleval != null || !sampleval.isEmpty()) {
				query = remove;
				query += load;
				query += sampleval;
				query += store;
			}
		}
		return query;
	}
	
	@Override
	public FieldList getInFeatures() throws RemoteException {
		return getDFEInput().get(DemoAction.key_input).get(0).getFields();
	}

	@Override
	public FieldList getNewFeatures() throws RemoteException {
		return getInFeatures();
	}


	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if (in != null) {
			if (interaction.getId().equals(sample.getId())) {
				sample.update();
			}
		}
	}
	

}
