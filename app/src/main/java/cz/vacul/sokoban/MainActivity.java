package cz.vacul.sokoban;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private GraphicsView gv;
    private Button restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gv = (GraphicsView)(findViewById(R.id.view));
        gv.lvlText((TextView) findViewById(R.id.textView));
        gv.setLevel(0);
        restart = (Button) findViewById(R.id.button);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gv.restartLvl();
            }
        });
    }
}
