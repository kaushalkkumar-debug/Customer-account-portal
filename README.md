# Customer Account Management Portal

[![Build](https://github.com/kaushalkkumar-debug/customer-account-portal/actions/workflows/build.yml/badge.svg)](https://github.com/kaushalkkumar-debug/customer-account-portal/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A J2EE web application for managing customer accounts, profiles, and
transaction history, with role-based secure login. Presentation layer in
**Struts 1.x** (JSP/JSTL), business layer as **EJB 3.x session beans**,
persistence via plain JDBC.

## Layers

| Layer | Technology | Where |
|---|---|---|
| Presentation | Struts 1.x `Action`/`ActionForm`, JSP/JSTL | `struts/`, `webapp/*.jsp` |
| Business | EJB 3.x `@Stateless` session beans | `ejb/` |
| Persistence | Plain JDBC, hand-written DAOs | `dao/`, `db/` |
| Security | Salted SHA-256 password hashing | `security/` |

`LoginAction` (Struts) validates a `LoginForm`, calls into
`AccountManagementBean` (EJB) to authenticate, and forwards to `adminHome`
or `customerHome` based on the account's role — the same request-goes-through-
every-layer shape as the original J2EE intranet app this is modelled on.

## Pages

- **`/login`** — role-based login (Struts `LoginForm`/`LoginAction`)
- **`/dashboard`** *(customer)* — real profile, running balance, full
  transaction history, an "update contact details" form, and a "record a
  transaction" form that posts through `TransactionServiceBean` and reloads
  with the new balance
- **`/admin`** *(admin)* — every customer account with a one-click
  deactivate/reactivate toggle per row, backed by
  `AccountManagementBean.setAccountActive()`
- **`/logout`** — invalidates the session

Every one of these is wired end to end and was exercised live against a
running Jetty instance with seeded accounts — not just unit-tested. The
dashboard's transaction form was used to record a real debit and the
balance/history updated correctly; the admin panel's deactivate button
was used to lock an account out (confirmed the login then actually
failed), then reactivate it.

## Domain

- **`CustomerAccount`** — username, password hash + salt, role
  (`CUSTOMER`/`ADMIN`), active flag
- **`CustomerProfile`** — display name, email, phone, address, one-to-one
  with an account
- **`Transaction`** — a signed amount + description against an account;
  balance is derived (`SUM(amount)`), never stored, so it can't drift out
  of sync with the ledger

## About the EJB layer

`AccountManagementBean` and `TransactionServiceBean` are real
`@Stateless`/`@Local`-annotated session beans — not plain POJOs pretending
to be beans. They're tested via direct instantiation (`new
AccountManagementBean()`) rather than inside a live EJB container.

That's a deliberate scoping choice, not an oversight: the era-appropriate
container for EJB 3.x on this stack (OpenEJB/TomEE, roughly Java EE 6) predates
Java 17 by a long way, and getting it to boot reliably here would be fighting
the sandbox rather than testing the module. Direct instantiation is a
legitimate, common way to unit-test session bean business logic in
isolation — each bean method opens and closes its own JDBC connection rather
than relying on container-managed transactions/injection, so nothing about
the test depends on being inside a container. What it doesn't cover:
container-managed transaction rollback, `@EJB` injection, and interceptors —
those would need a real container or an embeddable one, and are called out
below under Roadmap.

## About the database layer

Same substitution as my other J2EE-era projects (see Reporting & Dashboard
Module, Performance Analyzer): plain ANSI JDBC, no Oracle-specific syntax,
running against H2 in-memory here so the whole thing compiles, runs, and is
tested with nothing but `mvn test`. `ACCOUNTS_DB_URL`/`ACCOUNTS_DB_DRIVER`/
`ACCOUNTS_DB_USER`/`ACCOUNTS_DB_PASSWORD` env vars swap it back to a real
Oracle instance with no code change.

## Password hashing

`PasswordHasher` does salted SHA-256 (per-account random salt, constant-time
comparison on verify). That's a correct pattern, but SHA-256 alone is **not**
production-grade for password storage — it's fast, which is exactly the
wrong property for something an attacker might try to brute-force offline.
A real deployment should use bcrypt, scrypt, or Argon2 instead. Kept as
SHA-256 here to stay dependency-light and focused on the account/EJB/Struts
plumbing this project is actually about.

## Tests

```bash
mvn test
```

Real output from this repo — 20 tests, all passing:

```
Running com.example.accounts.ejb.AccountManagementBeanTest
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.469 s

Running com.example.accounts.ejb.TransactionServiceBeanTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.106 s

Running com.example.accounts.security.PasswordHasherTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s

Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

`AccountManagementBeanTest` covers registration, duplicate-username
rejection, authentication success/failure, role checks, deactivation,
profile read/update, listing every account, and reactivating a
deactivated account. `TransactionServiceBeanTest` covers history ordering
(most recent first), balance-as-sum-of-transactions, a zero balance with
no transactions, and that history is correctly scoped to its own account.
`PasswordHasherTest` covers hash/verify round-trips, wrong-password
rejection, and that two accounts with the same password get different
hashes (different salts).

## A real bug this caught

The `pom.xml` originally declared `struts:struts:1.3.10` — the coordinates
you'll find in a lot of older tutorials and Stack Overflow answers. Maven
Central resolves that groupId to a legacy POM-only stub with no attached
jar (`Could not find artifact struts:struts:jar:1.3.10`) — the same
"API declared but not actually resolvable" issue I hit with JSR 116 on the
SIP Telephony project. The real, currently-published coordinates are
`org.apache.struts:struts-core` and `org.apache.struts:struts-taglib`;
switching to those resolved and compiled cleanly against Java 17.

## Roadmap / not included

- A live EJB container in the test suite (see "About the EJB layer" above)
- Container-managed transactions/security (currently each bean method
  manages its own connection)
- No CSS/styling — plain HTML tables and forms, functionality over polish
- Self-service registration (accounts are seeded directly; there's no
  public sign-up form)
- A production-grade password hash (bcrypt/scrypt/Argon2) in place of
  salted SHA-256

## License

MIT — see [LICENSE](LICENSE).
