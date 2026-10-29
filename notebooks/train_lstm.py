"""
ShieldAI - Phishing Email & Scam Detection Engine
Training pipeline for LSTM text classification model.
Dataset: ealvaradob/phishing-dataset (HuggingFace)
"""

import os
import json
import pickle
import numpy as np
import pandas as pd
from pathlib import Path
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix

import tensorflow as tf
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Embedding, LSTM, Dense, Dropout, SpatialDropout1D
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
from huggingface_hub import hf_hub_download

# Configuration
VOCAB_SIZE = 10000
MAX_LEN = 200
EMBEDDING_DIM = 64
LSTM_UNITS = 64
BATCH_SIZE = 64
EPOCHS = 5
RANDOM_STATE = 42

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"
MODELS_DIR = BASE_DIR / "models"

DATA_DIR.mkdir(parents=True, exist_ok=True)
MODELS_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = MODELS_DIR / "phishing_lstm.keras"
TOKENIZER_PATH = MODELS_DIR / "tokenizer.pkl"
CONFIG_PATH = MODELS_DIR / "config.json"
DATASET_FILE = DATA_DIR / "phishing_texts.json"


def download_dataset() -> pd.DataFrame:
    """Download ealvaradob/phishing-dataset from HuggingFace and cache to data directory."""
    print("=" * 60)
    print("STEP 1: Fetching HuggingFace Dataset [ealvaradob/phishing-dataset]")
    print("=" * 60)
    
    if DATASET_FILE.exists():
        print(f"Loading cached dataset from: {DATASET_FILE}")
        with open(DATASET_FILE, "r", encoding="utf-8") as f:
            data = json.load(f)
    else:
        print("Downloading texts.json from Hugging Face Hub...")
        downloaded_path = hf_hub_download(
            repo_id="ealvaradob/phishing-dataset",
            filename="texts.json",
            repo_type="dataset",
        )
        print(f"Downloaded snapshot to: {downloaded_path}")
        with open(downloaded_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        
        # Save a local copy in data/
        with open(DATASET_FILE, "w", encoding="utf-8") as f:
            json.dump(data, f)
        print(f"Cached copy saved to: {DATASET_FILE}")

    df = pd.DataFrame(data)
    print(f"Raw dataset shape: {df.shape}")
    
    # Cleaning
    df = df.dropna(subset=["text", "label"])
    df["text"] = df["text"].astype(str).str.strip()
    df = df[df["text"].str.len() > 10]
    df["label"] = df["label"].astype(int)
    
    print(f"Cleaned dataset shape: {df.shape}")
    print(f"Class distribution:\n{df['label'].value_counts()}")
    return df


def preprocess_data(df: pd.DataFrame):
    """Tokenize and pad sequences for training."""
    print("\n" + "=" * 60)
    print("STEP 2: Preprocessing Text (Tokenization & Padding)")
    print("=" * 60)
    
    # Train / Test Split
    texts_list = df["text"].astype(str).tolist()
    labels_list = df["label"].astype(int).tolist()

    train_texts, test_texts, train_labels, test_labels = train_test_split(
        texts_list,
        labels_list,
        test_size=0.20,
        random_state=RANDOM_STATE,
        stratify=labels_list,
    )
    print(f"Training samples: {len(train_texts)} | Testing samples: {len(test_texts)}")

    # Fit Tokenizer
    tokenizer = Tokenizer(num_words=VOCAB_SIZE, oov_token="<OOV>", lower=True)
    tokenizer.fit_on_texts(train_texts)

    # Convert to sequences and pad
    X_train_seq = tokenizer.texts_to_sequences(train_texts)
    X_test_seq = tokenizer.texts_to_sequences(test_texts)

    X_train_pad = pad_sequences(X_train_seq, maxlen=MAX_LEN, padding="post", truncating="post")
    X_test_pad = pad_sequences(X_test_seq, maxlen=MAX_LEN, padding="post", truncating="post")

    y_train = np.array(train_labels, dtype=np.float32)
    y_test = np.array(test_labels, dtype=np.float32)

    print(f"X_train padded shape: {X_train_pad.shape}")
    print(f"X_test padded shape:  {X_test_pad.shape}")

    return X_train_pad, X_test_pad, y_train, y_test, tokenizer, test_texts


def build_lstm_model():
    """Build Sequential LSTM Neural Network."""
    print("\n" + "=" * 60)
    print("STEP 3: Building Sequential LSTM Model")
    print("=" * 60)

    model = Sequential([
        Embedding(input_dim=VOCAB_SIZE, output_dim=EMBEDDING_DIM, mask_zero=False),
        SpatialDropout1D(0.2),
        LSTM(LSTM_UNITS, dropout=0.2, recurrent_dropout=0.0),
        Dense(32, activation="relu"),
        Dropout(0.3),
        Dense(1, activation="sigmoid")
    ], name="ShieldAI_Phishing_LSTM")

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
        loss="binary_crossentropy",
        metrics=["accuracy", tf.keras.metrics.Precision(name="precision"), tf.keras.metrics.Recall(name="recall")]
    )

    model.summary()
    return model


def train_and_evaluate(model, X_train, y_train, X_test, y_test, tokenizer, test_raw_texts):
    """Train model, evaluate metrics, and save artifacts."""
    print("\n" + "=" * 60)
    print("STEP 4: Training LSTM Network")
    print("=" * 60)

    callbacks = [
        EarlyStopping(monitor="val_loss", patience=2, restore_best_weights=True, verbose=1),
        ModelCheckpoint(filepath=str(MODEL_PATH), monitor="val_loss", save_best_only=True, verbose=1)
    ]

    history = model.fit(
        X_train,
        y_train,
        validation_split=0.15,
        epochs=EPOCHS,
        batch_size=BATCH_SIZE,
        callbacks=callbacks,
        verbose=1
    )

    print("\n" + "=" * 60)
    print("STEP 5: Evaluation on Held-Out Test Set")
    print("=" * 60)

    test_preds_prob = model.predict(X_test, batch_size=BATCH_SIZE, verbose=0)
    test_preds = (test_preds_prob >= 0.5).astype(int).flatten()

    acc = accuracy_score(y_test, test_preds)
    print(f"\nFinal Test Accuracy: {acc * 100:.2f}%")
    print("\nClassification Report:\n", classification_report(y_test, test_preds, target_names=["Safe (0)", "Phishing (1)"]))
    print("Confusion Matrix:\n", confusion_matrix(y_test, test_preds))

    # Save Tokenizer and Model
    print("\n" + "=" * 60)
    print("STEP 6: Saving Model & Tokenizer Artifacts")
    print("=" * 60)

    model.save(MODEL_PATH)
    print(f"Model saved to: {MODEL_PATH}")

    with open(TOKENIZER_PATH, "wb") as f:
        pickle.dump(tokenizer, f, protocol=pickle.HIGHEST_PROTOCOL)
    print(f"Tokenizer artifact saved to: {TOKENIZER_PATH}")

    # Save Config Metadata
    config = {
        "vocab_size": VOCAB_SIZE,
        "max_len": MAX_LEN,
        "embedding_dim": EMBEDDING_DIM,
        "lstm_units": LSTM_UNITS,
        "accuracy": float(acc)
    }
    with open(CONFIG_PATH, "w", encoding="utf-8") as f:
        json.dump(config, f, indent=2)
    print(f"Configuration metadata saved to: {CONFIG_PATH}")
    print(f"Model weights saved to: {MODEL_PATH}")

    # Verify Inference
    print("\n" + "=" * 60)
    print("STEP 7: Verifying Test Inference")
    print("=" * 60)
    sample_text = "URGENT: Your bank account will be suspended in 24 hours. Click the link to verify your identity."
    seq = tokenizer.texts_to_sequences([sample_text])
    padded = pad_sequences(seq, maxlen=MAX_LEN, padding="post", truncating="post")
    prob = float(model.predict(padded, verbose=0)[0][0])
    label = "PHISHING / MALICIOUS" if prob >= 0.5 else "LEGITIMATE / SAFE"
    print(f"Test Input: '{sample_text}'")
    print(f"Threat Probability: {prob * 100:.2f}% -> Classification: {label}")


if __name__ == "__main__":
    df = download_dataset()
    X_train, X_test, y_train, y_test, tokenizer, test_raw_texts = preprocess_data(df)
    model = build_lstm_model()
    train_and_evaluate(model, X_train, y_train, X_test, y_test, tokenizer, test_raw_texts)
    print("\nTraining Pipeline Completed Successfully!")
