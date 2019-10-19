package com.scurab.dev.server

import java.util.concurrent.TimeUnit
import java.io.File
import java.nio.file.*


class FileWatcher {
    private var isRunning = true

    fun start(folders: Array<File>, callback: (Path) -> Unit) {
        Thread { startChecking(folders, callback) }.start()
    }

    fun stop() {
        isRunning = false
    }

    private fun startChecking(folders: Array<File>, callback: (Path) -> Unit) {
        val watcher = FileSystems.getDefault().newWatchService()
        folders.forEach { folder ->
            val path = folder.toPath()
            path.register(
                watcher,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE
            )
            Files.walk(path)
                .filter { Files.isDirectory(it) }
                .forEach {
                    it.register(
                        watcher,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE
                    )
                }
        }

        while (isRunning) {
            val key: WatchKey?
            try {
                key = watcher.poll(50, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                continue
            }

            if (key == null) {
                Thread.yield()
                continue
            }

            for (event in key.pollEvents()) {
                val kind = event.kind()

                val ev = event as WatchEvent<Path>
                val filename = ev.context()

                if (kind === StandardWatchEventKinds.OVERFLOW) {
                    continue
                } else if (kind === StandardWatchEventKinds.ENTRY_MODIFY) {
                    callback(filename)
                }
                val valid = key.reset()
                if (!valid) {
                    break
                }
            }
            Thread.yield()
        }
    }
}