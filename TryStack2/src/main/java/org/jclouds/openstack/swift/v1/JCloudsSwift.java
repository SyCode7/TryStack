package org.jclouds.openstack.swift.v1;

import static com.google.common.io.ByteSource.wrap;
import static org.jclouds.io.Payloads.newByteSourcePayload;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.io.Payload;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.domain.Container;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.options.CreateContainerOptions;
import org.jclouds.openstack.swift.v1.options.PutOptions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;

public class JCloudsSwift implements Closeable {
   public static final String CONTAINER_NAME = "SOMETHING_FUNNY2";
   public static final String OBJECT_NAME = "premier.txt";

   private SwiftApi swiftApi;
   public static void main(String[] args) throws IOException {
      JCloudsSwift jcloudsSwift = new JCloudsSwift();

      try {
         jcloudsSwift.TemporaryUrlKeyFromAccount();
    	 jcloudsSwift.uploadObjectFromString();
         jcloudsSwift.listContainers();
         jcloudsSwift.close();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         jcloudsSwift.close();
      }
   }

   
   TemporaryURLSigner signedURL = new TemporaryURLSigner(null);
   
   private void TemporaryUrlKeyFromAccount() {
	// TODO Auto-generated method stub
	
}

public JCloudsSwift() {
      Iterable<Module> modules = ImmutableSet.<Module>of(
            new SLF4JLoggingModule());

      String provider = "openstack-swift";
      String identity = "facebook633532755:facebook633532755"; // tenantName:userName
      String credential = "8PgaziE62RadEdUF";

      swiftApi = ContextBuilder.newBuilder(provider)
            .endpoint("http://x86.trystack.org:5000/v2.0/")
            .credentials(identity, credential)
            .modules(modules)
            .buildApi(SwiftApi.class);
   }

   private void createContainer() {
      System.out.println("Create Container");

      ContainerApi containerApi = swiftApi.getContainerApiForRegion("RegionOne");
      CreateContainerOptions options = CreateContainerOptions.Builder
            .metadata(ImmutableMap.of(
                  "key1", "value1",
                  "key2", "value2"));

      containerApi.create(CONTAINER_NAME);

      System.out.println("  " + CONTAINER_NAME);
   }

   private void uploadObjectFromString() {
      System.out.println("Upload Object From String");

      ObjectApi objectApi = swiftApi.getObjectApiForRegionAndContainer("RegionOne", CONTAINER_NAME);
  
     Payload payload = newByteSourcePayload(wrap("Hello Z Xz xZ xX ZXWorld".getBytes()));

      objectApi.put(OBJECT_NAME, payload, PutOptions.Builder.metadata(ImmutableMap.of("key1", "value1")));

      System.out.println("  " + OBJECT_NAME);
   }

   private void listContainers() {
      System.out.println("List Containers");

      ContainerApi containerApi = swiftApi.getContainerApiForRegion("RegionOne");
      Set<Container> containers = containerApi.list().toSet();

      for (Container container : containers) {
         System.out.println("  " + container);
      }
   }

   public void close() throws IOException {
      Closeables.close(swiftApi, true);
   }
}