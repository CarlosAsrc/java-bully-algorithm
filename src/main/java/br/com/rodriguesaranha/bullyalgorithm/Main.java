package br.com.rodriguesaranha.bullyalgorithm;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2){
            System.out.println("Uso: java Main <arquivo-config> <linha>");
            return;
        }
        int process = Integer.parseInt(args[1]);
        FileUtil.write(process, "",false);

        ActualNode actualNode = FileUtil.buildNode(args[0], process);
        actualNode.start();
    }
}
