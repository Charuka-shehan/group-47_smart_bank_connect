package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.controller.ReportController;
import com.lankatrust.smartbank.entity.ApprovalRequest;
import com.lankatrust.smartbank.entity.PublishedReport;
import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.ApprovalRequestRepository;
import com.lankatrust.smartbank.repository.PublishedReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportDraftCrudTest {

    @Mock private PublishedReportRepository publishedReportRepository;
    @Mock private ApprovalRequestRepository approvalRequestRepository;
    @Mock private ApprovalService approvalService;
    @Mock private UserService userService;
    @Mock private PdfReportService pdfReportService;
    @Mock private AccountService accountService;
    @Mock private TransactionService transactionService;
    @Mock private Authentication authentication;

    @InjectMocks
    private ReportController reportController;

    private User manager;
    private PublishedReport draftReport;

    @BeforeEach
    void setUp() {
        manager = User.builder().id(1L).email("manager@lankatrust.lk").role(Role.BANK_MANAGER).fullName("Manager One").build();
        draftReport = PublishedReport.builder()
                .id(10L)
                .name("Monthly Compliance Summary")
                .type("MONTHLY_COMPLIANCE")
                .status("DRAFT")
                .params(null)
                .generatedBy(manager)
                .build();
    }

    @Test
    void manageReportsViewRendersDraftsAndReportsPage() {
        Model model = new ExtendedModelMap();
        when(publishedReportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(draftReport));

        String viewName = reportController.manageReports(model);

        assertEquals("officer/reports-manage", viewName);
        assertTrue(model.containsAttribute("reports"));
        assertEquals(List.of(draftReport), model.asMap().get("reports"));
    }

    @Test
    void createDraftWithEmptyParamsSanitizesToNullAndSaves() {
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(authentication.getName()).thenReturn("manager@lankatrust.lk");
        when(userService.getByEmail("manager@lankatrust.lk")).thenReturn(manager);
        when(publishedReportRepository.save(any(PublishedReport.class))).thenAnswer(i -> i.getArgument(0));

        // Submit form with empty string in params
        String view = reportController.createDraft("Draft A", "COMPLIANCE", "   ", authentication, ra);

        assertEquals("redirect:/reports/draft", view);
        ArgumentCaptor<PublishedReport> captor = ArgumentCaptor.forClass(PublishedReport.class);
        verify(publishedReportRepository).save(captor.capture());
        PublishedReport saved = captor.getValue();
        assertEquals("Draft A", saved.getName());
        assertEquals("COMPLIANCE", saved.getType());
        assertEquals("DRAFT", saved.getStatus());
        assertNull(saved.getParams(), "Empty/blank params must be sanitized to null to prevent DB JSON/TEXT constraint violations");
    }

    @Test
    void updateDraftUpdatesNameAndParams() {
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(publishedReportRepository.findById(10L)).thenReturn(Optional.of(draftReport));
        when(publishedReportRepository.save(any(PublishedReport.class))).thenAnswer(i -> i.getArgument(0));

        String view = reportController.updateDraft(10L, "Updated Name", "COMPLIANCE", "{\"period\":\"2026-Q1\"}", ra);

        assertEquals("redirect:/reports/draft", view);
        assertEquals("Updated Name", draftReport.getName());
        assertEquals("{\"period\":\"2026-Q1\"}", draftReport.getParams());
        verify(publishedReportRepository).save(draftReport);
    }

    @Test
    void publishDraftChangesStatusToPublishedWithoutDuplicate() {
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(authentication.getName()).thenReturn("manager@lankatrust.lk");
        when(userService.getByEmail("manager@lankatrust.lk")).thenReturn(manager);
        when(publishedReportRepository.findById(10L)).thenReturn(Optional.of(draftReport));
        when(publishedReportRepository.save(any(PublishedReport.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalRequest req = ApprovalRequest.builder().id(99L).targetId(10L).requestType("REPORT_PUBLISH").status("PENDING").build();
        when(approvalRequestRepository.findByTargetId(10L)).thenReturn(List.of(req));

        String view = reportController.publishDraft(10L, authentication, ra);

        assertEquals("redirect:/reports/draft", view);
        assertEquals("PUBLISHED", draftReport.getStatus());
        verify(approvalRequestRepository).save(req);
        assertEquals("APPROVED", req.getStatus());
        verify(publishedReportRepository).save(draftReport);
    }

    @Test
    void deleteDraftRemovesFromDatabaseAndCleansApprovals() {
        RedirectAttributes ra = new RedirectAttributesModelMap();
        when(publishedReportRepository.findById(10L)).thenReturn(Optional.of(draftReport));

        ApprovalRequest req = ApprovalRequest.builder().id(88L).targetId(10L).requestType("REPORT_PUBLISH").build();
        when(approvalRequestRepository.findByTargetId(10L)).thenReturn(List.of(req));

        String view = reportController.deleteDraft(10L, authentication, ra);

        assertEquals("redirect:/reports/draft", view);
        verify(approvalRequestRepository).deleteAll(List.of(req));
        verify(publishedReportRepository).delete(draftReport);
    }

    @Test
    void deleteDraftApiReturnsSuccessJson() {
        when(publishedReportRepository.findById(10L)).thenReturn(Optional.of(draftReport));
        when(approvalRequestRepository.findByTargetId(10L)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = reportController.deleteDraftApi(10L);

        assertEquals(200, response.getStatusCode().value());
        verify(publishedReportRepository).delete(draftReport);
    }
}
