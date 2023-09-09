package cn.edu.cdut.navidemo3;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class WriteData2CSVThread extends Thread {
    StringBuilder data;
    String fileName;
    String folder;
    //StringBuilder sb;
    public WriteData2CSVThread(StringBuilder data, String folder, String fileName) {
        this.data = data;
        this.folder = folder;
        this.fileName = fileName;
    }
    private void createFolder() {
        File fileDir = new File(folder);
        boolean hasDir = fileDir.exists();
        if (!hasDir) {
            fileDir.mkdirs();// 这里创建的是目录
        }
    }
    @Override
    public void run() {
        super.run();
        createFolder();
        File eFile = new File(folder + File.separator + fileName);
        if (!eFile.exists()) {
            try {
                boolean newFile = eFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //FileOutputStream(file,true)是追加，不会覆盖之前写入的内容。
            FileOutputStream os = new FileOutputStream(eFile, true);

            os.write(data.toString().getBytes());
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}