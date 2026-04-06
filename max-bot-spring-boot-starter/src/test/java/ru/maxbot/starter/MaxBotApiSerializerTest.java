package ru.maxbot.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.maxbot.core.transport.IncomingUpdateList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MaxBotApiSerializerTest {

    @Test
    void deserializesIncomingUpdateWhenApiUsesMessageFieldInsteadOfBody() throws Exception {
        ObjectMapper objectMapper = MaxBotAutoConfiguration.createObjectMapper();

        String json = """
                {
                  "updates": [
                    {
                      "message": {
                        "recipient": {
                          "chat_id": 10001,
                          "chat_type": "dialog",
                          "user_id": 20002
                        },
                        "timestamp": 1700000000000,
                        "sender": {
                          "user_id": 30003,
                          "first_name": "Test",
                          "last_name": "",
                          "is_bot": false,
                          "last_activity_time": 1700000000000,
                          "name": "Test User"
                        },
                        "message": {
                          "mid": "mid.test-message-id",
                          "seq": 123456789,
                          "text": "/order"
                        }
                      },
                      "timestamp": 1700000000000,
                      "user_locale": "ru",
                      "update_type": "message_created"
                    }
                  ],
                  "marker": 42
                }
                """;

        IncomingUpdateList updateList = objectMapper.readValue(json, IncomingUpdateList.class);

        assertNotNull(updateList);
        assertNotNull(updateList.getUpdates());
        assertEquals(1, updateList.getUpdates().size());

        var update = updateList.getUpdates().get(0);
        assertNotNull(update.getMessage().getBody());
        assertEquals("/order", update.getMessage().getBody().getText());
        assertEquals("mid.test-message-id", update.getMessage().getBody().getMid());
    }
}


