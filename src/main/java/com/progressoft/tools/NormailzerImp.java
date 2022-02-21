package com.progressoft.tools;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NormailzerImp implements Normalizer {
    @Override
    public ScoringSummary zscore(Path csvPath, Path destPath, String colToStandardize)  {
        FileReader reader = null ;
        FileWriter writer = null ;
        CSVReader csvReader = null ;
        int noOfRows=0 ,idx = -1;
        double mean = 0, sd=0, sumOfValues= 0 ,
                variance = 0,min=0,
                max=0,median =0;
        ArrayList<Double> colValue= new ArrayList<Double>();

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

            // initialize min and max values
            if ( (nextRecord =  csvReader.readNext()) != null){
                double  value = Double.parseDouble(nextRecord[idx]);
                min = max = value ;
                colValue.add(value);
                sumOfValues += value;
                noOfRows ++ ;
            }


            // Get Sumation and number of rows to calculate mean
            while ( (nextRecord =  csvReader.readNext()) != null){
                double value = Double.parseDouble(nextRecord[idx]);
                if(value > max){
                    max = value;
                }
                if (value < min){
                    min = value ;
                }
                colValue.add(value);
                sumOfValues +=value;
                noOfRows ++ ;

            }
      //      noOfRows = colValue.size();
            mean = sumOfValues /noOfRows ;
            double sum = 0 ;
            // Calculate standard deviation
            for (Double item : colValue){
                sum += Math.pow(item - mean , 2);
            }
            variance = sum / noOfRows ;
            sd =  Math.sqrt(variance );

            // Get median value ;
            Collections.sort(colValue);
            if(noOfRows % 2 == 0){
                median = ( colValue.get(noOfRows/2)+ colValue.get(noOfRows/2 -1))/ 2.0;
            }else{
                median = colValue.get(noOfRows/2) ;
            }
            csvReader.close();
            reader.close();
        }
        catch (ArrayIndexOutOfBoundsException ex){
            System.out.print("Invalid column name");
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
            String [] nexRecord = csvReader.readNext() ;
            ArrayList<String> finalData =new ArrayList<String >(List.of(nexRecord));
            finalData.add(idx +1, colToStandardize+"_z");
            csvWriter.writeNext(finalData.toArray(nexRecord),false);

            // Copy file rows
            while ((nexRecord = csvReader.readNext() )!= null){
                finalData = new ArrayList<String >(List.of(nexRecord));
                finalData.add(idx + 1,String.valueOf(new BigDecimal((Double.parseDouble(nexRecord[idx]) - mean ) / sd )
                        .setScale(2,BigDecimal.ROUND_HALF_EVEN )) );

                csvWriter.writeNext(finalData.toArray(nexRecord),false);
            }


            writer.close();

        }catch (Exception ex){
            System.out.println(ex);
        }

        double  finalMean = mean, finalSd = sd, finalVariance = variance ,
                finalMin = min, finalMax = max, finalMedian = median;
        ScoringSummary scoringSummary = new ScoringSummary() {
            @Override
            public BigDecimal mean() {
                return new BigDecimal(finalMean).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal standardDeviation() {
                return new BigDecimal(finalSd).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            }

            @Override
            public BigDecimal variance() {
                return new BigDecimal(finalVariance).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal median() {
                return new BigDecimal(finalMedian).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal min() {
                return new BigDecimal(finalMin).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal max() {
                return new BigDecimal(finalMax).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }
        };

        return scoringSummary;
    }

    @Override
    public ScoringSummary minMaxScaling(Path csvPath, Path destPath, String colToNormalize) {
        FileReader reader = null ;
        FileWriter writer = null ;
        CSVReader csvReader = null ;
        int idx = -1,noOfRows=0;
        double min =0, max=0 , median =0,
         mean =0, sum = 0, sd=0 , variance = 0 ;
        ArrayList<Double> colValue= new ArrayList<Double>();

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
            if ( (nextRecord =  csvReader.readNext()) != null) {
                double value = Double.parseDouble(nextRecord[idx]);
                min = max = value;
                sum += value;
                noOfRows++;
                colValue.add(value);
            }


            // get Min(x) and Max(x) value
            while ( (nextRecord =  csvReader.readNext()) != null){
                double value = Double.parseDouble(nextRecord[idx]);
                if(value > max){
                    max = value;
                }
                if (value < min){
                    min = value ;
                }
                colValue.add(value);
                sum +=value ;
                noOfRows ++ ;
            }
            mean = sum/noOfRows ;
            sum=0;
            for (Double item : colValue){
                sum += Math.pow(item - mean , 2);
            }
            variance = sum/ noOfRows ;
            sd =  Math.sqrt(variance );

            // Get median value ;
            Collections.sort(colValue);
            if(noOfRows % 2 == 0){
                median = (colValue.get(noOfRows/2) + colValue.get(noOfRows/2 -1)) / 2.0;
            }else{
                median =colValue.get(noOfRows/2);
            }

        }catch (ArrayIndexOutOfBoundsException ex){
            System.out.print("Invalid column name");
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
            String [] nexRecord = csvReader.readNext() ;
            ArrayList<String> finalData =new ArrayList<String >(List.of(nexRecord));
            finalData.add(idx+1, colToNormalize+"_mm");
            csvWriter.writeNext(finalData.toArray(nexRecord),false);

            // Copy file rows
            while ((nexRecord = csvReader.readNext() )!= null){
                double value = Double.parseDouble(nexRecord[idx]) ;
                finalData = new ArrayList<String >(List.of(nexRecord));
                finalData.add(idx+1,String.valueOf(new BigDecimal(( value - min ) / (max - min))
                        .setScale(2,BigDecimal.ROUND_HALF_EVEN )) );
                csvWriter.writeNext(finalData.toArray(nexRecord),false);
            }

            writer.close();

        }catch (Exception ex){
            System.out.println(ex);
        }

        double finalMean = mean, finalSd = sd,
                finalMin = min, finalMax = max,
                finalVariance = variance,
                finalMedian = median;
        return new ScoringSummary() {
            @Override
            public BigDecimal mean() {
                return new BigDecimal(finalMean).setScale(2,BigDecimal.ROUND_HALF_EVEN );

            }

            @Override
            public BigDecimal standardDeviation() {
                return new BigDecimal(finalSd).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            }

            @Override
            public BigDecimal variance() {
                return new BigDecimal(finalVariance).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal median() {
                return new BigDecimal(finalMedian).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal min() {
                return new BigDecimal(finalMin).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }

            @Override
            public BigDecimal max() {
               return new BigDecimal(finalMax).setScale(2,BigDecimal.ROUND_HALF_EVEN );
            }
        };
    }
}
