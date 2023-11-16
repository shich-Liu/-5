package com.example;
import java.io.IOException;
import java.util.TreeMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class App {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable>{

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            // Assuming CSV format, split by comma
            String[] columns = value.toString().split(",");
            
            // Check if there are at least 26 columns
            if (columns.length >= 26) {
                String weekday = columns[25].trim().toUpperCase(); // Assuming 0-based index
                if (weekday.equals("SUNDAY") || weekday.equals("MONDAY") || weekday.equals("TUESDAY")
                        || weekday.equals("WEDNESDAY") || weekday.equals("THURSDAY") || weekday.equals("FRIDAY")
                        || weekday.equals("SATURDAY")) {
                    word.set(weekday);
                    context.write(word, one);
                }
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {

        private TreeMap<Integer, String> sortedMap = new TreeMap<>();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            // Sorting in descending order
            sortedMap.put(sum, key.toString());

            // Keeping only the top N records (adjust N as needed)
            if (sortedMap.size() > 7) {
                sortedMap.remove(sortedMap.firstKey());
            }
        }

        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (Integer count : sortedMap.descendingKeySet()) {
                context.write(new Text(sortedMap.get(count)), new IntWritable(count));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "weekday count");
        job.setJarByClass(App.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

