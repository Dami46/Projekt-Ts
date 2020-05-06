package com.company;

import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.parseInt;

public class Serwer implements Runnable {

    private static int clientPort;
    private final int port;
   private InetAddress clientAddress;
    private int czas_rozgrywki;
    private static Vector<Integer> idklietow = new Vector<Integer>();//lista id
    private static HashMap<Integer, Integer> clientMap; //MAPA CLIENTOW
    private int idsesji ;
    private static  int zagadka;
    public static DatagramSocket serwerSocket ;
  private Boolean GRA;
    private long tStart;
    private long tGame;
    private long tEnd;

    public Serwer(int port) {
        this.port = port;
    }

    private int generateid()
    {
        Random rand = new Random();
        idsesji = rand.nextInt(8999 ) +1000 ;
        return  idsesji;
    }
    private static int generateliczba()
    {  int liczba;
        Random rand = new Random();
        liczba= rand.nextInt(8)+1 ;
        return  liczba;
    }
     private void potwierdz(DatagramSocket serverSocket, int port, DatagramPacket request,int sesionid) throws IOException {
         String time = time();
         InetAddress clientAddress = request.getAddress();
         String message = "OP?Potwierdzenie<<TM?" + time + "<<ID?" + sesionid + "<<OD?Odebrano<<";
         DatagramPacket datagramPacket = new DatagramPacket(
                 message.getBytes(),
                 message.length(),
                 clientAddress,
                 port
         );
         serverSocket.send(datagramPacket);
     }

    public  static  Vector<Integer> getIdklietow() {
        return idklietow;
    }

    String time (){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat data = new SimpleDateFormat("HH:mm:ss_dd-MM-yyyy");
        String time = data.format(calendar.getTime());
        return  time;
    }
    public int getIdsesji() {
        return idsesji;
    }

    public static HashMap<Integer, Integer> getClientMap() {
        return clientMap;
    }

    @Override
    public void run() {
        System.out.println("Start serwera ");
        DatagramPacket request = new DatagramPacket(new byte[64], 64);

        zagadka = generateliczba();
        System.out.println("Liczba do zgadniecia " + zagadka);
        int liczbaklientow = 1;
        clientMap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> mapclone = new HashMap<Integer, Integer>();
        Timer czas = new Timer();
        boolean running = true;

            try  {
              serwerSocket = new DatagramSocket(port);
                while (running) {

                    //odbieranie połaczenia

                    serwerSocket.receive(request);
                    String received2 = new String(request.getData());


                    if (received2.contains("Nawiazanie_polaczenia")) {

                        //potwierdznie
                        potwierdz(serwerSocket,request.getPort(),request,idsesji);
                        //Nadawanie identyfikatorow klienta
                        idsesji = generateid();

                        clientPort = request.getPort();
                        czas_rozgrywki = 0;
                        //obliczanie czasu rozgrywki
                        idklietow.add(idsesji);
                        if (clientMap.size() > 0) {
                            czas_rozgrywki = idklietow.get(0)+idklietow.get(1);
                        }

                        czas_rozgrywki = (czas_rozgrywki * 99) % 100 + 30;   // [(id. sesji 1 + id. sesji 2) * 99] % 100 + 30
                        System.out.println("Wyznaczono czas rozgrywki: " + czas_rozgrywki);
                      // czas.schedule(new RemindTask(czas_rozgrywki), 0, 10000);





                        //pobieranie portu
                        if (!serwerSocket.isConnected()) running = true;
                        else running = false;


                        /** Dodawwanie klienta do mapy*/
                        clientMap.put(clientPort, idsesji);
                        System.out.println("Dodano klienta nr:" + liczbaklientow);




                            InetAddress clientAddress2 = request.getAddress();
                            String message = "OP?Sesja<<OD?Nadano_wlasciwosci" + "<<ID?" + idsesji + "<<CR?" + czas_rozgrywki + "<<TM?" + time() +"<<" ;
                            DatagramPacket datagramPacket = new DatagramPacket(
                                    message.getBytes(),
                                    message.length(),
                                    clientAddress2,
                                   clientPort
                            );
                            serwerSocket.send(datagramPacket);

                            liczbaklientow++;
                            // odbieranie potwierdzenia
                            serwerSocket.receive(request);


                    }


                    if (clientMap.size() >= 2 ) {
                                do {

                                    tStart = System.currentTimeMillis() % 100000;
                                    //System.out.println("Rozgrywka klienta nr: " + liczbaklientow);
                                    DatagramPacket zagadkaa = new DatagramPacket(new byte[512], 512);
                                    serwerSocket.receive(zagadkaa);

                                     // wait(10);
                                    String gra = new String(zagadkaa.getData());
                                    if (gra.contains("ZG?")) {
                                       // System.out.println(gra);
                                        Integer id = clientMap.get(zagadkaa.getPort());
                                        /** Poszukiwanie zagadkowej cyfry*/
                                        String result1 = "";
                                        for (int j =2; j < gra.length(); j++) {
                                            if (gra.substring(j - 2, j).contains("ZG")) {
                                                if (!gra.substring(j + 1, j + 2).equals("<")) {
                                                   result1 = result1 + gra.charAt(j + 1);
                                                }
                                                System.out.println("Odebrana cyfra: " + result1 + " Od klienta o id "+ id);
                                            }
                                        }
                                        int result = parseInt(result1);
                                        //potwierdzenie


                                        potwierdz(serwerSocket, zagadkaa.getPort(), zagadkaa, id);


                                            if (result==zagadka) {
                                                InetAddress clientAddress3 = zagadkaa.getAddress();
                                                String time = time();
                                                String wygrana = "OP?Wygrana<<OD?YAS<<TM?" + time + "<<ID?" + id + "<<";

                                                DatagramPacket wwygrana = new DatagramPacket(
                                                        wygrana.getBytes(),
                                                        wygrana.length(),
                                                        clientAddress3,
                                                      zagadkaa.getPort()
                                                );
                                                //tEnd = System.currentTimeMillis() % 100000;
                                                serwerSocket.send(wwygrana);
                                               // czas.cancel();
                                                //tGame = Math.abs(tStart - tEnd);
                                                //String tOfGame = String.valueOf(tGame / 1000);
                                                GRA = false;
                                                System.out.println("Koniec rozgrywki klienta o id sesji:" + id);
                                              //  System.out.println("Czas rozgrywki: " + tOfGame + " sekund/y" );

                                                break;
                                            } else {

                                                InetAddress clientAddress3 = zagadkaa.getAddress();
                                                String time = time();
                                                String przegrana = "OP?Przegrana<<OD?NOPE<<TM?" + time + "<<ID?" + id + "<<";
                                                DatagramPacket przgrana = new DatagramPacket(
                                                        przegrana.getBytes(),
                                                        przegrana.length(),
                                                        clientAddress3,
                                                        zagadkaa.getPort()
                                                );

                                                serwerSocket.send(przgrana);
                                                GRA = true;
                                            }
                                        }
                                        serwerSocket.receive(request);


                                } while (GRA);
                            }


                    }


                } catch(SocketException e){
                    System.err.println("Error: Nieznany port " + e.getMessage());
                } catch(UnknownHostException e){
                    System.err.println("Error: Nieznany host " + e.getMessage());
                } catch(IOException e){
                    e.printStackTrace();
                } //catch (InterruptedException e) {
               // System.err.println("Błąd przerwania komunikacji " + e.getMessage());
           // }


    }

    public static void main(String[] args) {
              int port = 50000;
              Serwer server = new Serwer(port);
              ExecutorService executorService = Executors.newFixedThreadPool(100);
              executorService.submit(server);


    }
}
