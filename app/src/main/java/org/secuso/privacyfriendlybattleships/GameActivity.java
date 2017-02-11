package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameGrid;
import org.secuso.privacyfriendlybattleships.game.GameMode;

import java.util.Timer;

public class GameActivity extends BaseActivity {

    private Timer timerUpdate;
    private SharedPreferences preferences = null;

    private GameMode gameMode;
    private int gridSize;
    private GameController controller;
    private GameGridAdapter adapterMainGrid;
    private GameGridAdapter adapterMiniGrid;
    private GridView gridViewBig;
    private GridView gridViewSmall;
    private GameActivityLayoutProvider layoutProvider;
    private int positionGridCell;   // Save the current position of the grid cell clicked
    private View prevCell = null;
    private static final String TAG = GameActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupPreferences();
        setContentView(R.layout.activity_game);

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");

        this.gridSize = controller.getGridSize();
        this.gameMode = controller.getMode();

        // Create a GameActivityLayoutProvider in order to scale the grids appropriately
        layoutProvider = new GameActivityLayoutProvider(this, this.gridSize);

        // Set up the grids for player one
        setupGridViews();

        //set correct size for small grid
        gridViewSmall.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = gridViewSmall.getLayoutParams();
                layoutParams.width = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
                layoutParams.height = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
                Log.d(TAG, "" + layoutParams.width);
                gridViewSmall.setLayoutParams(layoutParams);
            }
        });
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game;
    }

    public void onClickButton(View view) {
        int column = this.positionGridCell % this.gridSize;
        int row = this.positionGridCell / this.gridSize;

        Boolean currentPlayer = this.controller.getCurrentPlayer();
        GameGrid GridUnderAttack = currentPlayer ?
                this.controller.getGridFirstPlayer() :
                this.controller.getGridSecondPlayer();

        //Don't attack same cell twice
        if(GridUnderAttack.getCell(column, row).isHit()){
            return;
        }

        boolean isHit = this.controller.makeMove(currentPlayer, column, row);
        if(!isHit) {
            controller.switchPlayers();
        }
        adapterMainGrid.notifyDataSetChanged();


        if( this.controller.getCurrentPlayer() &&
                (this.controller.getMode() == GameMode.VS_AI_EASY ||
                 this.controller.getMode() == GameMode.VS_AI_HARD) ){
            //make move for AI
            this.controller.getOpponentAI().makeMove();
            adapterMiniGrid.notifyDataSetChanged();
        }else {//setup view for next player
            setupGridViews();
        }
    }

    protected void setupGridViews() {

        // Get the grid views of the respective XML-files
        gridViewBig = (GridView) findViewById(R.id.game_gridview_big);
        gridViewSmall = (GridView) findViewById(R.id.game_gridview_small);

        // Set the background color of the grid
        gridViewBig.setBackgroundColor(Color.GRAY);
        gridViewSmall.setBackgroundColor(Color.GRAY);

        // Set the columns of the grid
        gridViewBig.setNumColumns(this.gridSize);
        gridViewSmall.setNumColumns(this.gridSize);

        // Set the layout of the grids
        final ViewGroup.MarginLayoutParams marginLayoutParamsBig =
                (ViewGroup.MarginLayoutParams) gridViewBig.getLayoutParams();
        final ViewGroup.MarginLayoutParams marginLayoutParamsSmall =
                (ViewGroup.MarginLayoutParams) gridViewSmall.getLayoutParams();

        marginLayoutParamsBig.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),0);
        marginLayoutParamsSmall.setMargins(layoutProvider.getMarginLeft(), layoutProvider.getMargin(), layoutProvider.getMarginRight(),layoutProvider.getMargin());

        gridViewBig.setLayoutParams(marginLayoutParamsBig);
        gridViewSmall.setLayoutParams(marginLayoutParamsSmall);

        ViewGroup.LayoutParams layoutParams = gridViewSmall.getLayoutParams();
        layoutParams.width = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
        layoutParams.height = layoutProvider.getMiniGridCellSizeInPixel() * gridSize + gridSize-1;
        Log.d(TAG, "" + layoutParams.width);
        gridViewSmall.setLayoutParams(layoutParams);

        gridViewBig.setHorizontalSpacing(1);
        gridViewBig.setVerticalSpacing(1);

        gridViewSmall.setHorizontalSpacing(1);
        gridViewSmall.setVerticalSpacing(1);

        // Initialize the grid for player one
        adapterMainGrid = new GameGridAdapter(this, this.layoutProvider, this.controller, true);
        gridViewBig.setAdapter(adapterMainGrid);
        adapterMiniGrid= new GameGridAdapter(this, this.layoutProvider, this.controller, false);
        gridViewSmall.setAdapter(adapterMiniGrid);

        // Define the listener for the big grid view, such that it is possible to click on it. When
        // clicking on that grid, the corresponding cell should be yellow.
        gridViewBig.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(prevCell != null){
                    prevCell.setBackgroundColor(Color.WHITE);
                }
                positionGridCell = i;
                view.setBackgroundColor(Color.YELLOW);
                prevCell = view;
                // Display the grid cell, which was clicked.
                adapterMainGrid.notifyDataSetChanged();
            }
        });


    }

    public GridView getGridViewBig() {
        return gridViewBig;
    }
}
