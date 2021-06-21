/**
 * 
 */
package com.datastax.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * @author jake.awe
 *
 */

@Entity
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Asset {

	@Id
	private String id;
	@Column(columnDefinition="TEXT")
	private String url;
	private String assetName;
	private Date expiresAt;
	private String timeout;
	private String message;
	private boolean statusFlag;
	
	

	public String getId(String id) {
		return id;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}


	public Date getExpiresAt() {
		return expiresAt;
	}



	public String getTimeout() {
		return timeout;
	}



	public String getId() {
		return id;
	}



	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}



	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}



	public String getAssetName() {
		return assetName;
	}



	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}



	public boolean isStatusFlag() {
		return statusFlag;
	}



	public void setStatusFlag(boolean statusFlag) {
		this.statusFlag = statusFlag;
	}



	public Asset(String id, String url, String assetName, Date expiresAt, boolean statusFlag) {
		this.id = UUID.randomUUID().toString();
		this.url = url;
		this.assetName = assetName;
		this.expiresAt = expiresAt;
		this.statusFlag = statusFlag;
	}



	public Asset() {
		super();
	}



	public Asset(String message, String url) {

		this.message = message;
		this.url = url;
	}



	public Asset(String message, boolean statusFlag) {
		this.message = message;
		this.statusFlag = statusFlag;
		
	}



	public Asset(String message) {
		this.message = message;
	}



	/**
	 * @param string
	 * @param url2
	 * @param timeout2
	 */
	public Asset(String message, String url, String timeout) {
		this.message = message;
		this.url = url;
		this.timeout = timeout;
	}



	



	



	
	 
}
