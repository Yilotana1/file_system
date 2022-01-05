package com.company;

import com.company.exceptions.*;

import static com.company.BitMap.BLOCK_SIZE;

import java.util.*;

public class FSCore {


    public FileSystem fileSystem;

    public static final String NOT_INITIALIZED_DATA_FILLER = "0";


    public void mount() throws Exception {
        this.fileSystem = new FileSystem();

    }

    public void unmount() {
        this.fileSystem = null;
    }

    public void mkfs(int n) throws Exception {
        fileSystem = new FileSystem(n);
    }

    public void fstat(int id) throws Exception {
        FileDescriptor fileDesc = getFileDescriptorById(id);
        if (fileDesc == null) {
            throw new NoDescriptorWithSuchNumberException();
        }

        String descOutput =
                "id = " + fileDesc.getId() + "\n" +
                        "Type = " + fileDesc.getFileType() + "\n" +
                        "linksNumber = " + fileDesc.getLinksNumber() + "\n" +
                        "size = " + fileDesc.getSize() + " bytes\n";
        System.out.println(descOutput);
    }

    public void ls() {
        FileDescriptor dirDesc = getDirectoryDescriptor(this.fileSystem);
        Map<String, Integer> filesDescriptors = dirDesc.getDirectoryLinks();

        for (Map.Entry<String, Integer> fileInfo : filesDescriptors.entrySet()) {
            System.out.println("name = " + fileInfo.getKey() + "; descriptor number = " + fileInfo.getValue());
        }
    }


    public void create(String name) throws Exception {
        FileDescriptor fileDesc = new FileDescriptor(FileType.REGULAR);
        fileSystem.getDescriptors().add(fileDesc);

        // Find single folder's descriptor in filesystem
        FileDescriptor dirDesc = getDirectoryDescriptor(fileSystem);
        dirDesc.getDirectoryLinks().put(name, fileDesc.getId());
    }

    private FileDescriptor getDirectoryDescriptor(FileSystem fileSystem) {
        return fileSystem
                .getDescriptors()
                .stream()
                .filter(desc -> desc.getFileType() == FileType.DIRECTORY)
                .findFirst()
                .get();
    }

    // descriptors number begins from 1
    public int open(String name) throws NoFileWithSuchNameException {
        FileDescriptor fileDescriptor = findDescriptorByName(name);

        int newFd = generateNewFd();
        fileSystem.getOpenedFiles().put(newFd, fileDescriptor);
        return newFd;
    }

    private int generateNewFd() {
        for (int i = 1; ; i++) {
            if (!fileSystem.getOpenedFiles().containsKey(i)) {
                return i;
            }
        }
    }

    private FileDescriptor findDescriptorByName(String name) throws NoFileWithSuchNameException {
        Integer descId = findDirectory().getDirectoryLinks().get(name);
        FileDescriptor fileDesc = getFileDescriptorById(descId);
        if (fileDesc == null) {
            throw new NoFileWithSuchNameException();
        }
        return fileDesc;
    }

    private FileDescriptor findDirectory() {
        return this.fileSystem.getDescriptors().stream()
                .filter(desc -> desc.getFileType() == FileType.DIRECTORY)
                .findFirst()
                .get();
    }

    public void close(int fd) {
        FileDescriptor fileDescriptor = fileSystem.getOpenedFiles().get(fd);
        fileSystem.getOpenedFiles().remove(fd);
        clearIfNoLinksAndNotOpened(fileDescriptor);
    }

    private void clearIfNoLinksAndNotOpened(FileDescriptor fileDescriptor) {
        if (fileDescriptor.getLinksNumber() == 0 && !fileSystem.getOpenedFiles().containsValue(fileDescriptor)) {
            this.fileSystem.getDescriptors().remove(fileDescriptor);
            fileDescriptor.getBlocksMap().clear();
            fileSystem.bitMap.clearFileBlocks(fileDescriptor);
        }
    }

    public void read(int fd, int offset, int size) throws NoOpenedFilesWithSuchNumberException, DataLengthLessThanRequestedSizeException {
        FileDescriptor fileDescriptor = findFileDescriptorByFd(fd);
        if (offset + size > fileDescriptor.getSize()) {
            throw new DataLengthLessThanRequestedSizeException();
        }
        Map<Integer, Block> blocks = fileDescriptor.getBlocksMap();
        int initialBlockPosition = (offset / BLOCK_SIZE) * BLOCK_SIZE;
        int offsetFromInitialBlock = offset - initialBlockPosition;

        StringBuilder data = new StringBuilder();
        if (BLOCK_SIZE - offsetFromInitialBlock >= size) {
            data.append(blocks.get(initialBlockPosition).getData(), offsetFromInitialBlock, offsetFromInitialBlock + size);
            System.out.println(data);
            return;
        }
        data.append(blocks.get(initialBlockPosition).getData().substring(offsetFromInitialBlock));
        int blocksLeft = getHowManyBlocksLeft(size - (BLOCK_SIZE - offsetFromInitialBlock));
        System.out.println("qwerty [read] = " + (size - (BLOCK_SIZE - offsetFromInitialBlock)));
        System.out.println("blocksLeft [read] = " + blocksLeft);
        int dataOffset = BLOCK_SIZE - offsetFromInitialBlock;
        if (blocksLeft != 0) {
            for (int i = 1; i <= blocksLeft; i++) {
                if (i == blocksLeft) {
                    System.out.println("blocks [read] = " + blocks);
                    System.out.println("data [read] = " + data);
                    System.out.println("block [read] = " + blocks.get(initialBlockPosition + i * BLOCK_SIZE));
                    System.out.println("block position [read] = " + (initialBlockPosition + i * BLOCK_SIZE));
                    System.out.println("data offset [read] = " + dataOffset);
                    data.append(blocks.get(initialBlockPosition + i * BLOCK_SIZE).getData(), 0, size - dataOffset);
                    System.out.println("----READ___275_line = " + data);
                    break;
                }
                System.out.println("----BLOCK_---TEST-----!!!!!!");
                data.append(blocks.get(initialBlockPosition + i * BLOCK_SIZE).getData());
                dataOffset += BLOCK_SIZE;
            }
        }

        System.out.println("----READ---OUTPUT------");
        System.out.println(data);
    }


    private void putBlocksIntoMap(int n, TreeMap<Integer, Block> map) throws NoBlocksAvailableException {
        int lastBlockPosition;
        if (!map.isEmpty()) {
            lastBlockPosition = map.lastKey();
        } else lastBlockPosition = -BLOCK_SIZE;
        for (int i = 0; i < n; i++) {
            map.put(lastBlockPosition + BLOCK_SIZE, new Block(NOT_INITIALIZED_DATA_FILLER.repeat(BLOCK_SIZE), fileSystem.bitMap.takeFreeBlock()));
            lastBlockPosition += BLOCK_SIZE;
        }
    }


    private int getHowManyBlocksLeft(int dataLeft) {
        float dBlocks = (float) dataLeft / BLOCK_SIZE;
        if (dBlocks < 1) {
            return 1;
        } else {
            return (int) Math.ceil(dBlocks);
        }
    }

    private FileDescriptor findFileDescriptorByFd(int fd) throws NoOpenedFilesWithSuchNumberException {
        FileDescriptor fileDescriptor = fileSystem.getOpenedFiles().get(fd);
        if (fileDescriptor == null) throw new NoOpenedFilesWithSuchNumberException();
        return fileDescriptor;
    }

    public void write(int fd, int offset, int size, String data) throws NoOpenedFilesWithSuchNumberException, NoBlocksAvailableException, DataLengthLessThanRequestedSizeException {
        if (size > data.length()) throw new DataLengthLessThanRequestedSizeException();
        FileDescriptor fileDescriptor = findFileDescriptorByFd(fd);
        int initialBlockPosition = (offset / BLOCK_SIZE) * BLOCK_SIZE;
        int offsetFromInitialBlock = offset - initialBlockPosition;

        TreeMap<Integer, Block> blocks = fileDescriptor.getBlocksMap();
        int additionalBlocksAmount = 0;
        if (blocks.size() < (offset / BLOCK_SIZE) + 1) {
            additionalBlocksAmount += (offset / BLOCK_SIZE + 1) - blocks.size();
            System.out.println("add1 = " + additionalBlocksAmount);
        }
        System.out.println("getHowMany = " + getHowManyBlocksLeft(offset + size));
        if (getHowManyBlocksLeft(offset + size) > blocks.size()) {
            additionalBlocksAmount += getHowManyBlocksLeft(offset + size) - (blocks.size() + additionalBlocksAmount);
            System.out.println("add3 = " + (getHowManyBlocksLeft(offset + size) - blocks.size()));
            System.out.println("add4 = " + (getHowManyBlocksLeft(offset + size)));
        }
        if (fileSystem.bitMap.areBlocksAvailable(additionalBlocksAmount)) {
            putBlocksIntoMap(additionalBlocksAmount, blocks);
            System.out.println("add2 = " + additionalBlocksAmount);
        } else throw new NoBlocksAvailableException();

        Block currentBlock = blocks.get(initialBlockPosition);
        System.out.println("add blocks amount = " + additionalBlocksAmount);
        System.out.println("blocks = " + blocks.keySet());
        System.out.println("curr block = " + currentBlock);
        System.out.println("intial bolck pos = " + initialBlockPosition);
        System.out.println("offset fomr init block = " + offsetFromInitialBlock);

        if (BLOCK_SIZE - offsetFromInitialBlock >= size) {
            currentBlock.setData(currentBlock.getData().substring(0, offsetFromInitialBlock) + data.substring(0, size));
            return;
        }

        int dataOffset = BLOCK_SIZE - offsetFromInitialBlock;
        int dataLeft = size - dataOffset;
        System.out.println("dataLeft = " + dataLeft);

        currentBlock.setData(currentBlock.getData().substring(0, offsetFromInitialBlock) + data.substring(0, BLOCK_SIZE - offsetFromInitialBlock));
        int blocksLeft = getHowManyBlocksLeft(dataLeft);
        int currentBlockPosition = initialBlockPosition + BLOCK_SIZE;
        for (int i = 0; i < blocksLeft; i++) {
            if (i != blocksLeft - 1) {
                System.out.println("blocks = " + blocks);
                System.out.println("currentBlockPosition = " + currentBlockPosition);
                blocks.get(currentBlockPosition).setData(data.substring(dataOffset, dataOffset + BLOCK_SIZE));
                currentBlockPosition += BLOCK_SIZE;
                dataOffset += BLOCK_SIZE;
                dataLeft -= BLOCK_SIZE;
            } else {
                System.out.println("Data left !!! = " + dataLeft);
                System.out.println("Data offset !!! = " + dataOffset);
                blocks.get(currentBlockPosition).setData(data.substring(dataOffset, dataOffset + dataLeft));
            }
        }

        fileDescriptor.setSize(blocks.values().stream().map(Block::getSize).reduce(Integer::sum).get());
        System.out.println("SIZEEEE" + blocks.values().stream().map(Block::getSize).reduce(Integer::sum).get());
        System.out.println("CHAsdanfoadjngidsngsngijsgjngj;ogj idjg dfjingdf;j d = " + fileDescriptor.getSize());
    }


    public void link(String name1, String name2) throws NoFileWithSuchNameException {
        FileDescriptor dirDesc = getDirectoryDescriptor(this.fileSystem);
        Map<String, Integer> dirLinks = dirDesc.getDirectoryLinks();
        Integer fileDescId = dirLinks.get(name1);
        if (fileDescId == null) throw new NoFileWithSuchNameException();

        dirLinks.put(name2, fileDescId);
        FileDescriptor fileDesc = getFileDescriptorById(fileDescId);
        fileDesc.setLinksNumber(fileDesc.getLinksNumber() + 1);
    }

    public void unlink(String name) throws NoFileWithSuchNameException {
        FileDescriptor dirDesc = getDirectoryDescriptor(this.fileSystem);
        Map<String, Integer> dirLinks = dirDesc.getDirectoryLinks();
        Integer descId = dirLinks.get(name);
        if (descId == null) throw new NoFileWithSuchNameException();
        dirLinks.remove(name);
        FileDescriptor fileDesc = getFileDescriptorById(descId);
        fileDesc.setLinksNumber(fileDesc.getLinksNumber() - 1);
        clearIfNoLinksAndNotOpened(getFileDescriptorById(descId));
    }

    private FileDescriptor getFileDescriptorById(Integer id) {
        return this.fileSystem.getDescriptors().stream()
                .filter(desc -> Objects.equals(desc.getId(), id))
                .findFirst().orElse(null);
    }

    private FileDescriptor getFileDescriptorByName(String name) {
        FileDescriptor dirDesc = getDirectoryDescriptor(this.fileSystem);
        Integer fileDescNumber = dirDesc.getDirectoryLinks().get(name);
        return getFileDescriptorById(fileDescNumber);
    }

    public void truncate(String name, int size) throws NoFileWithSuchNameException, NoBlocksAvailableException {
        if (size <= 0) return;
        FileDescriptor fileDescriptor = getFileDescriptorByName(name);
        if (fileDescriptor == null) throw new NoFileWithSuchNameException();

        Block block;
        TreeMap<Integer, Block> blocksMap = fileDescriptor.getBlocksMap();
        if (size > fileDescriptor.getSize()) {
            int dataLeft = size - fileDescriptor.getSize();
            block = blocksMap.lastEntry().getValue();
            dataLeft -= BLOCK_SIZE - block.getSize();
            block.setData(block.getData() + NOT_INITIALIZED_DATA_FILLER.repeat(BLOCK_SIZE - block.getSize()));
            if (dataLeft == 0) {
                fileDescriptor.setSize(size);
                return;
            }
            putBlocksIntoMap(getHowManyBlocksLeft(dataLeft), blocksMap);
        }
        if (size < fileDescriptor.getSize()) {
            int blockPosition = (size / BLOCK_SIZE) * BLOCK_SIZE;
            block = blocksMap.get(blockPosition);
            System.out.println("MY_BEATY_BLOG" + block);
            block.setData(block.getData().substring(0, size - blockPosition));
            for (int pos = blockPosition + BLOCK_SIZE; pos < blocksMap.lastKey(); pos += BLOCK_SIZE) {
                blocksMap.remove(pos);
                fileSystem.bitMap.makeBlockFree(pos);
            }
            fileDescriptor.setSize(size);
        }
    }
}
