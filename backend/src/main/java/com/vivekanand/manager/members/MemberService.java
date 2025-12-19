package com.vivekanand.manager.members;

import com.vivekanand.manager.members.dto.CreateMemberRequest;
import com.vivekanand.manager.members.dto.UpdateMemberRequest;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository repo;
    public MemberService(MemberRepository repo){ this.repo=repo; }

    public Member create(CreateMemberRequest req){
        Member m = new Member();
        m.setFullName(req.fullName());
        m.setEmail(req.email());
        m.setPhone(req.phone());
        m.setAddress(req.address());
        m.setNotifyEmail(Boolean.TRUE.equals(req.notifyEmail()));
        m.setNotifyWhatsapp(Boolean.TRUE.equals(req.notifyWhatsapp()));
        return repo.save(m);
    }

    public Member update(Long id, UpdateMemberRequest req){
        Member m = repo.findById(id).orElseThrow();
        m.setFullName(req.fullName());
        m.setEmail(req.email());
        m.setPhone(req.phone());
        m.setAddress(req.address());
        if (req.active() != null) m.setActive(req.active());
        if (req.notifyEmail() != null) m.setNotifyEmail(req.notifyEmail());
        if (req.notifyWhatsapp() != null) m.setNotifyWhatsapp(req.notifyWhatsapp());
        return repo.save(m);
    }

    public void delete(Long id){ repo.deleteById(id); }
    public Member get(Long id){ return repo.findById(id).orElseThrow(); }

    public Page<Member> page(String q, boolean onlyActive, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        if (q != null && !q.isBlank()) {
            return repo.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
        }
        return onlyActive ? repo.findByActiveTrue(pageable) : repo.findAll(pageable);
    }
}
