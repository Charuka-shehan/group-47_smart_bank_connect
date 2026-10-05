<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartBank Connect | LankaTrust Bank PLC</title>
    <meta name="description" content="SmartBank Connect — a secure, role-based online banking prototype for LankaTrust Bank PLC.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/style.css'/>" rel="stylesheet">
</head>
<body>
<jsp:include page="common/topnav.jsp"/>

<!-- ============ HERO ============ -->
<section class="sb-hero-img" style="background-image: url('<c:url value="/images/home/hero-bg.jpg"/>');" id="home-section">
    <div class="container">
        <div class="row align-items-center">
            <div class="col-lg-7">
                <span class="sb-eyebrow">LankaTrust Bank PLC &bullet; Academic Prototype</span>
                <h1 class="display-5 mb-3">Banking made simple, secure, and instant.</h1>
                <p class="lead mb-4">SmartBank Connect brings account management, fund transfers, transaction
                    history, loans, and real-time alerts together in one platform — protected by role-based access
                    control and OTP-verified login.</p>
                <div class="d-flex flex-wrap gap-2">
                    <a href="<c:url value='/register'/>" class="btn btn-gold btn-lg px-4 me-2">Open an Account</a>
                    <a href="<c:url value='/login'/>" class="btn btn-outline-light btn-lg px-4">Log In</a>
                </div>
                <div class="sb-hero-chips">
                    <span class="sb-hero-chip"><i class="bi bi-shield-check"></i>Role-based access control</span>
                    <span class="sb-hero-chip"><i class="bi bi-envelope-check"></i>Email OTP verification</span>
                    <span class="sb-hero-chip"><i class="bi bi-journal-check"></i>Full audit logging</span>
                </div>
            </div>
            <div class="col-lg-5 d-none d-lg-block text-center">
                <i class="bi bi-bank2" style="font-size: 12rem; color: rgba(255,255,255,0.15);"></i>
            </div>
        </div>
    </div>
</section>

<!-- ============ STATS STRIP ============ -->
<div class="sb-stats-bar">
    <div class="container">
        <div class="row text-center g-3">
            <div class="col-6 col-md-3">
                <div class="stat-value">6</div>
                <div class="stat-label">Core Modules</div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-value">6</div>
                <div class="stat-label">User Roles</div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-value">24/7</div>
                <div class="stat-label">OTP-Secured Login</div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-value">100%</div>
                <div class="stat-label">Audit-Logged Actions</div>
            </div>
        </div>
    </div>
</div>

<!-- ============ HOW IT WORKS ============ -->
<section id="workflow" class="sb-section">
    <div class="container">
        <div class="text-center mb-5">
            <span class="sb-eyebrow">How It Works</span>
            <h2 class="sb-section-title mb-3">The same account-opening flow a real bank follows</h2>
            <p class="sb-section-lead mx-auto">No self-service account creation. Every account is opened by staff,
                approved by a manager, and only then can a customer register for online access.</p>
        </div>
        <div class="row g-4">
            <div class="col-md-6 col-lg-3">
                <div class="sb-step-card">
                    <div class="sb-step-num">1</div>
                    <h5 class="mt-3">Officer Opens Account</h5>
                    <p class="text-muted mb-0 small">A Bank Officer captures KYC details, uploads documents, and
                        submits the request for review.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="sb-step-card">
                    <div class="sb-step-num">2</div>
                    <h5 class="mt-3">Manager Approves</h5>
                    <p class="text-muted mb-0 small">A Bank Manager reviews and approves the request, auto-generating
                        the account number, customer ID, and ATM card.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="sb-step-card">
                    <div class="sb-step-num">3</div>
                    <h5 class="mt-3">Customer Registers</h5>
                    <p class="text-muted mb-0 small">The customer verifies their Customer ID and account number, then
                        sets up an online banking username and password.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="sb-step-card">
                    <div class="sb-step-num">4</div>
                    <h5 class="mt-3">OTP Login &amp; Dashboard</h5>
                    <p class="text-muted mb-0 small">Every login is confirmed with a 6-digit email OTP before landing
                        on the role-specific dashboard.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ============ FEATURES ============ -->
<section id="features" class="sb-section sb-section-alt">
    <div class="container">
        <div class="text-center mb-5">
            <span class="sb-eyebrow">What's Inside</span>
            <h2 class="sb-section-title mb-3">Everything you need, in one place</h2>
            <p class="sb-section-lead">Built module-by-module by Group 47, each covering one core banking capability
                end to end.</p>
        </div>
        <div class="row g-4">
            <div class="col-md-4">
                <div class="sb-card h-100">
                    <i class="bi bi-wallet2 fs-2" style="color: var(--sb-gold);"></i>
                    <h5 class="mt-3">Account Management</h5>
                    <p class="text-muted mb-0">Open, view, and manage your savings, current, or fixed deposit accounts.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="sb-card h-100">
                    <i class="bi bi-arrow-left-right fs-2" style="color: var(--sb-gold);"></i>
                    <h5 class="mt-3">Fund Transfer</h5>
                    <p class="text-muted mb-0">Send money instantly with automatic limit checks and OTP protection.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="sb-card h-100">
                    <i class="bi bi-clock-history fs-2" style="color: var(--sb-gold);"></i>
                    <h5 class="mt-3">Transaction History</h5>
                    <p class="text-muted mb-0">Search, filter, and download statements in seconds.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="sb-card h-100">
                    <i class="bi bi-cash-coin fs-2" style="color: var(--sb-gold);"></i>
                    <h5 class="mt-3">Loan Request &amp; Approval</h5>
                    <p class="text-muted mb-0">Track your loan application end-to-end, from submission to approval.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="sb-card h-100">
                    <i class="bi bi-shield-lock fs-2" style="color: var(--sb-gold);"></i>
                    <h5 class="mt-3">Secure Access</h5>
                    <p class="text-muted mb-0">Role-based access control, OTP verification, and full audit logging.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="sb-card h-100">
                    <i class="bi bi-bell fs-2" style="color: var(--sb-gold);"></i>
                    <h5 class="mt-3">Real-Time Alerts</h5>
                    <p class="text-muted mb-0">Instant email and in-app notifications for every event.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ============ ROLES ============ -->
<section id="roles" class="sb-section sb-section-alt">
    <div class="container">
        <div class="text-center mb-5">
            <span class="sb-eyebrow">Built for Every Role</span>
            <h2 class="sb-section-title mb-3">One platform, six purpose-built dashboards</h2>
            <p class="sb-section-lead mx-auto">Every role sees only what it needs — enforced on both the screen and
                the server.</p>
        </div>
        <div class="row g-4">
            <div class="col-md-6 col-lg-4">
                <div class="sb-role-card">
                    <div class="sb-role-icon"><i class="bi bi-person"></i></div>
                    <h5>Customer</h5>
                    <p class="text-muted mb-0 small">Accounts, transfers, statements, loan tracking, ATM card
                        status, and notifications.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-role-card">
                    <div class="sb-role-icon"><i class="bi bi-briefcase"></i></div>
                    <h5>Bank Officer</h5>
                    <p class="text-muted mb-0 small">Account opening, customer management, loan document
                        verification, and operational reports.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-role-card">
                    <div class="sb-role-icon"><i class="bi bi-person-badge"></i></div>
                    <h5>Bank Manager</h5>
                    <p class="text-muted mb-0 small">Approves every request across the bank and has full oversight
                        of all other dashboards.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-role-card">
                    <div class="sb-role-icon"><i class="bi bi-gear"></i></div>
                    <h5>System Administrator</h5>
                    <p class="text-muted mb-0 small">User &amp; role management, OTP and security settings, session
                        and audit monitoring.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-role-card">
                    <div class="sb-role-icon"><i class="bi bi-search"></i></div>
                    <h5>Compliance Officer</h5>
                    <p class="text-muted mb-0 small">KYC verification, AML review, risk assessment, and suspicious
                        transaction monitoring.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-role-card">
                    <div class="sb-role-icon"><i class="bi bi-headset"></i></div>
                    <h5>Customer Relations Executive</h5>
                    <p class="text-muted mb-0 small">Statement requests, customer enquiries, and communication
                        logs.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ============ ABOUT ============ -->
<section id="about" class="sb-section">
    <div class="container">
        <div class="row align-items-center g-5">
            <div class="col-lg-6">
                <div class="sb-about-img">
                    <img src="<c:url value='/images/home/about.jpg'/>" alt="LankaTrust Bank PLC banking hall" class="img-fluid">
                </div>
            </div>
            <div class="col-lg-6">
                <span class="sb-eyebrow">About This Project</span>
                <h2 class="sb-section-title mb-3">A full-stack banking system, built the way real banks think about it</h2>
                <p class="text-muted">SmartBank Connect is a web-based banking management system prototype developed
                    for LankaTrust Bank PLC as an SE2030 Software Engineering coursework project by Group 47 at the
                    SLIIT Faculty of Computing. It models the systems a retail bank actually relies on — account
                    lifecycles, transfer limits, loan workflows, and layered approvals — not just a UI mockup.</p>
                <ul class="sb-check-list mt-4">
                    <li><i class="bi bi-check-circle-fill"></i> Six individually owned modules, one shared audit trail</li>
                    <li><i class="bi bi-check-circle-fill"></i> Spring Boot, Spring Security, and MySQL under the hood</li>
                    <li><i class="bi bi-check-circle-fill"></i> Designed for demonstration, not for real financial use</li>
                </ul>
            </div>
        </div>
    </div>
</section>

<!-- ============ SERVICES ============ -->
<section id="services" class="sb-section sb-section-alt">
    <div class="container">
        <div class="text-center mb-5">
            <span class="sb-eyebrow">Services</span>
            <h2 class="sb-section-title mb-3">Core banking services, covered end to end</h2>
        </div>
        <div class="row g-4">
            <div class="col-md-6 col-lg-4">
                <div class="sb-service-card">
                    <div class="sb-service-icon"><i class="bi bi-piggy-bank"></i></div>
                    <h5>Savings &amp; Current Accounts</h5>
                    <p class="text-muted mb-0">Open and manage multiple account types with real-time balances.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-service-card">
                    <div class="sb-service-icon"><i class="bi bi-send-check"></i></div>
                    <h5>Instant Fund Transfers</h5>
                    <p class="text-muted mb-0">OTP-verified transfers between accounts with limit enforcement.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-service-card">
                    <div class="sb-service-icon"><i class="bi bi-cash-coin"></i></div>
                    <h5>Loan Origination</h5>
                    <p class="text-muted mb-0">Apply, verify, and approve loans through a staged review workflow.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-service-card">
                    <div class="sb-service-icon"><i class="bi bi-file-earmark-bar-graph"></i></div>
                    <h5>Statements &amp; Reports</h5>
                    <p class="text-muted mb-0">Download transaction history and account activity on demand.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-service-card">
                    <div class="sb-service-icon"><i class="bi bi-bell"></i></div>
                    <h5>Real-Time Notifications</h5>
                    <p class="text-muted mb-0">Stay informed on every deposit, withdrawal, and approval.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="sb-service-card">
                    <div class="sb-service-icon"><i class="bi bi-shield-check"></i></div>
                    <h5>Audit &amp; Compliance</h5>
                    <p class="text-muted mb-0">Every sensitive action is logged for full traceability.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ============ TESTIMONIALS ============ -->
<section id="testimonials" class="sb-section sb-testimonial-wrap">
    <div class="container">
        <div class="text-center mb-5">
            <span class="sb-eyebrow">Feedback</span>
            <h2 class="sb-section-title mb-3">What early testers are saying</h2>
        </div>
        <div id="sbTestimonials" class="carousel slide" data-bs-ride="carousel">
            <div class="carousel-inner">
                <div class="carousel-item active">
                    <div class="sb-testimonial-card">
                        <img src="<c:url value='/images/home/person-1.jpg'/>" alt="Tester avatar">
                        <blockquote class="mb-3">&ldquo;The role-based dashboards make it obvious what each staff
                            member can and can't do. Feels close to how a real bank back-office would work.&rdquo;</blockquote>
                        <div class="fw-semibold">Course Reviewer</div>
                        <div class="text-muted small">SE2030 Peer Evaluation</div>
                    </div>
                </div>
                <div class="carousel-item">
                    <div class="sb-testimonial-card">
                        <img src="<c:url value='/images/home/person-2.jpg'/>" alt="Tester avatar">
                        <blockquote class="mb-3">&ldquo;OTP on every sensitive action plus a full audit log — that's
                            more security thinking than most student projects bother with.&rdquo;</blockquote>
                        <div class="fw-semibold">Faculty Mentor</div>
                        <div class="text-muted small">SLIIT Faculty of Computing</div>
                    </div>
                </div>
                <div class="carousel-item">
                    <div class="sb-testimonial-card">
                        <img src="<c:url value='/images/home/person-3.jpg'/>" alt="Tester avatar">
                        <blockquote class="mb-3">&ldquo;Clean dashboard, clear navigation, and the loan approval flow
                            actually matches the diagrams in the proposal.&rdquo;</blockquote>
                        <div class="fw-semibold">Group 47 Teammate</div>
                        <div class="text-muted small">QA Pass</div>
                    </div>
                </div>
            </div>
            <button class="carousel-control-prev" type="button" data-bs-target="#sbTestimonials" data-bs-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Previous</span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#sbTestimonials" data-bs-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Next</span>
            </button>
        </div>
    </div>
</section>

<!-- ============ FAQ ============ -->
<section id="faq" class="sb-section sb-section-alt">
    <div class="container">
        <div class="text-center mb-5">
            <span class="sb-eyebrow">Questions</span>
            <h2 class="sb-section-title mb-3">Frequently asked questions</h2>
        </div>
        <div class="row justify-content-center">
            <div class="col-lg-9">
                <div class="accordion sb-faq" id="sbFaqAccordion">
                    <div class="accordion-item">
                        <h3 class="accordion-header">
                            <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#faq1">
                                Is SmartBank Connect a real bank?
                            </button>
                        </h3>
                        <div id="faq1" class="accordion-collapse collapse show" data-bs-parent="#sbFaqAccordion">
                            <div class="accordion-body text-muted">No. This is an academic prototype built for the
                                SE2030 Software Engineering module at SLIIT, modelling LankaTrust Bank PLC as a
                                fictional case study. Do not use real financial information here.</div>
                        </div>
                    </div>
                    <div class="accordion-item">
                        <h3 class="accordion-header">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#faq2">
                                How does the OTP login work?
                            </button>
                        </h3>
                        <div id="faq2" class="accordion-collapse collapse" data-bs-parent="#sbFaqAccordion">
                            <div class="accordion-body text-muted">After you sign in with your email and password, a
                                one-time code is emailed to you. You'll need to enter that code before you can reach
                                your dashboard, adding a second layer of verification to every login.</div>
                        </div>
                    </div>
                    <div class="accordion-item">
                        <h3 class="accordion-header">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#faq3">
                                What user roles are supported?
                            </button>
                        </h3>
                        <div id="faq3" class="accordion-collapse collapse" data-bs-parent="#sbFaqAccordion">
                            <div class="accordion-body text-muted">Customer, Branch Officer, Customer Relations
                                Executive, Compliance Officer, System Administrator, and Bank Manager — each with
                                its own dashboard and permitted actions enforced on both frontend and backend.</div>
                        </div>
                    </div>
                    <div class="accordion-item">
                        <h3 class="accordion-header">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#faq4">
                                How do I open an account?
                            </button>
                        </h3>
                        <div id="faq4" class="accordion-collapse collapse" data-bs-parent="#sbFaqAccordion">
                            <div class="accordion-body text-muted">Click "Open an Account" above to register, verify
                                your email via OTP, and log in to request a savings, current, or fixed deposit
                                account from your dashboard.</div>
                        </div>
                    </div>
                    <div class="accordion-item">
                        <h3 class="accordion-header">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#faq5">
                                Is every action tracked?
                            </button>
                        </h3>
                        <div id="faq5" class="accordion-collapse collapse" data-bs-parent="#sbFaqAccordion">
                            <div class="accordion-body text-muted">Yes. Account changes, transfers, loan decisions,
                                and staff actions are written to an audit log that administrators and managers can
                                review at any time.</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ============ CTA ============ -->
<section class="sb-section sb-section-alt pt-0">
    <div class="container">
        <div class="sb-cta-banner text-center text-lg-start">
            <div class="row align-items-center g-4">
                <div class="col-lg-8">
                    <h2 class="mb-2">Ready to see it in action?</h2>
                    <p class="mb-0" style="color: rgba(255,255,255,0.85);">Create an account and explore the full
                        SmartBank Connect experience in a few minutes.</p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <a href="<c:url value='/register'/>" class="btn btn-light btn-lg px-4">Open an Account</a>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ============ FOOTER ============ -->
<footer class="sb-footer" id="contact">
    <div class="container">
        <div class="row g-4 mb-4">
            <div class="col-lg-4">
                <h1 class="h5 mb-3" style="color:#fff;">SmartBank <span style="color:#8BA3CB;">Connect.</span></h1>
                <p class="small mb-0">A web-based banking management system prototype for LankaTrust Bank PLC,
                    built by Group 47 &mdash; SE2030 Software Engineering, SLIIT Faculty of Computing.</p>
            </div>
            <div class="col-6 col-lg-2">
                <div class="sb-footer-heading">Quick Links</div>
                <ul class="sb-footer-links">
                    <li><a href="<c:url value='/#features'/>">Features</a></li>
                    <li><a href="<c:url value='/#workflow'/>">How It Works</a></li>
                    <li><a href="<c:url value='/#roles'/>">Roles</a></li>
                    <li><a href="<c:url value='/#services'/>">Services</a></li>
                    <li><a href="<c:url value='/#about'/>">About</a></li>
                    <li><a href="<c:url value='/#faq'/>">FAQ</a></li>
                </ul>
            </div>
            <div class="col-6 col-lg-3">
                <div class="sb-footer-heading">Account</div>
                <ul class="sb-footer-links">
                    <li><a href="<c:url value='/login'/>">Log In</a></li>
                    <li><a href="<c:url value='/register'/>">Open an Account</a></li>
                </ul>
            </div>
            <div class="col-lg-3">
                <div class="sb-footer-heading">Contact</div>
                <ul class="sb-footer-links">
                    <li><i class="bi bi-envelope me-2"></i>support@smartbankconnect.demo</li>
                    <li><i class="bi bi-geo-alt me-2"></i>SLIIT, Malabe, Sri Lanka</li>
                </ul>
            </div>
        </div>
        <hr style="border-color: rgba(255,255,255,0.1);">
        <div class="text-center small">
            <p class="mb-1">SmartBank Connect &mdash; A Web-Based Banking Management System for LankaTrust Bank PLC</p>
            <p class="mb-0">Group 47 | SE2030 Software Engineering | SLIIT Faculty of Computing &mdash; Academic Prototype</p>
            <p class="mb-0 mt-2" style="opacity:0.6;">Home page layout adapted from the "Banker" template by
                <a href="https://colorlib.com" target="_blank" rel="noopener" style="color:#9fb0c8;">Colorlib</a>, used under its CC BY 3.0 license.</p>
        </div>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
