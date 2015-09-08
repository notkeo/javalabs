package snapshot.domain;

import snapshot.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Snapshot implements Serializable {
    private File root;
    private HashMap<FileInstance, String> hashMap;
    private HashMap<File, Snapshot> childSnapshots;

    public Snapshot(File root) {
        this.root = root;
        hashMap = new HashMap<>();
        childSnapshots = new HashMap<>();
    }

    public File getRoot() {
        return root;
    }

    public HashMap<FileInstance, String> getHashMap() {
        return hashMap;
    }

    public HashMap<File, Snapshot> getChildSnapshots() {
        return childSnapshots;
    }

    public Snapshot find(File f) {
        if (f.compareTo(f) == 0) {
            return this;
        } else {
            LinkedList<Snapshot> observable = (LinkedList<Snapshot>) childSnapshots.values();
            while (true) {
                boolean goDeeper = false;
                for (Snapshot s : observable) {
                    if (s.getRoot().compareTo(f) > 0) {
                        observable = (LinkedList<Snapshot>) s.getChildSnapshots().values();
                        goDeeper = true;
                        break;
                    }
                    if (s.getRoot().compareTo(f) == 0) {
                        return s;
                    }
                }
                if (!goDeeper) break;
            }
        }
        return null;
    }

    public boolean validate() {
        boolean result = true;
        LinkedList<Snapshot> tasks = new LinkedList<>();
        tasks.add(this);
        while (!tasks.isEmpty()) {
            Snapshot task = tasks.poll();
            Set<Map.Entry<FileInstance, String>> hashes = task.getHashMap().entrySet();
            File[] files = task.getRoot().listFiles();
            for (File f : task.hashMap.keySet()) {
                if (!f.exists()) {
                    String msg = "Next file was removed: \n --path: %s \n --hash: %s";
                    System.out.println(String.format(msg, f.getAbsolutePath(), task.getHashMap().get(f)));
                    result = false;
                }
            }
            for (File f : task.getChildSnapshots().keySet()) {
                if (!f.exists()) {
                    String msg = "Next directory was removed: \n --path: %s";
                    System.out.println(String.format(msg, f.getAbsolutePath()));
                    result = false;
                }
            }
            for (File f : files) {
                if (f.isFile()) {
                    String hash = "not calculated";
                    boolean founded = false;
                    for (Map.Entry<FileInstance, String> entry : hashes) {
                        if (entry.getKey().equals(f)) {
                            founded = true;
                            boolean changed = false;
                            if (entry.getKey().getSize() != f.length()) {
                                changed = true;
                            } else {
                                hash = Utils.calculateHash(f);
                                if (!hash.equals(entry.getValue())) changed = true;
                            }
                            if (changed) {
                                String msg = "Next file was changed: \n --path: %s \n --source hash: %s \n --current hash: %s \n --source size: %d \n --current size: %d ";
                                System.out.println(String.format(msg, f.getAbsolutePath(), entry.getValue(), hash, entry.getKey().getSize(), f.length()));
                                result = false;
                            }
                        }
                    }
                    if (!founded) {
                        boolean renamed = false;
                        for (Map.Entry<FileInstance, String> entry : hashes) {
                            if (entry.getValue().equals(hash) && !entry.getKey().exists()) {
                                String msg = "Next file was renamed from: %s to %s";
                                System.out.println(String.format(msg, entry.getKey().getName(), f.getName()));
                                renamed = true;
                                result = false;
                                break;
                            }
                        }
                        if (!renamed) {
                            String msg = "Next file was created: \n --path: %s";
                            System.out.println(String.format(msg, f.getAbsolutePath()));
                            result = false;
                        }
                    }
                } else {
                    if (!task.getChildSnapshots().containsKey(f)) {
                        System.out.println("Next directory was created: \n --path: " + f.getAbsolutePath());
                        result = false;
                    } else {
                        tasks.add(task.getChildSnapshots().get(f));
                    }
                }
            }
        }
        return result;
    }
}
