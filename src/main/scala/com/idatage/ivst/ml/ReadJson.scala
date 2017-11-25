package com.idatage.ivst.ml

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

object ReadJson {
  def main(args: Array[String]): Unit = {

    val log = LogManager.getRootLogger
    log.setLevel(Level.WARN)

    val myLog = LogManager.getLogger("myLogger")

    val spark = SparkSession
      .builder
      .master("local")
      .appName(s"${this.getClass.getSimpleName}")
      .getOrCreate()

    val df = spark.read.json("data/toutiao.json").sample(false, 0.3)
    myLog.warn(df.count())

    df.createOrReplaceTempView("toutiao")
    df.cache()

    val filterEmpty = udf((array: Seq[String]) => if (array.length > 0) true else false)

    import spark.implicits._

    val resultDf = df.select("tag").where(filterEmpty($"tag"))
    resultDf.show(30)

    myLog.warn(resultDf.count())

    spark.stop()

  }
}