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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class WorldPanel extends JPanel {

    private static final Map<Integer, BufferedImage> textureCache = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, UnaryOperator<BufferedImage>> imageTransformers;
    private static final Map<String, String> blockTransformers;
    static {
        // Image transformers
        imageTransformers = new HashMap<>();
        imageTransformers.put("grass_block_top", getTintTransformer(0x7cbd6b));
        imageTransformers.put("birch_leaves", getTintTransformer(0x80a755));
        imageTransformers.put("oak_leaves", getTintTransformer(0x48b518));
        // Block name transformers
        blockTransformers = new HashMap<>();
        blockTransformers.put("grass_block", "grass_block_top");
        blockTransformers.put("snow_block", "snow");
    }

    private Server server = null;
    private int[] numChunks;
    private Chunk[] chunks;
    private final int[] centre = {0, 0};
    private int[] dragStart = null;

    public WorldPanel() {
        super();
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = new int[]{e.getX(), e.getY()};
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    centre[0] -= e.getX() - dragStart[0];
                    centre[1] -= e.getY() - dragStart[1];
                    updateLoadedChunks();
                    repaint();
                }
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        for (Chunk chunk : chunks) {
            int[] offset = chunk.getOffset();
            g.drawImage(chunk.image, offset[0], offset[1], null);
        }
    }

    private static Color tintColor(Color color, int tint) {
        return new Color(
                color.getRed() * ((tint >> 16) & 0xff) / 255,
                color.getGreen() * ((tint >> 8) & 0xff) / 255,
                color.getBlue() * (tint & 0xff) / 255
        );
    }

    private static UnaryOperator<BufferedImage> getTintTransformer(int tint) {
        return image -> {
            BufferedImage newImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    Color color = new Color(image.getRGB(x, y));
                    Color newColor = tintColor(color, tint);
                    newImage.setRGB(x, y, newColor.getRGB());
                }
            }
            return newImage;
        };
    }

    public void setServer(Server server) {
        this.server = server;
        numChunks = new int[]{Math.ceilDiv(getWidth(), 256), Math.ceilDiv(getHeight(), 256)};
        LOGGER.info("Going for {} chunks", numChunks);
        chunks = new Chunk[numChunks[0] * numChunks[1]];
        int halfX = Math.ceilDiv(numChunks[0], 2);
        int halfZ = Math.ceilDiv(numChunks[1], 2);
        for (int x = 0; x < numChunks[0]; x++) {
            for (int z = 0; z < numChunks[1]; z++) {
                try {
                    chunks[x + z] = new Chunk(x - halfX, z - halfZ);
                } catch (AnvilException | IOException e) {
                    LOGGER.error("Failed to load chunk at (%s, %s)".formatted(x, z), e);
                }
            }
        }
        repaint();
    }

    private void updateLoadedChunks() {
        for (int i = 0; i < chunks.length; i++) {
            Chunk chunk = chunks[i];
            short issue = chunk.findIssue();
            try {
                if (Math.abs(issue) == 1) {
                    chunks[i] = new Chunk(chunk.actualX - issue * numChunks[0], chunk.actualZ);
                } else if (Math.abs(issue) == 2) {
                    chunks[i] = new Chunk(chunk.actualX, chunk.actualZ - issue * numChunks[1] / 2);
                }
            } catch (AnvilException | IOException e) {
                throw new RuntimeException(e);
            }
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

        public final Image image;
        private final int actualX;
        private final int actualZ;

        public Chunk(int actualX, int actualZ) throws AnvilException, IOException {
            int regionX = Math.floorDiv(actualX, 32);
            int regionZ = Math.floorDiv(actualZ, 32);
            int chunkX = actualX % 32;
            int chunkZ = actualZ % 32;
            String path = "%s/world/region/r.%s.%s.mca".formatted(server.serverLocation, regionX, regionZ);
            RegionFile region = new RegionFile(new RandomAccessFileSource(new RandomAccessFile(path, "r")), regionX, regionZ);
            this.actualX = actualX;
            this.actualZ = actualZ;
            ChunkColumn chunk = region.getChunk(chunkX, chunkZ);
            if (chunk == null)
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
            LOGGER.info("Loaded chunk at (%s, %s)".formatted(actualX, actualZ));
        }

        public short findIssue() { // 0 = no issue, 1 = x, 2 = y, sign = which direction
            int[] offset = getOffset();
            if (offset[0] > getWidth() / 2) return 1;
            if (offset[0] < -256 - getWidth() / 2) return -1;
            if (offset[1] > getHeight() / 2) return 2;
            if (offset[1] < -256 - getWidth() / 2) return -2;
            return 0;
        }

        private int[] getOffset() {
            return new int[]{actualX * 256 - centre[0], actualZ * 256 - centre[1]};
        }
    }
}
