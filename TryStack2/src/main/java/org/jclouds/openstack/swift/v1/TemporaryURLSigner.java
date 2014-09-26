package org.jclouds.openstack.swift.v1;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.io.BaseEncoding.base16;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.jclouds.openstack.swift.v1.features.AccountApi;

import com.google.common.base.Supplier;

/**
 * Use this utility to create temporary urls.
 */
public class TemporaryURLSigner {

   public static TemporaryURLSigner checkApiEvery(final AccountApi api, long seconds) {
      Supplier<String> keySupplier = memoizeWithExpiration(new TemporaryUrlKeyFromAccount(api), seconds, SECONDS);
      return new TemporaryURLSigner(keySupplier);
      
      public static main(String[] args) throws IOException {
    	  TemoraryURLSigner temoraryURLSigner = new TemoraryURLSigner();
    	  
    	  
      }
   }

   private final Supplier<String> keySupplier;

   TemporaryURLSigner(Supplier<String> keySupplier) {
      this.keySupplier = keySupplier;
   }

   public String sign(String method, String path, long expirationTimestampSeconds) {
      checkNotNull(method, "method");
      checkNotNull(path, "path");
      checkArgument(expirationTimestampSeconds > 0, "expirationTimestamp must be a unix epoch timestamp");
      String hmacBody = format("%s\n%s\n%s", method, expirationTimestampSeconds, path);
      return base16().lowerCase().encode(hmacSHA1(hmacBody));
   }

   byte[] hmacSHA1(String data) {
      try {
         String key = keySupplier.get();
         checkState(key != null, "%s returned a null temporaryUrlKey!", keySupplier);
         Mac mac = Mac.getInstance("HmacSHA1");
         mac.init(new SecretKeySpec(key.getBytes(UTF_8), "HmacSHA1"));
         return mac.doFinal(data.getBytes(UTF_8));
      } catch (Exception e) {
         throw propagate(e);
      }
   }

   static class TemporaryUrlKeyFromAccount implements Supplier<String> {
      private final AccountApi api;

      private TemporaryUrlKeyFromAccount(AccountApi api) {
         this.api = checkNotNull(api, "accountApi");
      }

      public String get() {
         return api.get().getTemporaryUrlKey().orNull();
      }

      @Override
      public String toString() {
         return format("get().getTemporaryUrlKey() using %s", api);
      }
   }
}