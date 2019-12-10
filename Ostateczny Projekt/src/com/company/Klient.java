package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Klient implements Runnable {
    private static int serwerport=50000;
    private InetAddress ipadress;
   boolean gra = true;
   private static String IDsesji ;

    public Klient() {
    }
    private void potwierdz(DatagramSocket serverSocket) throws IOException {
       String time = time();
        String message = "OP?Potwierdzenie<<TM?" + time + "<<ID?" + IDsesji + "<<OD?Odebrano<<";
        DatagramPacket POTWIERDZENIE = new DatagramPacket(
                message.getBytes(),
                message.length(),
                ipadress,
                serwerport
        );
        serverSocket.send(POTWIERDZENIE);
    }
    String time (){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat data = new SimpleDateFormat("HH:mm:ss_dd-MM-yyyy");
        String time = data.format(calendar.getTime());

        return  time;
    }

    @Override
    public void run() {
        System.out.println("Nowy klient ");
        try (DatagramSocket clientSocket = new DatagramSocket()) {

                ipadress=InetAddress.getLocalHost();
            byte[] buffer = new byte[256];

            // Set a timeout of 3000 ms for the client.
           // clientSocket.setSoTimeout(3000);

                //client wysyłą swoj port i ip i prosi o przydzielenie id
            String time = time();
            String message3 = "OP?Nawiazanie_polaczenia<<TM?" + time + "<<ID?" + IDsesji + "<<OD?null<<";
                DatagramPacket requestconnection = new DatagramPacket(message3.getBytes(), message3.length(), ipadress, serwerport);
                clientSocket.send(requestconnection);
                System.out.println("Nawiazanie połączenia");

            /** Odebranie potwierdzenia*/
            DatagramPacket request = new DatagramPacket(new byte[64], 64);
            clientSocket.receive(request);

                //odbieranie identifikatorow
                DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
                clientSocket.receive(datagramPacket);
            String receivedMessage = new String(datagramPacket.getData());

            String id = "null";
            for (int i = 0; i < receivedMessage.length(); i++) {
                if (receivedMessage.charAt(i) == 'I') {
                    if (receivedMessage.charAt(i + 1) == 'D') {
                        if (receivedMessage.charAt(i + 2) == '?') {
                            id = "" + receivedMessage.charAt(i + 3);
                            if (receivedMessage.charAt(i + 4) != '<') {
                                id = id + receivedMessage.charAt(i + 4);
                                if (receivedMessage.charAt(i + 5) != '<') {
                                    id = id + receivedMessage.charAt(i + 5);
                                    if (receivedMessage.charAt(i + 6) != '<') {
                                        id = id + receivedMessage.charAt(i + 6);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("\nPrzyznane ID sesji:" + id);
            IDsesji = id;




            //POTWIERDZENIE
           potwierdz(clientSocket);
           while (gra) {//wpisywanie liczby

               System.out.println("Wpisz cyfre od 1-9 zeby rozwiazac zagadke ");
               Scanner liczba = new Scanner(System.in);
               String liczbaa = liczba.nextLine();
               String time1 = time();
               String zagadka = "OP?Zagadka<<ZG?" + liczbaa + "<<TM?" + time1 + "<<ID?" + IDsesji + "<<OD?null<<";
               System.out.println("Wybrano liczbe: " + liczbaa);

               //wysylanie liczby do serwera
               DatagramPacket liczbazagadka = new DatagramPacket(zagadka.getBytes(), zagadka.length(), ipadress, serwerport);
               clientSocket.send(liczbazagadka);

               System.out.println("Wyslano liczbe");

               /** Odebranie potwierdzenia*/
               clientSocket.receive(request);

               //odbieranie odpowiedzi
               DatagramPacket odpowiedzzagadka = new DatagramPacket(new byte[256], 256);
               clientSocket.receive(odpowiedzzagadka);
               String odpowiedzzagadki = new String(odpowiedzzagadka.getData());

               if (odpowiedzzagadki.contains("OD?NOPE")) {
                  // System.out.println(odpowiedzzagadki);
                   odpowiedzzagadki = odpowiedzzagadki.replace(odpowiedzzagadki, "Nie udalo sie zgadnac ");
                   gra = true;
                   System.out.println(odpowiedzzagadki);
                   potwierdz(clientSocket);
               }

               if (odpowiedzzagadki.contains("OD?YAS")) {
                   //System.out.println(odpowiedzzagadki);
                   odpowiedzzagadki = odpowiedzzagadki.replace(odpowiedzzagadki, "Brawo udało się zgadnąc liczbe!");
                   System.out.println(odpowiedzzagadki);
                   System.out.println("Koniec gry");
                   potwierdz(clientSocket);
                   gra = false;
                   break;

               }
               if (odpowiedzzagadki.contains("CR?")) {
                   System.out.println(odpowiedzzagadki);

               }
               if (odpowiedzzagadki.contains("Timeout")) {
                   System.out.println(odpowiedzzagadki);
                   gra = false;
               }



    }






        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("ER?Timeout. Client is closing.");
        }
    }
    public static void main(String[] args) {

        Klient client = new Klient();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(client);

    }
}