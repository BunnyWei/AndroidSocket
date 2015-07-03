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
public class sendThread3 implements Runnable {
    private static final String str = "130.88.193.201";
    private static final int byteLength = 1024;
    private static final int port = 5000;

    @Override
    public void run() {
        try {
            InetAddress inetAddress = InetAddress.getByName(str);
            DatagramSocket socket = new DatagramSocket();
            String inputstr = "NACK3";
            byte[] nack = inputstr.getBytes();

            boolean flag = true;
            //int count = 0;
            while (flag) {
                String[] correction = new String[3];
                for(int count = 0; count < 3; count++){
                    DatagramPacket datagramPacket = new DatagramPacket(nack, nack.length,
                            inetAddress, port);
                    socket.send(datagramPacket);
                    Log.i("send success:", nack.toString());
                    DatagramPacket datagramPacket1 = new DatagramPacket(new byte[byteLength], byteLength);
                    try {
                        socket.receive(datagramPacket1);
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    }
                    byte[] checkCRC = Arrays.copyOfRange(datagramPacket1.getData(), 0, datagramPacket1.getLength());
                    StringBuffer checkCRCbin = ConvertStrToBinary(checkCRC);
                    StringBuffer reStrCheck = new StringBuffer();
                    for(int j = 0; j < checkCRCbin.length(); j++){
                        reStrCheck.append(checkCRCbin.charAt(j));
                    }
                    if (calcuCRC(reStrCheck)) {
                        String s = new String(datagramPacket1.getData(), 0, datagramPacket1.getLength());
                        Log.i("Receiver success:", s);
                        flag = false;
                        count = 3;
                    } else {
                        correction[count] = checkCRCbin.toString();
                        if (count >= 2) {
                            StringBuffer mergeString = mergePacket(correction);
                            System.out.println("count2:" + count);
                            System.out.println(mergeString);
                            //String merge = mergeString.toString();
                            StringBuffer reStrCheck2 = new StringBuffer();
                            for(int j = 0; j < checkCRCbin.length(); j++){
                                reStrCheck2.append(mergeString.charAt(j));
                            }
                            if (calcuCRC(reStrCheck2)) {
                                StringBuffer inputMsg = new StringBuffer();
                                for (int t = 0; t < mergeString.length() / 8; t++) {
                                    inputMsg.append((char) Integer.parseInt(mergeString.substring(t * 8, t * 8 + 8), 2));
                                }
                                Log.i("correct modified:", inputMsg.toString());
                                flag = false;
                                count = 3;
                            }
                            else{ count = 3; System.out.println("count1:"+count);}
                        }
                    }
                }
                //there are three wrong packets


            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public StringBuffer ConvertStrToBinary(byte[] check) {
        int iteration = check.length;
        StringBuffer changeStr = new StringBuffer();
        int i = 0;
        while (i < iteration) {
            String register = Integer.toBinaryString((check[i] + 256) % 256);
            if (register.length() < 8) {
                int count = 8 - register.length();
                while (count > 0) {
                    register = "0" + register;
                    count--;
                }
            }
            changeStr = changeStr.append(register);
            i++;
        }

        return changeStr;
    }

    public boolean calcuCRC(StringBuffer checkCRCbin) {
        String poly = "100000111";
        int iteration = checkCRCbin.length();
        StringBuffer replaceStr = new StringBuffer(checkCRCbin.substring(0, 8));
        String returnStr = new String();
        int count = 0;
        while (count < (iteration - 8)) {
            String TrStr = checkCRCbin.substring(count, count + 9); //substring end dont include the address of end
            if (TrStr.charAt(0) == '1') {
                for (int i = 1; i < 9; i++) {
                    if (TrStr.charAt(i) == poly.charAt(i)) {
                        replaceStr.replace(i - 1, i - 1, "0");
                    } else replaceStr.replace(i - 1, i - 1, "1");
                }
                checkCRCbin.replace(count + 1, count + 9, replaceStr.substring(0, 8)); //replace end include the address of end
            }
            //else checkCRCbin.replace(count,count+9,replaceStr.substring(0,8)); //replace end include the address of end

            count++;
            if (count == (iteration - 8)) {
                returnStr = checkCRCbin.substring(count, count + 8);
                System.out.println(returnStr);
            }
        }
        if (returnStr.equals("00000000")) {
            return true;
        } else {
            return false;
        }
    }

    public StringBuffer mergePacket(String[] correction) {
        StringBuffer mergeString = new StringBuffer();
        int packetLength = correction[0].length() < correction[1].length() ?
                correction[0].length() : correction[1].length();
        packetLength = packetLength < correction[2].length() ? packetLength : correction[2].length();
        for (int i = 0; i < packetLength; i++) {
            int ZeroCount = 0;
            int OneCount = 0;
            if (correction[0].charAt(i) == '0') {
                ZeroCount++;
            } else OneCount++;
            if (correction[1].charAt(i) == '0') {
                ZeroCount++;
            } else OneCount++;
            if (correction[2].charAt(i) == '0') {
                ZeroCount++;
            } else OneCount++;

            if (ZeroCount > OneCount) {
                mergeString.append('0');
            } else mergeString.append('1');
        }
        return mergeString;
    }
}
