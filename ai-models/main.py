"""
ShieldAI Engine — AI-Powered Fraud & Phishing Detection Service

This FastAPI service provides the core ML inference pipeline for scanning
content (SMS, email, URLs, QR codes) and returning risk assessments.

Architecture:
- Uses a rule-based heuristic engine as baseline
- Designed to integrate DistilBERT/BERT for production NLP inference
- Returns risk scores (0-100), risk labels, and human-readable explanations
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional
import re
import hashlib
from datetime import datetime

app = FastAPI(
    title="ShieldAI Engine",
    description="AI-Powered Fraud & Phishing Detection API",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# CORS middleware for cross-service communication
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ============================================
# Request/Response Models
# ============================================

class ScanRequest(BaseModel):
    content: str = Field(..., min_length=1, max_length=10000, description="Content to analyze")
    channel: str = Field(..., description="Source channel: sms, email, url, qr, notification")


class ScanResponse(BaseModel):
    risk_score: int = Field(..., ge=0, le=100, description="Risk score from 0 (safe) to 100 (critical)")
    risk_label: str = Field(..., description="Risk classification: Safe, Low, Medium, High, Critical")
    explanation: str = Field(..., description="Human-readable explanation of the risk assessment")
    indicators: list[str] = Field(default_factory=list, description="Detected threat indicators")
    model_version: str = Field(default="v1.0.0-heuristic", description="AI model version used")
    scan_timestamp: str = Field(default_factory=lambda: datetime.utcnow().isoformat())


class HealthResponse(BaseModel):
    status: str = "healthy"
    model_loaded: bool = True
    version: str = "1.0.0"


# ============================================
# Threat Detection Engine (Heuristic Baseline)
# ============================================

# Known phishing/scam indicators
SUSPICIOUS_DOMAINS = [
    "bit.ly", "tinyurl.com", "goo.gl", "t.co", "ow.ly",
    "rb.gy", "cutt.ly", "is.gd", "v.gd", "shorte.st",
]

PHISHING_KEYWORDS = [
    "urgent", "verify your account", "click here immediately",
    "suspended", "unusual activity", "confirm your identity",
    "prize", "winner", "congratulations", "claim your reward",
    "act now", "limited time", "expire", "password reset",
    "bank account", "social security", "credit card",
    "wire transfer", "western union", "bitcoin",
    "nigerian prince", "inheritance", "lottery",
    "free gift", "you have been selected", "dear customer",
    "security alert", "unauthorized access", "update your information",
    "otp", "one-time password", "kyc", "aadhar", "pan card",
]

SCAM_PATTERNS = [
    r"(?:https?://)?(?:\d{1,3}\.){3}\d{1,3}",  # IP-based URLs
    r"(?:https?://)?[a-z0-9]+\.[a-z]{2,3}\.[a-z]{2}",  # Suspicious subdomains
    r"\b\d{4}[\s-]?\d{4}[\s-]?\d{4}[\s-]?\d{4}\b",  # Credit card patterns
    r"(?:password|pwd|passwd)\s*[:=]\s*\S+",  # Password in plaintext
    r"(?:ssn|social\s*security)\s*[:=]?\s*\d{3}",  # SSN patterns
    r"(?:whatsapp|telegram|signal)\s*(?:group|join|click)",  # Social engineering
]


def analyze_content(content: str, channel: str) -> ScanResponse:
    """
    Heuristic-based content analysis engine.
    In production, this would invoke a fine-tuned DistilBERT model.
    """
    content_lower = content.lower()
    indicators = []
    risk_score = 0

    # 1. Check for URL shorteners
    for domain in SUSPICIOUS_DOMAINS:
        if domain in content_lower:
            indicators.append(f"URL shortener detected: {domain}")
            risk_score += 15

    # 2. Check for phishing keywords
    matched_keywords = []
    for keyword in PHISHING_KEYWORDS:
        if keyword in content_lower:
            matched_keywords.append(keyword)
            risk_score += 8

    if matched_keywords:
        indicators.append(f"Phishing keywords: {', '.join(matched_keywords[:5])}")

    # 3. Check for scam patterns (regex)
    for pattern in SCAM_PATTERNS:
        matches = re.findall(pattern, content_lower)
        if matches:
            indicators.append(f"Suspicious pattern detected: {pattern[:40]}...")
            risk_score += 20

    # 4. Channel-specific heuristics
    if channel == "sms":
        # SMS-specific: check for urgency + link combo
        has_url = bool(re.search(r"https?://\S+", content_lower))
        has_urgency = any(w in content_lower for w in ["urgent", "immediately", "expire", "act now"])
        if has_url and has_urgency:
            indicators.append("SMS with urgent language + link (high-risk combo)")
            risk_score += 25

    elif channel == "email":
        # Email: check for spoofing indicators
        if "from:" in content_lower and "reply-to:" in content_lower:
            indicators.append("Potential email header spoofing detected")
            risk_score += 15

    elif channel == "url":
        # URL: check for IP-based or lookalike domains
        if re.search(r"(?:https?://)?(?:\d{1,3}\.){3}\d{1,3}", content):
            indicators.append("IP-based URL detected (common phishing tactic)")
            risk_score += 30

    # 5. Content length heuristic (very short urgent messages are suspicious)
    if len(content) < 100 and risk_score > 0:
        indicators.append("Short message with threat indicators (higher suspicion)")
        risk_score += 10

    # Cap at 100
    risk_score = min(risk_score, 100)

    # Map to risk label
    if risk_score <= 10:
        risk_label = "Safe"
    elif risk_score <= 30:
        risk_label = "Low"
    elif risk_score <= 60:
        risk_label = "Medium"
    elif risk_score <= 85:
        risk_label = "High"
    else:
        risk_label = "Critical"

    # Build explanation
    if not indicators:
        explanation = "No malicious indicators detected. Content appears safe."
    else:
        explanation = (
            f"Detected {len(indicators)} threat indicator(s). "
            f"Risk assessment: {risk_label} ({risk_score}/100). "
            f"Primary concerns: {'; '.join(indicators[:3])}."
        )

    return ScanResponse(
        risk_score=risk_score,
        risk_label=risk_label,
        explanation=explanation,
        indicators=indicators,
        model_version="v1.0.0-heuristic",
    )


# ============================================
# API Endpoints
# ============================================

@app.post("/api/v1/scan", response_model=ScanResponse)
async def scan_content(request: ScanRequest):
    """
    Analyze content for phishing, scam, and fraud indicators.
    Returns a risk score (0-100) and detailed explanation.
    """
    try:
        result = analyze_content(request.content, request.channel)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")


@app.get("/api/v1/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint for service discovery and monitoring."""
    return HealthResponse()


@app.get("/")
async def root():
    return {
        "service": "ShieldAI Engine",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs",
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")
