/**
 * 
 */
package com.datastax.controller;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.datastax.dto.AssetResponse;
import com.datastax.dto.SignedUrlRequest;
import com.datastax.model.Asset;
import com.datastax.service.AssetUploaderService;

/**
 * @author jake.awe
 *
 */

@RestController
@RequestMapping(value = "/s3")
public class AssetUploaderController {
	
	private static final String FILE_NAME = "fileName";

	@Autowired
	private AssetUploaderService assetUploaderService;

	@PostMapping(value = "/assetName/create/{assetName}")
	public AssetResponse createNewAsset(@PathVariable String assetName) {
		return assetUploaderService.createAsset(assetName);
	}

	@PostMapping
	public ResponseEntity<Object> generatePresignedUrl(@RequestParam("assetName") String assetName) throws Exception {
		return new ResponseEntity<>(assetUploaderService.getPresignedUrl(assetName), HttpStatus.OK);
	}


	@GetMapping(value = "/files/{assetName}")
	public Asset getAsset(@RequestParam("id") String id, @RequestParam("timeout") String timeout, @PathVariable String assetName) throws Exception {
		return assetUploaderService.getAsset(id, assetName,  timeout);
	}
		
	@PutMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file, @RequestParam("bucketName") String bucketName, @RequestParam("presSignedUrl") String presSignedUrl) throws Exception {
        return this.assetUploaderService.uploadFileBucket(file, bucketName, presSignedUrl);
    }
	
	@PutMapping(value = "check/status/{assetName}")
	public AssetResponse getAssetStatus(@PathVariable String assetName,  boolean statusFlag, String id) {
		
		return assetUploaderService.getAssetStatus(assetName, statusFlag, id);
	}

	@PostMapping(value = "/view/asset")
	public Optional<Asset> getAssetPresignedUrl(@RequestBody SignedUrlRequest signedUrlRequest) throws Exception {

		return assetUploaderService.getAssetPresignedUrl(signedUrlRequest);

	}
	
	@GetMapping(value = "/download/asset")
	public String downloadAsset(String bucketName) throws Exception {

		return assetUploaderService.downloadAsset(bucketName);

	}
	

	@GetMapping(value = "/view/asset/{id}")
	public Optional<Asset> viewAsset(@PathVariable  String id) throws Exception {

		return assetUploaderService.getAsset(id);

	}

	@DeleteMapping(value = "/delete/{assetName}")
	public String deleteAsset(@PathVariable String assetName) {

		return assetUploaderService.deleteAsset(assetName);
	}
	 
	@GetMapping("/dowload")
	    public ResponseEntity<Object> findByName(HttpServletRequest request, Map<String, String> params) {
	        final String path = request.getServletPath();
	        if (params.containsKey(FILE_NAME))
	            return new ResponseEntity<>(assetUploaderService.findByName(params.get(FILE_NAME)), HttpStatus.OK);
			return null;
	    }

}
