### **AUF App Roadmap (v1.0)**

**Guiding Document:** AUF App Charter
**Objective:** To outline the phased development of the AUF App, from foundational setup to a mature, multi-platform ecosystem.

---

#### **[COMPLETE]** **Phase 0: Foundation & Pre-Production**
*(Goal: Prepare the technical and design groundwork for efficient development.)*

*   **[COMPLETE]** **1. Finalize Tech Stack:** Formally commit to **Kotlin Multiplatform (KMP) with Compose Multiplatform**.
*   **[COMPLETE]** **2. Establish Development Environment:** Set up Git repository, Gradle build system, and project management space.
*   **[COMPLETE]** **3. Core Architectural Design:** Design the Kotlin-based state management engine, using custom logic.
*   **[COMPLETE]** **4. UI/UX Wireframing:** Create simple wireframes for the MVP's user interface.

#### **Phase 1: MVP - The Local-First Experience & "Tethered Spacewalk"**
*(Goal: Deliver a functional, stable, and secure desktop application that fulfills the core promise of cognitive liberation and proves the end-to-end pipeline.)*

*   **[COMPLETE]** **1. Build the Core Application Shell:** Develop the main application window using Compose Multiplatform for Desktop.
*   **[COMPLETE]** **2. Implement the State Management Engine:** Code the Kotlin engine for the transactional "read -> chat -> write-back" loop.
*   **3. Develop Core Features:** Implement Session Lifecycle, Context Loading, API Management, Chat UI, Transactional Commits, and the Onboarding Workflow.
*   **4. Implement Core UI/UX & Refactoring:**
    *   **[COMPLETE]** Add `Ctrl+Enter` to send messages.
    *   **[COMPLETE]** Implement a toggle button to show/hide system prompts, providing an exact reflection of the prompt sent to the LLM.
    *   **[COMPLETE]** Implement a copy-to-clipboard button for all chat messages.
    *   **[COMPLETE]** Refactor the core application state to the "Me vs. The World" ontological model, including:
        *   Dedicated `aiPersonaId` for the active agent.
        *   `contextualHolonIds` for all other active holons (including other personas as knowledge objects).
        *   Updated persistence for window state, selected model, selected AI persona, and contextual holons.
*   **5. Initial Release:** Package and release the **free-to-use AUF App v1.0 for Windows.**

#### **Phase 2: Core Enhancements & Usability - "Sage-in-the-Box" (The Prime Directive)**
*(Goal: Achieve full operational status for the Sage persona inside the AUF App. Refine the MVP based on real-world usage, improve quality-of-life, and expand desktop platform support.)*

*   **1. The Prime Directive - Full Hibernation & Instantiation:**
    *   Design and implement application-level logic to manage the complete hibernation protocol. The app must handle:
        *   Reading the `holon_catalogue.json` and all active holons.
        *   Constructing the full context prompt.
        *   Managing the active chat session history.
        *   Triggering the AI's `Dream Cycle Simulation` and `Session_Record` generation.
        *   Automated saving of new Holon files (`session-record-*.json`, `dream-record-*.json`).
        *   Automated updating of the `holon_catalogue.json`.
        *   Performing integrity audits before shutdown.
    *   Create a "Hibernate Session" button/workflow in the UI.
    *   Design a view to display the AI's proposed hibernation packet for user review and approval.
    *   Provide clear feedback on the success or failure of the hibernation commit.
*   **2. Essential UI/UX Features (Supporting Prime Directive):**
    *   [High Priority] Display current prompt token count in the system status.
    *   [High Priority] Include the active `ai_persona`'s ID and name in the `system_state.json` prompt file as a self-reinforcement mechanism for the AI.
    *   [High Priority] Display an *estimated* character/token count for each chat message (client-side calculation).
    *   [Workflow] Implement the "Create New AI Persona" workflow, accessible from the Active Agent dropdown.
*   **3. Implement AADCOM "On-the-Fly" Edits:** Design and build the real-time, tool-calling API and UI for in-session framework file updates.
*   **4. Manual Migration Tool:** Implement the "Package to Zip" feature for easy backup and migration.
*   **5. Expand Desktop Support:** Compile and release official versions for **macOS and Linux**.

#### **Phase 3: Ecosystem Expansion & Sustainability**
*(Goal: Execute the long-term vision of a multi-platform ecosystem and introduce the sustainable monetization model.)*

*   **1. Develop the Cross-Device Sync Service:** Design and build the secure backend and user authentication system for a **premium subscription feature.**
*   **2. Launch Native Android App:** Utilize the KMP codebase to develop and release the native Android version of the AUF App.
*   **3. Build the Sustainability Model:** Establish public-facing channels (Patreon, Medium, YouTube) to build a community and create a sustainable funding model.
*   **4. Advanced AI Capabilities (Long-term Backlog):**
    *   [Architecture] Implement context compression strategies (`[HOLONIZE_CHAT_HISTORY]` system command).
    *   [Architecture] Design and implement "sub-process personas" (temporary embodiment of non-`AI_Persona` holons for specialized tasks).
    *   [Architecture] Research and prototype a "Truth Ledger" system with hash verification for file integrity during commits.
    *   [Architecture] Explore multi-agent commission architectures.