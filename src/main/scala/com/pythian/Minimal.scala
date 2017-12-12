package com.pythian

import java.io.FileInputStream
import dispatch._, Defaults._, oauth._
import org.asynchttpclient.oauth.{ConsumerKey, RequestToken}
import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.{PubsubMessage, TopicName}
import com.google.protobuf.ByteString
import com.typesafe.config.Config

object Minimal {

  private val conf: Config = com.typesafe.config.ConfigFactory.load.getConfig("app")

  val publisher = Publisher
    .newBuilder(TopicName.of(conf.getString("projectId"), conf.getString("topic")))
    .setCredentialsProvider(
      new CredentialsProvider {
        def getCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(conf.getString("key")))
      })
    .build()

  private val ck = new ConsumerKey(conf.getString("consumerKey"), conf.getString("consumerSecret"))
  private val cr = new RequestToken(conf.getString("accessToken"), conf.getString("accessTokenSecret"))

  Http.default(
    url("https://stream.twitter.com/1.1/statuses/sample.json") <@ (ck, cr) >
      as.stream.Lines{ tweet =>
        val data = ByteString.copyFromUtf8(tweet)
        val pubsubMessage = PubsubMessage.newBuilder.setData(data).build()
        publisher.publish(pubsubMessage)
      })

}
