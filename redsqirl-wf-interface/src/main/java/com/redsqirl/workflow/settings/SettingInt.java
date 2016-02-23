package com.redsqirl.workflow.settings;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SettingInt extends Remote{
	
	public enum Scope{
		SYSTEM,
		USER,
		ANY
	}
	
	public enum Type{
		BOOLEAN,
		INT,
		FLOAT,
		STRING
	}

	void deleteSysProperty() throws RemoteException;
	
	void deleteUserProperty() throws RemoteException;
	
	void deleteProperty(boolean user, boolean sys) throws RemoteException;

	boolean valid() throws RemoteException;
	
	String getSysPropetyValue() throws RemoteException;
	
	String getUserPropetyValue() throws RemoteException;
	
	String getUserPropetyValue(String user) throws RemoteException;
	
	void setUserValue(String userValue) throws RemoteException;

	String getUserValue() throws RemoteException;

	String getSysValue() throws RemoteException;

	void setSysValue(String sysValue) throws RemoteException;

	String getValue() throws RemoteException;
	
	void setValue(String value) throws RemoteException;

	Scope getScope() throws RemoteException;
	
	void setScope(Scope scope) throws RemoteException;
	
	String getDefaultValue() throws RemoteException;

	void setDefaultValue(String defaultValue) throws RemoteException;

	Type getType() throws RemoteException;
	
	void setType(Type type) throws RemoteException;

	String getDescription() throws RemoteException;

	void setDescription(String description) throws RemoteException;

	String getLabel() throws RemoteException;

	void setLabel(String label) throws RemoteException;

	String getPropertyName() throws RemoteException;

	void setPropertyName(String propertyName) throws RemoteException;

	boolean isExistUserProperty() throws RemoteException;

	void setExistUserProperty(boolean existUserProperty) throws RemoteException;

	boolean isExistSysProperty() throws RemoteException;

	void setExistSysProperty(boolean existSysProperty) throws RemoteException;
}
