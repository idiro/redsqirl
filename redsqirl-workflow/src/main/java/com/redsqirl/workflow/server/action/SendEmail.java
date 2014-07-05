package com.redsqirl.workflow.server.action;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.datatype.HiveTypePartition;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.EmailAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SendEmail extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5931579127027856445L;

	public static final String key_output = "", 
			/**Input Key*/
			key_input = "in",
			/**Destinatary Key*/
			key_destinatary="destinatary",
			/**cc Key*/
			key_cc="cc",
			/**Subject Key*/
			key_subject="subject",
			/**Message Key*/
			key_message="message";

	/**
	 * entries
	 */
	protected static Map<String, DFELinkProperty> input;
	
	/**
	 * Choose the email, subject and message.
	 */
	private Page page1;
	
	/**Input Interaction for email address*/
	protected InputInteraction destinataryInt;
	
	/**Input Interaction for cc address*/
	protected InputInteraction ccInt;
	
	/**Input Interaction for subject*/
	protected InputInteraction subjectInt;
	
	/**Input Interaction for message*/
	protected EditorInteraction messageInt;
	

	public SendEmail() throws RemoteException {
		super(new EmailAction());
		init();
		
		page1 = addPage(LanguageManagerWF.getText("email.page1.title"),
				LanguageManagerWF.getText("email.page1.legend"), 1);
		
		String email = WorkflowPrefManager.getUserProperty(WorkflowPrefManager.user_email,"");
		
		destinataryInt = new InputInteraction(
				key_destinatary,
				LanguageManagerWF.getText("email.destinatary_interaction.title"),
				LanguageManagerWF.getText("email.destinatary_interaction.legend"), 
				0, 0);
		destinataryInt.setValue(email);
		destinataryInt.setRegex("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+" +
				"(,[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)*$");
		
		ccInt = new InputInteraction(
				key_cc,
				LanguageManagerWF.getText("email.cc_interaction.title"),
				LanguageManagerWF.getText("email.cc_interaction.legend"), 
				1, 0);
		ccInt.setValue("");
		ccInt.setRegex("^$|^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+" +
				"(,[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)*$");
		
		subjectInt = new InputInteraction(
				key_subject,
				LanguageManagerWF.getText("email.subject_interaction.title"),
				LanguageManagerWF.getText("email.subject_interaction.legend"), 
				2, 0);
		subjectInt.setValue("");
		subjectInt.setRegex(".*");
		
		messageInt = new EditorInteraction(
				key_message,
				LanguageManagerWF.getText("email.message_interaction.title"),
				LanguageManagerWF.getText("email.message_interaction.legend"), 
				3, 0);
		messageInt.setValue("");
//		messageInt.setRegex(".*");
		
		page1.addInteraction(destinataryInt);
		page1.addInteraction(ccInt);
		page1.addInteraction(subjectInt);
		page1.addInteraction(messageInt);

	}
	
	/**
	 * Initialize the action
	 * 
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(new LinkedList<Class<? extends DFEOutput>>(), 
					0, Integer.MAX_VALUE));
			input = in;
		}

	}
	
	

	@Override
	public String getName() throws RemoteException {
		return "send_email";
	}

	/**
	 * Get the map of inputs
	 * @return input
	 * @throws RemoteException
	 */
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return true;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

	}
	
	
	@Override
	public String updateOut() throws RemoteException {
		String error = null;
		
		EmailAction emailAction = (EmailAction) getOozieAction();
		emailAction.setBodyText(messageInt.getValue());
		emailAction.setDestinatary(destinataryInt.getValue());
		emailAction.setSubjectText(subjectInt.getValue());
		emailAction.setCc(ccInt.getValue());
		
		return error;
	}


	// Override default static methods
	/**
	 * Get path to help
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getHelp() throws RemoteException {
		String absolutePath = "";
		String helpFile = "/help/" + getName().toLowerCase() + ".html";
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(helpFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source help absPath : " + absolutePath);
		logger.debug("Source help Path : " + path);
		logger.debug("Source help ans : " + ans);
		// absolutePath
		return absolutePath;
	}

	/**
	 * Get the path to the Image
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String absolutePath = "";
		String imageFile = "/image/" + getName().toLowerCase() + ".gif";
		String path = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source image abs Path : " + absolutePath);
		logger.debug("Source image Path : " + path);
		logger.debug("Source image ans : " + ans);

		return absolutePath;
	}

	public InputInteraction getDestinataryInt() {
		return destinataryInt;
	}

	public InputInteraction getSubjectInt() {
		return subjectInt;
	}

	public EditorInteraction getMessageInt() {
		return messageInt;
	}
	
	public InputInteraction getCcInt() {
		return ccInt;
	}
}
