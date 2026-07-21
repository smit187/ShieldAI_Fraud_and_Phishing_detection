# ShieldAI Environment Setup Script
# Creates a Python virtual environment and installs service dependencies.

$ErrorActionPreference = "Stop"
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "ShieldAI AI Engine Environment Setup" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Check Python installation
try {
    $pythonVersion = python --version 2>&1
    Write-Host "Found Python: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "Error: Python is not installed or not in PATH." -ForegroundColor Red
    Exit 1
}

# 2. Create Virtual Environment
if (Test-Path ".venv") {
    Write-Host "Virtual environment '.venv' already exists. Skipping creation." -ForegroundColor Yellow
} else {
    Write-Host "Creating virtual environment '.venv'..." -ForegroundColor Blue
    python -m venv .venv
    Write-Host "Virtual environment created successfully." -ForegroundColor Green
}

# 3. Upgrade Pip inside Virtual Environment
Write-Host "Upgrading pip..." -ForegroundColor Blue
& .\.venv\Scripts\python.exe -m pip install --upgrade pip

# 4. Install Dependencies
# We attempt to install full requirements. If torch/transformers installation fails
# due to network bandwidth or disk limits, we fall back to core server packages (fastapi, uvicorn, pydantic)
# since the baseline heuristic model does not require torch/transformers to execute.
Write-Host "Installing dependencies from requirements.txt..." -ForegroundColor Blue
try {
    & .\.venv\Scripts\pip install -r requirements.txt
    Write-Host "All dependencies installed successfully!" -ForegroundColor Green
} catch {
    Write-Host "Full installation failed. Attempting lightweight fallback installation..." -ForegroundColor Yellow
    try {
        & .\.venv\Scripts\pip install fastapi uvicorn pydantic
        Write-Host "Core packages installed successfully. You can run the heuristic baseline engine." -ForegroundColor Green
    } catch {
        Write-Host "Error: Fallback installation failed." -ForegroundColor Red
        Exit 1
    }
}

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Setup Completed Successfully!" -ForegroundColor Green
Write-Host "To start the FastAPI service, run:" -ForegroundColor Green
Write-Host "  .\\.venv\\Scripts\\python.exe -m uvicorn main:app --reload" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan
