package com.gringosexy.service.impl;

import com.gringosexy.enums.TicketStatus;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.model.SupportTicket;
import com.gringosexy.model.User;
import com.gringosexy.repository.SupportTicketRepository;
import com.gringosexy.service.ActivityLogService;
import com.gringosexy.service.SupportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository ticketRepository;
    private final ActivityLogService activityLogService;

    public SupportServiceImpl(SupportTicketRepository ticketRepository, ActivityLogService activityLogService) {
        this.ticketRepository = ticketRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public SupportTicket createTicket(User user, String subject, String message, String ipAddress) {
        SupportTicket ticket = new SupportTicket(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                subject.trim(),
                message.trim()
        );
        SupportTicket saved = ticketRepository.save(ticket);
        activityLogService.log(user.getId(), user.getUsername(), "TICKET_CREATED", "Ticket creado: " + subject, ipAddress);
        return saved;
    }

    @Override
    public List<SupportTicket> getUserTickets(String userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public SupportTicket getTicketById(String id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket de soporte no encontrado con ID: " + id));
    }

    @Override
    public Page<SupportTicket> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        if (status != null) {
            return ticketRepository.findByStatus(status, pageable);
        }
        return ticketRepository.findAll(pageable);
    }

    @Override
    public SupportTicket replyAndChangeStatus(String id, String response, TicketStatus status, String adminId, String ipAddress) {
        SupportTicket ticket = getTicketById(id);
        if (response != null && !response.trim().isEmpty()) {
            ticket.setAdminResponse(response.trim());
        }
        if (status != null) {
            ticket.setStatus(status);
        }
        ticket.setUpdatedAt(Instant.now());
        SupportTicket updated = ticketRepository.save(ticket);
        activityLogService.log(adminId, "Admin", "TICKET_UPDATED", "Ticket " + ticket.getId() + " actualizado a " + status, ipAddress);
        return updated;
    }
}
