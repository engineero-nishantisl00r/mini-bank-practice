package com.bank.domain.repository;

import com.bank.domain.entity.Posting;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class PostingRepository implements PanacheRepository<Posting> {

    public Optional<Posting> findByPostingNumber(String postingNumber) {
        return find("postingNumber", postingNumber).firstResultOptional();
    }
}
