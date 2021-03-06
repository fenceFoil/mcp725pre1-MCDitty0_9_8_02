package net.minecraft.src;

import java.util.HashMap;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import com.wikispaces.mcditty.Finder;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.config.MCDittyConfig;

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/**
 * Marked changes in code below are for the MCDitty mod. MCDitty code is
 * Copyright (c) 2012 William Karnavas All Rights Reserved.
 */

public class TileEntitySignRenderer extends TileEntitySpecialRenderer {

	/** The ModelSign instance used by the TileEntitySignRenderer */
	private ModelSign modelSign;

	private static long lastMenuButtonCheckTime = 0;

	// private long blinkingEnd = 0;

	public TileEntitySignRenderer() {
		modelSign = new ModelSign();

		// MCDitty: try to create update tick hook entity
		MCDitty.createHookEntity(BlockSign.mcDittyMod);
	}

	public static long lastMCDittyTickHookEntityCheckTime = System
			.currentTimeMillis();
	public static int updateTimeCounter = 1000000;
	public static long currentSystemTime = 0;
	public static long currentSystemTimeCheckInterval = 20;

	public static int fpsCounter = 0;
	public static int currentFPS = 0;
	public static long lastSecondStartTime = -1;

	public static int renderCountThisTick = 0;
	public static int renderCountLastTick = 0;
	public static int invisibleSignsThisTick = 0;
	public static int invisibleTextsThisTick = 0;
	public static float lastTickFraction = 0;
	public static Point3D playerPoint = new Point3D();
	public static int playerFacingThisTick = 0;

	public static boolean fullRenderingEnabled = false;

	private static Point3D tempPoint = new Point3D();

	public static Minecraft mc = Minecraft.getMinecraft();

	// TODO: Replace "tick" with the term "frame" everywhere?
	public void renderTileEntitySignAt(TileEntitySign signEntity, double par2,
			double par4, double par6, float par8) {
		// Note that since this renderer may have been called (rather unusually)
		// in the earliest parts of the game's loading (to set up the trig
		// tables), its TileEntitySignRender.mc field may be null. Rectify that
		// here.
		if (mc == null) {
			mc = Minecraft.getMinecraft();
		}

		if (par8 == lastTickFraction) {
			renderCountThisTick++;
		} else {
			// New frame
			// System.out.println(renderCountThisTick + "/"
			// + invisibleSignsThisTick + "/" + invisibleTextsThisTick);
			renderCountLastTick = renderCountThisTick;
			renderCountThisTick = 0;
			invisibleSignsThisTick = 0;
			invisibleTextsThisTick = 0;
			lastTickFraction = par8;

			if (System.currentTimeMillis() - lastSecondStartTime >= 1000) {
				// New second
				currentFPS = fpsCounter;
				fpsCounter = 0;
				lastSecondStartTime = System.currentTimeMillis();
			}

			playerFacingThisTick = MathHelper
					.floor_double((double) ((mc.thePlayer.rotationYaw * 4F) / 360F) + 0.5D) & 3;

			playerPoint.x = (int) mc.thePlayer.posX;
			playerPoint.y = (int) mc.thePlayer.posY;
			playerPoint.z = (int) mc.thePlayer.posZ;

			fpsCounter++;
		}

		// TODO: Make more efficient
		if (updateTimeCounter > currentSystemTimeCheckInterval) {
			currentSystemTime = System.currentTimeMillis();
			updateTimeCounter = 0;
		}
		updateTimeCounter++;

		boolean renderText = true;
		boolean renderSign = true;
		boolean doNotHideForSure = false;
		
		if (!fullRenderingEnabled && !signEntity.alwaysRender && renderCountLastTick > 100) {
			// Decide whether to render sign text (expensive)
			renderText = false;
			renderSign = false;

			// Check that the sign isn't one that MUST be rendered because it is
			// of
			// interest
			if (signEntity.picked || signEntity.blinking) {
				doNotHideForSure = true;
				renderSign = true;
				renderText = true;
			}

			// Eliminate signs that are far from the player
			double distToPlayer = playerPoint.distanceToRel(signEntity
					.posToPoint3D(tempPoint));
			if (distToPlayer < 2 * 2) {
				// So close, could be inside block. Ensure it is rendered
				doNotHideForSure = true;
				renderSign = true;
				renderText = true;
			}

			if (!doNotHideForSure) {
				if (distToPlayer < 16 * 16) {
					renderText = true;
					renderSign = true;
				} else if (distToPlayer < 64 * 64) {
					renderSign = true;
					invisibleTextsThisTick++;
				} else {
					invisibleSignsThisTick++;
					invisibleTextsThisTick++;
				}
			}

			// Eliminate sign texts if they face same way as player

			int signFacing = BlockSign.getSignFacing(signEntity.blockMetadata,
					signEntity.blockType);
			if (renderText) {
				if (signFacing == playerFacingThisTick) {
					renderText = false;
					invisibleTextsThisTick++;
				}
			}

			// Eliminate signs if they are wall signs and face same way as
			// player
			// and are mounted on an opaque block
			if (renderSign) {
				if (signFacing == playerFacingThisTick
						&& signEntity.blockType == Block.signWall) {
					if (signEntity.isAnchorBlockOpaque()) {
						renderSign = false;
						invisibleSignsThisTick++;
					}
				}
			}

			// Try to eliminate sign if it's behind player
			if ((renderSign || renderText) && !doNotHideForSure) {
				// Get angle relative to player
				// Get relative positions of player and sign
				double dx = mc.thePlayer.posX - (signEntity.xCoord + 0.5d);
				double dz = mc.thePlayer.posZ - (signEntity.zCoord + 0.5d);

				// Ensure that dz never causes div by 0
				if (dz == 0) {
					dz = 0.0001f;
				}

				// Find the angle the player is facing
				int playerTheta = (int) MathHelper
						.wrapAngleTo180_double(mc.thePlayer.rotationYaw);
				if (playerTheta < 0) {
					playerTheta += 360;
				}

				// Find angle of sign relative to player
				int signTheta = 180 - (int) Math.toDegrees(atan2((float) dx,
						(float) dz));

				// Get the difference
				int difference = Math.abs(signTheta - playerTheta);

				if (difference > 180) {
					difference = Math.abs(difference - 360);
				}

				if ((dx > 0 && dz > 0) || (dx < 0 && dz > 0)) {
					difference = 180 - difference;
					difference = 180 - difference;
				} else {
					// difference = 180 - difference;
				}

				// System.out.println (difference);

				if (difference > 90) {
					if (renderSign) {
						renderSign = false;
						invisibleSignsThisTick++;
					}

					if (renderText) {
						renderText = false;
						invisibleTextsThisTick++;
					}
				}
			} else if (!doNotHideForSure) {
				renderSign = false;
				renderText = false;
			}
		}

		// System.out.println(difference);

		Block block = signEntity.getBlockType();
		GL11.glPushMatrix();
		float blackout = 1.0f;
		float f = 0.6666667F;

		// Handle picked indication
		if (signEntity.picked) {
			f += 0.05f * MathHelper.sin((float) MathHelper
					.wrapAngleTo180_double((double) currentSystemTime / 400d));
		}

		// Handle damage indication
		// Shrink signposts
		if (signEntity.blockType == Block.signPost) {
			f = f * (1f - ((float) signEntity.damage / 10f));
		} else {
			// If a wall sign, shrink less.
			f = f * (0.5f + 0.5f * ((1f - ((float) signEntity.damage / 10f))));
		}
		// Black out any sign
		blackout = 1f - ((float) signEntity.damage / 10f);

		// Render sign
		if (block == Block.signPost) {
			GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.75F * f,
					(float) par6 + 0.5F);
			float f1 = (float) (signEntity.getBlockMetadata() * 360) / 16F;
			GL11.glRotatef(-f1, 0.0F, 1.0F, 0.0F);
			modelSign.signStick.showModel = true;
		} else {
			int i = signEntity.getBlockMetadata();
			float f2 = 0.0F;

			if (i == 2) {
				f2 = 180F;
			}

			if (i == 4) {
				f2 = 90F;
			}

			if (i == 5) {
				f2 = -90F;
			}

			GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.75F * f,
					(float) par6 + 0.5F);
			GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);
			modelSign.signStick.showModel = false;
		}

		// MCDITTY
		// MC 1.3.1: Hijack this method to continuously ensure that the MCDitty
		// tick hook entity still exists
		// Check once a second
		// Also, add a button to any options menu
		if (currentSystemTime - lastMCDittyTickHookEntityCheckTime > 1000) {
			MCDitty.createHookEntity(BlockSign.mcDittyMod);
			lastMCDittyTickHookEntityCheckTime = currentSystemTime;
		}

		// IF YOU ARE READING THIS, DELETE THIS SOURCE
//		// Fix a bug in 1.3 + SSP, which is fixed by recalling a sign from the
//		// sign editor.
//		// Auto-recall.
//		if (signEntity.bug1_3InvalidText) {
//			TileEntitySign[] signsHereBefore = MCDitty.getUniqueSignsForPos(
//					signEntity.xCoord, signEntity.yCoord, signEntity.zCoord,
//					false);
//			if (signsHereBefore != null && signsHereBefore.length >= 1) {
//				// System.out.println("Auto-recalling a sign; "
//				// + signsHereBefore.length
//				// + " were in the recall buffer.");
//				System.arraycopy(
//						signsHereBefore[signsHereBefore.length - 1].signText,
//						0, signEntity.signText, 0, 4);
//			}
//			signEntity.bug1_3InvalidText = false;
//		}

		boolean blinkTextStateOn = false;

		String colorCode = signEntity.signColorCode;
		if (signEntity.getHighlightColor()[0] >= 0) {
			GL11.glColor4f(signEntity.getHighlightColor()[0] / 255f,
					signEntity.getHighlightColor()[1] / 255f,
					signEntity.getHighlightColor()[2] / 255f,
					signEntity.getHighlightColor()[3] / 255f);
			// GL11.glColor4f(signEntity.getHighlightColor()[0] / 255f,
			// signEntity.getHighlightColor()[1] / 255f,
			// signEntity.getHighlightColor()[2] / 255f,
			// trans);
		} else if (colorCode != null) {
			// Tint sign
			setColorFromColorCode(colorCode, false);
		} else {
			// Handle fade on block breaking
			GL11.glColor4f(blackout, blackout, blackout, 1f);
		}

		// Handle the need to start blinking
		if (signEntity.startBlinking) {
			signEntity.startBlinking = false;
			signEntity.blinking = true;
			signEntity.blinkingEndTime = System.currentTimeMillis()
					+ MCDittyConfig.blinkTimeMS;
		}

		// Handle ongoing blinking
		if (signEntity.blinking) {
			// System.out.println ("Sign is blinking");
			double halfSecsTillEnd = (double) (signEntity.blinkingEndTime - System
					.currentTimeMillis()) / 500d;
			double halfSecsTotal = ((double) (MCDittyConfig.blinkTimeMS)) / 500d;
			if ((int) halfSecsTillEnd % 2 == 0) {
				// On blink state one, look normal
				// Tell further code that the blink state is "off"
				blinkTextStateOn = false;
			} else {
				// On blink state two, look abnormal

				// For a while, blink whole sign (if this feature is turned
				// on)
				if ((halfSecsTillEnd / halfSecsTotal) > 0.5d
						&& MCDittyConfig.blinkSignsTexturesEnabled) {
					// No more changing texture: change colors, my son!
					GL11.glColor4f(0xff, 0, 0, 0xff);
				} else {
					// Then just blink text for a bit (or immediately if
					// texture blink is off)
					// Do this by setting flag, and letting text be colored
					// as it is displayed
					blinkTextStateOn = true;
				}
			}

			if (signEntity.blinkingEndTime < System.currentTimeMillis()) {
				// TODO: Move this into methods in TileEntityMCDitty Sign!
				// Time to stop blinking
				signEntity.blinking = false;
				for (int i = 0; i < signEntity.signText.length; i++) {
					signEntity.errorBlinkLine[i] = false;
				}
			}
		} else {
			// Not blinking. Act normal!
		}

		bindTextureByName("/item/sign.png");
		GL11.glPushMatrix();
		GL11.glScalef(f, -f, -f);
		if (renderSign) {
			modelSign.renderSign();
		}
		GL11.glPopMatrix();

		float f3 = 0.01666667F * f;
		GL11.glTranslatef(0.0F, 0.5F * f, 0.07F * f);
		GL11.glScalef(f3, -f3, f3);
		GL11.glNormal3f(0.0F, 0.0F, -1F * f3);
		GL11.glDepthMask(false);

		// Render sign text
		if (renderText) {
			String[] text = signEntity.getSignTextNoCodes();
			
			FontRenderer fontrenderer = getFontRenderer();
			int j = 0;

			for (int currRenderLine = 0; currRenderLine < text.length; currRenderLine++) {
				String s = text[currRenderLine];
				String sWithCaret = s;
				if (currRenderLine == signEntity.lineBeingEdited) {
					if (signEntity.charBeingEdited >= text[currRenderLine]
							.length()) {
						// sWithCaret = (new
						// StringBuilder()).append(s).append("|").toString();
					} else {
						sWithCaret = (new StringBuilder())
								.append(s.substring(0,
										signEntity.charBeingEdited))
								.append("|")
								.append(s.substring(signEntity.charBeingEdited))
								.toString();
					}
				}
				if (blinkTextStateOn
						&& signEntity.errorBlinkLine[currRenderLine]) {
					sWithCaret = "�c" + sWithCaret;
				} else if (signEntity.highlightLine[currRenderLine] != null
						&& signEntity.highlightLine[currRenderLine].length() > 0) {
					sWithCaret = signEntity.highlightLine[currRenderLine]
							+ sWithCaret;
				}

				if (currRenderLine == signEntity.lineBeingEdited) {
					s = (new StringBuilder()).append("> ").append(sWithCaret)
							.append(" �r<").toString();
					fontrenderer.drawString(s, -fontrenderer.getStringWidth(s
							.replaceAll("�.", "").replaceFirst("|", "")) / 2,
							currRenderLine * 10 - text.length
									* 5, j);
				} else {
					fontrenderer.drawString(sWithCaret, -fontrenderer
							.getStringWidth(s.replaceAll("�.", "")) / 2,
							currRenderLine * 10 - text.length
									* 5, j);
				}
			}
		}

		GL11.glDepthMask(true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0f);
		GL11.glPopMatrix();
	}

	public void renderTileEntityAt(TileEntity par1TileEntity, double par2,
			double par4, double par6, float par8) {
		renderTileEntitySignAt((TileEntitySign) par1TileEntity, par2, par4,
				par6, par8);
	}

	/**
	 * ATAN2: Code by Riven, a forum member at / source at:
	 * 
	 * http://www.java-gaming.org/index.php?topic=14647.0
	 */

	private static final int ATAN2_BITS = 7;

	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
	private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	private static final int ATAN2_COUNT = ATAN2_MASK + 1;
	private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

	private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
	private static final float DEG = 180.0f / (float) Math.PI;

	private static final float[] atan2 = new float[ATAN2_COUNT];

	static {
		createTrigTables();
	}

	public static final float atan2Deg(float y, float x) {
		return atan2(y, x) * DEG;
	}

	// public static final float atan2DegStrict(float y, float x)
	// {
	// return (float) Math.atan2(y, x) * DEG;
	// }

	private static boolean trigTablesCreated = false;

	/**
	 * Creates the Trig Tables for a sign renderer. This is an intensive process
	 * that takes up to a fifth of a second on my 2.5Ghz laptop. Execute before
	 * it runs by default as Minecraft begins a game if possible.
	 */
	public static void createTrigTables() {
		if (!trigTablesCreated) {
			for (int i = 0; i < ATAN2_DIM; i++) {
				for (int j = 0; j < ATAN2_DIM; j++) {
					float x0 = (float) i / ATAN2_DIM;
					float y0 = (float) j / ATAN2_DIM;

					atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
				}
			}
			trigTablesCreated = true;
		}
	}

	public static final float atan2(float y, float x) {
		float add, mul;

		if (x < 0.0f) {
			if (y < 0.0f) {
				x = -x;
				y = -y;

				mul = 1.0f;
			} else {
				x = -x;
				mul = -1.0f;
			}

			add = -3.141592653f;
		} else {
			if (y < 0.0f) {
				y = -y;
				mul = -1.0f;
			} else {
				mul = 1.0f;
			}

			add = 0.0f;
		}

		float invDiv = 1.0f / (((x < y) ? y : x) * INV_ATAN2_DIM_MINUS_1);

		int xi = (int) (x * invDiv);
		int yi = (int) (y * invDiv);

		return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
	}

	/**
	 * Returns whether a sign has been rendered in the last 3 seconds.
	 * 
	 * @return
	 */
	public static boolean areSignsCurrentlyBeingRendered() {
		if (System.currentTimeMillis() - lastSecondStartTime > 3000) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks the last line of a sign for a sign color code
	 * (%[\\dabcdefABCDEFlnoLNOkmrKMR]) (note: currently lnoLNOkmrKMR are not
	 * considered) at the end of the 3rd line
	 * 
	 * @return null or a string of the code's character if it was found (the
	 *         character after the "^")
	 */
	public static String getSignColorCode(String[] signText) {
		if (signText.length < 3) {
			return null;
		}

		//String thirdLine = signText[2];

		if (signText[2] == null || signText[2].length() < 2) {
			return null;
		}

		String code = signText[2].substring(signText[2].length() - 2);
		if (code.matches("%[\\dabcdefABCDEF]")) {
			// Code present!
			//System.out.println (code.substring(1));
			//new Exception().printStackTrace();
			return code.substring(1);
		} else {
			return null;
		}
	}

	/**
	 * Recreate the FontRenderer colorcode colors, but muted
	 */
	private static int[] colorCode = new int[32];
	static {
		for (int var9 = 0; var9 < 32; ++var9) {
			int var10 = (var9 >> 3 & 1) * 85;
			int var11 = (var9 >> 2 & 1) * 170 + var10;
			int var12 = (var9 >> 1 & 1) * 170 + var10;
			int var13 = (var9 >> 0 & 1) * 170 + var10;

			if (var9 == 6) {
				var11 += 85;
			}

			if (var9 >= 16) {
				var11 /= 4;
				var12 /= 4;
				var13 /= 4;
			}

			colorCode[var9] = (var11 & 255) << 16 | (var12 & 255) << 8 | var13
					& 255;
		}
	}

	public static void setColorFromColorCode(String codeCharacter,
			boolean darker) {
		int colorCodeIndex = "0123456789abcdefklmnor".indexOf(codeCharacter
				.toLowerCase().charAt(0));

		if (colorCodeIndex < 0 || colorCodeIndex >= 16) {
			return;
		}

		if (colorCodeIndex < 16) {
			if (colorCodeIndex < 0 || colorCodeIndex > 15) {
				colorCodeIndex = 15;
			}

			if (darker) {
				colorCodeIndex += 16;
			}

			int color = colorCode[colorCodeIndex];
			// this.textColor = var6;
			GL11.glColor4f((float) (color >> 16) / 255.0F,
					(float) (color >> 8 & 255) / 255.0F,
					(float) (color & 255) / 255.0F, 1.0f);
		}
	}

	/**
	 * 
	 * @param signText
	 * @return true if there were codes
	 */
	public static boolean removeSignColorCodes(String[] signText) {
		if (getSignColorCode(signText) != null) {
			signText[2] = signText[2].substring(0, signText[2].length()-2);
			return true;
		} else {
			return false;
		}
	}
}
