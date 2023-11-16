package com.example;
import java.io.IOException;   
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Mapper;  
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class BayesMapper extends Mapper<Object, Text, IntWritable, MyWritable> {  
    Logger log = LoggerFactory.getLogger(BayesMapper.class);  
    private IntWritable myKey = new IntWritable();  
    private MyWritable myValue = new MyWritable();
    @Override  
    protected void map(Object key, Text value, Context context)  
            throws IOException, InterruptedException {  
        log.info("***"+value.toString());  
        int[] values = getIntData(value);  
        int label = values[0];  //存放类别  
        int[] result = new int[values.length-1]; //存放数据  
        for(int i =1;i<values.length;i++){  
            result[i-1] = values[i];
        }  
        myKey.set(label);  
        myValue.setValue(result);  
        context.write(myKey, myValue);  
    }  
    private int[] getIntData(Text value) {  
        String[] values = value.toString().split(",");  
        int[] data = new int[42];
        if (values.length == 56) {
            if (!values[2].equals("")) {
                // 如果第3列为M，则为1；如果为F，则为0
                data[0] = values[2].equalsIgnoreCase("M") ? 1 : 0;
            }
            if (!values[3].equals("")) {
                // 如果第4列为Y，则为1；如果为N，则为0
                data[1] = values[3].equalsIgnoreCase("Y") ? 1 : 0;
            }
            int j=1;
            for(int i=3; i < values.length;i++){
                if(!values[i].equals("")&&values[i].matches("^[0-9]+$")){
                    j++;
                    data[j] = Integer.parseInt(values[i]);
                }  
            }
        }
        
        return data;  
    }
}  