package models.communication;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Packet {
    private static final Class[] values = {ClientConnect.class, GameData.class, Ready.class, RoundData.class, Done.class, Winner.class, NewPosition.class, Ranking.class};

    /*
    0: ClientConnect
    1: GameData
    2: Ready
    3: RoundData
    4: Done
    5: Winner
    6: NewPosition
    7: Ranking
     */
    public int type;

    public Sendable value;

    public Packet(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);
        type = jsonNode.get("type").asInt();
        JsonNode value = jsonNode.get("value");
        Object o = objectMapper.treeToValue(value, values[type]);
        this.value = (Sendable) o;
    }

    public Packet(int type, Sendable sendable) {
        this.type = type;
        this.value = sendable;
    }

    public static String createPacket(Sendable sendable) throws JsonProcessingException {
        for(int i = 0;i<values.length;i++) {
            if(values[i].isInstance(sendable)) {
                return createPacket(i, sendable);
            }
        }
        return "";
    }

    private static String createPacket(int type, Sendable sendable) throws JsonProcessingException {
        Packet p = new Packet(type, sendable);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(p);
    }

}
