package com.andrzejdevcom.flappycircle.screen;

import com.andrzejdevcom.flappycircle.SkippyFlowersGame;
import com.andrzejdevcom.flappycircle.config.GameConfig;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SettingsScreen extends ApplicationAdapter implements Screen {

    private static final String MUSIC_KEY = "musicSettings";
    private final Preferences prefs;

    private Viewport viewport;
    private Stage stage;
    public static SkippyFlowersGame game;

    private TextButton musicOptions;
    private TextButton soundOption;
    private TextButton exitOption;

    SettingsScreen(SkippyFlowersGame game) {
        this.game = game;
        prefs = Gdx.app.getPreferences(SkippyFlowersGame.class.getSimpleName());
    }

    @Override
    public void show() {
        viewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT);
        stage = new Stage(viewport, game.getBatch());
        initUI();
        Gdx.input.setInputProcessor(stage);
        game.playServices.hideText();
    }

    private void initTextureRegions() {
        Table table = new Table();
        table.setSize(viewport.getScreenWidth(), viewport.getScreenHeight());
        final Skin skin = new Skin();
        TextureAtlas buttonAtlas = new TextureAtlas(Gdx.files.internal("skin/ui-orange.atlas"));
        skin.addRegions(buttonAtlas);
        BitmapFont font = new BitmapFont();
        font.setColor(0, 0, 0, 1);
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = skin.getDrawable("button_04");
        musicOptions = new TextButton("MUSIC ON", textButtonStyle);
        musicOptions.getLabel().setFontScale(2.0f, 2.0f);
        musicOptions.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (game.getMusic().isPlaying()) {
                    game.getMusic().stop();
                    musicOptions.setText("MUSIC OFF");
                    prefs.putBoolean(MUSIC_KEY, false);
                    prefs.flush();
                } else {
                    game.getMusic().play();
                    musicOptions.setText("MUSIC ON");
                    prefs.putBoolean(MUSIC_KEY, true);
                    prefs.flush();
                }
            }
        });
        soundOption = new TextButton("SOUND ON", textButtonStyle);
        soundOption.getLabel().setFontScale(2.0f, 2.0f);
        soundOption.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

            }
        });

        exitOption = new TextButton("EXIT", textButtonStyle);
        exitOption.getLabel().setFontScale(2.0f, 2.0f);
        exitOption.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new StartScreen(game));
            }
        });
        table.add(musicOptions).minWidth(GameConfig.WIDTH / 2f).minHeight(GameConfig.HEIGHT / 6);
        table.row();
        table.add(soundOption).minWidth(GameConfig.WIDTH / 2f).minHeight(GameConfig.HEIGHT / 6);
        table.row();
        table.add(exitOption).minWidth(GameConfig.WIDTH / 2f).minHeight(GameConfig.HEIGHT / 6);
        table.row();
        stage.addActor(table);
    }

    private void initUI() {
        initTextureRegions();
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        stage.dispose();
    }
}
