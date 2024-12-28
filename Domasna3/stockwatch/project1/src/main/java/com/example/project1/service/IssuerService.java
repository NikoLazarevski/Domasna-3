package com.example.project1.service;

import com.example.project1.entity.Issuer;
import com.example.project1.repository.IssuerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssuerService {

    private final IssuerRepository issuerRepository;

    public List<Issuer> findAll() {
        return issuerRepository.findAllByOrderByIdAsc();
    }

    public Issuer findById(Long id) throws Exception {
        return issuerRepository.findById(id).orElseThrow(Exception::new);
    }

}
