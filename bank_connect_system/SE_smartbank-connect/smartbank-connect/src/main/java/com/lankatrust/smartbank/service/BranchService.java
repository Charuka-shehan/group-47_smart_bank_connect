package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Branch;

import java.util.List;

public interface BranchService {
    List<Branch> getAll();
    Branch getById(Integer id);
    Branch create(String code, String name, String address);
    Branch update(Integer id, String name, String address);
    void delete(Integer id);
}
