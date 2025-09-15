1. **Scan the entire project** to:
   - Validate that all dependencies are correctly installed for each language.
   - Identify any missing, outdated, or incompatible packages.

2. **Run all available tests** for each language environment:
   - Node.js: Use `npm test` or `jest` if configured.
   - Java: Use `mvn test` or `gradle test`.
   - Python: Use `pytest` or `unittest`.

3. **Report the results** in a structured format:
   - ✅ Passed tests
   - ❌ Failed tests (with error messages)
   - ⚠️ Dependency issues (missing, version conflicts, etc.)

4. **Provide actionable fixes** for:
   - Installing or updating missing/incompatible dependencies.
   - Resolving test failures with suggested code or config changes.

Assume the project uses standard folder structures and package managers (npm, pip, Maven/Gradle). If any assumptions are needed, clearly state them.