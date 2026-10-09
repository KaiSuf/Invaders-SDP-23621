package engine;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import entity.EnemyShip;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import screen.MenuItem;
import screen.Screen;
import entity.Coin;
import entity.Entity;
import entity.Meteor;
import entity.Ship;

/**
 * Manages screen drawing.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class DrawManager {

	/** Singleton instance of the class. */
	private static DrawManager instance;
	/** Current frame. */
	private static Frame frame;
	/** FileManager instance. */
	private static FileManager fileManager;
	/** Application logger. */
	private static Logger logger;
	/** Graphics context. */
	private static Graphics graphics;
	/** Buffer Graphics. */
	private static Graphics backBufferGraphics;
	/** Buffer image. */
	private static BufferedImage backBuffer;
	/** Current world offset X (screen shake). */
	private static int worldOffsetX;
	/** Current world offset Y (screen shake). */
	private static int worldOffsetY;
	/** Normal sized font. */
	private static Font fontRegular;
	/** Normal sized font properties. */
	private static FontMetrics fontRegularMetrics;
	/** Big sized font. */
	private static Font fontBig;
	/** Big sized font properties. */
	private static FontMetrics fontBigMetrics;

	/** Sprite types mapped to their images. */
	private static Map<SpriteType, boolean[][]> spriteMap;

	/** Sprite types. */
	public static enum SpriteType {
		/** Player ship. */
		Ship,
		/** Destroyed player ship. */
		ShipDestroyed,
		/** Player bullet. */
		Bullet,
		/** Enemy bullet. */
		EnemyBullet,
		/** First enemy ship - first form. */
		EnemyShipA1,
		/** First enemy ship - second form. */
		EnemyShipA2,
		/** Second enemy ship - first form. */
		EnemyShipB1,
		/** Second enemy ship - second form. */
		EnemyShipB2,
		/** Third enemy ship - first form. */
		EnemyShipC1,
		/** Third enemy ship - second form. */
		EnemyShipC2,
		/** Bonus ship. */
		EnemyShipSpecial,
		/** Destroyed enemy ship. */
		Explosion,
		/** First Flight achievement icon. */
		FirstFlight
	};

	/**
	 * Private constructor.
	 */
	private DrawManager() {
		fileManager = Core.getFileManager();
		logger = Core.getLogger();
		logger.info("Started loading resources.");

		try {
			spriteMap = new LinkedHashMap<SpriteType, boolean[][]>();

			spriteMap.put(SpriteType.Ship, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipDestroyed, new boolean[13][8]);
			spriteMap.put(SpriteType.Bullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyBullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyShipA1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipA2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipSpecial, new boolean[16][7]);
			spriteMap.put(SpriteType.Explosion, new boolean[13][7]);
			spriteMap.put(SpriteType.FirstFlight, new boolean[11][8]);

			fileManager.loadSprite(spriteMap);
			logger.info("Finished loading the sprites.");

			// Font loading.
			fontRegular = fileManager.loadFont(14f);
			fontBig = fileManager.loadFont(24f);
			logger.info("Finished loading the fonts.");

		} catch (IOException e) {
			logger.warning("Loading failed.");
			fontRegular = new Font(Font.MONOSPACED, Font.PLAIN, 14);
			fontBig = new Font(Font.MONOSPACED, Font.PLAIN, 24);
		} catch (FontFormatException e) {
			logger.warning("Font formating failed.");
			fontRegular = new Font(Font.MONOSPACED, Font.PLAIN, 14);
			fontBig = new Font(Font.MONOSPACED, Font.PLAIN, 24);
		}
	}

	/**
	 * Returns shared instance of DrawManager.
	 *
	 * @return Shared instance of DrawManager.
	 */
	protected static DrawManager getInstance() {
		if (instance == null)
			instance = new DrawManager();
		return instance;
	}

	/**
	 * Sets the frame to draw the image on.
	 *
	 * @param currentFrame
	 *            Frame to draw on.
	 */
	public void setFrame(final Frame currentFrame) {
		frame = currentFrame;
	}

	/**
	 * First part of the drawing process. Initialices buffers, draws the
	 * background and prepares the images.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	public void initDrawing(final Screen screen) {
		backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		graphics = frame.getGraphics();
		backBufferGraphics = backBuffer.getGraphics();
		worldOffsetX = 0;
		worldOffsetY = 0;

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics
				.fillRect(0, 0, screen.getWidth(), screen.getHeight());

		fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
		fontBigMetrics = backBufferGraphics.getFontMetrics(fontBig);

		// drawBorders(screen);
		// drawGrid(screen);
	}

	/**
	 * Draws the completed drawing on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void completeDrawing(final Screen screen) {
		setWorldOffset(0, 0);
		graphics.drawImage(backBuffer, frame.getInsets().left,
				frame.getInsets().top, frame);
	}

	/**
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * Moves everything drawn after this call by the given offset.
	 * Used for screen shake. Call setWorldOffset(0, 0) before the HUD.
	 *
	 * @param offsetX
	 *            X offset in pixels.
	 * @param offsetY
	 *            Y offset in pixels.
	 */
	public void setWorldOffset(final int offsetX, final int offsetY) {
		backBufferGraphics.translate(offsetX - worldOffsetX,
				offsetY - worldOffsetY);
		worldOffsetX = offsetX;
		worldOffsetY = offsetY;
	}

	/**
	 * Draws an entity, using the apropiate image.
	 *
	 * @param entity
	 *            Entity to be drawn.
	 * @param positionX
	 *            Coordinates for the left side of the image.
	 * @param positionY
	 *            Coordinates for the upper side of the image.
	 */
	public void drawEntity(final Entity entity, final int positionX,
	                       final int positionY) {
		// Enemy ships can be see-through while flickering / fading out.
		float alpha = 1f;
		if (entity instanceof EnemyShip)
			alpha = ((EnemyShip) entity).getAlpha();
		if (alpha <= 0f)
			return;

		Graphics2D g2d = (Graphics2D) backBufferGraphics;
		g2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha));

		drawSprite(entity.getSpriteType(), positionX, positionY,
				entity.getColor());

		g2d.setComposite(AlphaComposite.SrcOver); // back to normal
	}

	/**
	 * Draws a sprite using the game's standard two-pixel scale.
	 *
	 * @param spriteType Sprite to draw.
	 * @param positionX Coordinates for the left side of the image.
	 * @param positionY Coordinates for the upper side of the image.
	 * @param color Color used for filled pixels.
	 */
	public void drawSprite(final SpriteType spriteType, final int positionX,
	                       final int positionY, final Color color) {
		boolean[][] image = spriteMap.get(spriteType);

		backBufferGraphics.setColor(color);

		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.drawRect(positionX + i * 2,
							positionY + j * 2, 1, 1);
	}


	/**
	 * Draws regular text at an exact position, left aligned.
	 *
	 * @param string    Text to draw.
	 * @param positionX Horizontal position of the left edge.
	 * @param positionY Vertical position of the baseline.
	 * @param color     Colour of the text.
	 */
	public void drawRegularString(final String string, final int positionX,
	                              final int positionY, final Color color) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(color);
		backBufferGraphics.drawString(string, positionX, positionY);
	}

	/**
	 * Draws an empty rectangle outline.
	 *
	 * @param positionX Horizontal position of the left edge.
	 * @param positionY Vertical position of the top edge.
	 * @param width     Width of the box.
	 * @param height    Height of the box.
	 * @param color     Colour of the outline.
	 */
	public void drawBox(final int positionX, final int positionY,
	                    final int width, final int height, final Color color) {
		backBufferGraphics.setColor(color);
		backBufferGraphics.drawRect(positionX, positionY, width, height);
	}

	/**
	 * Draws a dropped coin as a filled circle (GoG - Currency System).
	 * Coins have no entry in the shared sprite file, so they are drawn
	 * here instead of through drawEntity().
	 *
	 * @param coin
	 *            Coin to be drawn.
	 * @param positionX
	 *            Coordinates for the left side of the coin.
	 * @param positionY
	 *            Coordinates for the upper side of the coin.
	 */
	public void drawCoin(final Coin coin, final int positionX,
	                     final int positionY) {
		backBufferGraphics.setColor(coin.getColor());
		backBufferGraphics.fillOval(positionX, positionY, coin.getWidth(),
				coin.getHeight());
	}

	// Bonus meteor drawing. AUTHORED BY: VFX TEAM (Effection)
	/**
	 * Meteor rock surface, 9x9 cells of 2 px: # rock, o crater, . empty.
	 * The lumpy outline and craters rotate as the rock spins; light and
	 * shadow are added when drawing, so they stay fixed (lit from the
	 * top left) like on a real tumbling rock.
	 */
	private static final String[] METEOR_SHAPE = {
			"...###...",
			".######..",
			".##o####.",
			"###oo####",
			"####o####",
			"##o######",
			".#####o#.",
			"..#####..",
			"...###..." };
	/** Center cell of the meteor shape. */
	private static final int METEOR_MID = 4;
	/** Lit side of the rock. */
	private static final Color METEOR_HIGHLIGHT = new Color(176, 150, 128);
	/** Main rock color. */
	private static final Color METEOR_ROCK = new Color(124, 100, 84);
	/** Shaded side of the rock. */
	private static final Color METEOR_SHADOW = new Color(78, 60, 52);
	/** Crater color. */
	private static final Color METEOR_CRATER = new Color(52, 40, 36);
	/** Hot glowing rim on the meteor's leading side. */
	private static final Color METEOR_RIM = new Color(255, 150, 60);
	/** Flame colors: white-hot core, yellow, orange, red edge. */
	private static final Color[] METEOR_FLAME = { new Color(255, 250, 220),
			new Color(255, 225, 90), new Color(255, 150, 40),
			new Color(220, 60, 30) };
	/** Random source for the flame flicker. */
	private static final java.util.Random FLICKER = new java.util.Random();

	/**
	 * Draws the bonus meteor: the flying rock with its glow and flame
	 * tail, or its burst and coin popup once shot.
	 *
	 * @param meteor
	 *            Meteor to draw.
	 */
	public void drawMeteor(final Meteor meteor) {
		if (meteor.isBursting())
			drawMeteorBurst(meteor);
		else
			drawMeteorFlying(meteor);
		((Graphics2D) backBufferGraphics).setComposite(AlphaComposite.SrcOver);
	}

	/**
	 * Sets the transparency of everything drawn next.
	 *
	 * @param alpha
	 *            Opacity, 0 (invisible) to 1 (solid).
	 */
	private void setMeteorAlpha(final float alpha) {
		((Graphics2D) backBufferGraphics).setComposite(AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER,
						Math.max(0f, Math.min(1f, alpha))));
	}

	/**
	 * Draws a flying meteor: embers, layered flame tail, pulsing glow and
	 * the shaded pixel rock with a hot leading edge.
	 *
	 * @param meteor
	 *            Meteor to draw.
	 */
	private void drawMeteorFlying(final Meteor meteor) {
		int x = meteor.getPositionX();
		int y = meteor.getPositionY();
		int size = meteor.getWidth();
		int dir = meteor.getDirection();
		float slope = meteor.getFallSlope();
		int centerX = x + size / 2;
		int centerY = y + size / 2;
		long now = System.currentTimeMillis();

		// Tail: one column of flame cells every 2 px, going back along the
		// flight path (up and behind). Each column has a white-hot middle,
		// yellow and orange layers, and a red edge; it narrows and fades
		// with distance and flickers every frame.
		int tailLength = 16 + FLICKER.nextInt(5);
		for (int i = 0; i < tailLength; i++) {
			float t = (float) i / tailLength;
			int back = size / 2 - 2 + i * 2;
			int cellX = centerX - dir * back;
			int cellY = centerY - 1 - Math.round(slope * back);
			int half = Math.max(0, Math.round(5 * (1f - t)));
			for (int j = -half; j <= half; j++) {
				if (FLICKER.nextFloat() > 0.95f - t * 0.55f)
					continue;
				// 0 = middle of the flame, 1 = its edge, shifted to cooler
				// colors further from the rock.
				float edge = half == 0 ? 1f : (float) Math.abs(j) / half;
				int layer = Math.min(METEOR_FLAME.length - 1,
						(int) (edge * 2 + t * 2.2f));
				setMeteorAlpha(0.95f * (1f - t * 0.85f));
				backBufferGraphics.setColor(METEOR_FLAME[layer]);
				backBufferGraphics.fillRect(cellX,
						cellY + j * 2 + FLICKER.nextInt(2) - 1, 2, 2);
			}
		}

		// Embers: a few loose sparks drifting behind the tail.
		for (int i = 0; i < 4; i++) {
			int back = size / 2 + 10 + FLICKER.nextInt(tailLength * 2 + 10);
			setMeteorAlpha(0.3f + FLICKER.nextFloat() * 0.5f);
			backBufferGraphics.setColor(METEOR_FLAME[1 + FLICKER.nextInt(3)]);
			backBufferGraphics.fillRect(centerX - dir * back,
					centerY - Math.round(slope * back) + FLICKER.nextInt(13) - 6,
					2, 2);
		}

		// Heat glow: a soft warm halo that gently pulses.
		float pulse = 0.5f + 0.5f * (float) Math.sin(now / 90.0);
		backBufferGraphics.setColor(METEOR_FLAME[1]);
		setMeteorAlpha(0.12f + 0.08f * pulse);
		backBufferGraphics.fillOval(x - 3, y - 3, size + 6, size + 6);
		setMeteorAlpha(1f);

		// Spinning pixel rock. Each screen cell looks up which surface cell
		// has rotated into it; shading comes from the cell's place on
		// screen. The edge facing the travel direction (and the bottom, as
		// it falls) glows hot from entering the atmosphere.
		float cos = (float) Math.cos(meteor.getRotation());
		float sin = (float) Math.sin(meteor.getRotation());
		for (int row = 0; row < METEOR_SHAPE.length; row++)
			for (int col = 0; col < METEOR_SHAPE.length; col++) {
				char cell = meteorCellAt(row, col, cos, sin);
				if (cell == '.')
					continue;
				boolean leading = meteorCellAt(row, col + dir, cos, sin) == '.';
				if (slope > 0.5f)
					leading = leading
							|| meteorCellAt(row + 1, col, cos, sin) == '.';
				int light = (col - METEOR_MID) + (row - METEOR_MID);
				Color color;
				if (leading)
					color = pulse > 0.5f ? METEOR_FLAME[1] : METEOR_RIM;
				else if (cell == 'o')
					color = METEOR_CRATER;
				else if (light <= -3)
					color = METEOR_HIGHLIGHT;
				else if (light >= 3)
					color = METEOR_SHADOW;
				else
					color = METEOR_ROCK;
				backBufferGraphics.setColor(color);
				backBufferGraphics.fillRect(x + col * 2, y + row * 2, 2, 2);
			}
	}
	/**
	 * Finds which cell of the rock surface is shown at a screen cell when
	 * the rock is rotated.
	 *
	 * @param row
	 *            Screen cell row inside the meteor.
	 * @param col
	 *            Screen cell column inside the meteor.
	 * @param cos
	 *            Cosine of the rock's rotation.
	 * @param sin
	 *            Sine of the rock's rotation.
	 * @return Surface cell ('#', 'o') or '.' when empty or outside.
	 */
	private char meteorCellAt(final int row, final int col, final float cos,
			final float sin) {
		int dx = col - METEOR_MID;
		int dy = row - METEOR_MID;
		// Rotate backwards to find where this cell came from.
		int srcCol = METEOR_MID + Math.round(cos * dx + sin * dy);
		int srcRow = METEOR_MID + Math.round(-sin * dx + cos * dy);
		if (srcRow < 0 || srcRow >= METEOR_SHAPE.length || srcCol < 0
				|| srcCol >= METEOR_SHAPE[srcRow].length())
			return '.';
		return METEOR_SHAPE[srcRow].charAt(srcCol);
	}

	/**
	 * Draws a shot meteor: a flash, an expanding shockwave ring, rock
	 * pieces falling away, spark streaks, and a "+N" coin popup.
	 *
	 * @param meteor
	 *            Meteor to draw.
	 */
	private void drawMeteorBurst(final Meteor meteor) {
		long elapsed = meteor.getBurstElapsed();
		float progress = Math.min(1f, (float) elapsed / Meteor.BURST_DURATION);
		float fade = 1f - progress;
		int centerX = meteor.getPositionX() + meteor.getWidth() / 2;
		int centerY = meteor.getPositionY() + meteor.getHeight() / 2;

		// Flash at the moment of impact.
		if (progress < 0.15f) {
			int radius = 6 + (int) (progress * 70);
			setMeteorAlpha(0.85f * (1f - progress / 0.15f));
			backBufferGraphics.setColor(METEOR_FLAME[0]);
			backBufferGraphics.fillOval(centerX - radius, centerY - radius,
					radius * 2, radius * 2);
		}

		// Shockwave ring growing outward.
		if (progress < 0.5f) {
			int radius = 8 + (int) (progress * 2 * 34);
			setMeteorAlpha(0.7f * (1f - progress * 2));
			backBufferGraphics.setColor(METEOR_FLAME[2]);
			backBufferGraphics.drawOval(centerX - radius, centerY - radius,
					radius * 2, radius * 2);
		}

		// Rock pieces: half the sparks are slower chunks that fall.
		for (int i = 0; i < meteor.getSparkCount(); i += 2) {
			float dx = meteor.getSparkSpeedX(i) * 0.45f * elapsed;
			float dy = meteor.getSparkSpeedY(i) * 0.45f * elapsed
					+ 0.00006f * elapsed * elapsed;
			setMeteorAlpha(fade * 1.3f);
			backBufferGraphics.setColor(i % 4 == 0 ? METEOR_ROCK
					: METEOR_SHADOW);
			backBufferGraphics.fillRect(centerX + Math.round(dx) - 2,
					centerY + Math.round(dy) - 2, 4, 4);
		}

		// Sparks: short streaks flying outward, yellow turning red.
		backBufferGraphics.setColor(METEOR_FLAME[Math.min(
				METEOR_FLAME.length - 1, 1 + (int) (progress * 3))]);
		setMeteorAlpha(fade);
		for (int i = 1; i < meteor.getSparkCount(); i += 2) {
			float speedX = meteor.getSparkSpeedX(i);
			float speedY = meteor.getSparkSpeedY(i);
			int headX = centerX + Math.round(speedX * elapsed);
			int headY = centerY + Math.round(speedY * elapsed);
			int tailX = centerX + Math.round(speedX * elapsed * 0.7f);
			int tailY = centerY + Math.round(speedY * elapsed * 0.7f);
			backBufferGraphics.drawLine(tailX, tailY, headX, headY);
			backBufferGraphics.fillRect(headX - 1, headY - 1, 2, 2);
		}

		// "+N" coin popup floating up, with a dark outline so it is easy
		// to read over enemies.
		String popup = "+" + meteor.getCoinValue();
		backBufferGraphics.setFont(fontRegular);
		int textWidth = fontRegularMetrics.stringWidth(popup);
		int iconSize = 9;
		int popupX = centerX - (textWidth + iconSize + 3) / 2;
		int popupY = centerY - 8 - (int) (progress * 32);
		int textX = popupX + iconSize + 3;
		setMeteorAlpha(1f - progress * progress);
		backBufferGraphics.setColor(Color.BLACK);
		for (int ox = -1; ox <= 1; ox++)
			for (int oy = -1; oy <= 1; oy++)
				if (ox != 0 || oy != 0)
					backBufferGraphics.drawString(popup, textX + ox,
							popupY + oy);
		backBufferGraphics.fillOval(popupX - 1, popupY - iconSize - 1,
				iconSize + 2, iconSize + 2);
		backBufferGraphics.setColor(Color.YELLOW);
		backBufferGraphics.fillOval(popupX, popupY - iconSize, iconSize,
				iconSize);
		backBufferGraphics.setColor(new Color(255, 255, 200));
		backBufferGraphics.fillRect(popupX + 2, popupY - iconSize + 2, 2, 2);
		backBufferGraphics.setColor(Color.YELLOW);
		backBufferGraphics.drawString(popup, textX, popupY);
	}


	/**
	 * Draws a coin balance as a small coin icon followed by the amount,
	 * centered along the top bar of the screen (GoG - Currency System).
	 * Used by the in-game HUD and the shop.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param coins
	 *            Coin balance to display.
	 */
	public void drawCoinBalance(final Screen screen, final int coins) {
		drawCoinBalance(screen, coins, 25);
	}

	/**
	 * Draws a coin balance as a small coin icon followed by the amount,
	 * centered horizontally at the given baseline (GoG - Currency System).
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param coins
	 *            Coin balance to display.
	 * @param positionY
	 *            Baseline Y coordinate of the text.
	 */
	public void drawCoinBalance(final Screen screen, final int coins,
	                            final int positionY) {
		final int iconSize = 14;
		final int iconTextGap = 6;

		backBufferGraphics.setFont(fontRegular);
		String balanceString = Integer.toString(coins);
		int totalWidth = iconSize + iconTextGap
				+ fontRegularMetrics.stringWidth(balanceString);
		int startX = (screen.getWidth() - totalWidth) / 2;

		backBufferGraphics.setColor(Color.YELLOW);
		backBufferGraphics.fillOval(startX, positionY - iconSize + 1,
				iconSize, iconSize);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(balanceString, startX + iconSize
				+ iconTextGap, positionY);
	}

	/**
	 * Draws a diamond balance as a small diamond icon followed by the
	 * amount, centered horizontally at the given baseline, so it can be
	 * stacked with the coin balance (GoG - Currency System).
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param diamonds
	 *            Diamond balance to display.
	 * @param positionY
	 *            Baseline Y coordinate of the text.
	 */
	public void drawDiamondBalance(final Screen screen, final int diamonds,
	                               final int positionY) {
		final int iconSize = 14;
		final int iconTextGap = 6;

		backBufferGraphics.setFont(fontRegular);
		String balanceString = Integer.toString(diamonds);
		int totalWidth = iconSize + iconTextGap
				+ fontRegularMetrics.stringWidth(balanceString);
		int startX = (screen.getWidth() - totalWidth) / 2;
		int iconTop = positionY - iconSize + 1;

		int[] xPoints = { startX + iconSize / 2, startX + iconSize,
				startX + iconSize / 2, startX };
		int[] yPoints = { iconTop, iconTop + iconSize / 2,
				iconTop + iconSize, iconTop + iconSize / 2 };

		backBufferGraphics.setColor(Color.CYAN);
		backBufferGraphics.fillPolygon(xPoints, yPoints, 4);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(balanceString, startX + iconSize
				+ iconTextGap, positionY);
	}

	/**
	 * For debugging purpouses, draws the canvas borders.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawBorders(final Screen screen) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, 0, screen.getWidth() - 1, 0);
		backBufferGraphics.drawLine(0, 0, 0, screen.getHeight() - 1);
		backBufferGraphics.drawLine(screen.getWidth() - 1, 0,
				screen.getWidth() - 1, screen.getHeight() - 1);
		backBufferGraphics.drawLine(0, screen.getHeight() - 1,
				screen.getWidth() - 1, screen.getHeight() - 1);
	}

	/**
	 * For debugging purpouses, draws a grid over the canvas.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawGrid(final Screen screen) {
		backBufferGraphics.setColor(Color.DARK_GRAY);
		for (int i = 0; i < screen.getHeight() - 1; i += 2)
			backBufferGraphics.drawLine(0, i, screen.getWidth() - 1, i);
		for (int j = 0; j < screen.getWidth() - 1; j += 2)
			backBufferGraphics.drawLine(j, 0, j, screen.getHeight() - 1);
	}

	/**
	 * Draws current score on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Current score.
	 */
	public void drawScore(final Screen screen, final int score) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		String scoreString = String.format("%04d", score);
		backBufferGraphics.drawString(scoreString, screen.getWidth() - 60, 25);
	}

	/**
	 * Draws number of remaining lives on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param lives
	 *            Current lives.
	 */
	public void drawLives(final Screen screen, final int lives) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(Integer.toString(lives), 20, 25);
		Ship dummyShip = new Ship(0, 0);
		for (int i = 0; i < lives; i++)
			drawEntity(dummyShip, 40 + 35 * i, 10);
	}

	/**
	 * Draws a thick line from side to side of the screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param positionY
	 *            Y coordinate of the line.
	 */
	public void drawHorizontalLine(final Screen screen, final int positionY) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, positionY, screen.getWidth(), positionY);
		backBufferGraphics.drawLine(0, positionY + 1, screen.getWidth(),
				positionY + 1);
	}

	/**
	 * Draws game title.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawTitle(final Screen screen) {
		String titleString = "Invaders";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, titleString, screen.getHeight() / 3);
	}

	/**
	 * Draws main menu.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param selected
	 *            Item the cursor is on.
	 */
	public void drawMenu(final Screen screen, final MenuItem selected) {
		for (MenuItem item : MenuItem.values()) {
			if (item == selected)
				backBufferGraphics.setColor(Color.GREEN);
			else if (!item.isEnabled())
				backBufferGraphics.setColor(Color.DARK_GRAY);
			else
				backBufferGraphics.setColor(Color.WHITE);
			drawCenteredRegularString(screen, item.getTitle(),
					menuItemBaseline(screen, item.ordinal()));
		}
	}

	/**
	 * Finds the menu item drawn at a given height. Each item owns a full-width
	 * row as tall as the spacing between items, so the whole row is clickable,
	 * not just the text.
	 *
	 * @param screen
	 *            Screen the menu is drawn on.
	 * @param positionY
	 *            Height to check, in screen coordinates.
	 * @return Item on that row, or null if there is none or nothing has been
	 *         drawn yet.
	 */
	public MenuItem menuItemAt(final Screen screen, final int positionY) {
		if (fontRegularMetrics == null)
			return null;
		int spacing = menuItemSpacing();
		for (MenuItem item : MenuItem.values()) {
			int top = menuItemBaseline(screen, item.ordinal())
					- fontRegularMetrics.getAscent()
					- (spacing - fontRegularMetrics.getHeight()) / 2;
			if (positionY >= top && positionY < top + spacing)
				return item;
		}
		return null;
	}

	/**
	 * Height of the baseline of a menu item. Drawing and hit-testing both use
	 * this, so they cannot drift apart.
	 *
	 * @param screen
	 *            Screen the menu is drawn on.
	 * @param index
	 *            Position of the item in the menu, from the top.
	 * @return Baseline of the item's text.
	 */
	private int menuItemBaseline(final Screen screen, final int index) {
		return screen.getHeight() / 2 + menuItemSpacing() * (index + 2);
	}

	/**
	 * Distance between two menu items.
	 *
	 * @return Spacing, in pixels.
	 */
	private int menuItemSpacing() {
		return fontRegularMetrics.getHeight() * 5 / 4;
	}

	/**
	 * Draws the keys available on the current screen, one line along the
	 * bottom.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param hints
	 *            Keys to show, already worded for the current state.
	 */
	public void drawKeyHints(final Screen screen, final String hints) {
		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, hints,
				screen.getHeight() - fontRegularMetrics.getHeight());
	}

	/**
	 * Draws the exit confirmation over the menu. Filled first so the menu
	 * behind it does not show through.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param yesSelected
	 *            Whether the cursor is on Yes.
	 */
	public void drawExitConfirm(final Screen screen,
	                            final boolean yesSelected) {
		String question = "Really quit?";
		String yesString = "Yes";
		String noString = "No";

		int spacing = menuItemSpacing();
		int boxWidth = screen.getWidth() / 2;
		int boxHeight = spacing * 4;
		int boxX = (screen.getWidth() - boxWidth) / 2;
		int boxY = (screen.getHeight() - boxHeight) / 2;

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(boxX, boxY, boxWidth, boxHeight);
		backBufferGraphics.setColor(Color.GRAY);
		backBufferGraphics.drawRect(boxX, boxY, boxWidth, boxHeight);

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, question, boxY + spacing * 3 / 2);

		// Yes and No sit either side of the centre, so they need their own
		// x positions rather than the centred helper.
		int answerY = boxY + spacing * 3;
		int yesX = screen.getWidth() / 2 - boxWidth / 4
				- fontRegularMetrics.stringWidth(yesString) / 2;
		int noX = screen.getWidth() / 2 + boxWidth / 4
				- fontRegularMetrics.stringWidth(noString) / 2;

		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(yesSelected ? Color.GREEN : Color.WHITE);
		backBufferGraphics.drawString(yesString, yesX, answerY);
		backBufferGraphics.setColor(yesSelected ? Color.WHITE : Color.GREEN);
		backBufferGraphics.drawString(noString, noX, answerY);
	}

	/**
	 * Draws an achievement-unlocked popup over the game.
	 *
	 * @param screen Screen where the popup is drawn.
	 * @param achievement Newly unlocked achievement.
	 * @param elapsedMilliseconds Time since the popup started.
	 * @param durationMilliseconds Total popup duration.
	 * @param slideInMilliseconds Slide-in duration.
	 * @param slideOutMilliseconds Slide-out duration.
	 */
	public void drawAchievementUnlocked(final Screen screen,
	                                    final Achievement achievement, final long elapsedMilliseconds,
	                                    final int durationMilliseconds, final int slideInMilliseconds,
	                                    final int slideOutMilliseconds) {
		int boxWidth = 218;
		int boxHeight = 44;
		int visibleX = screen.getWidth() - boxWidth - 6;
		int hiddenX = screen.getWidth() + 2;
		int boxY = 46;
		int boxX = visibleX;

		if (elapsedMilliseconds < slideInMilliseconds)
			boxX = hiddenX - (hiddenX - visibleX) * (int) elapsedMilliseconds
					/ slideInMilliseconds;
		else if (elapsedMilliseconds > durationMilliseconds
				- slideOutMilliseconds)
			boxX = visibleX + (hiddenX - visibleX) * (int) (elapsedMilliseconds
					- (durationMilliseconds - slideOutMilliseconds))
					/ slideOutMilliseconds;

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(boxX, boxY, boxWidth, boxHeight);
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawRect(boxX, boxY, boxWidth, boxHeight);
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawString("ACHIEVEMENT UNLOCKED", boxX + 34,
				boxY + 16);
		drawSprite(achievement.getSpriteType(), boxX + 8, boxY + 23,
				Color.YELLOW);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(achievement.getName(), boxX + 34,
				boxY + 35);
	}

	/**
	 * Draws an achievement icon alongside its name, status, and description.
	 *
	 * @param screen Screen where the achievement is drawn.
	 * @param achievement Achievement to display.
	 */
	public void drawAchievement(final Screen screen,
	                            final Achievement achievement) {
		int iconX = screen.getWidth() / 5;
		int contentX = iconX + 40;
		int nameY = screen.getHeight() / 2;
		String status = achievement.isUnlocked() ? "UNLOCKED" : "LOCKED";

		drawSprite(achievement.getSpriteType(), iconX, nameY - 20,
				achievement.isUnlocked() ? Color.YELLOW : Color.DARK_GRAY);
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(achievement.isUnlocked() ? Color.WHITE
				: Color.GRAY);
		backBufferGraphics.drawString(achievement.getName() + " - " + status,
				contentX, nameY);
		backBufferGraphics.setColor(Color.GRAY);
		backBufferGraphics.drawString("Unlock: defeat "
						+ achievement.getRequiredEnemyKills() + " enemies.", contentX,
				nameY + fontRegularMetrics.getHeight() * 2);
	}

	/**
	 * Draws the title of a screen reached from the main menu, in the same
	 * colour and place as the high score screen's title. Sets its own colour,
	 * so it does not depend on what was drawn before it.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param title
	 *            Title to draw.
	 */
	public void drawScreenTitle(final Screen screen, final String title) {
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, title, screen.getHeight() / 8);
	}

	/**
	 * Draws game results.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Score obtained.
	 * @param livesRemaining
	 *            Lives remaining when finished.
	 * @param shipsDestroyed
	 *            Total ships destroyed.
	 * @param accuracy
	 *            Total accuracy.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawResults(final Screen screen, final int score,
	                        final int livesRemaining, final int shipsDestroyed,
	                        final float accuracy, final boolean isNewRecord) {
		String scoreString = String.format("score %04d", score);
		String livesRemainingString = "lives remaining " + livesRemaining;
		String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
		String accuracyString = String
				.format("accuracy %.2f%%", accuracy * 100);

		int height = isNewRecord ? 4 : 2;

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, scoreString, screen.getHeight()
				/ height);
		drawCenteredRegularString(screen, livesRemainingString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 2);
		drawCenteredRegularString(screen, shipsDestroyedString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 4);
		drawCenteredRegularString(screen, accuracyString, screen.getHeight()
				/ height + fontRegularMetrics.getHeight() * 6);
	}

	/**
	 * Draws interactive characters for name input.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param name
	 *            Current name selected.
	 * @param nameCharSelected
	 *            Current character selected for modification.
	 */
	public void drawNameInput(final Screen screen, final char[] name,
	                          final int nameCharSelected) {
		String newRecordString = "New Record!";
		String introduceNameString = "Introduce name:";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredRegularString(screen, newRecordString, screen.getHeight()
				/ 4 + fontRegularMetrics.getHeight() * 10);
		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, introduceNameString,
				screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 12);

		// 3 letters name.
		int positionX = screen.getWidth()
				/ 2
				- (fontRegularMetrics.getWidths()[name[0]]
				+ fontRegularMetrics.getWidths()[name[1]]
				+ fontRegularMetrics.getWidths()[name[2]]
				+ fontRegularMetrics.getWidths()[' ']) / 2;

		for (int i = 0; i < 3; i++) {
			if (i == nameCharSelected)
				backBufferGraphics.setColor(Color.GREEN);
			else
				backBufferGraphics.setColor(Color.WHITE);

			positionX += fontRegularMetrics.getWidths()[name[i]] / 2;
			positionX = i == 0 ? positionX
					: positionX
					  + (fontRegularMetrics.getWidths()[name[i - 1]]
						 + fontRegularMetrics.getWidths()[' ']) / 2;

			backBufferGraphics.drawString(Character.toString(name[i]),
					positionX,
					screen.getHeight() / 4 + fontRegularMetrics.getHeight()
							* 14);
		}
	}

	/**
	 * Draws basic content of game over screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param acceptsInput
	 *            If the screen accepts input.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawGameOver(final Screen screen, final boolean acceptsInput,
	                         final boolean isNewRecord) {
		String gameOverString = "Game Over";
		String continueOrExitString =
				"Press Space to play again, Escape to exit";

		int height = isNewRecord ? 4 : 2;

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, gameOverString, screen.getHeight()
				/ height - fontBigMetrics.getHeight() * 2);

		if (acceptsInput)
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, continueOrExitString,
				screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 10);
	}

	/**
	 * Draws high score screen title and instructions.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawHighScoreMenu(final Screen screen) {
		String highScoreString = "High Scores";
		String instructionsString = "Press Space to return";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, highScoreString, screen.getHeight() / 8);

		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString,
				screen.getHeight() / 5);
	}

	/**
	 * Draws high scores.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param highScores
	 *            List of high scores.
	 */
	public void drawHighScores(final Screen screen,
	                           final List<Score> highScores) {
		backBufferGraphics.setColor(Color.WHITE);
		int i = 0;
		String scoreString = "";

		for (Score score : highScores) {
			scoreString = String.format("%s        %04d", score.getName(),
					score.getScore());
			drawCenteredRegularString(screen, scoreString, screen.getHeight()
					/ 4 + fontRegularMetrics.getHeight() * (i + 1) * 2);
			i++;
		}
	}

	/**
	 * Draws a centered string on regular font.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredRegularString(final Screen screen,
	                                      final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Draws a centered string on big font.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredBigString(final Screen screen, final String string,
	                                  final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontBigMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 * Draws an entity shrunk around its center and faded, used when enemies
	 * disappear on game over.
	 *
	 * @param entity
	 *            Entity to be drawn.
	 * @param scale
	 *            Size of the entity, from 0 (gone) to 1 (normal size).
	 */
	public void drawEntityShrunk(final Entity entity, final double scale) {
		if (scale <= 0)
			return;
		boolean[][] image = spriteMap.get(entity.getSpriteType());
		Color color = entity.getColor();
		int alpha = (int) (255 * Math.min(1, scale));

		double centerX = entity.getPositionX() + entity.getWidth() / 2.0;
		double centerY = entity.getPositionY() + entity.getHeight() / 2.0;
		int pixelSize = Math.max(1, (int) Math.round(2 * scale));

		backBufferGraphics.setColor(new Color(color.getRed(),
				color.getGreen(), color.getBlue(), alpha));
		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.fillRect(
							(int) (centerX + (i * 2 - entity.getWidth()
									/ 2.0) * scale),
							(int) (centerY + (j * 2 - entity.getHeight()
									/ 2.0) * scale),
							pixelSize, pixelSize);
	}

	/**
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 * Draws the game over banner shown on the game screen, typed out up to
	 * the given number of characters. The text stays centered as a whole so
	 * letters do not shift while typing.
	 *
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param text
	 *            Full banner text.
	 * @param visibleChars
	 *            Number of characters typed so far.
	 */
	public void drawGameOverBanner(final Screen screen, final String text,
	                               final int visibleChars) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(
				text.substring(0, Math.min(visibleChars, text.length())),
				screen.getWidth() / 2 - fontBigMetrics.stringWidth(text) / 2,
				screen.getHeight() / 2);
	}

	/**
	 * Covers the screen with a translucent black layer, used to fade out.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param alpha
	 *            Opacity of the layer, from 0 (clear) to 255 (black).
	 */
	public void drawFadeOverlay(final Screen screen, final int alpha) {
		backBufferGraphics.setColor(new Color(0, 0, 0,
				Math.max(0, Math.min(255, alpha))));
		backBufferGraphics.fillRect(0, 0, screen.getWidth(),
				screen.getHeight());
	}

	/**
	 * Countdown to game start.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param level
	 *            Game difficulty level.
	 * @param number
	 *            Countdown number.
	 * @param bonusLife
	 *            Checks if a bonus life is received.
	 */
	public void drawCountDown(final Screen screen, final int level,
	                          final int number, final boolean bonusLife) {
		int rectWidth = screen.getWidth();
		int rectHeight = screen.getHeight() / 6;
		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(0, screen.getHeight() / 2 - rectHeight / 2,
				rectWidth, rectHeight);
		backBufferGraphics.setColor(Color.GREEN);
		if (number >= 4)
			if (!bonusLife) {
				drawCenteredBigString(screen, "Level " + level,
						screen.getHeight() / 2
								+ fontBigMetrics.getHeight() / 3);
			} else {
				drawCenteredBigString(screen, "Level " + level
								+ " - Bonus life!",
						screen.getHeight() / 2
								+ fontBigMetrics.getHeight() / 3);
			}
		else if (number != 0)
			drawCenteredBigString(screen, Integer.toString(number),
					screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
		else
			drawCenteredBigString(screen, "GO!", screen.getHeight() / 2
					+ fontBigMetrics.getHeight() / 3);
	}

	/**
	 * Draws a centered row of text in the menu colours: green when
	 * selected, white otherwise.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            Text to draw.
	 * @param height
	 *            Height of the drawing.
	 * @param selected
	 *            Whether the row is currently selected.
	 */
	public void drawMenuRow(final Screen screen, final String string,
	                        final int height, final boolean selected) {
		drawMenuRow(screen, string, height, selected, true);
	}

	/**
	 * Draws a centered row of text in the menu colours: green when
	 * selected, dark grey when disabled, white otherwise.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            Text to draw.
	 * @param height
	 *            Height of the drawing.
	 * @param selected
	 *            Whether the row is currently selected.
	 * @param enabled
	 *            Whether the row can be chosen.
	 */
	public void drawMenuRow(final Screen screen, final String string,
	                        final int height, final boolean selected,
	                        final boolean enabled) {
		if (selected)
			backBufferGraphics.setColor(Color.GREEN);
		else if (!enabled)
			backBufferGraphics.setColor(Color.DARK_GRAY);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, string, height);
	}
	/**
	 * Draws the damage dim overlay when the player is hit.
	 *AUTHORED BY: VFX TEAM (Effection)
	 *Any further inquiries please contact us.
	 * @param screen
	 *            Screen to draw on.
	 * @param effect
	 *            Dim effect to draw.
	 */
	public void drawDamageDim(final Screen screen,
	                          final DamageDimEffect effect) {
		if (effect != null)
			effect.draw(backBufferGraphics, screen.getWidth(),
					screen.getHeight());
	}                                          

	/**                                        
	 * Draws the low-health glitch effect.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *Any further inquiries please contact us.
	 *
	 * @param screen Screen to draw on.
	 * @param effect Glitch effect to draw.
	 */
	public void drawGlitch(final Screen screen, final GlitchEffect effect) {
		if (effect != null)
			effect.draw(backBuffer, backBufferGraphics);
	}
}
