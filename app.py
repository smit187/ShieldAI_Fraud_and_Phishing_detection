"""
ShieldAI - Enterprise Phishing Detection & Explainable AI (XAI) Dashboard
Frontend: Streamlit + Plotly
Inference Engine: TensorFlow / Keras (LSTM)
Explainability: LIME (Local Interpretable Model-agnostic Explanations)
"""

import os
import sys
import pickle
import numpy as np
import plotly.graph_objects as go
import streamlit as st
import streamlit.components.v1 as components

# Configure Streamlit Page
st.set_page_config(
    page_title="ShieldAI - Phishing Detection System",
    page_icon="🛡️",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom Cyber Dark Mode CSS
st.markdown("""
<style>
    /* Global dark theme overrides */
    .stApp {
        background-color: #0b0f19;
        color: #e2e8f0;
    }
    
    /* Header typography */
    .main-title {
        font-size: 2.2rem;
        font-weight: 800;
        background: linear-gradient(90deg, #38bdf8, #818cf8, #c084fc);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin-bottom: 0.2rem;
    }
    .sub-title {
        color: #94a3b8;
        font-size: 1.05rem;
        margin-bottom: 1.5rem;
    }

    /* Cards */
    .metric-card {
        background: #131c2e;
        border: 1px solid #1e293b;
        border-radius: 12px;
        padding: 1.2rem;
        margin-bottom: 1rem;
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    }
    
    .badge-critical {
        background: rgba(239, 68, 68, 0.2);
        color: #f87171;
        border: 1px solid #ef4444;
        padding: 6px 14px;
        border-radius: 20px;
        font-weight: 700;
        display: inline-block;
    }
    .badge-warning {
        background: rgba(245, 158, 11, 0.2);
        color: #fbbf24;
        border: 1px solid #f59e0b;
        padding: 6px 14px;
        border-radius: 20px;
        font-weight: 700;
        display: inline-block;
    }
    .badge-safe {
        background: rgba(34, 197, 94, 0.2);
        color: #4ade80;
        border: 1px solid #22c55e;
        padding: 6px 14px;
        border-radius: 20px;
        font-weight: 700;
        display: inline-block;
    }
    
    /* Trigger pills */
    .trigger-pill-phish {
        background: rgba(239, 68, 68, 0.25);
        color: #fca5a5;
        border: 1px solid #f87171;
        border-radius: 6px;
        padding: 3px 8px;
        margin: 2px 4px 2px 0;
        display: inline-block;
        font-family: monospace;
        font-size: 0.9rem;
    }
    .trigger-pill-safe {
        background: rgba(34, 197, 94, 0.25);
        color: #86efac;
        border: 1px solid #4ade80;
        border-radius: 6px;
        padding: 3px 8px;
        margin: 2px 4px 2px 0;
        display: inline-block;
        font-family: monospace;
        font-size: 0.9rem;
    }
</style>
""", unsafe_allow_html=True)


# Load Model and Tokenizer with Caching
@st.cache_resource(show_spinner="Loading Deep Learning Weights & Artifacts...")
def load_detection_engine():
    import tensorflow as tf
    from tensorflow.keras.models import load_model

    model_path = "models/phishing_lstm.keras"
    tokenizer_path = "models/tokenizer.pkl"

    if not os.path.exists(model_path) or not os.path.exists(tokenizer_path):
        return None, None, "Artifacts missing. Please train the model via `notebooks/train_lstm.py` first."

    try:
        model = load_model(model_path)
        with open(tokenizer_path, "rb") as f:
            tokenizer = pickle.load(f)
        return model, tokenizer, None
    except Exception as e:
        return None, None, f"Error loading artifacts: {str(e)}"


# LIME Prediction Wrapper
def make_prediction_fn(model, tokenizer, max_len=200):
    from tensorflow.keras.preprocessing.sequence import pad_sequences
    
    def predict_proba(texts):
        # Convert list of string inputs into sequences
        sequences = tokenizer.texts_to_sequences(texts)
        padded = pad_sequences(sequences, maxlen=max_len, padding="post", truncating="post")
        preds = model.predict(padded, verbose=0)
        # Ensure preds shape is (N, 1)
        preds = np.array(preds).reshape(-1, 1)
        # LIME expects shape (N, 2): [prob_legitimate, prob_phishing]
        probs = np.hstack([1.0 - preds, preds])
        return probs

    return predict_proba


# Plotly Threat Gauge
def render_threat_gauge(threat_score: float) -> go.Figure:
    """Build a cyberpunk-themed Plotly gauge chart."""
    fig = go.Figure(go.Indicator(
        mode="gauge+number",
        value=threat_score,
        domain={'x': [0, 1], 'y': [0, 1]},
        number={
            'suffix': "%",
            'font': {'size': 44, 'color': "#ffffff", 'family': "sans-serif"}
        },
        title={
            'text': "THREAT PROBABILITY SCORE",
            'font': {'size': 14, 'color': "#94a3b8", 'family': "sans-serif"}
        },
        gauge={
            'axis': {
                'range': [0, 100],
                'tickwidth': 1,
                'tickcolor': "#475569",
                'tickfont': {'color': "#94a3b8"}
            },
            'bar': {'color': "#ffffff", 'thickness': 0.15},
            'bgcolor': "#1e293b",
            'borderwidth': 1,
            'bordercolor': "#334155",
            'steps': [
                {'range': [0, 35], 'color': "#10b981"},    # Green / Safe
                {'range': [35, 70], 'color': "#f59e0b"},   # Yellow / Suspicious
                {'range': [70, 100], 'color': "#ef4444"}   # Red / Phishing
            ],
            'threshold': {
                'line': {'color': "#f87171", 'width': 4},
                'thickness': 0.85,
                'value': threat_score
            }
        }
    ))

    fig.update_layout(
        paper_bgcolor="#0f172a",
        plot_bgcolor="#0f172a",
        font={'color': "#f8fafc"},
        margin=dict(l=20, r=20, t=50, b=20),
        height=280
    )
    return fig


# Quick Test Presets
PRESETS = {
    "Select a pre-configured scenario...": "",
    "🚨 Urgent Bank Suspension (Phishing)": (
        "URGENT: Your bank account will be suspended in 24 hours. "
        "Click the link to verify your identity and prevent permanent suspension of your funds."
    ),
    "💳 Fake PayPal Security Alert (Phishing)": (
        "Dear Customer, we detected unauthorized login attempts from a new IP address. "
        "Please update your password and confirm your billing information immediately: http://bit.ly/secure-paypal-login"
    ),
    "💼 Internal Team Meeting (Legitimate)": (
        "Hi team, just a reminder that our weekly sprint retrospective is scheduled for tomorrow at 10:00 AM. "
        "Please review the sprint board and add your updates beforehand. Thanks, Sarah."
    ),
    "✈️ Flight Booking Confirmation (Legitimate)": (
        "Thank you for booking with Delta Air Lines. Your confirmation number is H7K29P. "
        "Your flight from Atlanta to Seattle departs on Friday at 8:45 AM. You can check in 24 hours prior to departure."
    )
}


# Main Application Layout
def main():
    # Header
    col_icon, col_txt = st.columns([0.08, 0.92])
    with col_icon:
        st.markdown("<h1 style='margin-bottom:0;'>🛡️</h1>", unsafe_allow_html=True)
    with col_txt:
        st.markdown("<div class='main-title'>ShieldAI: Phishing & Scam Detection Engine</div>", unsafe_allow_html=True)
        st.markdown("<div class='sub-title'>Deep Learning (LSTM) Threat Classification with Local Interpretable Model-agnostic Explanations (LIME)</div>", unsafe_allow_html=True)

    # Sidebar: System Metadata & Quick Templates
    with st.sidebar:
        st.markdown("### ⚙️ Engine Diagnostics")
        st.info("**Model Architecture:** Sequential LSTM\n- Embedding (dim=64)\n- SpatialDropout (0.2)\n- LSTM (units=64)\n- Dense (32, ReLU)\n- Sigmoid Output")
        
        st.markdown("---")
        st.markdown("### 📋 Quick Test Scenarios")
        selected_scenario = st.selectbox(
            "Load pre-built email template:",
            list(PRESETS.keys())
        )
        
        st.markdown("---")
        st.markdown("### 🎯 Risk Rating Guide")
        st.markdown("🟢 **0% - 35%**: Legitimate / Safe\n\n🟡 **36% - 70%**: Suspicious / Review\n\n🔴 **71% - 100%**: High Risk / Phishing")

    # Load Model
    model, tokenizer, error_msg = load_detection_engine()

    if error_msg:
        st.error(f"⚠️ **Model Artifact Status**: {error_msg}")
        st.info("Tip: Run `python notebooks/train_lstm.py` in your terminal to train and produce `models/phishing_lstm.keras` and `models/tokenizer.pkl`.")
        return

    # Text Input Area
    st.markdown("#### 📧 Suspicious Message / Email Content")
    
    default_text = PRESETS.get(selected_scenario, "")
    if not default_text:
        default_text = "URGENT: Your bank account will be suspended in 24 hours. Click the link to verify your identity."

    user_input = st.text_area(
        label="Paste email body, SMS, or notification text here:",
        value=default_text,
        height=130,
        placeholder="e.g. URGENT: Your PayPal account has been locked. Verify immediately..."
    )

    col_btn, col_empty = st.columns([0.25, 0.75])
    with col_btn:
        analyze_clicked = st.button("🔍 Analyze Email with ShieldAI", type="primary", use_container_width=True)

    # Analysis Execution
    if analyze_clicked or user_input.strip():
        if not user_input.strip():
            st.warning("Please enter or paste an email message to analyze.")
            return

        with st.spinner("Analyzing message with LSTM & generating LIME explanations..."):
            from lime.lime_text import LimeTextExplainer

            predict_fn = make_prediction_fn(model, tokenizer, max_len=200)
            
            # Predict single sample
            probabilities = predict_fn([user_input])[0]
            prob_safe = float(probabilities[0])
            prob_phishing = float(probabilities[1])
            threat_score = round(prob_phishing * 100, 2)

            # LIME Explainer
            explainer = LimeTextExplainer(
                class_names=["Legitimate / Safe", "Phishing / Malicious"],
                bow=True,
                random_state=42
            )
            exp = explainer.explain_instance(
                user_input,
                predict_fn,
                num_features=8,
                num_samples=100
            )

        st.markdown("---")
        
        # Layout: Left column (Gauge & Metrics) | Right column (XAI & Highlights)
        col_left, col_right = st.columns([0.42, 0.58], gap="large")

        with col_left:
            st.markdown("### 📊 Threat Assessment")
            
            # Status Badge
            if threat_score >= 70:
                badge_html = f"<div class='badge-critical'>🚨 CRITICAL THREAT: PHISHING DETECTED ({threat_score}%)</div>"
            elif threat_score >= 35:
                badge_html = f"<div class='badge-warning'>⚠️ SUSPICIOUS: ELEVATED RISK ({threat_score}%)</div>"
            else:
                badge_html = f"<div class='badge-safe'>✅ LEGITIMATE: MESSAGE APPEARS SAFE ({threat_score}%)</div>"
            
            st.markdown(badge_html, unsafe_allow_html=True)

            # Plotly Gauge Chart
            gauge_fig = render_threat_gauge(threat_score)
            st.plotly_chart(gauge_fig, use_container_width=True)

            # Metric Cards
            m1, m2 = st.columns(2)
            with m1:
                st.metric("Phishing Confidence", f"{prob_phishing * 100:.1f}%")
            with m2:
                st.metric("Legitimate Confidence", f"{prob_safe * 100:.1f}%")

            # Security Advice Card
            st.markdown("<div class='metric-card'>", unsafe_allow_html=True)
            st.markdown("#### 🛡️ Recommended Security Actions")
            if threat_score >= 70:
                st.markdown(
                    "- **Do NOT click** any embedded links or open attachments.\n"
                    "- **Do NOT provide** credentials, credit cards, or personal identifiers.\n"
                    "- Report to your organization's IT Security Operations Center (SOC).\n"
                    "- Block the sender domain."
                )
            elif threat_score >= 35:
                st.markdown(
                    "- Inspect URLs carefully by hovering over hyperlinks.\n"
                    "- Verify sender through an independent, verified contact channel."
                )
            else:
                st.markdown(
                    "- No malicious psychological triggers or threats detected.\n"
                    "- Standard security hygiene still applies."
                )
            st.markdown("</div>", unsafe_allow_html=True)

        with col_right:
            st.markdown("### 🧠 Explainable AI (LIME) Breakdown")
            st.markdown(
                "LIME uncovers the **exact psychological words & urgency triggers** that influenced the model's decision:"
            )

            # Trigger Words Extracted from LIME
            word_weights = exp.as_list()
            phishing_words = [w for w, weight in word_weights if weight > 0]
            safe_words = [w for w, weight in word_weights if weight < 0]

            col_p, col_s = st.columns(2)
            with col_p:
                st.markdown("**🚨 Threat Triggers (Phishing Signals):**")
                if phishing_words:
                    pills = "".join([f"<span class='trigger-pill-phish'>+{w}</span>" for w in phishing_words])
                    st.markdown(pills, unsafe_allow_html=True)
                else:
                    st.markdown("<span style='color:#64748b;'>None detected</span>", unsafe_allow_html=True)

            with col_s:
                st.markdown("**🛡️ Benign Signals (Safe Context):**")
                if safe_words:
                    pills = "".join([f"<span class='trigger-pill-safe'>{w}</span>" for w in safe_words])
                    st.markdown(pills, unsafe_allow_html=True)
                else:
                    st.markdown("<span style='color:#64748b;'>None detected</span>", unsafe_allow_html=True)

            st.markdown("<br>", unsafe_allow_html=True)
            st.markdown("#### 🔬 Interactive LIME Feature Contribution")

            # Render LIME's interactive HTML visualization
            lime_html = exp.as_html()
            components.html(lime_html, height=350, scrolling=True)


if __name__ == "__main__":
    main()

