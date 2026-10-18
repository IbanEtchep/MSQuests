/**
 * A UNIX cron (5 fields, what ArtisanQuests reads with cron-utils) said in words, for the
 * common shapes of a period reset: daily, weekly, monthly, every N hours or minutes.
 * Anything else returns null and the view shows the expression as is.
 */

const DAYS: Record<string, string[]> = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"],
};

const NUM = /^\d+$/;

function time(lang: string, h: number, m: number): string {
  const mm = String(m).padStart(2, "0");
  return lang === "fr"
    ? m === 0
      ? `${h} h`
      : `${h} h ${mm}`
    : `${String(h).padStart(2, "0")}:${mm}`;
}

function inRange(v: string, min: number, max: number): boolean {
  return NUM.test(v) && Number(v) >= min && Number(v) <= max;
}

/** Whether `expr` has the 5 fields of a UNIX cron (contents are the server's to judge). */
export function isCronShape(expr: string): boolean {
  return expr.trim().split(/\s+/).length === 5;
}

export function describeCron(expr: string, lang: string): string | null {
  const fr = lang === "fr";
  const parts = expr.trim().split(/\s+/);
  if (parts.length !== 5) return null;
  const [min, hour, dom, mon, dow] = parts as [string, string, string, string, string];
  if (mon !== "*") return null;

  const every = /^\*\/(\d+)$/;
  if (dom === "*" && dow === "*") {
    if (inRange(min, 0, 59) && inRange(hour, 0, 23)) {
      return fr
        ? `tous les jours à ${time(lang, +hour, +min)}`
        : `every day at ${time(lang, +hour, +min)}`;
    }
    const everyHours = every.exec(hour);
    if (inRange(min, 0, 59) && everyHours) {
      return fr
        ? `toutes les ${everyHours[1]} h, à la minute ${min}`
        : `every ${everyHours[1]} hours, at minute ${min}`;
    }
    if (inRange(min, 0, 59) && hour === "*") {
      return fr ? `toutes les heures, à la minute ${min}` : `every hour, at minute ${min}`;
    }
    const everyMinutes = every.exec(min);
    if (everyMinutes && hour === "*") {
      return fr ? `toutes les ${everyMinutes[1]} minutes` : `every ${everyMinutes[1]} minutes`;
    }
    return null;
  }
  if (!inRange(min, 0, 59) || !inRange(hour, 0, 23)) return null;
  const at = time(lang, +hour, +min);
  if (dom === "*" && inRange(dow, 0, 7)) {
    const day = DAYS[fr ? "fr" : "en"]![Number(dow) % 7];
    return fr ? `chaque ${day} à ${at}` : `every ${day} at ${at}`;
  }
  if (dow === "*" && inRange(dom, 1, 31)) {
    return fr
      ? `le ${dom === "1" ? "1er" : dom} de chaque mois à ${at}`
      : `on day ${dom} of each month at ${at}`;
  }
  return null;
}

/** The values a cron field allows, or null when it cannot be read. */
function field(expr: string, min: number, max: number): Set<number> | null {
  const out = new Set<number>();
  for (const part of expr.split(",")) {
    const m = /^(\*|(\d+)(?:-(\d+))?)(?:\/(\d+))?$/.exec(part);
    if (!m) return null;
    const step = m[4] ? Number(m[4]) : 1;
    const from = m[1] === "*" ? min : Number(m[2]);
    const to = m[1] === "*" ? max : m[3] ? Number(m[3]) : m[4] ? max : from;
    if (step < 1 || from < min || to > max || from > to) return null;
    for (let v = from; v <= to; v += step) out.add(v);
  }
  return out;
}

/**
 * The next time `expr` fires after `from`, in the browser's clock (the server reads the cron in its
 * own zone: the same when both are set to the same zone). Null when the cron cannot be read or
 * never fires within a year.
 */
export function nextCronRun(expr: string, from: Date): Date | null {
  const parts = expr.trim().split(/\s+/);
  if (parts.length !== 5) return null;
  const minutes = field(parts[0]!, 0, 59);
  const hours = field(parts[1]!, 0, 23);
  const doms = field(parts[2]!, 1, 31);
  const months = field(parts[3]!, 1, 12);
  const dows = field(parts[4]!, 0, 7);
  if (!minutes || !hours || !doms || !months || !dows) return null;
  if (dows.has(7)) dows.add(0);
  // UNIX: when both day fields are restricted, either one matching is enough.
  const domAny = parts[2] === "*";
  const dowAny = parts[4] === "*";
  const dayMatches = (d: Date) =>
    domAny && dowAny
      ? true
      : domAny
        ? dows.has(d.getDay())
        : dowAny
          ? doms.has(d.getDate())
          : doms.has(d.getDate()) || dows.has(d.getDay());

  const t = new Date(from.getTime());
  t.setSeconds(0, 0);
  t.setMinutes(t.getMinutes() + 1);
  const limit = from.getTime() + 366 * 24 * 3600 * 1000;
  while (t.getTime() <= limit) {
    if (!months.has(t.getMonth() + 1) || !dayMatches(t)) {
      t.setDate(t.getDate() + 1);
      t.setHours(0, 0, 0, 0);
      continue;
    }
    if (!hours.has(t.getHours())) {
      t.setHours(t.getHours() + 1, 0, 0, 0);
      continue;
    }
    if (minutes.has(t.getMinutes())) return t;
    t.setMinutes(t.getMinutes() + 1);
  }
  return null;
}
