package com.example.IdentityService.repository;

import com.example.IdentityService.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact,Long> {

    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);

    List<Contact> findByLinkedId(Long linkedId);
}
