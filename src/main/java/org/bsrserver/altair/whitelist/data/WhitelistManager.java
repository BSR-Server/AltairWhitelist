package org.bsrserver.altair.whitelist.data;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;

import org.slf4j.Logger;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;
import com.alibaba.fastjson2.JSONObject;

import org.bsrserver.altair.whitelist.config.Config;
import org.bsrserver.altair.whitelist.AltairWhitelist;

public class WhitelistManager {
    private final File WHITELIST_FILE;
    private final Map<String, Set<UUID>> whitelist = new HashMap<>();
    private final Logger logger;

    public WhitelistManager(AltairWhitelist altairGreeter) {
        this.logger = altairGreeter.getLogger();
        this.WHITELIST_FILE = new File(altairGreeter.getDataDirectory().toAbsolutePath().toString(), "whitelist.json");

        // init whitelist and start scheduled task
        readFromFile();
        altairGreeter.getScheduledExecutorService().scheduleAtFixedRate(this::scheduledTask, 0, 1, TimeUnit.MINUTES);
    }

    private void saveWhitelistCache(JSONObject whitelistJSONObject) {
        for (String serverName : whitelistJSONObject.keySet()) {
            whitelist.put(
                    serverName,
                    new HashSet<>(whitelistJSONObject.getJSONArray(serverName).toList(UUID.class))
            );
        }
    }

    private void saveToFile() {
        try (FileWriter file = new FileWriter(WHITELIST_FILE)) {
            file.write(JSONObject.toJSONString(whitelist));
            file.flush();
        } catch (IOException e) {
            logger.error("Failed to save whitelist from file.", e);
        }
    }

    private void readFromFile() {
        if (WHITELIST_FILE.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(WHITELIST_FILE))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                saveWhitelistCache(JSONObject.parseObject(stringBuilder.toString()));
            } catch (Exception e) {
                logger.error("Failed to read whitelist from file.", e);
            }
        }
    }

    private void scheduledTask() {
        try {
            updateWhitelist();
        } catch (Exception exception) {
            logger.error("Failed to get whitelist.", exception);
        }
    }

    synchronized public void updateWhitelist() {
        // request
        String authorization = "KeySecuredClient " + Config.getInstance().getBackendSecuredClientKey();
        OkHttpClient client = new OkHttpClient();
        Request getRequest = new Request.Builder()
                .url(Config.getInstance().getBackendBaseUrl() + "/v1/minecraft/whitelist")
                .header("Authorization", authorization)
                .build();

        // get whitelist
        JSONObject whitelistJSONObject = null;
        try (Response response = client.newCall(getRequest).execute()) {
            if (response.body() != null) {
                whitelistJSONObject = JSONObject
                        .parseObject(response.body().string())
                        .getJSONObject("data")
                        .getJSONObject("whitelist");
            }
        } catch (IOException exception) {
            logger.error("Failed to get whitelist.", exception);
        }

        // save whitelist
        if (whitelistJSONObject != null) {
            whitelist.clear();
            saveWhitelistCache(whitelistJSONObject);
            saveToFile();
        }
    }

    public Map<String, Set<UUID>> getWhitelist() {
        return whitelist;
    }
}
