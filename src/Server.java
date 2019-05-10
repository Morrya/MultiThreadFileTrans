import java.net.*;
import java.io.*;

public class Server {
    private int listenPort;// Socket服务器的端口
    private String savePath;

    /**
     * 构造方法
     *
     * @param listenPort 侦听端口
     * @param savePath   接收的文件要保存的路径
     * @throws IOException 如果创建保存路径失败
     */
    Server(int listenPort, String savePath) throws IOException {
        this.listenPort = listenPort;
        this.savePath = savePath;

        File file = new File(savePath);
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("无法创建文件夹 " + savePath);
        }
    }


    // 开始侦听
    public void start() {
        new ListenThread().start();
    }

    // 将字节转成 int。b 长度不得小于 4，且只会取前 4 位。
    public static int b2i(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * 侦听线程
     */
    private class ListenThread extends Thread {

        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(listenPort);
                System.out.println("等待连接");
                // 开始循环
                while (true) {
                    Socket socket = server.accept();
                    System.out.println("A new client is connected : " + socket);
                    new ClientHandler(socket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class ClientHandler extends Thread {
        private Socket socket = null;

        private ClientHandler(Socket s) {

            this.socket = s;

        }

        @Override
        public void run() {
            try {
                DataInputStream input = new DataInputStream(

                        new BufferedInputStream(socket.getInputStream()));
                DataOutputStream ack = new DataOutputStream(socket.getOutputStream());
                do {
                    readAndSave(input, ack);
                } while (true);
            } catch (FileNotFoundException e1) {
                //end
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }

        private void readAndSave(DataInputStream is, DataOutputStream ack) throws IOException {

            String filename = getFileName(is);
            int file_len = readInteger(is);
            System.out.println("接收文件：" + filename + "，长度：" + file_len);

            readAndSave0(is, savePath + filename, file_len);
            ParseFileName pFile = new ParseFileName(filename);
            if (pFile.isChunked()) {
                if (pFile.isEnd())
                    mergeFile(pFile.fileName, pFile.blockNum);
            }
            ack.writeUTF("OK");// 读完数据后给client一个ok回复

            System.out.println("文件保存成功（" + file_len + "字节）。");
        }

        void mergeFile(String fileName, int blockNum) {
            fileName = savePath + fileName;
            String filePath = fileName;//fileName  .doc
            int count = 1;
            try {

                FileOutputStream os = getFileOS(filePath);
                while (true) {
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

        private void readAndSave0(DataInputStream is, String path, int file_len) throws IOException {
            FileOutputStream os = getFileOS(path);
            readAndWrite(is, os, file_len);
            os.close();
        }

        // 边读边写，直到读取 size 个字节
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

        // 读取文件名
        private String getFileName(InputStream is) throws IOException {
            int name_len = readInteger(is);
            byte[] result = new byte[name_len];
            is.read(result);
            return new String(result);
        }

        // 读取一个数字
        private int readInteger(InputStream is) throws IOException {
            byte[] bytes = new byte[4];
            is.read(bytes);
            return b2i(bytes);
        }

        // 创建文件并返回输出流
        private FileOutputStream getFileOS(String path) throws IOException {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }

            return new FileOutputStream(file);
        }
    }
}
