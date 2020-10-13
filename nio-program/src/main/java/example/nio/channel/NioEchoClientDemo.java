package example.nio.channel;

import java.io.IOException;

public class NioEchoClientDemo {

    public static void main(String[] args) throws IOException, InterruptedException {
        Process server = NioEchoServer.start();
        NioEchoClient client = NioEchoClient.start();

        System.out.println(client.sendMessage("hello"));

        client.stop();
        server.destroy();
    }
}
