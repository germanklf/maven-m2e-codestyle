package com.despegar.tools.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;

/**
 * Helper class to manage native java serialization and deserialization.
 * 
 * @author germanklf
 */
public class StreamHelper {

    public static byte[] bytesOf(Serializable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("i/o error serializing object", e);
        } finally {
            IOUtils.closeQuietly(oos);
        }
    }

    public static Serializable objectOf(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            return (Serializable) ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException("i/o error deserializing object", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not found deserializing object", e);
        } finally {
            IOUtils.closeQuietly(ois);
        }
    }

}
