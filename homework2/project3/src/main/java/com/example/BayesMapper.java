package com.example;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Mapper;
 
public class BayesMapper extends Mapper<Object, Text, IntWritable, MyWritable> {
    private IntWritable myKey = new IntWritable();  
    private MyWritable myValue = new MyWritable();
    @Override  
    protected void map(Object key, Text value, Context context)  
            throws IOException, InterruptedException {
        int[] values = getIntData(value); 
        if(!(values[0]==-1)){
            int label = values[0];  //存放类别  
            int[] result = new int[values.length-1]; //存放数据  
            for(int i =1;i<values.length;i++){  
                result[i-1] = values[i];
            }  
            myKey.set(label);  
            myValue.setValue(result);  
            context.write(myKey, myValue);
        }
    }  
    private int[] getIntData(Text value) {  
        String[] values = value.toString().split(",");  
        int[] data = new int[34];
        Arrays.fill(data, -1); // 初始化数据数组为-1，表示数据无效
        boolean skipRow = false; // 用于标记是否跳过当前行
    
        for (int i = 0; i < 34; i++) {
            if (values[i].isEmpty()) {
                skipRow = true;
                break;  // 如果发现空值，将标记为跳过当前行，并终止循环
            }
        }
        if (!skipRow) {
            // 处理非空数据行的代码
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
        return data;  
    }    
}  