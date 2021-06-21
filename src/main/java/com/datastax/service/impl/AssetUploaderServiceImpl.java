/**
 * 
 */
package com.datastax.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.datastax.dto.AssetResponse;
import com.datastax.dto.SignedUrlRequest;
import com.datastax.model.Asset;
import com.datastax.repository.AssetRepository;
import com.datastax.service.AssetUploaderService;
import lombok.extern.slf4j.Slf4j;
import java.net.URL;

/**
 * @author jake.awe
 *
 */

@Slf4j
@Service
public class AssetUploaderServiceImpl implements AssetUploaderService {

	private static final Logger log = LoggerFactory.getLogger(AssetUploaderServiceImpl.class);

	@Autowired
	private AmazonS3 amazonS3;

	@Value("${s3.bucket.name}")
	private String s3BucketName;

	String objectKey = "aws-test";

	private final AssetRepository assetRepository;

	public AssetUploaderServiceImpl(AssetRepository assetRepository) {
		this.assetRepository = assetRepository;
	}

	@Override
	public AssetResponse createAsset(String assetName) {
		String message = "";
		boolean uploadSatatus = false;
		if (!amazonS3.doesBucketExistV2(assetName)) {
			amazonS3.createBucket(new CreateBucketRequest(assetName));

			AssetResponse resp = new AssetResponse(assetName, message, uploadSatatus);
			resp.getAssetName();
			resp.isStatusFlag();
			resp.setMessage("Congratulation your bucket has been created!!!");

			return resp;
		}
		return new AssetResponse("Bucket already exist");
	}

	public Asset getPresignedUrl(String assetName) throws Exception {

		String uniqueId = UUID.randomUUID().toString();
		String fileName = (uniqueId.toString() + assetName);
		return generateUrl(assetName, fileName, HttpMethod.PUT);

	}

	@Override
	public String deleteAsset(String assetName) {

		if (!amazonS3.doesBucketExistV2(assetName)) {
			log.error("No Bucket Found");
			return "No Bucket Found";
		}

		try {

			ObjectListing objectListing = amazonS3.listObjects(assetName);
			while (true) {
				Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
				while (objIter.hasNext()) {
					amazonS3.deleteObject(assetName, objIter.next().getKey());
				}

				if (objectListing.isTruncated()) {
					objectListing = amazonS3.listNextBatchOfObjects(objectListing);
				} else {
					break;
				}
			}

			VersionListing versionList = amazonS3.listVersions(new ListVersionsRequest().withBucketName(assetName));
			while (true) {
				Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
				while (versionIter.hasNext()) {
					S3VersionSummary vs = versionIter.next();
					amazonS3.deleteVersion(assetName, vs.getKey(), vs.getVersionId());
				}

				if (versionList.isTruncated()) {
					versionList = amazonS3.listNextBatchOfVersions(versionList);
				} else {
					break;
				}
			}

			amazonS3.deleteBucket(assetName);
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
		return "Bucket deleted succefully";
	}

	@Override
	public AssetResponse getAssetStatus(String assetName, boolean statusFlag, String id) {

		String statusCompleted = "Your upload is completed!!! Upload status has been set to true";
		String idError = "Pass in a valid ID";
		String s3Error = "Your request cannot be completed at this time, bucket has not been created";

		if (id == null) {

			AssetResponse resp = new AssetResponse(idError);

			return resp;
		}

		if (!amazonS3.doesBucketExistV2(assetName)) {

			log.error("No Bucket Found for this request");

			AssetResponse resp = new AssetResponse(s3Error);
			return resp;

		}

		Optional<Asset> assetData = assetRepository.findById(id);

		if (assetData.isPresent() && assetData.get().isStatusFlag()) {

			Asset asset = new Asset();
			asset.setStatusFlag(true);
			if (statusFlag == false)
				assetData.get().setStatusFlag(false);

			AssetResponse resp = new AssetResponse(statusCompleted);
			asset.setId(resp.getUniqueId());

			return resp;

		}
		return new AssetResponse(statusCompleted);

	}

	private Asset generateUrl(String assetName, String uniqueId, HttpMethod httpMethod) throws Exception {
		if (!amazonS3.doesBucketExistV2(assetName)) {

			log.error("No Bucket Found");
			return new Asset("Bucket does not exist in aws s3, create a bucket before generating presigned URL",
					assetName);
		}
		Asset asset = new Asset();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 60); // Generated URL will be valid for 60 minutes
		Date date = calendar.getTime();
		log.info("Date format >>>>>>>>>>>{}", date);
		String url = amazonS3.generatePresignedUrl(assetName, uniqueId, calendar.getTime(), httpMethod).toString();
		log.info("Generated Url>>>>>>>>>>>> {}", url);
		Date dateVal = calendar.getTime();
		log.info("Date format returned >>>>>>>>>>>{}", dateVal);
		asset.setStatusFlag(true);
		AssetResponse resp = new AssetResponse(uniqueId, url, dateVal);
		asset.setId(resp.getUniqueId());
		asset.setAssetName(assetName);
		asset.setUrl(resp.getUrl());
		asset.setTimeout("");
		asset.setExpiresAt(dateVal);
		assetRepository.save(asset);
		log.info("Generated Url>>>>>>>>>>>> {}", asset.toString());

		return asset;
	}

	@Override
	public Asset getAsset(String id, String assetName, String timeout) throws Exception {
		log.info("Bucket name passed>>>>>>>>>>>>>" + assetName);
		log.info("Timeout passed>>>>>>>>>>>>>>>>>" + timeout);

		if (!amazonS3.doesBucketExistV2(assetName)) {
			log.error("No Bucket Found");

			return new Asset("Oops!, Your request cannot be completed with buckect name: " + assetName
					+ " has not been created");
		}

		Asset bucketDetails = generateUrl(assetName, timeout, HttpMethod.PUT);

		log.info("asset pulled from generated >>>>>>>>>>>{}", bucketDetails);

		if (bucketDetails == null) {
			throw new Exception("No record was found with bucket name provided" + assetName);

		}
		Optional<Asset> asset = assetRepository.findById(id);
		if (asset != null && asset.isPresent() && bucketDetails != null && asset.get().isStatusFlag() == true) {

			bucketDetails.setTimeout(timeout);
			bucketDetails.getUrl();
			log.info("Generated Url >>>>>>>>>>>> {}", bucketDetails);

			return new Asset("Asset details succesfully retrieved!!!,  Upload status is true", bucketDetails.getUrl(),
					bucketDetails.getTimeout());

		}

		if (asset != null && asset.isPresent() && bucketDetails != null && asset.get().isStatusFlag() == false) {
			asset.get().isStatusFlag();

			Asset assetFalseResp = generateUrlIfFalse(assetName, timeout, HttpMethod.PUT);

			log.info("asset pulled from generated >>>>>>>>>>>", assetFalseResp);

			throw new Exception("Oops!, Your request cannot be completed. upload status is false");

		}
		return new Asset("Oops!, Your request cannot be completed. upload status is false");

	}

	private Asset generateUrlIfFalse(String assetName, String uniqueId, HttpMethod httpMethod) {
		Asset asset = new Asset();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 60); // Generated URL will be valid for 60 minutes
		Date date = calendar.getTime();
		log.info("Date format >>>>>>>>>>>{}", date);
		String url = amazonS3.generatePresignedUrl(assetName, uniqueId, calendar.getTime(), httpMethod).toString();
		log.info("Generated Url>>>>>>>>>>>> {}", url);
		Date dateVal = calendar.getTime();
		log.info("Date format returned >>>>>>>>>>>{}", dateVal);
		AssetResponse resp = new AssetResponse(uniqueId, url, dateVal);
		asset.setId(resp.getUniqueId());
		asset.setAssetName(assetName);
		asset.setUrl(resp.getUrl());
		asset.setStatusFlag(true);
		asset.setTimeout("false");
		asset.setExpiresAt(dateVal);
		assetRepository.save(asset);
		log.info("Generated Url>>>>>>>>>>>> {}", asset.toString());

		return asset;
	}

	@Override
	public Optional<Asset> getAssetPresignedUrl(SignedUrlRequest signedUrlRequest) throws Exception {
		Optional<Asset> assetData = assetRepository.findById(signedUrlRequest.getId());
		log.info(">>>>>>>>>>>>>> signedUrl" + signedUrlRequest.getSignedUrl());

		if (!amazonS3.doesBucketExistV2(signedUrlRequest.getAssetName())) {
			log.error("No Bucket Found");
			throw new Exception("No record was found with bucket name provided" + signedUrlRequest.getAssetName());
		}

		Asset asset = generateUrl(signedUrlRequest.getAssetName(), signedUrlRequest.getId(), HttpMethod.PUT);

		log.info("Generated Url>>>>>>>>>>>> {}", signedUrlRequest.getSignedUrl());

		log.info(">>>>>>>>>>>>>> assetResponse" + asset.getUrl());
		if (asset != null && asset.getUrl().contentEquals(assetData.get().getUrl())) {

			AssetResponse resp = new AssetResponse(signedUrlRequest.getId(), asset.getUrl(),
					signedUrlRequest.getAssetName());
			asset.setId(resp.getUniqueId());
			asset.setAssetName(signedUrlRequest.getAssetName());
			asset.setUrl(resp.getUrl());
			asset.setStatusFlag(true);
			asset.setTimeout("false");

			return Optional.of(asset);

		}
		return null;

	}

	@Override
	public Optional<Asset> getAsset(String id) {

		Optional<Asset> assetData = assetRepository.findById(id);

		if (assetData != null) {
			return assetData;
		}
		return null;
	}

	@Override
	public Asset getBucket(SignedUrlRequest signedUrlRequest) {

		log.info("URL passed>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + signedUrlRequest.getSignedUrl());
		Asset assetData = assetRepository.findByUrl(signedUrlRequest.getSignedUrl());
		log.info("URL passed>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + assetData);

		if (assetData != null) {
			return assetData;
		}
		return null;
	}

	@Override
	public String uploadFileBucket(MultipartFile multipartFile, String bucketName, String presSignedUrl)
			throws Exception {

		log.info(":::::::::::::::::::::::: The presSignedUrl passed URL", presSignedUrl);

		try {

			URL url = new URL(presSignedUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write("This text uploaded as an object via presigned URL.");
			out.close();

			connection.getResponseCode();
			System.out.println("HTTP response code: " + connection.getResponseCode());
			S3Object object = amazonS3.getObject(bucketName, objectKey);
			System.out.println("Object " + object.getKey() + " created in bucket " + object.getBucketName());
			return bucketName;

		} catch (SdkClientException e) {
		}
		return presSignedUrl;

	}

	@Override
	public String downloadAsset(String bucketName) throws Exception {

		S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;

		try {

			System.out.println("Downloading an object");
			fullObject = amazonS3.getObject(new GetObjectRequest(bucketName, objectKey));
			System.out.println("Content-Type: " + fullObject.getObjectMetadata().getContentType());
			System.out.println("Content: ");
			displayTextInputStream(fullObject.getObjectContent());

			GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucketName, objectKey).withRange(0, 9);
			objectPortion = amazonS3.getObject(rangeObjectRequest);
			System.out.println("Printing bytes retrieved.");
			displayTextInputStream(objectPortion.getObjectContent());
			ResponseHeaderOverrides headerOverrides = new ResponseHeaderOverrides().withCacheControl("No-cache")
					.withContentDisposition("attachment; filename=example.txt");
			GetObjectRequest getObjectRequestHeaderOverride = new GetObjectRequest(bucketName, objectKey)
					.withResponseHeaders(headerOverrides);
			headerOverrideObject = amazonS3.getObject(getObjectRequestHeaderOverride);
			displayTextInputStream(headerOverrideObject.getObjectContent());
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		} finally {
			if (fullObject != null) {
				fullObject.close();
			}
			if (objectPortion != null) {
				objectPortion.close();
			}
			if (headerOverrideObject != null) {
				headerOverrideObject.close();
			}
		}
		return "Your dowload is succefull";
	}

	private static void displayTextInputStream(InputStream input) throws IOException {
		// Read the text input stream one line at a time and display each line.
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		System.out.println();
	}

	private String generatedownloadUrl(String fileName, HttpMethod httpMethod) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 1); // Generated URL will be valid for 24 hours
		return amazonS3.generatePresignedUrl(s3BucketName, fileName, calendar.getTime(), httpMethod).toString();
	}

	@Override
	public String findByName(String fileName) {
		if (!amazonS3.doesObjectExist(s3BucketName, fileName))
			return "File does not exist";
		log.info("Generating signed URL for file name {}", fileName);
		return generatedownloadUrl(fileName, HttpMethod.GET);
	}

}
