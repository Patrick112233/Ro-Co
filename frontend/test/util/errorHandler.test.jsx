import { describe, it, expect, vi, beforeEach } from "vitest";
import handleErrorLogout from "@/util/ErrorHandler.jsx";
import axios from "@/util/axios.js";
import * as refresh from "@/auth/refresh.js";

vi.mock("@/util/axios.js", () => ({
  default: { post: vi.fn() }
}));
vi.mock("@/auth/refresh.js", () => ({
  getRefresh: vi.fn()
}));

describe("handleErrorLogout", () => {
  let navigate, signOut, authHeader;

  beforeEach(() => {
    vi.clearAllMocks();
    navigate = vi.fn();
    signOut = vi.fn();
    authHeader = "Bearer testtoken";
  });

  it("calls axios.post, signOut, and navigate on regular call", async () => {
    refresh.getRefresh.mockReturnValue("refresh-token");
    axios.post.mockResolvedValue({});

    handleErrorLogout("Some error", navigate, signOut, authHeader);

    // Wait for any promises to resolve
    await Promise.resolve();

    expect(axios.post).toHaveBeenCalledWith(
      "/auth/logout",
      { refreshToken: "refresh-token" },
      expect.objectContaining({
        headers: expect.any(Object),
        withCredentials: true,
        timeout: 3000
      })
    );
    expect(signOut).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  it("still calls signOut and navigate if axios.post throws", async () => {
    refresh.getRefresh.mockReturnValue("refresh-token");
    axios.post.mockRejectedValue(new Error("Network error"));

    handleErrorLogout("Some error", navigate, signOut, authHeader);

    // Wait for any promises to resolve
    await Promise.resolve();

    expect(axios.post).toHaveBeenCalled();
    expect(signOut).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith("/login", { replace: true });
  });
});