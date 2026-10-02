package com.gringosexy.service;

import com.gringosexy.enums.TicketStatus;
import com.gringosexy.model.SupportTicket;
import com.gringosexy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupportService {

    SupportTicket createTicket(User user, String subject, String message, String ipAddress);

    List<SupportTicket> getUserTickets(String userId);

    SupportTicket getTicketById(String id);

    Page<SupportTicket> getTicketsByStatus(TicketStatus status, Pageable pageable);

    SupportTicket replyAndChangeStatus(String id, String response, TicketStatus status, String adminId, String ipAddress);
}
