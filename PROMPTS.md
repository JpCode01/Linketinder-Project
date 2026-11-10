1. Template principal — Implementação de qualquer tarefa

# Task: Implement the Next Approved SDD Task

You are a senior software engineer implementing an approved feature through Specification-Driven Development (SDD).

**Objective: Implement ONLY the task explicitly identified by the user in the approved implementation plan.**

## 1. Mandatory Context

Read and follow:

1. `AGENTS.md`
2. The feature's approved `spec.md`
3. The feature's approved `plano.md`
4. The existing `feature-com-spec` skill
5. `CHANGELOG.md`

Treat the specification as the authoritative source for business requirements and the implementation plan as the authoritative source for task scope, dependencies, and acceptance criteria.

Review previously completed tasks only as necessary to understand dependencies and the current implementation.

Inspect relevant source files, database definitions, contracts, and tests. Never assume planned components already exist without verifying the repository.

## 2. Implementation

Implement **ONLY the requested task**, following its exact scope and acceptance criteria.

- Preserve existing architecture, conventions, and approved decisions.
- Follow the naming and security conventions documented in `AGENTS.md`.
- Make minimal, focused changes.
- Reuse existing components and patterns when appropriate.
- Maintain compatibility with completed tasks.
- Preserve authorization, security, and anonymity invariants.
- Respect database integrity and transaction boundaries where applicable.
- Do not implement subsequent tasks or introduce unrelated refactoring.
- Do not modify the approved specification.

If a genuine blocker or unresolved ambiguity prevents correct implementation, stop and request clarification.

## 3. Validation

After implementation:

1. Review the complete Git diff.
2. Confirm that all changes belong to the requested task.
3. Execute relevant builds and tests.
4. Perform applicable functional checks.
5. Verify security, authorization, anonymity, and regression behavior.
6. Validate database integrity and transaction behavior where applicable.
7. Verify every acceptance criterion in `plano.md`.

**Database safety:** Before running database-writing tests, verify that all connections target an isolated test database without fallback to development or production. Inspect setup and cleanup operations. If isolation cannot be established, do not execute the tests.

Report actual execution evidence. Never claim that unexecuted tests passed.

## 4. Documentation

Update:

**`CHANGELOG.md`**
- Record only changes actually implemented.
- Follow the existing Keep a Changelog structure.
- Avoid duplicate entries.

**`plano.md`**
- Mark the requested task as completed only when its acceptance criteria are verified.
- Otherwise, document remaining blockers or pending validation.
- Preserve all other tasks.

## 5. Final Report

Provide:

1. Task identifier, title, and objective.
2. Summary of changes.
3. Created and modified files.
4. Executed commands and actual results.
5. Security, anonymity, and database verification, if applicable.
6. Documentation updates.
7. Remaining issues.
8. Final Git diff summary.

## 6. Restrictions

- DO NOT execute `git add`, `git commit`, or `git push`.
- Do not modify unrelated files.
- Do not change approved business decisions.
- Do not implement subsequent tasks.
- Do not discard existing user changes.
- Do not invent APIs, files, or validation results.
- Do not advance automatically.

**Implement the requested task, validate it, update the documentation, report the results, and STOP.**

2. Template — Correções pontuais e refatorações

# Task: Focused Code Correction

You are a senior software engineer performing a narrowly scoped correction in an existing project.

**Objective: Apply ONLY the correction described in my request.**

## Instructions

1. Read `AGENTS.md` and inspect the relevant implementation.
2. Identify the root cause and affected references.
3. Apply the smallest correct change.
4. Preserve existing business logic, behavior, contracts, and compatibility unless the requested correction explicitly requires otherwise.
5. Update affected callers and tests when necessary.
6. Avoid unrelated refactoring or feature development.

## Documentation

- Update `CHANGELOG.md` with the actual correction and its rationale.
- If the correction establishes a reusable architectural or coding convention, document the decision and its reasoning in `AGENTS.md` to prevent future recurrence.
- Do not modify approved specifications or implementation plans unless explicitly requested.

## Validation

- Run relevant tests.
- Review the complete Git diff.
- Verify that existing behavior remains unchanged except for the requested correction.
- Report actual validation results and any limitations.

## Restrictions

- Do not implement unrelated changes or future tasks.
- Do not discard existing user changes.
- Do not execute `git add`, `git commit`, or `git push`.
- Stop after completing and reporting the correction.

**Apply the correction, validate it, update the necessary documentation, and STOP.**

3. Template — Revisão e aprovação de implementação

# Task: Review an Implemented SDD Task

You are a senior software engineer performing an independent technical review of an implemented task.

**Objective: Determine whether the requested task is ready for approval and manual commit.**

## Context

Read:

1. `AGENTS.md`
2. The approved feature specification
3. The approved implementation plan
4. `CHANGELOG.md`

Identify the requested task and review its actual implementation against the approved requirements and acceptance criteria.

## Review

Evaluate:

1. Correctness and completeness.
2. Compliance with the approved specification.
3. Architecture, Clean Code, and existing conventions.
4. Security, authorization, and anonymity invariants.
5. Database integrity and transaction safety, where applicable.
6. Compatibility and regression risks.
7. Test coverage and actual validation evidence.
8. Accuracy of `CHANGELOG.md` and task completion status.
9. Unrelated changes or accidental scope expansion.

Classify findings as **Critical, High, Medium, or Low**, explaining their impact and identifying affected files.

Distinguish confirmed defects from potential risks or suggestions.

Do not invent issues merely to produce findings.

## Final Report

Provide:

- Approval recommendation: APPROVE or REQUEST CHANGES.
- Findings ordered by severity.
- Evidence supporting each finding.
- Missing validation, if any.
- Required corrections before approval.
- Optional improvements that can be deferred.

## Restrictions

- READ-ONLY REVIEW.
- Do not modify files.
- Do not execute destructive operations.
- Do not execute `git add`, `git commit`, or `git push`.
- Do not implement subsequent tasks.

**Review the implementation, report your findings, and STOP.**

4. Template — Verificação de segurança dos testes JDBC

# Task: Verify JDBC Integration Test Safety

Before requesting authorization to execute JDBC integration tests outside the sandbox, verify their database isolation.

## Verification

1. Identify the exact database configuration used by the selected tests.
2. Inspect JDBC connection creation and environment variable resolution.
3. Confirm that all connections target the dedicated test database.
4. Verify that no connection can fall back to development or production.
5. Identify SQL operations that modify data or schema.
6. Inspect setup, teardown, and cleanup behavior.
7. Identify any filesystem changes or external resources accessed by the test command.
8. Confirm whether the proposed command is safe to execute.

Do not print passwords, credentials, or other secrets.

## Final Report

Provide:

- Database isolation evidence.
- Potentially destructive operations.
- Files and configurations inspected.
- Identified risks.
- Final recommendation: SAFE TO AUTHORIZE or NOT SAFE TO AUTHORIZE.
- Exact command proposed for execution.

## Restrictions

- Do not execute the tests yet.
- Do not modify any files or databases.
- Do not execute Git staging, commit, or push commands.
- Wait for my explicit authorization.

**Verify test safety, report the findings, and STOP.**