# 🧠 Synapse - an AI-powered knowledge management agent - Source Code Guidelines

## 📌 Purpose
This document defines the software development rules applied across the project to ensure:
- High-quality source code
- Maintainability and scalability
- Comprehensive testing and logging
- Compliance with language-specific coding conventions

---

## ✅ Development Rules

### 1. Test-Driven Development (TDD)
- All features must be developed using **TDD methodology**: write tests first, then implement the code.
- Recommended testing frameworks:
  - Java: JUnit, Mockito
  - Python: pytest, unittest
  - ReactJS: Jest, React Testing Library

### 2. Unit Test Coverage
- **Every public function or method** must have corresponding unit tests.
- Minimum coverage per module: **80%**
- Tests must include both **positive and negative cases**

### 3. Logging
- All important **input/output processes** must be logged.
- Logs should include:
  - Execution timestamp
  - Input data (if not sensitive)
  - Output or error details
- Use standard logging libraries:
  - Java: SLF4J + Logback
  - Python: logging module
  - ReactJS: console or backend-integrated logging

---

## 🧑‍💻 Coding Conventions

### 🔷 Java
- Follow the rules in [java-coding-convention.md](https://fptsoftware362-my.sharepoint.com/personal/daimt_fpt_com/Documents/Microsoft%20Copilot%20Chat%20Files/java-coding-convention.md?EntityRepresentationId=779c74f0-9866-4e33-a167-dda25fc4c153) [1](https://fptsoftware362-my.sharepoint.com/personal/daimt_fpt_com/Documents/Microsoft%20Copilot%20Chat%20Files/java-coding-convention.md)
- Key highlights:
  - No wildcard imports
  - Class names: PascalCase, method names: camelCase
  - One public class per file
  - Always use `@Override` for overridden methods
  - Javadoc required for all public classes and methods

### 🐍 Python
- Follow the rules in [python-coding-convention.md](https://fptsoftware362-my.sharepoint.com/personal/daimt_fpt_com/Documents/Microsoft%20Copilot%20Chat%20Files/python-coding-convention.md?EntityRepresentationId=0c900d4a-a75a-4363-96ce-5d79a16f61e3) [2](https://fptsoftware362-my.sharepoint.com/personal/daimt_fpt_com/Documents/Microsoft%20Copilot%20Chat%20Files/python-coding-convention.md)
- Key highlights:
  - Variable/function names: `snake_case`, class names: `PascalCase`
  - Docstrings must follow https://peps.python.org/pep-0257/
  - Avoid generic `except Exception`
  - Clear separation of standard, external, and internal imports

### ⚛️ ReactJS
- Follow the rules in [reactjs-coding-convention.md](https://fptsoftware362-my.sharepoint.com/personal/daimt_fpt_com/Documents/Microsoft%20Copilot%20Chat%20Files/reactjs-coding-convention.md?EntityRepresentationId=77a6e99f-59fe-4ea0-9ab2-2171b80903e9) [3](https://fptsoftware362-my.sharepoint.com/personal/daimt_fpt_com/Documents/Microsoft%20Copilot%20Chat%20Files/reactjs-coding-convention.md)
- Key highlights:
  - Components: PascalCase, handlers: camelCase
  - Use functional components and hooks
  - Extract reusable logic into custom hooks
  - Use ESLint + Prettier for formatting

---

## 📂 Recommended Folder Structure
See details in `./docs/architecture/source-structure.md`

---

## 📎 Notes
- All pull requests must pass CI (tests + lint) and be reviewed.
- Coding conventions may be extended based on project-specific needs.
- This document should be shared and trained across all project members.
