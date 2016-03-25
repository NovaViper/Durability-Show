package com.sixonethree.durabilityshow.client.gui;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiItemDurability extends Gui {
	private static Minecraft minecraftInstance;
	private static EnumGuiState guiState = EnumGuiState.OPEN;
	private static EnumCorner corner = EnumCorner.BOTTOM_RIGHT;
	private static int offsetPosition = 0;
	private static int closeSize = 16;
	private static int color_white = Color.WHITE.getRGB();
	private static FontRenderer fontRenderer;
	private static RenderItem itemRender;
	private static boolean renderCharacter = false;
	private static int overrideRenderCharacterTime = 0;
	private static Object[][] lastArmorSet = new Object[][] {
		new String[] {
			"", "", "", ""
		},
		new Integer[] {
			0, 0, 0, 0
		}
	};
	
	private static final int BOOTS = 1;
	private static final int LEGGINGS = 2;
	private static final int CHESTPLATE = 3;
	private static final int HELMET = 4;
	
	public static EnumGuiState getGuiState() { return guiState; }
	public static int getOffset() { return offsetPosition; }
	public static void setGuiState(EnumGuiState State) { guiState = State; }
	public static EnumCorner getCorner() { return corner; }
	public static void setCorner(EnumCorner newCorner) { corner = newCorner; }
	public static void lowerOffset() { offsetPosition --; }
	public static void raiseOffset() { offsetPosition ++; }
	public static void setCloseSize(int size) { closeSize = size; }
	public static int getCloseSize() { return closeSize; }
	public static int getOverrideTime() { return overrideRenderCharacterTime; }
	public static void decOverrideTime() { overrideRenderCharacterTime --; }
	public static boolean getRenderCharacter() { return renderCharacter; }
	public static void setRenderChararcter(boolean render) { renderCharacter = render; }
	
	public GuiItemDurability(Minecraft MC) {
		super();
		minecraftInstance = MC;
		fontRenderer = MC.fontRendererObj;
		itemRender = MC.getRenderItem();
	}
	
	private int getArrowsInInventory() {
		int arrows = 0;
		for (ItemStack stack : minecraftInstance.thePlayer.inventory.mainInventory) {
			if (stack != null) {
				if (stack.getItem() instanceof ItemArrow) {
					arrows += stack.stackSize;
				}
			}
		}
		return arrows;
	}
	
	private ItemStack getArrowToDraw() {
		if (this.isArrow(minecraftInstance.thePlayer.getHeldItem(EnumHand.OFF_HAND))) {
			return minecraftInstance.thePlayer.getHeldItem(EnumHand.OFF_HAND);
		} else if (this.isArrow(minecraftInstance.thePlayer.getHeldItem(EnumHand.MAIN_HAND))) {
			return minecraftInstance.thePlayer.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			for (int i = 0; i < minecraftInstance.thePlayer.inventory.getSizeInventory(); i ++) {
				ItemStack itemstack = minecraftInstance.thePlayer.inventory.getStackInSlot(i);
				if (this.isArrow(itemstack)) { return itemstack; }
			}
			return null;
		}
	}
	
	protected boolean isArrow(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemArrow;
	}
	
	public boolean allNull(ItemStack... stacks) {
		for (ItemStack s : stacks) {
			if (s != null) return false;
		}
		return true;
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL) public void onRender(RenderGameOverlayEvent.Post event) {
		EntityPlayer effectivePlayer = minecraftInstance.thePlayer;
		boolean noSpec = false;
		if (minecraftInstance.thePlayer.isSpectator()) {
			Entity spec = minecraftInstance.getRenderViewEntity();
			if (spec != null) {
				if (spec instanceof EntityPlayer) {
					effectivePlayer = (EntityPlayer) spec;
				} else {
					noSpec = true;
				}
			} else {
				noSpec = true;
			}
		}
		InventoryPlayer inventory = effectivePlayer.inventory;
		ItemStack current = inventory.getCurrentItem();
		ItemStack secondary = inventory.offHandInventory[0];
		ItemStack boots = inventory.armorInventory[0];
		ItemStack leggings = inventory.armorInventory[1];
		ItemStack chestplate = inventory.armorInventory[2];
		ItemStack helmet = inventory.armorInventory[3];
		
		if (event.isCanceled() ||
			allNull(current, boots, leggings, chestplate, helmet) ||
			minecraftInstance.thePlayer.capabilities.isCreativeMode ||
			noSpec ||
			event.getType() != ElementType.EXPERIENCE) return;
		
		/* Compare to last armor set */
		
		String curHelmetName = "";
		String curChestplateName = "";
		String curLeggingsName = "";
		String curBootsName = "";
		Integer curHelmetDur = 0;
		Integer curChestplateDur = 0;
		Integer curLeggingsDur = 0;
		Integer curBootsDur = 0;
		
		if (helmet != null) {
			curHelmetName = helmet.getUnlocalizedName();
			curHelmetDur = helmet.getItemDamage();
		}
		if (chestplate != null) {
			curChestplateName = chestplate.getUnlocalizedName();
			curChestplateDur = chestplate.getItemDamage();
		}
		if (leggings != null) {
			curLeggingsName = leggings.getUnlocalizedName();
			curLeggingsDur = leggings.getItemDamage();
		}
		if (boots != null) {
			curBootsName = boots.getUnlocalizedName();
			curBootsDur = boots.getItemDamage();
		}
		
		String lastHelmetName = (String) lastArmorSet[0][0];
		String lastChestplateName = (String) lastArmorSet[0][1];
		String lastLeggingsName = (String) lastArmorSet[0][2];
		String lastBootsName = (String) lastArmorSet[0][3];
		if (!lastHelmetName.equalsIgnoreCase(curHelmetName) ||
			lastArmorSet[1][0] != Integer.valueOf(curHelmetDur) ||
			!lastChestplateName.equalsIgnoreCase(curChestplateName) ||
			lastArmorSet[1][1] != Integer.valueOf(curChestplateDur) ||
			!lastLeggingsName.equalsIgnoreCase(curLeggingsName) ||
			lastArmorSet[1][2] != Integer.valueOf(curLeggingsDur) ||
			!lastBootsName.equalsIgnoreCase(curBootsName) ||
			lastArmorSet[1][3] != Integer.valueOf(curBootsDur)) {
			overrideRenderCharacterTime = 40;
		}
		
		lastArmorSet[0][0] = curHelmetName;
		lastArmorSet[0][1] = curChestplateName;
		lastArmorSet[0][2] = curLeggingsName;
		lastArmorSet[0][3] = curBootsName;
		lastArmorSet[1][0] = curHelmetDur;
		lastArmorSet[1][1] = curChestplateDur;
		lastArmorSet[1][2] = curLeggingsDur;
		lastArmorSet[1][3] = curBootsDur;
		
		/* Begin rendering */
		
		ScaledResolution scaled = new ScaledResolution(minecraftInstance);
		
		if (renderCharacter && overrideRenderCharacterTime <= 0) {
			renderCharacter(corner, 10, scaled, effectivePlayer);
		} else {
			int armorOffset = 16;
			int width = scaled.getScaledWidth() + offsetPosition;
			int height = scaled.getScaledHeight();
			GlStateManager.color(1, 1, 1, 1);
			RenderHelper.enableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			boolean armorAllNull = allNull(boots, leggings, chestplate, helmet);
			
			int[] params = new int[] {width, height, armorOffset, armorAllNull ? 1 : 0};
			int[] params2 = new int[] {width, height, 0, armorAllNull ? 1 : 0};
			
			if (corner.name().contains("RIGHT")) {
				params2 = renderItem(current, secondary, params, 1);
				if (!armorAllNull) {
					renderArmor(boots, BOOTS, params2, 2);
					renderArmor(leggings, LEGGINGS, params2, 2);
					renderArmor(chestplate, CHESTPLATE, params2, 2);
					renderArmor(helmet, HELMET, params2, 2);
				}
			} else {
				boolean params2gotten = false;
				if (boots != null) {
					if (!params2gotten) {
						params2 = renderArmor(boots, BOOTS, params, 1);
						params2gotten = true;
					} else {
						renderArmor(boots, BOOTS, params, 1);
					}
				}
				if (leggings != null) {
					if (!params2gotten) {
						params2 = renderArmor(leggings, LEGGINGS, params, 1);
						params2gotten = true;
					} else {
						renderArmor(leggings, LEGGINGS, params, 1);
					}
				}
				if (chestplate != null) {
					if (!params2gotten) {
						params2 = renderArmor(chestplate, CHESTPLATE, params, 1);
						params2gotten = true;
					} else {
						renderArmor(chestplate, CHESTPLATE, params, 1);
					}
				}
				if (helmet != null) {
					if (!params2gotten) {
						params2 = renderArmor(helmet, HELMET, params, 1);
						params2gotten = true;
					} else {
						renderArmor(helmet, HELMET, params, 1);
					}
				}
				renderItem(current, secondary, params2, 2);
			}
			
			RenderHelper.disableStandardItemLighting();
		}
	}
	
	private void renderItemAndEffectIntoGUI(ItemStack stack, int x, int y) {
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
	}
	
	private int[] renderItem(ItemStack mainHand, ItemStack offHand, int[] params, int turn) {
		int width = params[0];
		int height = params[1];
		boolean armorAllNull = params[3] == 1 ? true : false;
		int[] retStatement = new int[4];
		retStatement[0] = params[0];
		retStatement[1] = params[1];
		retStatement[2] = params[2];
		retStatement[3] = params[3];

		ItemStack firstStack = null;
		ItemStack secondStack = null;
		if (mainHand == null && offHand == null) return retStatement;
		if (mainHand == null) {
			if (offHand.isItemStackDamageable()) firstStack = offHand;
		} else {
			if (mainHand.isItemStackDamageable()) {
				firstStack = mainHand;
				if (offHand != null) {
					if (offHand.isItemStackDamageable()) secondStack = offHand;
				}
			} else {
				if (offHand != null) {
					if (offHand.isItemStackDamageable()) firstStack = offHand;
				}
			}
		}
		
		if (firstStack == null && secondStack == null) return retStatement;
		
		boolean mainBow = firstStack.getItem() instanceof ItemBow;
		boolean secondaryBow = secondStack != null ? secondStack.getItem() instanceof ItemBow : false;
		
		int itemX = corner.name().contains("LEFT") ? params[2] - (armorAllNull ? offsetPosition : 0) : width - 20;
		int mainHandY = corner.name().contains("TOP") ? !armorAllNull ? 16 : 0 : (armorAllNull ? height - 16 : height - 48);
		int arrowY = mainHandY + 16;
		int offHandY = mainHandY + ((mainBow || secondaryBow) && (getArrowsInInventory() > 0) ? 32 : 16);
		
		String mainDamage = String.valueOf(firstStack.getMaxDamage() - firstStack.getItemDamage());
		int damageStringWidth = fontRenderer.getStringWidth(mainDamage) + 2;
		renderItemAndEffectIntoGUI(firstStack, itemX - (corner.name().contains("LEFT") ? 0 : damageStringWidth - 2), mainHandY);
		if (firstStack.getItem() instanceof ItemBow) {
			int arrows = getArrowsInInventory();
			if (arrows > 0) {
				renderItemAndEffectIntoGUI(getArrowToDraw(), itemX - (corner.name().contains("LEFT") ? 0 : damageStringWidth - 2), arrowY);
				fontRenderer.drawString(String.valueOf(arrows), corner.name().contains("RIGHT") ? (itemX - damageStringWidth + 18) : itemX + 18, arrowY + (fontRenderer.FONT_HEIGHT / 2), color_white);
			}
		}
		fontRenderer.drawString(String.valueOf(mainDamage), corner.name().contains("RIGHT") ? (itemX - damageStringWidth + 18) : itemX + 18, mainHandY + (fontRenderer.FONT_HEIGHT / 2), color_white);
		if (secondStack != null) {
			String offHandDamage = String.valueOf(secondStack.getMaxDamage() - secondStack.getItemDamage());
			damageStringWidth = Math.max(damageStringWidth, fontRenderer.getStringWidth(offHandDamage) + 2);
			renderItemAndEffectIntoGUI(secondStack, itemX - (corner.name().contains("LEFT") ? 0 : damageStringWidth - 2), offHandY);
			if (secondStack.getItem() instanceof ItemBow && (!(firstStack.getItem() instanceof ItemBow))) {
				int arrows = getArrowsInInventory();
				if (arrows > 0) {
					renderItemAndEffectIntoGUI(getArrowToDraw(), itemX - (corner.name().contains("LEFT") ? 0 : damageStringWidth - 2), arrowY);
					fontRenderer.drawString(String.valueOf(arrows), corner.name().contains("RIGHT") ? (itemX - damageStringWidth + 18) : itemX + 18, arrowY + (fontRenderer.FONT_HEIGHT / 2), color_white);
				}
			}
			fontRenderer.drawString(String.valueOf(offHandDamage), corner.name().contains("RIGHT") ? (itemX - damageStringWidth + 18) : itemX + 18, offHandY + (fontRenderer.FONT_HEIGHT / 2), color_white);
		}
		
		retStatement[2] = damageStringWidth + 34;
		if (turn == 1 && armorAllNull) setCloseSize(18 + damageStringWidth);
		if (turn == 2 && armorAllNull) setCloseSize(18 + damageStringWidth);
		if (turn == 2 && !armorAllNull) setCloseSize(fontRenderer.getStringWidth("9999") + 36 + damageStringWidth);

		/*// OLD
		if (mainHand != null) {
			if (mainHand.isItemStackDamageable()) {
				int x = corner.name().contains("LEFT") ? params[2] - (armorAllNull ? offsetPosition : 0) : width - 20;
				int itemY = corner.name().contains("TOP") ? !armorAllNull ? 16 : 0 : (armorAllNull ? height - 16 : height - 48);
				int arrowY = corner.name().contains("BOTTOM") ? itemY - 16 : itemY + 16;
				String damage = String.valueOf(mainHand.getMaxDamage() - mainHand.getItemDamage());
				int damageStringWidth = fontRenderer.getStringWidth(damage) + 2;
				renderItemAndEffectIntoGUI(mainHand, x - (corner.name().contains("LEFT") ? 0 : damageStringWidth - 2), itemY);
				if (mainHand.getItem() instanceof ItemBow) {
					int arrows = getArrowsInInventory();
					if (arrows > 0) {
						renderItemAndEffectIntoGUI(getArrowToDraw(), x - (corner.name().contains("LEFT") ? 0 : damageStringWidth - 2), arrowY);
						fontRenderer.drawString(String.valueOf(arrows), corner.name().contains("RIGHT") ? (x - damageStringWidth + 18) : x + 18, arrowY + (fontRenderer.FONT_HEIGHT / 2), color_white);
					}
					fontRenderer.drawString(String.valueOf(damage), corner.name().contains("RIGHT") ? (x - damageStringWidth + 18) : x + 18, itemY + (fontRenderer.FONT_HEIGHT / 2), color_white);
				} else {
					fontRenderer.drawString(String.valueOf(damage), corner.name().contains("RIGHT") ? (x - damageStringWidth + 18) : x + 18, itemY + (fontRenderer.FONT_HEIGHT / 2), color_white);
				}
				retStatement[2] = damageStringWidth + 34;
				if (turn == 1 && armorAllNull) setCloseSize(18 + damageStringWidth);
				if (turn == 2 && armorAllNull) setCloseSize(18 + damageStringWidth);
				if (turn == 2 && !armorAllNull) setCloseSize(fontRenderer.getStringWidth("9999") + 36 + damageStringWidth);
			}
		}*/
		return retStatement;
	}
	
	private int[] renderArmor(ItemStack stack, int type, int[] params, int turn) {
		int width = params[0];
		int height = params[1];
		int armorOffset = params[2];
		int[] retStatement = new int[4];
		retStatement[0] = params[0];
		retStatement[1] = params[1];
		retStatement[3] = params[3];
		if (stack != null) {
			int x = (corner.name().contains("LEFT")) ? 0 + (armorOffset - 16) - offsetPosition : width - armorOffset;
			int y = (corner.name().contains("TOP")) ? (4 - type) * 16 : height - (16 * type);
			String damage = String.valueOf(stack.getMaxDamage() - stack.getItemDamage());
			int damageStringWidth = corner.name().contains("LEFT") ? Math.max(fontRenderer.getStringWidth(damage) + 2, fontRenderer.getStringWidth("9999") + 2) : fontRenderer.getStringWidth(damage) + 2;
			if (corner.name().contains("LEFT")) x += damageStringWidth;
			renderItemAndEffectIntoGUI(stack, x, y);
			fontRenderer.drawString(String.valueOf(damage), x - (corner.name().contains("LEFT") ? (damageStringWidth - 2) : damageStringWidth), y + (fontRenderer.FONT_HEIGHT / 2), color_white);
			retStatement[2] = x + 18;
			if (turn == 2) setCloseSize(16 + damageStringWidth + armorOffset);
		}
		return retStatement;
	}
	
	private void renderCharacter(EnumCorner side, int xPos, ScaledResolution scaled, EntityPlayer effectivePlayer) {
		if (side.name().contains("LEFT")) {
			GuiInventory.drawEntityOnScreen(xPos - offsetPosition, scaled.getScaledHeight(), xPos - (((xPos / 2) * -1) * 2), -50, - effectivePlayer.rotationPitch, effectivePlayer);
		} else {
			GuiInventory.drawEntityOnScreen((scaled.getScaledWidth() - xPos) + offsetPosition, scaled.getScaledHeight(), xPos - (((xPos / 2) * -1) * 2), 50, - effectivePlayer.rotationPitch, effectivePlayer);
		}
	}
}