package io.github.jaehyeonhan.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jaehyeonhan.project.controller.dto.request.CreateChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.SendMessageRequest;
import io.github.jaehyeonhan.project.controller.dto.response.MessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createChat_returns201_with_chatId() throws Exception {
        // given
        CreateChatRequest request = new CreateChatRequest("1234");

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatId").value("1234"));
    }

    @Test
    void joinChat_returns200() throws Exception {
        // given
        String chatId = "1234";

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/chats/" + chatId));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void sendMessage_returns200() throws Exception {
        // given
        String chatId = "1234";
        SendMessageRequest request = new SendMessageRequest("test message");

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/chats/" + chatId + "/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void getNewMessageList_returns200_with_testMessage() throws Exception {
        // given
        String chatId = "1234";
        String lastRead = "0";

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/chats/" + chatId + "/messages")
                .param("lastRead", lastRead));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].id").value(1))
                .andExpect(jsonPath("$.messages[0].content").value("this is a message"));

    }
}