package ua.ilyxa05;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Engine {

    public static void main(String[] args) throws InterruptedException {
        boolean Debug = true;
        String inputFilename = "http.txt";
        String outputFilename = "valid.txt";
        String url = "https://www.google.com";

        List<String> proxies = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                proxies.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(150); // potoki

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilename, true))) {
            for (String proxyString : proxies) {
                executorService.submit(() -> {
                    String[] parts = proxyString.split(":");
                    if (parts.length == 2) {
                        String host = parts[0];
                        int port = Integer.parseInt(parts[1]);

                        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

                        try {
                            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(proxy);
                            connection.setConnectTimeout(5000);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode == 200) {
                                if (!Debug) {
                                    System.out.println("Proxy " + host + ":" + port + " is working");
                                } else {
                                    System.out.println(host + ":" + port);
                                }
                                synchronized (writer) {
                                    writer.println(proxyString);
                                    writer.flush();
                                }
                            } else {
                                if (!Debug) {
                                    System.out.println("Proxy " + host + ":" + port + " is not working");
                                }
                            }
                        } catch (IOException e) {
                            if (!Debug) {
                                System.out.println("Proxy " + host + ":" + port + " is not working");
                            }
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }
}
