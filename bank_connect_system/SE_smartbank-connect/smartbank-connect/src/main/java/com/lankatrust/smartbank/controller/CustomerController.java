package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Customer;
import com.lankatrust.smartbank.entity.CustomerDocument;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.CustomerDocumentRepository;
import com.lankatrust.smartbank.service.FileStorageService;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final CustomerDocumentRepository customerDocumentRepository;
    private final NotificationService notificationService;
    private final com.lankatrust.smartbank.service.ValidationService validationService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Authentication authentication, Model model) {
        User user = userService.getByEmail(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("customer", user);
        boolean isCust = (user instanceof Customer);
        model.addAttribute("isCustomer", isCust);
        if (isCust) {
            Customer c = (Customer) user;
            model.addAttribute("dob", c.getDob());
            model.addAttribute("address", c.getAddress());
            model.addAttribute("profilePhotoPath", c.getProfilePhotoPath());
            model.addAttribute("customerId", c.getCustomerId());
        } else {
            model.addAttribute("dob", null);
            model.addAttribute("address", "");
            model.addAttribute("profilePhotoPath", null);
            model.addAttribute("customerId", user.getEmail());
        }
        return "customer/profile";
    }

    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
                                @RequestParam(required = false) MultipartFile profilePhoto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            if (fullName == null || fullName.trim().length() < 2) {
                throw new IllegalArgumentException("Full name must be at least 2 characters.");
            }
            validationService.validatePhoneNumber(phone);

            User user = userService.getByEmail(authentication.getName());
            if (user instanceof Customer) {
                if (address == null || address.trim().isBlank()) {
                    throw new IllegalArgumentException("Address is required for customer accounts.");
                }
                if (dob != null) {
                    validationService.validateDateNotFuture(dob, "Date of birth");
                    validationService.validateAge(dob, 18);
                } else {
                    throw new IllegalArgumentException("Date of birth is required for customer accounts.");
                }
            }
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                validationService.validateUploadedFile(profilePhoto, "photo");
            }

            String photoPath = null;
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                photoPath = fileStorageService.store(profilePhoto, "profile-photos");
            }
            userService.updateProfile(user.getId(), fullName, phone, address, dob, photoPath);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @GetMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public String changePasswordForm() {
        return "customer/change-password";
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            validationService.validatePassword(newPassword);
            validationService.validatePasswordMatch(newPassword, confirmPassword);
            User user = userService.getByEmail(authentication.getName());
            userService.changePassword(user.getId(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/change-password";
    }

    @GetMapping("/documents")
    @PreAuthorize("hasAnyRole('CUSTOMER','BANK_MANAGER')")
    public String documents(Authentication authentication, Model model) {
        User user = userService.getByEmail(authentication.getName());
        model.addAttribute("documents", customerDocumentRepository.findByCustomerId(user.getId()));
        return "customer/documents";
    }

    @PostMapping("/documents")
    @PreAuthorize("hasAnyRole('CUSTOMER','BANK_MANAGER')")
    public String uploadDocument(@RequestParam String documentType,
                                 @RequestParam MultipartFile document,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (document == null || document.isEmpty()) {
                throw new IllegalArgumentException("Please select a document file to upload.");
            }
            validationService.validateUploadedFile(document, "document");

            User user = userService.getByEmail(authentication.getName());
            if (!(user instanceof Customer customer)) {
                throw new IllegalArgumentException("Only customers can upload documents.");
            }
            String path = fileStorageService.store(document, "customer-documents");
            CustomerDocument doc = CustomerDocument.builder()
                    .customer(customer)
                    .documentType(documentType)
                    .fileName(document.getOriginalFilename())
                    .filePath(path)
                    .build();
            customerDocumentRepository.save(doc);
            notificationService.send(user, "Document Uploaded",
                    "Your " + documentType + " document has been uploaded successfully and is pending verification.",
                    com.lankatrust.smartbank.entity.NotificationChannel.IN_APP);
            redirectAttributes.addFlashAttribute("success", "Document uploaded successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/documents";
    }

    @PostMapping("/documents/{id}/delete")
    @PreAuthorize("hasAnyRole('CUSTOMER','BANK_MANAGER')")
    public String deleteDocument(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByEmail(authentication.getName());
            CustomerDocument doc = customerDocumentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found."));
            if (user.getRole() != com.lankatrust.smartbank.entity.Role.BANK_MANAGER && !doc.getCustomer().getId().equals(user.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to delete this document.");
            }
            customerDocumentRepository.delete(doc);
            redirectAttributes.addFlashAttribute("success", "Document deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/documents";
    }
}
