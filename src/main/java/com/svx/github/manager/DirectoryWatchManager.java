package com.svx.github.manager;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class DirectoryWatchManager {
    private final WatchService watchService;
    private final Map<Path, Long> lastModifiedTimes;

    public DirectoryWatchManager(Path path) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.lastModifiedTimes = new HashMap<>();
        registerPath(path);
    }

    private void registerPath(Path path) throws IOException {
        path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
    }

    public void startWatching() throws IOException {
        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path filePath = ((WatchEvent<Path>) event).context();
                Path fullPath = ((Path) key.watchable()).resolve(filePath);

                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    long lastModifiedTime = Files.getLastModifiedTime(fullPath).toMillis();
                    if (!lastModifiedTimes.containsKey(fullPath) || lastModifiedTimes.get(fullPath) < lastModifiedTime) {
                        lastModifiedTimes.put(fullPath, lastModifiedTime);
                        System.out.println("File modified: " + fullPath);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    System.out.println("File created: " + fullPath);
                    lastModifiedTimes.put(fullPath, Files.getLastModifiedTime(fullPath).toMillis());
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    System.out.println("File deleted: " + fullPath);
                    lastModifiedTimes.remove(fullPath);
                }
            }
            key.reset();
        }
    }
}
