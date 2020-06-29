package br.com.rodriguesaranha.bullyalgorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActualNode {

    private Integer id;
    private InetAddress address;
    private Integer port;
    private List<Node> nodes;
    private boolean isCoordinator;
    private String output;
    private DatagramSocket socket;
    private DatagramPacket receivedPacket;
    private DatagramPacket packetToSend;



    public Node getCoordinator() {
        Node highestIdNode = nodes.stream()
                .filter(Node::isCoordinator)
                .findFirst()
                .get();
        return id > highestIdNode.getId() ? mapActualNodeToNode(this) : highestIdNode;
    }



    private Node mapActualNodeToNode(ActualNode actualNode) {
        return Node.builder()
                .id(actualNode.getId())
                .address(actualNode.getAddress())
                .port(actualNode.port)
                .build();
    }


    public void start() throws IOException, InterruptedException {
        socket = new DatagramSocket(port);
        defineCoodinator();

        if(isCoordinator) {
            waitForOtherNodesToStart();
        }

        while (true) {
            if(isCoordinator) {
                //PROCESSO AGE COMO COORDENADOR:
                confirmStatusToOthersNodes();
            } else {
                //PROCESSO AGE NORMALMENTE:
                if(!isCoordinatorUp()) {
                    getCoordinator().setHealthy(false);
                    getCoordinator().setCoordinator(false);
                    startElection();
                    verifyPossibleCoordinator();
                }
            }
            Thread.sleep(3000);
        }
    }

    private void verifyPossibleCoordinator() throws IOException {
        if(isCoordinator) {
            return;
        }

        byte[] buffer = new byte[8192];
        receivedPacket = new DatagramPacket(buffer, buffer.length);
        socket.setSoTimeout(1000);
        socket.receive(receivedPacket);
        String content = new String(receivedPacket.getData());
        if(content.trim().equals("COORDINATOR")) {
            Node node = nodes.stream()
                    .filter(p -> p.getPort() == receivedPacket.getPort())
                    .findFirst()
                    .get();
            node.setCoordinator(true);
            System.out.println("Processo "+node.getId()+" virou coordenador!");
            FileUtil.write(id, "\nc "+node.getId(), true);
        }
    }

    private void defineCoodinator() {
        Node highestIdNode = nodes.stream()
                .max(Comparator.comparing(Node::getId))
                .get();
        if (id > highestIdNode.getId()) {
            isCoordinator = true;
        } else {
            highestIdNode.setCoordinator(true);
        }
    }

    private void startElection() {
        System.out.println(String.format("Processo %s iniciando eleição.", id));
        writeElectionOutput();
        for (Node node: getBiggerNodes()){
            try {
                byte[] sendBuffer = "ELECTION".getBytes();
                packetToSend = new DatagramPacket(sendBuffer, sendBuffer.length, node.getAddress(), node.getPort());
                socket.send(packetToSend);

            } catch (IOException e) {}
        }

        for(int i=1; i<=5; i++) {
            try {
                byte[] buffer = new byte[8192];
                receivedPacket = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(200);
                socket.receive(receivedPacket);

                String coordinatorAnswer = new String(receivedPacket.getData());
                if(coordinatorAnswer.equals("OK")) {
                    System.out.println("Processo "+id+" respondeu OK, eleição perdida");
                    return;
                }
            } catch (IOException e) {}
        }

        setCoordinator(true);
        System.out.println("Processo "+id+" virou coordenador!");
        for (Node node: getBiggerNodes()){
            try {
                byte[] sendBuffer = "COORDINATOR".getBytes();
                packetToSend = new DatagramPacket(sendBuffer, sendBuffer.length, node.getAddress(), node.getPort());
                socket.send(packetToSend);
            } catch (IOException e) {}
        }
    }

    private void writeElectionOutput() {
        StringBuilder nodes = new StringBuilder("\ne [");
        for (Node node: getBiggerNodes()) {
            nodes.append(" ").append(node.getId());
        }
        nodes.append(" ]");
        FileUtil.write(id, nodes.toString(), true);
    }

    private List<Node> getBiggerNodes() {
        return nodes.stream()
                .filter(node -> node.getId() > id)
                .collect(Collectors.toList());
    }

    private void waitForOtherNodesToStart() {
        byte[] buffer = new byte[8192];
        System.out.println("Aguardando todos os processos iniciarem..");
        while (!isAllProcessesReady()){
            try {
                receivedPacket = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(1000);
                socket.receive(receivedPacket);
                Node node = nodes.stream()
                     .filter(p -> p.getPort() == receivedPacket.getPort())
                     .findFirst()
                     .get();
                if(!node.isHealthy()){
                    System.out.println(String.format("Processo %s pronto!", node.getId()));
                    node.setHealthy(true);
                }
                byte[] bufferToSend = "OK".getBytes();
                this.packetToSend = new DatagramPacket(bufferToSend, bufferToSend.length, receivedPacket.getAddress(), receivedPacket.getPort());
                socket.send(packetToSend);
            } catch (IOException | NoSuchElementException ignored) {}
        }
        System.out.println("Todos os processos prontos! ");
    }

    private boolean isAllProcessesReady() {
        for (Node node : nodes) {
            if (!node.isHealthy()) return false;
        }
        return true;
    }

    private boolean isCoordinatorUp() {
        Node coordinator = getCoordinator();
        try {
            byte[] sendBuffer = "OK?".getBytes();
            packetToSend = new DatagramPacket(sendBuffer, sendBuffer.length, coordinator.getAddress(), coordinator.getPort());
            socket.send(packetToSend);

            byte[] buffer = new byte[8192];
            receivedPacket = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(1000);
            socket.receive(receivedPacket);

            String coordinatorAnswer = new String(receivedPacket.getData());
            System.out.println("HEALTH CHECK DO COORDENADOR: "+coordinatorAnswer);
            return coordinatorAnswer.trim().equals("OK");
        } catch (IOException e) {
            FileUtil.write(id, "t "+coordinator.getId(), true);
            return false;
        }
    }

    private void confirmStatusToOthersNodes() {
        new CoordinatorTimer(10);
        byte[] buffer = new byte[8192];
        while (true) {
            try {
                receivedPacket = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(100);
                socket.receive(receivedPacket);

                byte[] bufferToSend = "OK".getBytes();
                this.packetToSend = new DatagramPacket(bufferToSend, bufferToSend.length, receivedPacket.getAddress(), receivedPacket.getPort());
                socket.send(packetToSend);
            } catch (IOException | NoSuchElementException ignored) {}
        }
    }
}
