package com.idatage.ivst.ml.util

import scala.collection.mutable.LinkedHashMap

object LDATextClassifyAPP extends App {

    val texts = Array(args(0))
    //val texts = Array("新华社古巴圣地亚哥１２月４日电（记者钱泳文 倪瑞捷）古巴革命领袖菲德尔·卡斯特罗的骨灰４日在古巴第二大城市圣地亚哥市圣伊菲热尼亚墓地安葬，为期９天的国悼期也于当日结束。当地时间６时３５分，运送卡斯特罗骨灰的灵车从圣地亚哥安东尼奥·马赛奥革命广场出发，于７时整抵达圣伊菲热尼亚墓地。卡斯特罗的骨灰被安放在一个刻有他全名的木质骨灰盒内，骨灰盒上覆盖着一面折叠的古巴国旗，外有透明水晶壳保护。在抵达圣伊菲热尼亚墓地后，卡斯特罗的亲友举行了一场私人葬礼，葬礼并未对外开放。尽管启程时间非常早，但车队前往墓地的沿途仍聚满了送别卡斯特罗的群众，不少群众都是在参加完前一晚的集会活动后在广场上席地而睡，再向卡斯特罗做最后的告别。记者在现场看到，除了古巴民众外，还有来自委内瑞拉、厄瓜多尔、俄罗斯等国家的民众前来送别卡斯特罗。尽管知道葬礼不对外开放，但还是有许多民众聚集在墓地附近送别这位革命领袖。他们告诉记者：“失去领袖的心情非常沉痛，我们每个人都会继续怀念他。”１１月３０日至１２月３日，运送卡斯特罗骨灰的灵车车队从首都哈瓦那启程，沿着当年革命军的行进路线，途经１３个省份最终抵达圣地亚哥。菲德尔·卡斯特罗１１月２５日晚在哈瓦那逝世，享年９０岁。")
    val inputTexts = texts.zipWithIndex.map {
      case (x, i) =>
        val inputText = new InputText()
        inputText.setId(i.toString() + "." + i.toString())
        inputText.setContent(x)
        inputText
    }
    val baseFilePath = "spark-warehouse/data/user/toutiao_training.csv"
    val modelPath = "./ldamodel"
    val tagFile = "spark-warehouse/data/user/tags.json"
    val topicFile = "spark-warehouse/data/user/topic.json"
    val idfModelPath = "./idfmodel"
    val testDS = LDATextClassify.processText(inputTexts, baseFilePath, idfModelPath)
    val result = LDATextClassify.transform(modelPath, testDS, inputTexts, tagFile, topicFile)
    
    result.toIterable.foreach { case (id, labels) => 
        println(s"标签: ${labels.mkString(",")}")
    }

}