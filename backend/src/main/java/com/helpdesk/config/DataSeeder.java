package com.helpdesk.config;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.Ticket;
import com.helpdesk.domain.TicketCategory;
import com.helpdesk.domain.TicketComment;
import com.helpdesk.domain.TicketPriority;
import com.helpdesk.domain.TicketStatus;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.repository.TicketCommentRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        boolean createdUsers = false;
        createdUsers |= createUser("admin@helpdesk.local", "Admin123!", "Amina", "Okello", Role.ADMIN);
        createdUsers |= createUser("agent@helpdesk.local", "Agent123!", "Joseph", "Mwangi", Role.AGENT);
        createdUsers |= createUser("customer@helpdesk.local", "Customer123!", "Lydia", "Njeri", Role.CUSTOMER);
        if (createdUsers) {
            log.info("Seeded demo users: admin@helpdesk.local / Admin123! | agent@helpdesk.local / Agent123! | customer@helpdesk.local / Customer123!");
        }
        seedSampleTicket();
    }

    private boolean createUser(String email, String password, String firstName, String lastName, Role role) {
        if (userRepository.existsByEmail(email)) {
            return false;
        }
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }

    private void seedSampleTicket() {
        if (ticketRepository.count() > 0) {
            return;
        }
        UserAccount customer = userRepository.findByEmail("customer@helpdesk.local").orElseThrow();
        UserAccount agent = userRepository.findByEmail("agent@helpdesk.local").orElseThrow();

        Ticket ticket = new Ticket();
        ticket.setSubject("Laptop cannot join the office VPN");
        ticket.setDescription("Since this morning the VPN client connects, then drops within a minute. Other sites work. I am on the office Wi-Fi.");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setCategory(TicketCategory.NETWORK);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setRequester(customer);
        ticket.setAssignee(agent);
        ticketRepository.save(ticket);

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(agent);
        comment.setBody("I am checking the VPN profile on your machine and will follow up shortly.");
        comment.setInternalNote(false);
        commentRepository.save(comment);
    }
}
