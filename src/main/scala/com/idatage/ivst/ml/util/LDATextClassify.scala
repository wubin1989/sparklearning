package com.idatage.ivst.ml.util

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
import com.mongodb.spark._
import com.mongodb.spark.sql._
import com.mongodb.spark.config.WriteConfig
import org.bson.Document
import org.apache.spark.sql.types._

import scala.collection.mutable.LinkedHashMap
import scala.collection.immutable.HashMap
import scala.collection.mutable.Map
import scala.collection.immutable.Set
import org.apache.spark.ml.feature.IDFModel

class LDATextClassify {
}

object LDATextClassify {
  private val spark: SparkSession = SparkSession
      .builder
      .master("local")
      .appName(s"${this.getClass.getSimpleName}")
      //    .config("spark.mongodb.input.uri", "mongodb://127.0.0.1/tarantula.tags?readPreference=primaryPreferred")
      //    .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/tarantula.topics")
      .getOrCreate()
  
  import spark.implicits._

  def processText(inputTexts: Array[InputText], baseFilePath: String, idfModelPath: String): Dataset[LabeledPoint] = {
    val schema = StructType(Seq(StructField("id", StringType, false), StructField("tag", StringType, false), StructField("text", StringType, false)))
    val inputTextSeq: Seq[Row] = inputTexts.map { (inputText) => Row.fromSeq(Seq(inputText.getId, inputText.getId, JiebaCut.cutText(inputText.getContent))) }
    val inputTextDF = spark.createDataFrame(inputTextSeq, schema)
    //val df = spark.read.option("header", "true").schema(schema).csv(baseFilePath).union(inputTextDF)
    //val df = spark.sqlContext.read.option("header", "true").schema(schema).format("org.apache.spark.sql.execution.datasources.csv.CSVFileFormat").load(baseFilePath).union(inputTextDF)
    val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
    val wordsData = tokenizer.transform(inputTextDF)
    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures")
      .setNumFeatures(scala.math.pow(2, 10).asInstanceOf[Int])
    val featurizedData = hashingTF.transform(wordsData)
    featurizedData.cache()

    // generate and save idf model
    // val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    // val idfModel = idf.fit(featurizedData)
    // idfModel.save("./idfmodel")

    // load local idf model
    val idfModel = IDFModel.load(idfModelPath)

    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.select("tag", "features").map {
      case Row(label: String, features: Vector) => LabeledPoint(label.toDouble, Vectors.dense(features.toArray))
    }
  }

  def transform(modelPath: String, data: Dataset[LabeledPoint], inputTexts: Array[InputText], tagFile: String, topicFile: String): LinkedHashMap[String, Array[String]] = {
    val tagsDf = spark.read.json(tagFile)
    tagsDf.createOrReplaceTempView("tags")
    val topicDf = spark.read.json(topicFile)
    topicDf.createOrReplaceTempView("topic")
    val model = LocalLDAModel.load(modelPath).asInstanceOf[LDAModel]
    var transformed = model.transform(data)
    val vectorToArray = udf[Array[Double], Vector]((v: Vector) => v.toArray)
    transformed = transformed.withColumn("topicDistribution", vectorToArray($"topicDistribution"))
    transformed.createOrReplaceTempView("temp")
    val labelTopic = new LinkedHashMap[String, Array[String]]()
    inputTexts.foreach { x =>
      val label = x.getId.toDouble
      val row = spark.sql(s"select topicDistribution from temp where label=${label}")
      val topicDistribution = row.first().getAs[Seq[Double]]("topicDistribution")
      topicDistribution.foreach { x => println(x) }
      val topicDistributionMap = new LinkedHashMap[Int, Double]()
      topicDistribution.zipWithIndex.foreach {
        case (x, i) =>
          topicDistributionMap.put(i, x)
      }
      val sortedTopicDistributionMap = new LinkedHashMap[Int, Double]() ++ (topicDistributionMap.toSeq.sortBy(-_._2).slice(0, 2))
      val keySeq = sortedTopicDistributionMap.keySet.toSeq
      val nameRows = spark.sql(s"select name from topic where code=${keySeq(0)} or code=${keySeq(1)}")
      println("============= below is topic name table =============")
      nameRows.show()
      val names = nameRows.select("name").map(_.getString(0)).collect()
      labelTopic.put(x.getId, names)
    }
    labelTopic
  }
}