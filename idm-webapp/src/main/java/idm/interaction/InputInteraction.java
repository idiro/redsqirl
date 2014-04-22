package idm.interaction;

import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;

/**
 * 
 * @author etienne
 *
 */
public class InputInteraction extends CanvasModalInteraction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8667811028700533217L;

	/**
	 * The value in the input interaction 
	 */
	private String inputValue;
	
	/**
	 * The regex value
	 */
	private String inputRegex;
	
	public InputInteraction(DFEInteraction dfeInter) throws RemoteException {
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
