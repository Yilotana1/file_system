package com.company;

public class Main {

    public static void main(String[] args) throws Exception {

        FSCore fsCore = new FSCore();
        fsCore.mount();

        fsCore.create("test.txt");
        int fd = fsCore.open("test.txt");
        System.out.println(fd);
        String data = "Hello world!";
        System.out.println("DATA LENGTH = " + data.length());
//        fsCore.write(fd, 7, 11, data);
        System.out.println("-----WRITE------");
        fsCore.write(fd, 0, data.length(), data);
        System.out.println("------READ------");
//        fsCore.read(fd, 8, 12);
//        fsCore.read(fd, 0, 12);
        System.out.println("--------READ_--------");
//        fsCore.read(fd, 7, 4);
//        fsCore.read(fd, 5, 8);
        fsCore.close(fd);
        fsCore.link("test.txt", "test1.txt");
        int fd1 = fsCore.open("test1.txt");
//        fsCore.unlink("test1.txt");
        fsCore.read(fd1, 0, data.length());
        int fd2 = fsCore.open("test1.txt");
        fsCore.read(fd2, 0, data.length());
        fsCore.truncate("test1.txt", 10);
        System.out.println("----TEST----TRUNCATE--------!!!");
        System.out.println("----TEST----TRUNCATE--------!!!");
        System.out.println("----TEST----TRUNCATE--------!!!");
        fsCore.read(fd1, 0, 10);

        fsCore.ls();
        fsCore.fstat(2);
        fsCore.unmount();


//        FSCore fsCore = new FSCore();
//        fsCore.unmount();

//
//        BitMap bitMap = BitMap.getInstance();
//        System.out.println(bitMap);
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println();
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        System.out.println(bitMap.takeFreeBlock());
//        System.out.println(bitMap);
//
//        bitMap.makeBlockFree(9);
//        System.out.println(bitMap);


    }
}
