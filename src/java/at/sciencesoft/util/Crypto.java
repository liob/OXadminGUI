package at.sciencesoft.util;

import java.security.MessageDigest;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    /**
     * Creates a MD5 checksum.
     * @param data
     * @return MD5
     */
    public static String md5(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
        }
        md.update(data.getBytes());
        byte[] digest = md.digest();
        StringBuffer md5 = new StringBuffer();
        for (int i = 0; i < digest.length; ++i) {
            md5.append(toHexString(digest[i]));
        }
        // MD5-Prüfsumme
        return md5.toString();
    }

    /**
     * Converts a byte to HEX value
     * @param b byte
     * @return
     */
    public static String toHexString(byte b) {
        int value = (b & 0x7F) + (b < 0 ? 128 : 0);
        String ret = (value < 16 ? "0" : "");
        ret += Integer.toHexString(value).toUpperCase();
        return ret;
    }

    /**
     * Encryptes 
     * @param data
     * @param pwd
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String pwd) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(getVector(pwd), "AES");
        // Instantiate the cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(data.getBytes());

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < encrypted.length; ++i) {
            buffer.append(toHexString(encrypted[i]));
        }
        return buffer.toString();
    }

    public static void encryptFile(String in, String out, String pwd) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(getVector(pwd), "AES");
        // Instantiate the cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        Stream.writeByteStream(out, cipher.doFinal(Stream.readByteStream(in)));


    }

    public static void decryptFile(String in, String out, String pwd) throws Exception {
        Stream.writeByteStream(out, decrypt(Stream.readByteStream(in), pwd));

    }

    private static byte hexToByte(int hbyte, int lbyte) {

        if (hbyte >= '0' && hbyte <= '9') {
            hbyte -= '0';
        } else {
            hbyte -= 'A';
            hbyte += 10;
        }


        if (lbyte >= '0' && lbyte <= '9') {
            lbyte -= '0';
        } else {
            lbyte -= 'A';
            lbyte += 10;
        }
        return (byte) (hbyte * 16 + lbyte);
    }

    private static byte[] getVector(String pwd) throws Exception {
        MessageDigest md = null;
        md = MessageDigest.getInstance("MD5");
        md.update(pwd.getBytes());
        return md.digest();

    }

    public static String decrypt(String data, String pwd) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(getVector(pwd), "AES");

        // Instantiate the cipher

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        char[] carray = data.toString().toCharArray();
        byte[] encrypted = new byte[carray.length / 2];

        int index = 0;

        for (int i = 0; i < carray.length; ++i) {
            int hbyte = carray[i];
            int lbyte = carray[++i];
            encrypted[index++] = hexToByte(hbyte, lbyte);
        }


        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);

    }

    public static byte[] decrypt(byte[] data, String pwd) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(getVector(pwd), "AES");
        // Instantiate the cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(data);
        return decrypted;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(decrypt("xxx","xxx"));
    }
}
