/**
 * 
 */
package com.datastax.service;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.datastax.dto.AssetResponse;
import com.datastax.dto.SignedUrlRequest;
import com.datastax.model.Asset;

/**
 * @author jake.awe
 *
 */
public interface AssetUploaderService {


	AssetResponse createAsset(String assetName);
	
	Object getPresignedUrl(String assetName) throws Exception;

	String deleteAsset(String assetName);
	
	Asset getAsset(String id, String assetName, String timeout) throws Exception;

	AssetResponse getAssetStatus(String assetName, boolean statusFlag, String id);

	Optional<Asset> getAssetPresignedUrl(SignedUrlRequest signedUrlRequest) throws Exception;

	Optional<Asset> getAsset(String id); 

	Asset getBucket(SignedUrlRequest signedUrlRequest) throws Exception;
	
	String uploadFileBucket(MultipartFile file, String bucketName, String presSignedUrl) throws Exception;

	String downloadAsset(String bucketName) throws Exception;

	Object findByName(String string);


}
