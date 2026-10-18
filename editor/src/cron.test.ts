import { describe, expect, it } from "vitest";
import { describeCron, isCronShape, nextCronRun } from "./cron";

describe("describeCron", () => {
  it("says the common reset periods in words", () => {
    expect(describeCron("0 4 * * *", "fr")).toBe("tous les jours à 4 h");
    expect(describeCron("30 4 * * *", "en")).toBe("every day at 04:30");
    expect(describeCron("0 0 * * 1", "fr")).toBe("chaque lundi à 0 h");
    expect(describeCron("0 0 * * 7", "en")).toBe("every Sunday at 00:00");
    expect(describeCron("15 6 1 * *", "fr")).toBe("le 1er de chaque mois à 6 h 15");
    expect(describeCron("0 */6 * * *", "en")).toBe("every 6 hours, at minute 0");
    expect(describeCron("*/30 * * * *", "fr")).toBe("toutes les 30 minutes");
  });

  it("returns null for what it cannot say, and checks the shape", () => {
    expect(describeCron("0 4 * 1 *", "en")).toBeNull();
    expect(describeCron("0 4 1-5 * *", "en")).toBeNull();
    expect(describeCron("0 4 * *", "en")).toBeNull();
    expect(isCronShape("0 4 * * *")).toBe(true);
    expect(isCronShape("0 0 4 * * ?")).toBe(false);
  });
});

describe("nextCronRun", () => {
  // Saturday 18 October 2026, 03:30 in the test's local clock.
  const from = new Date(2026, 9, 18, 3, 30);
  const at = (d: Date | null) =>
    d &&
    `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, "0")}`;

  it("finds the next run of the common shapes", () => {
    expect(at(nextCronRun("0 4 * * *", from))).toBe("2026-10-18 4:00");
    expect(at(nextCronRun("0 3 * * *", from))).toBe("2026-10-19 3:00");
    expect(at(nextCronRun("0 0 * * 1", from))).toBe("2026-10-19 0:00");
    expect(at(nextCronRun("0 0 * * 7", from))).toBe("2026-10-25 0:00");
    expect(at(nextCronRun("15 6 1 * *", from))).toBe("2026-11-1 6:15");
    expect(at(nextCronRun("*/20 * * * *", from))).toBe("2026-10-18 3:40");
    expect(at(nextCronRun("0 */6 * * *", from))).toBe("2026-10-18 6:00");
    expect(at(nextCronRun("0 4 1-5 * 1,3", from))).toBe("2026-10-19 4:00");
  });

  it("returns null for what it cannot read", () => {
    expect(nextCronRun("0 4 * *", from)).toBeNull();
    expect(nextCronRun("0 25 * * *", from)).toBeNull();
    expect(nextCronRun("x 4 * * *", from)).toBeNull();
    expect(nextCronRun("0 4 31 2 *", from)).toBeNull();
  });
});
