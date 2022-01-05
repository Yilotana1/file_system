package com.company;

import com.company.exceptions.NoMoreDescriptorsAvailableException;

import java.io.Serializable;
import java.util.*;

import static com.company.FileSystem.descCounter;

public class FileDescriptor implements Serializable {


    private final FileType fileType;
    private int linksNumber;
    private int size;
    private final int id;
    private Map<String, Integer> directoryLinks;
    private TreeMap<Integer, Block> blocksMap = new TreeMap<>();

    public FileDescriptor(FileType fileType) throws NoMoreDescriptorsAvailableException {
        if (FileSystem.MAX_DESCRIPTORS_NUMBER == descCounter)
            throw new NoMoreDescriptorsAvailableException();
        this.id = ++descCounter;
        this.fileType = fileType;
        this.linksNumber++;

        if (fileType == FileType.DIRECTORY) {
            directoryLinks = new HashMap<>();
        }

    }


    public Map<String, Integer> getDirectoryLinks() {
        return directoryLinks;
    }

    public FileType getFileType() {
        return fileType;
    }

    public TreeMap<Integer, Block> getBlocksMap() {
        return blocksMap;
    }

    public void setLinksNumber(int linksNumber) {
        this.linksNumber = linksNumber;
    }

    public int getLinksNumber() {
        return linksNumber;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Integer getId() {
        return id;
    }

}
