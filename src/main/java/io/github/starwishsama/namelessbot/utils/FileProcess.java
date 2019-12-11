package io.github.starwishsama.namelessbot.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * @author 夕橘子-O & Stiven.ding
 * @see https://www.cnblogs.com/XiOrang/p/5652875.html
 */

@Deprecated
public class FileProcess {
    /**
     * 创建文件
     *
     * @param path        文件路径，如 C:\a.txt
     * @param filecontent 文件内容
     * @return 是否创建成功，成功则返回true
     */

    public static boolean createFile(String path, String filecontent) {

        Boolean bool = false;
        File file = new File(path);
        if (file.exists())
            delFile(path);
        System.out.print("[JSON] Path is " + path + "\n");
        try {
            file.createNewFile();
            bool = true;
            System.out.print("[JSON] Create file successfully at " + path + "\n");
            writeFileContent(path, filecontent);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("[JSON] Encountered an error when saving configuration!");
        }

        return bool;
    }

    /**
     * 按行读取文件
     *
     * @return 读取到的内容（仅支持单行）
     * @throws IOException
     */

    public static String readFile(String path) throws IOException {
        File file = new File(path);
        String tempString = "";
        // 判断文件是否存在
        if (!file.exists()) {
            System.out.println(path + "  文件不存在.");
            throw new IOException("File not exist");
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            tempString = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException localIOException1) {
                }
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException localIOException2) {
                }
        }
        return tempString;
    }

    /**
     * 向文件中写入内容 正常情况不应调用
     *
     * @param filepath 文件路径与名称
     * @param newstr   写入的内容
     * @throws IOException
     */
    private static boolean writeFileContent(String filepath, String newstr) throws IOException {
        Boolean bool = false;
        String filein = newstr + "\r\n";// 新写入的行，换行
        String temp = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            File file = new File(filepath);// 文件路径(包括文件名称)
            // 将文件读入输入流
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();

            // 文件原有内容
            for (; (temp = br.readLine()) != null;) {
                buffer.append(temp);
                // 行与行之间的分隔符 相当于“\n”
                buffer = buffer.append(System.getProperty("line.separator"));
            }
            buffer.append(filein);
            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buffer.toString().toCharArray());
            pw.flush();
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 不要忘记关闭
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return bool;
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return
     */
    public static boolean delFile(String path) {
        Boolean bool = false;
        File file = new File(path);
        try {
            if (file.exists()) {
                file.delete();
                bool = true;
            }
        } catch (Exception e) {
        }
        return bool;
    }

}
