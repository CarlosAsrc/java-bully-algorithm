package br.com.rodriguesaranha.bullyalgorithm.Model;

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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActualNode {

    private Integer id;
    private InetAddress address;
    private Integer port;
    private List<Node> nodes;
    private DatagramSocket socket;
    private DatagramPacket receivedPacket;
    private DatagramPacket packetToSend;



    public Node getCoordinator() {
        Node highestIdNode = nodes.stream().max(Comparator.comparing(Node::getId)).get();
        return id > highestIdNode.getId() ? mapActualNodeToNode(this) : highestIdNode;

    }

    private boolean imCoordinador() {
        return id.equals(getCoordinator().getId());
    }


    private Node mapActualNodeToNode(ActualNode actualNode) {
        return Node.builder()
                .id(actualNode.getId())
                .address(actualNode.getAddress())
                .port(actualNode.port)
                .build();
    }


    public void start() throws SocketException, InterruptedException {
        socket = new DatagramSocket(port);
        while (true) {
            if(imCoordinador()) {
                waitForOtherNodesToStart();
                confirmStatusToOthersNodes();
            } else {
                if(!isCoordinatorUp()) {
                    startElection();
                }
            }
            Thread.sleep(3000);
        }
    }

    private void startElection() {
        System.out.println(String.format("Processo %s iniciando eleição.", id));
    }

    private void waitForOtherNodesToStart() {
        byte[] buffer = new byte[8192];
        while (!isAllProcessesReady()){
            System.out.println("Aguardando todos os processos iniciarem..");
            try {
                receivedPacket = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(1000);
                socket.receive(receivedPacket);
                Node node = nodes.stream()
                     .filter(p -> p.getPort() == receivedPacket.getPort())
                     .findFirst()
                     .get();
                node.setReady(true);
                System.out.println(String.format("Processo %s pronto!", node.getId()));
                byte[] bufferToSend = "OK".getBytes();
                this.packetToSend = new DatagramPacket(bufferToSend, bufferToSend.length, receivedPacket.getAddress(), receivedPacket.getPort());
                socket.send(packetToSend);
            } catch (IOException | NoSuchElementException ignored) {}
        }
        System.out.println("Todos os processos prontos! ");
        new CoordinatorTimer(10);
    }

    private boolean isAllProcessesReady() {
        for (Node node : nodes) {
            if (!node.isReady()) return false;
        }
        return true;
    }

    private boolean isCoordinatorUp() {
        try {
            Node coordinator = getCoordinator();
            byte[] sendBuffer = "OK?".getBytes();
            packetToSend = new DatagramPacket(sendBuffer, sendBuffer.length, coordinator.getAddress(), coordinator.getPort());
            socket.send(packetToSend);

            byte[] buffer = new byte[8192];
            receivedPacket = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(1000);
            socket.receive(receivedPacket);

            String coordinatorAnswer = new String(receivedPacket.getData());
            return coordinatorAnswer.equals("OK");
        } catch (IOException e) {
            return false;
        }
    }

    private void confirmStatusToOthersNodes() {
        byte[] buffer = new byte[8192];
        for (int i=1; i<=10; i++){
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
