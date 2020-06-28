package br.com.rodriguesaranha.bullyalgorithm;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
//        if (args.length != 2){
//            System.out.println("Uso: java Main <arquivo-config> <linha>");
//            return;
//        }
        ActualNode actualNode = FileUtil.buildNode("data.txt", 4);
        actualNode.start();
    }
}
