package com.azure.keyvault;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.azure.keyvault.utils.AzureVaultUtils;
import com.azure.keyvault.utils.AzureVaultUtils.KeyVaultProperties;
import com.azure.keyvault.utils.M3AzureStorageUtils;
import com.azure.keyvault.utils.M3AzureStorageUtils.FileDetails;
/**
 * Application class
 * @author Sandeep Kumar
 *
 */
@SpringBootApplication(scanBasePackages = "com.azure.keyvault.*")
public class Application implements CommandLineRunner  {

	private static final Logger LOGGER = LogManager.getLogger(Application.class);	
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	
	@Autowired
	private DataSource dataSource;
	
	@Override
	public void run(String...args) throws Exception {

		/**
		 * Database password decryption with Azure KeyVault stored secret Example
		 * 
		 * Obtaining database connection from data source for verification purpose
		 * While starting the spring boot task, it checks jasypt configuration: 
		 * 1. If it is set online=true then obtain the Azure Key Vault configuration and 
		 * connect with Azure KeyVault and obtain database password.
		 * 
		 * In case of online, while getting secret value from Azure Key Vault if any exception
		 * occurs then default value from configration will be picked up
		 * 
		 * 2. If it is set online=false then try to get encryption key from boot argument
		 * passed from java invocation e.g.
		 * java -jar azure-key-vault.jar secretCode=abcdef
		 */
		Connection connection = dataSource.getConnection();
		DatabaseMetaData metadata = connection.getMetaData();
		LOGGER.info(metadata.getDatabaseProductName() + " " + metadata.getDatabaseProductVersion());
		connection.close();
		
		
		/**
		 * Suppose, we have to use Azure Blob Storage and it's credential want to store into Azure KeyVault.
		 * In this case, simply get the credential from vault and make a connection to Azure Blob Storage
		 * e.g.
		 * 
		 * Suppose, Azure Blob Storage connection string is stored into Azure KeyVault with secret name
		 * 'azure-blob-connection-string'
		 * 
		 * KeyVaultProperties properties details are available in PropertyEncryptionConfig.java file
		 * 
		 * In similar way, database, mq connections, sftp connections can be created by obtaining password
		 * from KeyVault
		 * 
		 */
		KeyVaultProperties properties = new KeyVaultProperties();
		properties.setAzureLoginUri(azureLoginUri);
		properties.setScope(scope);
		properties.setResourceUri(resourceUri);
		properties.setTenantId(tenantId);
		properties.setClientId(clientId);
		properties.setClientKey(clientKey);
		properties.setSecretName("azure-blob-connection-string");
		properties.setOnline(true);
		String azureBlobStorageConnectionString = AzureVaultUtils.getSecretFromVault(properties);
		String containerName = "attachment-container";
		String filePath = "/";
		List<FileDetails> files = M3AzureStorageUtils.listFiles(azureBlobStorageConnectionString, containerName, filePath);
		files.stream().forEach(f -> System.out.println(f.getFilePath()));
		
	}

	
	@Value("${azure-keyvault.azure-login-uri:}")
	private String azureLoginUri;
	
	@Value("${azure-keyvault.scope:}")
	private String scope;
	
	@Value("${azure-keyvault.resource-uri:}")
	private String resourceUri;

	@Value("${azure-keyvault.tenant-id:}")
	private String tenantId;
	
	@Value("${azure-keyvault.client-id:}")
	private String clientId;
	
	@Value("${azure-keyvault.client-key:}")
	private String clientKey;

}
