package idm.interaction;

import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;

public class InputInteraction extends CanvasModalInteraction {

	private String inputValue;
	private String inputRegex;
	
	public InputInteraction(DFEInteraction dfeInter) {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		inputValue = inter.getTree()
				.getFirstChild("input").getFirstChild("output")
				.getFirstChild().getHead();
		inputRegex = inter.getTree()
				.getFirstChild("input").getFirstChild("regex")
				.getFirstChild().getHead();
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("input")
		.getFirstChild("output").removeAllChildren();
		inter.getTree().getFirstChild("input")
		.getFirstChild("output")
		.add(inputValue);

	}

	public void setUnchanged(){
		try {
			unchanged = inputValue.equals(
					inter.getTree().getFirstChild("input")
					.getFirstChild("output")
					.getFirstChild().getHead());
		} catch (Exception e) {
			unchanged = false;
		}
	}

	/**
	 * @return the inputValue
	 */
	public String getInputValue() {
		return inputValue;
	}

	/**
	 * @param inputValue the inputValue to set
	 */
	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}

	/**
	 * @return the inputRegex
	 */
	public String getInputRegex() {
		return inputRegex;
	}

	/**
	 * @param inputRegex the inputRegex to set
	 */
	public void setInputRegex(String inputRegex) {
		this.inputRegex = inputRegex;
	}

}
