package com.ynov.coworking.member.web;

import com.ynov.coworking.member.model.Member;
import com.ynov.coworking.member.service.MemberService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<Member> list() {
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public Member get(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member create(@RequestBody Member member) {
        return memberService.create(member);
    }

    @PutMapping("/{id}")
    public Member update(@PathVariable Long id, @RequestBody Member member) {
        return memberService.update(id, member);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }
}
