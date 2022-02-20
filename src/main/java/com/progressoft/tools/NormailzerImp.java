package com.progressoft.tools;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class NormailzerImp implements Normalizer {
    @Override
    public ScoringSummary zscore(Path csvPath, Path destPath, String colToStandardize)  {
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
                sumOfMarks += Double.parseDouble(nextRecord[idx]);
                noOfRows ++ ;

            }
      //      noOfRows = colValue.size();
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
                finalData[nexRecord.length] =
                        String.valueOf(new BigDecimal((Double.parseDouble(nexRecord[idx]) - mean ) / sd )
                                .setScale(2,BigDecimal.ROUND_HALF_EVEN ));
                csvWriter.writeNext(finalData,false);
            }


            writer.close();

        }catch (Exception ex){
            System.out.println(ex);
        }

        final  double  finalMean = mean;

        double finalSd = sd;
        ScoringSummary scoringSummary = new ScoringSummary() {
            @Override
            public BigDecimal mean() {
                return new BigDecimal(finalMean).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal standardDeviation() {
                return new BigDecimal(finalSd).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal variance() {
                return null;
            }

            @Override
            public BigDecimal median() {
                return null;
            }

            @Override
            public BigDecimal min() {
                return null;
            }

            @Override
            public BigDecimal max() {
                return null;
            }
        };

        return scoringSummary;
    }

    @Override
    public ScoringSummary minMaxScaling(Path csvPath, Path destPath, String colToNormalize) {
        FileReader reader = null ;
        FileWriter writer = null ;
        CSVReader csvReader = null ;
        int idx = -1,nOfRows=0;
        double min =0, max=0 ,
         mean =0, sum = 0, sd=0;
        ArrayList<String> colValue= new ArrayList<String>();



        try{
            reader = new FileReader(csvPath.toString());
            csvReader = new CSVReader(reader);
            String[] nextRecord = csvReader.readNext();

            for(int i= 0 ; i < nextRecord.length; i++){
                if(nextRecord[i].equals(colToNormalize)){
                    idx = i ;
                }
            }
            // Initialize min and max values ;

            nextRecord = csvReader.readNext();
            min = max = Double.parseDouble( nextRecord[idx] );
            sum +=max ;
            nOfRows++ ;
            colValue.add(nextRecord[idx]);


            // get Min(x) and Max(x) value
            while ( (nextRecord =  csvReader.readNext()) != null){
                double value = Double.parseDouble(nextRecord[idx]);
                if(value > max){
                    max = value;
                }else if (value < min){
                    min = value ;
                }
                colValue.add(nextRecord[idx]);
                sum +=value ;
                nOfRows ++ ;
            }
            mean = sum/nOfRows ;

            sum=0;
            for (String item : colValue){
                double  value = Double.parseDouble(item);
                sum += Math.pow(value - mean , 2);
            }
            sd =  Math.sqrt(1.0/nOfRows * sum );

        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        try{
            writer = new FileWriter(destPath.toString());
            reader = new FileReader(csvPath.toString());
            CSVWriter csvWriter = new CSVWriter(writer);
            csvReader = new CSVReader(reader);

            // copy Header ;

            String [] nexRecord = csvReader.readNext();
            String [] finalData = Arrays.copyOf(nexRecord, nexRecord.length+1);
            finalData[nexRecord.length] = colToNormalize + "_mm";
            csvWriter.writeNext(finalData,false);

            while ((nexRecord = csvReader.readNext() )!= null){
                finalData = Arrays.copyOf(nexRecord, nexRecord.length+1);
                double value = Double.parseDouble(nexRecord[idx]) ;
                finalData[nexRecord.length] =
                        String.valueOf(new BigDecimal(( value - min ) / (max - min))
                                .setScale(2,BigDecimal.ROUND_HALF_EVEN ));
                csvWriter.writeNext(finalData,false);

            }

            writer.close();

        }catch (Exception ex){
            System.out.println(ex);
        }

        double finalMean = mean;
        double finalSd = sd;
        return new ScoringSummary() {
            @Override
            public BigDecimal mean() {
                return new BigDecimal(finalMean).setScale(2,BigDecimal.ROUND_HALF_EVEN );

            }

            @Override
            public BigDecimal standardDeviation() {
                return new BigDecimal(finalSd).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal variance() {
                return null;
            }

            @Override
            public BigDecimal median() {
                return null;
            }

            @Override
            public BigDecimal min() {
                return null;
            }

            @Override
            public BigDecimal max() {
                return null;
            }
        };
    }
}
