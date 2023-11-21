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
![image](https://github.com/shich-Liu/-5/assets/128021744/a840de06-f5cb-4795-bdbd-5270a534e9e9)
![image](https://github.com/shich-Liu/-5/assets/128021744/e0a6aff6-81d1-481d-9ad0-698ff120741d)
![image](https://github.com/shich-Liu/-5/assets/128021744/6d0ce2bb-b5d8-4c7c-b0b2-21f816e76560)
### 实验思路
贝叶斯分类是一种基于贝叶斯定理的概率分类方法，它通过结合先验概率和观测数据的似然概率来计算后验概率，从而实现对新数据的分类，广泛应用于数据挖掘和机器学习领域。其核心原理可以概述如下：

1. **贝叶斯定理**：这是贝叶斯分类的基础，其数学表达式为：
   \[ P(A|B) = \frac{P(B|A) \times P(A)}{P(B)} \]
   其中，\( P(A|B) \) 是在事件B发生的条件下事件A发生的概率，即后验概率；\( P(B|A) \) 是在事件A发生的条件下事件B发生的概率，即似然概率；\( P(A) \) 是事件A发生的无条件概率，即先验概率；\( P(B) \) 是事件B发生的无条件概率。

2. **类别概率**：在分类问题中，贝叶斯分类器会计算每个类别的概率\( P(C_k) \)，即该类别出现的频率。

3. **条件概率**：贝叶斯分类器还计算给定类别下特定特征出现的概率\( P(X|C_k) \)，即在类别\( C_k \)出现的条件下，特征\( X \)的概率。

4. **分类决策**：当给定一个新的观测值时，贝叶斯分类器会计算这个值属于每个类别的后验概率\( P(C_k|X) \)。分类器会选择具有最高后验概率的类别作为该观测值的分类。

5. **特征独立假设**：在实际应用中，如朴素贝叶斯分类器，通常会假设所有特征相互独立。这意味着每个特征对分类的贡献是独立的，这大大简化了计算过程，尽管这个假设在现实中往往不成立。

6. **实际应用**：贝叶斯分类器特别适用于维度较高的数据集。由于其概率模型的基础，它对于不完整数据集的处理也比较有效。

**细节分析：**
**Mapper：**
这段代码是一个用于Hadoop MapReduce的自定义Mapper类，名为`BayesMapper`。它继承自`Mapper`类，并重写了`map`方法，它解析和处理每行数据，然后将处理后的结果作为键值对写入上下文，供Reduce阶段使用。特别的对某些列进行了特殊处理（如性别和其他标记的转换）。下面逐行解释这个类的功能和代码。

```java
package com.example;
import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Mapper;
```
这部分是Java文件的包和导入声明。它导入了所需的类，包括Hadoop框架中的类和标准Java类。

```java
public class BayesMapper extends Mapper<Object, Text, IntWritable, MyWritable> {
```
定义了`BayesMapper`类，它继承自Hadoop的`Mapper`类。这里指定了Mapper的输入键和值类型分别为`Object`和`Text`，输出键和值类型为`IntWritable`和自定义的`MyWritable`。

```java
    private IntWritable myKey = new IntWritable();  
    private MyWritable myValue = new MyWritable();
```
在类中声明并初始化了输出键和值的实例。

```java
    @Override  
    protected void map(Object key, Text value, Context context)  
            throws IOException, InterruptedException {
```
重写`map`方法，这是MapReduce作业的核心部分，处理每一行输入数据。

```java
        int[] values = getIntData(value); 
```
调用`getIntData`方法将输入的`Text`类型的值转换为整型数组。

```java
        if(!(values[0]==-1)){
```
检查转换后的数组第一个元素是否为-1，以确认数据有效性。

```java
            int label = values[0];  //存放类别  
            int[] result = new int[values.length-1]; //存放数据  
            for(int i =1;i<values.length;i++){  
                result[i-1] = values[i];
            }  
```
将第一个元素作为标签，其余元素放入新数组`result`中。

```java
            myKey.set(label);  
            myValue.setValue(result);  
            context.write(myKey, myValue);
```
设置输出键和值，并写入上下文。

```
    }  
```
结束if块。

```
    private int[] getIntData(Text value) {  
        String[] values = value.toString().split(",");  
        int[] data = new int[34];
```
`getIntData`方法将Text对象转换为字符串，以逗号分隔，初始化一个长度为34的整型数组。

```java
        Arrays.fill(data, -1); // 初始化数据数组为-1，表示数据无效
        boolean skipRow = false; // 用于标记是否跳过当前行
    
        for (int i = 0; i < 34; i++) {
            if (values[i].isEmpty()) {
                skipRow = true;
                break;  // 如果发现空值，将标记为跳过当前行，并终止循环
            }
        }
```
检查数据中是否存在空值，如果有，则标记为跳过。

```java
        if (!skipRow) {
            // 处理非空数据行的代码
```
只处理非空数据行。

```java
            data[0] = Integer.parseInt(values[0]);
            if (!values[1].isEmpty() && !values[2].isEmpty() && !values[3].isEmpty()) {
                // 如果为M，则为1；如果为F，则为0
                data[1] = values[1].equalsIgnoreCase("M") ? 1 : 0;
                // 如果为Y，则为1；如果为N，则为0
                data[2] = values[2].equalsIgnoreCase("Y") ? 1 : 0;
                data[3] = values[3].equalsIgnoreCase("Y") ? 1 : 0;
            }
            for (int i = 4; i < 34; i++) {
                data[i] = Integer.parseInt(values[i]);  
            }
        }
```
转换字符串值为整数，并处理特定的字段。

```java
        return data;  
    }    
}  
```
返回处理后的整型数组，并结束类定义。

**Reducer：**
Reducer部分代码定义了一个名为`BayesReducer`的类，它继承自Hadoop的`Reducer`类。`BayesReducer`用于在MapReduce作业的Reduce阶段处理数据。`BayesReducer`类实现了贝叶斯分类器的Reduce阶段。它从Mapper接收数据，计算每个类别的属性概率，然后使用这些概率来对测试数据进行分类，计算了分类的准确率，并在任务完成后输出这些统计信息。下面是对代码的逐行解释：
1. **包和导入声明：**
   - 导入了Java和Hadoop的相关类。

2. **类定义：**
   - 定义了`BayesReducer`类，它继承自Hadoop的`Reducer`类。该类的输入键和值类型分别为`IntWritable`和自定义的`MyWritable`，输出键和值类型均为`IntWritable`。

3. **类成员变量：**
   - `testData`：用于存储测试数据的数组列表。
   - `allData`：存储所有数据的数组列表，用于分类的统计。

4. **setup方法：**
   - 在Reduce任务开始前执行，用于初始化配置和读取测试数据。
   - 从Hadoop配置中获取文件路径，并读取测试数据。

5. **reduce方法：**
   - 接收来自Mapper的键值对，进行处理。
   - 对每个键（类别）进行统计，计算每个属性值为1的概率。

6. **cleanup方法：**
   - 在Reduce任务结束后执行。
   - 计算每个类别在训练数据中出现的概率。
   - 对测试数据进行分类，并计算分类的准确率。

7. **getSum方法：**
   - 计算所有训练数据的总数。

8. **getClasify方法：**
   - 根据计算出的概率，对测试数据进行分类。

9. **readTestData方法：**
   - 从文件系统中读取测试数据。
   - 对读入的数据进行解析和预处理。

10. **myString方法：**
    - 将Double数组转换为字符串表示，用于打印或日志记录。
