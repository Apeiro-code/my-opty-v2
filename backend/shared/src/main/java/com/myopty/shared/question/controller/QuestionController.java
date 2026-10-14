package com.myopty.shared.question.controller;

import com.myopty.shared.question.dto.ApiResponse;
import com.myopty.shared.question.dto.CreateQuestionRequest;
import com.myopty.shared.question.dto.FaqResponse;
import com.myopty.shared.question.dto.QuestionResponse;
import com.myopty.shared.question.service.QuestionService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The customer Q&amp;A endpoints.
 *
 * <p>Access is decided in {@link com.myopty.shared.auth.SecurityConfig}, not here:
 * the FAQ read is public, while asking and reading your own questions require
 * {@code ROLE_CUSTOMER}. The principal is therefore only ever non-null on the two
 * customer routes — the filter chain answers an anonymous caller first.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questions;

    public QuestionController(QuestionService questions) {
        this.questions = questions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuestionResponse> ask(
            @Valid @RequestBody CreateQuestionRequest request, @AuthenticationPrincipal AppUser user) {
        return ApiResponse.of(questions.ask(user.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<QuestionResponse>> mine(@AuthenticationPrincipal AppUser user) {
        return ApiResponse.of(questions.listForCustomer(user.getId()));
    }

    @GetMapping("/faq")
    public ApiResponse<List<FaqResponse>> faq() {
        return ApiResponse.of(questions.listFaq());
    }
}
