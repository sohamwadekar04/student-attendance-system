/* student-app.js – read-only student portal */

const MIN_PCT = 75;
let toastTimer = null;

function showToast(text, type) {
    const toast = document.getElementById('toast');
    toast.textContent = text;
    toast.className = 'toast ' + type;
    toast.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { toast.hidden = true; }, 3500);
}

function pctColor(pct, hasData) {
    if (!hasData) return '';
    return pct >= MIN_PCT ? 'ok-color' : 'low-color';
}

async function loadDashboard() {
    try {
        const res = await fetch('/api/student/dashboard');
        if (res.status === 401) {
            window.location.href = '/student-login.html';
            return;
        }
        if (!res.ok) throw new Error('Failed to load data');
        const data = await res.json();

        // Profile
        document.getElementById('s-name').textContent  = data.name;
        document.getElementById('s-roll').textContent  = data.rollNo;
        document.getElementById('s-class').textContent = data.className;

        // Overall stats
        const hasAny = data.totalClassesAll > 0;
        const ovPctEl = document.getElementById('ov-pct');
        ovPctEl.textContent = hasAny ? data.overallPercentage.toFixed(1) + '%' : '—';
        ovPctEl.className   = 'val ' + pctColor(data.overallPercentage, hasAny);

        document.getElementById('ov-total').textContent   = data.totalClassesAll;
        document.getElementById('ov-present').textContent = data.totalPresentAll;

        const shortageCount = data.subjects.filter(s => s.shortage).length;
        const scEl = document.getElementById('ov-shortage-count');
        scEl.textContent = shortageCount;
        scEl.className   = 'val ' + (shortageCount > 0 ? 'low-color' : 'ok-color');

        // Subject table
        const tbody = document.getElementById('subject-tbody');
        tbody.innerHTML = '';
        let anyRecord = false;

        data.subjects.forEach((s, idx) => {
            const hasData = s.totalClasses > 0;
            if (hasData) anyRecord = true;

            const tr = document.createElement('tr');
            if (s.shortage) tr.className = 'low';

            // Index
            const tdIdx = document.createElement('td');
            tdIdx.textContent = idx + 1;
            tr.appendChild(tdIdx);

            // Subject name
            const tdSub = document.createElement('td');
            tdSub.textContent = s.subject;
            tdSub.style.fontWeight = '600';
            tr.appendChild(tdSub);

            // Total
            const tdTotal = document.createElement('td');
            tdTotal.textContent = hasData ? s.totalClasses : '—';
            tr.appendChild(tdTotal);

            // Present
            const tdPres = document.createElement('td');
            tdPres.textContent = hasData ? s.presentCount : '—';
            tr.appendChild(tdPres);

            // Percentage bar + value
            const tdPct = document.createElement('td');
            if (hasData) {
                const bar  = document.createElement('span');
                bar.className = 'bar';
                const fill = document.createElement('span');
                fill.className = s.shortage ? 'low' : '';
                fill.style.width = Math.min(s.percentage, 100) + '%';
                bar.appendChild(fill);
                tdPct.appendChild(bar);
                tdPct.appendChild(document.createTextNode(s.percentage.toFixed(1) + '%'));
            } else {
                tdPct.textContent = '—';
                tdPct.style.color = 'var(--muted)';
            }
            tr.appendChild(tdPct);

            // Status badge
            const tdStatus = document.createElement('td');
            if (!hasData) {
                tdStatus.innerHTML = '<span class="no-data-badge">No data</span>';
            } else if (s.shortage) {
                tdStatus.innerHTML = '<span class="shortage-badge">⚠ Shortage</span>';
            } else {
                tdStatus.innerHTML = '<span class="ok-badge">✓ OK</span>';
            }
            tr.appendChild(tdStatus);

            tbody.appendChild(tr);
        });

        document.getElementById('no-data-msg').hidden = anyRecord;
        document.getElementById('loading-msg').style.display     = 'none';
        document.getElementById('dashboard-content').style.display = 'grid';

    } catch (err) {
        showToast('Failed to load attendance data. Please refresh.', 'error');
        document.getElementById('loading-msg').textContent = '❌ Failed to load data. Please refresh.';
    }
}

// Logout
document.getElementById('s-logout-btn').addEventListener('click', async () => {
    try {
        await fetch('/api/auth/student-logout', { method: 'POST' });
    } catch (e) { /* ignore */ }
    window.location.href = '/student-login.html';
});

// Load data on page load
loadDashboard();
