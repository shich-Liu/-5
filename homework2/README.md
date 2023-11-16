# 金融大数据实验二
## 任务一
编写MapReduce程序，统计数据集中违约和⾮违约的数量，按照标签TARGET进⾏输出，即1代表有违约的情况出现，0代表其他情况。
### 实验结果
![F28E37EEBE7C93698F4BC041FD22926B](https://github.com/shich-Liu/-5/assets/128021744/e52e0923-936f-42f1-b601-44e489fbd6e8)
![6`TO3( `WSR`XD@(TUYM5SM](https://github.com/shich-Liu/-5/assets/128021744/02d6c2c5-14f7-4ada-94d5-acb44f4d7dd8)
![DI%12SG~XKVMI4Q)WQ{ PLG](https://github.com/shich-Liu/-5/assets/128021744/1c8eafbb-8104-41bc-874b-d41769c55861)

### 实验思路
这段代码用于csv文件中TARGET列中1/0的数量即违约和非违约的数量，并按照标签TARGET进行输出。
1. **Mapper类 (Map):**
   - `map` 方法: 该方法对输入的每一行进行映射。在这个例子中，输入数据被认为是逗号分隔的值，通过`split(",")`将一行数据分割成数组。然后，它获取数组的最后一列的值，即标签TARGET的值。特殊处理了"null"标记，将其输出为"0"。
   - 如果最后一列为空，将标记设为"null"，并输出 (word, 1)。
   - 如果最后一列为"0"，将标记设为"0"，并输出 (word, 1)。
   - 如果最后一列为"1"，将标记设为"1"，并输出 (word, 1)。

2. **Reducer类 (Reduce):**
   - `reduce` 方法: 该方法对Mapper输出的结果进行归约。对于相同的标记（"null", "0", "1"），对其对应的值进行求和。
   - 如果标记为"null"，将标记设为"0"，最后输出 (key, sum)。

3. **main:**
   - 设置Job的相关属性，包括输入输出路径、Mapper和Reducer类等。
   - 配置输入输出的数据格式和路径。
   - 提交Job并等待完成。

细节分析：
1、Mapper的map方法，对每一行数据进行映射
```
public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    // 将逗号分隔的一行数据拆分成数组
    String[] line = value.toString().split(",");
    // 获取最后一列的值，即标签TARGET的值
    String lastColumnValue = line[line.length - 1];
    // 如果最后一列为空，则标记为"null"，并输出 (word, 1)
    if (lastColumnValue.isEmpty()) {
        word.set("null");
        context.write(word, one);
    // 如果最后一列为"0"，则标记为"0"，并输出 (word, 1)
    } else if (lastColumnValue.equals("0")) {
        word.set("0");
        context.write(word, one);
    // 如果最后一列为"1"，则标记为"1"，并输出 (word, 1)
    } else if (lastColumnValue.equals("1")) {
        word.set("1");
        context.write(word, one);
    }
}
```

2、Reducer的reduce方法，对Mapper输出的结果进行归约
```
    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int sum = 0;
        // 对每个标记进行求和
        for (IntWritable val : values) {
            sum += val.get();
        }
        // 如果标记为"null"，将标记设为"0"
        if (key.toString().equals("null")) {
            key.set("0");
        }
        // 输出最终结果 (key, sum)
        result.set(sum);
        context.write(key, result);
    }
}
```

3、主函数
```
    public static void main(String[] args) throws Exception {
        // 创建一个Job实例
        Job job = Job.getInstance();
        // 设置Jar包
        job.setJarByClass(App.class);
        job.setJobName("Count Zeros and Ones");
        // 设置输入输出格式
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        // 设置Mapper和Reducer类
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);
        // 设置输出键值对的类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        // 设置输入输出路径
        TextInputFormat.addInputPath(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));
        // 提交Job并等待完成
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
```
   
## 任务二
编写MapReduce程序，统计⼀周当中每天申请贷款的交易数WEEKDAY_APPR_PROCESS_START，并按照交易数从⼤到⼩进⾏排序。
### 实验结果
![MI_VD 29F V`2U`J S{MY2](https://github.com/shich-Liu/-5/assets/128021744/6ef71db6-2164-48dc-90e6-25356afc093b)
![{}X`MGPJ0A(2)@_B `@JR8W](https://github.com/shich-Liu/-5/assets/128021744/a999e03f-cfaa-4834-a2f2-03be4b8cee1e)
![GH1AF% N S}09PI5 Y@S}H](https://github.com/shich-Liu/-5/assets/128021744/3d1eb27d-3e35-45ef-ab00-8ebf2bd59982)
### 实验思路
通过获取CSV文件中的`WEEKDAY_APPR_PROCESS_START`列来确定每个交易的weekday标签，并统计每个weekday的交易数量。最后，按照交易数从大到小排序，并输出每个weekday及其对应的交易数。

1. **Mapper阶段:**
   - `TokenizerMapper`类中的`map`方法将每行CSV数据按逗号分割，并检查是否至少有26列。
   - 如果有足够的列，提取第26列的数据，即`WEEKDAY_APPR_PROCESS_START`列
   - 如果该值是有效的星期几（"SUNDAY", "MONDAY", ..., "SATURDAY"），则作为键值对`(weekday, 1)`输出

2. **Combiner阶段:**
   - 使用了Combiner，即`IntSumReducer`类中的`reduce`方法。Combiner在Mapper和Reducer之间进行本地合并，提高效率。执行了和Reducer相同的逻辑，即对相同的键（周几）进行交易数量的求和。

3. **Reducer阶段:**
   - `IntSumReducer`类中的`reduce`方法将每个键（周几）的交易数量进行总和，然后将结果放入一个`TreeMap`中，以确保结果按降序排序。
   - 保留`TreeMap`中交易数量最大的前7个记录，由此可以剔除异常值

4. **cleanup方法:**
   - `cleanup`方法在Reducer阶段结束时被调用，用于最终输出。它遍历`TreeMap`，将每个周几及其对应的交易数量输出。

5. **主函数:**
   - 配置Hadoop Job，设置Mapper、Combiner、Reducer类等。
   - 指定输入输出路径。
   - 提交Job并等待完成。

细节分析：
1.Mapper的map方法，对每一行数据进行映射
```
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            // 假设CSV格式，通过逗号分隔
            String[] columns = value.toString().split(",");
            // 检查是否至少有26列，从而剔除异常情况
            if (columns.length >= 26) {
                String weekday = columns[25].trim().toUpperCase(); //索引从0开始，只有为weekday数据时才统计
                if (weekday.equals("SUNDAY") || weekday.equals("MONDAY") || weekday.equals("TUESDAY")
                        || weekday.equals("WEDNESDAY") || weekday.equals("THURSDAY") || weekday.equals("FRIDAY")
                        || weekday.equals("SATURDAY")) {
                    word.set(weekday);
                    context.write(word, one);
                }
            }
        }
```

2.Reducer的reduce方法，对Mapper输出的结果进行归约
```
        public void reduce(Text key, Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            // 按降序将结果放入TreeMap中
            sortedMap.put(sum, key.toString());
            // 保留前N个记录（根据需要调整N的值）
            if (sortedMap.size() > 7) {
                sortedMap.remove(sortedMap.firstKey());
            }
        }
```
3.cleanup方法在Reducer结束时被调用，输出最终结果
```
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (Integer count : sortedMap.descendingKeySet()) {
                context.write(new Text(sortedMap.get(count)), new IntWritable(count));
            }
        }
```

## 任务三
根据application_data.csv中的数据，基于MapReduce建⽴贷款违约检测模型，并评估实验结果的准确率。
### 实验结果
### 实验思路

