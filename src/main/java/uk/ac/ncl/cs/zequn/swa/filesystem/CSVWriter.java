package uk.ac.ncl.cs.zequn.swa.filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author ZequnLi
 *         Date: 14-4-26
 */
public class CSVWriter {
    private final String filePath;
    private final String fileName;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    public CSVWriter(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
        init();
    }
    private void init(){
        File file = new File(fileName,fileName);
        if(file.exists()){
            throw new IllegalArgumentException("file already exist");
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.fileWriter = new FileWriter(file);
            this.bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void write(String ...args){
        boolean flag = false;
        try {
            for(String s:args){
                if(!flag){
                    bufferedWriter.write(s);
                    flag = true;
                }else {
                    bufferedWriter.write(","+s);
                }
            }
            bufferedWriter.write("/n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
