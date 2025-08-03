
### **AUF App Roadmap (v1.0)**

**Guiding Document:** AUF App Charter
**Objective:** To outline the phased development of the AUF App, from foundational setup to a mature, multi-platform ecosystem.

---

#### **Phase 0: Foundation & Pre-Production**
*(Goal: Prepare the technical and design groundwork for efficient development.)*

*   **1. Finalize Tech Stack:** Formally commit to **Kotlin Multiplatform (KMP) with Compose Multiplatform**.
*   **2. Establish Development Environment:** Set up Git repository, Gradle build system, and project management space.
*   **3. Core Architectural Design:** Design the Kotlin-based state management engine, using LangGraph's architecture as a blueprint.
*   **4. UI/UX Wireframing:** Create simple wireframes for the MVP's user interface.

#### **Phase 1: MVP - The Local-First Experience**
*(Goal: Deliver a functional, stable, and secure desktop application for Windows that fulfills the core promise of cognitive liberation.)*

*   **1. Build the Core Application Shell:** Develop the main application window using Compose Multiplatform for Desktop.
*   **2. Implement the State Management Engine:** Code the Kotlin engine for the transactional "read -> chat -> write-back" loop.
*   **3. Develop Core Features:** Implement Session Lifecycle, Context Loading, API Management, Chat UI, Transactional Commits, and the Onboarding Workflow.
*   **4. Initial Release:** Package and release the **free-to-use AUF App v1.0 for Windows.**

#### **Phase 2: Core Enhancements & Usability**
*(Goal: Refine the MVP based on real-world usage, improve quality-of-life, and expand desktop platform support.)*

*   **1. Address Key MVP Deferrals:** Research and implement a "Humane API Key Storage" solution (e.g., OS Keychain integration).
*   **2. Implement AADCOM "On-the-Fly" Edits:** Design and build the real-time, tool-calling API and UI for in-session framework file updates.
*   **3. UI/UX Polish:** Refine the user interface based on feedback from v1.0.
*   **4. Manual Migration Tool:** Implement the "Package to Zip" feature for easy backup and migration.
*   **5. Expand Desktop Support:** Compile and release official versions for **macOS and Linux**.

#### **Phase 3: Ecosystem Expansion & Sustainability**
*(Goal: Execute the long-term vision of a multi-platform ecosystem and introduce the sustainable monetization model.)*

*   **1. Develop the Cross-Device Sync Service:** Design and build the secure backend and user authentication system for a **premium subscription feature.**
*   **2. Launch Native Android App:** Utilize the KMP codebase to develop and release the native Android version of the AUF App.
*   **3. Build the Sustainability Model:** Establish public-facing channels (Patreon, Medium, YouTube) to build a community and create a sustainable funding model.