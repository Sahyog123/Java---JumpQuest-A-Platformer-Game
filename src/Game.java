
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.sound.sampled.*;
import java.io.*;

import java.awt.*;


import game2D.*;


@SuppressWarnings("serial")

public class Game extends GameCore 
{
	// Useful game constants
	static int screenWidth = 512;
	static int screenHeight = 384;

	float 	lift = 0.005f;
	float	gravity = 0.0001f;

	// Game state flags
	boolean flap = false;

	// Game resources
	Animation idle;
	Animation enemy_right;
	
	//Initialise Sprites
	Sprite player = null;
	Sprite enemy1 = null;
	Sprite enemy2 = null;
	Sprite enemy3 = null;
	Sprite enemy4 = null;
	Sprite enemy5 = null;
	Sprite enemy6 = null;
	Sprite finish = null;
	ArrayList<Sprite> clouds = new ArrayList<Sprite>();

	
	TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()

	long total;         			// The score will be the total time elapsed since a crash






	// Game state flags
	private boolean jump = false;
	private boolean left = false;
	private boolean right = false;
	private boolean decelerate = false;

	
	public float postX;
	public float postY;

	int x1;
	int y1;
	
	// Positions of the tiles around the main character sprite
	int yT; // tile above the character
	int xT;
	int xB; // tile below the character
	int yB;
	int xR; // tile at the right
	int yR;
	int xL; // tile at the left
	int yL;

	private Image bg; // Background image
	//private Image fg; 
	//private Image back;

	//variables to keep track of game content
	private int levelNumber = 1;
	private int life;
	public long score;

	//Score and lives counter
	String msg = String.format("Score: %d", total/100);
	String lives = String.format("Life: %d", life);

	//User interface components
	public static STATE State = STATE.MENU;
	private Menu menu;
	private Dead dead;
	private Options options;
	private Complete complete;
	
	
	//Animations for the main character
	private Animation standing_right;
	private Animation running_right;
	private Animation running_left;
	private Animation dying_right;
	private Animation dying_left;
	private Animation jumping_right;
	private Animation jumping_left;
	private Animation falling_right;
	private Animation falling_left;
	
	//Animations for the enemies
	private Animation enemy_running_left;
	private Animation enemy_running_right;

	//Animation for the finish line flag
	private Animation Finishflag;




	public static void main(String[] args) {

		Game gct = new Game();
		gct.init("map.txt");
		// Start in windowed mode with the given screen height and width
		gct.run(false,screenWidth,screenHeight);
	}

	/**
	 * Initialise the class, e.g. set up variables, load images,
	 * create animations, register event handlers
	 */
	public void init(String map)
	{         
		Sprite s;	// Temporary reference to a sprite

		//Load the background image
		bg = loadImage("images/sky.png").getScaledInstance(728, 700, Image.SCALE_DEFAULT);

		// Load the tile map and print it out so we can check it is valid
		tmap.loadMap("maps", map);

		//Set the window size as desired
		setSize(600,500);
		setVisible(true);

		// Create a set of background sprites that we can 
		// rearrange to give the illusion of motion
		
		//Load all the animations the character will use
		idle = new Animation();
		idle.loadAnimationFromSheet("images/Idle (32x32).png", 11, 1, 100);

		enemy_running_right = new Animation();
		enemy_running_right.loadAnimationFromSheet("images/MushroomRun (32x32).png", 16, 1, 60);
		
		enemy_running_left = new Animation();
		enemy_running_left.loadAnimationFromSheet("images/MushroomRun (32x32)left.png", 16, 1, 60);

		running_right = new Animation();
		running_right.loadAnimationFromSheet("images/run (32x32).png", 12, 1, 120);
		
		running_left = new Animation();
		running_left.loadAnimationFromSheet("images/run (32x32)left.png", 12, 1, 120);

		jumping_right = new Animation();
		jumping_right.loadAnimationFromSheet("images/jump (32x32).png", 1, 1, 6000);
		
		jumping_left = new Animation();
		jumping_left.loadAnimationFromSheet("images/jump (32x32)left.png", 1, 1, 6000);

		falling_right = new Animation();
		falling_right.loadAnimationFromSheet("images/Fall (32x32).png", 1, 1, 6000);
		
		falling_left = new Animation();
		falling_left.loadAnimationFromSheet("images/Fall (32x32)left.png", 1, 1, 6000);

		dying_right = new Animation();
		dying_right.loadAnimationFromSheet("images/Hit (32x32).png", 7, 1, 6000);
		
		dying_left = new Animation();
		dying_left.loadAnimationFromSheet("images/Hit (32x32)left.png", 7, 1, 6000);

		//Finish line flag animation
		Animation finishflag = new Animation();
		finishflag.loadAnimationFromSheet("images/Checkpoint (Flag Idle)(64x64).png", 10, 1, 60);





		player = new Sprite(idle);

		finish = new Sprite(finishflag);

		enemy1 = new Sprite(enemy_right);
		enemy2 = new Sprite(enemy_right);
		enemy3 = new Sprite(enemy_right);
		enemy4 = new Sprite(enemy_right);
		enemy5 = new Sprite(enemy_right);
		enemy6 = new Sprite(enemy_right);

		// Load a single cloud animation
		Animation ca = new Animation();
		ca.addFrame(loadImage("images/cloud 1.png"), 1000);

		// Create 3 clouds at random positions off the screen
		// to the right
		for (int c=0; c<3; c++)
		{
			s = new Sprite(ca);
			s.setX(screenWidth + (int)(Math.random()*200.0f));
			s.setY(30 + (int)(Math.random()*150.0f));
			s.setVelocityX(-0.02f);
			s.show();
			clouds.add(s);
		}

		// Initialise the various menu screens
		menu = new Menu();
		dead = new Dead();
		options = new Options();
		complete = new Complete();

		initialiseGame();

		System.out.println(tmap);
	}


	public void initialiseGame()
	{
		total = 0;
		life = 4;
		
		//If level is one then initialise all sprite locations based on the first map
		if(levelNumber == 1)
		{
			tmap.loadMap("maps", "map.txt");
			player.setX(64);
			player.setY(300);
			player.setVelocityX(0);
			player.setVelocityY(0);
			player.show();

			enemy1.setSpawnX(tmap.getTileXC(15, 15));
			enemy1.setSpawnY(tmap.getTileYC(10, 10));
			enemy1.setMaxPatrol(enemy1.getSpawnX() + 440);
			enemy1.setMinPatrol(enemy1.getSpawnX() - 200);

			enemy2.setSpawnX(tmap.getTileXC(31, 15));
			enemy2.setSpawnY(tmap.getTileYC(10, 10));
			enemy2.setMaxPatrol(enemy2.getSpawnX() + 250);
			enemy2.setMinPatrol(enemy2.getSpawnX());

			enemy3.setSpawnX(tmap.getTileXC(51, 15));
			enemy3.setSpawnY(tmap.getTileYC(10, 10));
			enemy3.setMaxPatrol(enemy3.getSpawnX() + 250);
			enemy3.setMinPatrol(enemy3.getSpawnX());

			enemy4.setSpawnX(tmap.getTileXC(74, 15));
			enemy4.setSpawnY(tmap.getTileYC(10, 10));
			enemy4.setMaxPatrol(enemy4.getSpawnX() + 250);
			enemy4.setMinPatrol(enemy4.getSpawnX());

			enemy5.setSpawnX(tmap.getTileXC(90, 15));
			enemy5.setSpawnY(tmap.getTileYC(10, 10));
			enemy5.setMaxPatrol(enemy5.getSpawnX() + 650);
			enemy5.setMinPatrol(enemy5.getSpawnX());

			enemy6.setSpawnX(tmap.getTileXC(151, 15));
			enemy6.setSpawnY(tmap.getTileYC(10, 10));
			enemy6.setMaxPatrol(enemy6.getSpawnX() + 460);
			enemy6.setMinPatrol(enemy6.getSpawnX());

			finish.setX(5500);
			finish.setY(290);
		}
		
		//If the level is two then initialise sprites based on second map
		if (levelNumber == 2)
		{
			tmap.loadMap("maps", "map2.txt");

			player.setX(64);
			player.setY(300);
			player.setVelocityX(0);
			player.setVelocityY(0);
			player.show();

			enemy1.setSpawnX(tmap.getTileXC(21, 15));
			enemy1.setSpawnY(tmap.getTileYC(10, 10));
			enemy1.setMaxPatrol(enemy1.getSpawnX() + 305);
			enemy1.setMinPatrol(enemy1.getSpawnX());

			enemy2.setSpawnX(tmap.getTileXC(42, 15));
			enemy2.setSpawnY(tmap.getTileYC(10, 10));
			enemy2.setMaxPatrol(enemy2.getSpawnX() + 300);
			enemy2.setMinPatrol(enemy2.getSpawnX());

			enemy3.setSpawnX(tmap.getTileXC(52, 15));
			enemy3.setSpawnY(tmap.getTileYC(10, 10));
			enemy3.setMaxPatrol(enemy3.getSpawnX() + 298);
			enemy3.setMinPatrol(enemy3.getSpawnX());

			enemy4.setSpawnX(tmap.getTileXC(79, 15));
			enemy4.setSpawnY(tmap.getTileYC(10, 10));
			enemy4.setMaxPatrol(enemy4.getSpawnX() + 340);
			enemy4.setMinPatrol(enemy4.getSpawnX());

			enemy5.setSpawnX(tmap.getTileXC(105, 15));
			enemy5.setSpawnY(tmap.getTileYC(10, 10));
			enemy5.setMaxPatrol(enemy5.getSpawnX() + 210);
			enemy5.setMinPatrol(enemy5.getSpawnX());

			enemy6.setSpawnX(tmap.getTileXC(140, 15));
			enemy6.setSpawnY(tmap.getTileYC(10, 10));
			enemy6.setMaxPatrol(enemy6.getSpawnX() + 365);
			enemy6.setMinPatrol(enemy6.getSpawnX());

			finish.setX(5500);
			finish.setY(290);
		}

	}

	/**
	 * Draw the current state of the game
	 */
	public void draw(Graphics2D g)
	{    	
		// Be careful about the order in which you draw objects - you
		// should draw the background first, then work your way 'forward'

		// First work out how much we need to shift the view 
		// in order to see where the player is.
		int xo = (int) -player.getX() + 100; // offsets for the 'camera' view of the game based on the player's position
		int yo = (int) -player.getY() + 250;

		int x1 = 0; // offsets for the 'camera' view of the game based on the player's position
		int y1 = 0;

		// If relative, adjust the offset so that
		// it is relative to the player

		// ...?



		// Get the background image
		for (int y = 0; y < tmap.getMapHeight(); y += bg.getHeight(null))
		{
			for (int x = 0; x < tmap.getMapWidth(); x += bg.getWidth(null))
			{
				g.drawImage(bg, x1 /10, y1 /10, null); // draw image
			}
		}




		if (State == STATE.GAME) // If in the 'Game' state
		{




			// Apply offsets to sprites then draw them
			for (Sprite s: clouds)
			{
				s.setOffsets(xo,yo);
				s.draw(g);
			}

			

			// add all sprites in an array list
			ArrayList<Sprite> sprites = new ArrayList<>();
			sprites.add(player);
			sprites.add(enemy1);
			sprites.add(enemy2);
			sprites.add(enemy3);
			sprites.add(enemy4);
			sprites.add(enemy5);
			sprites.add(enemy6);
			
			// Apply offsets to player, enemies and draw 
			for (Sprite s : sprites)
			{
				s.setOffsets(xo, yo); 
				checkOnScreen(g, s, xo, yo);
			}

			//Set offsets for the finish line and draw
			finish.setOffsets(xo, yo);
			finish.draw(g);


			// Apply offsets to tile map and draw
			tmap.draw(g,xo,yo);    

			// Show score and status information
			msg = String.format("Score: %d", total);
			g.setColor(Color.darkGray);
			g.drawString(msg, getWidth() - 80, 50);

			lives = String.format("Life: %d", life);
			g.setColor(Color.darkGray);
			g.drawString(lives, getWidth() - 80, 65);


		}

		else if (State == STATE.MENU) // If in 'Menu' state
		{
			this.addMouseListener(new Menu()); // Add mouse listener
			menu.render(g);// call render for this menu
		}

		else if (State == STATE.DEAD) // If in 'Dead' state
		{
			this.addMouseListener(new Dead());
			dead.render(g);
		}
		else if (State == STATE.OPTIONS) // If in 'Help' state
		{
			this.addMouseListener(new Options());
			options.render(g);

		}
		else if (State == STATE.COMPLETE) // If in 'Complete' state
		{
			this.addMouseListener(new Complete());
			complete.render(g);
		}
	}



	// Method to check if a sprite is on screen

	/**
	 *
	 * @param g the graphics object to draw to
	 * @param s the current sprite
	 * @param xo the x offset value
	 * @param yo the y offset value
	 */
	public void checkOnScreen(Graphics2D g, Sprite s, int xo, int yo)
	{
		Rectangle rect = (Rectangle) g.getClip(); // Create a rectangle around the edges of the screen
		int xc, yc; // variables to register the position of the

		// get the x and y position of the sprite
		xc = (int) (xo + s.getX());
		yc = (int) (yo + s.getY());

		if (rect.contains(xc, yc)) // if the sprite's coordinates are within the rectangle border
		{
			s.show(); // show the sprite
			s.draw(g); // draw them to the screen
		}
		else
		{
			s.hide(); // hide the sprite
		}
	}






	/**
	 * Update any sprites and check for collisions
	 * 
	 * @param elapsed The elapsed time between this call and the previous call of elapsed
	 */    
	public void update(long elapsed)
	{


		//If the state is 'GAME'
		if (State == STATE.GAME) 
		{
			
			score = total;


			checkTileCollision(player,tmap); // check for further tile collisions
			player.setAnimation(idle);


			if (left) // if moving left
			{
				// variables to calculate future position
				postX = player.getX() + player.getImage().getWidth(null);
				postY = player.getY() + player.getImage().getHeight(null) / 2;

				if (tmap.getTileChar((int) (postX - 0.02f), (int) postY) == 'g' || tmap.getTileChar((int) (postX - 0.02f), (int) postY) == 'k' || tmap.getTileChar((int) (postX - 0.02f), (int) postY) == 'q' || tmap.getTileChar((int) (postX - 0.02f), (int) postY) == 'p' || tmap.getTileChar((int) (postX - 0.02f), (int) postY) == 'u' || tmap.getTileChar((int) (postX - 0.02f), (int) postY) == 'o')
				{
					player.setVelocityX(0);
				}
				else
				{
					player.setVelocityX(-0.2f); // move player to the left

					player.setAnimation(running_left);

				}


			}
			if (right) // if moving right
			{
				// variables to calculate future position
				postX = player.getX() + player.getImage().getWidth(null);
				postY = player.getY() + player.getImage().getHeight(null) / 2;

				if (tmap.getTileChar((int) (postX + 0.02f), (int) postY) == 'g' || tmap.getTileChar((int) (postX + 0.02f), (int) postY) == 's' || tmap.getTileChar((int) (postX + 0.02f), (int) postY) == 'q' || tmap.getTileChar((int) (postX + 0.02f), (int) postY) == 'p' || tmap.getTileChar((int) (postX + 0.02f), (int) postY) == 'u' || tmap.getTileChar((int) (postX + 0.02f), (int) postY) == 'w')
				{
					player.setVelocityX(0);
				}
				else
				{
					player.setVelocityX(0.2f); // move player to the right

					player.setAnimation(running_right);

				}
			}





			if(jump)
			{
				if(gravity == 0)
				{
					//Play the jump audio
					Sound jumpsound = new Sound("sounds/jump 1sec.wav");
					jumpsound.start();
					//If the character is jumping then make gravity 0.001 for the character
					gravity = 0.001f;
					player.setVelocityY(-0.55f);
				}


			}
			
			//The character should stop slowly rather than instantly for a more natural feel
			if (decelerate) 
			{
				 // set velocity to 90% of the current value
				player.setVelocityX(player.getVelocityX() * 0.9f);
				
				/* using this method the player could be moving at
				0.00000001 pixels per frame and the deceleration would never stop, 
				so need this condition to stop eventually*/
				if (player.getVelocityX() <= 0.01f && player.getVelocityX() >= -0.01f) 
				{
					// set velocity 0
					player.setVelocityX(0);
					// set decelerate value to false to break the loop
					decelerate = false; 
				}
			}

			//If the character is jumping
			else if (jump) 
			{
				player.setAnimation(jumping_right);
				
				//if the character is jumping left
				if(left)
				{
					//Set the jumping left animation
					player.setAnimation(jumping_left);
				}
				//If the character is jumping right
				else if(right) 
				{
					//Set the character jumping right animation
					player.setAnimation(jumping_right);
				}
			}
			
			//If the character is falling
			else if (player.getVelocityY()>0)
			{
				//Set the character to falling right animation
				player.setAnimation(falling_right);
				
				//If the character is falling to the left
				if(left)
				{
					//Set the character to falling left animation
					player.setAnimation(falling_left);
				}
			}

			//add all sprites to an array list
			ArrayList<Sprite> enemies = new ArrayList<>();
			enemies.add(enemy1);
			enemies.add(enemy2);
			enemies.add(enemy3);
			enemies.add(enemy4);
			enemies.add(enemy5);
			enemies.add(enemy6);
			
			for (Sprite enemy : enemies)
			{
				//if the enemy hits the edge of their patrol area
				if ((enemy.getX() > enemy.getMaxPatrol() && enemy.getDirection()) || (enemy.getX() < enemy.getMinPatrol() && !enemy.getDirection()))
				{
					// Change direction boolean to make the character turn
					enemy.setDirection(!enemy.getDirection());
				}
				//If the enemy is moving right
				if (enemy.getDirection())
				{
					// move enemy to right
					enemy.setVelocityX(0.03f);
					// Set animation to the enemy moving right
					enemy.setAnimation(enemy_running_right);
				}
				// if the enemy is moving left
				else
				{
					// move enemy to left
					enemy.setVelocityX(-0.03f); 
					// Set animation to the enemy moving left
					enemy.setAnimation(enemy_running_left); 
				}
				enemy.update(elapsed);
			}


			// Make adjustments to the speed of the sprite due to gravity
			player.setVelocityY(player.getVelocityY()+(gravity*elapsed));

			player.setAnimationSpeed(1.0f);

			for (Sprite s: clouds)
				s.update(elapsed);

			// Now update the sprites animation and position
			player.update(elapsed);


			// Then check for any collisions that may have occurred
			handleScreenEdge(player, tmap, elapsed);
			checkSpriteCollision();
			checkTileCollision(player, tmap);

			if (life == 0)
			{
				System.out.println("You died!");
				// reset the level to 1
				levelNumber = 1;
				// Change game state to 'Dead'
				Game.State = Game.STATE.DEAD; 
			}

		}

	}


	/**
	 * Checks and handles collisions with the edge of the screen
	 * 
	 * @param s			The Sprite to check collisions for
	 * @param tmap		The tile map to check 
	 * @param elapsed	How much time has gone by since the last call
	 */
	public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed)
	{
		// This method just checks if the sprite has gone off the bottom screen.
		// Ideally you should use tile collision instead of this approach

		if (s.getY() + s.getHeight() > tmap.getPixelHeight())
		{
			// Put the player back on the map 1 pixel above the bottom
			s.setY(tmap.getPixelHeight() - s.getHeight() - 1); 

			// and make them bounce
			s.setVelocityY(-s.getVelocityY());
		}


	}



	/**
	 * Override of the keyPressed event defined in GameCore to catch our
	 * own events
	 * 
	 *  @param e The event that has been generated
	 */
	public void keyPressed(KeyEvent e) 
	{ 
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_ESCAPE) stop();

		if (key == KeyEvent.VK_SPACE) 
		{

			jump = true; // jump

		}

		if (key == KeyEvent.VK_LEFT)
		{
			left = true; // move left
		}

		if (key == KeyEvent.VK_RIGHT)
		{
			right = true; // move right
		}

		if (key == KeyEvent.VK_S)
		{
			// Example of playing a sound as a thread
			Sound s = new Sound("sounds/caw.wav");
			s.start();
		}
	}

	/*
	 * Method to detect collision between sprites by drawing a box around the sprites
	 */
	public boolean boundingBoxCollision(Sprite s1, Sprite s2)
	{
		return ((s1.getX() + s1.getImage().getWidth(null) >= s2.getX())
				&& (s1.getX() <= (s2.getX() + s2.getImage().getWidth(null)))
				&& ((s1.getY() + s1.getImage().getHeight(null) >= s2.getY())
						&& (s1.getY() <= s2.getY() + s2.getImage().getHeight(null))));
	}

	
	public boolean boundingCircleCollision(Sprite s1, Sprite s2)
	{
		return false;
	}
	
	
	/**
	 * Check and handles collisions with a tile map for the
	 * given sprite 's'. Initial functionality is limited...
	 * 
	 * @param s			The Sprite to check collisions for
	 * @param tmap		The tile map to check 
	 */

	public void checkTileCollision(Sprite s, TileMap tmap)
	{

		// Take a note of a sprite's current position
		float sx = s.getX();
		float sy = s.getY();

		// Find out how wide and how tall a tile is
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();


		// Get position of tile to the right
		xR = (int) (player.getX() / tmap.getTileWidth() + 0.5);
		yR = (int) ((player.getY() + player.getHeight()) / tmap.getTileHeight() - 0.75);

		// Get position of tile to the left
		xL = (int) (player.getX() / tmap.getTileWidth());
		yL = (int) ((player.getY() + player.getHeight()) / tmap.getTileHeight() - 0.75);



		//Get the number of tiles across the x and y axis the sprite is positioned at
		int	xtile = (int)(sx / tileWidth);
		int ytile = (int)(sy / tileHeight);

		//tile character at the top left corner of the sprite
		char ch = tmap.getTileChar(xtile, ytile);

		if(ch == 2) 
		{
			player.setY(-10);
		}


		if (ch != '.') // If it's not a dot (empty space), handle it
		{
			// Here we just stop the sprite. 
			s.setVelocityX(0);
			s.stop();
			//Move the sprite to position that does not colliding
			if(ch =='l' || ch =='0' || ch == '1')
			{
				//Handle collision to the right side of the character
				player.setX(xR * tmap.getTileWidth() - (player.getImage().getWidth(null)-35));
			}
			
			//Handle collision to the left side of the character
			if(ch =='r' || ch =='t' || ch =='p' || ch =='3' || ch == 'z'|| ch == '2' || ch =='q' || ch =='i' || ch =='o')
			{
				player.setX(xL * tmap.getTileWidth() + tmap.getTileWidth());
			}
		}

		//Handle collision with coins
		if(ch == 's')
		{
			//Start audio when coin is collected
			Sound collect = new Sound("sounds/score collect.wav"); 
			collect.start();

			//Remove the coin which we collected
			tmap.setTileChar('.',xtile,ytile);
			total++;
			
			System.out.println(total);
		}

		xtile = (int)(sx / tileWidth);
		ytile = (int)((sy + s.getHeight())/ tileHeight);
		// We need to consider the other corners of the sprite
		// The above looked at the top left position, let's look at the bottom left.
		ch = tmap.getTileChar(xtile, ytile);

		// If it's not empty space
		if (ch != '.' ) 
		{
			s.setVelocityX(0);
			gravity=0;
			s.stop();
		}
		
		//If it is an empty space
		if(ch == '.')
		{
			gravity = 0.001f;
		}
		
		//If the sprite hits water reset the position of the character
		if(ch == 'w')
		{
			player.setX(64);
			player.setY(300);

		}
		
		//Handle coin collcetion
		if(ch == 's')
		{
			//Start collect audio when the character picks the coin
			Sound collect = new Sound("sounds/score collect.wav"); 
			collect.start();
			
			//Remove the coin we collected
			tmap.setTileChar('.',xtile,ytile);
			//Increase the score
			total++;
			
			System.out.println(total);
		}

	}


	public void checkSpriteCollision() 
	{
		//If in 'Game' state
		if (State == STATE.GAME)
		{

			// add all enemies to ArrayList
			ArrayList<Sprite> enemies = new ArrayList<>();
			enemies.add(enemy1);
			enemies.add(enemy2);
			enemies.add(enemy3);
			enemies.add(enemy4);
			enemies.add(enemy5);
			enemies.add(enemy6);

			boolean collided = false;

			for (Sprite enemy : enemies)
			{
				 // if the player and enemy collide with each other
				if (boundingBoxCollision(player, enemy))
				{
					 // if the player is landing on the enemy
					if (player.getVelocityY() > 0.2f)
					{
						 //play the mushrooms death sound
						Sound enemyDeath = new Sound("sounds/Goomba.wav");
						enemyDeath.start();
						
						//stop the enemy movement
						enemy.stop(); 
						
						//hide enemy
						enemy.hide(); 
						
						//move the enemy to some place not visible
						enemy.setX(0); 
						enemy.setY(0);
						total++;
					}
					//if enemy walked into the player
					else 
					{
						// set collided variable to true
						collided = true; 

					}
				}
			}

			if (collided)
			{
				// load the damage sound effect
				Sound damage = new Sound("sounds/hurt.wav"); 
				damage.start();

				//If the user is out of lives
				if (life == 1)
				{
					life--;
					player.setAnimation(dying_right);
					player.stop();
				}
				else
				{
					// if player moving right
					if (player.getPlayerDirection()) 
					{
						player.setAnimation(dying_right);
						// send the character flying to left
						player.setVelocityY(-0.2f); 
						player.setVelocityX(-0.2f);
						 // set the x and y position back a little to prevent 3 collisions happening at once
						player.setX(player.getX() - 15);
						player.setY(player.getY() - 10);
					}
					 //if player is moving left
					else 
					{
						player.setAnimation(dying_left);
						// send the character flying to right
						player.setVelocityY(-0.2f); 
						player.setVelocityX(0.2f);
						player.setX(player.getX() + 20);
						player.setY(player.getY() - 10);
					}
					//reduce the life
					life--;
				}

			}

			//If the character collides with the finish line
			if ((boundingBoxCollision(player, finish)))
			{
				finishLevel(); // call the finishLevel() method
			}


		}
	}




	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key)
		{
		case KeyEvent.VK_ESCAPE : stop(); break;
		case KeyEvent.VK_UP     : flap = false; break;

		case KeyEvent.VK_SPACE:
			jump = false; // set jump flag to false
			break;
		case KeyEvent.VK_LEFT:
			left = false; // stop travelling left
			decelerate = true; // start decelerating
			break;
		case KeyEvent.VK_RIGHT:
			right = false; // stop travelling right
			decelerate = true; // start decelerating
			break;


		default :  break;
		}
	}

	public void finishLevel()
	{
		 // if on level 1
		if (levelNumber == 1)
		{
			levelNumber++; 
			System.out.println("Level 1 Complete!");
			
			//load the second level
			init("map2.txt");
			//initialise the game again
			initialiseGame(); 
		}
		else if (levelNumber == 2)
		{
			levelNumber = 1;
			System.out.println("The End"); 
			
			//Change game state to complete to generate the game completed window
			Game.State = Game.STATE.COMPLETE; 
		}
	}







	public enum STATE // an enumerated list of states for the game to use (as above)
	{
		MENU,
		GAME,
		OPTIONS,
		DEAD,
		COMPLETE
	}









}
