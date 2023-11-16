package com.example;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
public class App {
	public static void main(String[] args) throws Exception {  
        Configuration conf = new Configuration();  
        String[] otherArgs = new GenericOptionsParser(conf, args)  
                .getRemainingArgs();  
        if (otherArgs.length != 2) {  
            System.err.println("Usage: numbersum <in> <out>");  
            System.exit(2);  
        }  
        long startTime = System.currentTimeMillis();// 计算时间  
        Job job = Job.getInstance(conf);
        job.setJarByClass(App.class);  
        job.setMapperClass(BayesMapper.class);  
        job.setReducerClass(BayesReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(MyWritable.class);
        job.setOutputKeyClass(IntWritable.class);  
        job.setOutputValueClass(MyWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));  
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));  
        job.waitForCompletion(true);  
        long endTime = System.currentTimeMillis();  
        System.out.println("time=" + (endTime - startTime));  
        System.exit(0);  
    }  
 
}