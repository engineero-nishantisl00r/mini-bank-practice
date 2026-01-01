package com.bank.domain.repository;

import com.bank.domain.entity.Posting;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PostingRepositoryTest {

    @Test
    void findByPostingNumber_returnsOptionalWhenFound() {
        PostingRepository repo = Mockito.spy(new PostingRepository());

        PanacheQuery<Posting> query = Mockito.mock(PanacheQuery.class);
        Posting p = new Posting();
        p.postingNumber = "P-XYZ";

        Mockito.when(query.firstResultOptional()).thenReturn(Optional.of(p));
        Mockito.doReturn(query).when(repo).find(Mockito.eq("postingNumber"), Mockito.eq("P-XYZ"));

        Optional<Posting> res = repo.findByPostingNumber("P-XYZ");
        assertTrue(res.isPresent());
        assertEquals("P-XYZ", res.get().postingNumber);
    }

    @Test
    void findByPostingNumber_returnsEmptyWhenNotFound() {
        PostingRepository repo = Mockito.spy(new PostingRepository());

        PanacheQuery<Posting> query = Mockito.mock(PanacheQuery.class);
        Mockito.when(query.firstResultOptional()).thenReturn(Optional.empty());
        Mockito.doReturn(query).when(repo).find(Mockito.eq("postingNumber"), Mockito.eq("NOPE"));

        Optional<Posting> res = repo.findByPostingNumber("NOPE");
        assertTrue(res.isEmpty());
    }
}
