package com.example.student.serverapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    TextView textView,textView2;
    EditText editText;
    Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.textView);
        textView2=findViewById(R.id.textView2);
        editText=findViewById(R.id.editText);

        try {
            server=new Server();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void click(View v){
        String str=editText.getText().toString();
        server.sendAll(str);
    }


    public void setSpeed(final String msg){ //스피드 받아오는거
        Runnable r=new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(msg);
                    }
                });
            }
        };
        new Thread(r).start();
    }
    public void setConnect(final String client, final String msg){ //커넥트받아오는거
        Runnable r=new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(msg.equals("t")) {
                            textView2.setText(client+"Client Connected");
                        }else{
                            textView2.setText("Client DisConnected");
                        }
                    }
                });
            }
        };
        new Thread(r).start();
    }

    //ServerSocket Start..................................

    public class Server extends Thread {

        ServerSocket serverSocket;
        boolean flag = true;
        boolean rflag = true;
        HashMap<String, DataOutputStream> map = new HashMap<>();

        public Server() throws IOException {
            // Create ServerSocket ...
            serverSocket = new ServerSocket(8888);
            System.out.println("Ready Server...");
        }

        @Override
        public void run() {
            // Accept Client Connection ...
            try {
                while (flag) {
                    System.out.println("Ready Accept");
                    Socket socket = serverSocket.accept();
                    String client =socket.getInetAddress().getHostAddress();
                    setConnect(client,"t");
                    new Receiver(socket).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        class Receiver extends Thread {

            InputStream in;
            DataInputStream din;
            OutputStream out;
            DataOutputStream dout;
            Socket socket;
            String ip;

            public Receiver(Socket socket) {
                try {
                    this.socket = socket;
                    in = socket.getInputStream();
                    din = new DataInputStream(in);
                    out = socket.getOutputStream();
                    dout = new DataOutputStream(out);
                    ip = socket.getInetAddress().toString();
                    map.put(ip, dout);
                    System.out.println("Connected Count:" + map.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // end Recevier

            @Override
            public void run() {
                try {
                    while (rflag) {

                        if (socket.isConnected() && din != null && din.available() > 0) {
                            String str = din.readUTF();
                            setSpeed(str);
                            SendHttp sendHttp = new SendHttp(str);
                            sendHttp.execute();

                            /* sendAll("["+ip+"]"+str);*/ //보내준거 받아서 화면에 띄우기만 하면되니까 필요 없음
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setConnect(null,"f");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (dout != null) {
                        try {
                            dout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (din != null) {
                        try {
                            din.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        public void sendAll(String msg) { //서버에서 클라이언트로
            Sender sender = new Sender();
            sender.setMeg(msg);
            sender.start();
        }

        // Send Message All Clients
        class Sender extends Thread {

            String msg;

            public void setMeg(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                try {
                    Set<String> keys = map.keySet();
                    Iterator<String> it = keys.iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        map.get(key).writeUTF(msg);
                        System.out.println(msg);
                    }

                } catch (Exception e) {
                }
            }
        }
        public void stopServer() {
            rflag=false;
        }
    }



  class SendHttp extends AsyncTask<Void,Void,Void>{

        String surl = "http://70.12.114.136/ws/main.do?speed=";
        URL url;
        HttpURLConnection urlConn;
        String speed;

        public SendHttp(){

        }

      public SendHttp(String speed){

            this.speed = speed;
            surl += speed;

          try {
              url = new URL(surl);
          } catch (MalformedURLException e) {
              e.printStackTrace();
          }
      }



      @Override
      protected Void doInBackground(Void... voids) {
          try {
              urlConn = (HttpURLConnection) url.openConnection();
              urlConn.getResponseCode();

          } catch (IOException e) {
              e.printStackTrace();
          }
          return null;
      }
  }


}