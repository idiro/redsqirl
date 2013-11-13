package idm.pckmng;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;



public class IdmPackagesBean {

	//resource injection
	/*@Resource(name="jdbc/idm_pck_mng")*/
	private DataSource ds;
	
	private static Logger logger = Logger.getLogger(IdmPackagesBean.class);
	
	//if resource inject is not support, you still can get it manually.
	public IdmPackagesBean(){
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
	
	//connect to DB and get customer list
	public List<IdmPackage> getPackageList() throws SQLException, ClassNotFoundException{
		return showPackageList(null);
	}
	
	public List<IdmPackage> showPackageList(String packageName) throws SQLException, ClassNotFoundException{
		logger.info("getPackageList");
		
		if(packageName != null && ! packageName.matches("[0-9a-zA-Z_\\-]+")){
			packageName = null;
		}
		
		//Class.forName("com.mysql.jdbc.Driver");
		//con = DriverManager.getConnection("jdbc:mysql://localhost:3306/idm_pck_mng","idm_pck", "hadoop");
		if(ds==null)
			throw new SQLException("Can't get data source");
		
		//get database connection
		Connection con = ds.getConnection();
		if(con==null)
			throw new SQLException("Can't get database connection");
		
		PreparedStatement ps = null;
		if(packageName != null){
			ps = con.prepareStatement(
				"select distinct name, license, short_description from idm_packages where packageName = '"+packageName+"'");
		}else{
			ps = con.prepareStatement(
				"select distinct name, license, short_description from idm_packages order by name");
		}
		
		//get customer data from database
		ResultSet result =  ps.executeQuery();
		
		List<IdmPackage> list = new ArrayList<IdmPackage>();
		
		while(result.next()){
			IdmPackage pck = new IdmPackage();
			pck.setName(result.getString("name"));
			//pck.setVersion(result.getString("version"));
			pck.setLicense(result.getString("license"));
			//pck.setPrice(result.getString("price"));
			pck.setShort_description(result.getString("short_description"));
			//pck.setDescription(result.getString("description"));
			//pck.setPackage_date(result.getDate("package_date"));
			//pck.setUrl(result.getString("url"));
			
			list.add(pck);
		}
		con.close();
		return list;
	}
}
