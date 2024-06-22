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
import java.awt.event.*;
import java.awt.geom.AffineTransform;
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
        imageTransformers.put("water", bufferedImage -> {
            BufferedImage newImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics g = newImage.getGraphics();
            g.setColor(new Color(0x3d57d6));
            g.fillRect(0, 0, 16, 16);
            return newImage;
        });
        // Block name transformers
        blockTransformers = new HashMap<>();
        blockTransformers.put("grass_block", "grass_block_top");
        blockTransformers.put("snow_block", "snow");
        blockTransformers.put("pumpkin", "pumpkin_top");
        blockTransformers.put("pointed_dripstone", "dripstone");
        blockTransformers.put("tall_seagrass", "seagrass");
    }

    private Server server = null;
    private int[] numChunks;
    private Chunk[] chunks;
    private double[] centre = {0, 0};
    private int[] lastDrag = null;
    private int scale = 256; // The size of one chunk in pixels

    public WorldPanel() {
        super();
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDrag = new int[]{e.getX(), e.getY()};
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDrag = null;
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDrag != null) {
                    centre[0] -= e.getX() - lastDrag[0];
                    centre[1] -= e.getY() - lastDrag[1];
                    lastDrag = new int[]{e.getX(), e.getY()};
                    updateLoadedChunks();
                    repaint();
                }
            }
        });
        this.addMouseWheelListener(e -> {
            double[] scaledCentre = new double[]{centre[0] / scale, centre[1] / scale};
            int proposed = scale + e.getWheelRotation() * 10;
            scale = Math.max(1, proposed);
            refreshScale();
            centre = new double[]{scaledCentre[0] * scale, scaledCentre[0] * scale};
        });
    }

    @Override
    public void paint(Graphics g) {
        for (Chunk chunk : chunks) {
            double[] offset = chunk.getOffset();
            g.drawImage(chunk.image, (int) (getWidth() / 2 + offset[0]), (int) (getHeight() / 2 + offset[1]), null);
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
        Chunk.regionCache.clear();
        refreshScale();
    }

    private void refreshScale() {
        numChunks = new int[]{Math.ceilDiv(getWidth(), scale) + 1, Math.ceilDiv(getHeight(), scale) + 1};
        LOGGER.info("Going for {} chunks", numChunks);
        chunks = new Chunk[numChunks[0] * numChunks[1]];
        int halfX = Math.ceilDiv(numChunks[0], 2);
        int halfZ = Math.ceilDiv(numChunks[1], 2);
        for (int x = 0; x < numChunks[0]; x++) {
            for (int z = 0; z < numChunks[1]; z++) {
                try {
                    chunks[x * numChunks[1] + z] = new Chunk(x - halfX, z - halfZ);
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
//                    LOGGER.info("Chunk unloaded off the x-axis");
                    chunks[i] = new Chunk(chunk.actualX - issue * numChunks[0], chunk.actualZ);
                } else if (Math.abs(issue) == 2) {
//                    LOGGER.info("Chunk unloaded off the y-axis");
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
            BufferedImage texture;
            try {
                texture = ImageIO.read(new URL(url));
            } catch (IOException e) {
                LOGGER.warn("Failed to get texture from %s".formatted(url));
                texture = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            }
            UnaryOperator<BufferedImage> transformer = imageTransformers.get(block);
            if (transformer != null) {
                texture = transformer.apply(texture);
            }
            textureCache.put(hash, texture);
        }
        return textureCache.get(hash);
    }

    private class Chunk {

        private static final Map<Integer, RegionFile> regionCache = new HashMap<>();

        public final Image image;
        private final int actualX;
        private final int actualZ;

        public Chunk(int actualX, int actualZ) throws AnvilException, IOException {
            int regionX = Math.floorDiv(actualX, 32);
            int regionZ = Math.floorDiv(actualZ, 32);
            int chunkX = actualX % 32;
            int chunkZ = actualZ % 32;
            RegionFile region = getRegion(regionX, regionZ);
            this.actualX = actualX;
            this.actualZ = actualZ;
            ChunkColumn chunk = region.getChunk(chunkX, chunkZ);
            if (chunk == null)
                throw new IllegalArgumentException("No chunk exists at %s, %s".formatted(chunkX, chunkZ));
            this.image = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setTransform(AffineTransform.getScaleInstance(scale / 256d, scale / 256d));
            Heightmap heightMap = chunk.getMotionBlockingHeightMap();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int y = heightMap.get(x, z) - 65;
                    if (y >= -64) {
                        String block = StringUtils.substringAfter(chunk.getBlockState(x, y, z).getName(), ':');
                        block = blockTransformers.getOrDefault(block, block);
                        BufferedImage texture = getTexture(server.mcVersion, block);
                        g.drawImage(texture, x * 16, z * 16, null);
                    }
                }
            }
            g.setColor(Color.RED);
            g.drawRect(0, 0, 256, 256);
        }

        private RegionFile getRegion(int regionX, int regionZ) throws IOException, AnvilException {
            int hash = Objects.hash(regionX, regionZ);
            if (!regionCache.containsKey(hash)) {
                String path = "%s/world/region/r.%s.%s.mca".formatted(server.serverLocation, regionX, regionZ);
                RegionFile region = new RegionFile(new RandomAccessFileSource(new RandomAccessFile(path, "r")), regionX, regionZ);
                regionCache.put(hash, region);
                return region;
            }
            return regionCache.get(hash);
        }

        public short findIssue() { // 0 = no issue, 1 = x, 2 = y, sign = which direction
            double[] offset = getOffset();
            if (offset[0] > getWidth() / 2d) return 1;
            if (offset[0] < -scale - getWidth() / 2d) return -1;
            if (offset[1] > getHeight() / 2d) return 2;
            if (offset[1] < -scale - getHeight() / 2d) return -2;
            return 0;
        }

        private double[] getOffset() {
            return new double[]{actualX * scale - centre[0], actualZ * scale - centre[1]};
        }
    }
}
