package com.pythian

import java.io.FileInputStream
import dispatch._
import dispatch.oauth._
import Defaults._
import org.asynchttpclient.oauth.{ ConsumerKey, RequestToken }
import com.google.api.gax.batching.BatchingSettings
import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.pubsub.v1.Publisher
import com.google.common.util.concurrent.MoreExecutors
import com.google.pubsub.v1.{ PubsubMessage, TopicName }
import com.google.protobuf.ByteString
import com.typesafe.config.Config
import org.threeten.bp.Duration
import com.typesafe.scalalogging.LazyLogging
import scala.util._

object Twitter2pubsub extends App with LazyLogging {

  private val conf: Config = com.typesafe.config.ConfigFactory.load.getConfig("app")

  private val topic = TopicName.of(conf.getString("projectId"), conf.getString("topic"))

  val batchingSettings = BatchingSettings.newBuilder()
    .setElementCountThreshold(100L)
    .setRequestByteThreshold(4096L)
    .setDelayThreshold(Duration.ofSeconds(1L))
    .build()

  val credentialsProvider = new CredentialsProvider {
    def getCredentials: ServiceAccountCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(conf.getString("key")))
  }

  val publisher = Publisher
    .newBuilder(topic)
    .setCredentialsProvider(credentialsProvider)
    .setBatchingSettings(batchingSettings)
    .build()

  def publishPS(str: String): Unit = {
    val data = ByteString.copyFromUtf8(str)
    val pubsubMessage = PubsubMessage.newBuilder.setData(data).build
    val messageId = publisher.publish(pubsubMessage)
    val t1 = System.nanoTime()

    messageId.addListener(() ⇒ {
      Try(messageId.get()).failed.foreach(logger.info(s"Publish failed", _))
    }, MoreExecutors.directExecutor())
  }

  private val ck = new ConsumerKey(conf.getString("consumerKey"), conf.getString("consumerSecret"))
  private val cr = new RequestToken(conf.getString("accessToken"), conf.getString("accessTokenSecret"))

  private val svc = url("https://stream.twitter.com/1.1/statuses/sample.json") <@ (ck, cr)
  private val res = Http.default(svc > as.stream.Lines(publishPS))

  res.failed.foreach { ex ⇒
    ex.printStackTrace()
    Http.default.shutdown()
  }
}
