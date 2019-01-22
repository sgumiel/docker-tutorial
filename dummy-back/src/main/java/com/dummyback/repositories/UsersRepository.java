package com.dummyback.repositories;

import org.springframework.data.repository.CrudRepository;

import com.dummyback.models.User;

public interface UsersRepository extends CrudRepository<User, Integer> {

}