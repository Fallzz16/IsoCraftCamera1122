package dev.isocraft.camera;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Client-only Forge 1.12.2 isometric camera; does not require a server-side installation. */
@Mod(modid = IsoCraftCamera.MODID, name = "IsoCraft Camera", version = "0.3.0",
     acceptedMinecraftVersions = "[1.12.2]", clientSideOnly = true)
public final class IsoCraftCamera {
    public static final String MODID = "isocraftcamera";
    public static final Logger LOGGER = LogManager.getLogger("IsoCraftCamera");
    private static CameraSettings settings;
    public static CameraSettings settings() { return settings; }

    @EventHandler public void preInit(FMLPreInitializationEvent event) {
        settings = CameraSettings.load(event.getModConfigurationDirectory());
    }

    @EventHandler public void init(FMLInitializationEvent event) {
        CameraEvents.registerKeys();
        CursorEvents cursor = new CursorEvents();
        CameraEvents camera = new CameraEvents(cursor);
        MinecraftForge.EVENT_BUS.register(camera);
        MinecraftForge.EVENT_BUS.register(cursor);
        MinecraftForge.EVENT_BUS.register(new FoliageGhostRenderer());
        MinecraftForge.EVENT_BUS.register(new PlayerFacingCursor(cursor));
        FMLCommonHandler.instance().bus().register(camera);
        LOGGER.info("IsoCraft Camera Forge 1.12.2 v0.3.0 initialized");
    }
}
