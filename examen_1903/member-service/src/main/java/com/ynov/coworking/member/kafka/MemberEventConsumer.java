package com.ynov.coworking.member.kafka;

import com.ynov.coworking.member.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MemberEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MemberEventConsumer.class);

    private final MemberRepository memberRepository;

    public MemberEventConsumer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /** Quand une réservation est créée et atteint le quota → suspension du membre */
    @KafkaListener(topics = "member-suspend", groupId = "member-service-group")
    @Transactional
    public void handleSuspend(String memberId) {
        try {
            Long id = Long.parseLong(memberId.trim());
            memberRepository.findById(id).ifPresent(m -> {
                m.setSuspended(true);
                memberRepository.save(m);
                log.info("Membre {} suspendu", id);
            });
        } catch (NumberFormatException e) {
            log.warn("memberId invalide reçu sur member-suspend: {}", memberId);
        }
    }

    /** Quand une réservation est annulée/complétée et repasse sous quota → désuspension */
    @KafkaListener(topics = "member-unsuspend", groupId = "member-service-group")
    @Transactional
    public void handleUnsuspend(String memberId) {
        try {
            Long id = Long.parseLong(memberId.trim());
            memberRepository.findById(id).ifPresent(m -> {
                m.setSuspended(false);
                memberRepository.save(m);
                log.info("Membre {} désuspendu", id);
            });
        } catch (NumberFormatException e) {
            log.warn("memberId invalide reçu sur member-unsuspend: {}", memberId);
        }
    }

    /** Quand une salle est supprimée, les réservations sont annulées → ré-évaluer la suspension */
    @KafkaListener(topics = "reservation-cancelled-for-member", groupId = "member-service-group")
    @Transactional
    public void handleReservationCancelled(String memberId) {
        handleUnsuspend(memberId);
    }
}
