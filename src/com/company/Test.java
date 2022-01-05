package com.company;

import com.company.exceptions.DataLengthLessThanRequestedSizeException;
import com.company.exceptions.NoFileWithSuchNameException;
import com.company.exceptions.NoOpenedFilesWithSuchNumberException;

public class Test {

    public static void main(String[] args) throws Exception {

        String testData = "Lorem ipsum dolor sit amet, consectetur adipiscing elit," +
                " sed do eiusmod tempor incididunt ut" +
                " labore et dolore magna aliqua.";

        System.out.println("------TEST mount()------\n");
        FSCore fsCore = new FSCore();
        fsCore.mount();
        if (fsCore.fileSystem != null) {
            System.out.println("File system is mounted");
        }

        System.out.println();
        System.out.println("----------TEST create() and fstat()------------\n");

        fsCore.create("text1.txt");
        System.out.println("fstat() output for text1.txt: ");
        fsCore.fstat(2);

        System.out.println();
        System.out.println("----------TEST ls()------------\n");

        fsCore.create("text2.txt");
        System.out.println("ls: show 2 files name and id:");
        fsCore.ls();
        System.out.println();


        System.out.println("----------TEST open(), write() and read()------------\n");

        int fd1 = fsCore.open("text1.txt");

        fsCore.write(fd1, 0, testData.length(), testData);
        System.out.println("Show written data in opened file with fd = " + fd1 + ":");
        fsCore.read(fd1, 0, testData.length());

        System.out.println();

        int offset = 10;
        int size = 23;
        System.out.println("Show written data with offset = " + offset + " and size = " + size + ". Result should be = "
                + testData.substring(offset, offset + size) + ":");
        fsCore.read(fd1, offset, size);

        System.out.println();
        System.out.println("----------TEST link()------------\n");


        int fd2;
        try {
            fd2 = fsCore.open("qwerty.txt");
        } catch (NoFileWithSuchNameException e) {
            System.out.println("This exception appears because we can't open file with unknown name, need to use link() to fix it");
        }

        fsCore.link("text1.txt", "qwerty.txt");
        fd2 = fsCore.open("qwerty.txt");
        System.out.println("File text1.txt was opened and read after using link() with name 'qwerty' and new fd = " + fd2);
        fsCore.read(fd2, 0, 15);


        System.out.println();
        System.out.println("--------TEST unlink()-------\n");


        fsCore.unlink("text1.txt");
        try {
            fsCore.open("text1.txt");
        } catch (NoFileWithSuchNameException e) {
            System.out.println("This exception appears because link 'text1.txt' has been removed, we can't use it to open file but we still can use link 'qwerty.txt'");
        }

        System.out.println();
        System.out.println("--------TEST truncate()-------\n");

        fsCore.truncate("qwerty.txt", testData.length() - 10);
        System.out.println("Size of 'qwerty.txt' file has been changed and" +
                " now it should be equal " + (testData.length() - 10) + "" +
                " and data should look like:\n" + testData.substring(0, testData.length() - 10));

        System.out.println("Current info of 'qwerty.txt' : \n");
        fsCore.fstat(2);
        try {
            fsCore.read(fd2, 0, testData.length());
        } catch (DataLengthLessThanRequestedSizeException e) {
            System.out.println("size was changed so we need to insert other size for reading: ");
            fsCore.read(fd2, 0, testData.length() - 10);
        }

        System.out.println();
        System.out.println("------TEST close()------\n");
        fsCore.close(fd2);
        try {
            fsCore.read(fd2, 0, testData.length() - 10);
        } catch (NoOpenedFilesWithSuchNumberException e) {
            System.out.println("This exception appears because fd = " + fd2 + " isn't connected to file anymore");
        }

        System.out.println();
        System.out.println("------TEST mkfs()-----\n");
        fsCore.mkfs(5);
        System.out.println("There should be no output after call ls()");
        fsCore.ls();

        System.out.println();
        System.out.println("------TEST unmount()-----\n");
        fsCore.unmount();
        if (fsCore.fileSystem == null) {
            System.out.println("File system is unmounted");
        }

    }
}
