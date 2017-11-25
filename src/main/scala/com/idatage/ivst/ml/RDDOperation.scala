package com.idatage.ivst.ml

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
import scala.collection.mutable._

object RDDOperation {
  def main(args: Array[String]) {

    val conf: SparkConf = new SparkConf()
      .setAppName("spark-learning")
      .setMaster("local")
      .set("spark.executor.memory", "1g")
    val sc: SparkContext = new SparkContext(conf)
    val rdd: RDD[String] = sc.parallelize(List("word", "hello", "world", "word", "hello", "apple", "bear", "apple"))
    val valueCount = rdd.countByValue()
    val sortedResult = new LinkedHashMap() ++ valueCount.toSeq.sortBy(-_._2)
    sortedResult.foreach(println)
//    rdd.saveAsTextFile("./savedfile")

    //    args.foreach(println) // jar后面的是参数
    //    val inputfile = args(0)
    //    val outputfile = args(1)
    //    val conf: SparkConf = new SparkConf()
    //                .setAppName("spark-learning")
    //                .setMaster("local")
    //                .set("spark.executor.memory", "1g")
    //    val sc: SparkContext = new SparkContext(conf)
    //    val lines = sc.textFile(inputfile)
    //    //lines.foreach(println)
    //    //lines.filter(_.contains("show")).foreach(println)
    //    
    //    val words = lines.flatMap(_.split(" "))
    //    val counts = words.map((_, 1)).reduceByKey(_ + _)
    //    counts.saveAsTextFile(outputfile)
  }
}