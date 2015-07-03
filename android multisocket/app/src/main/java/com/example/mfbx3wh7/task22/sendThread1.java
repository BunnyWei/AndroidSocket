package com.example.mfbx3wh7.task22;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by mfbx3wh7 on 04/05/15.
 */
public class sendThread1 implements Runnable {
    private static final String str = "130.88.193.201";
    private static final int byteLength = 1024;
    @Override
    public void run() {
        try {

            InetAddress inetAddress = InetAddress.getByName("130.88.193.201");
            DatagramSocket socketSend = new DatagramSocket();
            String str1 = "NACK1";
            byte[] nack = str1.getBytes();
            DatagramPacket datagramPacketnack = new DatagramPacket(nack, nack.length,
                    inetAddress, 5000);
            socketSend.send(datagramPacketnack);
            byte[] sendtext1 = new byte[1024];
            final InetAddress inetAddress1 = InetAddress.getByName(str);
            DatagramPacket datagramPacket1 = new DatagramPacket(new byte[byteLength], byteLength);
            try{
                socketSend.receive(datagramPacket1);
            }catch(SocketTimeoutException e){
                e.printStackTrace();
            }

            String checkCRCStr = new String(datagramPacket1.getData(),0,datagramPacket1.getLength());
            byte[] checkCRC = Arrays.copyOfRange(datagramPacket1.getData(), 0, datagramPacket1.getLength());
            Log.i("the receive data:",checkCRCStr);
            socketSend.close();

    } catch (UnknownHostException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }

    }
}
