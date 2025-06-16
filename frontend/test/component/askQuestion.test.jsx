import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import AskQuestion from "@/component/askQuestion.jsx";
import axios from "@/util/axios.js";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import handleErrorLogout from "@/util/ErrorHandler.jsx";

// Mock hooks
vi.mock('react-auth-kit/hooks/useSignOut', () => ({
  default: () => vi.fn(),
}));
vi.mock('react-auth-kit/hooks/useAuthHeader', () => ({
  default: () => "Bearer testtoken",
}));
vi.mock('react-auth-kit/hooks/useAuthUser', () => ({
  default: () => ({ uid: "123" }),
}));
vi.mock('@/util/ErrorHandler.jsx', () => ({
  __esModule: true,
  default: vi.fn(),
}));

describe("AskQuestion", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should post a question and close the popup (normal case)", async () => {
    // Mock axios.post to return a successful response
    vi.spyOn(axios, "post").mockResolvedValueOnce({
      status: 201,
      data: {
        id: 1,
        title: "Test Title",
        description: "Test Description",
        createdAt: "2024-01-01",
        answered: false,
        author: { id: "123", username: "Alice" }
      }
    });

    // Mock refreshHook
    const refreshHook = { refreshHook: vi.fn() };

    render(
      <MemoryRouter>
        <AskQuestion {...refreshHook} />
      </MemoryRouter>
    );

    // Open the popup (Assumes  there is only one button)
    fireEvent.click(screen.getByRole("button"));

    fireEvent.change(screen.getByTestId("subject"), { target: { value: "Test Title" } });
    fireEvent.change(screen.getByTestId("question"), { target: { value: "Test Description" } });

    //Trigger post action
    fireEvent.click(screen.getByText("Post"));

    // Wait for popup to close (it should not be in the document)
    await waitFor(() => {
      expect(screen.queryByText("Ask your Question")).toBeNull();
    });

    // Ensure refreshHook was called
    expect(refreshHook.refreshHook).toHaveBeenCalledWith(expect.objectContaining({
      title: "Test Title",
      description: "Test Description",
      author: expect.objectContaining({ username: "Alice" })
    }));
  });

  
  it("should call handleErrorLogout if axios.post throws", async () => {
    vi.spyOn(axios, "post").mockRejectedValueOnce(new Error("Network Error"));
    const refreshHook = { refreshHook: vi.fn() };

    render(
      <MemoryRouter>
        <AskQuestion {...refreshHook} />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole("button"));
    fireEvent.change(screen.getByTestId("subject"), { target: { value: "Test Title" } });
    fireEvent.change(screen.getByTestId("question"), { target: { value: "Test Description" } });
    fireEvent.click(screen.getByText("Post"));

    // Wait for error handler to be called
    await waitFor(() => {
      expect(handleErrorLogout).toHaveBeenCalled();
    });
  });
});