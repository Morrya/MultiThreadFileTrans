import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseFileName {
    public String fileName;
    public int blockIndex;
    public int blockNum;

    public ParseFileName(String fileName){
        //对文件名进行解密处理得到文件名和分片数
        String regex = "(.+\\.[a-z]+\\.[0-9]+)";// 匹配分片文件名的正则表达式规则
        Matcher matcher = Pattern.compile(regex).matcher(fileName);
        if (matcher.find()) {// 是分片了的文件，对文件名进行解码处理
            String[] arrOfStr = fileName.split("\\.");
            this.fileName = arrOfStr[0] + "." + arrOfStr[1];
            this.blockIndex = Integer.parseInt(arrOfStr[2]);
            this.blockNum = Integer.parseInt(arrOfStr[3]);
        }
    }
    public boolean isChunked() {
        if (blockNum>1)
            return true;
        else
            return false;
    }
    public boolean isEnd() {
        if (blockIndex == blockNum)
            return true;
        else
            return false;
    }


}
