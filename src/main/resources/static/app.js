const STUDENTS_URL = "/api/students";
const ATTENDANCE_URL = "/api/attendance";
const DASHBOARD_URL = "/api/dashboard/stats";
const MIN_PERCENTAGE = 75; // students below this are flagged

let lastReport = [];
let toastTimer = null;

/* ================= helpers ================= */

async function api(url, options) {
    const response = await fetch(url, options);
    if (response.status === 401) {
        window.location.href = "/login";
        return null;
    }
    if (response.status === 204) return null;
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        const error = new Error(body.message || "Request failed");
        error.errors = body.errors;
        throw error;
    }
    return body;
}

function showToast(text, type) {
    const toast = document.getElementById("toast");
    toast.textContent = text;
    toast.className = "toast " + type;
    toast.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { toast.hidden = true; }, 3500);
}

function makeCell(text) {
    const td = document.createElement("td");
    td.textContent = text; // textContent prevents HTML injection
    return td;
}

function todayString() {
    const d = new Date();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return d.getFullYear() + "-" + month + "-" + day;
}

/* ================= tabs ================= */

function switchToTab(tabId) {
    document.querySelectorAll(".tab").forEach(b => {
        b.classList.toggle("active", b.dataset.tab === tabId);
    });
    document.querySelectorAll(".tab-panel").forEach(p => {
        p.hidden = p.id !== tabId;
    });

    if (tabId === "dashboard-tab") {
        loadDashboard();
    } else if (tabId === "students-tab") {
        loadStudents();
    } else if (tabId === "mark-tab") {
        loadSubjects();
    } else if (tabId === "report-tab") {
        loadSubjects();
    }
}

document.querySelectorAll(".tab").forEach(button => {
    button.addEventListener("click", () => {
        switchToTab(button.dataset.tab);
    });
});

document.querySelectorAll(".quick-nav-btn").forEach(button => {
    button.addEventListener("click", () => {
        switchToTab(button.dataset.target);
    });
});

/* ================= dashboard tab ================= */

async function loadDashboard() {
    try {
        const stats = await api(DASHBOARD_URL);
        if (!stats) return;

        document.getElementById("stat-total-students").textContent = stats.totalStudents;
        document.getElementById("stat-avg-attendance").textContent = stats.averageAttendance.toFixed(1) + "%";
        document.getElementById("stat-shortage-count").textContent = stats.shortageCount;
        document.getElementById("stat-highest-attendance").textContent = stats.highestAttendancePercentage.toFixed(1) + "%";
        document.getElementById("stat-highest-name").textContent = stats.highestAttendanceStudent || "-";

        const tbody = document.getElementById("dashboard-shortage-body");
        tbody.innerHTML = "";
        const shortageStudents = stats.shortageStudents || [];
        document.getElementById("dashboard-no-shortage").hidden = shortageStudents.length > 0;

        shortageStudents.forEach(r => {
            const tr = document.createElement("tr");
            tr.className = "low";
            tr.appendChild(makeCell(r.rollNo));
            tr.appendChild(makeCell(r.name));
            tr.appendChild(makeCell(r.totalClasses));
            tr.appendChild(makeCell(r.presentCount));

            const pctCell = document.createElement("td");
            const bar = document.createElement("span");
            bar.className = "bar";
            const fill = document.createElement("span");
            fill.className = "low";
            fill.style.width = Math.min(r.percentage, 100) + "%";
            bar.appendChild(fill);
            pctCell.appendChild(bar);
            pctCell.appendChild(document.createTextNode(r.percentage.toFixed(1) + "%"));
            tr.appendChild(pctCell);

            const status = document.createElement("td");
            status.textContent = "Shortage";
            status.className = "status-low";
            tr.appendChild(status);

            tbody.appendChild(tr);
        });
    } catch (err) {
        showToast("Failed to load dashboard metrics", "error");
    }
}

/* ================= students tab ================= */

const studentFields = ["rollNo", "name", "email", "className"];

function clearStudentErrors() {
    studentFields.forEach(f => { document.getElementById("err-" + f).textContent = ""; });
}

function showStudentErrors(errors) {
    Object.keys(errors).forEach(field => {
        const el = document.getElementById("err-" + field);
        if (el) el.textContent = errors[field];
    });
}

function validateStudent(data) {
    const errors = {};
    if (!data.rollNo) errors.rollNo = "Roll number is required";
    if (!data.name) errors.name = "Name is required";
    if (!data.email) errors.email = "Email is required";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) errors.email = "Enter a valid email address";
    if (!data.className) errors.className = "Class is required";
    return errors;
}

async function loadStudents() {
    try {
        const students = await api(STUDENTS_URL);
        const body = document.getElementById("student-body");
        body.innerHTML = "";
        document.getElementById("student-count").textContent = students.length;
        document.getElementById("no-students").hidden = students.length > 0;

        students.forEach(s => {
            const tr = document.createElement("tr");
            tr.appendChild(makeCell(s.rollNo));
            tr.appendChild(makeCell(s.name));
            tr.appendChild(makeCell(s.email));
            tr.appendChild(makeCell(s.className));

            const td = document.createElement("td");
            const del = document.createElement("button");
            del.className = "btn small danger";
            del.textContent = "Delete";
            del.addEventListener("click", () => deleteStudent(s));
            td.appendChild(del);
            tr.appendChild(td);
            body.appendChild(tr);
        });
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function addStudent() {
    clearStudentErrors();
    const data = {};
    studentFields.forEach(f => { data[f] = document.getElementById(f).value.trim(); });

    const errors = validateStudent(data);
    if (Object.keys(errors).length > 0) {
        showStudentErrors(errors);
        return;
    }

    try {
        await api(STUDENTS_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });
        studentFields.forEach(f => { document.getElementById(f).value = ""; });
        showToast("Student added", "success");
        loadStudents();
    } catch (err) {
        if (err.errors) showStudentErrors(err.errors);
        showToast(err.message, "error");
    }
}

async function deleteStudent(student) {
    if (!confirm("Delete " + student.name + " and all of their attendance records?")) return;
    try {
        await api(STUDENTS_URL + "/" + student.id, { method: "DELETE" });
        showToast("Student deleted", "success");
        loadStudents();
    } catch (err) {
        showToast(err.message, "error");
    }
}

/* ================= mark attendance tab ================= */

async function loadSubjects() {
    try {
        const subjects = await api(ATTENDANCE_URL + "/subjects");
        const list = document.getElementById("subject-list");
        list.innerHTML = "";
        subjects.forEach(s => {
            const option = document.createElement("option");
            option.value = s;
            list.appendChild(option);
        });
    } catch (err) {
        /* suggestions are optional, ignore errors */
    }
}

async function loadSheet() {
    const date = document.getElementById("att-date").value;
    const subject = document.getElementById("att-subject").value.trim();
    if (!date || !subject) {
        showToast("Please select a date and enter a subject", "error");
        return;
    }

    try {
        const students = await api(STUDENTS_URL);
        const existing = await api(ATTENDANCE_URL + "?date=" + encodeURIComponent(date)
                + "&subject=" + encodeURIComponent(subject));

        // if attendance was already taken, show the saved values
        const saved = new Map();
        existing.forEach(r => saved.set(r.studentId, r.present));

        const body = document.getElementById("sheet-body");
        body.innerHTML = "";
        students.forEach(s => {
            const tr = document.createElement("tr");
            tr.appendChild(makeCell(s.rollNo));
            tr.appendChild(makeCell(s.name));

            const td = document.createElement("td");
            const box = document.createElement("input");
            box.type = "checkbox";
            box.dataset.studentId = s.id;
            box.checked = saved.has(s.id) ? saved.get(s.id) : true;
            td.appendChild(box);
            tr.appendChild(td);
            body.appendChild(tr);
        });

        document.getElementById("sheet-card").hidden = false;
        if (existing.length > 0) showToast("Attendance already taken for this date. You can edit it.", "success");
    } catch (err) {
        showToast(err.message, "error");
    }
}

function setAllChecked(value) {
    document.querySelectorAll("#sheet-body input[type=checkbox]").forEach(b => { b.checked = value; });
}

async function submitAttendance() {
    const date = document.getElementById("att-date").value;
    const subject = document.getElementById("att-subject").value.trim();
    const records = Array.from(document.querySelectorAll("#sheet-body input[type=checkbox]"))
            .map(box => ({ studentId: Number(box.dataset.studentId), present: box.checked }));

    if (records.length === 0) {
        showToast("There are no students to mark", "error");
        return;
    }

    try {
        await api(ATTENDANCE_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ date: date, subject: subject, records: records })
        });
        showToast("Attendance saved for " + subject + " on " + date, "success");
    } catch (err) {
        showToast(err.message, "error");
    }
}

/* ================= report tab ================= */

async function generateReport() {
    const subject = document.getElementById("report-subject").value.trim();
    const url = subject ? ATTENDANCE_URL + "/report?subject=" + encodeURIComponent(subject)
                        : ATTENDANCE_URL + "/report";
    try {
        lastReport = await api(url);
        renderReport(lastReport, subject);
    } catch (err) {
        showToast(err.message, "error");
    }
}

function renderReport(rows, subject) {
    const body = document.getElementById("report-body");
    body.innerHTML = "";
    let lowCount = 0;

    rows.forEach(r => {
        const low = r.totalClasses > 0 && r.percentage < MIN_PERCENTAGE;
        if (low) lowCount++;

        const tr = document.createElement("tr");
        if (low) tr.className = "low";
        tr.appendChild(makeCell(r.rollNo));
        tr.appendChild(makeCell(r.name));
        tr.appendChild(makeCell(r.totalClasses));
        tr.appendChild(makeCell(r.presentCount));

        const pctCell = document.createElement("td");
        const bar = document.createElement("span");
        bar.className = "bar";
        const fill = document.createElement("span");
        if (low) fill.className = "low";
        fill.style.width = Math.min(r.percentage, 100) + "%";
        bar.appendChild(fill);
        pctCell.appendChild(bar);
        pctCell.appendChild(document.createTextNode(r.percentage.toFixed(1) + "%"));
        tr.appendChild(pctCell);

        const status = document.createElement("td");
        if (r.totalClasses === 0) {
            status.textContent = "No data";
        } else if (low) {
            status.textContent = "Shortage";
            status.className = "status-low";
        } else {
            status.textContent = "OK";
            status.className = "status-ok";
        }
        tr.appendChild(status);
        body.appendChild(tr);
    });

    const label = subject ? "Subject: " + subject : "All subjects";
    document.getElementById("report-summary").textContent =
            label + " | Students: " + rows.length + " | Below " + MIN_PERCENTAGE + "%: " + lowCount;
    document.getElementById("report-card").hidden = false;
    document.getElementById("csv-btn").hidden = rows.length === 0;
}

function downloadCsv() {
    const lines = ["Roll No,Name,Total Classes,Present,Percentage"];
    lastReport.forEach(r => {
        const name = '"' + r.name.replace(/"/g, '""') + '"';
        lines.push([r.rollNo, name, r.totalClasses, r.presentCount, r.percentage].join(","));
    });
    const blob = new Blob([lines.join("\n")], { type: "text/csv" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "attendance-report.csv";
    link.click();
    URL.revokeObjectURL(link.href);
}

/* ================= start ================= */

document.getElementById("add-student-btn").addEventListener("click", addStudent);
document.getElementById("load-sheet-btn").addEventListener("click", loadSheet);
document.getElementById("all-present-btn").addEventListener("click", () => setAllChecked(true));
document.getElementById("all-absent-btn").addEventListener("click", () => setAllChecked(false));
document.getElementById("submit-att-btn").addEventListener("click", submitAttendance);
document.getElementById("report-btn").addEventListener("click", generateReport);
document.getElementById("csv-btn").addEventListener("click", downloadCsv);

const logoutBtn = document.getElementById("logout-btn");
if (logoutBtn) {
    logoutBtn.addEventListener("click", async () => {
        try {
            await fetch("/api/auth/logout", { method: "POST" });
        } catch (e) {
            // ignore network error
        }
        window.location.href = "/login";
    });
}

document.getElementById("att-date").value = todayString();
loadDashboard();
