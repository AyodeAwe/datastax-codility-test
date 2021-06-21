/**
 * 
 */
package com.datastax.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * @author jake.awe
 *
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetResponse {

	private String uniqueId;
	private String assetName;
	private String url;
	private String timeout;
	private boolean statusFlag;
	private Date expiresAt;
	private String message;

	public AssetResponse(String uniqueId, String url, Date expiresAt) {

		this.uniqueId = uniqueId;
		this.url = url;
		this.expiresAt = expiresAt;

	}

	public AssetResponse(String assetName, String url, boolean statusFlag) {

		this.assetName = assetName;
		this.url = url;
		this.statusFlag = statusFlag;
	}

	public AssetResponse(String uniqueId, Date expiresAt) {
		this.uniqueId = uniqueId;
		this.expiresAt = expiresAt;
	}

	public AssetResponse(String assetName, String url, String timeout) {

		this.uniqueId = assetName;
		this.url = url;
		this.timeout = timeout;
	}

	public AssetResponse() {

	}

	public boolean isStatusFlag() {
		return statusFlag;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setStatusFlag(boolean statusFlag) {
		this.statusFlag = statusFlag;
	}

	public AssetResponse(AssetResponse assetResp) {

	}

	public AssetResponse(String message) {
		this.message = message;

	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
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

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

}
