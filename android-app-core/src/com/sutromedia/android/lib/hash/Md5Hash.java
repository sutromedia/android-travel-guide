package com.sutromedia.android.lib.hash;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Formatter;

public class Md5Hash {

    public static String calculateHash(final String filename) throws Exception {
        MessageDigest md5  = MessageDigest.getInstance("MD5");        
        return calculateHash(md5, filename);
    }

    public static String calculateHash(
        final MessageDigest algorithm,
        final String fileName) throws Exception {

        FileInputStream     fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DigestInputStream   dis = new DigestInputStream(bis, algorithm);

        // read the file and update the hash calculation
        while (dis.read() != -1);

        // get the hash value as byte array
        byte[] hash = algorithm.digest();

        return byteArray2Hex(hash);
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}