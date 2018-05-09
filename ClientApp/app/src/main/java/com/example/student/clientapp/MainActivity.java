package com.example.student.clientapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    EditText speed;
    ImageView imageView;
    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        speed = findViewById(R.id.speed);
        imageView = findViewById(R.id.imageView);
        client = new Client();
        client.start();


    }

    public void click(View v) {
        String msg = speed.getText().toString();

        //버튼 클릭하면 서버로 메세지 보내기
        client.sendMsg(msg);
    }


    //앱 종료시 실행
    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.stopClient();
    }

    public class Client extends Thread {

        boolean flag = true;
        String address = "192.168.0.38";
        int port = 8888;
        Socket socket;

        @Override
        public void run() {

            while (true) {
                try {
                    Log.d("[Client]","Try Connecting Server");
                    socket = new Socket(address, port);
                    if (socket != null && socket.isConnected()) {
                        break;
                    }
                } catch (IOException e) {
                    //Toast.makeText(MainActivity.this, "Retry", Toast.LENGTH_SHORT).show();
                   Log.d("[Client]","Re-Try");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                    }
                }

            }

            Log.d("[Client]","Server Connected");

            // 소켓 연결 후, 리시버 생성
            try {
                new Receiver(socket).start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void sendMsg(String msg) {
            try {

                Sender sender = new Sender(socket);
                sender.setSendMsg(msg);
                new Thread(sender).start();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        class Sender implements Runnable {
            Socket socket;
            OutputStream out;
            DataOutputStream outw;
            String sendMsg;

            public Sender(Socket socket) throws IOException {
                this.socket = socket;
                out = socket.getOutputStream();
                outw = new DataOutputStream(out);
            }

            public void setSendMsg(String sendMsg) {
                this.sendMsg = sendMsg;
            }

            @Override
            public void run() {
                try {
                    if (outw != null) {
                        Log.d("Client Sender","write ready");
                        outw.writeUTF(sendMsg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        public void convertImg (final String str){

            Runnable r= new Runnable() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(str.equals("1")){
                                imageView.setImageResource(R.drawable.car1);
                            }else if(str.equals("2")){
                                imageView.setImageResource(R.drawable.car2);
                            }else if(str.equals("3")){
                                imageView.setImageResource(R.drawable.car3);
                            }
                        }
                    });

                }
            };

            new Thread(r).start();

        }

        class Receiver extends Thread {
            Socket socket;
            InputStream in;
            DataInputStream inr;

            public Receiver(Socket socket) throws IOException {
                this.socket = socket;
                in = socket.getInputStream();
                inr = new DataInputStream(in);

            }

            @Override
            public void run() {

                try {
                    while (inr != null && flag) {

                        String str = inr.readUTF();
                        Log.d("[Client Msg]",str);
                        convertImg(str);

                    }

                } catch (Exception e) {

                } finally {

                    if (inr != null)
                        try {
                            inr.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                }


            }
        }

        public void stopClient() {

            try {

                Thread.sleep(1000);
                flag = false;
                if (socket != null)
                    socket.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

        }


    }

}
