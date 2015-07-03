package com.example.mfbx3wh7.task22;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by mfbx3wh7 on 04/05/15.
 */
public class sendThread4 implements Runnable {
    private static final String str = "130.88.193.201";
    private static final int byteLength = 1024;
    private static final int port = 5000;
    private static final String barker = "10110111000";

    @Override
    public void run(){
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress inetAddress = InetAddress.getByName(str);
            String str = "NACK4";
            byte[] nack = str.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(nack, nack.length,
                    inetAddress,port);
            socket.send(datagramPacket);
            Log.i("hello+:", datagramPacket.getData().toString());
            DatagramPacket datagramPacket1 = new DatagramPacket(new byte[byteLength],byteLength);
            try{
                socket.receive(datagramPacket1);
            }catch(SocketTimeoutException e){
                e.printStackTrace();
            }
            byte[] receive = Arrays.copyOfRange(datagramPacket1.getData(), 0, datagramPacket1.getLength());
            for(int i = 0; i < datagramPacket.getLength(); i++){
                System.out.println("have no choice:"+receive[i]);
            }
            String receiveBinary = ConvertStrToBinary(receive).toString();
            Log.i("hello1+:",receiveBinary);
            StringBuffer finalResult = XORDSSS(receiveBinary);
            Log.i("show message about:", finalResult.toString());
            String temp = finalResult.toString();
            String message = EleToZeroOne(temp).toString();
            Log.i("the input message:",message);

        } catch (SocketException e) {
            e.printStackTrace();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }


    }

    public StringBuffer ConvertStrToBinary(byte[] check){
        int iteration = check.length;
        StringBuffer changeStr = new StringBuffer();
        int i = 0;
        while(i < iteration){
            String register = Integer.toBinaryString((check[i] + 256) % 256);
            if(register.length() < 8){
                int count = 8 - register.length();
                while(count > 0){
                    register = "0" + register;
                    count--;
                }
            }
            changeStr =changeStr.append(register);
            i++;
        }

        return changeStr;
    }

    public StringBuffer XORDSSS(String receive){
        int length = receive.length();
        StringBuffer returnByte = new StringBuffer();
        for(int i = 0; i < length/11; i++){
            for(int j = 11 * i; j < 11 * i + 11; j++){
                int bakerIndex = j % 11;
                //xor the string with the dsss to generate the original value;
                if(receive.charAt(j) == barker.charAt(bakerIndex)){
                    //returnByte[j] = 0;
                    returnByte.append('0');
                }else returnByte.append('1');
            }
        }
        return returnByte;
    }

    public StringBuffer EleToZeroOne(String decreaseStr){
        //String inputMsg = null;
        StringBuffer tempStr = new StringBuffer();
        for(int i = 0; i < decreaseStr.length() / 11; i++){
            int ZeroCount = 0;
            int OneCount = 0;
            for(int j = 11 * i ;j < (11 * i + 11) ; j ++){
                if(decreaseStr.charAt(j) == '0'){
                    ZeroCount++;
                }else OneCount++;
            }
            if(ZeroCount > OneCount){
                tempStr.append('0');
            }else tempStr.append('1');
        }
        System.out.println(tempStr);
        StringBuffer inputMsg = new StringBuffer();
        for(int t = 0; t < tempStr.length() /8; t++){
            inputMsg.append((char) Integer.parseInt(tempStr.substring(t*8,t*8+8),2));
        }
        return inputMsg;

    }

}
