/**
 * 
 */
package com.datastax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author jake.awe
 *
 */

@Data
@AllArgsConstructor
public class AssetRequest {
	
    private String name;
    @JsonProperty("expires_in")
    private long expiresIn;
	
    
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

    
    
}
