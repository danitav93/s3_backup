package com.nodelab.s3;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * This sample demonstrates how to make basic requests to Amazon S3 using the
 * AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use Amazon S3. For more information on Amazon
 * S3, see http://aws.amazon.com/s3.
 * <p>
 * Fill in your AWS access credentials in the provided credentials file
 * template, and be sure to move the file to the default location
 * (C:\\Users\\Daniele\\.aws\\credentials) where the sample code will load the credentials from.
 * <p>
 * <b>WARNING:</b> To avoid accidental leakage of your credentials, DO NOT keep
 * the credentials file in your source directory.
 *
 * http://aws.amazon.com/security-credentials
 */
public class S3 {

	public AmazonS3 s3;
	public String bucketName;

	/**
	 * Costruttore
	 * @throws IOException
	 * @throws Exceptions.PropertyNotFoundException
	 * @throws AmazonClientException
	 */
	public  S3() throws Exceptions.PropertyNotFoundException,AmazonClientException {

		/*
		 * The ProfileCredentialsProvider will return your [default]
		 * credential profile by reading from the credentials file located at
		 * (C:\\Users\\Daniele\\.aws\\credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("C:\\S3_BACKUP\\credentials", "default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"NON SONO RIUSCITO A CARICARE LE CREDENZIALI. " +
							"ASSICURATI CHE LE TUE CREDENZIALI SIANO SALVATI NELLA " +
							"LOCATION "+Constants.S3_CREDENTIALS_PATH+", E SONO IN UN FORMATO VALIDO.",
							e);
		}

		String region = PropertiesUtility.getPropValues(Constants.regionProperties);
		if (region==null) {
			System.out.println("===========================================");
			System.out.println("ATTENZIONE NON HO TROVATO IL FILE CONFIG O LA PROPRIETà: "+Constants.regionProperties);
			System.out.println("===========================================\n");
			throw new Exceptions.PropertyNotFoundException("ATTENZIONE NON HO TROVATO IL FILE CONFIG O LA PROPRIETà: "+Constants.regionProperties);
		}


		s3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(region)
				.build();


		bucketName = PropertiesUtility.getPropValues(Constants.bucketNameProperties);

		if (bucketName==null) {
			System.out.println("===========================================");
			System.out.println("ATTENZIONE NON HO TROVATO IL FILE CONFIG O LA PROPRIETà: "+Constants.bucketNameProperties);
			System.out.println("===========================================\n");
			throw new Exceptions.PropertyNotFoundException("ATTENZIONE NON HO TROVATO IL FILE CONFIG O LA PROPRIETà: "+Constants.bucketNameProperties);
		}


	}

	public void  createBucket() {

		/*
		 * Create a new S3 bucket - Amazon S3 bucket names are globally unique,
		 * so once a bucket name has been taken by any user, you can't create
		 * another bucket with that same name.
		 *
		 * You can optionally specify a location for your bucket if you want to
		 * keep your data closer to your applications or users.
		 */

		System.out.println("Creating bucket " + bucketName + "\n");

		s3.createBucket(bucketName);


	}



	public List<String> listBuckets() {

		List<String> list= new ArrayList<>();
		/*
		 * List the buckets in your account
		 */
		System.out.println("Listing buckets");
		for (Bucket bucket : s3.listBuckets()) {
			System.out.println(" - " + bucket.getName());
			list.add(bucket.getName());
		}
		System.out.println();

		return list;
	}

	public void downloadAndSaveObject(String key,String directoryToSaveFIlesPath) throws AmazonServiceException,AmazonClientException,IOException,Exceptions.CannotSaveException{

		/*
		 * Download an object - When you download an object, you get all of
		 * the object's metadata and a stream from which to read the contents.
		 * It's important to read the contents of the stream as quickly as
		 * possibly since the data is streamed directly from Amazon S3 and your
		 * network connection will remain open until you read all the data or
		 * close the input stream.
		 *
		 * GetObjectRequest also supports several other options, including
		 * conditional downloading of objects based on modification times,
		 * ETags, and selectively downloading a range of an object.
		 */
		System.out.println("Downloading an object");



		//This is where the downloaded file will be saved
		File localFile = new File(directoryToSaveFIlesPath+"/"+key);

		//This returns an ObjectMetadata file but you don't have to use this if you don't want 
		s3.getObject(new GetObjectRequest(bucketName, key), localFile);

		//Now your file will have your image saved 
		if (!(localFile.exists() && localFile.canRead())) {
			throw new Exceptions.CannotSaveException("Errore durante il salvataggio");
		}



	}

	public void executeBackup(JProgressBar pbProgress, JLabel lblStatus, Date da,Date a,String directoryToSaveFIlesPath) throws AmazonServiceException,AmazonClientException,IOException,Exceptions.CannotSaveException {
		List<String> keys=new ArrayList<>();
		Date lastModified;
		ObjectListing  objectListing= s3.listObjects(bucketName);
		boolean last=false;
		while (!last) {
			List<S3ObjectSummary> list=objectListing.getObjectSummaries();
			for (S3ObjectSummary summary:list) {

				lastModified = summary.getLastModified();

				if ( (!lastModified.after(a)) && (!lastModified.before(da)) )  {
					keys.add(summary.getKey());
				}

			}
			if (objectListing.isTruncated()) {
				objectListing= s3.listNextBatchOfObjects(objectListing);
			} else {
				last=true;
			}
		} ;


		int i=0;

		lblStatus.setText(i+" di "+keys.size());

		for (String key : keys) {

			downloadAndSaveObject(key,directoryToSaveFIlesPath);

			i++;

			lblStatus.setText(i+" di "+keys.size());

			pbProgress.setValue(100*i/keys.size());

		}


	}










}
