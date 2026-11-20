package hw5;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Адаптация 07_NioEchoServer.java из https://gitverse.ru/kataus/hse_samples. */
public final class NioEchoServer {
    private NioEchoServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 9001 : Integer.parseInt(args[0]);
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
            System.out.println("NIO echo server listening on " + server.getLocalAddress());
            run(server);
        }
    }

    static void run(ServerSocketChannel server) throws IOException {
        try (Selector selector = Selector.open()) {
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    selector.select();
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (key.isAcceptable()) {
                            acceptClient(server, selector);
                        } else {
                            Client client = (Client) key.attachment();
                            try {
                                if (key.isReadable()) {
                                    client.read(key, buffer);
                                } else if (key.isWritable()) {
                                    client.write(key);
                                }
                            } catch (IOException e) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } finally {
                for (SelectionKey key : selector.keys()) {
                    if (key.channel() != server) {
                        key.channel().close();
                    }
                }
            }
        }
    }

    private static void acceptClient(ServerSocketChannel server, Selector selector) throws IOException {
        SocketChannel client = server.accept();
        if (client != null) {
            try {
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ, new Client());
            } catch (IOException e) {
                client.close();
                throw e;
            }
        }
    }

    private static final class Client {
        private final ByteArrayOutputStream line = new ByteArrayOutputStream();
        private ByteBuffer response;
        private boolean inputClosed;

        private void read(SelectionKey key, ByteBuffer buffer) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            buffer.clear();
            inputClosed = channel.read(buffer) == -1;
            buffer.flip();

            // Собираем строку целиком: TCP может разделить даже один символ UTF-8.
            StringBuilder reply = new StringBuilder();
            while (buffer.hasRemaining()) {
                byte value = buffer.get();
                line.write(value);
                if (value == '\n') {
                    appendLine(reply);
                }
            }
            if (inputClosed && line.size() > 0) {
                appendLine(reply);
            }

            if (!reply.isEmpty()) {
                response = UTF_8.encode(reply.toString());
                key.interestOps(SelectionKey.OP_WRITE);
            } else if (inputClosed) {
                channel.close();
            }
        }

        private void appendLine(StringBuilder reply) {
            String text = line.toString(UTF_8);
            reply.append("echo: ").append(text.toUpperCase(Locale.ROOT));
            line.reset();
        }

        private void write(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(response);
            if (!response.hasRemaining()) {
                response = null;
                if (inputClosed) {
                    channel.close();
                } else {
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }
}
