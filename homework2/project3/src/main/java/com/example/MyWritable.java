package com.example;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;
 
public class MyWritable implements Writable{  
    private int[] value;  
    public MyWritable() {  
        
    }  
    public MyWritable(int[] value){  
        this.setValue(value);  
    } 
    public void write(DataOutput out) throws IOException {  
        out.writeInt(value.length);  
        for(int i=0; i<value.length;i++){  
            out.writeInt(value[i]);  
        }  
    }   
    public void readFields(DataInput in) throws IOException {  
        int vLength = in.readInt();  
        value = new int[vLength];  
        for(int i=0; i<vLength;i++){  
            value[i] = in.readInt();  
        }  
    }  
    public int[] getValue() {  
        return value;  
    }  
    public void setValue(int[] value) {  
        this.value = value;  
    }  
}  