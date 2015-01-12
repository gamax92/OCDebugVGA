package gamax92.ocdebugvga;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = OCDebugVGA.MODID, name = OCDebugVGA.NAME, version = OCDebugVGA.VERSION, dependencies = "required-after:OpenComputers@[1.4.0,)")
public class OCDebugVGA {
	public static final String MODID = "ocdebugvga";
	public static final String NAME = "OCDebugVGA";
	public static final String VERSION = "1.0";

	@Instance
	public static OCDebugVGA instance;

	public static OCDebugVGACard debugvgaCard;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		debugvgaCard = new OCDebugVGACard();
		GameRegistry.registerItem(debugvgaCard, "debugVGA");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		li.cil.oc.api.Driver.add(new DriverOCDebugVGACard());
	}
}
