# 金融大数据作业五
## 实验结果截图展示
![result1](https://github.com/shich-Liu/-5/assets/128021744/7a37e841-8f7f-4bc4-bbc5-205bb798fd65)
![result2](https://github.com/shich-Liu/-5/assets/128021744/b63b0470-2dbc-468d-be6e-3c81bd4cfc76)
![result3](https://github.com/shich-Liu/-5/assets/128021744/eafcf793-5a14-490d-8917-7b7f4fa12a62)

## 代码解释
这段代码使用Hadoop MapReduce框架实现的简单程序，目的是对两个文件进行合并，并去除其中的重复内容，最终输出到一个新的文件。
1. public static class Map extends Mapper<Object, Text, Text, Text>:
   这是Mapper类的定义，它继承自Hadoop MapReduce框架中的`Mapper`类。
   泛型参数的含义是输入键类型（`Object`）、输入值类型（`Text`）、输出键类型（`Text`）、输出值类型（`Text`）。
   Mapper的主要任务是将输入数据切分成键值对并进行初步处理。

2. public void map(Object key, Text value, Context context) throws IOException, InterruptedException:
   这是Mapper类中的`map`方法，实际的映射逻辑在这里实现。
   输入参数是`key`、`value`和`Context`对象。`key`是输入的键，`value`是输入的值，`Context`用于写出Mapper的输出。
   在这个例子中，`map`方法直接将输入的文本值作为输出的键，值设为空字符串。

3. public static class Reduce extends Reducer<Text, Text, Text, Text>:
   这是Reducer类的定义，同样继承自Hadoop MapReduce框架中的`Reducer`类。
   泛型参数的含义是输入键类型（`Text`）、输入值类型（`Text`）、输出键类型（`Text`）、输出值类型（`Text`）。
   Reducer的主要任务是对相同键的值进行聚合。

4. public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException:
   这是Reducer类中的`reduce`方法，实现了Reducer的逻辑。
   输入参数是`key`、`values`和`Context`对象。`key`是输入的键，`values`是对应键的所有值的迭代器，`Context`用于写出Reducer的输出。
   在这个例子中，reduce方法直接将输入的键作为输出的键，值设为空字符串。

5. public static void main(String[] args) throws Exception:
   这是主程序入口，包含了整个MapReduce任务的设置和提交。
   Configuration conf = new Configuration();：创建Hadoop配置对象。
   String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();：解析命令行参数。
   Job job = Job.getInstance(conf, "Merge and duplicate removal");：创建一个新的Job。
   job.setJarByClass(App.class);：设置Job运行的主类。
   job.setMapperClass(Map.class);：设置Mapper类。
   job.setCombinerClass(Reduce.class);：设置Combiner类，它在Map端进行局部聚合。
   job.setReducerClass(Reduce.class);：设置Reducer类。
   job.setOutputKeyClass(Text.class);：设置输出键的类型。
   job.setOutputValueClass(Text.class);：设置输出值的类型。
   FileInputFormat.addInputPath(job, new Path(otherArgs[0]));：设置输入路径。
   FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));：设置输出路径。
   System.exit(job.waitForCompletion(true) ? 0 : 1);：提交Job并等待完成。

