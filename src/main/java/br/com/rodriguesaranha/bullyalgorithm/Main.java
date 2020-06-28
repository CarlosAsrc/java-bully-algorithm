package br.com.rodriguesaranha.bullyalgorithm;

import br.com.rodriguesaranha.bullyalgorithm.Model.ActualNode;
import br.com.rodriguesaranha.bullyalgorithm.util.FileUtil;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ActualNode actualNode = FileUtil.buildNode("data.txt", 4);
        actualNode.start();
    }
}
