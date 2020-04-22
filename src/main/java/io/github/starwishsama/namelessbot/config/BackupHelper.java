package io.github.starwishsama.namelessbot.config;

import cn.hutool.core.io.file.FileWriter;
import com.google.gson.Gson;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.BotMain;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupHelper {
    private static final File backupLocation = new File(BotMain.getJarPath() + "/backups");

    public static void createBackup(){
        if (!backupLocation.exists()){
            backupLocation.mkdirs();
        }

        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));

        FileWriter.create(new File(backupLocation, fileName + ".json")).write(new Gson().toJson(BotConstants.users));
        BotMain.getLogger().log("[备份] 自动备份完成");
    }
}
