package com.idatage.ivst.ml.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestJiebaCut {
	
	private String text="工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作";
			
	@Test
	public void test() {
		System.out.println(JiebaCut.cutText(text));
		assertNotNull(JiebaCut.cutText(text));
	}

}
