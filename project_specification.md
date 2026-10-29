# ShieldAI: Phishing Detection System - Agent Implementation Guide

## 1. Project Context & Objectives
**To the AI Agent:** You are assisting the lead developer in building "ShieldAI." 
ShieldAI is an enterprise-grade cybersecurity web application designed to detect phishing emails using Deep Learning (NLP) and explain its reasoning using Explainable AI (XAI). 

The goal is to move beyond a "black box" binary classifier by integrating LIME (Local Interpretable Model-agnostic Explanations) so the user can visually see exactly which psychological triggers (e.g., "urgent", "account suspended") caused the system to flag the text as malicious.

### Tech Stack
*   **Backend/ML:** Python 3.10+, TensorFlow/Keras (LSTM network), Scikit-Learn.
*   **Explainability:** LIME (`lime-text`).
*   **Frontend/UI:** Streamlit, Plotly (for gauge charts).
*   **Dataset:** `ealvaradob/phishing-dataset` (from HuggingFace).

---

## 2. Target Directory Structure
The workspace should eventually look like this:
```text
ShieldAI_Fraud_and_Phishing_detection/
│
├── data/                   # Raw and processed datasets (git-ignored)
├── models/                 # Saved .keras weights and tokenizer artifacts
├── notebooks/              # Training scripts (train_lstm.py)
├── app.py                  # Main Streamlit frontend application
├── requirements.txt        # Pinned dependencies
└── project_specification.md # This file
```

---

## 3. Execution Prompts (Phased Implementation Plan)
**To the Human Developer:** Feed these prompts one by one into the AI Agent panel to build the project sequentially.

### Phase 1: Environment & Scaffolding
**Prompt 1:**
> "Please review our project context in `@project_specification.md`. Let's begin with Phase 1. Create the base folder structure (`/data`, `/models`, `/notebooks`). Then, generate a strict `requirements.txt` file including `streamlit`, `tensorflow`, `pandas`, `scikit-learn`, `plotly`, and `lime`. Do not write the model code yet, just set up the environment and files."

### Phase 2: The Deep Learning Engine
**Prompt 2:**
> "Now execute Phase 2. Create a script named `notebooks/train_lstm.py`. This script must: 
> 1. Download the `ealvaradob/phishing-dataset` from HuggingFace.
> 2. Preprocess the text (Tokenization and Padding).
> 3. Build and train a Sequential LSTM model (Embedding -> LSTM -> Dense). 
> 4. Include code to save the trained model as `models/phishing_lstm.keras` and save the fitted tokenizer using `pickle` so we can load them later."

### Phase 3: The Explainable AI Dashboard
**Prompt 3:**
> "Let's build the frontend UI in Phase 3. Create the main `app.py` file. 
> Configure a wide, dark-mode Streamlit layout. Include a text area for users to paste suspicious emails. Write a function using `@st.cache_resource` to load our `phishing_lstm.keras` model and tokenizer. 
> When the user clicks 'Analyze':
> 1. Run the model prediction.
> 2. Display the threat probability score using a Plotly Gauge Chart (green for safe, red for high risk).
> 3. Implement `LimeTextExplainer` from the `lime` library to analyze the model's decision. 
> 4. Render the LIME HTML output directly in Streamlit using `st.components.v1.html` so the user sees the exact highlighted words that triggered the phishing alert. Use Streamlit columns to organize the gauge chart and the text analysis side-by-side."

### Phase 4: Autonomous Testing
**Prompt 4:**
> "Phase 4: Run `streamlit run app.py` in the terminal. Use your browser testing capabilities to open the local server URL. Paste this text: 'URGENT: Your bank account will be suspended in 24 hours. Click the link to verify your identity.' into the text box, click analyze, and verify that the Plotly gauge and LIME explanations render correctly without throwing any errors."

