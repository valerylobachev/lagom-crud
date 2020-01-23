/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annette.shared.elastic

import java.security.cert.X509Certificate

import com.sksamuel.elastic4s.http.{JavaClient, _}
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.typesafe.config.Config
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.conn.ssl.{AllowAllHostnameVerifier, NoopHostnameVerifier, SSLConnectionSocketFactory}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.ssl.{SSLContexts, TrustStrategy}
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import play.api.Configuration

import scala.util.Try

object ElasticProvider {

  def create(config: Config): ElasticClient = {
    val url = Try { config.getString("elastic.url") }.toOption.getOrElse("http://localhost:9200")
    val maybeUsername = Try { config.getString("elastic.username") }.toOption
    val password = Try { config.getString("elastic.password") }.toOption.getOrElse("")
    val allowInsecure = Try { config.getBoolean("elastic.allowInsecure") }.toOption.getOrElse(false)

    val maybeProvider = maybeUsername.map { username =>
      val provider = new BasicCredentialsProvider
      val credentials = new UsernamePasswordCredentials(username, password)
      provider.setCredentials(AuthScope.ANY, credentials)
      provider
    }

    val mayBeSslContext = if (allowInsecure) {
      Some(
        SSLContexts
          .custom()
          .loadTrustMaterial(new TrustStrategy() {
            def isTrusted(chain: Array[X509Certificate], authType: String): Boolean = true
          })
          .build
      )
    } else None

    ElasticClient(
      JavaClient(
        ElasticProperties(url),
        NoOpRequestConfigCallback,
        new HttpClientConfigCallback {
          override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder) = {
            var res = httpClientBuilder
            res = maybeProvider.map(provider => res.setDefaultCredentialsProvider(provider)).getOrElse(res)
            res = mayBeSslContext
              .map { sslContext =>
                res
                  .setSSLContext(sslContext)
                  .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
              }
              .getOrElse(res)
            res
          }
        }
      )
    )
  }
}
