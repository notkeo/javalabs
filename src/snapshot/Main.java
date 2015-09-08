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
                Snapshot snapshot = createSnapshot(param, false);
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
                System.out.println("All okay for : " + param);
            }
        } else {
            Snapshot newSnap = createSnapshot(param, true);
            Scanner s = new Scanner(System.in);
            System.out.println("Persist new files?");
            int i = s.nextInt();
            if (i == 1) {
                for (File f : db.keySet()) {
                    if (!db.get(f).equals(newSnap.getRoot())) {
                        Snapshot snapshot1 = newSnap.find(f);
                        if (!snapshot1.equals(db.get(f))) {
                            snapshot1.getChildSnapshots().put(f, db.get(f));
                        }
                    }
                }
                db.put(toCheck, newSnap);
                Utils.persist(db);
            }
        }
        return false;
    }

    private static Snapshot getSnapshotFor(File toCheck) {
        for (File f : db.keySet()) {
            if (toCheck.compareTo(f) == 0) return db.get(f);
        }
        return null;
    }

    private static Snapshot createSnapshot(String param, boolean log) {
        File root = new File(param);
        Snapshot rootSnapshot = new Snapshot(root);
        LinkedList<Snapshot> tasks = new LinkedList<>();
        tasks.add(rootSnapshot);
        while (!tasks.isEmpty()) {
            Snapshot task = tasks.poll();
            File[] files = task.getRoot().listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    if (log) System.out.println("New file was founded: " + f.getAbsoluteFile());
                    String hash = Utils.calculateHash(f);
                    if (hash != null) {
                        FileInstance fileInstance = new FileInstance(f.getAbsolutePath());
                        task.getHashMap().put(fileInstance, hash);
                    }
                } else {
                    Snapshot snapshotFor = getSnapshotFor(f);
                    if (snapshotFor == null) {
                        if (log) System.out.println("New directory was founded: " + f.getAbsoluteFile());
                        Snapshot s = new Snapshot(f);
                        task.getChildSnapshots().put(f, s);
                        tasks.add(s);
                    } else {
                        checkSnapshot(f.getAbsolutePath());
                    }
                }
            }
        }
        return rootSnapshot;
    }

}
