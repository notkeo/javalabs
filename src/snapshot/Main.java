package snapshot;

import snapshot.domain.FileInstance;
import snapshot.domain.Snapshot;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {

    private static HashMap<File, Snapshot> db;

    public static void main(String[] args) {
        db = Utils.loadDB();
        Scanner scanner = new Scanner(System.in);
        String action = scanner.next();
        String param = scanner.next();
        switch (action) {
            case "create": {
                Snapshot snapshot = createSnapshot(param);
                db.put(snapshot.getRoot(), snapshot);
                Utils.persist(db);
            }
            case "check":
                checkSnapshot(param);
        }
    }


    private static boolean checkSnapshot(String param) {
        File toCheck = new File(param);
        Snapshot snapshot = getSnapshotFor(toCheck);
        if (snapshot != null) {
            Snapshot container = snapshot.find(toCheck);
            if (container.validate()) {
                System.out.println("Кажется что все хорошо, но что-то не так");
            }
        } else
            System.out.println("Snapshot for selected directory is not founded");
        return false;
    }

    private static Snapshot getSnapshotFor(File toCheck) {
        for (File f : db.keySet()) {
            if (toCheck.compareTo(f) >= 0) return db.get(f);
        }
        return null;
    }

    private static Snapshot createSnapshot(String param) {
        File root = new File(param);
        Snapshot rootSnapshot = new Snapshot(root);
        LinkedList<Snapshot> tasks = new LinkedList<>();
        tasks.add(rootSnapshot);
        while (!tasks.isEmpty()) {
            Snapshot task = tasks.poll();
            File[] files = task.getRoot().listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    String hash = Utils.calculateHash(f);
                    FileInstance fileInstance = new FileInstance(f.getAbsolutePath());
                    task.getHashMap().put(fileInstance, hash);
                } else {
                    Snapshot s = new Snapshot(f);
                    task.getChildSnapshots().put(f, s);
                    tasks.add(s);
                }
            }
        }
        return rootSnapshot;
    }

}
