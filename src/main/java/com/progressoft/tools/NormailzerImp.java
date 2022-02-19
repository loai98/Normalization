package com.progressoft.tools;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class NormailzerImp implements Normalizer {
    @Override
    public ScoringSummary zscore(Path csvPath, Path destPath, String colToStandardize) {
        FileReader reader = null ;
        FileWriter writer = null ;
        CSVReader csvReader = null ;
        int noOfRows=0, sumOfMarks= 0 ,idx = -1;
        double mean = 0, sd=0;
        ArrayList<String> colValue= new ArrayList<String>();

        try{
            reader = new FileReader(csvPath.toString());
            csvReader = new CSVReader(reader);
            String[] nextRecord = csvReader.readNext();

            // Get column index from col name
            for(int i= 0 ; i < nextRecord.length; i++){
                if(nextRecord[i].equals(colToStandardize)){
                    idx = i ;
                }
            }

            // Get Sumation and number of rows to calculate mean
            while ( (nextRecord =  csvReader.readNext()) != null){
                colValue.add(nextRecord[idx]);
                noOfRows ++;
                sumOfMarks += Double.parseDouble(nextRecord[idx]);
            }

            mean = sumOfMarks /noOfRows ;
            double sum = 0 ;

            // Calculate standard deviation
            for (String item : colValue){
                double  value = Double.parseDouble(item);
                sum += Math.pow(value - mean , 2);
            }
            sd =  Math.sqrt(1.0/noOfRows * sum );


            csvReader.close();
            reader.close();
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        try{
            // open target file to save results
            writer = new FileWriter(destPath.toString());
            reader = new FileReader(csvPath.toString());
            CSVWriter csvWriter = new CSVWriter(writer);
            csvReader = new CSVReader(reader);

            // copy Header ;
            String [] nexRecord = csvReader.readNext();
            String [] finalData = Arrays.copyOf(nexRecord, nexRecord.length+1);
            finalData[nexRecord.length] = colToStandardize+"_z";
            csvWriter.writeNext(finalData,false);

            // Copy file rows
            while ((nexRecord = csvReader.readNext() )!= null){
                finalData = Arrays.copyOf(nexRecord, nexRecord.length+1);
                finalData[nexRecord.length] = String.format( "%.2f" , (Double.parseDouble(nexRecord[idx]) - mean ) / sd );
                csvWriter.writeNext(finalData,false);
            }


            writer.close();

        }catch (Exception ex){
            System.out.println(ex);
        }
        return null;
    }

    @Override
    public ScoringSummary minMaxScaling(Path csvPath, Path destPath, String colToNormalize) {


        // result = (Xi - min(X))  /  ( max(X) - min (x))

        return null;
    }
}
