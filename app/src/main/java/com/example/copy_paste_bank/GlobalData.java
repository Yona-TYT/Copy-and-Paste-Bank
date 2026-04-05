package com.example.copy_paste_bank;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

public class GlobalData {

    private static GlobalData instance;
    private final Context applicationContext;

    // Variables globales

    public List<String> spinTasa = Arrays.asList("BCV", "Promedio", "Paralelo", "Valor Personalizado");
    public int optTasa = 0;
    public double tasaDolar = 0.0;
    public double sendValue = 0.0;

    public Double[][] listCalc = {
            new Double[] {0.0, 0.0, 0.0},
            new Double[] {0.0, 0.0, 0.0},
            new Double[] {0.0, 0.0, 0.0}
    };

    public int optCalc = 0;


    private GlobalData(Context context) {
        this.applicationContext = context.getApplicationContext(); // Garantizamos ApplicationContext
    }

    /**
     * Método seguro para obtener la instancia
     */
    public static GlobalData getInstance(Context context) {
        if (instance == null) {
            synchronized (GlobalData.class) {
                if (instance == null) {
                    if (context == null) {
                        throw new IllegalArgumentException("Context cannot be null when initializing AppData");
                    }
                    instance = new GlobalData(context);
                }
            }
        }
        return instance;
    }

    /**
     * Método recomendado para usar desde Application
     */
    public static void initialize(Context context) {
        if (instance == null) {
            instance = new GlobalData(context);
        }
    }

    public void setTasaDolar(double tasa) {
        this.tasaDolar = tasa;
    }
    public double getTasaDolar() {
        return this.tasaDolar;
    }

    public void setSendValue(double value){
        this.sendValue = value;
    }

    public double getSendValue() {
        return this.sendValue;
    }

    public void setOptTasa(int opt) {
        this.optTasa = opt;
    }

    public int getOptTasa() {
        return this.optTasa;
    }



    public List<String> getSpinTasa(){
        return this.spinTasa;
    }

    public void setOptCalc(int opt) {
        this.optCalc = opt;
    }

    public int getOptCalc() {
        return this.optCalc;
    }

    public void setListCalc(Double[] mList, int opt) {
        if (opt < this.listCalc.length){
            this.listCalc[opt] = mList;
        }
    }

    public Double[] getListCalc(int opt) {
        if (opt < this.listCalc.length){
            return this.listCalc[opt];
        }
        return new Double[] {(double)0, (double)0, (double)0};
    }
    public void cleanListCalc() {
        this.listCalc = new Double[][]{
                new Double[]{(double) 0, (double) 0, (double) 0},
                new Double[]{(double) 0, (double) 0, (double) 0},
                new Double[]{(double) 0, (double) 0, (double) 0}
        };
    }
}
