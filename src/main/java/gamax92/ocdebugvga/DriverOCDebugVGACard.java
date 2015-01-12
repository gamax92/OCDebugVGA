package gamax92.ocdebugvga;

import li.cil.oc.api.Network;
import li.cil.oc.api.component.TextBuffer;
import li.cil.oc.api.component.TextBuffer.ColorDepth;
import li.cil.oc.api.driver.EnvironmentHost;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class DriverOCDebugVGACard extends DriverItem {
	static final int[] depths = { 1, 4, 8 };

	protected DriverOCDebugVGACard() {
		super(new ItemStack(OCDebugVGA.debugvgaCard));
	}

	@Override
	public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost container) {
		if (container instanceof TileEntity)
			return new Environment((TileEntity) container);
		return null;
	}

	@Override
	public String slot(ItemStack stack) {
		return Slot.Card;
	}

	public class Environment extends li.cil.oc.api.prefab.ManagedEnvironment {
		protected final TileEntity container;

		boolean bounded = false;
		String bindAddr;
		TextBuffer screen;

		public Environment(TileEntity container) {
			this.container = container;
			this.setNode(Network.newNode(this, Visibility.Neighbors).withComponent("gpu").create());
		}

		@Override
		public void load(NBTTagCompound nbt) {
			super.load(nbt);
			if (nbt.hasKey("screen")) {
				/*
				// This has issues.
				String address = nbt.getString("screen");
				
				Node testNode = this.node().network().node(address);
				if (testNode != null && testNode.canBeReachedFrom(node())) {
					li.cil.oc.api.network.Environment host = testNode.host();
					if (host instanceof TextBuffer) {
						bounded = true;
						bindAddr = address;
						screen = (TextBuffer)host;
					}
				}
				*/
			}
		}

		@Override
		public void save(NBTTagCompound nbt) {
			super.save(nbt);
			if (bounded)
				nbt.setString("screen", bindAddr);
		}

		@Callback(direct = true, doc = "function(address:string):boolean -- Binds the GPU to the screen with the specified address.")
		public Object[] bind(Context context, Arguments args) {
			String address = args.checkString(0);

			Node testNode = this.node().network().node(address);
			if (testNode == null || !testNode.canBeReachedFrom(node()))
				return new Object[] { null, "invalid address" };
			li.cil.oc.api.network.Environment host = testNode.host();
			if (!(host instanceof TextBuffer)) {
				return new Object[] { null, "not a screen" };
			}

			bounded = true;
			bindAddr = address;
			screen = (TextBuffer) host;

			return new Object[] { true };
		}

		@Callback(direct = true, doc = "function():number, boolean -- Get the current foreground color and whether it's from the palette or not.")
		public Object[] getForeground(Context context, Arguments args) {
			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { screen.getForegroundColor(), screen.isForegroundFromPalette() };
		}

		@Callback(direct = true, doc = "function(value:number[, palette:boolean]):number, number or nil -- Sets the foreground color to the specified value. Optionally takes an explicit palette index. Returns the old value and if it was from the palette its palette index.")
		public Object[] setForeground(Context context, Arguments args) {
			int value = args.checkInteger(0);
			boolean palette = args.optBoolean(1, false);

			if (!bounded)
				return new Object[] { null, "no screen" };

			int oldrgb;
			int index = 0;
			boolean fromIndex = screen.isForegroundFromPalette();
			if (fromIndex) {
				index = screen.getForegroundColor();
				oldrgb = screen.getPaletteColor(index);
			} else {
				oldrgb = screen.getForegroundColor();
			}

			screen.setForegroundColor(value, palette);

			return new Object[] { oldrgb, fromIndex ? index : null };
		}

		@Callback(direct = true, doc = "function():number, boolean -- Get the current background color and whether it's from the palette or not.")
		public Object[] getBackground(Context context, Arguments args) {
			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { screen.getBackgroundColor(), screen.isBackgroundFromPalette() };
		}

		@Callback(direct = true, doc = "function(value:number[, palette:boolean]):number, number or nil -- Sets the background color to the specified value. Optionally takes an explicit palette index. Returns the old value and if it was from the palette its palette index.")
		public Object[] setBackground(Context context, Arguments args) {
			int value = args.checkInteger(0);
			boolean palette = args.optBoolean(1, false);

			if (!bounded)
				return new Object[] { null, "no screen" };

			int oldrgb;
			int index = 0;
			boolean fromIndex = screen.isBackgroundFromPalette();
			if (fromIndex) {
				index = screen.getBackgroundColor();
				oldrgb = screen.getPaletteColor(index);
			} else {
				oldrgb = screen.getBackgroundColor();
			}

			screen.setBackgroundColor(value, palette);

			return new Object[] { oldrgb, fromIndex ? index : null };
		}

		@Callback(direct = true, doc = "function():number -- Returns the currently set color depth.")
		public Object[] getDepth(Context context, Arguments args) {
			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { depths[screen.getColorDepth().ordinal()] };
		}

		@Callback(direct = true, doc = "function(depth:number):number -- Set the color depth. Returns the previous value.")
		public Object[] setDepth(Context context, Arguments args) throws Exception {
			int depth = args.checkInteger(0);

			if (!bounded)
				return new Object[] { null, "no screen" };

			int olddepth = depths[screen.getColorDepth().ordinal()];

			switch (depth) {
			case 1:
				screen.setColorDepth(ColorDepth.OneBit);
				break;
			case 4:
				screen.setColorDepth(ColorDepth.FourBit);
				break;
			case 8:
				screen.setColorDepth(ColorDepth.EightBit);
				break;
			default:
				throw new Exception("unsupported depth");
			}

			return new Object[] { olddepth };
		}

		@Callback(direct = true, doc = "function():number -- Get the maximum supported color depth.")
		public Object[] maxDepth(Context context, Arguments args) {
			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { depths[screen.getMaximumColorDepth().ordinal()] };
		}

		@Callback(direct = true, doc = "function(depth:number):number -- Set the maximum supported color depth. Returns the previous value.")
		public Object[] setMaxDepth(Context context, Arguments args) throws Exception {
			int depth = args.checkInteger(0);

			if (!bounded)
				return new Object[] { null, "no screen" };

			int olddepth = depths[screen.getMaximumColorDepth().ordinal()];

			switch (depth) {
			case 1:
				screen.setMaximumColorDepth(ColorDepth.OneBit);
				break;
			case 4:
				screen.setMaximumColorDepth(ColorDepth.FourBit);
				break;
			case 8:
				screen.setMaximumColorDepth(ColorDepth.EightBit);
				break;
			default:
				throw new Exception("unsupported depth");
			}

			return new Object[] { olddepth };
		}

		@Callback(direct = true, doc = "function(x:number, y:number, width:number, height:number, char:string):boolean -- Fills a portion of the screen at the specified position with the specified size with the specified character.")
		public Object[] fill(Context context, Arguments args) {
			int x = args.checkInteger(0) - 1;
			int y = args.checkInteger(1) - 1;
			int width = args.checkInteger(2);
			int height = args.checkInteger(3);
			String character = args.checkString(4);

			if (!bounded)
				return new Object[] { null, "no screen" };

			if (character.length() != 1)
				return new Object[] { null, "invalid fill value" };

			screen.fill(x, y, width, height, character.charAt(0));

			return new Object[] { true };
		}

		@Callback(direct = true, doc = "function():string -- Get the address of the screen the GPU is currently bound to.")
		public Object[] getScreen(Context context, Arguments args) {
			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { bindAddr };
		}

		@Callback(direct = true, doc = "function():number, number -- Get the current screen resolution.")
		public Object[] getResolution(Context context, Arguments args) {
			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { screen.getWidth(), screen.getHeight() };
		}

		@Callback(direct = true, doc = "function(width:number, height:number):boolean -- Set the screen resolution. Returns true if the resolution changed.")
		public Object[] setResolution(Context context, Arguments args) {
			int width = args.checkInteger(0);
			int height = args.checkInteger(1);

			if (!bounded)
				return new Object[] { null, "no screen" };

			int curwidth = screen.getWidth();
			int curheight = screen.getHeight();
			int maxwidth = screen.getMaximumWidth();
			int maxheight = screen.getMaximumHeight();

			if (width == curwidth && height == curheight)
				return new Object[] { false };

			if (width <= 0 || height <= 0 || width > maxwidth || height > maxheight)
				return new Object[] { false };

			screen.setResolution(width, height);

			return new Object[] { true };
		}

		@Callback(direct = true, doc = "function():number, number -- Get the maximum screen resolution.")
		public Object[] maxResolution(Context context, Arguments args) {
			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { screen.getMaximumWidth(), screen.getMaximumHeight() };
		}

		@Callback(direct = true, doc = "function(width:number, height:number):number, number -- Set the maximum screen resolution. Returns the old maximum width and height.")
		public Object[] setMaxResolution(Context context, Arguments args) {
			int width = args.checkInteger(0);
			int height = args.checkInteger(1);

			if (!bounded)
				return new Object[] { null, "no screen" };

			int maxwidth = screen.getMaximumWidth();
			int maxheight = screen.getMaximumHeight();

			screen.setMaximumResolution(width, height);

			return new Object[] { maxwidth, maxheight };
		}

		@Callback(direct = true, doc = "function(index:number):number -- Get the palette color at the specified palette index.")
		public Object[] getPaletteColor(Context context, Arguments args) {
			int index = args.checkInteger(0);

			if (!bounded)
				return new Object[] { null, "no screen" };

			return new Object[] { screen.getPaletteColor(index) };
		}

		@Callback(direct = true, doc = "function(index:number, color:number):number -- Set the palette color at the specified palette index. Returns the previous value.")
		public Object[] setPaletteColor(Context context, Arguments args) {
			int index = args.checkInteger(0);
			int color = args.checkInteger(1);

			if (!bounded)
				return new Object[] { null, "no screen" };

			int oldcolor = screen.getPaletteColor(index);

			screen.setPaletteColor(index, color);

			return new Object[] { oldcolor };
		}

		@Callback(direct = true, doc = "function(x:number, y:number):string, number, number, number or nil, number or nil -- Get the value displayed on the screen at the specified index, as well as the foreground and background color. If the foreground or background is from the palette, returns the palette indices as fourth and fifth results, else nil, respectively.")
		public Object[] get(Context context, Arguments args) {
			int x = args.checkInteger(0) - 1;
			int y = args.checkInteger(1) - 1;

			if (!bounded)
				return new Object[] { null, "no screen" };

			boolean fgpal = screen.isForegroundFromPalette(x, y);
			boolean bgpal = screen.isBackgroundFromPalette(x, y);

			return new Object[] { screen.get(x, y), fgpal ? screen.getPaletteColor(screen.getForegroundColor(x, y)) : screen.getForegroundColor(x, y), bgpal ? screen.getPaletteColor(screen.getBackgroundColor(x, y)) : screen.getBackgroundColor(x, y), fgpal ? screen.getForegroundColor(x, y) : null, bgpal ? screen.getBackgroundColor(x, y) : null };
		}

		@Callback(direct = true, doc = "function(x:number, y:number, value:string[, vertical:boolean]):boolean -- Plots a string value to the screen at the specified position. Optionally writes the string vertically.")
		public Object[] set(Context context, Arguments args) {
			int x = args.checkInteger(0) - 1;
			int y = args.checkInteger(1) - 1;
			String value = args.checkString(2);
			boolean vertical = args.optBoolean(3, false);

			if (!bounded)
				return new Object[] { null, "no screen" };

			screen.set(x, y, value, vertical);

			return new Object[] { true };
		}

		@Callback(direct = true, doc = "function(x:number, y:number, width:number, height:number, tx:number, ty:number):boolean -- Copies a portion of the screen from the specified location with the specified size by the specified translation.")
		public Object[] copy(Context context, Arguments args) {
			int x = args.checkInteger(0) - 1;
			int y = args.checkInteger(1) - 1;
			int width = args.checkInteger(2);
			int height = args.checkInteger(3);
			int tx = args.checkInteger(4);
			int ty = args.checkInteger(5);

			if (!bounded)
				return new Object[] { null, "no screen" };

			screen.copy(x, y, width, height, tx, ty);

			return new Object[] { true };
		}
	}
}
