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
public class sendThread5 implements Runnable{

    private static final String Addressstr = "130.88.193.201";
    private static final int byteLength = 1024;
    private static final int port = 5000;

    @Override
    public void run(){
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress inetAddress = InetAddress.getByName(Addressstr);
            String sendStr = "NACK5";
            byte[] nack = sendStr.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(nack,nack.length,
                    inetAddress,port);

            boolean flag = true;
            while(flag){
                socket.send(datagramPacket);
                DatagramPacket datagramPacket1 = new DatagramPacket(new byte[byteLength],byteLength);
                try{
                    socket.receive(datagramPacket1);
                }catch(SocketTimeoutException e){
                    e.printStackTrace();
                }

                byte[] receive = Arrays.copyOfRange(datagramPacket1.getData(), 0, datagramPacket1.getLength());
                StringBuffer strCheck = ConvertStrToBinary(receive);
                short[] Inp = ConvertStringToShort(strCheck);

                bbsdvd08 BBsdv = new bbsdvd08();
//                short[] Inp = new short[receive.length];
//                int j = 0;
//                for(int i = 0; i < receive.length ; i++){
//                    Inp[j++] = (short)((short)receive[i+1]>>8 + (short)receive[i]);
//                    i = i + 1;
//                }
                short[] outShort = BBsdv.executeVertical(Inp.length,Inp);
                byte[] resultByte = shortToByte(outShort);
                StringBuffer stringBuffer = ConvertStrToBinary(resultByte);
                StringBuffer inputMsg = new StringBuffer();
                for(int t = 0; t < stringBuffer.length() /8; t++){
                    inputMsg.append((char) Integer.parseInt(stringBuffer.substring(t*8,t*8+8),2));
                }
                if(calcuCRC(stringBuffer)){
                    Log.i("success", inputMsg.toString());
                    flag = false;
                }else {
                    Log.i("fail, retransmission  ",inputMsg.toString());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch (SocketException e) {
            e.printStackTrace();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public short[] ConvertStringToShort(StringBuffer buffer){
        int bufferLength = buffer.toString().length();
        short[] Inp = new short[bufferLength];
        for(int i = 0 ; i < bufferLength; i++){
            Inp[i] =(short) ((int)buffer.charAt(i) - 48);
        }
        return Inp;
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

    public byte[] shortToByte(short[] outShort){
        byte[] resultByte = new byte[outShort.length /8];
        for(int i = 0; i < resultByte.length; i++){
            int b = 0;
            for(int j = 8 * i ; j < 8 * i + 8 ; j++){
                b = b * 2 + outShort[j];
            }
            resultByte[i] = (byte)b;
        }
        return resultByte;
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
