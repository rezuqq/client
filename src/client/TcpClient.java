/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 *
 * @author admin
 */
public class TcpClient {

    public static void main(String[] args) throws Exception {

        // Подключаемся к серверу
        Socket socket = new Socket("127.0.0.1", 6000);
        System.out.println("Connected to TCP server");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );
        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
        );

        // Регистрируемся
        out.println("HELLO");
        System.out.println("Client registered on server");

        while (true) {

            // Ждём задачу
            String msg = in.readLine();
            if (msg == null) {
                System.out.println("Server closed connection");
                break;
            }

            System.out.println("Received task: " + msg);

            if (!msg.startsWith("TASK;"))
                continue;

            double a = 0, b = 0, h = 0;

            String[] parts = msg.split(";");
            for (String p : parts) {
                if (p.startsWith("a=")) a = Double.parseDouble(p.substring(2));
                if (p.startsWith("b=")) b = Double.parseDouble(p.substring(2));
                if (p.startsWith("h=")) h = Double.parseDouble(p.substring(2));
            }

            try {
                double result = calculateIntegralMultithread(a, b, h);
                out.println("RESULT;value=" + result);
                System.out.println("Sent result: " + result);
            } catch (Exception e) {
                out.println("RESULT;error=" + e.getMessage());
                System.out.println("Sent error: " + e.getMessage());
            }
        }

        socket.close();
    }

    private static double calculateIntegralMultithread(double a, double b, double h) throws Exception {
        int threadsCount = 2;
        java.util.concurrent.ExecutorService executor =
                java.util.concurrent.Executors.newFixedThreadPool(threadsCount);

        java.util.concurrent.Future<Double>[] futures = new java.util.concurrent.Future[threadsCount];

        double interval = (b - a) / threadsCount;

        for (int i = 0; i < threadsCount; i++) {
            double start = a + i * interval;
            double end = (i == threadsCount - 1) ? b : start + interval;

            IntegralTask task = new IntegralTask(start, end, h);
            futures[i] = executor.submit(task);
        }

        double total = 0;
        for (java.util.concurrent.Future<Double> f : futures) {
            total += f.get();
        }

        executor.shutdown();
        return total;
    }

}
