package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import engine.Achievement;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;

/**
 * Implements the game screen, where the action happens.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameScreen extends Screen {

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000;
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/** Pause after the player ship explodes, before enemies disappear. */
	private static final int GAME_OVER_PAUSE = 500;
	/** Time between the first enemy rows starting to shrink on game over. */
	private static final int GAME_OVER_ROW_INTERVAL_START = 400;
	/** Fastest time between enemy rows starting to shrink on game over. */
	private static final int GAME_OVER_ROW_INTERVAL_MIN = 250;
	/** Reduction of the row interval after each enemy row starts. */
	private static final int GAME_OVER_ROW_INTERVAL_STEP = 50;
	/** Time an enemy takes to shrink and fade out on game over. */
	private static final int GAME_OVER_SHRINK_DURATION = 450;
	/** Time after the last enemy disappears before the banner appears. */
	private static final int GAME_OVER_LAST_ROW_HOLD = 400;
	/** Text of the game over banner. */
	private static final String GAME_OVER_TEXT = "GAME OVER";
	/** Time between each letter of the game over banner appearing. */
	private static final int GAME_OVER_TYPE_INTERVAL = 100;
	/** Time the game over banner stays on or off while blinking. */
	private static final int GAME_OVER_BLINK_INTERVAL = 200;
	/** Number of times the game over banner blinks. */
	private static final int GAME_OVER_BLINK_COUNT = 3;
	/** Time the game over banner stays steady before fading out. */
	private static final int GAME_OVER_TEXT_HOLD = 600;
	/** Time the fade to black takes before the game over menu. */
	private static final int GAME_OVER_FADE_DURATION = 800;
	/** How long an achievement unlock popup remains visible. */
	private static final int ACHIEVEMENT_POPUP_INTERVAL = 3000;
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 40;

	/** Current game difficulty settings. */
	private GameSettings gameSettings;
	/** Current difficulty level number. */
	private int level;
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation;
	/** Player's ship. */
	private Ship ship;
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial;
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown;
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown;
	/** Time until the next enemy row explodes on game over. */
	private Cooldown gameOverRowCooldown;
	/** Current time between enemy rows starting to shrink on game over. */
	private int gameOverRowInterval;
	/** Enemies shrinking on game over, with the moment each one started. */
	private Map<EnemyShip, Long> shrinkingEnemies;
	/** Moment the last enemy disappeared, 0 while enemies remain. */
	private long gameOverLastEnemyGone;
	/** Moment the game over banner starts appearing. */
	private long gameOverBannerStart;
	/** Checks if the game over sequence is playing. */
	private boolean gameOverActive;
	/** Checks if the game over banner is shown. */
	private boolean showGameOverText;
	/** Time until the achievement unlock popup closes. */
	private Cooldown achievementPopupCooldown;
	/** Achievement currently shown in the unlock popup. */
	private Achievement unlockedAchievement;
	/** Set of all bullets fired by on screen ships. */
	private Set<Bullet> bullets;
	/** Current score. */
	private int score;
	/** Player lives left. */
	private int lives;
	/** Total bullets shot by the player. */
	private int bulletsShot;
	/** Total ships destroyed by the player. */
	private int shipsDestroyed;
	/** Moment the game starts. */
	private long gameStartTime;
	/** Checks if the level is finished. */
	private boolean levelFinished;
	/** Checks if a bonus life is received. */
	private boolean bonusLife;

	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param gameState
	 *            Current game state.
	 * @param gameSettings
	 *            Current game settings.
	 * @param bonnusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState gameState,
			final GameSettings gameSettings, final boolean bonusLife,
			final int width, final int height, final int fps) {
		super(width, height, fps);

		this.gameSettings = gameSettings;
		this.bonusLife = bonusLife;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		this.lives = gameState.getLivesRemaining();
		if (this.bonusLife)
			this.lives++;
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();

		enemyShipFormation = new EnemyShipFormation(this.gameSettings);
		enemyShipFormation.attach(this);
		this.ship = new Ship(this.width / 2, this.height - 30);
		// Appears each 10-30 seconds.
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.achievementPopupCooldown = Core.getCooldown(
				ACHIEVEMENT_POPUP_INTERVAL);
		this.bullets = new HashSet<Bullet>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		this.score += LIFE_SCORE * (this.lives - 1);
		this.logger.info("Screen cleared with a score of " + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		if (this.inputDelay.checkFinished() && !this.levelFinished) {

			if (!this.ship.isDestroyed()) {
				boolean moveRight = inputManager.isKeyDown(KeyEvent.VK_RIGHT)
						|| inputManager.isKeyDown(KeyEvent.VK_D);
				boolean moveLeft = inputManager.isKeyDown(KeyEvent.VK_LEFT)
						|| inputManager.isKeyDown(KeyEvent.VK_A);

				boolean isRightBorder = this.ship.getPositionX()
						+ this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
				boolean isLeftBorder = this.ship.getPositionX()
						- this.ship.getSpeed() < 1;

				if (moveRight && !isRightBorder) {
					this.ship.moveRight();
				}
				if (moveLeft && !isLeftBorder) {
					this.ship.moveLeft();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
					if (this.ship.shoot(this.bullets))
						this.bulletsShot++;
			}

			if (this.enemyShipSpecial != null) {
				if (!this.enemyShipSpecial.isDestroyed())
					this.enemyShipSpecial.move(2, 0);
				else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
					this.enemyShipSpecial = null;

			}
			if (this.enemyShipSpecial == null
					&& this.enemyShipSpecialCooldown.checkFinished()) {
				this.enemyShipSpecial = new EnemyShip();
				this.enemyShipSpecialCooldown.reset();
				this.logger.info("A special ship appears");
			}
			if (this.enemyShipSpecial != null
					&& this.enemyShipSpecial.getPositionX() > this.width) {
				this.enemyShipSpecial = null;
				this.logger.info("The special ship has escaped");
			}

			this.ship.update();
			this.enemyShipFormation.update();
			this.enemyShipFormation.shoot(this.bullets);
		}

		manageCollisions();
		cleanBullets();
		draw();

		// Game over sequence, only when the player runs out of lives.
		if (this.lives == 0 && !this.levelFinished)
			startGameOverSequence();
		if (this.gameOverActive) {
			updateGameOverSequence();
			return;
		}

		if ((this.enemyShipFormation.isEmpty() || this.lives == 0)
				&& !this.levelFinished) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();
		}

		if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
			this.isRunning = false;

	}

	/**
	 * Starts the game over sequence: the player ship explodes, then after a
	 * pause the remaining enemies shrink away row by row.
	 */
	private void startGameOverSequence() {
		this.levelFinished = true;
		this.gameOverActive = true;

		// Shows the player ship explosion; it stays since the ship is no
		// longer updated.
		this.ship.update();

		BulletPool.recycle(this.bullets);
		this.bullets.clear();

		// Clears explosions left from enemies shot just before.
		this.enemyShipFormation.removeDestroyed();
		this.shrinkingEnemies = new LinkedHashMap<EnemyShip, Long>();

		this.gameOverRowInterval = GAME_OVER_ROW_INTERVAL_START;
		this.gameOverRowCooldown = Core.getCooldown(GAME_OVER_PAUSE);
		this.gameOverRowCooldown.reset();
		this.logger.info("Game over, starting disappear sequence.");
	}

	/**
	 * Makes the next enemy row start shrinking, speeding up after each row,
	 * shows the game over banner once no enemies are left, then ends the
	 * screen.
	 */
	private void updateGameOverSequence() {
		long now = System.currentTimeMillis();
		if (this.showGameOverText) {
			if (now - this.gameOverBannerStart
					>= getGameOverFadeStart() + GAME_OVER_FADE_DURATION)
				this.isRunning = false;
			return;
		}

		// Clears a bonus ship explosion (shot just before game over) after
		// its usual time, as gameplay does, since that update no longer runs.
		if (this.enemyShipSpecial != null
				&& this.enemyShipSpecial.isDestroyed()
				&& this.enemyShipSpecialExplosionCooldown.checkFinished())
			this.enemyShipSpecial = null;

		// Removes enemies that finished shrinking.
		Iterator<Long> starts = this.shrinkingEnemies.values().iterator();
		while (starts.hasNext())
			if (now - starts.next() >= GAME_OVER_SHRINK_DURATION)
				starts.remove();

		if (!this.gameOverRowCooldown.checkFinished())
			return;

		List<EnemyShip> row = this.enemyShipFormation.removeBottomRow();
		if (!row.isEmpty()) {
			for (EnemyShip enemyShip : row)
				this.shrinkingEnemies.put(enemyShip, now);
			this.gameOverRowCooldown = Core.getCooldown(
					this.gameOverRowInterval);
			this.gameOverRowCooldown.reset();
			this.gameOverRowInterval = Math.max(GAME_OVER_ROW_INTERVAL_MIN,
					this.gameOverRowInterval - GAME_OVER_ROW_INTERVAL_STEP);
		} else if (this.enemyShipSpecial != null
				&& !this.enemyShipSpecial.isDestroyed()) {
			// The bonus ship sits above the formation, so it goes last.
			this.shrinkingEnemies.put(this.enemyShipSpecial, now);
			this.enemyShipSpecial = null;
		} else if (this.shrinkingEnemies.isEmpty()) {
			// Short beat on the empty screen before the banner.
			if (this.gameOverLastEnemyGone == 0)
				this.gameOverLastEnemyGone = now;
			if (now - this.gameOverLastEnemyGone >= GAME_OVER_LAST_ROW_HOLD) {
				this.enemyShipSpecial = null;
				this.showGameOverText = true;
				this.gameOverBannerStart = now;
			}
		}
	}

	/**
	 * Draws the enemies shrinking and fading away on game over.
	 */
	private void drawShrinkingEnemies() {
		long now = System.currentTimeMillis();
		for (Map.Entry<EnemyShip, Long> entry
				: this.shrinkingEnemies.entrySet()) {
			double progress = (double) (now - entry.getValue())
					/ GAME_OVER_SHRINK_DURATION;
			drawManager.drawEntityShrunk(entry.getKey(), 1 - progress);
		}
	}

	/**
	 * Gets the time, from the banner start, when the fade to black begins.
	 *
	 * @return Milliseconds after the banner starts.
	 */
	private int getGameOverFadeStart() {
		return GAME_OVER_TEXT.length() * GAME_OVER_TYPE_INTERVAL
				+ GAME_OVER_BLINK_COUNT * GAME_OVER_BLINK_INTERVAL * 2
				+ GAME_OVER_TEXT_HOLD;
	}

	/**
	 * Draws the game over banner: typed out letter by letter, then blinking,
	 * then the screen fades to black.
	 */
	private void drawGameOverSequence() {
		long elapsed = System.currentTimeMillis() - this.gameOverBannerStart;
		int typeTime = GAME_OVER_TEXT.length() * GAME_OVER_TYPE_INTERVAL;
		int blinkTime = GAME_OVER_BLINK_COUNT * GAME_OVER_BLINK_INTERVAL * 2;

		if (elapsed < typeTime) {
			drawManager.drawGameOverBanner(this, GAME_OVER_TEXT,
					(int) (elapsed / GAME_OVER_TYPE_INTERVAL) + 1);
		} else {
			long blinkElapsed = elapsed - typeTime;
			boolean visible = blinkElapsed >= blinkTime
					|| (blinkElapsed / GAME_OVER_BLINK_INTERVAL) % 2 == 1;
			if (visible)
				drawManager.drawGameOverBanner(this, GAME_OVER_TEXT,
						GAME_OVER_TEXT.length());
		}

		long fadeElapsed = elapsed - getGameOverFadeStart();
		if (fadeElapsed > 0)
			drawManager.drawFadeOverlay(this, (int) (fadeElapsed * 255
					/ GAME_OVER_FADE_DURATION));
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawEntity(this.ship, this.ship.getPositionX(),
				this.ship.getPositionY());
		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY());

		enemyShipFormation.draw();

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet, bullet.getPositionX(),
					bullet.getPositionY());

		// Interface.
		drawManager.drawScore(this, this.score);
		drawManager.drawLives(this, this.lives);
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
		if (this.unlockedAchievement != null) {
			drawManager.drawAchievementUnlocked(this, this.unlockedAchievement);
			if (this.achievementPopupCooldown.checkFinished())
				this.unlockedAchievement = null;
		}

		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
							- this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height
					/ 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height
					/ 12);
		}

		if (this.shrinkingEnemies != null)
			drawShrinkingEnemies();
		if (this.showGameOverText)
			drawGameOverSequence();

		drawManager.completeDrawing(this);
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height)
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets)
			if (bullet.getSpeed() > 0) {
				if (checkCollision(bullet, this.ship) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.ship.isDestroyed()) {
						this.ship.destroy();
						this.lives--;
						this.logger.info("Hit on player ship, " + this.lives
								+ " lives remaining.");
					}
				}
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						this.score += enemyShip.getPointValue();
						this.shipsDestroyed++;
						this.enemyShipFormation.destroy(enemyShip);
						showUnlockedAchievement(Core.getAchievementManager()
								.recordEnemyDefeated());
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.score += this.enemyShipSpecial.getPointValue();
					this.shipsDestroyed++;
					this.enemyShipSpecial.destroy();
					showUnlockedAchievement(Core.getAchievementManager()
							.recordEnemyDefeated());
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Displays a popup when an enemy defeat unlocks an achievement.
	 *
	 * @param achievement Newly unlocked achievement, if any.
	 */
	private void showUnlockedAchievement(final Achievement achievement) {
		if (achievement != null) {
			this.unlockedAchievement = achievement;
			this.achievementPopupCooldown.reset();
		}
	}

	/**
	 * Checks if two entities are colliding.
	 * 
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}

	/**
	 * Returns a GameState object representing the status of the game.
	 * 
	 * @return Current game state.
	 */
	public final GameState getGameState() {
		return new GameState(this.level, this.score, this.lives,
				this.bulletsShot, this.shipsDestroyed);
	}
}