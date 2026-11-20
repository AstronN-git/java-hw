package hw5;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NioEchoServerTest {
    @Test
    void echoesUtf8LinesInUppercaseForMultipleClients() throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(loopback, 0));
            int port = ((InetSocketAddress) server.getLocalAddress()).getPort();
            FutureTask<Void> running = new FutureTask<>(() -> {
                NioEchoServer.run(server);
                return null;
            });
            Thread thread = Thread.ofPlatform().daemon().start(running);

            try (Socket first = new Socket(loopback, port); Socket second = new Socket(loopback, port)) {
                first.setSoTimeout(3000);
                second.setSoTimeout(3000);
                BufferedReader firstReply = new BufferedReader(new InputStreamReader(first.getInputStream(), UTF_8));
                BufferedReader secondReply = new BufferedReader(new InputStreamReader(second.getInputStream(), UTF_8));

                byte[] russian = "ёж!\n".getBytes(UTF_8);
                first.getOutputStream().write("Привет, мир!\n".getBytes(UTF_8));
                first.getOutputStream().write(russian[0]);
                assertEquals("echo: ПРИВЕТ, МИР!", firstReply.readLine());

                String longLine = "я".repeat(1500);
                second.getOutputStream().write((longLine + "\nstraße").getBytes(UTF_8));
                second.shutdownOutput();
                assertEquals("echo: " + "Я".repeat(1500), secondReply.readLine());
                assertEquals("echo: STRASSE", secondReply.readLine());
                assertNull(secondReply.readLine());

                first.getOutputStream().write(russian, 1, russian.length - 1);
                assertEquals("echo: ЁЖ!", firstReply.readLine());
            } finally {
                thread.interrupt();
                running.get(5, TimeUnit.SECONDS);
            }
        }
    }
}
