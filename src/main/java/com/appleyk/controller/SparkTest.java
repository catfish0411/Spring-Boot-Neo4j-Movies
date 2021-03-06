package com.appleyk.controller;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SparkTest {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("wordCountTest");
        JavaSparkContext sc = new JavaSparkContext(conf);
        String outputDir="out";

        List<String> list=new ArrayList<String>();
        list.add("1 1 2 a b");
        list.add("a b 1 2 3");
        JavaRDD<String> RddList=sc.parallelize(list);
        System.out.println("===========>"+ RddList.toString());

        //先切分为单词，扁平化处理
        JavaRDD<String> flatMapRdd = RddList.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String str) {
                return Arrays.asList(str.split(" ")).iterator();
            }
        });

        //再转化为键值对
        JavaPairRDD<String, Integer> pairRdd = flatMapRdd.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });

        //对每个词语进行计数
        JavaPairRDD<String, Integer> countRdd = pairRdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });
        System.out.println("结果："+countRdd.collect());
        countRdd.saveAsTextFile(outputDir);
        sc.close();
    }
}
