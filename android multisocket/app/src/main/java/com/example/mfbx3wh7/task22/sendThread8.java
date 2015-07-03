package com.example.mfbx3wh7.task22;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class sendThread8 implements Runnable {

    private static final String addressStr = "130.88.193.201";
    private static final int port = 5000;
    private static final int byteLength = 1024;
    @Override
    public void run(){
        String str = "NACK6_";
        try {
            InetAddress inetAddress = InetAddress.getByName(addressStr);
            DatagramSocket socket = new DatagramSocket();
            int packetCount = 0;
            byte[][] tempByte = new byte[150][162];
            int askRetreCount = 0;
            while(packetCount < 150){
                String sendStr = str + Integer.toString(packetCount);
                byte[] nack = sendStr.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(nack,nack.length,
                        inetAddress,port);
                socket.send(datagramPacket);
                DatagramPacket datagramPacket1 = new DatagramPacket(new byte[byteLength],byteLength);

                try{
                    socket.receive(datagramPacket1);
                }catch(SocketTimeoutException e){
                    e.printStackTrace();
                }

                byte[] inputWav = Arrays.copyOfRange(datagramPacket1.getData(), 0, datagramPacket1.getLength());
                StringBuffer CRCcheckStr = ConvertStrToBinary(inputWav);
                Log.i("the number of package:", sendStr);
                tempByte[packetCount] = inputWav;
                packetCount = packetCount + 1;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            byte[] finalByte = new byte[packetCount * 162];
            int tempCount = 0;
            for(int i = 0; i < finalByte.length; i =tempCount *162){
                for(int j = 0 ; j < 162; j++){
                    finalByte[i+j] = tempByte[tempCount][j];
                }
                tempCount++;
            }
            SoundConvert soundConvert = new SoundConvert();
            byte[] interMediaWav = soundConvert.convert_a_law_to_byte_wav(finalByte, finalByte.length);
            System.out.println(interMediaWav);

            File file = File.createTempFile("bunny","wav");
            // InputStream byteArray = new ByteArrayInputStream(interMediaWav);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(interMediaWav);
            fos.flush();
            fos.close();
            MediaPlayer mediaPlayer = new MediaPlayer();
            FileInputStream fis = new FileInputStream(file);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();


            //byte[] nack =
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (SocketException e) {
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
