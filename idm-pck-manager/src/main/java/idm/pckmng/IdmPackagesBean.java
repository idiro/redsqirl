package idm.pckmng;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

@Path("allpackages")
public class IdmPackagesBean {

	//@Context 
	//ServletContext context;
	
	//resource injection
    //@Resource(name="jdbc/idm_pck_mng")
	private DataSource ds;
	
	private static Logger logger = Logger.getLogger(IdmPackagesBean.class);
	
	//if resource inject is not support, you still can get it manually.
	public IdmPackagesBean(){
		System.out.println("Call constructor");
		try {
			System.out.println("C1");
			Context ctx = new InitialContext();
			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/idm_pck_mng");
			//ds = (DataSource) context.getAttribute("java:comp/env/jdbc/idm_pck_mng");
		} catch (Exception e) {
			System.out.println("C2");
			e.printStackTrace();
		}
		System.out.println("C3");
	}
	
	//connect to DB and get customer list
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public List<IdmPackage> getPackageList(@QueryParam("name") String packageName,
			@QueryParam("version") String version) throws SQLException, ClassNotFoundException{
		System.out.println("getPackageList");
		
		if(packageName != null && ! packageName.matches("[0-9a-zA-Z_\\-]+")){
			packageName = null;
		}
		
		if(version != null && ! version.matches("[0-9a-zA-Z_\\-.]+")){
			version = null;
		}
		
		if(ds==null)
			throw new SQLException("Can't get data source");
		
		//get database connection
		Connection con = ds.getConnection();
		
		if(con==null)
			throw new SQLException("Can't get database connection");
		
		PreparedStatement ps = con.prepareStatement(query(packageName,version));
		
		//get customer data from database
		ResultSet result =  ps.executeQuery();
		
		List<IdmPackage> list = new ArrayList<IdmPackage>();
		
		while(result.next()){
			IdmPackage pck = new IdmPackage();
			pck.setName(result.getString("name"));
			
			pck.setLicense(result.getString("license"));
			pck.setShort_description(result.getString("short_description"));
			if(packageName != null){
				pck.setVersion(result.getString("version"));
				pck.setPrice(result.getString("price"));
				pck.setDescription(result.getString("description"));
				pck.setPackage_date(result.getDate("package_date"));
				pck.setUrl(result.getString("url"));
			}
			
			list.add(pck);
		}
		con.close();
		return list;
	}
	
	
	private String query(String packageName, String version){
		String ans = null;
		if(packageName == null){
			ans = "select distinct name, license, short_description from idm_packages order by name";
		}else if(version == null){
			ans = "select * from idm_packages where name = '"+packageName+"'  order by package_date DESC";
		}else{
			ans = "select * from idm_packages where name = '"+packageName+"' AND version = '"+version+"' order by package_date DESC";
		}
		return ans;
	}
}
