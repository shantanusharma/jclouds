/**
 *
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.http.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jclouds.http.HttpCommandExecutorService;
import org.jclouds.http.TransformingHttpCommandExecutorService;
import org.jclouds.http.TransformingHttpCommandExecutorServiceImpl;
import org.jclouds.http.internal.JavaUrlHttpCommandExecutorService;
import org.jclouds.logging.Logger;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/**
 * Configures {@link JavaUrlHttpCommandExecutorService}.
 * 
 * Note that this uses threads
 * 
 * @author Adrian Cole
 */
@ConfiguresHttpCommandExecutorService
public class JavaUrlHttpCommandExecutorServiceModule extends AbstractModule {

   @Override
   protected void configure() {
      bindClient();
   }

   protected void bindClient() {
      bind(HttpCommandExecutorService.class).to(JavaUrlHttpCommandExecutorService.class).in(Scopes.SINGLETON);
      bind(HostnameVerifier.class).to(LogToMapHostnameVerifier.class);
      bind(TransformingHttpCommandExecutorService.class).to(TransformingHttpCommandExecutorServiceImpl.class).in(
               Scopes.SINGLETON);
   }

   /**
    * 
    * Used to get more information about HTTPS hostname wrong errors.
    * 
    * @author Adrian Cole
    */
   @Singleton
   static class LogToMapHostnameVerifier implements HostnameVerifier {
      @Resource
      private Logger logger = Logger.NULL;
      private final Map<String, String> sslMap = Maps.newHashMap();;

      public boolean verify(String hostname, SSLSession session) {
         logger.warn("hostname was %s while session was %s", hostname, session.getPeerHost());
         sslMap.put(hostname, session.getPeerHost());
         return true;
      }
   }

   @Provides
   @Singleton
   @Named("untrusted")
   SSLContext provideUntrustedSSLContext(TrustAllCerts trustAllCerts) throws NoSuchAlgorithmException,
            KeyManagementException {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, new TrustManager[] { trustAllCerts }, new SecureRandom());
      return sc;
   }

   /**
    * 
    * Used to trust all certs
    * 
    * @author Adrian Cole
    */
   @Singleton
   static class TrustAllCerts implements X509TrustManager {
      public X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {
         return;
      }

      public void checkServerTrusted(X509Certificate[] certs, String authType) {
         return;
      }
   }
}