package com.ifedorenko.m2e.mavendev.launch.ui.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

abstract class BuildProgressListenerServer {

  private static final Charset UTF_8 = Charsets.UTF_8;

  private ServerSocket serverSocket;

  private ExecutorService executors = Executors.newFixedThreadPool(10);

  private Thread serverSocketThread = new Thread("m2e.buildListener.socketThread") {
    public void run() {
      while (!serverSocket.isClosed()) {
        try {
          final Socket socket = serverSocket.accept();
          executors.execute(new Runnable() {
            @Override
            public void run() {
              handle(socket);
            }
          });
        } catch (IOException e) {
          if (!serverSocket.isClosed()) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    };
  };

  private void handle(Socket socket) {
    Gson gson = new Gson();
    try (BufferedReader br = newBufferedReader(socket)) {
      String str;
      while ((str = br.readLine()) != null) {
        try {
          @SuppressWarnings("unchecked")
          HashMap<String, Object> data = gson.fromJson(str, HashMap.class);
          onMessage(data);
        } catch (RuntimeException e) {
          // JsonParseException extends RuntimException
          System.err.println(str);
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {}
    }
  }

  private static BufferedReader newBufferedReader(Socket socket) throws IOException {
    return new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
  }

  public void start() throws IOException {
    InetAddress loopback = InetAddress.getLoopbackAddress();
    SocketAddress address = new InetSocketAddress(loopback, 0);
    serverSocket = new ServerSocket();
    serverSocket.setReuseAddress(false);
    serverSocket.bind(address);
    serverSocketThread.start();
  }

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  public void stop() {
    serverSocketThread.interrupt();

    executors.shutdownNow();
    try {
      executors.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      serverSocket.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  protected abstract void onMessage(Map<String, Object> data);
}
