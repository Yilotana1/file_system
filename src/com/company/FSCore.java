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
            System.out.println("name = " + fileInfo.getKey() + "; id = " + fileInfo.getValue());
        }
    }


    public void create(String name) throws Exception {
        FileDescriptor fileDesc = new FileDescriptor(FileType.REGULAR);
        fileSystem.getDescriptors().add(fileDesc);

        // Find single folder's descriptor in filesystem
        FileDescriptor dirDesc = getDirectoryDescriptor(fileSystem);
        dirDesc.getDirectoryLinks().put(name, fileDesc.getId());
    }

    // descriptors number begins from 1
    public int open(String name) throws NoFileWithSuchNameException {
        FileDescriptor fileDescriptor = findDescriptorByName(name);

        int newFd = generateNewFd();
        fileSystem.getOpenedFiles().put(newFd, fileDescriptor);
        return newFd;
    }

    public void close(int fd) {
        FileDescriptor fileDescriptor = fileSystem.getOpenedFiles().get(fd);
        fileSystem.getOpenedFiles().remove(fd);
        clearIfNoLinksAndNotOpened(fileDescriptor);
    }

    public void read(int fd, int offset, int size) throws NoOpenedFilesWithSuchNumberException, DataLengthLessThanRequestedSizeException {
        FileDescriptor fileDescriptor = findFileDescriptorByFd(fd);
        if (offset + size > fileDescriptor.getSize()) {
            throw new DataLengthLessThanRequestedSizeException();
        }
        Map<Integer, Block> blocks = fileDescriptor.getBlocksMap();
        int firstBlockToRead = (offset / BLOCK_SIZE) * BLOCK_SIZE;
        int offsetFromFirstBlockToRead = offset - firstBlockToRead;

        StringBuilder data = new StringBuilder();
        if (BLOCK_SIZE - offsetFromFirstBlockToRead >= size) {
            data.append(blocks.get(firstBlockToRead).getData(), offsetFromFirstBlockToRead, offsetFromFirstBlockToRead + size);
            System.out.println(data);
            return;
        }
        data.append(blocks.get(firstBlockToRead).getData().substring(offsetFromFirstBlockToRead));
        int blocksLeft = getHowManyBlocksLeft(size - (BLOCK_SIZE - offsetFromFirstBlockToRead));
        int dataOffset = BLOCK_SIZE - offsetFromFirstBlockToRead;
        if (blocksLeft != 0) {
            for (int i = 1; i <= blocksLeft; i++) {
                if (i == blocksLeft) {
                    data.append(blocks.get(firstBlockToRead + i * BLOCK_SIZE).getData(), 0, size - dataOffset);
                    break;
                }
                data.append(blocks.get(firstBlockToRead + i * BLOCK_SIZE).getData());
                dataOffset += BLOCK_SIZE;
            }
        }
        System.out.println(data);
    }

    public void write(int fd, int offset, int size, String data) throws NoOpenedFilesWithSuchNumberException, NoBlocksAvailableException, DataLengthLessThanRequestedSizeException {
        if (size > data.length()) throw new DataLengthLessThanRequestedSizeException();
        FileDescriptor fileDescriptor = findFileDescriptorByFd(fd);
        int firstBlockToWritePosition = (offset / BLOCK_SIZE) * BLOCK_SIZE;
        int offsetFromFirstBlockToWrite = offset - firstBlockToWritePosition;

        TreeMap<Integer, Block> blocks = fileDescriptor.getBlocksMap();
        int newBlocksToCreate = 0;
        if (blocks.size() < (offset / BLOCK_SIZE) + 1) {
            newBlocksToCreate += (offset / BLOCK_SIZE + 1) - blocks.size();
        }
        if (getHowManyBlocksLeft(offset + size) > blocks.size()) {
            newBlocksToCreate += getHowManyBlocksLeft(offset + size) - (blocks.size() + newBlocksToCreate);
        }
        if (fileSystem.bitMap.areBlocksAvailable(newBlocksToCreate)) {
            putBlocksIntoMap(newBlocksToCreate, blocks);
        } else throw new NoBlocksAvailableException();

        Block currentBlock = blocks.get(firstBlockToWritePosition);
        if (BLOCK_SIZE - offsetFromFirstBlockToWrite >= size) {
            currentBlock.setData(currentBlock.getData().substring(0, offsetFromFirstBlockToWrite) + data.substring(0, size));
            return;
        }
        int dataOffset = BLOCK_SIZE - offsetFromFirstBlockToWrite;
        int dataLeft = size - dataOffset;
        currentBlock.setData(currentBlock.getData().substring(0, offsetFromFirstBlockToWrite) + data.substring(0, BLOCK_SIZE - offsetFromFirstBlockToWrite));
        int blocksLeft = getHowManyBlocksLeft(dataLeft);
        int currentBlockPosition = firstBlockToWritePosition + BLOCK_SIZE;
        for (int i = 0; i < blocksLeft; i++) {
            if (i != blocksLeft - 1) {
                blocks.get(currentBlockPosition).setData(data.substring(dataOffset, dataOffset + BLOCK_SIZE));
                currentBlockPosition += BLOCK_SIZE;
                dataOffset += BLOCK_SIZE;
                dataLeft -= BLOCK_SIZE;
            } else {
                blocks.get(currentBlockPosition).setData(data.substring(dataOffset, dataOffset + dataLeft));
            }
        }
        fileDescriptor.setSize(blocks.values().stream().map(Block::getSize).reduce(Integer::sum).get());
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
            block.setData(block.getData().substring(0, size - blockPosition));
            for (int pos = blockPosition + BLOCK_SIZE; pos < blocksMap.lastKey(); pos += BLOCK_SIZE) {
                blocksMap.remove(pos);
                fileSystem.bitMap.makeBlockFree(pos);
            }
            fileDescriptor.setSize(size);
        }
    }





    private FileDescriptor getDirectoryDescriptor(FileSystem fileSystem) {
        return fileSystem
                .getDescriptors()
                .stream()
                .filter(desc -> desc.getFileType() == FileType.DIRECTORY)
                .findFirst()
                .get();
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


    private void clearIfNoLinksAndNotOpened(FileDescriptor fileDescriptor) {
        if (fileDescriptor.getLinksNumber() == 0 && !fileSystem.getOpenedFiles().containsValue(fileDescriptor)) {
            this.fileSystem.getDescriptors().remove(fileDescriptor);
            fileDescriptor.getBlocksMap().clear();
            fileSystem.bitMap.clearFileBlocks(fileDescriptor);
        }
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


}
