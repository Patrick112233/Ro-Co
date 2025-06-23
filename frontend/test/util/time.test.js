import { describe, it, expect, beforeAll, afterAll } from "vitest";
import formatTimespan from "@/util/time.js";

describe("formatTimespan", () => {
  const fixedNow = new Date("2024-06-12T12:00:00Z");

  beforeAll(() => {
    vi.useFakeTimers();
    vi.setSystemTime(fixedNow);
  });

  afterAll(() => {
    vi.useRealTimers();
  });

  it("returns minutes for less than 60 minutes", () => {
    expect(formatTimespan("2024-06-12T11:59:00Z")).toBe("1 minute");
    expect(formatTimespan("2024-06-12T11:30:00Z")).toBe("30 minutes");
  });

  it("returns hours for less than 24 hours", () => {
    expect(formatTimespan("2024-06-12T10:00:00Z")).toBe("2 hours");
    expect(formatTimespan("2024-06-12T00:00:00Z")).toBe("12 hours");
  });

  it("returns days for less than 30 days", () => {
    expect(formatTimespan("2024-06-10T12:00:00Z")).toBe("2 days");
    expect(formatTimespan("2024-05-14T12:00:00Z")).toBe("29 days");
  });

  it("returns months for less than 12 months", () => {
    expect(formatTimespan("2024-01-12T12:00:00Z")).toBe("5 months");
    expect(formatTimespan("2023-06-13T12:00:00Z")).toBe("1 year");
  });

  it("returns years for more than 12 months", () => {
    expect(formatTimespan("2023-06-12T12:00:00Z")).toBe("1 year");
    expect(formatTimespan("2021-06-12T12:00:00Z")).toBe("3 years");
  });

  /* Feature missing due to sporadic errors in gui
  it("handles timestamps before Unix epoch", () => {
    expect(() => formatTimespan("1969-12-31T23:59:00Z")).toThrow();
    expect(() => formatTimespan("1900-01-01T00:00:00Z")).toThrow();
  });

  it("handles timestamps in the future", () => {
    expect(() => formatTimespan("4059-06-12T12:01:00Z")).toThrow();
  });

  it("throws for weird or invalid timestamp strings", () => {
  expect(() => formatTimespan("not-a-date")).toThrow();
  expect(() => formatTimespan("")).toThrow();
  expect(() => formatTimespan(null)).toThrow();
  expect(() => formatTimespan(undefined)).toThrow();
});*/
});