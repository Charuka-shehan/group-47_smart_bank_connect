package com.lankatrust.smartbank.entity;

/**
 * System roles used for role-based access control (RBAC), as defined
 * in Section 3 (Functional Requirements) and Section 6.5 (Authentication
 * & Access Control) of the SmartBank Connect proposal.
 */
public enum Role {
    CUSTOMER,
    BANK_MANAGER,
    BANK_OFFICER,
    CUSTOMER_RELATIONS_EXECUTIVE,
    SYSTEM_ADMINISTRATOR,
    COMPLIANCE_OFFICER
}
