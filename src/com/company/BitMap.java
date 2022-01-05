package com.company;

import com.company.exceptions.NoBlocksAvailableException;

import java.io.Serializable;
import java.util.Map;

//BitMap provides interface for free blocks managing
public class BitMap implements Serializable {


    public static final int BLOCK_SIZE = 5;

    public static final int MAX_BLOCKS_NUMBER = 50;
    private int bitmap;

    private static boolean isCreated = false;
    private static BitMap instance;


    //Singleton for creating single instance of bitmap
    private BitMap() {
    }

    public static BitMap getInstance() {
        if (!isCreated) {
            isCreated = true;
            instance = new BitMap();
        }
        return instance;
    }

    //Return index of taken block or -1 if there is no free block anymore
    public int takeFreeBlock() throws NoBlocksAvailableException {
        for (int i = 0; i < MAX_BLOCKS_NUMBER; i++) {

            if ((this.bitmap & (int) Math.pow(2, i)) == 0) {
                this.bitmap = (this.bitmap | (int) Math.pow(2, i));
                return i + 1;
            }
        }
        throw new NoBlocksAvailableException();
    }

    public boolean areBlocksAvailable(int n) {
        int counter = 0;
        for (int i = 0; i < MAX_BLOCKS_NUMBER; i++) {
            if (((int) Math.pow(2, i) & bitmap) == 0) {
                counter++;
            }
        }
        return counter >= n;
    }


    public void clearFileBlocks(FileDescriptor fileDescriptor) {
        Map<Integer, Block> blocks = fileDescriptor.getBlocksMap();
        blocks.values().forEach(block -> this.makeBlockFree(block.getId()));
    }

    public void makeBlockFree(int blockId) {
        this.bitmap = (short) (this.bitmap & ~(1 << blockId - 1));
    }

    @Override
    public String toString() {
        return Integer.toBinaryString(this.bitmap);
    }
}
