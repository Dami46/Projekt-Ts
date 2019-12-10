package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

 class RemindTask extends TimerTask {
     private int seconds,n;

     public RemindTask(int seconds) {

         this.seconds = seconds;

     }

    public void run() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat data = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy ");
        String time = data.format(calendar.getTime());
        HashMap<Integer, Integer> Map = Serwer.getClientMap();
        try {
            if (seconds > 0) {
                String t = Integer.toString(seconds);

                for (java.util.Map.Entry<Integer, Integer> entry : Map.entrySet()) {
                    String message = "TM?" + time + "<<ID?" + entry.getValue() + "<<OP?Czas do zakonczenia<<CR?" + t + "s";
                    InetAddress clientAddress = InetAddress.getLocalHost();
                    DatagramPacket clientPacket = new DatagramPacket(message.getBytes(), message.length(), clientAddress,entry.getKey());

                    Serwer.serwerSocket.send(clientPacket);
                }
            }

            if (seconds <= 0) {
                for (Map.Entry<Integer, Integer> entry : Map.entrySet()) {

                    String message = "TM?" + time + "<<ID?" + entry.getValue() + "<<OP?Timeout";
                    InetAddress clientAddress = InetAddress.getLocalHost();

                    DatagramPacket clientPacket = new DatagramPacket(message.getBytes(), message.length(), clientAddress,entry.getKey());

                    Serwer.serwerSocket.send(clientPacket);
                    this.cancel();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        seconds = seconds - 10;
        n++;
    }

}
