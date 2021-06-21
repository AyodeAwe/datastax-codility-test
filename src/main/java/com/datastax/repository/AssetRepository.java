package com.datastax.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datastax.model.Asset;

/**
 * @author jake.awe
 *
 */

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {

	@Query("select b from Asset b where b.assetName =?1")
	Asset findByBucketName(String assetName);
	
	@Query("select a from Asset a where a.url =?1")
	Asset findByUrl(String url);

}
