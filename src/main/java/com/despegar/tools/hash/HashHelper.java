package com.despegar.tools.hash;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.despegar.tools.io.StreamHelper;

/**
 * Helper class to create MD5 hashes out from {@link Byte}[], {@link String} or any {@link Serializable}.
 * 
 * @author germanklf
 */
public class HashHelper {

    private static final String MD5_ALGORITHM = "MD5";

    public static String md5(byte[] bytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] messageDigest = algorithm.digest(bytes);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5(Serializable obj) {
        return md5(StreamHelper.bytesOf(obj));
    }

    public static String md5(String plainText) {
        return md5(plainText.getBytes());
    }

}
