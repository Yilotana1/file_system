//package com.company;
//
//import java.util.List;
//import java.util.Map;
//
////Class providing methods for calculation data in java structures
//public class MemCalcUtil {
//
//    //Reference in java takes 4 bytes memory
//    public static final int REFERENCE_BYTES = 4;
//
//    //4 bytes (link) + 4 bytes (primitive) + 8 bytes (object's header) + 4 bytes (alignment)
//    public static final int INTEGER_WRAP_BYTES = 20;
//
//    //4bytes (link) + 1 bytes (primitive) + 8 bytes (object's header) + 4 bytes (alignment)
//    public static final int BYTE_WRAP_BYTES = 13;
//
//
//    //Calculation for map kind of Map<String, Integer>
//    public static int getMapDataSize(Map<String, Integer> map) {
//        int size = 0;
//        for (Map.Entry<String, Integer> entry : map.entrySet()) {
//            size += getStringDataSize(entry.getKey()) + INTEGER_WRAP_BYTES;
//        }
//
//        return size;
//    }
//
//    //Calculation for list kind of List<Byte>
//    public static int getBlocksIdsListSize(List<Byte> list) {
//        return list.size() * BYTE_WRAP_BYTES;
//    }
//
//
//    public static int getStringDataSize(String string) {
//        if (string == null) return 0;
//        return string.getBytes().length + REFERENCE_BYTES;
//    }
//
//    public static int getFileSize(FileDescriptor fileDescriptor) {
//        int blocksNumber = 0;
//        Map<Byte, Block> blocksMap = fileDescriptor.getBlocksMap();
//        blocksNumber += blocksMap.size();
//        if (blocksNumber > FileSystem.MAX_EXPLICIT_BLOCK_LINKS) {
//            blocksNumber += blocksMap.get((byte) (blocksNumber - 1)).getBlocksIds().size();
//        }
//
//        return blocksNumber * FileSystem.BLOCK_SIZE;
//    }
//
//
//}
