package net.myitian.boat_maneuverability_enhancing;
/*
 * Copyright (c) 2021 magistermaks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

public class SimpleConfig {
    private static final Logger LOGGER = LogManager.getLogger("BoatManeuverabilityEnhancing/SimpleConfig");
    private final HashMap<String, String> config = new HashMap<>();

    private SimpleConfig(ConfigRequest request) {
        String identifier = "Config '" + request.filename + "'";
        if (!request.file.exists()) {
            LOGGER.info(identifier + " is missing, generating default one...");
            try {
                // create config
                // try creating missing files
                request.file.getParentFile().mkdirs();
                Files.createFile(request.file.toPath());
                // write default config data
                PrintWriter writer = new PrintWriter(request.file, StandardCharsets.UTF_8.toString());
                writer.write(request.getConfig());
                writer.close();
                try {
                    // load config
                    Scanner reader = new Scanner(request.file);
                    for (int line = 1; reader.hasNextLine(); line++) {
                        String entry = reader.nextLine();
                        if (!entry.isEmpty() && !entry.startsWith("#")) {
                            String[] parts = entry.split("=", 2);
                            if (parts.length == 2) {
                                config.put(parts[0], parts[1].split("#")[0]);
                            } else {
                                throw new RuntimeException("Syntax error in config file on line " + line + "!");
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(identifier + " failed to load!");
                    LOGGER.trace(e);
                }
            } catch (IOException e) {
                LOGGER.error(identifier + " failed to generate!");
                LOGGER.trace(e);
            }
        }
    }

    /**
     * Creates new config request object, ideally `namespace`
     * should be the name of the mod id of the requesting mod
     *
     * @param filename - name of the config file
     * @return new config request object
     */
    public static ConfigRequest of(String filename) {
        Path path = FabricLoader.getInstance().getConfigDir();
        return new ConfigRequest(path.resolve(filename + ".toml").toFile(), filename);
    }

    /**
     * Returns double value from config corresponding to the given
     * key, or the default string if the key is missing or invalid.
     *
     * @return value corresponding to the given key, or the default value
     */
    public double getOrDefault(String key, double def) {
        try {
            return Double.parseDouble(config.get(key));
        } catch (Exception e) {
            return def;
        }
    }

    public interface DefaultConfig {
        static String empty(String namespace) {
            return "";
        }

        String get(String namespace);
    }

    public static class ConfigRequest {
        private final File file;
        private final String filename;
        private DefaultConfig provider;

        private ConfigRequest(File file, String filename) {
            this.file = file;
            this.filename = filename;
            this.provider = DefaultConfig::empty;
        }

        /**
         * Sets the default config provider, used to generate the
         * config if it's missing.
         *
         * @param provider default config provider
         * @return current config request object
         * @see DefaultConfig
         */
        public ConfigRequest provider(DefaultConfig provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Loads the config from the filesystem.
         *
         * @return config object
         * @see SimpleConfig
         */
        public SimpleConfig request() {
            return new SimpleConfig(this);
        }

        private String getConfig() {
            return provider.get(filename) + "\n";
        }
    }
}
