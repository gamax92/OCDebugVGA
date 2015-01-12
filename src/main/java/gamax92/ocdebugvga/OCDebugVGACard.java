package gamax92.ocdebugvga;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OCDebugVGACard extends Item {
	public OCDebugVGACard() {
		super();
		setUnlocalizedName("debugVGA");
		setCreativeTab(li.cil.oc.api.CreativeTab.instance);
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon(OCDebugVGA.MODID + ":" + this.getUnlocalizedName().substring(5));
	}
}
