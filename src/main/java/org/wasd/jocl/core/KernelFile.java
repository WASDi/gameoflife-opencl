package org.wasd.jocl.core;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public enum KernelFile {

    ADD("add.cl", "add"),
    CONVOLUTION("convolution.cl", "convolution"),
    MAGIC_DOTS("magic_dots.cl", "magic_dots"),
    WARP("warp.cl", "warp"),
    GAME_OF_LIFE_UNOPTIMIZED("game_of_life_unoptimized.cl", "game_step", "render_image"),
    GAME_OF_LIFE("game_of_life_local.cl", "game_step", "render_image"),
    ROTATE_IMAGE("rotateImage.cl", "rotateImage");

    public final String fileName;
    public final String[] functionNames;

    KernelFile(String fileName, String... functionNames) {
        this.fileName = fileName;
        this.functionNames = functionNames;
    }

    public String getFunctionName() {
        return functionNames[0];
    }

    public String load() {
        try {
            URL u = KernelFile.class.getResource("/kernels/" + fileName);
            Path p = Paths.get(u.toURI());
            List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
            return lines.stream().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

}
