# Calorie Tracker — Team AKS
**CS 4261: Mobile Apps and Services**  
**Team Members:**  
- Aryan Patel  
- Khoa (John) Nguyen  
- Sachin Kathiresan  

---

## Overview  
**Calorie Tracker** is a mobile app that helps users track their daily nutrition through **AI‑driven automation** and **user engagement tools.** The app differentiates itself from existing calorie tracking platforms by minimizing manual input and promoting consistent, sustainable habits through **AI vision**, **personalized notifications**, and soon, an **AI companion** that provides nutritional guidance and healthy recommendations.

The project was developed in multiple sprints throughout the semester as part of Georgia Tech’s **CS 4261 — Mobile Apps and Services** course, with each sprint focusing on a different layer of functionality, user testing, and learning objectives.

---

## Project Goals
1. Make calorie tracking faster, more convenient, and less repetitive.  
2. Use AI to automate calorie and macronutrient estimation.  
3. Help users stay consistent through reminders and motivation.  
4. Guide users toward healthier long‑term eating habits through an intelligent AI Companion.  

---

## Key Features (Across Sprints)

### **Sprint 1: Ideation and Research**
- Conducted user interviews to identify core pain points:
  - Manual entry and time cost of current apps.
  - Need for quick, automatic logging.
- Defined core problem and initial architecture.
- Proposed three solution concepts:
  1. LLM Tracking (image-based AI recognition)
  2. Automated Logging
  3. Smart Suggestions
- Built initial wireframes and team agreement on project scope.

---

### **Sprint 2: Solution Refinement and Early Mockups**
- Refined solutions into distinct approaches:
  - **LLM Tracking** – AI estimates calories from food photos.  
  - **Voice Logging** – Users log meals via speech input.  
  - **AI Companion** – Personalized suggestions with reminders.
- Created polished mockups and storyboards for each solution.
- Conducted additional user interviews (6 total) for validation.
- Built the **Value Proposition Canvas (VPC)** and **Business Model Canvas (BMC)**.  
- Defined learning prototype goals for future sprints.

---

### **Sprint 3: First Functional Prototype (AI Vision & Data Collection)**
- Implemented **LLM-based image recognition** using OpenAI/Gemini APIs.  
- Built initial **Android prototype** with Firebase backend:
  - Image capture/upload.
  - LLM nutritional estimation.
  - Editable confirmation screen with portion options.
- Added thumbs‑up/thumbs‑down feedback system.  
- Conducted usability testing with 8 participants.  
- Quantified user trust and accuracy perception (~3.9/5 avg.).  
- Learned that users valued **ease and transparency** over perfect accuracy.

---

### **Sprint 4: Second Prototype — Notification and Engagement System**
- Goal: Test how **push notifications** improve engagement and consistency.  
- Integrated **Firebase Cloud Messaging** for timed reminders and custom schedules.  
- Created analytics system to track app opens, logging rates, and fatigue patterns.  
- Conducted **A–B testing:**
  - Group A (with notifications): 3.2 logs/day  
  - Group B (without notifications): 1.8 logs/day  
- Found that 2–3 daily reminders boosted engagement but >3 caused fatigue.  
- Updated architecture to include:
  - **Firebase Cloud Messaging**  
  - **Engagement Analytics Dashboard**
- Learned that behavior‑driven personalization increases engagement more than quantity of reminders.

---

### **Sprint 5 (In Progress): AI Companion**
- Current focus: building an **AI Companion** that provides:
  - Meal suggestions based on logged data.  
  - Personalized coaching and nutrition insights.  
  - Contextual support messages (“You’re close to your daily goal!”).  
  - Transparent reasoning (“Based on your last 3 meals, you could add more protein.”)
- Goals for this sprint:
  - Test user trust, satisfaction, and motivation driven by intelligent interaction.
  - Evaluate how personalized support sustains long‑term usage beyond notifications.

---

## Technical Architecture  
**Components:**
- **Frontend (Android):**  
  - User interface for logging, receiving reminders, and viewing meal insights.
- **Firebase Services (Backend):**  
  - Authentication and database management (Firestore).  
  - Cloud Messaging for notifications.  
  - Analytics Dashboard for engagement metrics.
- **LLM Integration (OpenAI/Gemini API):**  
  - Processes meal photos and returns calorie/macro estimations.  
- **Analytics Layer:**  
  - Tracks app open frequency, log count, and user feedback.

**Data Flow Overview:**  
Users upload food photos → Sent to Firebase → Forwarded to LLM → LLM returns estimated nutrients → Stored in Firestore → Notifications and insights generated based on user behavior → Data visualized in Analytics Dashboard.

---

## Key Learnings (So Far)
- **User Research is Iterative:** Each sprint revealed new pain points, shaping later prototypes.  
- **Behavioral Design Matters:** Engagement and motivation must be tested as much as functionality.  
- **Quantitative + Qualitative Testing:** A–B testing, surveys, and sentiment data provide holistic insights.  
- **Transparency Builds Trust:** Users value clear explanations from AI models (“how” and “why”).  
- **Personalization is the Future:** Adaptive AI guidance drives consistent user retention more effectively than one‑size‑fits‑all solutions.

---

## Next Steps
1. Complete the **AI Companion** prototype (Sprint 5).  
2. Test contextual suggestions, motivation style, and trust feedback loops.  
3. Continue improving **data privacy and transparency** for user trust.  
4. Prepare final presentation and demo video summarizing full project lifecycle.  

---

## Tech Stack
| Category | Technology |
|-----------|-------------|
| **Frontend** | Android Studio (Kotlin/Java) |
| **Backend** | Firebase (Firestore, Authentication, Cloud Functions, Cloud Messaging) |
| **AI / NLP** | OpenAI or Gemini API |
| **Storage / Database** | Firebase Firestore |
| **Analytics** | Firebase Analytics Dashboard |
| **Design Tools** | Figma, Lucidchart |
| **Version Control** | Git & GitHub |

---

## Project Timeline Overview
| Sprint | Focus | Key Achievement |
|--------|--------|-----------------|
| 1 | Ideation + Research | Defined user problem and core app concept |
| 2 | Concept Refinement | Created mockups and VPC/BMC |
| 3 | First Prototype | Implemented LLM vision model for calorie recognition |
| 4 | Second Prototype | Integrated notification system and A–B testing |
| 5 | Final Prototype | Building AI Companion for guided engagement (in progress) |

---
