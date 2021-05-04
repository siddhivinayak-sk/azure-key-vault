package com.azure.keyvault.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

/**
 * AzureStorageUtils class is responsible handle azure blob operation. 
 * @author Sandeep Kumar
 *
 */
public final class M1AzureStorageUtils {
	
	private static final Logger LOGGER = LogManager.getLogger(M1AzureStorageUtils.class);
	
	private M1AzureStorageUtils() {
	    throw new IllegalStateException("Utility class");
	  }
	
     /**
      * createCloudBlobClient method is responsible to create CloudBlobClient on 
      * the basis of  azure connectionString.
      * @param connectionString
      * @return CloudBlobClient
      * @throws InvalidKeyException
      * @throws URISyntaxException
      */
	private static CloudBlobClient createCloudBlobClient(String connectionString) throws InvalidKeyException, URISyntaxException {
		LOGGER.info("Inside AzureStorageUtils:: createCloudBlobClient method");
		CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
		return account.createCloudBlobClient();
	}
	
	/**
	 * getBlobConatiner method is responsible to  create  CloudBlobContainer.
	 * @param connectionString
	 * @param containerName
	 * @return CloudBlobContainer
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws com.microsoft.azure.storage.StorageException 
	 * @throws StorageException
	 */
	private static CloudBlobContainer getBlobConatiner(String connectionString, String containerName) throws InvalidKeyException, URISyntaxException, StorageException {
		LOGGER.info("Inside AzureStorageUtils:: getBlobConatiner method");
		CloudBlobClient cloudBlobClient = createCloudBlobClient(connectionString);
		CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(containerName);
		cloudBlobContainer.createIfNotExists();
		return cloudBlobContainer;
	}
	
	/**
	 * uploadFile method is responsible to upload file on azure location.
	 * @param connectionString
	 * @param containerName
	 * @param filePath
	 * @param data
	 * @return CloudBlockBlob
	 * @throws RuntimeException
	 */
	public static CloudBlockBlob uploadFile(String connectionString, String containerName, String filePath, byte[] data) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: getBlobConatiner method");
		try {
			CloudBlobContainer cloudBlobContainer = getBlobConatiner(connectionString, containerName);
			CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(filePath);			
			cloudBlockBlob.uploadFromByteArray(data, 0, data.length);
			return cloudBlockBlob;
		}
		catch(InvalidKeyException | URISyntaxException | StorageException | IOException ex) {
			LOGGER.error("Exception occurred while upload file on azure location! ", ex);
			throw new Exception(ex);
		}
	}

	/**
	 * downloadFile method is responsible to download file from azure location.
	 * @param connectionString
	 * @param containerName
	 * @param filePath
	 * @return byte[]
	 * @throws RuntimeException
	 */
	public static byte[] downloadFile(String connectionString, String containerName, String filePath) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: downloadFile method");
		try {
			CloudBlobContainer cloudBlobContainer = getBlobConatiner(connectionString, containerName);
			CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(filePath);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			cloudBlockBlob.download(outputStream);
			byte[] byteArray =  outputStream.toByteArray();
			outputStream.close();
			return byteArray;
		}
		catch(InvalidKeyException | URISyntaxException | StorageException | IOException ex) {
			LOGGER.error("Exception occurred while download file from azure location! ", ex);
			throw new Exception(ex);
		}
	}

	/**
	 * delete method is responsible to delete file from azure location. 
	 * @param connectionString
	 * @param containerName
	 * @param filePath
	 * @return boolean
	 * @throws RuntimeException
	 */
	public static boolean delete(String connectionString, String containerName, String filePath) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: delete method");
		try {
			CloudBlobContainer cloudBlobContainer = getBlobConatiner(connectionString, containerName);
			CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(filePath);
			return cloudBlockBlob.deleteIfExists();
		}
		catch(InvalidKeyException | URISyntaxException | StorageException ex) {
			LOGGER.error("Exception occurred while delete file from azure location! ", ex);
			throw new Exception(ex);
		}
	}
	
	/**
	 * sasURL method is responsible to generate SAS url.
	 * @param connectionString
	 * @param containerName
	 * @param filePath
	 * @return String
	 * @throws RuntimeException
	 */
	public static String sasURL(String connectionString, String containerName, String filePath) throws Exception {
		LOGGER.info("Inside AzureStorageUtils:: sasURL method");
		try {
			CloudBlobContainer cloudBlobContainer = getBlobConatiner(connectionString, containerName);
			CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(filePath);
			SharedAccessBlobPolicy sasPolicy = new SharedAccessBlobPolicy();
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calendar.add(Calendar.HOUR, 10);
			sasPolicy.setSharedAccessExpiryTime(calendar.getTime());
			sasPolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.LIST));
	        String sas = cloudBlockBlob.generateSharedAccessSignature(sasPolicy,null);
	        return cloudBlockBlob.getUri()+"?"+sas;
		}
		catch(InvalidKeyException | URISyntaxException | StorageException ex) {
			LOGGER.error("Exception occurred while creating sas  url for azure location! ", ex);
			throw new Exception(ex);
		}
	}
}