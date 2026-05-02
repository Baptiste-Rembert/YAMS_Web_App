package com.yams.service;

import com.yams.model.Invitation;
import com.yams.model.GameEvent;
import com.yams.repository.InvitationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public InvitationService(InvitationRepository invitationRepository, SimpMessagingTemplate messagingTemplate) {
        this.invitationRepository = invitationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Invitation createInvitation(String fromUsername, String toUsername, Long gameId) {
        Invitation inv = new Invitation();
        inv.setFromUsername(fromUsername);
        inv.setToUsername(toUsername);
        inv.setGameId(gameId);
        invitationRepository.save(inv);

        // Broadcast an invite event on a general topic; clients will filter by 'to'
        Map<String, Object> data = new HashMap<>();
        data.put("to", toUsername);
        data.put("from", fromUsername);
        data.put("gameId", gameId);

        GameEvent evt = new GameEvent("PLAYER_INVITED", gameId, data);
        try {
            messagingTemplate.convertAndSend("/topic/invites", evt);
        } catch (Exception e) {
            // ignore messaging failures — invite is persisted
        }

        return inv;
    }
}
