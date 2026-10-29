"""
Automated end-to-end verification script for ShieldAI Phishing Detection System.
Tests model loading, inference prediction, Plotly gauge creation, and LIME XAI explanation.
"""

import pickle
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.sequence import pad_sequences
from lime.lime_text import LimeTextExplainer
import plotly.graph_objects as go

def run_tests():
    print("=" * 60)
    print("ShieldAI: Running Verification Pipeline")
    print("=" * 60)

    # 1. Load Model and Tokenizer
    model = load_model("models/phishing_lstm.keras")
    with open("models/tokenizer.pkl", "rb") as f:
        tokenizer = pickle.load(f)
    print("SUCCESS: Loaded model and tokenizer artifacts.")

    # 2. Prediction Function
    def predict_proba(texts):
        seqs = tokenizer.texts_to_sequences(texts)
        padded = pad_sequences(seqs, maxlen=200, padding="post", truncating="post")
        preds = model.predict(padded, verbose=0)
        preds = np.array(preds).reshape(-1, 1)
        return np.hstack([1.0 - preds, preds])

    test_text = "URGENT: Your bank account will be suspended in 24 hours. Click the link to verify your identity."
    probs = predict_proba([test_text])[0]
    threat_score = round(float(probs[1]) * 100, 2)

    print(f"\nTarget Email Content:\n\"{test_text}\"")
    print(f"Safe Probability:        {probs[0]:.4f}")
    print(f"Phishing Probability:    {probs[1]:.4f}")
    print(f"Threat Score:            {threat_score}%")
    assert threat_score >= 70, f"Expected high risk phishing score >= 70%, got {threat_score}%"
    print("SUCCESS: Phishing threat correctly classified as High Risk (>= 70%).")

    # 3. LIME Explainer
    print("\nRunning LIME Text Explainer (sampling perturbations)...")
    explainer = LimeTextExplainer(
        class_names=["Legitimate / Safe", "Phishing / Malicious"],
        bow=True,
        random_state=42
    )
    exp = explainer.explain_instance(test_text, predict_proba, num_features=8, num_samples=100)
    word_weights = exp.as_list()
    print("\nLIME Top Word Influences:")
    for word, weight in word_weights:
        direction = "PHISHING SIGNAL (+)" if weight > 0 else "SAFE SIGNAL (-)"
        print(f"  - '{word}': {weight:+.6f} ({direction})")

    html = exp.as_html()
    assert len(html) > 1000, "LIME HTML string generation failed"
    print(f"SUCCESS: LIME HTML visualization generated ({len(html)} bytes).")

    # 4. Plotly Gauge Construction
    fig = go.Figure(go.Indicator(
        mode="gauge+number",
        value=threat_score,
        title={"text": "THREAT PROBABILITY SCORE"}
    ))
    assert fig is not None, "Plotly gauge generation failed"
    print("SUCCESS: Plotly threat gauge generated successfully.")

    print("\n" + "=" * 60)
    print("ALL TESTS AND VERIFICATION CRITERIA PASSED!")
    print("=" * 60)

if __name__ == "__main__":
    run_tests()

