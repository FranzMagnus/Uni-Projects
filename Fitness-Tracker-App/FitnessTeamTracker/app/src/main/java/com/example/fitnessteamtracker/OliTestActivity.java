package com.example.fitnessteamtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fitnessteamtracker.Util.Consumer;
import com.example.fitnessteamtracker.fitness.FitnessActivity;
import com.example.fitnessteamtracker.fitness.JumpingJackListener;
import com.example.fitnessteamtracker.fitness.PushupListener;
import com.example.fitnessteamtracker.fitness.SquatListener;
import com.example.fitnessteamtracker.fitness.StepListener;
import com.example.fitnessteamtracker.models.communication.ClientConnect;
import com.example.fitnessteamtracker.models.communication.Packet;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.appcompat.app.AppCompatActivity;
import tech.gusavila92.websocketclient.WebSocketClient;

public class OliTestActivity extends AppCompatActivity {
    private TextView txt;
    private LinearLayout layout;

    private Vibrator v;

    private PushupListener pushupListener;
    private JumpingJackListener jumpingJackListener;
    private SquatListener squatListener;
    private StepListener stepListener;

    private int mode;
    private boolean tracking;

    private WebSocketClient webSocketClient;
    private int counter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oli_test);

        txt = findViewById(R.id.txtView_oli_2);
        layout = findViewById(R.id.layout_oli_background);

        pushupListener = new PushupListener(this, savedInstanceState, new Runnable() {
            @Override
            public void run() {
                pushupDetected();
            }
        }, new Consumer<PushupListener.FailedPushup>() {
            @Override
            public void accept(PushupListener.FailedPushup failedPushup) {
                failedPushup(failedPushup);
            }
        });

        jumpingJackListener = new JumpingJackListener(this, savedInstanceState, new Runnable() {
            @Override
            public void run() {
                jumpingJackDetected();
            }
        }, new Consumer<JumpingJackListener.FailedJumpingJack>() {
            @Override
            public void accept(JumpingJackListener.FailedJumpingJack failedJumpingJack) {
                failedJumpingJack(failedJumpingJack);
            }
        });

        squatListener = new SquatListener(this, savedInstanceState, new Runnable() {
            @Override
            public void run() {
                squatDetected();
            }
        }, new Consumer<SquatListener.FailedSquat>() {
            @Override
            public void accept(SquatListener.FailedSquat failedSquat) {
                failedSquat(failedSquat);
            }
        });

        stepListener = new StepListener(this, savedInstanceState, new Runnable() {
            @Override
            public void run() {
                stepDetected();
            }
        });

        final Button btnStart = findViewById(R.id.btn_oli_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (tracking) {
                    if (mode == 0) {
                        pushupListener.stop();
                    } else if (mode == 1) {
                        jumpingJackListener.stop();
                    } else if (mode == 2) {
                        squatListener.stop();
                    } else if (mode == 3) {
                        stepListener.stop();
                    }

                    btnStart.setText("Start Tracking");
                    tracking = false;
                } else {
                    if (mode == 0) {
                        pushupListener.start();
                    } else if (mode == 1) {
                        jumpingJackListener.start();
                    } else if (mode == 2) {
                        squatListener.start();
                    } else if (mode == 3) {
                        stepListener.start();
                    }

                    btnStart.setText("Stop Tracking");
                    tracking = true;
                }
            }
        });

        final Button btnSwitch = findViewById(R.id.btn_oli_switch);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = (mode + 1) % 4;
                if (mode == 0) {
                    btnSwitch.setText("Switch mode (Pushup)");
                } else if (mode == 1) {
                    btnSwitch.setText("Switch mode (Jumping Jack)");
                } else if (mode == 2) {
                    btnSwitch.setText("Switch mode (Squats)");
                } else if (mode == 3) {
                    btnSwitch.setText("Switch mode (Steps)");
                }
            }

        });

        final Button btnTest = findViewById(R.id.btn_oli_test);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OliTestActivity.this, FitnessActivity.class);
                Bundle b = new Bundle();
                b.putInt("exerciseID", 0);
                b.putInt("neededCount", 10);
                b.putInt("time", 60);
                intent.putExtras(b);
                startActivityForResult(intent, 0);
            }

        });

        final Button btnWeb = findViewById(R.id.btn_oli_web);
        btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;
                webSocketClient.send("Button clicked " + counter + " times");
            }

        });

        TextView txt = findViewById(R.id.txt_oli_json);
        ClientConnect c = new ClientConnect("deineMutter");
        try {
            txt.setText(Packet.createPacket(c));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        final Button btnGame = findViewById(R.id.btn_oli_game);
        btnGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OliTestActivity.this, GameTestActivity.class);
                startActivity(intent);
            }

        });

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //createWebSocketClient();
    }

    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://192.168.178.20:9000/ws");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                ClientConnect cC = new ClientConnect("myID1");
                try {
                    webSocketClient.send(Packet.createPacket(cC));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TextView textView = findViewById(R.id.txt_oli_socket);
                            textView.setText(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (0): {
                if (resultCode == Activity.RESULT_OK) {
                    txt.setText(data.getStringExtra("resultText"));
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //pushupListener.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //pushupListener.pause();
    }

    private void vibrate(int millis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(millis);
        }
    }

    private void pushupDetected() {
        vibrate(500);
        txt.setText("Pushups: " + pushupListener.pushupCount);
        layout.setBackgroundColor(pushupListener.pushupCount % 2 == 0 ? Color.GREEN : Color.BLUE);
    }

    private void failedPushup(PushupListener.FailedPushup reason) {
        if (reason == PushupListener.FailedPushup.TooFast) {
            layout.setBackgroundColor(Color.RED);
        } else if (reason == PushupListener.FailedPushup.NotDeepEnough) {
            layout.setBackgroundColor(Color.YELLOW);
        }
    }

    private void jumpingJackDetected() {
        vibrate(500);
        txt.setText("Jumping Jacks: " + jumpingJackListener.jumpingJackCount);
        layout.setBackgroundColor(jumpingJackListener.jumpingJackCount % 2 == 0 ? Color.GREEN : Color.BLUE);
    }

    private void failedJumpingJack(JumpingJackListener.FailedJumpingJack reason) {
        if (reason == JumpingJackListener.FailedJumpingJack.TooFast) {
            layout.setBackgroundColor(Color.RED);
        } else if (reason == JumpingJackListener.FailedJumpingJack.NotHighEnough) {
            layout.setBackgroundColor(Color.YELLOW);
        } else if (reason == JumpingJackListener.FailedJumpingJack.TooSlow) {
            layout.setBackgroundColor(Color.CYAN);
        }
    }

    private void squatDetected() {
        vibrate(500);
        txt.setText("Squats: " + squatListener.squatCount);
        layout.setBackgroundColor(squatListener.squatCount % 2 == 0 ? Color.GREEN : Color.BLUE);
    }

    private void failedSquat(SquatListener.FailedSquat reason) {
        if (reason == SquatListener.FailedSquat.TooFast) {
            layout.setBackgroundColor(Color.RED);
        }
    }

    private void stepDetected() {
        vibrate(500);
        txt.setText("Steps: " + stepListener.stepCount);
        layout.setBackgroundColor(stepListener.stepCount % 2 == 0 ? Color.GREEN : Color.BLUE);
    }
}
