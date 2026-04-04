package org.openmarkov.learning.metric.cmi.accuracy;

import org.openmarkov.core.io.database.CaseDatabase;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Dataset {



    private int[][][] training;
    private int[][][] test;
    private int[][] cases;
    private int sampleSize;
    private int numOfSamples;


    public Dataset(CaseDatabase cdb, int sampleFraction){
        cases=cdb.getCases();
        numOfSamples = sampleFraction;
        training = new int[sampleFraction][][];
        test = new int[sampleFraction][][];
        sampleSize=cases.length/sampleFraction;
        initializeDatasets();
    }


    private void initializeDatasets(){

        IntStream.range(0, numOfSamples).forEach(it ->{

            int maxCase = new Random().nextInt(cases.length-sampleSize-1);
            test[it]= Arrays.copyOfRange(cases, maxCase, maxCase+sampleSize);
            training[it] = new int[cases.length-sampleSize][];

            for(int i = 0; i<maxCase;i++){
                training[it][i]=cases[i];
            }
            for(int i=maxCase+sampleSize; i<cases.length;i++){
                training[it][i-sampleSize]=cases[i];
            }
        });
    }


    public int[][][] getTraining() {
        return training;
    }

    public int[][][] getTest() {
        return test;
    }

    public boolean isEmpty(){
        return  this.cases==null ||this.test==null || this.training==null
                ||this.cases.length==0 || this.test.length==0 || this.training.length ==0;
    }

}
