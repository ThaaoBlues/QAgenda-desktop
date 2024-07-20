package com.qsync.qagenda;


// this class is only here because without it the jar building breaks
public class Main_1 {
    public static void main(String[] args){
        try {
            QAgenda.main(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
