package cz.vacul.sokoban;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class GraphicsView extends View {

    private int lx = 10;
    private int ly = 10;

    TextView lvltxt = null;

    private int phoneWidth;
    private int phoneHeight;
    private float bitmapSize;
    private Paint tPaint;
    private int[] moved;
    private int positionInArray;
    private List<Integer> goalList = null;
    private int levelNumb = 0;
    //TODO
    //nacitat levely ze souboru
    private int level[];

    Bitmap[] bmp;

    public GraphicsView(Context context) {
        this(context, null);
    }

    public GraphicsView(Context context, AttributeSet attset) {
        super(context, attset);
        setFocusable(true);
        setFocusableInTouchMode(true);
        tPaint = new Paint();
        bmp = new Bitmap[6];
        bmp[0] = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
        bmp[1] = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        bmp[2] = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        bmp[3] = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
        bmp[4] = BitmapFactory.decodeResource(getResources(), R.drawable.hero);
        bmp[5] = BitmapFactory.decodeResource(getResources(), R.drawable.boxok);
    }

    public void setLevel(int newLevel) {
        levelNumb = newLevel;
        lvltxt.setText("Level: " + levelNumb);

        try {
            loadLevel("L".concat(String.valueOf(levelNumb)));  //method for load level
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Integer> getGoals() {
        ArrayList<Integer> goalList = new ArrayList<>();
        for(int i = 0; i < level.length; i++){
            if(level[i] == 3 || level[i] == 5)
                goalList.add(i);
            else if(level[i] == 4)
                positionInArray = i;
        }
        return  goalList;
    }

    private void calculateSize() {
        phoneWidth = getResources().getDisplayMetrics().widthPixels;
        phoneHeight = getResources().getDisplayMetrics().heightPixels;
        this.bitmapSize = bmp[0].getWidth();
    }

    public void loadLevel(String levelName) throws IOException{
        Log.d("Loading level: ", levelName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.level)));
        List<String> loadingLevel = new ArrayList<>();
        String line;
        Boolean found = false;
        int maxLength = 0;
        int i = 0;

        while((line = reader.readLine()) != null) {
                if(!line.isEmpty() && !found) {
                    if (line.equals(levelName)) {
                        found = true;
                        continue;
                    }
                    continue;
                }
                if(line.contains(";"))
                    break;
                loadingLevel.add(line);
                if (line.length() > maxLength) {
                maxLength = line.length();
            }
        }
        level = new int[loadingLevel.size() * maxLength];
        ly = loadingLevel.size();
        lx = maxLength;


        for(String l: loadingLevel) {
            for (int j = 0; j < maxLength; j++) {
                if (j > l.length() - 1) {
                    level[i] = 0;
                    i++;
                    continue;
                }
                switch (l.charAt(j)) {
                    case '#':
                        level[i] = 1;
                        i++;
                        break;
                    case '.':
                        level[i] = 3;
                        i++;
                        break;
                    case ' ':
                        level[i] = 0;
                        i++;
                        break;
                    case '@':
                        level[i] = 4;
                        i++;
                        break;
                    case '$':
                        level[i] = 2;
                        i++;
                        break;
                    case '*':
                        level[i] = 5;
                        i++;
                        break;
                }
            }
        }
        reader.close();
        goalList = getGoals();
        calculateSize();
        invalidate();
        Log.d("Level size: ", String.valueOf(loadingLevel.size()));
    }

    private int checkPosition(float x, float y) {
        if( x > 0 && x < 250 && y > 250 && y < (phoneHeight - 250))
            return 1; // move left
        if( x > (phoneWidth - 250) && x < phoneWidth && y > 250 && y < (phoneHeight - 250))
            return 2; // move right
        if(y > 0 && y < 400 )
            return 3; // move up
        if( y > (phoneHeight -850) && y < phoneHeight)
            return 4; // move down
        return 0;
    }

    public int checkBoxMovement(int direction) {
        // if hero would like go thru the wall
        if(level[positionInArray + moved[direction - 1]] == 1) {
            return 0;
        }

        // if ahead of hero is box / boxOk  and if ahead of box is next box / boxOK
        if(level[positionInArray + moved[direction - 1]] == 2 || level[positionInArray + moved[direction - 1]] == 5) {
            if(level[positionInArray + moved[direction - 1] * 2] == 2 || level[positionInArray + moved[direction - 1] * 2] == 5 ||
                    level[positionInArray + moved[direction - 1] * 2] == 1) {
                return 0; // no move / two box before
            } else {
                return 2; // move with box
            }
        } else {
            return 1; // clear move without box
        }
    }

    private int ifWin() {
        for(int i = 0; i < level.length; i++) {
            if(level[i] == 2)
                return 0;
        }
        return 1;
    }

    public void lvlText(TextView t){
        lvltxt = t;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int position = checkPosition(event.getX(), event.getY());
                if (position > 0) {
                    moved = new int[4];

                    moved[0] = -1;  // move left
                    moved[1] = 1;  // move right
                    moved[2] = -lx; // move up
                    moved[3] = lx; // move down

                    int result = checkBoxMovement(position);

                    if (result > 0) {
                        level[positionInArray + moved[position - 1]] = 4;
                        if (result > 1) {
                            if (level[positionInArray + moved[position - 1] * 2] == 3)
                                level[positionInArray + moved[position - 1] * 2] = 5;
                            else
                                level[positionInArray + moved[position - 1] * 2] = 2;

                        }

                        // change old bitmap position of hero
                        if (goalList.contains(positionInArray)) {
                            level[positionInArray] = 3;
                        } else {
                            level[positionInArray] = 0;
                        }
                        positionInArray += moved[position - 1];
                        invalidate();
                    }
                }
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.phoneWidth = w;
        this.phoneHeight = h;
        calculateSize();
    }

    public void restartLvl(){
        setLevel(levelNumb);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < ly; i++)
            for (int j = 0; j < lx; j++)
                canvas.drawBitmap(bmp[level[i * lx + j]], j * bitmapSize, i * bitmapSize, tPaint);
        if (ifWin() == 1) {
            levelNumb++;
            level = null;
            setLevel(levelNumb);
        }
    }
}