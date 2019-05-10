import java.net.*;
import java.util.Arrays;
import java.io.*;

public class Client extends Thread {
    // 上传的文件保存路径，可在main中修改
    private String dir;
    // socket服务器地址和端口号
    private String host;
    private int port;

    int bufferSize = 1024 * 1024 * 1;// 设置缓冲区大小为10M
    String uploadUrl = "D:\\study file\\Third\\operate system\\proj_school\\upload\\";
    String fileQue[] = readDirectory(uploadUrl, bufferSize);// 读入待传输文件
    static int index = 0;// 待传输队列指针
    //private String fileName;// 当前传输的文件名

    public Client(String dir, String host, int port) {
        this.dir = dir;

        this.host = host;

        this.port = port;
    }

    @Override
    public void run() {
        new SendThread().start();
    }

    // 将 int 转成字节
    public static byte[] i2b(int i) {
        return new byte[]{(byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF), (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)};
    }


    private class SendThread extends Thread {
        String filePath;

        @Override
        public void run() {
            try {
                Socket socket = new Socket(host, port);// 设置socket，建立连接connected
                DataOutputStream output = new DataOutputStream(
                        socket.getOutputStream());// socket的数据输出流
                DataOutputStream rubOs = new DataOutputStream(new FileOutputStream("D:\\study file\\Third\\operate system\\proj_school\\download\\rubbish.txt"));// socket的数据输出流
                DataInputStream getAck = new DataInputStream(
                        socket.getInputStream());// 设置socket的输入流
                String fileName = null;// 当前传输的文件名
                long startTime = System.currentTimeMillis();
                int blockNum = 1, blockIndex = 1;//分片数、当前分片所在位置
                do {

                    /**
                     * 分配任务
                     */
                    fileName = request_align();
                    //blockIndex=getBlockIndex();
                    if (fileName == null)
                        break;
                    filePath = dir + fileName;
                    File file = new File(filePath);

                    /**
                     * 传输通讯
                     */
                    // 将文件长度传输过去
                    int filelen = (int) file.length();

                    System.out.println("发送文件：" + file.getName() + ",长度:"
                            + filelen);

                    // 发送文件名和文件内容
                    writeFileName(file, output);

                    /**
                     * 被切片的文件
                     */
                    if (filelen == 0) {
                        ParseFileName pFName = new ParseFileName(fileName);
                        fileName = pFName.fileName;
                        blockIndex = pFName.blockIndex;
                        filePath = dir + fileName;
                        file = new File(filePath);
                        filelen = (int) file.length();
                    }
                    DataInputStream input = new DataInputStream(
                            new FileInputStream(filePath));// client的数据输入流为指定文件
                    writeFileContent(input, output, filelen, blockIndex, rubOs);

                    /**
                     * 结束此次任务
                     */
                    if (!getAck.readUTF().equals("OK"))// 如果从server传过来的确认信号不是ok
                    {
                        System.out.println("服务器" + host + ":" + port + "失去连接！");
                        break;
                    }
                    input.close();
                } while (true);
                System.out.println(this.getName() + "线程任务完成，耗时："
                        + (System.currentTimeMillis() - startTime) + "ms");
                output.close();

                socket.close();

                getAck.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // 输出文件内容
        private void writeFileContent(InputStream is, OutputStream os,
                                      int length, int blockIndex, OutputStream rbos) throws IOException {
            //切片输出
            if (length > bufferSize) {
                int blockNum = length / bufferSize + 1;
                //最后一片特殊处理
                if (blockIndex == blockNum) length = length - bufferSize * (blockNum - 1);
                else length = bufferSize;
            }
            // 输出文件长度
            os.write(i2b(length));

            // 输出文件内容
            //byte[] buffer = new byte[4096];
            byte[] buffer = new byte[bufferSize];
//			int size;
//			while ((size = is.read(buffer)) != -1) {
//				os.write(buffer, 0, size);
//			}
//			int count = 0;
//	        while (count < length) {
//	        	int off=(blockIndex-1)*bufferSize;
//	        	System.out.println(""+off+","+length);
//	            int n = is.read(buffer,off,length);
//	            // 这里没有考虑 n = -1 的情况
//	            os.write(buffer, 0, n);
//	            count += n;
//	        }

            int[] size = new int[100];
            int i = 1;
            while ((size[i] = is.read(buffer)) != -1) {
                if (i == blockIndex) {
                    os.write(buffer, 0, size[i]);
                    //System.out.println(""+i+","+size[i]);
                    i++;
                    break;
                } else rbos.write(buffer, 0, size[i]);
                i++;
            }


        }

        // 输出文件名
        private void writeFileName(File file, OutputStream os)
                throws IOException {
            byte[] fn_bytes = file.getName().getBytes();

            os.write(i2b(fn_bytes.length)); // 输出文件名长度
            os.write(fn_bytes); // 输出文件名
        }
    }

    // 从入待传输任务队列请求分配任务传输
    String request_align() {
        String fileName = null;
        if (index < fileQue.length - 1) {
            fileName = fileQue[index];
            index++;
        }
        return fileName;
    }


    // 从指定位置读取待传输文件入待传输任务队列
    String[] readDirectory(String dirname, int bufferSize) {
        File f1 = new File(dirname);
        int M_num = 100;// 假设任务队列最多可容纳100个任务
        String[] fileQue = new String[M_num];
        int i = 0;
        for (File file : f1.listFiles()) {
            int blockNum = (int) file.length() / bufferSize + 1;// 分片数
            if (blockNum == 1) {// 1代表无需分片
                fileQue[i] = file.getName();
                i++;
            } else {// 分片，实际上是对文件名做了处理
                for (int j = 1; j <= blockNum; j++, i++) {
                    fileQue[i] = file.getName() + "." + j + "." + blockNum;
                }
            }

        }
        String[] s = (String[]) Arrays.copyOf(fileQue, i);
        return s;
    }
}
