package br.com.rodriguesaranha.bullyalgorithm;


import br.com.rodriguesaranha.bullyalgorithm.Node;
import br.com.rodriguesaranha.bullyalgorithm.ActualNode;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtil {

    public static final String INPUT_PATH = "src/main/resources/ARQUIVOS - input|output/input/";

    public static void write(String name, byte[] data) {
        try {
            File file = new File("./ARQUIVOS - input|output/output/");
            if(!file.exists()) {
                file.mkdir();
            }
            file = new File("./ARQUIVOS - input|output/ouput/" + name);
            OutputStream os = new FileOutputStream(file);
            os.write(data);
            os.close();
            System.out.println("Saída de processo "+name+" gravada!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ActualNode buildNode(String path, Integer nodeLine) throws IOException {
        try {
            System.out.println(String.format("Iniciando leitura do arquivo de configuração %s para o id %s", path, nodeLine));
            FileReader file = new FileReader(INPUT_PATH + path);
            Scanner scanner = new Scanner(file);
            ActualNode actualNode = new ActualNode();
            List<Node> nodes = new ArrayList<>();
            String line, id, host, port;
            int count=1;

            while (scanner.hasNext()) {
                line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] lineData = line.split(" ");
                    id = lineData[0];
                    host = lineData[1];
                    port = lineData[2];

                    if(count != nodeLine) {
                        Node node = Node.builder()
                                .id(Integer.parseInt(id))
                                .port(Integer.parseInt(port))
                                .address(InetAddress.getByName(host))
                                .isHealthy(false)
                                .isCoordinator(false)
                                .build();
                        nodes.add(node);
                    } else {
                        actualNode = ActualNode.builder()
                                .id(Integer.parseInt(id))
                                .port(Integer.parseInt(port))
                                .address(InetAddress.getByName(host))
                                .isCoordinator(false)
                                .build();
                    }
                }
                count++;
            }
            actualNode.setNodes(nodes);
            System.out.println(String.format("Finalizada leitura do arquivo de configuração %s para o id %s", path, nodeLine));
            return actualNode;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
