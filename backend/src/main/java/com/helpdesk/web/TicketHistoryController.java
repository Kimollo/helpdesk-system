package com.helpdesk.web;

import com.helpdesk.dto.TicketHistoryResponse;
import com.helpdesk.security.UserPrincipal;
import com.helpdesk.service.TicketHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@RequiredArgsConstructor
@Tag(name = "Ticket history")
public class TicketHistoryController {

    private final TicketHistoryService historyService;

    @GetMapping
    public List<TicketHistoryResponse> list(@PathVariable Long ticketId,
                                             @AuthenticationPrincipal UserPrincipal actor) {
        return historyService.list(ticketId, actor);
    }
}
