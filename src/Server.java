import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private final ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean close;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        close = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(6969);
            pool = Executors.newCachedThreadPool();

            while (!close) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutDown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch: connections) {
            ch.sendMessage(message);
        }
    }

    public void shutDown() {
        close = true;
        try {
            pool.shutdown();
            if (!server.isClosed()) server.close();

            for (ConnectionHandler ch: connections) {
                ch.shutDown();
            }
        } catch (IOException e) {
            //nothing here
        }
    }

    class ConnectionHandler implements Runnable{
        private final Socket client;
        private PrintWriter out;
        private BufferedReader in;

        ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Enter an username: ");
                String username = in.readLine();
                if (username != null) {
                    System.out.println(username + " connected");
                    broadcast(username + " just joined the chat");
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/quit")) {
                        System.out.println(username + " left");
                        broadcast(username + " left the chat");
                        shutDown();
                    } else {
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutDown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutDown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) client.close();
            } catch (IOException e) {
                // nothing here
            }
        }
    }
}
