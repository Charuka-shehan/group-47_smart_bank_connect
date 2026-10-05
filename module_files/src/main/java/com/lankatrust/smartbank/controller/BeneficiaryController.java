package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.BeneficiaryService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/beneficiaries")
@PreAuthorize("hasAnyRole('CUSTOMER','BANK_MANAGER')")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final UserService userService;

    @GetMapping
    public String list(Authentication authentication, Model model) {
        User user = userService.getByEmail(authentication.getName());
        model.addAttribute("beneficiaries", beneficiaryService.getForUser(user.getId()));
        return "customer/beneficiaries";
    }

    @PostMapping
    public String add(@RequestParam String beneficiaryName,
                      @RequestParam String accountNumber,
                      @RequestParam(required = false) String bank,
                      @RequestParam(required = false) String nickname,
                      Authentication authentication,
                      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByEmail(authentication.getName());
            beneficiaryService.addBeneficiary(user.getId(), beneficiaryName, accountNumber,
                    bank != null && !bank.isBlank() ? bank : "LankaTrust Bank",
                    nickname);
            redirectAttributes.addFlashAttribute("success", "Beneficiary added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/beneficiaries";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String nickname,
                         @RequestParam(required = false) String bank,
                         Authentication authentication,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByEmail(authentication.getName());
            beneficiaryService.updateBeneficiary(id, user.getId(), nickname, bank);
            redirectAttributes.addFlashAttribute("success", "Beneficiary updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/beneficiaries";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication authentication,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByEmail(authentication.getName());
            beneficiaryService.deleteBeneficiary(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Beneficiary removed successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/beneficiaries";
    }
}
