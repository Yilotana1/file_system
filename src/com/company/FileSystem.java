package com.company;

import com.company.exceptions.NoMoreDescriptorsAvailableException;

import java.io.Serializable;
import java.util.*;

public class FileSystem implements Serializable {


    public static int MAX_DESCRIPTORS_NUMBER = 8;

    public static final int MAX_EXPLICIT_BLOCK_LINKS = 3;

    public static int descCounter = 0;

    private final Collection<FileDescriptor> descriptors = new HashSet<>();

    private final Map<Integer, FileDescriptor> openedFiles = new HashMap<>();

    private final List<Block> blocks = new ArrayList<>();

    public final BitMap bitMap = BitMap.getInstance();

//    private FileDescriptor directory = new FileDescriptor(FileType.DIRECTORY);

    public Collection<FileDescriptor> getDescriptors() {
        return descriptors;
    }

    public Map<Integer, FileDescriptor> getOpenedFiles() {
        return openedFiles;
    }


    //    public FileDescriptor getDirectory() {
//        return directory;
//    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public FileSystem() throws NoMoreDescriptorsAvailableException {
        descriptors.add(new FileDescriptor(FileType.DIRECTORY));
    }

    public FileSystem(int maxDescriptors) throws NoMoreDescriptorsAvailableException {
        descriptors.add(new FileDescriptor(FileType.DIRECTORY));
        MAX_DESCRIPTORS_NUMBER = maxDescriptors;
    }

}

