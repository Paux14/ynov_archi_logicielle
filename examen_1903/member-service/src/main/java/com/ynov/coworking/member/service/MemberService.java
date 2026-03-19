package com.ynov.coworking.member.service;

import com.ynov.coworking.member.exception.ResourceNotFoundException;
import com.ynov.coworking.member.model.Member;
import com.ynov.coworking.member.repository.MemberRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public MemberService(MemberRepository memberRepository,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.memberRepository = memberRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membre " + id + " introuvable"));
    }

    @Transactional
    public Member create(Member member) {
        member.setId(null);
        // Dérive maxConcurrentBookings depuis subscriptionType
        if (member.getMaxConcurrentBookings() == null) {
            member.setSubscriptionType(member.getSubscriptionType());
        }
        return memberRepository.save(member);
    }

    @Transactional
    public Member update(Long id, Member input) {
        Member existing = findById(id);
        existing.setFullName(input.getFullName());
        existing.setEmail(input.getEmail());
        existing.setSubscriptionType(input.getSubscriptionType());
        existing.setSuspended(input.isSuspended());
        return memberRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Membre " + id + " introuvable");
        }
        // On notifie reservation-service de supprimer toutes ses réservations
        try {
            kafkaTemplate.send("member-deleted", String.valueOf(id));
        } catch (Exception e) {
            log.warn("Kafka indisponible — événement member-deleted non envoyé: {}", e.getMessage());
        }
        memberRepository.deleteById(id);
    }

    @Transactional
    public Member updateSuspension(Long id, boolean suspended) {
        Member member = findById(id);
        member.setSuspended(suspended);
        return memberRepository.save(member);
    }
}
