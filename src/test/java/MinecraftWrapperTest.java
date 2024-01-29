import me.jsedwards.mod.CurseForge;
import me.jsedwards.mod.Modrinth;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.MinecraftUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.atomic.AtomicInteger;

public class MinecraftWrapperTest {

    @Test
    public void fabricVersions() {
        Assertions.assertEquals("0.15.6", ModLoader.FABRIC_LOADER_VERSION, "Wrong Fabric loader version");
        Assertions.assertEquals("1.0.0", ModLoader.FABRIC_INSTALLER_VERSION, "Wrong Fabric installer version");
    }

    @Test
    public void modrinthSearch() {
        String query = "create";
        ModLoader[] loaders = {ModLoader.FABRIC, ModLoader.FORGE};
        String mcVersion = "1.20.1";
        for (ModLoader loader : loaders) {
            Assertions.assertTrue(Modrinth.search(query, loader, mcVersion).size() > 0, "No results for query \"%s\", loader %s, version %s".formatted(query, loader, mcVersion));
        }
    }

    @Test
    public void curseforgeSearch() throws InterruptedException {
        String query = "create";
        ModLoader[] loaders = {ModLoader.FABRIC, ModLoader.FORGE};
        String mcVersion = "1.20.1";
        for (ModLoader loader : loaders) {
            AtomicInteger counter = new AtomicInteger(0);
            CurseForge.search(query, loader, mcVersion, p -> counter.incrementAndGet());
            Thread.sleep(3000);
            Assertions.assertTrue(counter.get() > 0, "No results for query \"%s\", loader %s, version %s".formatted(query, loader, mcVersion));
        }
    }

    @Test
    public void uuidQuery() {
        Assertions.assertEquals("1a23c2ed-e669-4369-9329-caac284778f9", MinecraftUtils.getPlayerUuid("Contrabass26"));
    }
}
