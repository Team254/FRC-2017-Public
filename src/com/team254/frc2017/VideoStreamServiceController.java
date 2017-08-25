package com.team254.frc2017;

import edu.wpi.first.wpilibj.DriverStation;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Controls the first person view stream, ensuring that the service will be restarted and if the webcam is unplugged and
 * changes names.
 */
public class VideoStreamServiceController {

    private Process mExistingProcess;

    public void registerWatcher() {
        System.out.println("regstering watcher");
        FileSystem fileSystem = FileSystems.getDefault();

        Path devPath = fileSystem.getPath("/dev");
        try {
            final WatchService watchService = fileSystem.newWatchService();
            devPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("watcher registered");

            new Thread(() -> {
                findInitialVideoDevice();
                for (;;) {
                    try {
                        System.out.println("waiting for key");
                        WatchKey key = watchService.take();
                        System.out.println("got key");
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind != StandardWatchEventKinds.ENTRY_CREATE) {
                                System.out.println("File watcher Non create event");
                                continue;
                            }

                            Path newFilename = devPath.resolve(((WatchEvent<Path>) event).context());
                            if (!newFilename.getFileName().toString().startsWith("video")) {
                                System.out.println(
                                        "Don't care about new device: " + newFilename.toString());
                                continue;
                            }

                            System.out.println("New video device: " + newFilename.toString());
                            restartProcess(newFilename);
                        }
                        key.reset();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Exception: " + e);
                        continue;
                    }
                }
            }).run();
        } catch (IOException e) {
            e.printStackTrace();
            DriverStation.reportError("Couldn't start video service: " + e.getMessage(), false);
        }

    }

    private void findInitialVideoDevice() {
        File devFolder = new File("/dev");
        File[] videoDevices = devFolder.listFiles((dir, name) -> name.startsWith("video"));
        if (videoDevices.length == 0) {
            System.out.println("no video devices found");
        } else {
            System.out.println("Using video device " + videoDevices[0].toString());
            restartProcess(videoDevices[0].toPath());
        }
    }

    private void restartProcess(Path newVideoDevice) {
        System.out.println("Is old process alive? " + (mExistingProcess != null && mExistingProcess.isAlive()));
        if (mExistingProcess != null) {
            mExistingProcess.destroy();
            try {
                System.out.println("Wait for old process");
                mExistingProcess.waitFor();
                System.out.println("Done waiting for old process");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mExistingProcess = null;
        }

        try {
            mExistingProcess = new ProcessBuilder(
                    "/usr/local/bin/mjpg_streamer",
                    "-i",
                    "/usr/local/lib/mjpg-streamer/input_uvc.so --device " + newVideoDevice.toString() + " -r 160x120",
                    "-o",
                    "/usr/local/lib/mjpg-streamer/output_http.so -w /usr/local/share/mjpg-streamer/www -p 5801")
                            .redirectError(ProcessBuilder.Redirect.INHERIT)
                            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                            .start();
            System.out.println("New process alive: " + mExistingProcess.isAlive());
        } catch (IOException e) {
            DriverStation.reportError("Didn't start mjpg-streamer: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
