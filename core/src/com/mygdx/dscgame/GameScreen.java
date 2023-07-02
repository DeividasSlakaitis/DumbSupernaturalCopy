package com.mygdx.dscgame;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen{
	final Dscgame game;

	Texture dropImage;
	Texture bucketImage;
	Texture hell;
	Texture diamond;
	Texture cassiel;
	Texture comet;
	
	
	Sound dropSound;
	Music pixelMusic;
	
	
	OrthographicCamera camera;
	Rectangle bucket;
	Rectangle bucketP;
	
	Array<Rectangle> raindrops;
	Array<Rectangle> blocks;
	long lastDropTime;
	int dropsGathered;
	
	boolean afloat = false;
	
	float velocity = 1.04f;
	float gravity=-200f;
	

	public GameScreen(final Dscgame game) {
		this.game = game;

		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("drop.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		hell = new Texture(Gdx.files.internal("hell.png"));
		diamond = new Texture(Gdx.files.internal("walkstone.png"));
		cassiel = new Texture(Gdx.files.internal("cassiel.png")); 
		comet = new Texture(Gdx.files.internal("comet.png")); 

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		pixelMusic = Gdx.audio.newMusic(Gdx.files.internal("pixelMusic.mp3"));
		pixelMusic.setLooping(true);

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

		// create a Rectangle to logically represent the bucket
		bucket = new Rectangle();
		bucket.x = 1280 / 2 - 64 / 2; // center the bucket horizontally
		bucket.y = 100; // bottom left corner of the bucket is 20 pixels above
						// the bottom screen edge
		bucket.width = 75;
		bucket.height = 125;
		
		bucketP = new Rectangle();
		bucketP.x=bucket.x;
		bucketP.y=bucket.y;
		bucketP.width = 66;
		bucketP.height = 66;
		
		

		// create the raindrops array and spawn the first raindrop
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
		
		blocks = new Array<Rectangle>();
		createWorld();
		

	}
	
	private void createWorld() {

		Rectangle block = new Rectangle();
		for(int i=0;i<400;i++)
		{
			block = new Rectangle();
			block.x = i*64;
			block.y = 20-64;
			block.width = 64;
			block.height = 64;
			blocks.add(block);
		}
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 1280 - 64);
		raindrop.y = 720;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}
	
	public boolean collisionCheck(float x, float y) {
		Iterator<Rectangle> blocker = blocks.iterator();
		bucketP.x=x;
		bucketP.y=y;
		while (blocker.hasNext()) {
			Rectangle block = blocker.next();
			if (block.overlaps(bucketP)) {
				return true;
			}
		}
		return false;
	}
	
	public void gravityCalc() {
		if (collisionCheck(bucket.x,bucket.y-1+gravity*Gdx.graphics.getDeltaTime())==false) {
			gravity += -200f*velocity;
			if (collisionCheck(bucket.x,bucket.y-1+gravity*Gdx.graphics.getDeltaTime())==false) {
				bucket.y += gravity * Gdx.graphics.getDeltaTime();
			}
		}
		else {
			gravity = -200f;
		}
	}

	@Override
	public void render(float delta) {
		// clear the screen with a dark blue color. The
		// arguments to clear are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
//		ScreenUtils.clear(0, 0, 0.2f, 1);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bucket and
		// all drops
		

		
		game.batch.begin();

		//draw background
		game.batch.draw(hell, camera.position.x-(1280/2f), 0);
		
		game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 720);
		game.batch.draw(cassiel, bucket.x, bucket.y, bucket.width, bucket.height);
		for (Rectangle raindrop : raindrops) {
			game.batch.draw(comet, raindrop.x, raindrop.y);
		}
		for(Rectangle block : blocks) {
			game.batch.draw(diamond,block.x,block.y);
		}
		game.batch.end();


		
		// process user input
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}
		float moveSpeed=(500 * Gdx.graphics.getDeltaTime());
		if (Gdx.input.isKeyPressed(Keys.LEFT)&& collisionCheck(bucket.x-1-moveSpeed,bucket.y)==false)
			bucket.x -= moveSpeed;
		if (Gdx.input.isKeyPressed(Keys.RIGHT)&& collisionCheck(bucket.x+1+moveSpeed,bucket.y)==false)
			bucket.x += moveSpeed;
		if (Gdx.input.isKeyPressed(Keys.DOWN)&& collisionCheck(bucket.x,bucket.y-1-moveSpeed)==false)
			bucket.y -= moveSpeed;
		//Jump
		if (Gdx.input.isKeyPressed(Keys.SPACE)&& collisionCheck(bucket.x,bucket.y-1-moveSpeed)==true) {
			gravity = 2500f;
		}
		
		//gravity
		gravityCalc();
	    
		// make sure the bucket stays within the screen bounds
		if (bucket.x < 0)
			bucket.x = 0;
//		if (bucket.x > 1280 - 64)
//			bucket.x = 1280 - 64;

		// check if we need to create a new raindrop
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
			spawnRaindrop();

		// move the raindrops, remove any that are beneath the bottom edge of
		// the screen or that hit the bucket. In the later case we increase the
		// value our drops counter and add a sound effect.
		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0)
				iter.remove();
			if (raindrop.overlaps(bucket)) {
				dropsGathered++;
				dropSound.play();
				iter.remove();
			}
		}
		
		//camera follow the player/bucket
		camera.position.set(bucket.x,720/2f,0);
		
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// start the playback of the background music
		// when the screen is shown
		pixelMusic.play();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		hell.dispose();
		dropImage.dispose();
		cassiel.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		pixelMusic.dispose();
	}

}
