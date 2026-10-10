package com.helpdesk.web;

import com.helpdesk.domain.TicketCategory;
import com.helpdesk.domain.TicketPriority;
import com.helpdesk.domain.TicketStatus;
import com.helpdesk.dto.CreateTicketRequest;
import com.helpdesk.dto.PageResponse;
import com.helpdesk.dto.TicketResponse;
import com.helpdesk.dto.TicketStatsResponse;
import com.helpdesk.dto.UpdateTicketRequest;
import com.helpdesk.security.UserPrincipal;
import com.helpdesk.service.TicketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request,
                                                  @AuthenticationPrincipal UserPrincipal actor) {
        TicketResponse body = ticketService.create(request, actor);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.id())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String queue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal actor
    ) {
        return ticketService.list(status, priority, category, q, queue, page, size, actor);
    }

    @GetMapping("/stats")
    public TicketStatsResponse stats(@AuthenticationPrincipal UserPrincipal actor) {
        return ticketService.stats(actor);
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor) {
        return ticketService.get(id, actor);
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable Long id,
                                 @Valid @RequestBody UpdateTicketRequest request,
                                 @AuthenticationPrincipal UserPrincipal actor) {
        return ticketService.update(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor) {
        ticketService.delete(id, actor);
    }
}
