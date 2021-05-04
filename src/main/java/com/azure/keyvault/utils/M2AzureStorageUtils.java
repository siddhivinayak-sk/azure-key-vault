package com.azure.keyvault.utils;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.models.BlobDeleteResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.util.FlowableUtil;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * AzureStorageUtils class is responsible handle azure blob operation. 
 * @author Sandeep Kumar
 *
 */
public class M2AzureStorageUtils {
	
	private static final Logger LOGGER = LogManager.getLogger(M2AzureStorageUtils.class);
	
	private M2AzureStorageUtils() {
	    throw new IllegalStateException("Utility class");
	}
	
	private static BlockBlobURL getBlobURL(String credentials, String containerName, String filename) throws Exception {
		LOGGER.info("getBlobURL: Started");
		ContainerURL containerURL = null;
		BlockBlobURL blobURL = null;
		String accountName = credentials.split("<@#@>")[0];
		String accountKey = credentials.split("<@#@>")[1];
		try {
			SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);
			HttpPipeline pipeline = StorageURL.createPipeline(credential, new PipelineOptions());
			URL url = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName));
			LOGGER.info("URL >> " + url);
			ServiceURL serviceURL = new ServiceURL(url, pipeline);
			LOGGER.info("ServiceURL >> " + serviceURL);
			containerURL = serviceURL.createContainerURL(containerName);
			LOGGER.info("ContainerURL >> " + containerURL);
			blobURL = containerURL.createBlockBlobURL(filename);
			LOGGER.info("BlobURL >> " + containerURL);
		} catch (Exception e) {
			LOGGER.error("getBlobURL", e);
			throw e;
		}
		LOGGER.info("getBlobURL: Started");
		return blobURL;
	}
	
	public static boolean uploadFile(String connectionString, String containerName, String filePath, byte[] data) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: getBlobConatiner method start");
		BlockBlobURL blobURL = getBlobURL(connectionString, containerName, filePath);
		Single<BlockBlobUploadResponse> blobResponse = blobURL.upload(Flowable.just(ByteBuffer.wrap(data)),
				data.length, null, null, null, null);
		boolean result = blobResponse.blockingGet().statusCode() == 201; 
		LOGGER.info("Inside AzureStorageUtils:: getBlobConatiner method end");
		return result;
	}

	public static byte[] downloadFile(String connectionString, String containerName, String filePath) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: downloadFile method start");
		BlockBlobURL blobURL = getBlobURL(connectionString, containerName, filePath);
		Single<DownloadResponse> blobResponse = blobURL.download(null, null, false, null);
		LOGGER.info("Inside AzureStorageUtils:: downloadFile method downloaded");
		Flowable<ByteBuffer> fByteBuffer = blobResponse.blockingGet().body(null);
		Single<ByteBuffer> bf = FlowableUtil.collectBytesInBuffer(fByteBuffer);
		byte[] retVal = bf.blockingGet().array();
		LOGGER.info("Inside AzureStorageUtils:: downloadFile method end");
		return retVal;
	}

	public static boolean delete(String connectionString, String containerName, String filePath) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: delete method start");
		BlockBlobURL blobURL = getBlobURL(connectionString, containerName, filePath);
		Single<BlobDeleteResponse> blobResponse = blobURL.delete(null, null, null);
		boolean retVal = blobResponse.blockingGet().statusCode() == 201;
		LOGGER.info("Inside AzureStorageUtils:: delete method end");
		return retVal;
	}
	
	public static String sasURL(String connectionString, String containerName, String filePath) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: sasURL method start");
		BlockBlobURL blobURL = getBlobURL(connectionString, containerName, filePath);
		return blobURL.toURL().toString();
	}
}