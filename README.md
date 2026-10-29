# ShieldAI: Enterprise Phishing & Fraud Detection System

ShieldAI is an enterprise-grade cybersecurity web application that detects phishing emails and malicious messages using Deep Learning (NLP) and explains its decisions using Explainable AI (XAI) via LIME.

---

## 🌟 Key Features
- **Deep Learning NLP Engine:** Sequential LSTM Neural Network (`Embedding` $\rightarrow$ `SpatialDropout` $\rightarrow$ `LSTM` $\rightarrow$ `Dense` $\rightarrow$ `Sigmoid`) trained on 20,000+ real-world phishing and legitimate communications.
- **Explainable AI (LIME):** Unpacks the "black box" by pinpointing the exact psychological trigger keywords (e.g., urgency, account suspension, verify) that influenced the classification.
- **Interactive Cyber Threat Dashboard:** Streamlit UI in cyber dark-mode featuring Plotly threat gauges, classification badges, confidence metrics, and interactive HTML feature importance visualizations.
- **Instant Scenario Presets:** Pre-configured email templates for immediate testing.

---

## 📁 Project Architecture
```text
ShieldAI_Fraud_and_Phishing_detection/
│
├── data/                         # Cached HuggingFace datasets
│   └── phishing_texts.json
├── models/                       # Saved model artifacts & config
│   ├── phishing_lstm.keras       # Trained Keras 3 LSTM model
│   ├── tokenizer.pkl             # Fitted Keras text tokenizer
│   └── config.json               # Model hyperparameters & metrics
├── notebooks/                    # Training and verification scripts
│   ├── train_lstm.py             # End-to-end dataset download & model training
│   └── verify_pipeline.py        # Automated test verification pipeline
├── app.py                        # Streamlit web dashboard
├── requirements.txt              # Pinned python dependencies
└── project_specification.md       # Implementation guide
```

---

## 🚀 Quickstart Guide

### 1. Install Dependencies
```bash
pip install -r requirements.txt
```

### 2. Train the LSTM Model
```bash
python notebooks/train_lstm.py
```
This script will:
- Download `ealvaradob/phishing-dataset` from Hugging Face Hub.
- Preprocess, tokenize, and pad sequences (`vocab_size=10,000`, `max_len=200`).
- Train the Sequential LSTM model and save weights to `models/phishing_lstm.keras`.
- Save the fitted tokenizer to `models/tokenizer.pkl`.

### 3. Launch the Streamlit Dashboard
```bash
streamlit run app.py
```
Open your browser to `http://localhost:8501`.

### 4. Run Automated Verification Tests
```bash
python notebooks/verify_pipeline.py
```
