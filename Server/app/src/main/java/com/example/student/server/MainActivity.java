package com.example.student.server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;

import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity {

    private aServer server;
    private TextView txtV1;
    private TextView txtV2;
    private boolean rflag;
    ConnectWeb web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtV1 = findViewById(R.id.txtV1);
        txtV2 = findViewById(R.id.txtV2);

        rflag = true;

        server = new aServer();
        server.start();
    }

    public class aServer extends Thread {

        private int port;
        private ServerSocket serverSocket;
        private boolean flag;


        public aServer() {

            flag = true;
            rflag = true;
            port = 9999;

            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                System.out.println("비정상적으로 종료 되었습니다...Receiver(0)");
            }
        }

        public void run() {
            System.out.println("Server starts...");
            while (flag) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Connected Client..." + socket.getInetAddress());
                    Thread receiver = new Thread(new Receiver(socket));
                    receiver.start();
                } catch (IOException e) {
                    System.out.println("비정상적으로 종료 되었습니다...Server(1)");
                }
            }
        }

    }

    class Receiver implements Runnable {
        private Socket socket;
        private String address;
        private DataInputStream dis;
        private String receiveMessage;

        public Receiver(Socket socket) {
            this.socket = socket;
            address = this.socket.getInetAddress().toString();

            try {
                dis = new DataInputStream(this.socket.getInputStream());
            } catch (IOException e) {
                System.out.println("비정상적으로 종료 되었습니다...Receiver(0)");
            }

        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtV2.setText(address);
                }
            });
            try {
                while (rflag) {
                    receiveMessage = dis.readUTF();
                    if (receiveMessage.equals("q")) {
                        receiveMessage = "Disconnected.." + address;
                        break;
                    } else {
                        web = new ConnectWeb ("http://70.12.114.136/ws/main.do?text="+receiveMessage);
                        web.execute();
                        receiveMessage = address + " : " + receiveMessage;

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtV1.setText(receiveMessage);
                        }
                    });
                }

            } catch(Exception e) {
                System.out.println("비정상적으로 종료 되었습니다...Receiver(1)");

            } finally {
                try {
                    if (dis != null)
                        dis.close();
                } catch (IOException e) {
                    System.out.println("비정상적으로 종료 되었습니다...Receiver(2)");
                }
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    System.out.println("비정상적으로 종료 되었습니다...Receiver(3)");
                }

            }

        }
    }





    class ConnectWeb extends AsyncTask<String,Void,String> {

        String url;

        ConnectWeb() {
        }

        ConnectWeb(String url) {
            this.url = url;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... strings) {

            //http request
            StringBuilder sb = new StringBuilder();
            URL url;
            HttpURLConnection con= null;
           BufferedReader reader = null;

            try{
                url = new URL(this.url);
                con = (HttpURLConnection) url.openConnection();

                if(con!=null){
                    con.setConnectTimeout(5000);   //connection 5초이상 길어지면 exepction
                    //con.setReadTimeout(10000);
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Accept","*/*");
                    if(con.getResponseCode()!=HttpURLConnection.HTTP_OK)
                        return null;



                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line = null;
                    while(true){
                        line = reader.readLine();
                        if(line == null){
                            break;
                        }
                        sb.append(line);
                    }


                }



            }catch(Exception e){
                return e.getMessage();   //리턴하면 post로

            }finally {

                try {
                    if(reader!= null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                con.disconnect();
            }


            return sb.toString();

        }

        @Override
        protected void onPostExecute(String s) {


        }

    }

}
