package controllers;

import akka.actor.*;

public class MyWebSocketActor extends AbstractActor {

    public static Props props(ActorRef out, GameController gameController) {
        return Props.create(MyWebSocketActor.class, out, gameController);
    }

    private final ActorRef out;
    private final GameController gameController;

    public MyWebSocketActor(ActorRef out, GameController gameController) {
        this.gameController = gameController;
        this.out = out;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    gameController.receive(message, this);
                })
                .build();
    }

    public void send(String message) {
        out.tell(message, self());
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        self().tell(PoisonPill.getInstance(), self());
    }
}