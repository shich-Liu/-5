package com.example;
import java.io.BufferedReader;
import java.io.IOException;  
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;  
import org.apache.hadoop.conf.Configuration;  
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;  
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;  
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BayesReducer extends Reducer<IntWritable, MyWritable, IntWritable, IntWritable>{  
    Logger log = LoggerFactory.getLogger(BayesReducer.class);  
    private String testFilePath;  
    // 测试数据  
    private ArrayList<int[]> testData = new ArrayList<>();  
    // 保存相同k的所有数据  
    private ArrayList<CountAll> allData = new ArrayList<>();  
    @Override  
    protected void setup(Context context)  
            throws IOException, InterruptedException {  
        Configuration conf = context.getConfiguration();
        testFilePath = conf.get("/Lsc/homework2/input/application_data2.csv");  
        Path path = new Path("/Lsc/homework2/input/application_data2.csv");  
        FileSystem fs = path.getFileSystem(conf);  
        readTestData(fs,path);  
    }  
    @Override  
    protected void reduce(IntWritable key, Iterable<MyWritable> values,  
            Context context)  
            throws IOException, InterruptedException {  
        Double[] myTest = new Double[testData.get(0).length-1];  
        for(int i=0;i<myTest.length;i++){  
            myTest[i] = 1.0;  
        }  
        Long sum = 2L;  
        // 计算每个类别中，每个属性值为1的个数  
        for (MyWritable myWritable : values) {  
            int[] myvalue = myWritable.getValue();  
            for(int i=0; i < myvalue.length;i++){  
                myTest[i] += myvalue[i];  
            }  
            sum += 1;  
        }  
        for(int i=0;i<myTest.length;i++){  
            myTest[i] = myTest[i]/sum;  
        }  
        allData.add(new CountAll(sum,myTest,key.get()));  
    }  
    private IntWritable myKey = new IntWritable();  
    private IntWritable myValue = new IntWritable();  
      
    protected void cleanup(Context context)  
            throws IOException, InterruptedException {  
        // 保存每个类别的在训练数据中出现的概率  
        // k,v  0,0.4  
        // k,v  1,0.6  
        HashMap<Integer, Double> labelG = new HashMap<>();  
        Long allSum = getSum(allData); //计算训练数据的长度  
        for(int i=0; i<allData.size();i++){  
            labelG.put(allData.get(i).getK(),   
                    Double.parseDouble(allData.get(i).getSum().toString())/allSum);  
        }  
        //test的长度 要比训练数据中的长度大1  
        int sum = 0;  
        int yes = 0;  
        for(int[] test: testData){  
            int value = getClasify(test, labelG);  
            if(test[0] == value){  
                yes += 1;  
            }  
            sum +=1;  
            myKey.set(test[0]);  
            myValue.set(value);  
            context.write(myKey, myValue);  
        }  
        System.out.println("正确率为："+(double)yes/sum);  
    }  
    /*** 
     * 求得所有训练数据的条数 
     * @param allData2 
     * @return 
     */  
    private Long getSum(ArrayList<CountAll> allData2) {  
        Long allSum = 0L;  
        for (CountAll countAll : allData2) {  
            log.info("类别："+countAll.getK()+"数据："+myString(countAll.getValue())+"总数："+countAll.getSum());  
            allSum += countAll.getSum();  
        }  
        return allSum;  
    }  
    /*** 
     * 得到分类的结果 
     * @param test 
     * @param labelG 
     * @return 
     */  
    private int getClasify(int[] test,HashMap<Integer, Double> labelG ) {  
        double[] result = new double[allData.size()]; //以类别的长度作为数组的长度  
        for(int i = 0; i<allData.size();i++){  
            double count = 0.0;  
            CountAll ca = allData.get(i);  
            Double[] pdata = ca.getValue();  
            for(int j=1;j<test.length;j++){  
                if(test[j] == 1){  
                    // 在该类别中，相同位置上的元素的值出现1的概率  
                    count += Math.log(pdata[j-1]);   
                }else{  
                    count += Math.log(1- pdata[j-1]);   
                }  
                log.info("count: "+count);  
            }  
            count += Math.log(labelG.get(ca.getK()));  
            result[i] = count;  
        }   
        if(result[0] > result[1]){  
            return 0;  
        }else{  
            return 1;  
        }  
    }  
    /*** 
     * 读取测试数据 
     * @param fs 
     * @param path 
     * @throws NumberFormatException 
     * @throws IOException 
     */  
    private void readTestData(FileSystem fs, Path path) throws NumberFormatException, IOException {  
        FSDataInputStream data = fs.open(path);  
        BufferedReader bf = new BufferedReader(new InputStreamReader(data));  
        String line = "";  
        while ((line = bf.readLine()) != null) {  
            String[] str = line.split(",");  
            int[] myData = new int[str.length];  
            for(int i=0;i<str.length;i++){
            	if(str[i]!=""||!str[i].equals(""))
            		if(str[i].matches("^[0-9]+$"))
                myData[i] = Integer.parseInt(str[i]);  
            }  
            testData.add(myData);  
        }  
        bf.close();  
        data.close();  
          
    }  
    public static String myString(Double[] arr){  
        String num = "";  
        for(int i=0;i<arr.length;i++){  
            if(i==arr.length-1){  
                num += String.valueOf(arr[i]);  
            }else{  
                num += String.valueOf(arr[i])+',';  
            }  
        }  
        return num;  
    }  
}  