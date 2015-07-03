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
public class sendThread implements Runnable {

    private static final String str = "130.88.193.201";
    private static final int byteLength = 1024;
    @Override
    public void run() {
        try {

            InetAddress inetAddress = InetAddress.getByName("130.88.193.201");
            DatagramSocket socketSend = new DatagramSocket();
            String str1 = "NACK2";
            byte[] nack = str1.getBytes();
            boolean flag = true;
            int count = 0;
            while(flag){
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
                //System.out.println("bunny:"+checkCRCStr);
                //String reCheck = "ab";
                byte[] checkCRC = Arrays.copyOfRange(datagramPacket1.getData(), 0, datagramPacket1.getLength());
//                    Crc crc = new Crc();
//                    crc.updateCRC8(checkCRC,0, checkCRC.length);
//                    byte crcResult = crc.checksum();
                //byte[] checkCRC = datagramPacket1.getData();
                //byte[] checkCRC = new byte[]{6,23,4,1};
                //                   System.out.println("tast the crc class:"+crcResult);
                StringBuffer checkCRCbin = ConvertStrToBinary(checkCRC);
                if(calcuCRC(checkCRCbin)) {
                    String s = new String(datagramPacket1.getData(), 0, datagramPacket1.getLength());
                    Log.i("Receiver:", s);
                    flag = false;
                }else{
                    count++;
                }
                if(count >= 5){
                    flag = false;
                    Log.i("failure","failure");
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            socketSend.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public boolean calcuCRC(StringBuffer checkCRCbin){
        String poly = "100000111";
        int iteration = checkCRCbin.length();
        StringBuffer replaceStr = new StringBuffer(checkCRCbin.substring(0,8));
        String returnStr = new String();
        int count = 0;
        while(count < (iteration - 8)){
            String TrStr = checkCRCbin.substring(count, count+9); //substring end dont include the address of end
            if(TrStr.charAt(0) == '1'){
                for(int i = 1; i < 9 ; i++){
                    if(TrStr.charAt(i) == poly.charAt(i)){
                        replaceStr.replace(i-1,i-1,"0");
                        //replaceStr.append("0");
                    }else replaceStr.replace(i-1,i-1,"1");
                }
                checkCRCbin.replace(count+1,count+9,replaceStr.substring(0,8)); //replace end include the address of end
            }
            //else checkCRCbin.replace(count,count+9,replaceStr.substring(0,8)); //replace end include the address of end

            count++;
            if(count == (iteration -8)){
                returnStr = checkCRCbin.substring(count, count+8);
                //returnStr = replaceStr.substring(0,8).toString();
                System.out.println(returnStr);
            }
        }
        if(returnStr.equals("00000000")){
            return true;
        }else{
            return false;
        }
    }
}

