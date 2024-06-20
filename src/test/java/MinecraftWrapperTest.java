import me.jsedwards.mod.ModProvider;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.MinecraftUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class MinecraftWrapperTest {

    @Test
    public void fabricVersions() {
        Assertions.assertEquals("0.15.11", ModLoader.FABRIC_LOADER_VERSION, "Wrong Fabric loader version");
        Assertions.assertEquals("1.0.1", ModLoader.FABRIC_INSTALLER_VERSION, "Wrong Fabric installer version");
    }

    @Test
    public void uuidQuery() {
        Assertions.assertEquals("1a23c2ed-e669-4369-9329-caac284778f9", MinecraftUtils.getPlayerUuid("Contrabass26"));
    }
}
