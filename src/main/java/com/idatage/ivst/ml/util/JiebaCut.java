package com.idatage.ivst.ml.util;

import java.util.List;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

public class JiebaCut {
	public static String cutText(String text){
		String result = "";
		JiebaSegmenter segmenter = new JiebaSegmenter();
	    List<SegToken> sts = segmenter.process(text, SegMode.INDEX);
	    for(SegToken st: sts){
	    	result += st.word + " ";
	    }
	    return result.trim();
	}
}
