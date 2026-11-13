# Contributing to Student Management System

First off, thank you for considering contributing to the Student Management System! It's people like you that make it a great tool for educational environments.

## Code of Conduct

Please be respectful and constructive in all communications.

## How Can I Contribute?

### Reporting Bugs
- Ensure the bug was not already reported by searching on GitHub under [Issues](../../issues).
- If you're unable to find an open issue addressing the problem, open a new one using the **Bug Report** template.
- Include step-by-step instructions, logs, and your local environment specifications.

### Suggesting Enhancements
- Open an issue describing the proposed feature or improvement using the **Feature Request** template.
- Explain why this enhancement would be useful to most users.

### Pull Requests
1. Fork the repository and create your branch from `main`:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. Set up your local development environment:
   - Backend: Ensure Java 17+ and MySQL 8.0 are configured.
   - Frontend: Node.js 18+ and `npm install`.
3. Make your changes:
   - Follow clean code conventions (naming, indentation, modularity).
   - Verify that both backend builds (`./mvnw clean package`) and frontend builds (`npm run build`) succeed without errors.
4. Commit your changes:
   ```bash
   git commit -m "feat: add support for course prerequisites"
   ```
5. Push to your branch and submit a Pull Request to `main`.

## License

By contributing, you agree that your contributions will be licensed under the project's [MIT License](LICENSE).
