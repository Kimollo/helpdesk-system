package com.helpdesk.web;

import com.helpdesk.dto.CommentResponse;
import com.helpdesk.dto.CreateCommentRequest;
import com.helpdesk.security.UserPrincipal;
import com.helpdesk.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentResponse> list(@PathVariable Long ticketId, @AuthenticationPrincipal UserPrincipal actor) {
        return commentService.list(ticketId, actor);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse add(@PathVariable Long ticketId,
                               @Valid @RequestBody CreateCommentRequest request,
                               @AuthenticationPrincipal UserPrincipal actor) {
        return commentService.add(ticketId, request, actor);
    }
}
