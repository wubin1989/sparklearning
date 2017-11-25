package com.idatage.ivst.ml

import com.mongodb.spark._
import com.mongodb.spark.sql._
import org.bson.Document
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.linalg.{ Vector, Vectors }
import org.apache.spark.mllib.linalg.{ Matrix, Matrices }
import org.apache.spark.mllib.regression.LabeledPoint
import java.util.ArrayList
import scala.collection.JavaConversions._
import org.apache.spark.rdd._
import org.apache.spark.mllib.util.MLUtils
import scala.collection.immutable._
import scala.collection.mutable.{ Seq }
import org.apache.log4j.{ Level, LogManager, PropertyConfigurator }
import com.mongodb.spark.config.WriteConfig

object MongoDemo {
  def main(args: Array[String]): Unit = {

    val log = LogManager.getRootLogger
    log.setLevel(Level.WARN)

    val myLog = LogManager.getLogger("myLogger")

    val spark = SparkSession
      .builder
      .master("local")
      .appName(s"${this.getClass.getSimpleName}")
      .config("spark.mongodb.input.uri", "mongodb://127.0.0.1/tarantula.toutiaos?readPreference=primaryPreferred")
      .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/tarantula.tags")
      .getOrCreate()

//    val docs = """
//  {"name": "Bilbo Baggins", "age": 50}
//  {"name": "Gandalf", "age": 1000}
//  {"name": "Thorin", "age": 195}
//  {"name": "Balin", "age": 178}
//  {"name": "Kíli", "age": 77}
//  {"name": "Dwalin", "age": 169}
//  {"name": "Óin", "age": 167}
//  {"name": "Glóin", "age": 158}
//  {"name": "Fíli", "age": 82}
//  {"name": "Bombur"}""".trim.stripMargin.split("[\\r\\n]+").toSeq

    import spark.implicits._

//    val writeConfig = WriteConfig(Map("collection" -> "demos", "writeConcern.w" -> "majority"), Some(WriteConfig(spark)))
//    val rddDocuments = spark.sparkContext.parallelize(docs.map(Document.parse))
//    MongoSpark.save(rddDocuments, writeConfig)

    val df = MongoSpark.load(spark)

    println(df.count())

    val filterEmpty = udf((array: Seq[String]) => if (array.length > 0) true else false)
    val resultDf = df.select("tag").where(filterEmpty($"tag"))
    resultDf.show(30)
    
    spark.stop()
  }
}