const state = {
  tables: {},
  currentView: "dashboard",
  currentTable: null,
  page: 1,
  limit: 50,
  count: 0,
  rows: [],
  editing: null
};

const el = {
  login: document.querySelector("#login"),
  app: document.querySelector("#app"),
  loginForm: document.querySelector("#loginForm"),
  loginError: document.querySelector("#loginError"),
  logout: document.querySelector("#logout"),
  refresh: document.querySelector("#refresh"),
  tableNav: document.querySelector("#tableNav"),
  pageTitle: document.querySelector("#pageTitle"),
  eyebrow: document.querySelector("#eyebrow"),
  dashboardView: document.querySelector("#dashboardView"),
  tableView: document.querySelector("#tableView"),
  statCards: document.querySelector("#statCards"),
  topBands: document.querySelector("#topBands"),
  topTracks: document.querySelector("#topTracks"),
  recentHistory: document.querySelector("#recentHistory"),
  unreadNotifications: document.querySelector("#unreadNotifications"),
  search: document.querySelector("#search"),
  createRow: document.querySelector("#createRow"),
  tableHead: document.querySelector("#tableHead"),
  tableBody: document.querySelector("#tableBody"),
  prevPage: document.querySelector("#prevPage"),
  nextPage: document.querySelector("#nextPage"),
  pageInfo: document.querySelector("#pageInfo"),
  editor: document.querySelector("#editor"),
  editorTitle: document.querySelector("#editorTitle"),
  editorFields: document.querySelector("#editorFields"),
  editorError: document.querySelector("#editorError"),
  deleteRow: document.querySelector("#deleteRow"),
  saveRow: document.querySelector("#saveRow"),
  toast: document.querySelector("#toast")
};

async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(payload.error || "Запит не виконано");
  return payload;
}

function showToast(message) {
  el.toast.textContent = message;
  el.toast.hidden = false;
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => (el.toast.hidden = true), 2600);
}

function prettyValue(value) {
  if (value === null || value === undefined || value === "") return "—";
  if (Array.isArray(value)) return value.join(", ");
  if (typeof value === "object") return JSON.stringify(value);
  if (typeof value === "boolean") return value ? "Так" : "Ні";
  return String(value);
}

function shortValue(value) {
  const text = prettyValue(value);
  return text.length > 120 ? `${text.slice(0, 117)}...` : text;
}

function setScreen(authenticated) {
  el.login.hidden = authenticated;
  el.app.hidden = !authenticated;
}

async function init() {
  const session = await api("/api/session").catch(() => ({ authenticated: false }));
  setScreen(session.authenticated);
  if (!session.authenticated) return;
  const config = await api("/api/config");
  state.tables = config.tables;
  renderNav();
  await loadDashboard();
}

function renderNav() {
  el.tableNav.innerHTML = "";
  for (const [name, config] of Object.entries(state.tables)) {
    const button = document.createElement("button");
    button.className = "nav-item";
    button.dataset.table = name;
    button.textContent = config.label;
    button.addEventListener("click", () => openTable(name));
    el.tableNav.append(button);
  }
}

function activateNav(target) {
  document.querySelectorAll(".nav-item").forEach((item) => item.classList.remove("active"));
  const selector = target === "dashboard" ? "[data-view='dashboard']" : `[data-table='${target}']`;
  document.querySelector(selector)?.classList.add("active");
}

async function loadDashboard() {
  state.currentView = "dashboard";
  state.currentTable = null;
  activateNav("dashboard");
  el.dashboardView.hidden = false;
  el.tableView.hidden = true;
  el.pageTitle.textContent = "Статистика";
  el.eyebrow.textContent = "Огляд";

  const stats = await api("/api/stats");
  const priority = ["authUsers", "bands", "tracks", "profiles", "playlists", "history", "notifications", "applications"];
  const labels = { authUsers: "Користувачі Auth", ...Object.fromEntries(Object.entries(state.tables).map(([key, value]) => [key, value.label])) };
  el.statCards.innerHTML = priority
    .filter((key) => key === "authUsers" || key in stats.counts)
    .map((key) => `<article class="stat-card"><strong>${key === "authUsers" ? stats.authUsers : stats.counts[key]}</strong><span>${labels[key]}</span></article>`)
    .join("");

  renderList(el.topBands, stats.topBands, (row) => [row.name, `${row.plays_count || 0} прослуховувань · ${row.followers_count || 0} підписників`]);
  renderList(el.topTracks, stats.topTracks, (row) => [row.title, `${row.plays_count || 0} прослуховувань`]);
  renderList(el.recentHistory, stats.recentHistory, (row) => [row.track_id, row.listened_at || row.user_id]);
  renderList(el.unreadNotifications, stats.unreadNotifications, (row) => [row.title, row.body]);
}

function renderList(container, rows, mapper) {
  if (!rows?.length) {
    container.innerHTML = `<div class="list-row"><span>Поки що немає даних</span><small>—</small></div>`;
    return;
  }
  container.innerHTML = rows
    .map((row) => {
      const [title, meta] = mapper(row);
      return `<div class="list-row"><span>${escapeHtml(shortValue(title))}</span><small>${escapeHtml(shortValue(meta))}</small></div>`;
    })
    .join("");
}

async function openTable(name) {
  state.currentView = "table";
  state.currentTable = name;
  state.page = 1;
  el.search.value = "";
  activateNav(name);
  el.dashboardView.hidden = true;
  el.tableView.hidden = false;
  el.pageTitle.textContent = state.tables[name].label;
  el.eyebrow.textContent = name;
  await loadTable();
}

async function loadTable() {
  const params = new URLSearchParams({ page: state.page, limit: state.limit, search: el.search.value.trim() });
  const data = await api(`/api/table/${state.currentTable}?${params}`);
  state.rows = data.rows;
  state.count = data.count;
  renderTable();
}

function renderTable() {
  const config = state.tables[state.currentTable];
  const columns = Object.keys(config.columns);
  el.tableHead.innerHTML = `<tr>${columns.map((column) => `<th>${escapeHtml(column)}</th>`).join("")}<th></th></tr>`;
  el.tableBody.innerHTML = state.rows
    .map((row, index) => {
      const cells = columns
        .map((column) => {
          const value = row[column];
          const content = column.endsWith("_id") || column === "id" ? `<code>${escapeHtml(shortValue(value))}</code>` : escapeHtml(shortValue(value));
          return `<td>${content}</td>`;
        })
        .join("");
      return `<tr>${cells}<td><button class="row-action" data-index="${index}">Редагувати</button></td></tr>`;
    })
    .join("");

  el.tableBody.querySelectorAll(".row-action").forEach((button) => {
    button.addEventListener("click", () => openEditor(state.rows[Number(button.dataset.index)]));
  });

  const totalPages = Math.max(Math.ceil(state.count / state.limit), 1);
  el.pageInfo.textContent = `Сторінка ${state.page} з ${totalPages} · ${state.count} записів`;
  el.prevPage.disabled = state.page <= 1;
  el.nextPage.disabled = state.page >= totalPages;
}

function openEditor(row = null) {
  const config = state.tables[state.currentTable];
  state.editing = row;
  el.editorError.textContent = "";
  el.editorTitle.textContent = row ? "Редагування запису" : "Новий запис";
  el.deleteRow.hidden = !row;
  el.editorFields.innerHTML = "";

  for (const [column, type] of Object.entries(config.columns)) {
    const label = document.createElement("label");
    label.dataset.column = column;
    label.dataset.type = type;
    if (type === "longtext" || type === "json") label.dataset.wide = "true";
    label.textContent = column;

    const input = document.createElement(type === "longtext" || type === "json" ? "textarea" : "input");
    input.name = column;
    input.disabled = Boolean(row && config.primaryKey.includes(column));
    if (type === "number") input.type = "number";
    else if (type === "boolean") input.type = "checkbox";
    else if (type === "datetime") input.type = "text";
    else input.type = type === "url" ? "url" : "text";

    const value = row?.[column];
    if (type === "boolean") input.checked = Boolean(value);
    else input.value = type === "json" && value !== undefined && value !== null ? JSON.stringify(value, null, 2) : value ?? "";
    label.append(input);
    el.editorFields.append(label);
  }

  el.editor.showModal();
}

function readEditorPayload() {
  const payload = {};
  for (const label of el.editorFields.querySelectorAll("label")) {
    const column = label.dataset.column;
    const type = label.dataset.type;
    const input = label.querySelector("input, textarea");
    if (input.disabled) continue;
    if (type === "boolean") payload[column] = input.checked;
    else if (type === "json" && input.value.trim()) payload[column] = JSON.parse(input.value);
    else payload[column] = input.value.trim();
  }
  return payload;
}

async function saveEditor() {
  try {
    el.editorError.textContent = "";
    const payload = readEditorPayload();
    if (state.editing) {
      await api(`/api/table/${state.currentTable}/${state.editing.__admin_key}`, { method: "PATCH", body: JSON.stringify(payload) });
    } else {
      await api(`/api/table/${state.currentTable}`, { method: "POST", body: JSON.stringify(payload) });
    }
    el.editor.close();
    await loadTable();
    showToast("Зміни збережено");
  } catch (error) {
    el.editorError.textContent = error.message;
  }
}

async function deleteEditor() {
  if (!state.editing) return;
  const ok = confirm("Видалити цей запис?");
  if (!ok) return;
  try {
    await api(`/api/table/${state.currentTable}/${state.editing.__admin_key}`, { method: "DELETE" });
    el.editor.close();
    await loadTable();
    showToast("Запис видалено");
  } catch (error) {
    el.editorError.textContent = error.message;
  }
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

el.loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  el.loginError.textContent = "";
  const form = new FormData(el.loginForm);
  try {
    await api("/api/login", {
      method: "POST",
      body: JSON.stringify({ email: form.get("email"), password: form.get("password") })
    });
    await init();
  } catch (error) {
    el.loginError.textContent = error.message;
  }
});

document.querySelector("[data-view='dashboard']").addEventListener("click", loadDashboard);
el.logout.addEventListener("click", async () => {
  await api("/api/logout", { method: "POST" });
  setScreen(false);
});
el.refresh.addEventListener("click", () => (state.currentView === "dashboard" ? loadDashboard() : loadTable()));
el.createRow.addEventListener("click", () => openEditor());
el.saveRow.addEventListener("click", saveEditor);
el.deleteRow.addEventListener("click", deleteEditor);
el.prevPage.addEventListener("click", async () => {
  state.page -= 1;
  await loadTable();
});
el.nextPage.addEventListener("click", async () => {
  state.page += 1;
  await loadTable();
});
el.search.addEventListener("input", () => {
  clearTimeout(el.search.timer);
  el.search.timer = setTimeout(async () => {
    state.page = 1;
    await loadTable();
  }, 260);
});

init();
