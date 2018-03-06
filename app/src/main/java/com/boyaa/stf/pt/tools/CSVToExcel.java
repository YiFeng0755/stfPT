package com.boyaa.stf.pt.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by GuixiangGui on 2017/2/20.
 */

public class CSVToExcel {
    private String xlsFilePath = null;
    private String csvFilePath = null;

    public CSVToExcel(String csvFilePath){
        this.csvFilePath = csvFilePath;
        File csvFile = new File(csvFilePath);
        if(csvFile.exists()){
            xlsFilePath = csvFilePath.substring(0, csvFilePath.lastIndexOf("."))+".xls";
        }
    }

    public void converToXls(){

    }

    private ArrayList<ArrayList<String>> readCsvFile(String filePath){
        ArrayList<ArrayList<String>> dataList = new ArrayList();
        ArrayList<String> ar = null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(filePath);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);

            String line = null;
            while(null != (line = br.readLine())){
                ar = new ArrayList<String>();
                String[] strar = line.split(",");
                for(int i = 0;i<strar.length;i++){
                    ar.add(strar[i]);
                }
                dataList.add(ar);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != fis){
                    fis.close();
                }
                if(null != isr){
                    isr.close();
                }
                if(null != br){
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        return dataList;
    }

  /*  private void createXls(Stirng filePath){

    }*/

}
