// Auth check
const token = localStorage.getItem('token');
if (!token) window.location.href = '/login.html';
document.getElementById('userName').textContent = localStorage.getItem('fullName') || 'User';

const API = (url, opts = {}) => fetch(url, {
    ...opts,
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token, ...opts.headers }
}).then(r => { if (r.status === 401 || r.status === 403) { logout(); } return r; });

let currentResult = null;
let healthChart = null;

// Tabs
function showTab(tab) {
    document.querySelectorAll('[id^="tab-"]').forEach(el => el.style.display = 'none');
    document.getElementById('tab-' + tab).style.display = 'block';
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    if (tab === 'history') loadHistory();
}

// Theme
function toggleTheme() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    document.documentElement.setAttribute('data-theme', isDark ? '' : 'dark');
    document.querySelector('.theme-toggle').textContent = isDark ? '🌙' : '☀️';
    localStorage.setItem('theme', isDark ? 'light' : 'dark');
}
if (localStorage.getItem('theme') === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark');
    document.querySelector('.theme-toggle').textContent = '☀️';
}

function logout() { localStorage.clear(); window.location.href = '/login.html'; }

// Lab Form
document.getElementById('labForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('analyzeBtn');
    btn.innerHTML = '<span class="loader"></span> Analyzing...';

    const fields = ['hemoglobin','rbc','wbc','platelets','bloodSugar','vitaminD','vitaminB12','iron','calcium','cholesterol','tsh','height','weight'];
    const body = {};
    fields.forEach(f => {
        const v = document.getElementById(f).value;
        if (v) body[f] = parseFloat(v);
    });

    try {
        const res = await API('/api/analyze', { method: 'POST', body: JSON.stringify(body) });
        const data = await res.json();
        if (res.ok) {
            currentResult = data;
            renderResults(data);
            document.querySelectorAll('.tab-btn')[1].click();
        } else { alert(data.error || 'Analysis failed'); }
    } catch (err) { alert('Network error'); }
    btn.innerHTML = '🔍 Analyze Health';
});

function renderResults(data) {
    const c = document.getElementById('resultsContent');
    const scoreClass = data.healthScore >= 80 ? 'score-high' : data.healthScore >= 50 ? 'score-medium' : 'score-low';

    let html = `
        <div class="grid-2">
            <div class="card" style="text-align:center">
                <h2 style="margin-bottom:16px;color:var(--primary)">Health Score</h2>
                <div class="score-circle ${scoreClass}">
                    ${data.healthScore}<small>/100</small>
                </div>
                <p style="font-size:1.1rem;font-weight:600;color:var(--text-secondary)">Risk Level: 
                    <span style="color:${data.overallRisk==='Low'?'var(--success)':data.overallRisk==='Medium'?'var(--warning)':'var(--danger)'}">${data.overallRisk}</span>
                </p>
            </div>
            <div class="card" style="text-align:center">
                <h2 style="margin-bottom:16px;color:var(--primary)">BMI</h2>
                <div style="font-size:3rem;font-weight:800;color:var(--primary)">${data.bmi}</div>
                <p style="font-size:1.2rem;margin-top:8px;color:var(--text-secondary)">${data.bmiCategory}</p>
            </div>
        </div>

        <div class="card">
            <h2 style="margin-bottom:16px;color:var(--primary)">Parameter Results</h2>
            <div class="grid-4">
                ${data.parameters.map(p => `
                    <div class="health-card ${p.status.toLowerCase()}">
                        <h3>${p.name}</h3>
                        <div class="value">${p.value}</div>
                        <div class="status">${p.status} ${p.unit}</div>
                        ${p.percentDifference > 0 ? `<div style="font-size:0.75rem;margin-top:4px">${p.percentDifference}% ${p.status === 'Low' ? 'below' : 'above'} range</div>` : ''}
                    </div>
                `).join('')}
            </div>
        </div>

        <div class="card">
            <h2 style="margin-bottom:16px;color:var(--primary)">📊 Parameter Chart</h2>
            <div class="chart-container"><canvas id="healthChart"></canvas></div>
        </div>

        <div class="grid-2">
            <div class="card">
                <h2 style="margin-bottom:12px;color:var(--success)">🥗 Diet Recommendations</h2>
                <ul class="rec-list">${data.dietRecommendations.map(r => `<li>${r}</li>`).join('')}</ul>
            </div>
            <div class="card">
                <h2 style="margin-bottom:12px;color:var(--primary)">🏋️ Exercise Recommendations</h2>
                <ul class="rec-list">${data.exerciseRecommendations.map(r => `<li>${r}</li>`).join('')}</ul>
            </div>
        </div>
        <div class="card">
            <h2 style="margin-bottom:12px;color:var(--accent)">🧘 Lifestyle Recommendations</h2>
            <ul class="rec-list">${data.lifestyleRecommendations.map(r => `<li>${r}</li>`).join('')}</ul>
        </div>

        <div style="text-align:center;margin:20px 0">
            <button class="btn btn-primary" onclick="exportPDF()">📄 Download PDF Report</button>
        </div>
    `;
    c.innerHTML = html;
    renderChart(data.parameters);
}

function renderChart(params) {
    const ctx = document.getElementById('healthChart');
    if (!ctx) return;
    if (healthChart) healthChart.destroy();

    const colors = params.map(p => p.status === 'Normal' ? '#4CAF50' : p.status === 'Low' ? '#FF9800' : '#F44336');

    healthChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: params.map(p => p.name),
            datasets: [{
                label: 'Your Value',
                data: params.map(p => p.value),
                backgroundColor: colors.map(c => c + '33'),
                borderColor: colors,
                borderWidth: 2,
                borderRadius: 6
            }, {
                label: 'Normal Min',
                data: params.map(p => p.normalMin),
                type: 'line',
                borderColor: '#4CAF50',
                borderDash: [5, 5],
                pointRadius: 3,
                fill: false
            }, {
                label: 'Normal Max',
                data: params.map(p => p.normalMax),
                type: 'line',
                borderColor: '#F44336',
                borderDash: [5, 5],
                pointRadius: 3,
                fill: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'top' } },
            scales: { y: { beginAtZero: true } }
        }
    });
}

async function loadHistory() {
    const div = document.getElementById('historyContent');
    try {
        const res = await API('/api/history');
        const data = await res.json();
        if (!data.length) { div.innerHTML = '<p style="color:var(--text-secondary)">No analysis history yet.</p>'; return; }
        div.innerHTML = `<table class="history-table">
            <thead><tr><th>Date</th><th>Health Score</th><th>BMI</th><th>Risk</th></tr></thead>
            <tbody>${data.map(d => `<tr>
                <td>${new Date(d.analyzedAt).toLocaleString()}</td>
                <td><strong>${d.healthScore}</strong>/100</td>
                <td>${d.bmi} (${d.bmiCategory})</td>
                <td style="color:${d.overallRisk==='Low'?'var(--success)':d.overallRisk==='Medium'?'var(--warning)':'var(--danger)'}"><strong>${d.overallRisk}</strong></td>
            </tr>`).join('')}</tbody>
        </table>`;
    } catch (e) { div.innerHTML = '<p style="color:var(--danger)">Failed to load history.</p>'; }
}

function exportPDF() {
    if (!currentResult) return;
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    const d = currentResult;
    let y = 20;

    doc.setFontSize(20);
    doc.setTextColor(21, 101, 192);
    doc.text('Medical Health Analyzer Report', 105, y, { align: 'center' });
    y += 10;
    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text('Generated: ' + new Date().toLocaleString(), 105, y, { align: 'center' });
    y += 15;

    doc.setFontSize(14);
    doc.setTextColor(0);
    doc.text(`Health Score: ${d.healthScore}/100  |  BMI: ${d.bmi} (${d.bmiCategory})  |  Risk: ${d.overallRisk}`, 14, y);
    y += 12;

    doc.setFontSize(12);
    doc.setTextColor(21, 101, 192);
    doc.text('Parameter Results', 14, y);
    y += 8;

    doc.setFontSize(9);
    doc.setTextColor(0);
    d.parameters.forEach(p => {
        doc.text(`${p.name}: ${p.value} ${p.unit} — ${p.status} (${p.riskLevel} risk${p.percentDifference > 0 ? ', ' + p.percentDifference + '% off' : ''})`, 14, y);
        y += 6;
        if (y > 270) { doc.addPage(); y = 20; }
    });

    y += 6;
    doc.setTextColor(21, 101, 192);
    doc.setFontSize(12);
    doc.text('Diet Recommendations', 14, y); y += 7;
    doc.setFontSize(9); doc.setTextColor(0);
    d.dietRecommendations.forEach(r => {
        const lines = doc.splitTextToSize('• ' + r, 180);
        doc.text(lines, 14, y); y += lines.length * 5;
        if (y > 270) { doc.addPage(); y = 20; }
    });

    y += 6;
    doc.setTextColor(21, 101, 192); doc.setFontSize(12);
    doc.text('Exercise Recommendations', 14, y); y += 7;
    doc.setFontSize(9); doc.setTextColor(0);
    d.exerciseRecommendations.forEach(r => {
        const lines = doc.splitTextToSize('• ' + r, 180);
        doc.text(lines, 14, y); y += lines.length * 5;
        if (y > 270) { doc.addPage(); y = 20; }
    });

    y += 6;
    doc.setTextColor(21, 101, 192); doc.setFontSize(12);
    doc.text('Lifestyle Recommendations', 14, y); y += 7;
    doc.setFontSize(9); doc.setTextColor(0);
    d.lifestyleRecommendations.forEach(r => {
        const lines = doc.splitTextToSize('• ' + r, 180);
        doc.text(lines, 14, y); y += lines.length * 5;
        if (y > 270) { doc.addPage(); y = 20; }
    });

    y += 10;
    doc.setFontSize(8); doc.setTextColor(150);
    doc.text('Disclaimer: This report is for informational purposes only and does not constitute medical advice.', 14, y);

    doc.save('Health_Report_' + new Date().toISOString().slice(0, 10) + '.pdf');
}


// async function uploadReport() {
//     const fileInput = document.getElementById("reportFile");

//     if (!fileInput.files.length) {
//         alert("Please select a file");
//         return;
//     }

//     const file = fileInput.files[0];
//     const formData = new FormData();
//     formData.append("file", file);

//     try {
//         const response = await fetch("/api/report/upload", {
//             method: "POST",
//             body: formData
//         });

//         const data = await response.json();

//         if (!data.success) {
//             alert("Failed: " + data.message);
//             return;
//         }

//         alert("Report processed successfully!");

//         autoFillFields(data.extractedValues);
//     } catch (error) {
//         console.error(error);
//         alert("Error processing report");
//     }
// }


async function uploadReport() {
    const fileInput = document.getElementById("reportFile");

    if (!fileInput.files.length) {
        alert("Please select a file");
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append("file", file);

    try {
        const response = await fetch("/api/report/upload", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token
            },
            body: formData
        });

        if (response.status === 401 || response.status === 403) {
            logout();
            return;
        }

        const data = await response.json();

        if (!data.success) {
            alert("Failed: " + data.message);
            return;
        }

        alert("Report processed successfully!");
        autoFillFields(data.extractedValues);
    } catch (error) {
        console.error(error);
        alert("Error processing report");
    }
}

function autoFillFields(values) {
    if (!values) return;

    if (values.hemoglobin !== undefined) document.getElementById("hemoglobin").value = values.hemoglobin;
    if (values.rbc !== undefined) document.getElementById("rbc").value = values.rbc;
    if (values.wbc !== undefined) document.getElementById("wbc").value = values.wbc;
    if (values.platelets !== undefined) document.getElementById("platelets").value = values.platelets;
    if (values.bloodSugar !== undefined) document.getElementById("bloodSugar").value = values.bloodSugar;
    if (values.vitaminD !== undefined) document.getElementById("vitaminD").value = values.vitaminD;
    if (values.vitaminB12 !== undefined) document.getElementById("vitaminB12").value = values.vitaminB12;
    if (values.iron !== undefined) document.getElementById("iron").value = values.iron;
    if (values.calcium !== undefined) document.getElementById("calcium").value = values.calcium;
    if (values.cholesterol !== undefined) document.getElementById("cholesterol").value = values.cholesterol;
    if (values.tsh !== undefined) document.getElementById("tsh").value = values.tsh;
}
