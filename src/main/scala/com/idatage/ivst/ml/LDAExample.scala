package com.idatage.ivst.ml

import scala.collection.JavaConversions._
import org.apache.spark.ml.clustering._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.ml.linalg._
import org.apache.spark.ml.feature._
import com.idatage.ivst.ml._
import org.apache.zookeeper.server.quorum.Election
import org.apache.spark.mllib.util._
import scala.util._
import org.apache.spark.ml.{ Pipeline, PipelineModel }
import scala.collection.mutable._
import com.mongodb.spark._
import com.mongodb.spark.sql._
import com.mongodb.spark.config.WriteConfig
import org.bson.Document

object LDAExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .master("local")
      .appName(s"${this.getClass.getSimpleName}")
      .config("spark.mongodb.input.uri", "mongodb://127.0.0.1/tarantula.tags?readPreference=primaryPreferred")
      .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/tarantula.topics")
      .getOrCreate()

    val tagsDf = MongoSpark.load(spark)
    tagsDf.createOrReplaceTempView("tags")

    val clazzSize = 7

    val df = spark.read.option("header", "true").csv("spark-warehouse/data/user/toutiao_training.csv")
    val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
    val wordsData = tokenizer.transform(df)
    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures")
      .setNumFeatures(scala.math.pow(2, 12).asInstanceOf[Int])
    val featurizedData = hashingTF.transform(wordsData)

    //计算每个词的TF-IDF
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    println("===================================")
    println("===================================")

    //转换成Bayes的输入格式
    import spark.implicits._
    val trainDataRdd = rescaledData.select("tag", "features").map {
      case Row(label: String, features: Vector) => LabeledPoint(label.toDouble, Vectors.dense(features.toArray))
    }

    // Trains a LDA model.
    val lda = new LDA().setK(clazzSize).setMaxIter(12)
    val model = lda.fit(trainDataRdd)

    val ll = model.logLikelihood(trainDataRdd)
    val lp = model.logPerplexity(trainDataRdd)
    println(s"The lower bound on the log likelihood of the entire corpus: $ll")
    println(s"The upper bound bound on perplexity: $lp")

    model.save("./ldamodel")

    //val model = LocalLDAModel.load("./ldamodel").asInstanceOf[LDAModel]

    val topics = model.describeTopics(5)
    println("The topics described by their top-weighted terms:")
    topics.show(false)

    var transformed = model.transform(trainDataRdd)
    
    val apply_vec = udf((v: Vector, i: Int) => Try(v(i)).toOption)
    val vectorToArray = udf[Array[Double], Vector]((v: Vector) => v.toArray)

    transformed = transformed.withColumn("topicDistribution", vectorToArray($"topicDistribution"))
    transformed.printSchema()
    transformed.createOrReplaceTempView("temp")

    Range(0, 7).foreach { x =>
      val orderedRows = spark.sql(s"select label, avg(topicDistribution[$x]) from temp group by label order by avg(topicDistribution[$x]) desc")
      val label = orderedRows.first().getDouble(0).asInstanceOf[Int]
      val labelRow = spark.sql(s"select name from tags where _id=$label")
      val labelName = labelRow.first().getString(0)
      val document = new Document()
      document.put("code", x)
      document.put("name", labelName)
      val resultRdd = spark.sparkContext.parallelize(Seq(document))
      resultRdd.saveToMongoDB()
    }

    spark.stop()
  }
}

