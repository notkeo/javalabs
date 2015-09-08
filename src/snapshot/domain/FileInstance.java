package snapshot.domain;

import java.io.File;
import java.nio.file.FileSystem;

public class FileInstance extends File {

    private long size;

    public FileInstance(String pathname) {
        super(pathname);
        size = this.length();
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        return compareTo((File) o) == 0;
    }


}
