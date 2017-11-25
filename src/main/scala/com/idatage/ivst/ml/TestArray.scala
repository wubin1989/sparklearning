package com.idatage.ivst.ml

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
import com.mongodb.spark._
import com.mongodb.spark.sql._
import com.mongodb.spark.config.WriteConfig
import com.mongodb.spark.sql.fieldTypes.ObjectId

object TestArray extends App {

  val log = LogManager.getRootLogger
  log.setLevel(Level.WARN)

  val myLog = LogManager.getLogger("myLogger")

  val spark = SparkSession
    .builder
    .master("local")
    .appName(s"${this.getClass.getSimpleName}")
    .config("spark.mongodb.input.uri", "mongodb://127.0.0.1/tarantula.demos?readPreference=primaryPreferred")
    .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/tarantula.demos")
    .getOrCreate()
    
    import spark.implicits._

//    val docs = """
//    {"name": "Bilbo Baggins", "score": [11, 60,34,53,23,98, 221]}
//    {"name": "Bilbo Baggins", "score": [80, 60,34,53,23,98, 221]}
//    {"name": "Bilbo Baggins", "score": [75, 60,34,53,23,98, 221]}
//    {"name": "Bilbo Baggins", "score": [67, 60,34,53,23,98, 221]}
//    {"name": "Gandalf", "score": [24, 60,34,53,23,98, 221]}
//    {"name": "Thorin", "score": [67, 60,34,53,23,98, 221]}
//    {"name": "Balin", "score": [56, 60,34,53,23,98, 221]}
//    {"name": "Kíli", "score": [44, 60,34,53,23,98, 221]}
//    {"name": "Kíli", "score": [54, 60,34,53,23,98, 221]}
//    {"name": "Kíli", "score": [34, 60,34,53,23,98, 221]}
//    {"name": "Dwalin", "score": [22, 60,34,53,23,98, 221]}
//    {"name": "Óin", "score": [99, 60,34,53,23,98, 221]}
//    {"name": "Glóin", "score": [23, 60,34,53,23,98, 221]}
//    {"name": "Fíli", "score": [38, 60,34,53,23,98, 221]}
//    {"name": "Fíli", "score": [18, 60,34,53,23,98, 221]}
//    {"name": "Fíli", "score": [17, 60,34,53,23,98, 221]}
//    {"name": "Fíli", "score": [25, 60,34,53,23,98, 221]}
//    {"name": "Bombur", "score": [74, 60,34,53,23,98, 221]}""".trim.stripMargin.split("[\\r\\n]+").toSeq
//
//    val writeConfig = WriteConfig(Map("collection" -> "demos", "writeConcern.w" -> "majority"), Some(WriteConfig(spark)))
//    val rddDocuments = spark.sparkContext.parallelize(docs.map(Document.parse))
//    MongoSpark.save(rddDocuments, writeConfig)

  val df = MongoSpark.load(spark)

  println(df.count())
  
  df.createOrReplaceTempView("scores")
  
  val returnedDf = spark.sql("select _id, name, score[0] as average_score from scores order by score[0] desc")
  returnedDf.show()
  
  val newDocument = new Document()
  
  val firstRow = returnedDf.first()

  val oid = new ObjectId(firstRow.getAs("_id").toString())

  newDocument.put("_id", oid.toString())
  newDocument.put("name", firstRow.getAs("name"))
  newDocument.put("score", firstRow.getAs("average_score"))
  
  val rddDocuments = spark.sparkContext.parallelize(Seq(Document.parse(newDocument.toJson())))
  rddDocuments.saveToMongoDB()
  

  
  
  
  
  spark.stop()

}