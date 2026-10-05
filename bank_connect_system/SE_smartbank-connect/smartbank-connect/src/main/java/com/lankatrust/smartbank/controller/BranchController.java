package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/branches")
@PreAuthorize("hasRole('BANK_MANAGER')")
public class BranchController {

    private final BranchService branchService;
    private final AuditLogService auditLogService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", branchService.getAll());
        return "admin/branches";
    }

    @PostMapping
    public String create(@RequestParam String code, @RequestParam String name, @RequestParam String address,
                         Authentication authentication, RedirectAttributes ra) {
        try {
            branchService.create(code, name, address);
            auditLogService.log(authentication.getName(), "BRANCH_CREATED", "Branch", null, code);
            ra.addFlashAttribute("success", "Branch created.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/branches";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam String name, @RequestParam String address,
                         Authentication authentication, RedirectAttributes ra) {
        try {
            branchService.update(id, name, address);
            auditLogService.log(authentication.getName(), "BRANCH_UPDATED", "Branch", id.longValue(), name);
            ra.addFlashAttribute("success", "Branch updated.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/branches";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, Authentication authentication, RedirectAttributes ra) {
        try {
            branchService.delete(id);
            auditLogService.log(authentication.getName(), "BRANCH_DELETED", "Branch", id.longValue(), "Branch deleted #" + id);
            ra.addFlashAttribute("success", "Branch deleted successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/branches";
    }
}
