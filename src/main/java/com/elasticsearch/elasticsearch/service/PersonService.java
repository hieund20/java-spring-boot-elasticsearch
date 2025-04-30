package com.elasticsearch.elasticsearch.service;

import com.elasticsearch.elasticsearch.document.Person;
import com.elasticsearch.elasticsearch.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    public void save(final Person person) {
        personRepository.save(person);
    }

    public Person findById(final String id) {
        return personRepository.findById(id).orElse(null);
    }
}
