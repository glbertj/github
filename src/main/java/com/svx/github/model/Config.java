package com.svx.github.model;

import com.svx.github.utility.ConfigReader;
import java.io.File;
import java.util.Map;

public class Config {
    private final Map<String, String> configData;

    public Config(File configFile) {
        configData = ConfigReader.readConfig(configFile);
    }

    public String getValue(String section, String key) {
        return configData.get(section + "." + key);
    }
}
