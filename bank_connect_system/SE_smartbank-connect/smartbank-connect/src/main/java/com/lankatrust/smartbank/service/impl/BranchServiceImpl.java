package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.Branch;
import com.lankatrust.smartbank.repository.BranchRepository;
import com.lankatrust.smartbank.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final com.lankatrust.smartbank.repository.AccountRepository accountRepository;

    @Override
    public List<Branch> getAll() {
        return branchRepository.findAll();
    }

    @Override
    public Branch getById(Integer id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + id));
    }

    @Override
    public Branch create(String code, String name, String address) {
        if (branchRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("A branch with this code already exists.");
        }
        return branchRepository.save(Branch.builder()
                .code(code)
                .name(name)
                .address(address)
                .build());
    }

    @Override
    public Branch update(Integer id, String name, String address) {
        Branch branch = getById(id);
        branch.setName(name);
        branch.setAddress(address);
        return branchRepository.save(branch);
    }

    @Override
    public void delete(Integer id) {
        Branch branch = getById(id);
        boolean hasAccounts = accountRepository.findAll().stream()
                .anyMatch(a -> a.getBranch() != null && id.equals(a.getBranch().getId()));
        if (hasAccounts) {
            throw new IllegalStateException("Cannot delete branch with active associated accounts. Reassign or close accounts first.");
        }
        branchRepository.delete(branch);
    }
}
