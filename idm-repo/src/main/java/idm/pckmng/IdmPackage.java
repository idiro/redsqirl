package idm.pckmng;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class IdmPackage {

	@XmlElement 
	private String id,
				    name,
				   version,
				   license,
				   short_description,
				   description,
				   price,
				   url,
				   release;
	
	@XmlElement 
	private Date package_date;
	
	/**
	 * @return the id
	 */
	public final String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public final void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the version
	 */
	public final String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public final void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the short_description
	 */
	public final String getShort_description() {
		return short_description;
	}
	/**
	 * @param short_description the short_description to set
	 */
	public final void setShort_description(String short_description) {
		this.short_description = short_description;
	}
	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the price
	 */
	public final String getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public final void setPrice(String price) {
		this.price = price;
	}
	/**
	 * @return the url
	 */
	public final String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public final void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the package_date
	 */
	public final Date getPackage_date() {
		return package_date;
	}
	/**
	 * @param package_date the package_date to set
	 */
	public final void setPackage_date(Date package_date) {
		this.package_date = package_date;
	}
	/**
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}
	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
	}
	/**
	 * @return the release
	 */
	public String getRelease() {
		return release;
	}
	/**
	 * @param release the release to set
	 */
	public void setRelease(String release) {
		this.release = release;
	}
	
	
}
