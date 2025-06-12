import { describe, it, expect, vi, beforeEach } from "vitest";
import axios from "@/util/axios.js";
import { getRefresh, refreshApiCallback } from "@/auth/refresh.js";

vi.mock("@/util/axios.js", () => ({
  default: { post: vi.fn() }
}));

describe("refresh.js", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset cookies before each test
    document.cookie = "";
  });

  it("getRefresh loads the value of the _auth_refresh cookie", () => {
    document.cookie = "_auth_refresh=my_refresh_token";
    expect(getRefresh()).toBe("my_refresh_token");
  });
  
    it("getRefresh returns empty string if cookie is missing", () => {
    // Remove the cookie explicitly
    document.cookie = "_auth_refresh=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "other_cookie=abc";
    expect(getRefresh()).toBe("");
    });
  it("refreshApiCallback delivers success on successful axios post", async () => {
    axios.post.mockResolvedValueOnce({
      data: {
        token: "new_token",
        tokenExpiresIn: 1234,
        refreshTokenExpiresIn: 5678
      }
    });

    const result = await refreshApiCallback({ refreshToken: "my_refresh_token" });
    expect(result).toEqual({
      isSuccess: true,
      newAuthToken: "new_token",
      newAuthTokenExpireIn: 1234,
      newRefreshTokenExpiresIn: 5678
    });
    expect(axios.post).toHaveBeenCalledWith(
      "auth/refresh",
      { refreshToken: "my_refresh_token" },
      { headers: { Authorization: "Bearer my_refresh_token" } }
    );
  });

  it("refreshApiCallback delivers success=false on axios post throw", async () => {
    axios.post.mockRejectedValueOnce(new Error("fail"));
    const result = await refreshApiCallback({ refreshToken: "bad_token" });
    expect(result).toEqual({ isSuccess: false });
  });
});