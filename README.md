# 🤖 Autonomous AI Code Review Agent

An intelligent, event-driven Spring Boot microservice that automatically inspects GitHub Pull Requests using **Google Gemini**, provides line-by-line or general architectural reviews, posts feedback directly to the PR timeline, and logs metrics to a PostgreSQL database.

---

## 🚀 Features

* **Event-Driven Webhooks:** Listens to GitHub `pull_request` events (`opened` and `synchronize`) in real time.
* **Automated Diff Fetching:** Securely pulls code diffs directly from GitHub's REST API using Spring `RestClient`.
* **AI-Powered Code Analysis:** Leverages **Google Gemini 3.5 Flash** (via Spring AI) to review code changes for performance bottlenecks, time/space complexity, and code smells.
* **Interactive Feedback:** Automatically publishes the AI's review comments back to the GitHub Pull Request conversation timeline.
* **Audit Logging:** Persists every code review transaction into a cloud PostgreSQL database (Neon).
* **Infinite Loop Prevention:** Intelligently ignores bot-generated comments to prevent recursive webhook triggering.

---

## 🛠️ Technology Stack

* **Backend:** Java, Spring Boot, Spring AI, Spring Web (`RestClient`)
* **AI Engine:** Google Gemini API (`gemini-3.5-flash`)
* **Database:** PostgreSQL (Neon)
* **Integration:** GitHub Webhooks & REST API
* **Tunneling:** Microsoft Dev Tunnels (for local webhook delivery)
* **Build Tool:** Maven

---
