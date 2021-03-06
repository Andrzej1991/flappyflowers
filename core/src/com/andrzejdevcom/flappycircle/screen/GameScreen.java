package com.andrzejdevcom.flappycircle.screen;

import com.andrzejdevcom.flappycircle.SkippyFlowersGame;
import com.andrzejdevcom.flappycircle.assets.AssetDescriptors;
import com.andrzejdevcom.flappycircle.assets.RegionNames;
import com.andrzejdevcom.flappycircle.common.ScoreController;
import com.andrzejdevcom.flappycircle.config.GameConfig;
import com.andrzejdevcom.flappycircle.entity.Flower;
import com.andrzejdevcom.flappycircle.entity.Skippy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;

class GameScreen implements Screen {

    private final SkippyFlowersGame game;
    private final ScoreController scoreController;
    private final SpriteBatch batch;
    private final AssetManager assetManager;

    private OrthographicCamera camera;
    private StretchViewport viewport;
    private ShapeRenderer rendered;
    private StretchViewport hudViewport;
    private BitmapFont font;
    private GlyphLayout glyph = new GlyphLayout();

    private Skippy skippe;
    private Array<Flower> flowers = new Array<Flower>();
    private float skippyStartX;
    private float skippyStartY;
    private boolean changeScreen;
    private Sound hit, jump, score;
    private TextureRegion topRegion, botRegion, trumpRegion;

    GameScreen(SkippyFlowersGame game) {
        this.game = game;
        scoreController = game.getScoreController();
        batch = game.getBatch();
        assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        hudViewport = new StretchViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT);
        rendered = new ShapeRenderer();
        font = assetManager.get(AssetDescriptors.SCORE_FONT);
        skippyStartX = GameConfig.WORLD_WIDTH / 4f;
        skippyStartY = GameConfig.WORLD_HEIGHT / 2f;
        hit = assetManager.get(AssetDescriptors.HIT);
        jump = assetManager.get(AssetDescriptors.JUMP);
        score = assetManager.get(AssetDescriptors.SCORE);
        skippe = new Skippy();
        skippe.setPosition(skippyStartX, skippyStartY);
        TextureAtlas atlas = assetManager.get(AssetDescriptors.GAME_PLAY);
        botRegion = atlas.findRegion(RegionNames.MERKEL_BOTTOM);
        topRegion = atlas.findRegion(RegionNames.MERKEL_TOP);
        trumpRegion = atlas.findRegion(RegionNames.TRUMP);

        createNewFlower();
        restart();
        game.playServices.hideText();
        game.playServices.unlockAchievement();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update(delta);
        hudViewport.apply();
        renderGamePlay();
        renderHud();
        renderDebug();
        if (changeScreen) {
            scoreController.updateHighScore();
            game.setScreen(new ScoreDialogScreen(game));
        }

    }

    private void renderGamePlay() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawGamePlay();
        batch.end();
    }

    private void drawGamePlay() {
        batch.draw(trumpRegion, skippe.getX() - GameConfig.SKIPPY_HALF_SIZE, skippe.getY() - GameConfig.SKIPPY_HALF_SIZE,
                GameConfig.SKIPPY_SIZE, GameConfig.SKIPPY_SIZE);
        for (Flower flower : flowers) {
            float bottmRegionX = flower.getBottomcollistionCircle().x - Flower.WIDTH / 2f;
            float bottomRegionY = flower.getBottomCollisionRect().y + flower.getBottomcollistionCircle().radius;
            batch.draw(botRegion, bottmRegionX, bottomRegionY,Flower.WIDTH, Flower.HEIGHT);
            float topRegionX = flower.getTopCollistionCircle().x - Flower.WIDTH / 2;
            float topRegionY = flower.getTopCollisionRect().y - flower.getTopCollistionCircle().radius;
            batch.draw(topRegion, topRegionX, topRegionY, Flower.WIDTH, Flower.HEIGHT);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        rendered.dispose();
    }

    private void renderHud() {
        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        drawHud();
        batch.end();
    }

    private void drawHud() {
        String scoreString = scoreController.getScoreString();
        glyph.setText(font, scoreString);
        float scoreX = (GameConfig.HUD_WIDTH - glyph.width) / 2f;
        float scoreY = 4 * GameConfig.HUD_HEIGHT / 5 - glyph.height / 2;
        font.draw(batch, scoreString, scoreX, scoreY);
    }

    private void update(float delta) {
        skippe.update(delta);
        if (Gdx.input.justTouched()) {
            jump.play();
            skippe.flyUp();
        }
        blockSkippyFromLeavingTheWorld();
        for (Flower flower : flowers) {
            flower.update(delta);
        }
        spawnFlower();
        removePassedFlowers();
        checkCollision();
        updateScore();
        game.setCurrentGameScore(scoreController.getScoreString());

    }

    private void checkCollision() {
        for (Flower flower : flowers) {
            if (flower.isSkippyColliding(skippe)) {
                hit.play();
                changeScreen = true;
            }
        }
    }

    private void restart() {
        skippe.setPosition(skippyStartX, skippyStartY);
        flowers.clear();
        scoreController.reset();
    }

    private void removePassedFlowers() {
        if (flowers.size > 0) {
            Flower firstFlower = flowers.first();
            if (firstFlower.getX() <- Flower.WIDTH) {
                flowers.removeValue(firstFlower, true);
            }
        }
    }

    private void createNewFlower() {
        Flower flower = new Flower();
        flower.setX(GameConfig.WORLD_WIDTH + Flower.WIDTH);
        flowers.add(flower);
    }

    private void spawnFlower() {
        if (flowers.size == 0) {
            createNewFlower();
        } else {
            Flower lastFlower = flowers.peek();
            if (lastFlower.getX() < GameConfig.WORLD_WIDTH - GameConfig.GAP_BETWEEN_FLOWERS) {
                createNewFlower();
            }
        }
    }

    private void blockSkippyFromLeavingTheWorld() {
        float newY = MathUtils.clamp(skippe.getY(), 0, GameConfig.WORLD_HEIGHT);
        skippe.setY(newY);
    }

    private void renderDebug() {
        rendered.setProjectionMatrix(camera.combined);
        rendered.begin(ShapeRenderer.ShapeType.Line);
        drawDebug();
        rendered.end();
    }

    private void updateScore() {
        if (flowers.size > 0) {
            Flower flower = flowers.first();
            if (!flower.isScoreCollected() && flower.isSkippyCollidingWithSensor(skippe)) {
                score.play();
                flower.collectScore();
                scoreController.incrementScore();
            }
        }
    }

    private void drawDebug() {
//        Circle skippyCollisionsCircle = skippe.getCollistionCircle();
//        rendered.circle(skippyCollisionsCircle.x, skippyCollisionsCircle.y,
//                skippyCollisionsCircle.radius, 30);
//        for (Flower flower : flowers) {
//            Circle bottomflowerCollisionCircle = flower.getBottomcollistionCircle();
//            Rectangle bottomCollisionRectangle = flower.getBottomCollisionRect();
//            rendered.circle(bottomflowerCollisionCircle.x, bottomflowerCollisionCircle.y,
//                    bottomflowerCollisionCircle.radius, 30);
//            rendered.rect(bottomCollisionRectangle.x, bottomCollisionRectangle.y,
//                    bottomCollisionRectangle.width, bottomCollisionRectangle.height);
//            Circle topFlowersCollisionCircle = flower.getTopCollistionCircle();
//            Rectangle topCollisionRectanlge = flower.getTopCollisionRect();
//            rendered.circle(topFlowersCollisionCircle.x, topFlowersCollisionCircle.y,
//                    topFlowersCollisionCircle.radius, 30);
//            rendered.rect(topCollisionRectanlge.x, topCollisionRectanlge.y,
//                    topCollisionRectanlge.width, topCollisionRectanlge.height);
//            Rectangle sensorRectangle = flower.getSensorRectangle();
//            rendered.rect(sensorRectangle.x, sensorRectangle.y, sensorRectangle.width, sensorRectangle.height, 0, 0, 0, 0, 0);
//        }
    }


}