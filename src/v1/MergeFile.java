package v1;

import java.io.*;

public class MergeFile extends Thread{
    String fileName;
    int blockNum;
    String server = "127.0.0.1";
    String savePath = "D:\\study file\\Third\\operate system\\proj_school\\download\\" + server + "\\";
    public MergeFile(String fileName, int blockNum){
        this.fileName=fileName;
        this.blockNum=blockNum;
    }
    @Override
    public  void run(){
        fileName = savePath + fileName;
        String filePath = fileName;//fileName  .doc
        int count = 1;
        try {

            FileOutputStream os = getFileOS(filePath);
            while (count<=blockNum) {
                String filename = fileName + "." + count + "." + blockNum;//.doc.1
                File file = new File(filename);
                DataInputStream is = new DataInputStream(

                        new FileInputStream(filename));
                //readAndSave0(is,filePath,(int)file.length());
                readAndWrite(is, os, (int) file.length());
                is.close();
                count += 1;
                file.delete();
            }
        } catch (FileNotFoundException e1) {
            System.out.println(fileName);
        } catch (IOException e) {

        }
    }

    // 创建文件并返回输出流
    private FileOutputStream getFileOS(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        return new FileOutputStream(file);
    }
    private void readAndWrite(InputStream is, FileOutputStream os, int size) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        while (count < size) {
            int n = is.read(buffer);
            // 这里没有考虑 n = -1 的情况
            os.write(buffer, 0, n);
            count += n;
        }
    }
}
