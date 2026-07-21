// ==========================================================================
// ShieldAI Dashboard Controller Logic
// Handles real-time metric counters, logs stream filtering, and threat simulation
// ==========================================================================

document.addEventListener("DOMContentLoaded", () => {
    // Initialize Lucide Icons
    lucide.createIcons();

    // App State
    let totalScans = 1248390;
    let threatsBlocked = 14832;
    let scanHistory = [];
    let isLiveStreaming = true;

    // Presets
    const presets = {
        sms_bank: {
            channel: "sms",
            content: "SECURITY ALERT: Unusual login activity detected on your Chase Account. Please verify your identity immediately to prevent suspension: https://bit.ly/chse-auth-net"
        },
        url_phish: {
            channel: "url",
            content: "http://182.16.24.11/paypal-update/verification.php?token=493"
        },
        safe_text: {
            channel: "sms",
            content: "Hi Mom, I am on my way home now. Can you please check if the packages arrived?"
        }
    };

    // Suspicious terms & heuristics mirroring the Python backend model
    const SUSPICIOUS_DOMAINS = ["bit.ly", "tinyurl.com", "goo.gl", "t.co", "rb.gy", "cutt.ly"];
    const PHISHING_KEYWORDS = [
        "urgent", "verify your account", "click here immediately", "suspended", 
        "unusual activity", "confirm your identity", "prize", "winner", 
        "congratulations", "claim your reward", "password reset", "otp", "kyc"
    ];

    // ==========================================
    // UI Selectors
    // ==========================================
    const totalScansEl = document.getElementById("metricTotalScans");
    const threatsBlockedEl = document.getElementById("metricThreatsBlocked");
    const scanLogBody = document.getElementById("scanLogBody");
    const searchInput = document.getElementById("searchInput");
    const channelFilter = document.getElementById("channelFilter");
    const btnSimulate = document.getElementById("btnSimulate");
    const btnAnalyzeSim = document.getElementById("btnAnalyzeSim");
    const simChannel = document.getElementById("simChannel");
    const simContent = document.getElementById("simContent");

    // Modal elements
    const detailModal = document.getElementById("detailModal");
    const modalClose = document.getElementById("modalClose");
    const modalRiskGauge = document.getElementById("modalRiskGauge");
    const modalRiskValue = document.getElementById("modalRiskValue");
    const modalClassification = document.getElementById("modalClassification");
    const modalChannel = document.getElementById("modalChannel");
    const modalContent = document.getElementById("modalContent");
    const modalExplanation = document.getElementById("modalExplanation");
    const modalIndicators = document.getElementById("modalIndicators");

    // ==========================================
    // Core Engine Logic (Mirrors main.py heuristics)
    // ==========================================
    function analyzeContent(content, channel) {
        const contentLower = content.toLowerCase();
        let indicators = [];
        let score = 0;

        // 1. Check for URL shorteners
        SUSPICIOUS_DOMAINS.forEach(domain => {
            if (contentLower.includes(domain)) {
                indicators.push(`URL shortener detected: ${domain}`);
                score += 15;
            }
        });

        // 2. Check for keywords
        let matchedKeywords = [];
        PHISHING_KEYWORDS.forEach(keyword => {
            if (contentLower.includes(keyword)) {
                matchedKeywords.push(keyword);
                score += 8;
            }
        });
        if (matchedKeywords.length > 0) {
            indicators.push(`Phishing keywords: ${matchedKeywords.slice(0, 3).join(", ")}`);
        }

        // 3. Regex checks
        if (/(?:https?:\/\/)?(?:\d{1,3}\.){3}\d{1,3}/.test(contentLower)) {
            indicators.push("IP-based URL detected (common phishing host)");
            score += 30;
        }
        if (/\b\d{4}[\s-]?\d{4}[\s-]?\d{4}[\s-]?\d{4}\b/.test(contentLower)) {
            indicators.push("Plaintext Credit Card number pattern detected");
            score += 25;
        }

        // 4. Channel-specific combos
        if (channel === "sms") {
            const hasUrl = /https?:\/\/\S+/.test(contentLower);
            const hasUrgency = ["urgent", "immediately", "expire", "act now"].some(w => contentLower.includes(w));
            if (hasUrl && hasUrgency) {
                indicators.push("SMS containing both urgent language & link (high-risk indicator)");
                score += 25;
            }
        }

        score = Math.min(score, 100);

        let label = "SAFE";
        if (score > 10) label = "LOW";
        if (score > 30) label = "MEDIUM";
        if (score > 60) label = "HIGH";
        if (score > 85) label = "CRITICAL";

        let explanation = "";
        if (indicators.length === 0) {
            explanation = "This content passed all heuristic check gates. No malicious links, keywords, or phishing patterns were found. Risk is minimal.";
        } else {
            explanation = `Threat engine flagged ${indicators.length} primary risk factors. Risk score assigned is ${score}/100. Primary vulnerabilities: ${indicators.join("; ")}. Recommend blocking communication.`;
        }

        return {
            id: generateUUID(),
            timestamp: new Date().toLocaleTimeString(),
            channel: channel,
            content: content,
            score: score,
            label: label,
            explanation: explanation,
            indicators: indicators
        };
    }

    function generateUUID() {
        return 'xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    // ==========================================
    // Seed Historical Data
    // ==========================================
    function initSeedData() {
        const seedScans = [
            { channel: "sms", content: "Hey, can you send me the slide deck for tomorrow's demo?" },
            { channel: "url", content: "https://github.com/google/guava" },
            { channel: "email", content: "URGENT ACTION REQUIRED: Verify your employee profile within 24 hours to avoid system lockout: http://bit.ly/company-verify-sso" },
            { channel: "qr", content: "WIFI:S:HomeNetwork;T:WPA;P:SecretPassword;;" },
            { channel: "url", content: "http://91.198.174.192/secure-login/signin.htm" },
            { channel: "sms", content: "CONGRATS! You have won a $1,000 Amazon Gift Card. Claim your reward immediately: http://cutt.ly/amzn-claim-reward" }
        ];

        seedScans.forEach(scan => {
            const result = analyzeContent(scan.content, scan.channel);
            // shift timestamps slightly back
            result.timestamp = new Date(Date.now() - Math.random() * 300000).toLocaleTimeString();
            scanHistory.unshift(result);
        });

        renderTable();
    }

    // ==========================================
    // Render Functions
    // ==========================================
    function renderTable() {
        const filterVal = channelFilter.value;
        const searchVal = searchInput.value.toLowerCase();

        scanLogBody.innerHTML = "";

        const filtered = scanHistory.filter(scan => {
            const matchesFilter = filterVal === "all" || scan.channel === filterVal;
            const matchesSearch = scan.content.toLowerCase().includes(searchVal) || 
                                  scan.label.toLowerCase().includes(searchVal);
            return matchesFilter && matchesSearch;
        });

        filtered.forEach(scan => {
            const tr = document.createElement("tr");
            tr.id = `row-${scan.id}`;
            
            // Add flash style if newly simulated
            if (scan.isNew) {
                tr.classList.add("threat-flash-alert");
                setTimeout(() => {
                    tr.classList.remove("threat-flash-alert");
                }, 3000);
                scan.isNew = false;
            }

            const channelIcon = getChannelIcon(scan.channel);
            
            tr.innerHTML = `
                <td>${scan.timestamp}</td>
                <td>
                    <span class="channel-tag">
                        <i data-lucide="${channelIcon}"></i>
                        ${scan.channel}
                    </span>
                </td>
                <td>
                    <div class="content-snippet" title="${escapeHtml(scan.content)}">
                        ${escapeHtml(scan.content)}
                    </div>
                </td>
                <td class="risk-cell" style="color: ${getRiskColor(scan.score)}">${scan.score}</td>
                <td>
                    <span class="badge ${scan.label.toLowerCase()}">${scan.label}</span>
                </td>
                <td>
                    <button class="btn-detail" data-id="${scan.id}">
                        <i data-lucide="external-link"></i> Detail
                    </button>
                </td>
            `;

            scanLogBody.appendChild(tr);
        });

        lucide.createIcons();
        bindDetailButtons();
    }

    function getChannelIcon(channel) {
        switch(channel) {
            case "sms": return "message-square";
            case "email": return "mail";
            case "url": return "link";
            case "qr": return "qr-code";
            default: return "shield";
        }
    }

    function getRiskColor(score) {
        if (score <= 10) return "var(--status-safe)";
        if (score <= 30) return "var(--status-low)";
        if (score <= 60) return "var(--status-medium)";
        if (score <= 85) return "var(--status-high)";
        return "var(--status-critical)";
    }

    function escapeHtml(text) {
        return text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // ==========================================
    // Telemetry Stream Simulator
    // ==========================================
    setInterval(() => {
        if (!isLiveStreaming) return;

        // Increment overall scan counts
        const addition = Math.floor(Math.random() * 4) + 1;
        totalScans += addition;
        totalScansEl.textContent = totalScans.toLocaleString();

        // 10% chance to generate a live traffic threat
        if (Math.random() < 0.12) {
            const channels = ["sms", "url", "email", "qr"];
            const selectedChannel = channels[Math.floor(Math.random() * channels.length)];
            const trafficPresets = [
                "Hello, hope you are doing well today!",
                "Check out this cool repository I found on Github: https://github.com",
                "Your package delivery failed. Complete details immediately at: https://rb.gy/dhl-track-93",
                "Please scan the QR code to connect to public terminal router wifi.",
                "URGENT: Suspicious transaction of $499.00 on card ending 4039. Reply block to verify."
            ];
            const content = trafficPresets[Math.floor(Math.random() * trafficPresets.length)];
            
            const scanResult = analyzeContent(content, selectedChannel);
            scanResult.isNew = true;
            
            if (scanResult.score > 30) {
                threatsBlocked++;
                threatsBlockedEl.textContent = threatsBlocked.toLocaleString();
            }

            scanHistory.unshift(scanResult);
            // Cap history
            if (scanHistory.length > 50) scanHistory.pop();

            renderTable();
        }
    }, 2500);

    // ==========================================
    // Modal Drawer Details Event Binding
    // ==========================================
    function bindDetailButtons() {
        document.querySelectorAll(".btn-detail").forEach(btn => {
            btn.onclick = (e) => {
                const scanId = btn.getAttribute("data-id");
                const scan = scanHistory.find(s => s.id === scanId);
                if (scan) {
                    openDetailDrawer(scan);
                }
            };
        });
    }

    function openDetailDrawer(scan) {
        modalChannel.textContent = scan.channel;
        modalRiskValue.textContent = scan.score;
        modalClassification.textContent = scan.label;
        modalClassification.className = `badge ${scan.label.toLowerCase()}`;
        modalContent.textContent = scan.content;
        modalExplanation.textContent = scan.explanation;

        // Draw indicators
        modalIndicators.innerHTML = "";
        if (scan.indicators.length === 0) {
            modalIndicators.innerHTML = "<li>No indicators flagged. Content appears clean.</li>";
        } else {
            scan.indicators.forEach(ind => {
                const li = document.createElement("li");
                li.textContent = ind;
                modalIndicators.appendChild(li);
            });
        }

        // Risk gauge circle path animation
        const percent = scan.score;
        const color = getRiskColor(percent);
        modalRiskGauge.style.stroke = color;
        
        // Circular gauge dashoffset: stroke-dasharray = 283 (circumference)
        const offset = 283 - (283 * percent / 100);
        modalRiskGauge.style.strokeDashoffset = offset;

        // Show drawer
        detailModal.classList.add("active");
    }

    modalClose.onclick = () => {
        detailModal.classList.remove("active");
    };

    // Close on overlay clicking
    detailModal.onclick = (e) => {
        if (e.target === detailModal) {
            detailModal.classList.remove("active");
        }
    };

    // ==========================================
    // Interactive Simulator Config & Trigger
    // ==========================================
    window.setPreset = function(presetKey) {
        const preset = presets[presetKey];
        if (preset) {
            simChannel.value = preset.channel;
            simContent.value = preset.content;
        }
    };

    btnSimulate.onclick = () => {
        // Toggle simulator scroll view or focus
        simContent.scrollIntoView({ behavior: 'smooth' });
        simContent.focus();
    };

    btnAnalyzeSim.onclick = () => {
        const text = simContent.value.trim();
        const channelVal = simChannel.value;

        if (!text) {
            alert("Please type or choose a simulated threat payload to scan.");
            return;
        }

        // Run client-side analysis
        const scanResult = analyzeContent(text, channelVal);
        scanResult.isNew = true;

        if (scanResult.score > 30) {
            threatsBlocked++;
            threatsBlockedEl.textContent = threatsBlocked.toLocaleString();
        }

        totalScans++;
        totalScansEl.textContent = totalScans.toLocaleString();

        scanHistory.unshift(scanResult);
        renderTable();

        // Automatically open detailed drawer report of simulated threat
        setTimeout(() => {
            openDetailDrawer(scanResult);
        }, 300);

        // Reset text area
        simContent.value = "";
    };

    // Event listener for filters
    channelFilter.onchange = renderTable;
    searchInput.oninput = renderTable;

    // Run Initializer
    initSeedData();
});
