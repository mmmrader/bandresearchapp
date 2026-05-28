import crypto from "node:crypto";
import path from "node:path";
import { fileURLToPath } from "node:url";
import bcrypt from "bcryptjs";
import express from "express";
import { createClient } from "@supabase/supabase-js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const app = express();
const port = process.env.PORT || 3000;

const requiredEnv = ["SUPABASE_URL", "SUPABASE_SERVICE_ROLE_KEY", "ADMIN_EMAIL", "SESSION_SECRET"];
const missingEnv = requiredEnv.filter((key) => !process.env[key]);
if (!process.env.ADMIN_PASSWORD_HASH && !process.env.ADMIN_PASSWORD) {
  missingEnv.push("ADMIN_PASSWORD_HASH або ADMIN_PASSWORD");
}

const supabase = missingEnv.length
  ? null
  : createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY, {
      auth: { autoRefreshToken: false, persistSession: false }
    });

const tableConfig = {
  bands: {
    label: "Гурти",
    primaryKey: ["id"],
    search: ["name", "slug", "country", "city"],
    order: "name",
    columns: {
      id: "uuid",
      name: "text",
      slug: "text",
      genres: "json",
      description: "longtext",
      avatar_url: "url",
      cover_url: "url",
      youtube_url: "url",
      country: "text",
      city: "text",
      formed_year: "number",
      manager_id: "uuid",
      followers_count: "number",
      plays_count: "number"
    }
  },
  tracks: {
    label: "Треки",
    primaryKey: ["id"],
    search: ["title", "lyrics"],
    order: "title",
    columns: {
      id: "uuid",
      band_id: "uuid",
      release_id: "uuid",
      title: "text",
      duration_sec: "number",
      audio_url: "url",
      cover_url: "url",
      lyrics: "longtext",
      track_number: "number",
      plays_count: "number"
    }
  },
  releases: {
    label: "Релізи",
    primaryKey: ["id"],
    search: ["title", "release_type"],
    order: "title",
    columns: {
      id: "uuid",
      band_id: "uuid",
      title: "text",
      release_type: "text",
      release_year: "number",
      cover_url: "url",
      tracks_count: "number"
    }
  },
  videos: {
    label: "Відео",
    primaryKey: ["id"],
    search: ["title", "youtube_id"],
    order: "title",
    columns: {
      id: "uuid",
      band_id: "uuid",
      title: "text",
      youtube_id: "text",
      thumbnail_url: "url",
      duration_sec: "number",
      views_count: "number"
    }
  },
  profiles: {
    label: "Профілі",
    primaryKey: ["id"],
    search: ["display_name", "location", "instrument", "bio"],
    order: "display_name",
    columns: {
      id: "uuid",
      display_name: "text",
      avatar_url: "url",
      bio: "longtext",
      social_link: "url",
      music_genres: "json",
      instrument: "text",
      experience: "text",
      location: "text",
      youtube_link: "url",
      audio_link: "url"
    }
  },
  follows: {
    label: "Підписки",
    primaryKey: ["user_id", "band_id"],
    search: ["user_id", "band_id"],
    columns: {
      user_id: "uuid",
      band_id: "uuid"
    }
  },
  playlists: {
    label: "Плейлісти",
    primaryKey: ["id"],
    search: ["name", "description"],
    order: "name",
    columns: {
      id: "uuid",
      user_id: "uuid",
      name: "text",
      description: "longtext",
      cover_url: "url",
      is_public: "boolean"
    }
  },
  playlist_tracks: {
    label: "Треки плейлістів",
    primaryKey: ["playlist_id", "track_id"],
    search: ["playlist_id", "track_id"],
    columns: {
      playlist_id: "uuid",
      track_id: "uuid",
      position: "number"
    }
  },
  history: {
    label: "Історія прослуховувань",
    primaryKey: ["id"],
    search: ["user_id", "track_id"],
    order: "listened_at",
    descending: true,
    columns: {
      id: "uuid",
      user_id: "uuid",
      track_id: "uuid",
      listened_at: "datetime"
    }
  },
  band_events: {
    label: "Події та новини",
    primaryKey: ["id"],
    search: ["title", "description", "type", "city", "venue"],
    order: "created_at",
    descending: true,
    columns: {
      id: "uuid",
      band_id: "uuid",
      band_name: "text",
      title: "text",
      description: "longtext",
      type: "text",
      event_date: "datetime",
      venue: "text",
      city: "text",
      cover_url: "url",
      smart_link: "url",
      spotify_url: "url",
      apple_music_url: "url",
      youtube_music_url: "url",
      likes_count: "number",
      comments_count: "number",
      rsvp_count: "number",
      created_at: "datetime"
    }
  },
  event_comments: {
    label: "Коментарі подій",
    primaryKey: ["id"],
    search: ["text", "author_name"],
    order: "created_at",
    descending: true,
    columns: {
      id: "uuid",
      event_id: "uuid",
      user_id: "uuid",
      author_name: "text",
      text: "longtext",
      created_at: "datetime"
    }
  },
  event_likes: {
    label: "Лайки подій",
    primaryKey: ["event_id", "user_id"],
    search: ["event_id", "user_id"],
    columns: {
      event_id: "uuid",
      user_id: "uuid"
    }
  },
  event_rsvps: {
    label: "RSVP подій",
    primaryKey: ["event_id", "user_id"],
    search: ["event_id", "user_id"],
    columns: {
      event_id: "uuid",
      user_id: "uuid"
    }
  },
  vacancies: {
    label: "Вакансії",
    primaryKey: ["id"],
    search: ["instrument", "description", "city"],
    order: "created_at",
    descending: true,
    columns: {
      id: "uuid",
      band_id: "uuid",
      instrument: "text",
      description: "longtext",
      city: "text",
      is_active: "boolean",
      created_at: "datetime"
    }
  },
  applications: {
    label: "Заявки",
    primaryKey: ["id"],
    search: ["message", "status"],
    order: "created_at",
    descending: true,
    columns: {
      id: "uuid",
      vacancy_id: "uuid",
      user_id: "uuid",
      message: "longtext",
      status: "text",
      created_at: "datetime"
    }
  },
  notifications: {
    label: "Сповіщення",
    primaryKey: ["id"],
    search: ["title", "body", "type"],
    order: "created_at",
    descending: true,
    columns: {
      id: "uuid",
      user_id: "uuid",
      type: "text",
      title: "text",
      body: "longtext",
      is_read: "boolean",
      created_at: "datetime"
    }
  }
};

app.disable("x-powered-by");
app.use(express.json({ limit: "1mb" }));
app.use(express.static(path.join(__dirname, "public"), { extensions: ["html"] }));

function parseCookies(header = "") {
  return Object.fromEntries(
    header
      .split(";")
      .map((part) => part.trim())
      .filter(Boolean)
      .map((part) => {
        const index = part.indexOf("=");
        return [decodeURIComponent(part.slice(0, index)), decodeURIComponent(part.slice(index + 1))];
      })
  );
}

function sign(value) {
  return crypto.createHmac("sha256", process.env.SESSION_SECRET).update(value).digest("base64url");
}

function makeSession(email) {
  const payload = Buffer.from(JSON.stringify({ email, exp: Date.now() + 12 * 60 * 60 * 1000 })).toString("base64url");
  return `${payload}.${sign(payload)}`;
}

function readSession(req) {
  const token = parseCookies(req.headers.cookie).admin_session;
  if (!token) return null;
  const [payload, signature] = token.split(".");
  if (!payload || signature !== sign(payload)) return null;
  try {
    const session = JSON.parse(Buffer.from(payload, "base64url").toString("utf8"));
    if (session.exp < Date.now() || session.email !== process.env.ADMIN_EMAIL) return null;
    return session;
  } catch {
    return null;
  }
}

function requireReady(req, res, next) {
  if (missingEnv.length) {
    return res.status(500).json({ error: `Не задані змінні: ${missingEnv.join(", ")}` });
  }
  return next();
}

function requireAuth(req, res, next) {
  const session = readSession(req);
  if (!session) return res.status(401).json({ error: "Потрібен вхід в адмін-панель" });
  req.admin = session;
  return next();
}

function getTable(name) {
  const config = tableConfig[name];
  if (!config) {
    const error = new Error("Невідома таблиця");
    error.status = 404;
    throw error;
  }
  return config;
}

function encodeKey(config, row) {
  const key = {};
  for (const column of config.primaryKey) key[column] = row[column];
  return Buffer.from(JSON.stringify(key)).toString("base64url");
}

function decodeKey(key) {
  return JSON.parse(Buffer.from(key, "base64url").toString("utf8"));
}

function cleanPayload(config, payload, { forInsert = false } = {}) {
  const clean = {};
  for (const [column, type] of Object.entries(config.columns)) {
    if (!(column in payload)) continue;
    const value = payload[column];
    if (value === "" || value === undefined) {
      clean[column] = null;
    } else if (type === "number") {
      clean[column] = Number(value);
    } else if (type === "boolean") {
      clean[column] = Boolean(value);
    } else if (type === "json") {
      clean[column] = Array.isArray(value) || typeof value === "object" ? value : JSON.parse(value);
    } else {
      clean[column] = value;
    }
  }

  if (!forInsert) {
    for (const key of config.primaryKey) delete clean[key];
  }

  return clean;
}

function applyKeyFilter(query, keyValues) {
  return Object.entries(keyValues).reduce((builder, [column, value]) => builder.eq(column, value), query);
}

async function tableCount(table) {
  const { count, error } = await supabase.from(table).select("*", { count: "exact", head: true });
  if (error) throw error;
  return count || 0;
}

app.get("/api/session", requireReady, (req, res) => {
  res.json({ authenticated: Boolean(readSession(req)), adminEmail: process.env.ADMIN_EMAIL });
});

app.post("/api/login", requireReady, async (req, res) => {
  const { email, password } = req.body || {};
  const emailMatches = String(email || "").trim().toLowerCase() === process.env.ADMIN_EMAIL.toLowerCase();
  const hash = process.env.ADMIN_PASSWORD_HASH;
  const plain = process.env.ADMIN_PASSWORD;
  const passwordInput = Buffer.from(String(password || ""));
  const plainPassword = Buffer.from(String(plain || ""));
  const passwordMatches = hash
    ? await bcrypt.compare(String(password || ""), hash)
    : plainPassword.length === passwordInput.length && crypto.timingSafeEqual(passwordInput, plainPassword);

  if (!emailMatches || !passwordMatches) {
    return res.status(401).json({ error: "Неправильна пошта або пароль" });
  }

  const secure = process.env.NODE_ENV === "production" ? "; Secure" : "";
  res.setHeader("Set-Cookie", `admin_session=${makeSession(process.env.ADMIN_EMAIL)}; HttpOnly; Path=/; Max-Age=43200; SameSite=Lax${secure}`);
  res.json({ ok: true });
});

app.post("/api/logout", (req, res) => {
  res.setHeader("Set-Cookie", "admin_session=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax");
  res.json({ ok: true });
});

app.get("/api/config", requireReady, requireAuth, (req, res) => {
  res.json({
    tables: Object.fromEntries(
      Object.entries(tableConfig).map(([name, config]) => [
        name,
        { label: config.label, primaryKey: config.primaryKey, columns: config.columns }
      ])
    )
  });
});

app.get("/api/stats", requireReady, requireAuth, async (req, res, next) => {
  try {
    const countNames = Object.keys(tableConfig);
    const counts = Object.fromEntries(await Promise.all(countNames.map(async (name) => [name, await tableCount(name)])));
    const [{ data: topBands }, { data: topTracks }, { data: recentHistory }, { data: unreadNotifications }, usersResult] =
      await Promise.all([
        supabase.from("bands").select("id,name,followers_count,plays_count").order("plays_count", { ascending: false }).limit(5),
        supabase.from("tracks").select("id,title,band_id,plays_count").order("plays_count", { ascending: false }).limit(5),
        supabase.from("history").select("*").order("listened_at", { ascending: false }).limit(8),
        supabase.from("notifications").select("*").eq("is_read", false).order("created_at", { ascending: false }).limit(8),
        supabase.auth.admin.listUsers({ page: 1, perPage: 1 })
      ]);

    res.json({
      counts,
      authUsers: usersResult?.data?.total || 0,
      topBands: topBands || [],
      topTracks: topTracks || [],
      recentHistory: recentHistory || [],
      unreadNotifications: unreadNotifications || []
    });
  } catch (error) {
    next(error);
  }
});

app.get("/api/table/:table", requireReady, requireAuth, async (req, res, next) => {
  try {
    const config = getTable(req.params.table);
    const limit = Math.min(Number(req.query.limit || 50), 200);
    const page = Math.max(Number(req.query.page || 1), 1);
    const from = (page - 1) * limit;
    const to = from + limit - 1;

    let query = supabase.from(req.params.table).select("*", { count: "exact" }).range(from, to);
    if (config.order) query = query.order(config.order, { ascending: !config.descending });
    if (req.query.search && config.search?.length) {
      const term = String(req.query.search).replaceAll("%", "\\%");
      query = query.or(config.search.map((column) => `${column}.ilike.%${term}%`).join(","));
    }

    const { data, count, error } = await query;
    if (error) throw error;
    res.json({ rows: (data || []).map((row) => ({ ...row, __admin_key: encodeKey(config, row) })), count: count || 0, page, limit });
  } catch (error) {
    next(error);
  }
});

app.post("/api/table/:table", requireReady, requireAuth, async (req, res, next) => {
  try {
    const config = getTable(req.params.table);
    const payload = cleanPayload(config, req.body || {}, { forInsert: true });
    const { data, error } = await supabase.from(req.params.table).insert(payload).select().single();
    if (error) throw error;
    res.status(201).json({ row: { ...data, __admin_key: encodeKey(config, data) } });
  } catch (error) {
    next(error);
  }
});

app.patch("/api/table/:table/:key", requireReady, requireAuth, async (req, res, next) => {
  try {
    const config = getTable(req.params.table);
    const payload = cleanPayload(config, req.body || {});
    const key = decodeKey(req.params.key);
    const { data, error } = await applyKeyFilter(supabase.from(req.params.table).update(payload).select(), key).single();
    if (error) throw error;
    res.json({ row: { ...data, __admin_key: encodeKey(config, data) } });
  } catch (error) {
    next(error);
  }
});

app.delete("/api/table/:table/:key", requireReady, requireAuth, async (req, res, next) => {
  try {
    const key = decodeKey(req.params.key);
    const { error } = await applyKeyFilter(supabase.from(req.params.table).delete(), key);
    if (error) throw error;
    res.json({ ok: true });
  } catch (error) {
    next(error);
  }
});

app.get("*", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "index.html"));
});

app.use((error, req, res, next) => {
  console.error(error);
  res.status(error.status || 500).json({ error: error.message || "Помилка сервера" });
});

app.listen(port, () => {
  console.log(`BandResearch admin panel is running on port ${port}`);
});
