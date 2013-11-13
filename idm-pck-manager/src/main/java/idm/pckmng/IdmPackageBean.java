package idm.pckmng;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class IdmPackageBean {

private DataSource ds;
	
	private static Logger logger = Logger.getLogger(IdmPackageBean.class);
	
	private String packageName;
	private IdmPackage idmPck;
	
	//if resource inject is not support, you still can get it manually.
	public IdmPackageBean(){
		logger.info("Call constructor");
		
		try {
			logger.info("C1");
			Context ctx = new InitialContext();
			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/idm_pck_mng");
		} catch (NamingException e) {
			logger.info("C2");
			e.printStackTrace();
		}
		logger.info("C3");
	}
	
	public List<String> getVersions() throws SQLException{
		logger.info("get list "+packageName);
		if(ds==null)
			throw new SQLException("Can't get data source");
		
		//get database connection
		Connection con = ds.getConnection();
		if(con==null)
			throw new SQLException("Can't get database connection");
		
		PreparedStatement ps = con.prepareStatement(
				"select version from idm_packages where name = '"+packageName+"' order by package_date DESC");
		
		
		//get customer data from database
		ResultSet result =  ps.executeQuery();
		
		List<String> list = new ArrayList<String>();
		
		while(result.next()){
			list.add(result.getString("version"));
		}
		con.close();
		logger.info("return: "+list);
		return list;
	}
	
	public void setVersion() throws SQLException{
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String version = params.get("pckVersion");
		
		logger.info("set Package version: "+version);
		
		
		if(version != null && ! version.matches("[0-9a-zA-Z_\\-.]+")){
			return;
		}
		
		if(version != null){
			setPackageName(version);
		}
		
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName the packageName to set
	 * @throws SQLException 
	 */
	public void setPackageName() throws SQLException {
		setPackageName(null);
	}
	
	public void setPackageName(String version) throws SQLException {
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		packageName = params.get("pckName") == null ? packageName : params.get("pckName");
		
		logger.info("set Package name: "+packageName);
		
		if(packageName != null && ! packageName.matches("[0-9a-zA-Z_\\-]+")){
			this.packageName = null;
		}
		
		//get database connection
		Connection con = ds.getConnection();
		if(con==null)
			throw new SQLException("Can't get database connection");
		
		PreparedStatement ps = null;
		
		if(version == null){
			ps = con.prepareStatement(
				"select * from idm_packages where name = '"+packageName+"'  order by package_date DESC limit 1");
		}else{
			ps = con.prepareStatement(
					"select * from idm_packages where name = '"+packageName+"' AND version = '"+version+"' order by package_date DESC limit 1");
		}
		
		//get customer data from database
		ResultSet result =  ps.executeQuery();
		if(result.next()){
			idmPck= new IdmPackage();
			idmPck.setName(result.getString("name"));
			idmPck.setVersion(result.getString("version"));
			idmPck.setLicense(result.getString("license"));
			idmPck.setPrice(result.getString("price"));
			idmPck.setShort_description(result.getString("short_description"));
			idmPck.setDescription(result.getString("description"));
			idmPck.setPackage_date(result.getDate("package_date"));
			idmPck.setUrl(result.getString("url"));
			idmPck.setRelease(result.getString("release_notes"));
		}
		
		con.close();
	}

	/**
	 * @return the idmPck
	 */
	public IdmPackage getIdmPck() {
		return idmPck;
	}

	/**
	 * @param idmPck the idmPck to set
	 */
	public void setIdmPck(IdmPackage idmPck) {
		this.idmPck = idmPck;
	}
}
