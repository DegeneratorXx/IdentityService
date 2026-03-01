package com.example.IdentityService.service;

import com.example.IdentityService.dto.ContactResponse;
import com.example.IdentityService.dto.IdentifyRequest;
import com.example.IdentityService.dto.IdentifyResponse;
import com.example.IdentityService.entity.Contact;
import com.example.IdentityService.enums.LinkPrecedence;
import com.example.IdentityService.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final ContactRepository contactRepository;

    @Transactional
    public IdentifyResponse identify(IdentifyRequest request) {

        String email = request.getEmail();
        String phone = request.getPhoneNumber();

        //Find matching contacts
        List<Contact> matchedContacts =
                contactRepository.findByEmailOrPhoneNumber(email, phone);

        // ===============================
        // CASE 1: No contact exists
        // ===============================
        if (matchedContacts.isEmpty()) {

            Contact newPrimary = Contact.builder()
                    .email(email)
                    .phoneNumber(phone)
                    .linkPrecedence(LinkPrecedence.PRIMARY)
                    .linkedId(null)
                    .build();

            Contact saved = contactRepository.save(newPrimary);

            return buildResponse(saved, List.of(saved));
        }

        // ===============================
        // CASE 2: Contacts exist
        // ===============================

        // Collect all related contacts
        Set<Contact> allContacts = new HashSet<>(matchedContacts);

        for (Contact contact : matchedContacts) {

            if (contact.getLinkedId() != null) {
                allContacts.addAll(
                        contactRepository.findByLinkedId(contact.getLinkedId())
                );
            }

            if (contact.getLinkPrecedence() == LinkPrecedence.PRIMARY) {
                allContacts.addAll(
                        contactRepository.findByLinkedId(contact.getId())
                );
            }
        }

        // Find oldest PRIMARY contact
        Contact primary = allContacts.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();

        // Convert other primaries → secondary
        for (Contact contact : allContacts) {

            if (!contact.getId().equals(primary.getId())
                    && contact.getLinkPrecedence() == LinkPrecedence.PRIMARY) {

                contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                contact.setLinkedId(primary.getId());
                contactRepository.save(contact);
            }
        }

        // Check if new info introduced
        boolean emailExists = allContacts.stream()
                .anyMatch(c -> Objects.equals(c.getEmail(), email));

        boolean phoneExists = allContacts.stream()
                .anyMatch(c -> Objects.equals(c.getPhoneNumber(), phone));

        if ((!emailExists && email != null) ||
                (!phoneExists && phone != null)) {

            Contact secondary = Contact.builder()
                    .email(email)
                    .phoneNumber(phone)
                    .linkedId(primary.getId())
                    .linkPrecedence(LinkPrecedence.SECONDARY)
                    .build();

            contactRepository.save(secondary);
            allContacts.add(secondary);
        }

        // Refresh list from DB (ensures latest state)
        List<Contact> finalContacts = new ArrayList<>();
        finalContacts.add(primary);
        finalContacts.addAll(
                contactRepository.findByLinkedId(primary.getId())
        );

        return buildResponse(primary, finalContacts);
    }

    // =====================================
    // RESPONSE BUILDER
    // =====================================

    private IdentifyResponse buildResponse(Contact primary,
                                           List<Contact> contacts) {

        List<String> emails = contacts.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> phones = contacts.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> secondaryIds = contacts.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.SECONDARY)
                .map(Contact::getId)
                .collect(Collectors.toList());

        ContactResponse contactResponse = ContactResponse.builder()
                .primaryContactId(primary.getId())
                .emails(emails)
                .phoneNumbers(phones)
                .secondaryContactIds(secondaryIds)
                .build();

        return IdentifyResponse.builder()
                .contact(contactResponse)
                .build();
    }
}