package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jglrxavpok.hephaistos.data.RandomAccessFileSource;
import org.jglrxavpok.hephaistos.mca.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class WorldPanel extends JPanel {

    private static final Map<Integer, BufferedImage> textureCache = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final UnaryOperator<BufferedImage> GREEN_SATURATION = image -> {
        BufferedImage newImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int current = image.getRGB(x, y);
                newImage.setRGB(x, y, current & 0xff00);
            }
        }
        return newImage;
    };
    private static final Map<String, UnaryOperator<BufferedImage>> imageTransformers;
    private static final Map<String, String> blockTransformers;
    static {
        // Image transformers
        imageTransformers = new HashMap<>();
        imageTransformers.put("grass_block_top", GREEN_SATURATION);
        // Block name transformers
        blockTransformers = new HashMap<>();
        blockTransformers.put("grass_block", "grass_block_top");
    }

    private Server server = null;
    private Chunk chunk = null;

    @Override
    public void paint(Graphics g) {
        if (server != null && chunk != null) {
            g.drawImage(chunk.image, 0, 0, null);
        }
    }

    public void setServer(Server server) {
        this.server = server;
        String path = server.serverLocation + "/world/region/r.0.0.mca";
        try {
            RegionFile region = new RegionFile(new RandomAccessFileSource(new RandomAccessFile(path, "r")), 0, 0);
            chunk = new Chunk(region, 0, 0);
            repaint();
        } catch (AnvilException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage getTexture(String mcVersion, String block) {
        int hash = Objects.hash(mcVersion, block);
        if (!textureCache.containsKey(hash)) {
            String url = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/%s/assets/minecraft/textures/block/%s.png".formatted(mcVersion, block);
            try {
                BufferedImage texture = ImageIO.read(new URL(url));
                UnaryOperator<BufferedImage> transformer = imageTransformers.get(block);
                if (transformer != null) {
                    texture = transformer.apply(texture);
                }
                textureCache.put(hash, texture);
            } catch (IOException e) {
                LOGGER.warn("Failed to get texture from %s".formatted(url));
                BufferedImage texture = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
                textureCache.put(hash, texture);
            }
        }
        return textureCache.get(hash);
    }

    private class Chunk {

        private final ChunkColumn chunk;
        public final Image image;

        public Chunk(RegionFile region, int chunkX, int chunkZ) throws AnvilException, IOException {
            this.chunk = region.getChunk(chunkX, chunkZ);
            if (this.chunk == null)
                throw new IllegalArgumentException("No chunk exists at %s, %s".formatted(chunkX, chunkZ));
            this.image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            Heightmap heightMap = chunk.getMotionBlockingHeightMap();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int y = heightMap.get(x, z) - 65;
                    String block = StringUtils.substringAfter(chunk.getBlockState(x, y, z).getName(), ':');
                    block = blockTransformers.getOrDefault(block, block);
                    BufferedImage texture = getTexture(server.mcVersion, block);
                    g.drawImage(texture, x * 16, z * 16, null);
                }
            }
        }
    }
}
