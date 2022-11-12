package com.keshav.example.flappybird.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    Texture bg;
    Texture base;
    Texture[] birds;
    Texture bottomPipe;
    Texture topPipe;
    Texture gameOver;
    Texture message;
    Texture start;

    ArrayList<Float> baseX = new ArrayList<>();

    int birdState = 0;
    float speed = 0;
    float birdY = 0;

    int pipeState = 0;
    Random random;
    ArrayList<Float> pipeX = new ArrayList<>();
    ArrayList<Float> pipeY = new ArrayList<>();

    int score = 0;
    BitmapFont font;

    int gameState = 0;

    int screenWidth;
    int screenHeight;
    int birdWidth;
    int birdHeight;
    float pipeWidth;
    float pipeHeight;
    int messageWidth;
    int messageHeight;
    int gameOverWidth;
    int gameOverHeight;

    //Sound
    Sound wingSound;
    Sound pointSound;
    Sound hitSound;

    //Pref
    Preferences preferences;
    int highScore = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        bg = new Texture("bg.png");
        base = new Texture("bs.png");
        birds = new Texture[]{new Texture("bird.png"), new Texture("bird2.png")};
        bottomPipe = new Texture("bottomtube.png");
        topPipe = new Texture("toptube.png");
        gameOver = new Texture("gameover.png");
        message = new Texture("message.png");
        start = new Texture("start.png");

        random = new Random();
        font = new BitmapFont();
        font.getData().setScale(3);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        birdWidth = birds[0].getWidth();
        birdHeight = birds[0].getHeight();
        pipeWidth = 75;
        pipeHeight = 6 * screenHeight / 10f;
        messageWidth = 400;
        messageHeight = 600;
        gameOverWidth = 400;
        gameOverHeight = 150;

        for (int i = 0; i < screenWidth / 30f + 1; i++) {
            baseX.add(i * 30f);
        }

        birdY = screenHeight;

        wingSound = Gdx.audio.newSound(Gdx.files.internal("sfx_wing.wav"));
        pointSound = Gdx.audio.newSound(Gdx.files.internal("sfx_point.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("sfx_hit.wav"));

        preferences = Gdx.app.getPreferences("My Preferences");
        highScore = preferences.getInteger("highScore");

    }

    @Override
    public void render() {
//      shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        batch.begin();

        //Background
        batch.draw(bg, 0, 0, screenWidth, screenHeight);

        //Pipes
        if (gameState == 1) {
            if (pipeState == 74) {
                pipeX.add((float) screenWidth);

                float state = random.nextFloat() * 2;
                pipeY.add(-state * screenHeight / 10f);
            }

            pipeState++;
            pipeState = pipeState % 75;

            ArrayList<Integer> removePipes = new ArrayList<>();
            ArrayList<Rectangle> rectangles = new ArrayList<>();

//            shapeRenderer.setColor(Color.GREEN);
            for (int i = 0; i < pipeX.size(); i++) {
                float x = pipeX.get(i);
                float y = pipeY.get(i);

                batch.draw(bottomPipe, x, y, pipeWidth, pipeHeight);
                batch.draw(topPipe, x, y + (8 * screenHeight / 10f), pipeWidth, pipeHeight);

/*
                shapeRenderer.rect(x, y, pipeWidth, pipeHeight);
                shapeRenderer.rect(x, y + (8 * screenHeight / 10f), pipeWidth, pipeHeight);
*/

                rectangles.add(new Rectangle(x, y, pipeWidth, pipeHeight));
                rectangles.add(new Rectangle(x, y + (8 * screenHeight / 10f), pipeWidth, pipeHeight));

                if (x >= -pipeWidth) {
                    pipeX.set(i, x - 5);
                } else {
                    removePipes.add(i);
                    score++;
                    pointSound.play(0.5f);
                    Gdx.app.log("SCORE", " " + score);
                }
            }

            for (int i : removePipes) {
                pipeX.remove(i);
                pipeY.remove(i);
            }

            //Bird Position with Touch
            if (Gdx.input.justTouched()) {
                //Touch DOWN EVENT

                if (birdY < screenHeight - birdHeight - 10) {
                    speed = 10;
                } else {
                    speed = 0;
                }
                wingSound.play(0.5f);
            } else if (birdY > screenHeight / 10f) {
                speed -= 0.7;
            } else {
                speed = 0;
            }

            birdY += speed;

            birdY = Math.max(birdY, screenHeight / 10f);
            birdY = Math.min(birdY, screenHeight - birdHeight);

            //Bird Image Animation
            batch.draw(birds[birdState / 5], 50, birdY);
            birdState++;
            birdState = birdState % 10;

            //Score
            font.draw(batch, "Score : " + score, 25, screenHeight - 25);

            //Collision detection
            Circle birdCircle = new Circle(50 + birdWidth / 2f, birdY + birdHeight / 2f, birdHeight / 2f);

//            shapeRenderer.setColor(Color.RED);
//            shapeRenderer.circle(50 + birdWidth/2f, birdY + birdHeight / 2f, birdHeight / 2f);

            for (Rectangle rectangle : rectangles) {
                if (Intersector.overlaps(birdCircle, rectangle)) {
                    Gdx.app.log("Collision", "OUT");
                    hitSound.play(0.5f);
                    gameState = 2;
                    preferences.putInteger("highScore",score);
                }
            }

            if (birdY == screenHeight / 10f) {
                hitSound.play(0.5f);
                gameState = 2;
            }

        } else if (gameState == 0) {
            batch.draw(message, (screenWidth - messageWidth) / 2f, (screenHeight - messageHeight) / 2f, messageWidth, messageHeight);
            if (Gdx.input.justTouched()) {
                gameState = 1;

                birdY = screenHeight;
                score = 0;
                speed = 0;
                pipeX.clear();
                pipeY.clear();
                wingSound.play(0.5f);

            }
        } else {
            for (int i = 0; i < pipeX.size(); i++) {
                float x = pipeX.get(i);
                float y = pipeY.get(i);

                batch.draw(bottomPipe, x, y, pipeWidth, pipeHeight);
                batch.draw(topPipe, x, y + (8 * screenHeight / 10f), pipeWidth, pipeHeight);
            }
            batch.draw(birds[birdState / 5], 50, birdY);
            batch.draw(gameOver, (screenWidth - gameOverWidth) / 2f, screenHeight / 2f + 25, gameOverWidth, gameOverHeight);
            font.draw(batch, "Score : " + score, screenWidth / 2f - 80, screenHeight / 2f - 25);

            float startX = (screenWidth - start.getWidth()) / 2f;
            float startY = screenHeight / 2f - 200;

            batch.draw(start, startX, startY);

            if (Gdx.input.justTouched() &&
                    Gdx.input.getX() > startX && Gdx.input.getX() < startX + start.getWidth() &&
                    Gdx.input.getY() < (screenHeight - startY) && Gdx.input.getY() > (screenHeight - startY - start.getHeight())) {

                gameState = 0;

            }

            Gdx.app.log("highScore",String.valueOf(preferences.getInteger("highScore")));

        }

        //Base
        //batch.draw(base, 0, 0, screenWidth, screenHeight / 10f);
        for (int i = 0; i < baseX.size(); i++) {
            float a = baseX.get(i);
            batch.draw(base, a, 0, base.getWidth(), screenHeight / 10f);
            if (gameState == 1) {
                if (a < -45) {
                    baseX.add((baseX.size() - 1) * 30f - 15);
                    baseX.remove(0);
                } else {
                    baseX.set(i, a - 5);
                }
            }
        }
        batch.end();
//        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        bg.dispose();
        base.dispose();
        bottomPipe.dispose();
        topPipe.dispose();
        gameOver.dispose();
        message.dispose();
        start.dispose();

        wingSound.dispose();
        pointSound.dispose();
        hitSound.dispose();
    }
}
