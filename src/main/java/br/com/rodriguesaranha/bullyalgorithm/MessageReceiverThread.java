//package br.com.rodriguesaranha.bullyalgorithm;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//
//public class MessageReceiverThread {
//
//    private InetAddress address;
//    private Integer port;
//
//    private DatagramSocket socket;
//    private DatagramPacket receivedPacket;
//    private DatagramPacket packetToSend;
//
//
//    private void verifyPossibleCoordinator() {
//
//        while (true) {
//            byte[] buffer = new byte[8192];
//            receivedPacket = new DatagramPacket(buffer, buffer.length);
//            try {
//                socket.setSoTimeout(1000);
//                socket.receive(receivedPacket);
//                String content = new String(receivedPacket.getData());
//                if(content.trim().equals("COORDINATOR")) {
//                    Node node = nodes.stream()
//                            .filter(p -> p.getPort() == receivedPacket.getPort())
//                            .findFirst()
//                            .get();
//                    node.setCoordinator(true);
//                    System.out.println("Processo "+node.getId()+" virou coordenador!");
//                    FileUtil.write(id, "\nc "+node.getId(), true);
//                }
//            } catch (IOException e) {}
//        }
//    }
//
//}
