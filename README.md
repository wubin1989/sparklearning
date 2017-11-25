# scala-maven-example
Scala Maven project example.

Simple project with no interesting code, only to show how to use scala-maven-plugin.


I have just began with Spark Streaming and I am trying to build a sample application that counts words from a Kafka stream. Although it compiles with sbt package, when I run it, I get NoClassDefFoundError. This post seems to have the same problem, but the solution is for Maven and I have not been able to reproduce it with sbt.

KafkaApp.scala:

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

object KafkaApp {
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("kafkaApp").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(1))
    val kafkaParams = Map(
        "zookeeper.connect" -> "localhost:2181",
        "zookeeper.connection.timeout.ms" -> "10000",
        "group.id" -> "sparkGroup"
    )

    val topics = Map(
        "test" -> 1
    )

    // stream of (topic, ImpressionLog)
    val messages = KafkaUtils.createStream(ssc, kafkaParams, topics, storage.StorageLevel.MEMORY_AND_DISK)
    println(s"Number of words: %{messages.count()}")
  }
}
build.sbt:

name := "Simple Project"

version := "1.1"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "1.1.1",
    "org.apache.spark" %% "spark-streaming" % "1.1.1",
    "org.apache.spark" %% "spark-streaming-kafka" % "1.1.1"
)

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"
And I submit it with:

bin/spark-submit \
  --class "KafkaApp" \
  --master local[4] \
  target/scala-2.10/simple-project_2.10-1.1.jar
Error:

14/12/30 19:44:57 INFO AkkaUtils: Connecting to HeartbeatReceiver: akka.tcp://sparkDriver@192.168.5.252:65077/user/HeartbeatReceiver
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/spark/streaming/kafka/KafkaUtils$
    at KafkaApp$.main(KafkaApp.scala:28)
    at KafkaApp.main(KafkaApp.scala)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:606)
    at org.apache.spark.deploy.SparkSubmit$.launch(SparkSubmit.scala:329)
    at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:75)
    at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: java.lang.ClassNotFoundException: org.apache.spark.streaming.kafka.KafkaUtils$
    at java.net.URLClassLoader$1.run(URLClassLoader.java:366)
    at java.net.URLClassLoader$1.run(URLClassLoader.java:355)
    at java.security.AccessController.doPrivileged(Native Method)
    at java.net.URLClassLoader.findClass(URLClassLoader.java:354)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:425)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:358)