package com.nihal.marioclone.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;


import java.util.ArrayList;
import java.util.Random;

import sun.rmi.runtime.Log;

public class MarioClone extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture[] man;
	int manState = 0;
	int pause = 0;
	float gravity = 0.2f;
	float velocity = 0;
	int manY = 0;
	Rectangle manRectangle;
	BitmapFont font;
	Texture dizzy;
	int score = 0;
	int gameState = 0;		//game didn't start = 0, game is live = 1, game is over = 2

	Random random;

	ArrayList<Integer> coinXs = new ArrayList<Integer>();
	ArrayList<Integer> coinYs = new ArrayList<Integer>();
	ArrayList<Rectangle> coinRectangles =  new ArrayList<Rectangle>();
	Texture coin;
	int coinCount;

	ArrayList<Integer> bombXs = new ArrayList<Integer>();
	ArrayList<Integer> bombYs = new ArrayList<Integer>();
	ArrayList<Rectangle> bombRectangles =  new ArrayList<Rectangle>();
	Texture bomb;
	int bombCount;


	/**
	 * Adding assets.
	 */
	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");
		man[0] = new Texture("frame-1.png");
		man[1] = new Texture("frame-2.png");
		man[2] = new Texture("frame-3.png");
		man[3] = new Texture("frame-4.png");
		manY = Gdx.graphics.getHeight() / 2;
		coin = new Texture("coin.png");
		bomb = new Texture("bomb.png");
		random = new Random();
		dizzy = new Texture("dizzy-1.png");
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(10);
	}

	/**
	 * Makes the coins at random heights.
	 */
	public void makeCoin() {
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		coinYs.add((int)height);
		coinXs.add(Gdx.graphics.getWidth());
	}

	/**
	 * Makes the bombs at random heights.
	 */
	public void makeBomb() {
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		bombYs.add((int)height);
		bombXs.add(Gdx.graphics.getWidth());
	}

	/**
	 * Shows the graphics of the game with the man, coins and bombs.
	 */
	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		if (gameState == 1) {				// Game is live.
			setLiveGame();
		}
		else if (gameState == 0) {		    // Game didn't start.
			if (Gdx.input.justTouched()) {	// Wait for screen to be tapped to start the game
				gameState = 1;
			}
		}
		else if (gameState == 2) {		    // Game is over.
			setGameOver();
		}

		//If game is over, show a dizzy man, else the game is live so show the man moving.
		if (gameState == 2) {
			batch.draw(dizzy, Gdx.graphics.getWidth() / 2 - man[manState].getWidth() / 2, manY);
		} else {
			batch.draw(man[manState], Gdx.graphics.getWidth() / 2 - man[manState].getWidth() / 2, manY);
		}
		manRectangle = new Rectangle(Gdx.graphics.getWidth() / 2 - man[manState].getWidth() / 2, manY, man[manState].getWidth(), man[manState].getHeight());

		countCoinsHit();

		//If the man hits a bomb, the game is over.
		for (int i=0; i < bombRectangles.size();i++) {
			if (Intersector.overlaps(manRectangle, bombRectangles.get(i))) {
				Gdx.app.log("Bomb!", "Collision!");
				gameState = 2;
			}
		}

		font.draw(batch, String.valueOf(score),100,200);
		batch.end();
	}


	/**
	 * Adds the bombs, coins and moves the man as the game is live.
	 */
	private void setLiveGame(){
		showBombs();
		showCoins();

		// If the screen is tapped, make the man jump up (this velocity is added to gravity)
		if (Gdx.input.justTouched()) {
			velocity = -10;
		}
		changeManState();
		velocity += gravity;
		manY -= velocity;

		// So the man doesn't go below the screen and stays above the ground.
		if (manY <= 0) {
			manY = 0;
		}
	}

	/**
	 * When game is over, everything is reset and the coin counter gets to 0.
	 * The game returns to the pause state waiting for the tap of user to start it again.
	 */
	private void setGameOver(){
		if (Gdx.input.justTouched()) {
			gameState = 1;
			manY = Gdx.graphics.getHeight() / 2;
			score = 0;
			velocity = 0;
			coinXs.clear();
			coinYs.clear();
			coinRectangles.clear();
			coinCount = 0;
			bombXs.clear();
			bombYs.clear();
			bombRectangles.clear();
			bombCount = 0;
		}
	}

	/**
	 * Changes the man's state so he appears as moving.
	 */
	private void changeManState() {
		// The pause is for changing the states not so quickly.
		if (pause < 8) {
			pause++;
		} else {
			pause = 0;
			if (manState < 3) {
				manState++;
			} else {
				manState = 0;
			}
		}
	}

	/**
	 * Increases the counter of coins hit by the user.
	 */
	private void countCoinsHit() {
		for (int i=0; i < coinRectangles.size();i++) {
			if (Intersector.overlaps(manRectangle, coinRectangles.get(i))) {
				score++;
				coinRectangles.remove(i);
				coinXs.remove(i);
				coinYs.remove(i);
				break;
			}
		}
	}


	/**
	 * Shows the coins floating across the screen.
	 */
	private void showCoins() {

		// Wait a while then show a coin on screen.
		// Less than 100 to be shown more frequently than bombs.
		if (coinCount < 100) {
			coinCount++;
		} else {
			coinCount = 0;
			makeCoin();
		}

		// Add the coin to float on screen.
		coinRectangles.clear();
		for (int i=0;i < coinXs.size();i++) {
			batch.draw(coin, coinXs.get(i), coinYs.get(i));
			coinXs.set(i, coinXs.get(i) - 4);
			coinRectangles.add(new Rectangle(coinXs.get(i), coinYs.get(i), coin.getWidth(), coin.getHeight()));
		}
	}

	/**
	 * Shows the bombs floating across the screen.
	 */
	private void showBombs() {

		// Wait a while then show a bomb on screen.
		if (bombCount < 250) {
			bombCount++;
		} else {
			bombCount = 0;
			makeBomb();
		}

		// Add the bomb to float on screen.
		bombRectangles.clear();
		for (int i=0;i < bombXs.size();i++) {
			batch.draw(bomb, bombXs.get(i), bombYs.get(i));
			bombXs.set(i, bombXs.get(i) - 8);
			bombRectangles.add(new Rectangle(bombXs.get(i), bombYs.get(i), bomb.getWidth(), bomb.getHeight()));
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
