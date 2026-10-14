package com.myopty.shared.question.controller;

import com.myopty.shared.question.dto.AnswerQuestionRequest;
import com.myopty.shared.question.dto.ApiResponse;
import com.myopty.shared.question.dto.QuestionResponse;
import com.myopty.shared.question.dto.ShopQuestionResponse;
import com.myopty.shared.question.service.QuestionService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop's side of the Q&amp;A: the queue of unanswered questions and the answer
 * that clears one.
 *
 * <p>Access is decided in {@link com.myopty.shared.auth.SecurityConfig}, not here:
 * every {@code /api/shop/**} route needs {@code ROLE_CLIENT}, so a customer or an
 * anonymous caller is refused before this class runs. The answering client is
 * taken from that session principal, never from the request, so one client cannot
 * post an answer under another's name.
 */
@RestController
@RequestMapping("/api/shop/questions")
public class ShopQuestionController {

    private final QuestionService questions;

    public ShopQuestionController(QuestionService questions) {
        this.questions = questions;
    }

    @GetMapping
    public ApiResponse<List<ShopQuestionResponse>> unanswered() {
        return ApiResponse.of(questions.listUnanswered());
    }

    @PutMapping("/{id}/answer")
    public ApiResponse<QuestionResponse> answer(
            @PathVariable long id,
            @Valid @RequestBody AnswerQuestionRequest request,
            @AuthenticationPrincipal AppUser client) {
        return ApiResponse.of(questions.answer(id, client.getId(), request));
    }
}
