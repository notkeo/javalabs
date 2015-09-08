package snapshot;

import snapshot.domain.Snapshot;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

public class Utils {

    public static String calculateHash(File file) {
        if (!file.isFile()) throw new IllegalArgumentException("Argument mast be file, not directory");
        try {
            byte[] data = new byte[4096];
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            while (true) {
                int rs = dataInputStream.read(data);
                digest.update(data);
                if (rs == -1) break;
            }
            return new BigInteger(digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("Access to file was denied: " + file.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void persist(HashMap<File, Snapshot> db) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("db"));
            stream.writeObject(db);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<File, Snapshot> loadDB() {
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream("db"));
            return (HashMap<File, Snapshot>) stream.readObject();
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
        return new HashMap<>();
    }
}
