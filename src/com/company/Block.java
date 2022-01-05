package com.company;

import java.io.Serializable;

public class Block implements Serializable {

//    private Map<String, Integer> folderLinks = new HashMap<>();

//    private List<Byte> blocksIds = new ArrayList<>();

    private String data;

    private final int id;
//    public Map<String, Integer> getFolderLinks() {
//        return folderLinks;
//    }

//    public List<Byte> getBlocksIds() {
//        return blocksIds;
//    }

    public String getData() {
        return data;
    }

    public void setData(String blockData) {
        this.data = blockData;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return data.length();
    }

    public Block(String data, int id) {
        this.data = data;
        this.id = id;
    }

//    public void setBlocks(Map<Byte, Block> blocks) {
//        this.blocks = blocks;
//    }
//

//    public void setNewBlock(byte blockId) {
//        blocksIds.add(blockId);
//    }

//    public void setNewFolderLink(String name, int descId) {
//        folderLinks.put(name, descId);
//    }

//    public void setFolderLinks(Map<String, Integer> folderLinks) {
//        this.folderLinks = folderLinks;
//    }
}
