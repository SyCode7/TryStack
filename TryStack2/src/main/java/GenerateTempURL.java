/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.http.HttpRequest;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;


public class GenerateTempURL implements Closeable {
   private static final String FILENAME = "file.txt";
   private static final int TEN_MINUTES = 10 * 60;

   private final SwiftApi swiftApi ;
   
  
     public static void main(String[] args) throws IOException {
      GenerateTempURL generateTempURL = new GenerateTempURL(args[0], args[1]);

      try {
         generateTempURL.generatePutTempURL();
         generateTempURL.generateGetTempURL();
         generateTempURL.generateDeleteTempURL();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
      finally {
         generateTempURL.close();
      }
   }

   public GenerateTempURL(String username, String apiKey) {
	   
	      String provider = "openstack-swift";
	      String identity = "facebook633532755:facebook633532755"; // tenantName:userName
	      String credential = "8PgaziE62RadEdUF";
	   
	   swiftApi = ContextBuilder.newBuilder(provider)
	            .endpoint("http://x86.trystack.org:5000/v2.0/")
	            .credentials(identity, credential)
	            .modules(modules)
	            .buildApi(SwiftApi.class);
	   }

   private void generatePutTempURL() throws IOException {
      System.out.format("Generate PUT Temp URL%n");

      // Create the Payload
      String data = "This object will be public for 10 minutes.";
      ByteSource source = ByteSource.wrap(data.getBytes());
      Payload payload = Payloads.newByteSourcePayload(source);

      // Create the Blob
      Blob blob = blobStore.blobBuilder(FILENAME).payload(payload).contentType("text/plain").build();
      HttpRequest request = blobStoreContext.getSigner().signPutBlob(CONTAINER, blob, TEN_MINUTES);

      System.out.format("  %s %s%n", request.getMethod(), request.getEndpoint());

      // PUT the file using jclouds
      HttpResponse response = blobStoreContext.utils().http().invoke(request);
      int statusCode = response.getStatusCode();

      if (statusCode >= 200 && statusCode < 299) {
         System.out.format("  PUT Success (%s)%n", statusCode);
      }
      else {
         throw new HttpResponseException(null, response);
      }
   }

   private void generateGetTempURL() throws IOException {
      System.out.format("Generate GET Temp URL%n");

      HttpRequest request = blobStoreContext.getSigner().signGetBlob(CONTAINER, FILENAME, TEN_MINUTES);

      System.out.format("  %s %s%n", request.getMethod(), request.getEndpoint());

      // GET the file using jclouds
      File file = File.createTempFile(FILENAME, ".tmp");
      Payload payload = blobStoreContext.utils().http().invoke(request).getPayload();

      try {
         Files.asByteSink(file).writeFrom(payload.openStream());

         System.out.format("  GET Success (%s)%n", file.getAbsolutePath());
      } finally {
         payload.release();
         file.delete();
      }
   }

   private void generateDeleteTempURL() throws IOException {
      System.out.format("Generate DELETE Temp URL%n");

      HttpRequest request = blobStoreContext.getSigner().signRemoveBlob(CONTAINER, FILENAME);

      System.out.format("  %s %s%n", request.getMethod(), request.getEndpoint());

      // DELETE the file using jclouds
      HttpResponse response = blobStoreContext.utils().http().invoke(request);
      int statusCode = response.getStatusCode();

      if (statusCode >= 200 && statusCode < 299) {
         System.out.format("  DELETE Success (%s)", statusCode);
      }
      else {
         throw new HttpResponseException(null, response);
      }
   }

   /**
    * Always close your service when you're done with it.
    */
   public void close() throws IOException {
      Closeables.close(blobStore.getContext(), true);
   }
}