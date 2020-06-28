package br.com.rodriguesaranha.bullyalgorithm.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Node {

    private Integer id;
    private InetAddress address;
    private Integer port;
    private boolean isReady;

}
