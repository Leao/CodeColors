package io.leao.codecolors.plugin.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IgnoreResFileFile extends File {
    private Set<File> mResFiles;

    public IgnoreResFileFile(String pathname, Set<File> resFiles) {
        super(pathname);
        mResFiles = resFiles;
    }

    @Override
    public String[] list() {
        String[] names = super.list();
        if (names == null) {
            return null;
        }
        List<String> v = new ArrayList<>();
        for (String name : names) {
            if (!mResFiles.contains(new File(this, name))) {
                v.add(name);
            }
        }
        return v.toArray(new String[v.size()]);
    }

    @Override
    public File[] listFiles() {
        String[] ss = list();
        if (ss == null) return null;
        int n = ss.length;
        File[] fs = new File[n];
        for (int i = 0; i < n; i++) {
            fs[i] = createFile(this, ss[i]);
        }
        return fs;
    }

    @Override
    public File[] listFiles(FilenameFilter filter) {
        String ss[] = list();
        if (ss == null) return null;
        ArrayList<File> files = new ArrayList<>();
        for (String s : ss) {
            if ((filter == null) || filter.accept(this, s)) {
                files.add(createFile(this, s));
            }
        }
        return files.toArray(new File[files.size()]);
    }

    @Override
    public File[] listFiles(FileFilter filter) {
        String ss[] = list();
        if (ss == null) return null;
        ArrayList<File> files = new ArrayList<>();
        for (String s : ss) {
            File f = createFile(this, s);
            if ((filter == null) || filter.accept(f)) {
                files.add(f);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    private File createFile(File file, String child) {
        File newFile = new File(file, child);
        return FileUtils.inFolder(file, mResFiles) ?
                new IgnoreResFileFile(newFile.getPath(), mResFiles) :
                newFile;
    }
}